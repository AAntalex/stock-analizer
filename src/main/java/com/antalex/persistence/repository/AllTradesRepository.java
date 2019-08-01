package com.antalex.persistence.repository;

import com.antalex.persistence.entity.AllTrades;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface AllTradesRepository extends Repository<AllTrades, String> {
    List<AllTrades> findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(String code, String sDateBegin, String sDateEnd, String ClassCode);
    List<AllTrades> findByCodeAndUnoGreaterThanEqualAndClassCode(String code, String sDateBegin, String ClassCode);
}