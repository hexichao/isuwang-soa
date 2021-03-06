package com.isuwang.dapeng.transaction.api.domain;

import com.isuwang.org.apache.thrift.TEnum;

public enum TGlobalTransactionProcessStatus implements TEnum {

    /**
     *
     **/
    New(1),

    /**
     *
     **/
    Success(2),

    /**
     *
     **/
    Fail(3),

    /**
     *
     **/
    Unknown(4),

    /**
     *
     **/
    HasRollback(5);


    private final int value;

    private TGlobalTransactionProcessStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static TGlobalTransactionProcessStatus findByValue(int value) {
        switch (value) {

            case 1:
                return New;

            case 2:
                return Success;

            case 3:
                return Fail;

            case 4:
                return Unknown;

            case 5:
                return HasRollback;

            default:
                return null;
        }
    }
}
      