package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;

@Table(name = "Z#EVENT_TRIGGERS")
@Data
@Entity
public class EventTriggerEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_EVENT")
    private Long eventId;
    @Column(name = "C_ORDER")
    private Integer order;
    @OneToOne
    @JoinColumn(name = "C_TRIGGER")
    private TriggerEntity trigger;
}
