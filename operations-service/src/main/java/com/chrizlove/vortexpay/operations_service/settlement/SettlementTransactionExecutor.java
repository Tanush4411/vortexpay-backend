package com.chrizlove.vortexpay.operations_service.settlement;

import com.chrizlove.vortexpay.common_lib.dto.PaymentSettlementView;
import com.chrizlove.vortexpay.common_lib.dto.SettlementBankDetails;
import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.common_lib.enums.EventAggregateType;
import com.chrizlove.vortexpay.common_lib.enums.SettlementStatus;
import com.chrizlove.vortexpay.common_lib.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.operations_service.client.MerchantServiceClient;
import com.chrizlove.vortexpay.operations_service.client.PaymentServiceClient;
import com.chrizlove.vortexpay.operations_service.entity.Settlement;
import com.chrizlove.vortexpay.operations_service.entity.SettlementPayment;
import com.chrizlove.vortexpay.operations_service.entity.SettlementPaymentId;
import com.chrizlove.vortexpay.operations_service.outbox.OutboxEventPublisher;
import com.chrizlove.vortexpay.operations_service.repository.SettlementPaymentRepository;
import com.chrizlove.vortexpay.operations_service.repository.SettlementRepository;
import com.chrizlove.vortexpay.operations_service.settlement.dto.BankTransferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementTransactionExecutor {

    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final BankTransferProcessor bankTransferProcessor;
    private final OutboxEventPublisher outboxEventPublisher;
    private final MerchantServiceClient  merchantServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    private static final double FEE_RATE = 0.02;
    private static final double GST_RATE = 0.18;

    @Transactional
    public void processForMerchant(UUID merchantId, LocalDate settlementDate){

            List<PaymentSettlementView> unsettledPayments = paymentServiceClient.findUnsettledCapturedPayments(merchantId);
            if (unsettledPayments.isEmpty()) {
                return;
            }

            log.info("Processing {} unsettled payments for merchantId: {} on date: {}", unsettledPayments.size(), merchantId,  settlementDate);

            //adds all money for a currency
            Integer grossAmount = unsettledPayments.stream()
                    .map(PaymentSettlementView::amountUnits)
                    .reduce(Integer::sum)
                    .orElse(0);

            Money gross= Money.of(grossAmount, unsettledPayments.getFirst().currency());
            int fee = Math.toIntExact(Math.round(gross.getAmountUnits() * FEE_RATE));
            int gst = Math.toIntExact(Math.round(fee * GST_RATE));
            Money feeAmount = Money.of(fee, gross.getCurrency());
            Money gstAmount = Money.of(gst, gross.getCurrency());
            Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

            Settlement settlement = Settlement.builder()
                    .merchantId(merchantId)
                    .grossAmount(gross)
                    .gstAmount(gstAmount)
                    .netAmount(netAmount)
                    .feeAmount(feeAmount)
                    .settlementStatus(SettlementStatus.INITIATED)
                    .build();

            settlementRepository.save(settlement);

        try {
            List<SettlementPayment> links = new ArrayList<>();
            for (PaymentSettlementView payment : unsettledPayments) {
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(), payment.paymentId()))
                        .settlement(settlement)
                        .build());
            }

            settlementPaymentRepository.saveAll(links);

            SettlementBankDetails settlementBankDetails = merchantServiceClient.getSettlementBankDetails(merchantId);

            //call the bank transfer service to transfer the net amount to merchant settlement bank acc
            BankTransferResult bankTransferResult = bankTransferProcessor.initiate(settlement.getId(), merchantId, netAmount,
                    settlementBankDetails.accountNumber(), settlementBankDetails.ifsc());

            settlement.setSettlementStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTransferResult.registrationRef());
        }catch (Exception e){
            log.error("Settlement failed for settlement id: {} on date: {}", settlement.getId(), settlementDate, e);
            settlement.setSettlementStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }
    }

    @Transactional
    public void resolveTransfer(UUID settlementId,String errorCode, String errorDescription){

        Settlement settlement=settlementRepository.findById(settlementId).
                orElseThrow(()-> new ResourceNotFoundException("settlement", settlementId));

        if(settlement.getSettlementStatus()!=SettlementStatus.TRANSFER_PENDING){
            log.info("Settlement resolved, skipping for id: {}",  settlement.getId());
            return;
        }

        if(errorCode == null){
            //success
            settlement.setSettlementStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);

            //marking payments as settled
            List<SettlementPayment> settlementPaymentList= settlementPaymentRepository.findBySettlement(settlement);
            List<UUID> paymentIds = settlementPaymentList.stream().
                    map(SettlementPayment::getId).map(SettlementPaymentId::getPaymentId).toList();
            paymentServiceClient.markSettled(paymentIds);

            log.info("Settlement processed successfully, settlementId: {}", settlement.getId());

            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT, settlementId, "SETTLEMENT_PROCESSED",
                    Map.of("settlementId", settlement.getId()
                    ,"merchantId", settlement.getMerchantId()
                    ,"status", settlement.getSettlementStatus().name()
                            ,"settlementAmount", settlement.getNetAmount().getAmountUnits()
                            ,"settlementCurrency", settlement.getNetAmount().getCurrency()
                    ));
        }
        else{
            //failure
            settlement.setSettlementStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode+" : "+errorDescription);
            settlementRepository.save(settlement);

            log.warn("Settlement failed, settlementId: {}", settlement.getId());

            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT, settlementId, "SETTLEMENT_FAILED",
                    Map.of("settlementId", settlement.getId()
                            ,"merchantId", settlement.getMerchantId()
                            ,"status", settlement.getSettlementStatus().name()
                            ,"settlementAmount", settlement.getNetAmount().getAmountUnits()
                            ,"settlementCurrency", settlement.getNetAmount().getCurrency()
                    ));
        }
    }


}
