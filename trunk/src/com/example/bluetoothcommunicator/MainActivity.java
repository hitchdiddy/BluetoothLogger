package com.example.bluetoothcommunicator;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Context context;
	private final static int REQUEST_ENABLE_BT = 1337;
	private static final String UUID_SERIAL_PORT_PROFILE 
    = "00001101-0000-1000-8000-00805F9B34FB";

	public boolean cardIsMounted() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else
			return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.context = (Context)this;

		final Button but = (Button) findViewById(R.id.Connect);

		but.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText tw = (EditText) findViewById(R.id.infoWindow);
				tw.setText(tw.getText() + "fuck");
			}
		});
		
		
		final EditText info = (EditText) findViewById(R.id.infoWindow);
		
		
		
		
	
		
		if(this.cardIsMounted()) {
			info.append("External Storage Device was found\n");
			
			File newF = new File(Environment.getExternalStorageDirectory(),"BluetoothOutput.txt");
			

			try {
				final FileWriter wr = new FileWriter(newF);

				
				
				
				
				
				

				
				
				
				
				
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
					info.append("No Bluetooth device, jurk\n");
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
						  btd.createBond();
						  //info.append(btd.toString());
						  
						  BluetoothSocket mSocket = null;
						  BufferedReader mBufferedReader = null;
						    InputStream aStream = null;
						    InputStreamReader aReader = null;	
						  try {
							mSocket = btd.createRfcommSocketToServiceRecord( UUID.fromString(UUID_SERIAL_PORT_PROFILE) );
					        mSocket.connect();
					        aStream = mSocket.getInputStream();
						  
						  } catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					        aReader = new InputStreamReader( aStream );
					        mBufferedReader = new BufferedReader( aReader );
					        
					        
					        String line = "";
					        try {
								while((line = mBufferedReader.readLine())!=null) {
									wr.write(line+"\n");
									wr.flush();
								}
								  wr.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}				        
					        /*
					        CharBuffer target = CharBuffer.allocate(200);
					        
					        try {
								while(mBufferedReader.read(target)!=-1) {
									wr.write(target.toString()+"\n");
									wr.flush();
								}
								  wr.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							*/
					      
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
					info.append("No paired bt-devices\n");
				}
				
				boolean startDiscovery = mBluetoothAdapter.startDiscovery();

				// Create a BroadcastReceiver for ACTION_FOUND
				final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
			info.append("External Storage Device wasn't found, buy a SD Card bitch\n");
		}
		
		
		
		
		

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
