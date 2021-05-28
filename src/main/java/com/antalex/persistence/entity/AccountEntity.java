package com.antalex.persistence.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Z#AAA_ACCOUNT")
@Data
@Entity
public class AccountEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "C_FIRM_ID")
    private String firmId;
    @Column(name = "C_CLIENT_CODE")
    private String clientCode;
    @Column(name = "C_NUM")
    private String accountNumber;
    @Column(name = "C_TAG")
    private String tag;
    @Column(name = "C_LEVERAGE")
    private BigDecimal leverage;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "C_ACCOUNT")
    private List<MoneyPositionEntity> positions = new ArrayList<>();
}
