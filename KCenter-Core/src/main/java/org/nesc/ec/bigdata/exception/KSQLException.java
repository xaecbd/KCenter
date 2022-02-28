package org.nesc.ec.bigdata.exception;

public class KSQLException extends Exception{
    public KSQLException(Throwable cause) {
        super(cause);
    }

    public KSQLException(String message) {
        super(message);
    }

    public  KSQLException(String messages ,Throwable cause  ){
        super(messages , cause);
    }
}
