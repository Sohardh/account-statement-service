package com.sohardh.account.dto;

import java.util.Date;

public record Statement(Date date,
                        String description,
                        String refNo,
                        String valueDate,
                        Double debit,
                        Double credit,
                        Double closingBalance) {

}
