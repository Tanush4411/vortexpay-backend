package com.chrizlove.vortexpay.payment.gateway;


import com.chrizlove.vortexpay.payment.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate (PaymentRequest paymentRequest);

    PaymentResult capture(UUID paymentId);
}
