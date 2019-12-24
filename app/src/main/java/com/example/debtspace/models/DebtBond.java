package com.example.debtspace.models;

public class DebtBond {

    private String partnerUsername;
    private String debt;

    public DebtBond() {}

    public DebtBond(String partnerUsername, String debt) {
        this.partnerUsername = partnerUsername;
        this.debt = debt;
    }

    public String getPartnerUsername() {
        return partnerUsername;
    }

    public String getDebt() {
        return debt;
    }
}
