package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AllHistoryRpt;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AllHistoryRepository extends Repository<AllHistoryRpt, String> {
    List<AllHistoryRpt> findAllByCodeInAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(
            List<String> codes,
            String sDateBegin,
            String sDateEnd,
            String classCode
    );
    List<AllHistoryRpt> findAllByCodeInAndUnoGreaterThanEqualAndClassCode(
            List<String> codes,
            String sDateBegin,
            String classCode
    );
}