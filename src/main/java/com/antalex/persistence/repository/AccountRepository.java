package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AccountEntity;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AccountRepository extends Repository<AccountEntity, Long> {
    List<AccountEntity> findAll();
    AccountEntity findByAccountNumber(String accountNumber);
}