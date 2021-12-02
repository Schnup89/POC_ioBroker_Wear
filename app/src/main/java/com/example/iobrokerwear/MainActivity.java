package com.example.iobrokerwear;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    public ArrayList<MenuItem> menuItems = new ArrayList<>();
    public MainMenuAdapter adapter;
    public static final String PREFS_NAME = "ioBSettings";
    public static final String sJSON_Config_ID = "0_userdata.0.wearos";
    public String sHost;
    public String sPort;
    public Boolean bListCreationRunning = false;
    public Boolean bHttpReqRunning = false;
    private Timer tmr_refresh = new Timer();
    Volley vol = null;
    WearableRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.main_menu_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(this));
        recyclerView.requestFocus();

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (tmr_refresh != null) {
            tmr_refresh.cancel();
            tmr_refresh.purge();
            tmr_refresh = null;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tmr_refresh != null) {
            tmr_refresh.cancel();
            tmr_refresh.purge();
            tmr_refresh = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        menuItems.clear();
        adapter = null;
        //Load Settings
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        sHost = settings.getString("settings_host","");
        sPort = settings.getString("settings_port", "8087");


        adapter = new MainMenuAdapter(this, menuItems, new MainMenuAdapter.AdapterCallback() {
            @Override
            public void onItemClicked(final Integer menuPosition) {
                //ReadOnly or ID empty... do nothing!
                if (menuItems.get(menuPosition).getId().isEmpty()  || Boolean.valueOf(menuItems.get(menuPosition).getRo().toString())) { return; }

                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);

                if (menuItems.get(menuPosition).getId().equals("settings")) {
                    Intent intent = new Intent(MainActivity.this, activity_settings.class);
                    startActivity(intent);
                    return;
                }
                if (menuItems.get(menuPosition).getType().equals("toggle")) {
                    String sUrl_toggleID = "http://" + sHost + ":" + sPort + "/toggle/"+ menuItems.get(menuPosition).getId();
                    ListenerWithTag httpRefrListener = new ListenerWithTag() {
                        @Override
                        public void onResponse(Object tag, String response) {
                            try {
                                JSONObject jToggleRes = new JSONObject(response);
                                menuItems.get(Integer.parseInt(tag.toString())).setVal(jToggleRes.getString("val"));
                                adapter.notifyDataSetChanged();
                            }catch (JSONException e){
                                //Nothing
                            }
                        }
                    };
                    callApi(sUrl_toggleID, httpRefrListener,menuPosition);
                }
            }
        });

        menuItems.add(new MenuItem("internal", "Einstellungen", "settings", "settings", "settings", false,""));
        adapter.notifyDataSetChanged();

        //Create State List
        create_states();

        recyclerView.setAdapter(adapter);
    }

    private void create_states(){
        if (bListCreationRunning) {
            return;
        }else{
            bListCreationRunning = true;
        }
        menuItems.add(0, new MenuItem("internal", "Lädt", "loading", "loading","", true, ""));
        adapter.notifyDataSetChanged();

        RequestQueue queue = vol.newRequestQueue(this);

        if (sHost.isEmpty()){
            menuItems.set(0, new MenuItem("internal", "Kein Host", "arrow_down_red", "arrow_down_red","", true, ""));
            adapter.notifyDataSetChanged();
            bListCreationRunning = false;
            return;
        }

        // Get JSON List with Configuration from ioBroker
        String sUrl_JConf = "http://" + sHost + ":" + sPort + "/getPlainValue/"+ sJSON_Config_ID;
        StringRequest httpReq_JConf = new StringRequest(Request.Method.GET, sUrl_JConf,
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String sRes = response.replace("\\r\\n", "");
                sRes = sRes.replace("\\","");
                try {
                    JSONObject jStates = new JSONObject(removeFirstandLast(sRes));
                    JSONArray jStatesArray = jStates.optJSONArray("states");
                    menuItems.remove(0);
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < jStatesArray.length(); i++) {
                        JSONObject jState = jStatesArray.getJSONObject(i);
                        menuItems.add(0, new MenuItem(jState.get("type").toString(),jState.get("name").toString(),jState.get("icon_on").toString(),jState.get("icon_off").toString(), jState.get("id").toString(), Boolean.valueOf(jState.get("readonly").toString()),""));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    menuItems.set(0, new MenuItem("internal", e.getMessage(),"error","error", "", true, ""));
                    adapter.notifyDataSetChanged();
                    bListCreationRunning = false;
                    return;
                }

                //Create Update-Timer
                tmr_refresh = new Timer();
                tmr_refresh.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (!bHttpReqRunning)  refresh_states();
                    }
                }, 0, 1*1000); //3 Seconds
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                menuItems.set(0, new MenuItem("internal", error.toString(),"error","error", "", true, ""));
                adapter.notifyDataSetChanged();
                bListCreationRunning = false;
                return;
            }
        });

        bListCreationRunning = false;
        // Add the request to the RequestQueue.
        queue.add(httpReq_JConf);
    }


    public interface ListenerWithTag {
        /** Called when a response is received. */
        public void onResponse(Object tag, String response);
    }

    private void refresh_states(){
        if (tmr_refresh == null) { return; }
        bHttpReqRunning = true;
        //Build Bulk URL
        String sIDS = "";
        for(int iPos=0; iPos<menuItems.size()-1; iPos++) {
            sIDS = iPos > 0 ? sIDS + "," + menuItems.get(iPos).getId() : menuItems.get(iPos).getId();
        }
        String sUrl = "http://" + sHost + ":" + sPort + "/getBulk/"+ sIDS;
        ListenerWithTag httpRefrListener = new ListenerWithTag() {
            @Override
            public void onResponse(Object tag, String response) {
                try {
                    JSONArray aRes = new JSONArray(response);
                    for (int i = 0; i < aRes.length(); i++) {
                        JSONObject jRes = aRes.getJSONObject(i);
                        if (menuItems.get(i).getVal() != jRes.get("val")) {
                            menuItems.get(i).setVal(jRes.get("val").toString());
                            adapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    //Nothing
                }
                bHttpReqRunning = false;
            }
        };
        callApi(sUrl,httpRefrListener, "");
    }

    public void callApi(String sUrl, final ListenerWithTag listener, final Object tag){
        StringRequest httpReq = new StringRequest(Request.Method.GET,
                sUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onResponse(tag, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                bHttpReqRunning = false;
            }
        });
        Volley.newRequestQueue(this).add(httpReq);
    }

    public static String removeFirstandLast(String str) {
        // Creating a StringBuilder object
        StringBuilder sb = new StringBuilder(str);
        // Removing the last character
        // of a string
        sb.deleteCharAt(str.length() - 1);
        // Removing the first character
        // of a string
        sb.deleteCharAt(0);
        // Converting StringBuilder into a string
        // and return the modified string
        return sb.toString();
    }

}



class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.RecyclerViewHolder> {

    private ArrayList<MenuItem> dataSource = new ArrayList<MenuItem>();

    public interface AdapterCallback{
        void onItemClicked(Integer menuPosition);
    }
    private AdapterCallback callback;

    private String drawableIcon;
    private Context context;

    public MainMenuAdapter(Context context, ArrayList<MenuItem> dataArgs, AdapterCallback callback){
        this.context = context;
        this.dataSource = dataArgs;
        this.callback = callback;
    }

    public static int getResId(String sIconName) {
        try {
            Field idField = R.drawable.class.getDeclaredField("icon_" + sIconName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return R.drawable.icon_unknown;
        }
    }


    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_menu_item,parent,false);

        RecyclerViewHolder recyclerViewHolder = new RecyclerViewHolder(view);

        return recyclerViewHolder;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder
    {
        RelativeLayout menuContainer;
        TextView menuItem;
        ImageView menuIcon;
        TextView menuIconText;

        public RecyclerViewHolder(View view) {
            super(view);
            menuContainer = view.findViewById(R.id.menu_container);
            menuItem = view.findViewById(R.id.menu_item);
            menuIcon = view.findViewById(R.id.menu_icon);
            menuIconText = view.findViewById(R.id.menu_icontext);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MenuItem data_provider = dataSource.get(position);

        holder.menuItem.setText(data_provider.getText());
        holder.menuIcon.setImageResource(getResId("unknown"));
        if (data_provider.getType().equals("internal")) {
            holder.menuIcon.setImageResource(getResId(data_provider.getIcon_on()));
        }
        if (data_provider.getType().equals("toggle")) {
            holder.menuIconText.setText("");
            if (data_provider.getVal() == "") {
                holder.menuIcon.setImageResource(getResId("unknown"));
            }else {
                if (data_provider.getVal().equalsIgnoreCase("on") || data_provider.getVal().equalsIgnoreCase("true") || data_provider.getVal().equals("1")) {
                    holder.menuIcon.setImageResource(getResId(data_provider.getIcon_on()));
                } else {
                    holder.menuIcon.setImageResource(getResId(data_provider.getIcon_off()));
                }
            }
        }else {
            if (data_provider.getType().equals("text")) {
                holder.menuIconText.setText(data_provider.getVal().replaceAll("[\\n\\t ]", ""));
                holder.menuIcon.setImageIcon(null);
            }
        }
        holder.menuContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if(callback != null) {
                    callback.onItemClicked(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

}

class MenuItem {
    private String type;
    private String text;
    private String icon_on;
    private String icon_off;
    private String id;
    private Boolean ro;
    private String val;

    public MenuItem(String type, String text, String icon_on, String icon_off, String stateID, Boolean readOnly, String value) {
        this.type = type;
        this.text = text;
        this.icon_on = icon_on;
        this.icon_off = icon_off;
        this.id = stateID;
        this.ro = readOnly;
        this.val = value;
    }

    public void setVal(String value) {
        this.val = value;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getType() { return type; }
    public String getText() { return text; }
    public String getIcon_on() { return icon_on; }
    public String getIcon_off() { return icon_off; }
    public String getId() { return id; }
    public Boolean getRo() { return ro; }
    public String getVal() { return val; }
}

