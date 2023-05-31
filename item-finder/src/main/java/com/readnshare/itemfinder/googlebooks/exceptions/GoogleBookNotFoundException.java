package com.readnshare.itemfinder.googlebooks.exceptions;

public class GoogleBookNotFoundException extends GoogleBookException {
    public GoogleBookNotFoundException(String is) {
        super("Not Found book with such id" + is);
    }
}
