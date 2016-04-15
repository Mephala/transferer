package com.mephalay.transferer;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Mephalay on 4/15/2016.
 */
public interface ReceiveNotifier {

    void notifyRAMReceive(String s);

    void notifyHDDReceive(File f);
}
