package com.antalex.persistence.repository;

import com.antalex.persistence.entity.OrderHistoryRpt;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderHistoryRepository extends CrudRepository<OrderHistoryRpt, Long> {
    List<OrderHistoryRpt> findByCodeAndClassCodeAndUnoGreaterThanEqualAndUnoLessThanEqual(String code, String classCode, String sDateBegin, String sDateEnd);
    List<OrderHistoryRpt> findByCodeAndClassCodeAndUnoGreaterThanEqual(String code, String classCode, String sDateBegin);
}