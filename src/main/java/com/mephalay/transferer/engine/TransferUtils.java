package com.mephalay.transferer.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mephalay on 4/15/2016.
 */
 class TransferUtils {

    protected static int readAndWrite(InputStream is, OutputStream os, byte[] buffer, int readSize) throws IOException {
        byte[] meaningfulData = new byte[readSize];
        System.arraycopy(buffer,0,meaningfulData,0,readSize);
        os.write(meaningfulData);
        os.flush();
        readSize = is.read(buffer);
        return readSize;
    }
}
