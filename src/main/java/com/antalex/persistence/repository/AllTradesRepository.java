package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AllTradesRpt;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AllTradesRepository extends Repository<AllTradesRpt, String> {
    List<AllTradesRpt> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(String code, String sDateBegin, String sDateEnd, String classCode);
    List<AllTradesRpt> findByCodeAndUnoGreaterThanEqualAndClassCode(String code, String sDateBegin, String classCode);
}