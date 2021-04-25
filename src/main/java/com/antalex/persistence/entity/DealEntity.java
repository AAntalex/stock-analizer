package com.antalex.persistence.entity;

import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "Z#AAA_DEAL")
@Data
@Entity
public class DealEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_TRADE_NUM")
    private Long tradeNum;
    @Column(name = "C_TRANS_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRANS_SEQ")
    @SequenceGenerator(name = "TRANS_SEQ", sequenceName = "TRANS_SEQ")
    private String transId;
    @Column(name = "C_DATE_TIME")
    private Date date;
    @Column(name = "C_UNO")
    private String uno;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @OneToOne
    @JoinColumn(name = "C_ORDER_REF")
    private OrderEntity order;
    @OneToOne
    @JoinColumn(name = "C_SEC_REF")
    private ClassSecEntity sec;
    @Column(name = "C_QTY")
    private Double volume;
    @Column(name = "C_DEAL_TYPE")
    private EventType type;
    @Column(name = "C_BALANCE")
    private Double balance;
    @Column(name = "C_RESULT")
    private BigDecimal result;
    @Column(name = "C_QUIT")
    private String quit;
}
