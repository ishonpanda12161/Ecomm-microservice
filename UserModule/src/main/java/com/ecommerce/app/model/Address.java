package com.ecommerce.app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "address")
public class Address {

    @Id
    private String id;

    private String userId;

    @Size(min = 2,message = "Street Name contain at least 2 characters.")
    private String street;

    @Size(min = 2,message = "City Name contain at least 2 characters.")
    private String city;

    @Size(min = 2,message = "State Name contain at least 2 characters.")
    private String state;

    @Size(min = 3,message = "Country Name contain at least 3 characters.")
    private String country;

    @NotBlank(message = "Zipcode cannot be blank.")
    @Size(min = 6,message = "Zipcode Name contain 6 characters.")
    private String zipcode;

}
