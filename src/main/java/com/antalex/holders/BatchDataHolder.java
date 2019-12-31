package com.antalex.holders;

import org.springframework.web.context.request.RequestContextHolder;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

public class BatchDataHolder {
    private BatchDataHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    public static Integer getBachSize() {
        return (Integer) RequestContextHolder.getRequestAttributes()
                .getAttribute("batchSize", SCOPE_REQUEST);
    }

    public static void setBatchSize(Integer batchSize) {
        RequestContextHolder.getRequestAttributes().setAttribute("batchSize", batchSize, SCOPE_REQUEST);
    }

}