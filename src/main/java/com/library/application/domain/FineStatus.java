package com.library.application.domain;

public enum FineStatus {
    PENDING,
    PARTIAL_PAID,
    PAID,

    /**
     * Fine has been waived by an administrator
     */
    WAIVED
}
