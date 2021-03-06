package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "Z#AAA_CLASS_SEC")
@Data
@Entity
public class ClassSecEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_SHORTNAME")
    private String shortName;
    @Column(name = "C_LONGNAME")
    private String longName;
    @Column(name = "C_CLASS_CODE")
    private String classCode;
    @Column(name = "C_SEC_SCALE")
    private Integer scale;
    @Column(name = "C_LOTSIZE")
    private Integer lotSize;
}
