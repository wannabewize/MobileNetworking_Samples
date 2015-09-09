package com.vanillastep.example.bluetoothdevicelist;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

   private static final String TAG = "Bluetooth-Sample";
   private static final int BLUETOOTH_ENABLE_REQUEST = 1;
   private Switch mSwitch;
   private BluetoothAdapter mBluetoothAdapter;
   private ListView pairedDeviceList;
   private ArrayAdapter pairedDeviceAdapter;
   private ListView newDeviceList;
   private ArrayAdapter newDeviceAdapter;
   private TextView mStateLabel;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mSwitch = (Switch)findViewById(R.id.onOffSwitch);
      mStateLabel = (TextView)findViewById(R.id.stateLabel);

      // 블루투스 아답터
      mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (mBluetoothAdapter == null) {
         mStateLabel.setText("블루투스 지원 안함");
         mSwitch.setEnabled(false);
      } else {
         mStateLabel.setText("블루투스 지원함");

         Log.d(TAG, "블루투스 어댑터 " + mBluetoothAdapter);

         IntentFilter filter = new IntentFilter();
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
         filter.addAction(BluetoothDevice.ACTION_FOUND);
         filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
         filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
         registerReceiver(mReceiver, filter);

         pairedDeviceList = (ListView)findViewById(R.id.pairedDeviceList);
         pairedDeviceAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
         pairedDeviceList.setAdapter(pairedDeviceAdapter);

         newDeviceList = (ListView) findViewById(R.id.newDeviceList);
         newDeviceAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
         newDeviceList.setAdapter(newDeviceAdapter);
      }
   }

   public void startDiscover(View v) {
      if (!mBluetoothAdapter.isEnabled()) {
         Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
         startActivityForResult(enableBtIntent, BLUETOOTH_ENABLE_REQUEST);
         return;
      }

      if ( mBluetoothAdapter.isDiscovering() ) {
         mBluetoothAdapter.cancelDiscovery();
         mStateLabel.setText("블루투스 장치 검색 중지");
      }
      else {
         newDeviceAdapter.clear();
         pairedDeviceAdapter.clear();

         // 페어링됐던 기기 목록
         Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
         if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
               pairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
         }
         pairedDeviceAdapter.notifyDataSetChanged();

         // 주변 블루투스 기기 검색 시작
         Log.d(TAG, "Start discovery");
         mBluetoothAdapter.startDiscovery();
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if ( BLUETOOTH_ENABLE_REQUEST == requestCode ) {
         if ( RESULT_OK == resultCode ) {
            mStateLabel.setText("블루투스 활성화");
            // 다시 장치 검색 시작
            startDiscover(null);
         }
         else {
            mStateLabel.setText("블루투스 활성화 거부");
         }
      }
   }

   // 블루투스 기기 검색 이벤트 리시버
   BroadcastReceiver mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         if ( BluetoothDevice.ACTION_FOUND.equals(action)) {
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Log.d(TAG, "name : " + name);
            Log.d(TAG, "Address : " + device.getAddress());

            newDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
            newDeviceAdapter.notifyDataSetChanged();
         }
         else if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            // 블루투스 기기 검색 시작
            Log.d(TAG, "블루투스 기기 검색 시작");
            mStateLabel.setText("블루투스 장치 검색 시작");
         }
         else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            // 블루투스 기기 검색 끝
            Log.d(TAG, "블루투스 기기 검색 종료");
            mStateLabel.setText("블루투스 기기 검색 종료");
         }
         else {
            Log.d(TAG, "what? : " + action);
         }
      }
   };

   @Override
   protected void onStop() {
      super.onStop();
      unregisterReceiver(mReceiver);
   }
}
