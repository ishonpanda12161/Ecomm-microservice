package com.ecommerce.app.exception;

import java.time.LocalDateTime;

public class ResourceAlreadyExistsException extends RuntimeException {

    public LocalDateTime timestamp;
    public String resourceName;
    public String attributeName;
    public String attribute;

    public ResourceAlreadyExistsException(String resourceName, String attributeName, String attribute, LocalDateTime timestamp) {
        super(timestamp+" - "+resourceName+" already exists with "+attributeName+": "+attribute);
        this.timestamp = timestamp;
        this.resourceName = resourceName;
        this.attributeName = attributeName;
        this.attribute = attribute;
    }
}
