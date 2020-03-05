package com.antalex.persistence.entity;

import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_ORDER")
@Data
@Entity
public class OrderEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_SEC_REF")
    private Long secId;
    @Column(name = "C_LOT_SIZE")
    private Integer lotSize;
    @Column(name = "C_SCALE")
    private Integer scale;
    @Column(name = "C_ORDER_TYPE")
    private EventType type;
    @Column(name = "C_UNO")
    private String uno;
    @Column(name = "C_PRICE")
    private BigDecimal price;
    @Column(name = "C_LIMIT_PRICE")
    private BigDecimal limitPrice;
    @Column(name = "C_MAX_PRICE")
    private BigDecimal maxPrice;
    @Column(name = "C_MIN_PRICE")
    private BigDecimal minPrice;
    @Column(name = "C_RESULT")
    private BigDecimal result;
    @Column(name = "C_VOLUME")
    private Double volume;
    @Column(name = "C_CAPTION")
    private String caption;
    @Column(name = "C_STATUS")
    private OrderStatusType status;
    @OneToOne
    @JoinColumn(name = "C_EVENT")
    private EventEntity event;
    @OneToOne
    @JoinColumn(name = "C_MAIN")
    private OrderEntity main;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "C_TAKE_PROFIT")
    private TakeProfitEntity takeProfit;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "C_STOP_LIMIT")
    private StopLimitEntity stopLimit;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_PARENT")
    private List<IndicatorValueEntity> indicators = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_PARENT")
    private List<TraceValueEntity> traceValues = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_PARENT")
    private List<RateValueEntity> rates = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_ORDER_REF")
    private List<OrderHistoryEntity> history = new ArrayList<>();
    @Transient
    private List<BigDecimal> boolTriggerValues = new ArrayList<>();
    @Transient
    private List<BigDecimal> deltaTriggerValues = new ArrayList<>();
}
