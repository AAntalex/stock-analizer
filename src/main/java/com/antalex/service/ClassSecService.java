package com.antalex.service;

import com.antalex.persistence.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface ClassSecService {
    Double getVolume(ClassSecEntity sec, BigDecimal amount, BigDecimal price);
    List<ClassSecEntity> findForAutoTrade();
}
