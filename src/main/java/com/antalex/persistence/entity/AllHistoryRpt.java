package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "VW_RPT_ALL_TRADES_AND_QUOTES")
@Data
@Entity
public class AllHistoryRpt implements History{
    @Id
    @Column(name = "UNO")
    private String uno;
    @Column(name = "CLASS_CODE")
    private String classCode;
    @Column(name = "CODE")
    private String code;
    @Column(name = "QUOTES")
    private String quotes;
    @Column(name = "TRADE_NUM")
    private Long tradeNum;
    @Column(name = "QTY")
    private Double qty;
    @Column(name = "BID_FLAG")
    private Boolean bidFlag;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "LOTSIZE")
    private Integer lotSize;
    @Column(name = "SEC_SCALE")
    private Integer scale;
    @OneToOne
    @JoinColumn(name = "SEC_REF")
    private ClassSecEntity sec;
}
