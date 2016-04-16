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
    private final Logger logger;
    private final ReceiveNotifier rn;
    private AtomicBoolean stopSignal = new AtomicBoolean(false);
    private boolean ramReceive;
    private String folderPath;
    private boolean isComposite;

    protected ListenObject(int port, Logger logger, ReceiveNotifier rn, String receiveFileFolderPath) throws IOException {
        this(port, logger, rn, false, receiveFileFolderPath);
    }

    protected ListenObject(final int port, final Logger logger, ReceiveNotifier rn, final boolean ramReceive, String receiveFileFolderPath, boolean isComposite) throws IOException {
        this.port = port;
        this.logger = logger;
        this.rn = rn;
        this.ramReceive = ramReceive;
        serverSocket = new ServerSocket(port);
        this.folderPath = receiveFileFolderPath;
        this.isComposite = isComposite;
        serverSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopSignal.get()) {
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
                        if (!stopSignal.get())
                            logger.error("Failed processing listen", t);
                        else
                            logger.info("Terminated server listening thread...");
                    }

                }
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.error("Failed closing server socket", e);
                }
            }
        });
        serverSocketThread.start();
    }

    protected ListenObject(final int port, final Logger logger, ReceiveNotifier rn, final boolean ramReceive, String receiveFileFolderPath) throws IOException {
        this(port, logger, rn, ramReceive, receiveFileFolderPath, false);
    }

    protected ListenObject(final int port, final Logger logger, ReceiveNotifier rn, final boolean ramReceive) throws IOException {
        this(port, logger, rn, ramReceive, null);
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

    private void processConnection(Socket processSocket) {
        BufferedReader br = null;
        try {
            long start = System.currentTimeMillis();
            logger.info("Processing client connection on port:" + port);
            if (isComposite) {
                handleComposite(processSocket, rn);
            } else {
                br = handleNonComposite(processSocket, br);
            }
            logger.info("Processed client connection on port:" + port + " in " + (System.currentTimeMillis() - start) + " ms.");
        } catch (Throwable t) {
            logger.error("Failed processing client connection", t);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error("Failed closing BufferedReader", e);
                }
            }
        }
    }

    /**
     * Handle with care.
     *
     * @param processSocket
     * @param rn
     * @throws IOException
     */

    private void handleComposite(Socket processSocket, ReceiveNotifier rn) throws IOException {
        InputStream is = processSocket.getInputStream();
        byte[] buffer = new byte[4096];
        int readSize = is.read(buffer);
        boolean incomingFile = false;
        File file = new File(folderPath + File.separator + UUID.randomUUID().toString());
        FileOutputStream fos = new FileOutputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (readSize != -1) {
            byte[] meaningfulData = new byte[readSize];
            System.arraycopy(buffer, 0, meaningfulData, 0, readSize);
            if (meaningfulData.length < 9) {
                String ack = new String(meaningfulData, "UTF-8");
                if ("ACK".equals(ack)) {
                    logger.info("Ack received, completed receiving composite serializable...");
                    incomingFile = true;
                    logger.info("Sending ack...");
                    OutputStream os = processSocket.getOutputStream();
                    os.flush();
                    os.write("ACK".getBytes("UTF-8"));
                    os.flush();
                    logger.info("ACK sent, awaiting composite file transfer...");
                    readSize = is.read(buffer);
                    continue;
                }
            }
            processRoutine(incomingFile, fos, baos, meaningfulData);
            readSize = is.read(buffer);
        }
        logger.info("Completed composite network transfer, finishing up...");
        byte[] encodedBytes = baos.toByteArray();
        byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
        String s = new String(decodedBytes, "UTF-8");
        baos.close();
        fos.close();
        is.close();
        processSocket.close();
        rn.notifyComposite(s, file);
    }

    private void processRoutine(boolean incomingFile, FileOutputStream fos, ByteArrayOutputStream baos, byte[] meaningfulData) throws IOException {
        if (incomingFile) {
            fos.write(meaningfulData);
            fos.flush();
        } else {
            baos.write(meaningfulData);
        }
    }

    private BufferedReader handleNonComposite(Socket processSocket, BufferedReader br) throws IOException {
        if (ramReceive) {
            InputStream is = processSocket.getInputStream();
            String notVal = receiveSerializedData(is);
            rn.notifyRAMReceive(notVal);
        } else {
            InputStream is = processSocket.getInputStream();
            File fileToWrite = receiveHDDFile(is);
            rn.notifyHDDReceive(fileToWrite);
        }
        return br;
    }

    private File receiveHDDFile(InputStream is) throws IOException {
        int bufferSize = 4096;
        File fileToWrite = new File(folderPath + File.separator + UUID.randomUUID().toString());
        FileOutputStream fos = new FileOutputStream(fileToWrite);
        byte[] buffer = new byte[bufferSize];
        int readSize = is.read(buffer);
        while (readSize != -1) {
            readSize = TransferUtils.readAndWrite(is, fos, buffer, readSize);
        }
        fos.close();
        return fileToWrite;
    }

    private String receiveSerializedData(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        String serializedObj = sb.toString();
        byte[] data = Base64.decodeBase64(serializedObj);
        return new String(data, "UTF-8");
    }


    public void terminate() {
        stopSignal.set(true);
        try {
            if (!serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
    }
}
