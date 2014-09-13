package com.example.bluetoothcommunicator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.bluetooth.BluetoothDevice;

public class BtArrayAdapter extends ArrayAdapter<BluetoothDevice> {

	public BtArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		
	    // Get item
	    BluetoothDevice device = getItem(position);
	
	    View row = convertView;

	    if (row == null)
	    {
	        // ROW INFLATION
	        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        row = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
	    }




	    TextView buddyName = (TextView) row.findViewById(android.R.id.text1);   //change this to textField1  from simple_list_item_2
	    buddyName.setText(device.getName());

	    TextView buddyStatus = (TextView) row.findViewById(android.R.id.text2); //change this to textField2 from simple_list_item_2
	    buddyStatus.setText(device.getAddress());
	    //      Log.d(tag, buddy.getIdentity()+"'s mood is "+buddyStatus.getText());



	    return row;
	}
	
	
}
