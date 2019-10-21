package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AllHistory;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AllHistoryRepository extends Repository<AllHistory, String> {
    List<AllHistory> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(String code, String sDateBegin, String sDateEnd, String ClassCode);
    List<AllHistory> findByCodeAndUnoGreaterThanEqualAndClassCode(String code, String sDateBegin, String ClassCode);
}