package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "Z#FT_MONEY")
@Data
@Entity
public class CurrencyEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "C_CODE_ISO")
    private String codeISO;
    @Column(name = "C_CUR_SHORT")
    private String curShort;
}
