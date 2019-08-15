package com.antalex.persistence.entity;

import com.antalex.model.IndicatorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_INDICATOR")
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_DESCRIPTION")
    private String description;
    @Column(name = "C_EXPRESSION")
    private String expression;
    @Column(name = "C_TYPE")
    private IndicatorType type;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "C_INDICATOR")
    private List<IndicatorPeriodEntity> periods = new ArrayList<>();
}
