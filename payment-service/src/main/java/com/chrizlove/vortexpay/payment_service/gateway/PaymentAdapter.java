package com.chrizlove.vortexpay.payment_service.gateway;


import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    PaymentResult initiate (PaymentRequest paymentRequest);

    PaymentResult capture(UUID paymentId);
}
