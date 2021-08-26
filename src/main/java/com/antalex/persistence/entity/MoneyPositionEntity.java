package com.antalex.persistence.entity;

import com.antalex.model.enums.StatusType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "Z#AAA_MONEY")
@Data
@Entity
public class MoneyPositionEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @OneToOne
    @JoinColumn(name = "C_CUR")
    private CurrencyEntity cur;
    @Column(name = "C_STATUS")
    private StatusType status;
    @Column(name = "C_AMOUNT")
    private BigDecimal amount;
    @Column(name = "C_AVLB_AMOUNT")
    private BigDecimal availableAmount;
    @Column(name = "C_AVLB_PERCENT")
    private BigDecimal availablePercent;
    @Column(name = "C_USED_AMOUNT")
    private BigDecimal usedAmount;
    @Column(name = "C_ACCOUNT")
    private Long account;
}
