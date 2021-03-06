package com.antalex.persistence.entity;

import com.antalex.model.enums.ExecType;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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
    @Column(name = "C_ORDER_NUM")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_SEQ")
    @SequenceGenerator(name = "ORDER_SEQ", sequenceName = "ORDER_SEQ")
    private Long orderNum;
    @Column(name = "C_DATE_TIME")
    private Date date;
    @Column(name = "C_TRANS_ID")
    private String transId;
    @Column(name = "C_EXEC_TYPE")
    private ExecType execType;
    @OneToOne
    @JoinColumn(name = "C_SEC_REF")
    private ClassSecEntity sec;
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
