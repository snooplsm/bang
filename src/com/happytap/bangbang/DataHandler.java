package com.happytap.bangbang;

/**
 * Created by IntelliJ IDEA.
 * User: rgravener
 * Date: 1/27/11
 * Time: 8:36 PM
 */
public interface DataHandler<T> {

    Class<T> getDataClass();

    void process(T data);

    Byte getInstructionByte();

}
