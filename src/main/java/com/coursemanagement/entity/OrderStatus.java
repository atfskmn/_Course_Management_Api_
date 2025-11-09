package com.coursemanagement.entity;

public enum OrderStatus {
    PENDING,    // Sipariş oluşturuldu, ödeme bekleniyor
    COMPLETED,  // Ödeme alındı, sipariş tamamlandı
    CANCELLED,  // Sipariş iptal edildi
    REFUNDED    // İade edildi
}
