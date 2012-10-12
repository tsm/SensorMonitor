package com.tomszom.sensormonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.preference.PreferenceManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SensorMonitorActivity extends ListActivity {
	private final String TAG = getClass().getName();
	private ListView lv;
	private boolean openport=false;
	public static ArrayList<Measurement> measurementArrayList; // TODO make singleton?
	public String[] months;
	
	Handler handler = new Handler(new Callback() {
        public boolean handleMessage(final Message msg) {
            runOnUiThread(new Runnable() {
                 public void run() {
                     //pb.setVisibility(ProgressBar.INVISIBLE); //od�wie�enie layoutu
                     switch(msg.arg1){
                     case 0 : openport=false;//finish();
                     	break;
                     //case 1 : tvErr.setText(getResources().getString(R.string.error_connection));
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
			String device = items.get(position).getDevice();
			if (device != null) {
				TextView tv_device = (TextView) v.findViewById(R.id.tv_device);
				if (tv_device != null) {
					tv_device.setText(device);
				}
				TextView tv_measurement = (TextView) v.findViewById(R.id.tv_measurement);
				TextView tv_value = (TextView) v.findViewById(R.id.tv_value);
				if (tv_measurement != null) {
					tv_measurement.setText(items.get(position).getMeasurement());
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
        //setListAdapter(new MeasurementAdapter(this, R.layout.date_row, measurementArrayList));
        
        Log.d(TAG,"measurementsListActivity starts");
    }
    
    public void openPort(){
    	if(!openport){
    		openport=true;
	    	new Thread(new Runnable() {
				  public void run() {
			    		ServerSocket ss;
			    		Socket s;
			    		Boolean end = false;
			    		try {						  
				            ss = new ServerSocket(26123);
				            //Server is waiting for client here, if needed
		                    s = ss.accept();
				            while(!end){
				                    
				                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
				                    PrintWriter output = new PrintWriter(s.getOutputStream(),true); //Autoflush
				                    String st = input.readLine();
				                    Log.d(TAG, "Received: "+st);
				                    // tu jakie� przetwarzanie danych
				                    if ( 1==2 ){ end = true; } // STOPPING conditions
	
				            }
				            s.close();
				            ss.close();  
						  } catch (UnknownHostException e) {
				            // TODO Auto-generated catch block
				            //e.printStackTrace();
				            end=true;
						  } catch (IOException e) {
				            // TODO Auto-generated catch block
				            //e.printStackTrace();
				            end=true;
						  }
						  Message message = new Message();
						  //if(login.equals("test") && pass.equals("test")){
							message.arg1 = 0;                		
						//  }
		                //else message.arg1=2;				 
		                handler.sendMessage(message);
		                openport=false;
				  }
			       }).start();
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	//refreshList();
    }
    
    public void refreshList(){
        measurementArrayList= new ArrayList<Measurement>();
        List<NameValuePair> pair = new ArrayList<NameValuePair>();
        pair.add(new BasicNameValuePair("action", "get_subscriptions"));
        //String respond = sendPost(pair); //tu co� kurcze sypie b��dami
//        StringTokenizer st = new StringTokenizer(respond); // tutaj przyda�oby si� rozwali� odpowied� zamiast r�cznie
//		if (st.hasMoreTokens()) {
//			if (st.nextToken().equalsIgnoreCase("ok")) {
//				if (st.hasMoreTokens()) {
//					return Long.parseLong(st.nextToken());
//				}
//			}
//		}

        measurementArrayList.add(new Measurement("Fakedruino1","Temperature","")); //Reczne dodanie do listy

        setListAdapter(new MeasurementAdapter(this, R.layout.date_row, measurementArrayList));
        lv = getListView();
        lv.setTextFilterEnabled(true);
        
        //Ustawiam listener dla listy
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
            Log.i(TAG, "Kliknieto "+position+" id:"+id);
            openPort();
          }
        });
    }
    
    public void get_measurements(View target) {
//    	Log.d(TAG, "Launch User Configuration...");
//    	Intent intent = new Intent().setClass(this,
//				UserConfigurationActivity.class);
//		this.startActivityForResult(intent, 0);
    	refreshList();
	}
    
	public String sendPost(List<NameValuePair> pairs) {
//		SharedPreferences prefs = PreferenceManager
//				.getDefaultSharedPreferences(getBaseContext()); 
//																	
//		String serverAddress = prefs
//				.getString(
//						this.getResources().getString(
//								R.string.serverAddressOption), "");
		//String serverAddress = "192.12.8.100";
		String serverAddress = "http://tomszom.com/";
		SensorMonitor app = (SensorMonitor) this.getApplication();
		HttpClient client = app.getHttpClient();
		HttpPost post = new HttpPost(serverAddress);
		Log.d(TAG, serverAddress);
		BufferedReader in = null;


		String textResult = "";
		
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
    
    public void configuration(View target) {
    	//Log.d(TAG, "Launch Car Configuration...");
    	//Intent intent = new Intent().setClass(this,
		//		configurationActivity.class);
		//this.startActivityForResult(intent, 0);
	}
    

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    
}