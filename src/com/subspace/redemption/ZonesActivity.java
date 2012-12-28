package com.subspace.redemption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import com.subspace.network.*;
import com.subspace.redemption.database.DataHelper;
import com.subspace.redemption.dataobjects.Zone;


import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class ZonesActivity extends ListActivity  {
	
	static class ViewHolder
	{	    
	    TextView topText;
	    TextView bottomText;
	}
	
	static final String TAG = "Subspace";	
	
	ArrayList<Zone> zones;
	ZoneAdapter adapter;
	DataHelper db;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {    	
    	  super.onCreate(savedInstanceState);
    	  setContentView(R.layout.zones_activity);
    	  
    	  zones = new ArrayList<Zone>();
    	  adapter = new ZoneAdapter(this, R.layout.zone_item, zones);
    	  db = new DataHelper(this);
    	  
          setListAdapter(adapter);
    }
    
    
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {		 
		super.onListItemClick(l, v, position, id);
		
		
	}



	@Override
	protected void onStart() 
    {		
		super.onStart();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);		
		String directoryServer =  prefs.getString("pref_directoryServer", "");		
		new DownloadZonesTask(this).execute(directoryServer);
    }  
    
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    
    private class DownloadZonesTask extends AsyncTask<String, String,ArrayList<DirectoryZone>> {
    	private ProgressDialog _dialog;
    	private Activity _activity;
    	public DownloadZonesTask(Activity activity)
    	{
    		_activity = activity;
    		_dialog = new ProgressDialog(_activity);

    	}
    	
        protected void onPreExecute() {
            _dialog.setMessage("Progress start");
            _dialog.show();
        }
        
        
        @Override
		protected void onProgressUpdate(String... progress) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(progress);
			_dialog.setMessage(progress[0]);
			Log.d(TAG, progress[0]);
		}

		protected ArrayList<DirectoryZone> doInBackground(String... server) 
        {       	
        	
        	ArrayList<DirectoryZone> zones =null;
        	try {		
        		NetworkDirectory nd = new NetworkDirectory();
        		zones = nd.Download(server[0]);
        		//sort by player count
        		Collections.sort(zones, new Comparator<DirectoryZone>(){
        			 
        	            public int compare(DirectoryZone o1, DirectoryZone o2) {
        	            	return (o1.PlayerCount>o2.PlayerCount ? -1 : (o1.PlayerCount==o2.PlayerCount ? 0 : 1));
        	            }
        	 
        	        });
        	} 
    		catch (Exception e)
    		{    			
    			Log.e(TAG, Log.getStackTraceString(e));    			
    		}	
        	return zones;        	
        }
        
        protected void onPostExecute(ArrayList<DirectoryZone> result) {
        	
        	List<Zone> zoneList = new ArrayList<Zone>();
        	//delete all zones
        	db.clearZones();
        	//referesh
        	for(DirectoryZone dz : result)
        	{
        		Zone zone = new Zone(dz);
        		//add to db        		
        		db.addZone(zone);
        		zoneList.add(zone);
        	}        	
        	
        	adapter.notifyDataSetChanged();        	
        	adapter.clear();     
        	
        	for(Zone z : zoneList)
        	{
        		adapter.add(z);
        	}
        	adapter.notifyDataSetChanged();
			
            if (_dialog.isShowing()) {
                _dialog.dismiss();
            }
        }


    }
    
    private class ZoneAdapter extends ArrayAdapter<Zone> {
        private ArrayList<Zone> items;        

        public ZoneAdapter(Context context, int textViewResourceId, ArrayList<Zone> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder;
        	Zone o = items.get(position);
        	if(convertView==null)
        	{
        		convertView = View.inflate(getContext(), R.layout.zone_item, null);
        		holder = new ViewHolder();
        		holder.topText = (TextView)convertView.findViewById(R.id.toptext);
        		holder.bottomText = (TextView)convertView.findViewById(R.id.bottomtext);
        		convertView.setTag(holder);
        	}
        	else {
        		holder = (ViewHolder)convertView.getTag();
        	}
        	
        	if(o!=null)
        	{
        		holder.topText.setText(o.Name + " : " + o.Population + " Players");
        	}
            return convertView;
        }
    }

}