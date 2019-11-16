package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "Z#TAKE_PROFIT")
@Data
@Entity
public class TakeProfitEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @Column(name = "C_GAP")
    private BigDecimal gap;
    @Column(name = "C_SPREAD")
    private BigDecimal spread;
    @Column(name = "C_VOLUME")
    private Double volume;
    @Column(name = "C_ACTIVE")
    private Boolean active;
}
