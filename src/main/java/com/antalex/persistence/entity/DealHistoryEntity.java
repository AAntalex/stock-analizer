package com.antalex.persistence.entity;

import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "Z#AAA_DEAL_HISTORY")
@Data
@Entity
public class DealHistoryEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_DATE")
    private Date date;
    @Column(name = "C_UNO")
    private String uno;
    @Column(name = "C_DEAL_TYPE")
    private EventType type;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @Column(name = "C_STATUS")
    private DealStatusType status;
    @Column(name = "C_DEAL")
    private Long deal;
}
