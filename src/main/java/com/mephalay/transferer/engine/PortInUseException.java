package com.mephalay.transferer.engine;

/**
 * Created by Mephalay on 4/15/2016.
 */
public class PortInUseException extends Exception {
    protected PortInUseException(String msg){
        super(msg);
    }
}
