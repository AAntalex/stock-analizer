package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;

@Table(name = "Z#EVENT_TRIGGER")
@Data
@Entity
public class TriggerEntity {
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id")
    @SequenceGenerator(name = "seq_id", sequenceName = "SEQ_ID")
    private Long id;
    @Column(name = "C_CODE")
    private String code;
    @Column(name = "C_CAPTION")
    private String caption;
    @Column(name = "C_CONDITION")
    private String condition;
}
