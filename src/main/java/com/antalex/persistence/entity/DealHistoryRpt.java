package com.antalex.persistence.entity;

import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Table(name = "VW_RPT_DEAL_HISTORY")
@Data
@Entity
public class DealHistoryRpt implements History {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "UNO")
    private String uno;
    @Column(name = "CLASS_CODE")
    private String classCode;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DEAL_TYPE")
    private EventType type;
    @Column(name = "PRICE")
    private BigDecimal price;
    @Column(name = "STATUS")
    private DealStatusType status;
}