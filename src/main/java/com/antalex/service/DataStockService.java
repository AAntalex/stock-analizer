package com.antalex.service;

import java.util.List;

public interface DataStockService<T> {
    List<T> query(String secClass, String sDateBegin, String sDateEnd, String stockClass);
}
