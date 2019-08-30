package com.antalex.persistence.repository;

import com.antalex.persistence.entity.Quotes;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface QuotesRepository extends Repository<Quotes, String> {
    List<Quotes> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCodeAndQuotesIsNotNull(String code, String sDateBegin, String sDateEnd, String ClassCode);
    List<Quotes> findByCodeAndUnoGreaterThanEqualAndClassCodeAndQuotesIsNotNull(String code, String sDateBegin, String ClassCode);
}