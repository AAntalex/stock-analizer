package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AllHistoryRpt;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AllHistoryRepository extends Repository<AllHistoryRpt, String> {
    List<AllHistoryRpt> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(String code, String sDateBegin, String sDateEnd, String classCode);
    List<AllHistoryRpt> findByCodeAndUnoGreaterThanEqualAndClassCode(String code, String sDateBegin, String classCode);
}