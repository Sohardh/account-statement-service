package com.sohardh.account.dto;

public record Statement(String date,
                        String description,
                        String refNo,
                        String valueDate,
                        Double debit,
                        Double credit,
                        Double closingBalance) {

}
