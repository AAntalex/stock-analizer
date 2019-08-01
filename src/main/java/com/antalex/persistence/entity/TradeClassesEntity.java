package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_CLASSES")
@Data
@Entity
public class TradeClassesEntity {
    @Id
    @Column(name = "C_CLASS_CODE")
    private String code;
    @Column(name = "C_CLASS_NAME")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name="C_CLASS_CODE")
    private List<ClassSecEntity> classSecList = new ArrayList<>();
}
