package eu.limbergorama.btlogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import static android.widget.AbsListView.CHOICE_MODE_SINGLE;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private Context context;
    private final static int REQUEST_ENABLE_BT = 1337;

    public final Handler handler = new Handler() {
        @Override
public void handleMessage(Message msg) {			  
		Bundle bundle = msg.getData();
		String string = bundle.getString("info");
		//TextView myTextView = (TextView)findViewById(R.id.infoWindow);
		//myTextView.setText(string);
                printInfo(string);
	      }
    
    }
            ;

    

    ArrayList<LogWriter> logWriter;

    TextView info;
    ListView storageListView;
    EditText fileNameEditText;

    public MainActivity() {

    }

    public boolean cardIsMounted() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }
    synchronized void printInfo(String str)
    {
        String text = str+ "\n" + this.info.getText().toString();
        if(text.length() > 2000) {
            text = text.substring(0, 2000);
        }
        
        this.info.setText(str);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.context = this;

        this.info = (TextView) findViewById(R.id.infoWindow);
        this.storageListView = (ListView) findViewById(R.id.storageDeviceList);
        this.fileNameEditText = (EditText) findViewById(R.id.fileNameEditText);

        logWriter = new ArrayList<LogWriter>();

        List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();
        ArrayAdapter<StorageUtils.StorageInfo> storageAdapter = new ArrayAdapter<StorageUtils.StorageInfo>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        storageAdapter.addAll(storageList);

        storageListView.setChoiceMode(CHOICE_MODE_SINGLE);
        storageListView.setAdapter(storageAdapter);
        
        
        this.doTheAutoRefresh(50);
        

        if (this.cardIsMounted()) {
            printInfo("External Storage Device was found");

            File newF = new File(Environment.getExternalStorageDirectory(), "BluetoothOutput.txt");

            try {
                final FileWriter wr = new FileWriter(newF);

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter == null) {
                    printInfo("No Bluetooth device, jurk");
                }

                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                final BtArrayAdapter mArrayAdapter = new BtArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text2);

                ListView deviceList = (ListView) findViewById(R.id.deviceList);
                deviceList.setAdapter(mArrayAdapter);

                deviceList.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
                        BluetoothDevice btd = (BluetoothDevice) parent.getItemAtPosition(position);

                        String fileName = "";
                        if (fileNameEditText.getText().toString().length() == 0) {
                            fileName = UUID.randomUUID().toString().substring(0, 10);
                        } else {
                            fileName = fileNameEditText.getText().toString();
                        }
                        StorageUtils.StorageInfo storageDev = (StorageUtils.StorageInfo) storageListView.getSelectedItem();
                        if (storageDev == null) {
                            //wenn kein storage ausgewählt wurde, nehme einfach den ersten
                            storageDev = (StorageUtils.StorageInfo) storageListView.getAdapter().getItem(0);
                        }

                        String filePath = storageDev.path + "/BluetoothLogger";
                        
                        

                        LogWriter lw = new LogWriter(filePath,fileName, btd);
                        logWriter.add(lw);
                        lw.enableObserver((MainActivity) context);
                        printInfo("started thread for logging "+btd.getName()+ " save in "+filePath);
                        Thread thre = new Thread(lw);
                        thre.start();
                        
                    }
                });

                // If there are paired devices
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices) {
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device);
                    }
                } else {
                    printInfo("No paired bt-devices");
                }

                boolean startDiscovery = mBluetoothAdapter.startDiscovery();

                // Create a BroadcastReceiver for ACTION_FOUND
                final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        // When discovery finds a device
                        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                            // Get the BluetoothDevice object from the Intent
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            // Add the name and address to an array adapter to show in a ListView
                            mArrayAdapter.add(device);
                        }
                    }
                };
                // Register the BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            printInfo("External Storage Device wasn't found, buy a SD Card bitch");
        }

    }
    
    
    String lastReadLine = "";
    
    void doTheAutoRefresh(long time) {
        handler.removeMessages(0);
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if(lastReadLine != "") {
                    MainActivity.this.printInfo(MainActivity.this.lastReadLine);
                    lastReadLine = "";
                    MainActivity.this.doTheAutoRefresh(50);
                }
            }

         }, time);
    }

    
    

}
