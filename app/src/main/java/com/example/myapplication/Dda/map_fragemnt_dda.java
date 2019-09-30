package com.example.myapplication.Dda;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class map_fragemnt_dda extends Fragment {
    private final String TAG = "map fragment";

    public GoogleMap map = null;
    private String url_pending = "http://13.235.100.235:8000/api/locations/dda/assigned";
    private String token;
    private String next;
    private SupportMapFragment mapFragment;
    private int count = 0;
    private ProgressBar pbar;

    private ArrayList<Double> latitude;
    private ArrayList<Double> longitude;
    private ArrayList<String> villname;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragemnt, container, false);
        mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        final SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");
        latitude = new ArrayList<>();
        longitude = new ArrayList<>();
        villname = new ArrayList<>();
        pbar = view.findViewById(R.id.pbar);


        next = url_pending;
        getMarkers(next);


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                Log.d(TAG, "onMapReady:value if map is : " + map);

                LatLng one = new LatLng(7.798000, 68.14712);
                LatLng two = new LatLng(37.090000, 97.34466);

                LatLng shimala = new LatLng(31.104815, 77.173401);
                LatLng jaipur = new LatLng(26.912434, 75.787270);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                LatLngBounds.Builder builder1 = new LatLngBounds.Builder();


                //add them to builder
                builder.include(one);
                builder.include(two);

                builder1.include(shimala);
                builder1.include(jaipur);

                LatLngBounds bounds = builder.build();
                LatLngBounds bounds1 = builder1.build();

                //get width and height to current display screen
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;

                // 20% padding
                int padding = (int) (width * 0.20);

                //set latlong bounds
                map.setLatLngBoundsForCameraTarget(bounds);

                //move camera to fill the bound to screen
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds1, width, height, padding));

                //set zoom to level to current so that you won't be able to zoom out viz. move outside bounds
                map.setMinZoomPreference(map.getCameraPosition().zoom);


            }
        });


        Log.d(TAG, "onCreateView: look me here " + mapFragment);
        return view;
    }

    void getMarkers(String url) {
        final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        final JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        Double lat = Double.valueOf(c.getString("latitude"));
                        Double lon = Double.valueOf(c.getString("longitude"));
                        String vill = c.getString("village_name");
                        latitude.add(lat);
                        longitude.add(lon);
                        villname.add(vill);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error);
                pbar.setVisibility(View.GONE);
                if (error instanceof NoConnectionError) {
                    Toast.makeText(getActivity(), "Check your internet connection", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "something went wrong", Toast.LENGTH_LONG).show();
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };

        requestQueue.add(jsonObjectRequest2);
        requestFinished(requestQueue);
    }

    private void requestFinished(RequestQueue queue) {

        queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {

            @Override
            public void onRequestFinished(Request<Object> request) {
                Log.d(TAG, "onRequestFinished: here too");
               /* if(count == 0)nextRequest();
                else if(count == 1)*/
                marklocation();

            }
        });

    }

    private void marklocation() {

        for (int i = 0; i < latitude.size(); i++) {
            MarkerOptions Dlocation = new MarkerOptions().position(new LatLng(latitude.get(i), longitude.get(i))).title(villname.get(i)).icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_action_name));
            map.addMarker(Dlocation);
            if (i == 0) {
                pbar.setVisibility(View.GONE);

            }
        }
    }

   /* void nextRequest(){
        count = 1;
      next= url_unassigned;
      getMarkers(next);


    }*/

    BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
