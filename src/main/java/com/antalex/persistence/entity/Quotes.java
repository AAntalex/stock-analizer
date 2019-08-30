package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "VW_RPT_ALL_QUOTES")
@Data
@Entity
public class Quotes {
    @Id
    @Column(name = "UNO")
    private String uno;
    @Column(name = "CLASS_CODE")
    private String classCode;
    @Column(name = "CODE")
    private String code;
    @Column(name = "QUOTES")
    private String quotes;
    @Column(name = "LOTSIZE")
    private Integer lotSize;
}
