package com.mephalay.transferer.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mephalay.transferer.ReceiveNotifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mephalay on 4/15/2016.
 */
public class Transferer {
    private Logger logger;
    private List<ListenObject> listenObjects = new ArrayList<>();
    private ObjectMapper om = new ObjectMapper();

    public Transferer(Logger logger){
        this.logger = logger;
        if (logger == null)
            this.logger = Logger.getLogger(this.getClass());
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
        Socket socket = new Socket(ip,port);
        OutputStream os =socket.getOutputStream();
        os.flush();
        processSerializableTransferInner(s, os);
        os.close();
        socket.close();
    }

    public void transferString(String string, String ip, int port) throws IOException {
        Socket socket = new Socket(ip, port);
        OutputStream os = socket.getOutputStream();
        os.flush();
        String encoded = Base64.encodeBase64String(string.getBytes("UTF-8"));
        os.write(encoded.getBytes("UTF-8"));
        os.close();
        socket.close();
    }

    private void processSerializableTransferInner(Serializable s, OutputStream os) throws IOException {
        String serializedObj = om.writeValueAsString(s);
        String encoded = Base64.encodeBase64String(serializedObj.getBytes("UTF-8"));
        os.write(encoded.getBytes("UTF-8"));
        os.flush();
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
        processFileTransferInner(file, os);
        os.close();
        socket.close();
    }

    private void processFileTransferInner(File file, OutputStream os) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        final int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];
        int readSize = fis.read(buffer);
        while (readSize != -1) {
            readSize = TransferUtils.readAndWrite(fis, os, buffer, readSize);
        }
        fis.close();
    }

    public void receiveComposite(int port, String receiveFileFolderPath, ReceiveNotifier rn) throws PortInUseException, IOException {
        validatePort(port);
        ListenObject lo = new ListenObject(port, logger, rn, false, receiveFileFolderPath, true);
        listenObjects.add(lo);
    }

    public void transferComposite(Serializable s, File f, String ip, int port) throws IOException, TransfererProtocolException {
        logger.info("Sending composite object...");
        Socket socket = new Socket(ip, port);
        OutputStream os = socket.getOutputStream();
        processSerializableTransferInner(s, os);
        logger.info("Sent composite serializable...");
        os.write("ACK".getBytes("UTF-8"));
        os.flush();
        InputStream is = socket.getInputStream();
        byte[] ackBytes = new byte[3];
        is.read(ackBytes);
        if (!"ACK".equals(new String(ackBytes, "UTF-8")))
            throw new TransfererProtocolException("Failed to receive ACK from destination");
        logger.info("Received ack from destination, proceeding on sending composite file...");
        processFileTransferInner(f, os);
        logger.info("Sent composite file...");
        os.close();
        socket.close();
    }

    public void terminateAll() {
        for (ListenObject listenObject : listenObjects) {
            listenObject.terminate();
        }
    }
}
