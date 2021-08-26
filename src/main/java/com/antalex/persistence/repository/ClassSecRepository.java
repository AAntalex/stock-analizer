package com.antalex.persistence.repository;

import com.antalex.persistence.entity.ClassSecEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClassSecRepository extends CrudRepository<ClassSecEntity, Long> {
    List<ClassSecEntity> findAllByAutoTrade(Boolean autoTrade);
}