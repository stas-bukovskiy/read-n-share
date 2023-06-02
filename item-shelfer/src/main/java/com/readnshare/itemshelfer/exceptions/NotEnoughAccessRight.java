package com.readnshare.itemshelfer.exceptions;

public class NotEnoughAccessRight extends RuntimeException {

    public NotEnoughAccessRight(String action) {
        super("Not enough access rights to " + action);
    }

}
