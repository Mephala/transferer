package com.mephalay.transferer.engine;

import com.mephalay.transferer.ReceiveNotifier;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Mephalay on 4/15/2016.
 */
 class ListenObject {
    private final Thread serverSocketThread;
    private final ServerSocket serverSocket;
    private final int port;
    private AtomicBoolean stopSignal = new AtomicBoolean(false);
    private final Logger logger;
    private final ReceiveNotifier rn;
    private boolean ramReceive;
    private String folderPath;

    public ListenObject(int port, Logger logger, ReceiveNotifier rn, String receiveFileFolderPath) throws IOException {
        this(port,logger,rn,false,receiveFileFolderPath);
    }

    private ListenObject(final int port, final Logger logger, ReceiveNotifier rn, final boolean ramReceive, String receiveFileFolderPath) throws IOException {
        this.port = port;
        this.logger = logger;
        this.rn = rn;
        this.ramReceive = ramReceive;
        serverSocket= new ServerSocket(port);
        this.folderPath = receiveFileFolderPath;
        serverSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stopSignal.get()){
                    try {
                        final Socket processSocket = serverSocket.accept();
                        logger.info("Received a client connection over port:" + port + ", ip:" + processSocket.getInetAddress().getHostAddress());
                        Thread lpProcessThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                processConnection(processSocket);
                            }
                        });
                        lpProcessThread.setPriority(Thread.MIN_PRIORITY);
                        lpProcessThread.setDaemon(true);
                        lpProcessThread.start();
                    } catch (Throwable t) {
                        logger.error("Failed processing listen",t);
                    }

                }
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.error("Failed closing server socket",e);
                }
            }
        });
        serverSocketThread.start();

    }

    public int getPort() {
        return port;
    }

    public AtomicBoolean getStopSignal() {
        return stopSignal;
    }

    public void setStopSignal(AtomicBoolean stopSignal) {
        this.stopSignal = stopSignal;
    }

    public boolean isRamReceive() {
        return ramReceive;
    }

    public void setRamReceive(boolean ramReceive) {
        this.ramReceive = ramReceive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListenObject that = (ListenObject) o;

        return port == that.port;

    }

    @Override
    public int hashCode() {
        return port;
    }

    @Override
    public String toString() {
        return "ListenObject{" +
                "port=" + port +
                ", ramReceive=" + ramReceive +
                '}';
    }

    protected ListenObject(final int port, final Logger logger, ReceiveNotifier rn, final boolean ramReceive) throws IOException {
        this(port,logger,rn,ramReceive,null);
    }

    private void processConnection(Socket processSocket) {
        BufferedReader br = null;
        try {
            long start = System.currentTimeMillis();
            logger.info("Processing client connection on port:" + port);
            if(ramReceive){
                InputStream is =processSocket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                String line ;
                StringBuilder sb = new StringBuilder();
                line = br.readLine();
                while(line !=null){
                    sb.append(line);
                    line = br.readLine();
                }
                String serializedObj = sb.toString();
                byte[] data = Base64.decodeBase64(serializedObj);
                rn.notifyRAMReceive(new String(data,"UTF-8"));
            }else{
                InputStream is =processSocket.getInputStream();
                int bufferSize = 4096;
                File fileToWrite = new File(folderPath + File.separator + UUID.randomUUID().toString());
                FileOutputStream fos = new FileOutputStream(fileToWrite);
                byte[] buffer = new byte[bufferSize];
                int readSize = is.read(buffer);
                while(readSize != -1){
                    readSize = TransferUtils.readAndWrite(is, fos, buffer, readSize);
                }
                fos.close();
               rn.notifyHDDReceive(fileToWrite);
            }
            logger.info("Processed client connection on port:" + port + " in " + (System.currentTimeMillis() - start) + " ms.");
        } catch (Throwable t) {
            logger.error("Failed processing client connection",t);
        }finally {
            if(br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("Failed closing BufferedReader",e);
                }
            }
        }
    }


}
