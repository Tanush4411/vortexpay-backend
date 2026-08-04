package com.chrizlove.vortexpay.payment.simulator;

import com.chrizlove.vortexpay.common.enums.ChaosMode;
import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankCallBackSimulator {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    public void processCallbacks(){
        LocalDateTime globalWindow=LocalDateTime.now().minusSeconds(1);

        List<Payment> candidates= paymentRepository.findByPaymentStatusAndCreatedAtBefore(PaymentStatus.AUTHORIZING,globalWindow);

        log.info("Simulating payments for {} payments", candidates.size());
        if(candidates.isEmpty()) return;

        for(Payment payment:candidates){
            simulateCallback(payment);
        }
    }


    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig =simulatorConfig.configFor(payment.getPaymentMethod());

        LocalDateTime dueAt=dueAt(payment,methodSimulatorConfig);

        if(LocalDateTime.now().isBefore(dueAt)){
            return;
        }

        ChaosMode chaosMode=simulatorConfig.getChaosMode();

        switch (chaosMode){
            case SUCCESS -> resolve(payment,true);
            case FAILURE -> resolve(payment,false);
            case TIMEOUT -> {
                log.debug("BankCallBackSimulator: Payment Timed Out");
            }
            case NORMAL,SLOW-> resolve(payment,shouldApprove(payment,methodSimulatorConfig));
        }
    }

    private void resolve(Payment payment, boolean approve){
    if(approve){
        String bankRef = "SIM_BANK_REF"+ RandomizerUtil.randomBase64(8);
        paymentService.resolveAuthorization(payment.getId(),true,bankRef,null,null);
    }
    else{
        paymentService.resolveAuthorization(payment.getId(),false,null,"SIMULATED_BANK_ERROR_CODE","Simulated Bank Decline");
    }
    }

    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){
        int bucket=Math.abs(payment.getId().hashCode())%100;
        return bucket<methodSimulatorConfig.getSuccessRate();
    }

    private LocalDateTime dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig){
        int range=methodSimulatorConfig.getMaxDelaySeconds()-methodSimulatorConfig.getMinDelaySeconds();
        int delaySeconds=methodSimulatorConfig.getMinDelaySeconds()+Math.abs(payment.getId().hashCode())%(range+1);

        if(simulatorConfig.getChaosMode()== ChaosMode.SLOW){
            delaySeconds*=2;
        }

        return payment.getCreatedAt().plusSeconds(delaySeconds);
    }
}
