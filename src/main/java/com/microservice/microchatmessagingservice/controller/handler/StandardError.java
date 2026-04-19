package com.microservice.microchatmessagingservice.controller.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardError {

    private LocalDate timestamp;
    private Integer status;
    private String error;
    private String path;

}
