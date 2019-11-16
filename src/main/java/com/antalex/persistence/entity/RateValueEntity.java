package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "Z#AAA_RATE_VALUE")
@Data
@Entity
public class RateValueEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_PARENT")
    private Long parent;
    @Column(name = "C_VALUE")
    private BigDecimal value;
    @OneToOne
    @JoinColumn(name = "C_RATE_REF")
    private RateEntity rate;
}
