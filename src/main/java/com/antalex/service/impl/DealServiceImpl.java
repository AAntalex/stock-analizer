package com.antalex.service.impl;

import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.OrderEntity;
import com.antalex.persistence.repository.DealRepository;
import com.antalex.service.DealService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DealServiceImpl implements DealService {
    private DealRepository dealRepository;

    @Override
    public Boolean quitDeals(DealEntity deal, DealEntity dealForQuit) {
        if (deal.getBalance().compareTo(0d) > 0 && dealForQuit.getBalance().compareTo(0d) > 0) {
            Double balance = Double.min(deal.getBalance(), dealForQuit.getBalance());
            BigDecimal result = getSum(deal, balance);

            deal.setBalance(deal.getBalance() - balance);
            deal.setResult(deal.getResult().add(result));
            deal.setQuit(
                    deal.getQuit().isEmpty()
                            ? dealForQuit.getId().toString()
                            : deal.getQuit().concat(",").concat(dealForQuit.getId().toString())
            );

            dealForQuit.setBalance(dealForQuit.getBalance() - balance);
            dealForQuit.setResult(dealForQuit.getResult().add(result));
            dealForQuit.setQuit(
                    dealForQuit.getQuit().isEmpty()
                            ? deal.getId().toString()
                            : dealForQuit.getQuit().concat(",").concat(deal.getId().toString())
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
}

