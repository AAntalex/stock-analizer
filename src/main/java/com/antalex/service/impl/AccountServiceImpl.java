package com.antalex.service.impl;

import com.antalex.model.enums.StatusType;
import com.antalex.persistence.entity.AccountEntity;
import com.antalex.persistence.entity.FtMoneyEntity;
import com.antalex.persistence.entity.MoneyPositionEntity;
import com.antalex.persistence.repository.AccountRepository;
import com.antalex.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public Optional<MoneyPositionEntity> getMoneyPosition(AccountEntity account, FtMoneyEntity cur) {
        return account
                .getPositions()
                .stream()
                .filter(it -> it.getStatus() == StatusType.ENABLED && it.getCur().equals(cur))
                .findFirst();
    }

    @Override
    public List<AccountEntity> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public BigDecimal getAvailableAmount(AccountEntity account, FtMoneyEntity cur) {
        return this.getMoneyPosition(account, cur)
                .map(it ->
                        Optional
                                .ofNullable(it.getAvailableAmount())
                                .map(
                                        availableAmount ->
                                                availableAmount.min(
                                                        getAvailableAmountByPercent(it)
                                                                .orElse(
                                                                        Optional
                                                                                .ofNullable(it.getAmount())
                                                                                .orElse(BigDecimal.ZERO)
                                                                )
                                                )
                                )
                                .orElse(
                                        getAvailableAmountByPercent(it)
                                                .orElse(BigDecimal.ZERO)
                                )
                                .subtract(
                                        Optional
                                                .ofNullable(it.getUsedAmount())
                                                .orElse(BigDecimal.ZERO)
                                )
                                .max(BigDecimal.ZERO)
                )
                .orElse(BigDecimal.ZERO);
    }

    private Optional<BigDecimal> getAvailableAmountByPercent(MoneyPositionEntity position) {
        return Optional
                .ofNullable(position.getAvailablePercent())
                .map(p ->
                        p.multiply(
                                Optional
                                        .ofNullable(position.getAmount())
                                        .orElse(BigDecimal.ZERO)
                        )
                                .divide(
                                        BigDecimal.valueOf(100)
                                        , 2
                                        , RoundingMode.HALF_UP
                                )
                );
    }
}
