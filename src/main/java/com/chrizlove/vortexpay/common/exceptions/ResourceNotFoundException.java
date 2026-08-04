package com.chrizlove.vortexpay.common.exceptions;

import lombok.Getter;

import java.util.UUID;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final Object identifier;


    public ResourceNotFoundException(String resourceName, UUID identifier) {
        super(resourceName+"not found: "+identifier);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(resourceName+"not found: "+identifier);
        this.resourceName = resourceName;
        this.identifier = identifier;
    }
}
