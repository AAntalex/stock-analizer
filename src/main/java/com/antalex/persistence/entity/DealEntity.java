package com.antalex.persistence.entity;

import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_DEAL")
@Data
@Entity
public class DealEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_SEC_REF")
    private Long secId;
    @Column(name = "C_DEAL_TYPE")
    private EventType type;
    @Column(name = "C_EVENT")
    private EventEntity event;
    @Column(name = "C_UNO")
    private String uno;
    @Column(name = "C_PRICE")
    private BigDecimal price;
}
