package org.nesc.ec.bigdata.exception;

public class ConnectorException  extends Exception{

    public ConnectorException(Throwable cause) {
        super(cause);
    }

    public ConnectorException(String message) {
        super(message);
    }

    public  ConnectorException(String messages ,Throwable cause  ){
        super(messages , cause);
    }
}
