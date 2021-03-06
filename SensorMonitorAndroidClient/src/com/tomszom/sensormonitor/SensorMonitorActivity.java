package com.tomszom.sensormonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SensorMonitorActivity extends ListActivity {
	private final String TAG = getClass().getName();
	static public String serverAddress;
	static public String clientName;
	private final int PORT = 26123;
	private ListView lv;
	private boolean openport = false;
	public static ArrayList<Measurement> measurementArrayList; // TODO make
																// singleton?
	public String[] months;

	Handler handler = new Handler(new Callback() {
		public boolean handleMessage(final Message msg) {
			runOnUiThread(new Runnable() {
				public void run() {
					// pb.setVisibility(ProgressBar.INVISIBLE); //od�wie�enie
					// layoutu
					
					switch (msg.arg1) {
					case 0:
						openport = false;// finish();
						break;
					case 1 :
						refreshList();
					}
				}
			});
			return false;
		}
	});

	private class MeasurementAdapter extends ArrayAdapter<Measurement> {

		private ArrayList<Measurement> items;

		public MeasurementAdapter(Context context, int textViewResourceId,
				ArrayList<Measurement> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.date_row, null);
			}
			String stock = items.get(position).getStock();
			if (stock != null) {
				TextView tv_stock = (TextView) v.findViewById(R.id.tv_stock);
				if (tv_stock != null) {
					tv_stock.setText(stock);
				}
				TextView tv_measurement = (TextView) v
						.findViewById(R.id.tv_measurement);
				TextView tv_value = (TextView) v.findViewById(R.id.tv_value);
				if (tv_measurement != null) {
					tv_measurement
							.setText(items.get(position).getMeasurement());
				}
				if (tv_value != null) {
					tv_value.setText(items.get(position).getValue());
				}
			}
			return v;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sensor_monitor);
		// setListAdapter(new MeasurementAdapter(this, R.layout.date_row,
		// measurementArrayList));

		Log.d(TAG, "measurementsListActivity starts");
	}

	public void openPort() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		SensorMonitorActivity.serverAddress = prefs.getString(this.getResources()
				.getString(R.string.serverAddressOption), "");
		if (serverAddress.equals("")) {
			Log.d(TAG, "Empty address, launch Preferneces...");
			Intent intent = new Intent().setClass(this,
					SensorMonitorPreferencesActivity.class);
			this.startActivityForResult(intent, 0);
			return;
		}
		SensorMonitorActivity.clientName = prefs.getString(this.getResources()
				.getString(R.string.clientOption), "");
		if (clientName.equals("")) {
			Log.d(TAG, "Empty client name, launch Preferneces...");
			Intent intent = new Intent().setClass(this,
					SensorMonitorPreferencesActivity.class);
			this.startActivityForResult(intent, 0);
			return;
		}
		if (!openport) {
			openport = true;
			new Thread(new Runnable() {
				public void run() {
					Socket s;
					Boolean end = false;
					try {
						Log.d(TAG, "serv "+SensorMonitorActivity.serverAddress+" client "+SensorMonitorActivity.clientName);	
						s = new Socket(SensorMonitorActivity.serverAddress, PORT);
						// outgoing stream redirect to socket
						OutputStream out = s.getOutputStream();
						
						PrintWriter output = new PrintWriter(out);
						
						BufferedReader input = new BufferedReader(
								new InputStreamReader(s.getInputStream()));
						String st = input.readLine();
						Log.d(TAG, "Received from monitor: " + st);												
					    output.println("CONNECT " + SensorMonitorActivity.clientName+"\r\n");
					    output.flush();
						while (!end) {
							st = input.readLine();
							Log.d(TAG, "Received from monitor: " + st);
							String[] splitted = st.split(";");
							for (int i = 0; i < measurementArrayList.size(); i++) {
								if (measurementArrayList.get(i).getStock()
										.equals(splitted[0])
										&& measurementArrayList.get(i)
												.getMeasurement()
												.equals(splitted[1])) {
									measurementArrayList.get(i).setValue(
											splitted[2]);
									Message message = new Message();
									message.arg1 = 1; // trzeba da� zna�, �e dane uleg�y zmianie
									handler.sendMessage(message);
								}
							}
						}
						s.close();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						end = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						end = true;
					}
					Message message = new Message();
					message.arg1 = 0;
					handler.sendMessage(message);
					Log.d(TAG,"polaczenie zamkniete");
					openport = false;
				}
			}).start();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// refreshList();
	}

	/**
	 * Tworzenie menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_sensor_monitor, menu);
		return true;
	}

	/**
	 * Akcja po przyci�ni�ciu elementu z menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this,
					SensorMonitorPreferencesActivity.class));
			break;
		}
		return true;
	}

	public void refreshList() {

		setListAdapter(new MeasurementAdapter(this, R.layout.date_row,
				measurementArrayList));
		lv = getListView();
		lv.setTextFilterEnabled(true);

		// Ustawiam listener dla listy
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "Kliknieto " + position + " id:" + id);
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());

				String clientName = prefs.getString("client_option", "");
				if (!clientName.equals("")) {
					pairs.add(new BasicNameValuePair("client", clientName));
				}
				if (measurementArrayList.get(position).isSubscribing()) {

					pairs.add(new BasicNameValuePair("action", "cancel"));
					pairs.add(new BasicNameValuePair("stock",
							measurementArrayList.get(position).getStock()));
					pairs.add(new BasicNameValuePair("metrics",
							measurementArrayList.get(position).getMeasurement()));
					sendPost(pairs);
					measurementArrayList.get(position).setSubscribing(false);
				} else {
					pairs.add(new BasicNameValuePair("action", "subscribe"));
					pairs.add(new BasicNameValuePair("stock",
							measurementArrayList.get(position).getStock()));
					pairs.add(new BasicNameValuePair("metrics",
							measurementArrayList.get(position).getMeasurement()));
					sendPost(pairs);
					measurementArrayList.get(position).setSubscribing(true);
				}
			}
		});
	}

	public void get_measurements(View target) {
		openPort(); // otwieram po�aczenie
		if (openport){
			measurementArrayList = new ArrayList<Measurement>();
			List<NameValuePair> pair = new ArrayList<NameValuePair>();
			String respond = sendPost(pair);

			if (respond.equals(""))
				return; // nic nie dosta� nic nie robi
			// parsowanie listy:
			String[] splitted = respond.split(";");
			for (int i = 0; i < splitted.length - 1; i += 2) {
				measurementArrayList.add(new Measurement(splitted[i],
						splitted[i + 1], ""));
			}
		
			refreshList();
		}
	}

	public String sendPost(List<NameValuePair> pairs) {
		String textResult = "";
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		String serverAddress = prefs
				.getString(
						this.getResources().getString(
								R.string.serverAddressOption), "");
		if (serverAddress.equals("")) {
			Log.d(TAG, "Empty address, launch Preferneces...");
			Intent intent = new Intent().setClass(this,
					SensorMonitorPreferencesActivity.class);
			this.startActivityForResult(intent, 0);
			return "";
		}
		String clientName = prefs.getString(
				this.getResources().getString(R.string.clientOption), "");
		if (clientName.equals("")) {
			Log.d(TAG, "Empty client name, launch Preferneces...");
			Intent intent = new Intent().setClass(this,
					SensorMonitorPreferencesActivity.class);
			this.startActivityForResult(intent, 0);
			return "";
		} else {
			// pairs.add(new BasicNameValuePair("client", clientName));
		}
		// String serverAddress = "192.12.8.100";
		serverAddress = "http://" + serverAddress
				+ "/Monitor/subscriptions";
		SensorMonitor app = (SensorMonitor) this.getApplication();
		HttpClient client = app.getHttpClient();
		HttpPost post = new HttpPost(serverAddress);
		Log.d(TAG, serverAddress);
		BufferedReader in = null;

		try {
			post.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Log.d(TAG, "unsupprotedEncoding");
			return "";
			// e.printStackTrace();
		}
		try {
			HttpResponse response = client.execute(post);
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			textResult = sb.toString();

		} catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException");
			return "";
			// e.printStackTrace();
		} catch (IOException e) {
			// Log.d(TAG, "IOException");
			e.printStackTrace();
		}
		Log.d(TAG, textResult);

		return textResult;
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

}
