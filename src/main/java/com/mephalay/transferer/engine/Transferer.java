package com.mephalay.transferer.engine;

import com.mephalay.transferer.ReceiveNotifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mephalay on 4/15/2016.
 */
public class Transferer {
    private final Logger logger;
    private List<ListenObject> listenObjects = new ArrayList<>();
    private ObjectMapper om = new ObjectMapper();

    public Transferer(Logger logger){
        this.logger = logger;
    }

    public void receiveRAM(int port, ReceiveNotifier rn) throws PortInUseException, IOException {
        validatePort(port);
        ListenObject lo = new ListenObject(port,logger,rn,true);
        listenObjects.add(lo);
    }

    private void validatePort(int port) throws PortInUseException {
        for (ListenObject listenObject : listenObjects) {
            if(listenObject.getPort()==port)
                throw new PortInUseException("Port in use");
        }
    }

    /**
     * All transfer is handled from RAM. Don't use for large objects.
     * @param s
     * @param ip
     * @param port
     * @throws IOException
     */

    public void transfer(Serializable s, String ip, int port) throws IOException {
        String serializedObj =  om.writeValueAsString(s);
        String encoded = Base64.encodeBase64String(serializedObj.getBytes("UTF-8"));
        Socket socket = new Socket(ip,port);
        OutputStream os =socket.getOutputStream();
        os.flush();
        os.write(encoded.getBytes("UTF-8"));
        os.flush();
        os.close();
        socket.close();
    }

    public void receiveHDD(String receiveFileFolderPath, int port, ReceiveNotifier rn) throws PortInUseException, IOException {
        validatePort(port);
        ListenObject lo = new ListenObject(port,logger,rn,receiveFileFolderPath);
        listenObjects.add(lo);
    }

    public void transferHDD(File file, String ip, int port) throws IOException {
        Socket socket = new Socket(ip,port);
        OutputStream os = socket.getOutputStream();
        os.flush();
        FileInputStream fis = new FileInputStream(file);
        final int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        int readSize = fis.read(buffer);
        while( readSize != -1){
           readSize = TransferUtils.readAndWrite(fis,os,buffer,readSize);
        }
        fis.close();
        os.close();
        socket.close();
    }
}
