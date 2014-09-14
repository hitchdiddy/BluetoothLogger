/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.limbergorama.btlogger;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chris
 */
public class LogWriter implements Runnable {

    private final BluetoothDevice btDevice;
    private static final String UUID_SERIAL_PORT_PROFILE
            = "00001101-0000-1000-8000-00805F9B34FB";
    private final String filePath;
    private final String fileName;
    FileWriter wr;
    BufferedReader mBufferedReader;
    private MainActivity observer;

    public LogWriter(String filepath, String filename, BluetoothDevice btDev) {

        this.btDevice = btDev;
        this.filePath = filepath;
        this.fileName = filename;
        this.initBt();
        this.initFw();
        this.observer = null;

    }

    private void initBt() {
        if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            btDevice.createBond();
            //info.append("bondet device " + btd.getName() + "\n");
        }
        //info.append(btd.toString());

        BluetoothSocket mSocket = null;
        mBufferedReader = null;
        InputStream aStream = null;
        InputStreamReader aReader = null;
        try {
            mSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString(UUID_SERIAL_PORT_PROFILE));
            mSocket.connect();
            aStream = mSocket.getInputStream();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        aReader = new InputStreamReader(aStream);
        mBufferedReader = new BufferedReader(aReader);

    }

    private void initFw() {
        File fi = new File(this.filePath);
        if (!fi.exists()) {
            fi.mkdirs();
        }

        File fifi = new File(this.filePath + "/" + fileName);
        if (fifi.exists()) {
            fifi.delete();
        }

        try {
            wr = new FileWriter(fifi);
        } catch (IOException ex) {
            Logger.getLogger(LogWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {

    this.sendMessageToObserver("beginning main thread for device " + this.btDevice.getName());

        String line = "";

        try {
            while ((line = mBufferedReader.readLine()) != null) {
                wr.write(line + "\n");
                wr.flush();
                this.sendMessageToObserver(line);
            }
            wr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void enableObserver(MainActivity tw) {
        this.observer = tw;
    }

    public void sendMessageToObserver(String message) {
        if (this.observer != null) {
            Message msg = this.observer.handler.obtainMessage();
            Bundle bundle = new Bundle();
         
            bundle.putString("info", message);
            msg.setData(bundle);
            this.observer.handler.sendMessage(msg);
        }
    }

}
