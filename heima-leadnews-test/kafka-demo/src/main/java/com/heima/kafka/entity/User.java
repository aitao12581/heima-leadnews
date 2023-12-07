package com.heima.kafka.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private String username;

    private Integer age;

    private String sex;
}
