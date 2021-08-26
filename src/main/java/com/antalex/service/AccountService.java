package com.antalex.service;

import com.antalex.persistence.entity.AccountEntity;
import com.antalex.persistence.entity.CurrencyEntity;
import com.antalex.persistence.entity.MoneyPositionEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<AccountEntity> findAll();
    BigDecimal getAvailableAmount(AccountEntity account, CurrencyEntity cur);
    Optional<MoneyPositionEntity> getMoneyPosition(AccountEntity account, CurrencyEntity cur);
}
