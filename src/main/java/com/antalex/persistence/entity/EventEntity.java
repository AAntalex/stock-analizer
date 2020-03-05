package com.antalex.persistence.entity;

import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Table(name = "Z#EVENT_ORDER")
@Data
@Entity
public class EventEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_STATUS")
    private StatusType status;
    @Column(name = "C_EVENT_TYPE")
    private EventType type;
    @OneToOne
    @JoinColumn(name = "C_TAKE_PROFIT")
    private TakeProfitTuneEntity takeProfit;
    @OneToOne
    @JoinColumn(name = "C_STOP_LIMIT")
    private StopLimitTuneEntity stopLimit;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "C_EVENT")
    private List<EventTriggerEntity> triggers;
}
