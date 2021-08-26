package com.antalex.service.impl;

import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.repository.ClassSecRepository;
import com.antalex.service.ClassSecService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClassSecServiceImpl implements ClassSecService {
    private ClassSecRepository сlassSecRepository;

    @Override
    public Double getVolume(ClassSecEntity sec, BigDecimal amount, BigDecimal price) {
        return  Optional
                .ofNullable(price)
                .map(it ->
                        it.multiply(
                                BigDecimal.valueOf(
                                        Optional.ofNullable(sec)
                                                .map(ClassSecEntity::getLotSize)
                                                .orElse(0)
                                )
                        )
                )
                .filter(it -> it.compareTo(BigDecimal.ZERO) > 0)
                .map(
                        it -> Optional
                                .ofNullable(amount)
                                .map(a -> a.divide(it, 2, RoundingMode.HALF_UP))
                                .orElse(BigDecimal.ZERO)
                )
                .orElse(BigDecimal.ZERO)
                .toBigInteger()
                .doubleValue();
    }

    @Override
    public List<ClassSecEntity> findForAutoTrade() {
        return сlassSecRepository.findAllByAutoTrade(true);
    }
}
