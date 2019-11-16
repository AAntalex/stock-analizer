package com.antalex.persistence.entity;

import com.antalex.model.enums.RateType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "Z#AAA_RATE")
@Data
@Entity
public class RateEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_NAME")
    private String name;
    @Column(name = "C_RATE")
    private BigDecimal rate;
    @Column(name = "C_MAX_PRICE")
    private BigDecimal maxPrice;
    @Column(name = "C_MIN_PRICE")
    private BigDecimal minPrice;
    @Column(name = "C_TARIFF_PLAN_REF")
    private Long tariffPlanId;
    @Column(name = "C_TYPE")
    private RateType type;
}
