package com.antalex.persistence.entity;

import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "Z#AAA_ORDER_HIST")
@Data
@Entity
public class OrderHistoryEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_DATE")
    private Date date;
    @Column(name = "C_UNO")
    private String uno;
    @Column(name = "C_ORDER_TYPE")
    private EventType type;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @Column(name = "C_STATUS")
    private OrderStatusType status;
    @Column(name = "C_ORDER_REF")
    private Long order;
}
