package com.antalex.service.impl;

import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.service.DealService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DealServiceImpl implements DealService {

    @Override
    public Boolean quitDeals(DealEntity deal, DealEntity dealForQuit) {
        if (deal.getBalance().compareTo(0d) > 0 && dealForQuit.getBalance().compareTo(0d) > 0) {
            Double balance = Double.min(deal.getBalance(), dealForQuit.getBalance());
            BigDecimal result = getSum(deal, balance);
            deal.setBalance(deal.getBalance() - balance);
            setResult(deal, result);
            deal.setQuit(
                    Optional.ofNullable(deal.getQuit())
                            .map(it -> it.concat(",").concat(dealForQuit.getTradeNum().toString()))
                            .orElse(dealForQuit.getTradeNum().toString())
            );

            dealForQuit.setBalance(dealForQuit.getBalance() - balance);
            setResult(dealForQuit, result);
            dealForQuit.setQuit(
                    Optional.ofNullable(dealForQuit.getQuit())
                            .map(it -> it.concat(",").concat(deal.getTradeNum().toString()))
                            .orElse(deal.getTradeNum().toString())
            );
        }
        return deal.getBalance().compareTo(0d) == 0;
    }

    @Override
    public BigDecimal getSum(DealEntity deal) {
        return getSum(deal, deal.getVolume());
    }

    private BigDecimal getSum(DealEntity deal, Double balance) {
        return Optional.ofNullable(deal)
                .map(DealEntity::getPrice)
                .map(it ->
                        it
                                .multiply(new BigDecimal(balance))
                                .multiply(new BigDecimal(deal.getSec().getLotSize()))
                )
                .orElse(null);
    }

    private void setResult(DealEntity deal, BigDecimal result) {
        if (deal.getType() == EventType.BUY) {
            deal.setResult(deal.getResult().add(result));
        } else {
            deal.setResult(deal.getResult().subtract(result));
        }
    }
}

