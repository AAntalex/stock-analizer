package com.antalex.persistence.repository;

import com.antalex.persistence.entity.DealHistoryRpt;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DealHistoryRepository extends CrudRepository<DealHistoryRpt, Long> {
    List<DealHistoryRpt> findByCodeAndClassCodeAndUnoGreaterThanEqualAndUnoLessThanEqual(String code, String classCode, String sDateBegin, String sDateEnd);
    List<DealHistoryRpt> findByCodeAndClassCodeAndUnoGreaterThanEqual(String code, String classCode, String sDateBegin);
}