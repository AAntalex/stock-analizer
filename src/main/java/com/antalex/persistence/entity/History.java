package com.antalex.persistence.entity;

import com.antalex.holders.DateFormatHolder;

import java.util.Date;

public interface History {
    String getUno();
    String getCode();
    String getClassCode();

    default Date getDate() {
        return DateFormatHolder.getDateFromString(getUno());
    }
}
