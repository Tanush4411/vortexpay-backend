package com.chrizlove.vortexpay.payment.api;

import com.chrizlove.vortexpay.payment.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {

    List<Payment> findUnsettledCapturedPayments(UUID merchantId);

}
