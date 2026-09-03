package com.chrizlove.vortexpay.operations.settlement;

import com.chrizlove.vortexpay.common.dto.SettlementBankDetails;
import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.EventAggregateType;
import com.chrizlove.vortexpay.common.enums.SettlementStatus;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.merchant.api.MerchantLookupService;
import com.chrizlove.vortexpay.operations.entity.Settlement;
import com.chrizlove.vortexpay.operations.entity.SettlementPayment;
import com.chrizlove.vortexpay.operations.entity.SettlementPaymentId;
import com.chrizlove.vortexpay.operations.repository.SettlementPaymentRepository;
import com.chrizlove.vortexpay.operations.repository.SettlementRepository;
import com.chrizlove.vortexpay.operations.settlement.dto.BankTransferResult;
import com.chrizlove.vortexpay.payment.api.PaymentLookupService;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.outbox.OutboxEventPublisher;
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

    private final PaymentLookupService paymentLookupService;
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final MerchantLookupService merchantLookupService;
    private final BankTransferProcessor bankTransferProcessor;
    private final OutboxEventPublisher outboxEventPublisher;

    private static final double FEE_RATE = 0.02;
    private static final double GST_RATE = 0.18;

    @Transactional
    public void processForMerchant(UUID merchantId, LocalDate settlementDate){


            List<Payment> unsettledPayments = paymentLookupService.findUnsettledCapturedPayments(merchantId);
            if (unsettledPayments.isEmpty()) {
                return;
            }

            log.info("Processing {} unsettled payments for merchantId: {} on date: {}", unsettledPayments.size(), merchantId,  settlementDate);

            //adds all money for a currency
            Money gross = unsettledPayments.stream()
                    .map(Payment::getAmount)
                    .reduce(Money::add)
                    .orElseThrow();

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
            for (Payment payment : unsettledPayments) {
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(), payment.getId()))
                        .settlement(settlement)
                        .build());
            }

            settlementPaymentRepository.saveAll(links);

            SettlementBankDetails settlementBankDetails = merchantLookupService.getSettlementBankDetails(merchantId);

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
