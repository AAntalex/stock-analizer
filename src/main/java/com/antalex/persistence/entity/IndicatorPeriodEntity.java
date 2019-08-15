package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;

@Table(name = "Z#AAA_IND_PERIODS")
@Data
@Entity
public class IndicatorPeriodEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_PERIOD")
    private Integer period;
    @Column(name = "C_INDICATOR")
    private Long indicatorId;
}
