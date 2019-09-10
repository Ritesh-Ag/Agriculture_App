package com.example.myapplication.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class assignedfragment extends Fragment {

    private static final String TAG = "assignedfragment";
    private ArrayList<String> Id;
    private ArrayList<String> Address;
    private DdapendingassignedAdapter ddaassignedAdapter;
    private String urlget = "http://13.235.100.235:8000/api/locations/dda/assigned";
    private String token;
    private String villagename;
    private String blockname;
    private String district;
    private String state;
    private boolean isReferesh;

    public assignedfragment() {
        Id = new ArrayList<String>(3);
        Address = new ArrayList<String>(3);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ongoing,container,false);
        isReferesh = false;
        ddaassignedAdapter = new DdapendingassignedAdapter(getActivity(),Id,Address);
        RecyclerView review = view.findViewById(R.id.recyclerViewongoing);
        review.setAdapter(ddaassignedAdapter);
        review.setLayoutManager( new LinearLayoutManager(getActivity()));

        SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token","");
        Log.d(TAG, "onCreateView: "+token);

        Log.d(TAG, "onCreateView: inflated fragment_ongoing");
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlget, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject c = jsonArray.getJSONObject(i);
                        Id.add(c.getString("id"));
                        villagename = c.getString("village_name");
                        blockname = c.getString("block_name");
                        district = c.getString("district");
                        state = c.getString("state");
                        Address.add(villagename+","+blockname+","+district+","+state);
                        ddaassignedAdapter.notifyDataSetChanged();
                    }
                }catch (JSONException e){
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error );
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonObjectRequest);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        isReferesh = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        isReferesh = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if(isReferesh)
        {
            getFragmentManager().beginTransaction().detach(assignedfragment.this)
                    .attach(assignedfragment.this).commit();
            Log.d(TAG, "onResume: REFRESH");
            isReferesh = false;
        }
}
}
