package com.antalex.persistence.repository;

import com.antalex.persistence.entity.QuotesRpt;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface QuotesRepository extends Repository<QuotesRpt, String> {
    List<QuotesRpt> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCodeAndQuotesIsNotNull(String code, String sDateBegin, String sDateEnd, String classCode);
    List<QuotesRpt> findByCodeAndUnoGreaterThanEqualAndClassCodeAndQuotesIsNotNull(String code, String sDateBegin, String classCode);
}