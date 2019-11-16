package com.antalex.persistence.entity;

import com.antalex.model.enums.StatusType;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_TARIFF_PLAN")
@Data
@Entity
public class TariffPlanEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_NAME")
    private String name;
    @Column(name = "C_STATUS")
    private StatusType status;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_TARIFF_PLAN_REF")
    private List<RateEntity> rates = new ArrayList<>();

}
