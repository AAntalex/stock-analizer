package com.antalex.model.enums;

/**
 * Тип события
 */
public enum ExecType {
    /**
     * Значение не указано
     */
    NOT_SET,
    /**
     * Немедленно или отклонить
     */
    IMMEDIATELY,
    /**
     * Поставить в очередь
     */
    TO_QUEUE,
    /**
     * Снять остаток
     */
    REMOVE_SALDO,
    /**
     * До снятия
     */
    BEFORE_REMOVE,
    /**
     * До даты
     */
    BEFORE_DATE,
    /**
     * В течение сессии
     */
    DURING_SESSION,
    /**
     * Открытие
     */
    OPEN,
    /**
     * Закрытие
     */
    CLOSE,
    /**
     * Кросс
     */
    CROSS,
    /**
     * До следующей сессии
     */
    UNTIL_SESSION,
    /**
     * До отключения
     */
    UNTIL_OPEN,
    /**
     * До времени
     */
    UNTIL_TIME,
    /**
     * Следующий аукцион
     */
    NEXT_AUCTION,
}
