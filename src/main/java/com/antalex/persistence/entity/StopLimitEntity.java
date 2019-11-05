package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "Z#STOP_LIMIT")
@Data
@Entity
public class StopLimitEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @Column(name = "C_STOP_PRICE")
    private BigDecimal stopPrice;
    @Column(name = "C_VOLUME")
    private Double volume;
}
