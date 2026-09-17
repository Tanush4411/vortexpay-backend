package com.chrizlove.vortexpay.common_lib.exceptions;

import lombok.Getter;

@Getter
public class InvalidStateTransitionException extends RuntimeException {

    private final String fromState;
    private final String event;

    public InvalidStateTransitionException(String fromState, String event) {
        super("Invalid state transition from " + fromState + " with event " + event);
        this.fromState=fromState;
        this.event=event;
    }
}
