package com.greenla.app.greenla;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureSet;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.util.ListChangedEvent;
import com.esri.arcgisruntime.util.ListChangedListener;
import com.esri.arcgisruntime.util.ListenableList;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapActivity extends AppCompatActivity {

    private MapView mMapView;

    private LocationDisplay mLocationDisplay;

    private GraphicsOverlay graphicsOverlay;
    private LocatorTask locator = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
    private Spinner spinner;

    //final private ListenableList<GraphicsOverlay> allGraphics;

    private FeatureLayer gardenLayer;
    private FeatureLayer waterLayer;

    private GraphicsOverlay mGraphicsOverlay;
    private Point mStart;
    private Point mEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setPrompt("Search for nearby produce buyers:");
        spinner.bringToFront();

        mMapView = findViewById(R.id.mapView);

        setupMap();
        setupLocationDisplay();

        mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
            @Override
            public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
                if (graphicsOverlay == null) {
                    graphicsOverlay = new GraphicsOverlay();
                    mMapView.getGraphicsOverlays().add(graphicsOverlay);
                    setupSpinner();
                    setupPlaceTouchListener();
                    setupNavigationChangedListener();
                    mMapView.removeViewpointChangedListener(this);
                }
            }
        });

        createGraphicsOverlay();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(MapActivity.this, GardenPage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay.startAsync();
        } else {
            Toast.makeText(MapActivity.this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

    private void createGraphicsOverlay() {
        mGraphicsOverlay = new GraphicsOverlay();
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    }

    private void setMapMarker(Point location, SimpleMarkerSymbol.Style style, int markerColor, int outlineColor) {
        float markerSize = 8.0f;
        float markerOutlineThickness = 2.0f;
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(style, markerColor, markerSize);
        pointSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, outlineColor, markerOutlineThickness));
        Graphic pointGraphic = new Graphic(location, pointSymbol);
        mGraphicsOverlay.getGraphics().add(pointGraphic);
    }


    private void setStartMarker(Point location) {
        mGraphicsOverlay.getGraphics().clear();
        setMapMarker(location, SimpleMarkerSymbol.Style.DIAMOND, Color.rgb(226, 119, 40), Color.BLUE);
        mStart = location;
        mEnd = null;
    }

    private void setEndMarker(Point location) {
        setMapMarker(location, SimpleMarkerSymbol.Style.SQUARE, Color.rgb(40, 119, 226), Color.RED);
        mEnd = location;
        // findRoute();
    }

    private void mapClicked(Point location) {
        if (mStart == null) {
            // Start is not set, set it to a tapped location
            setStartMarker(location);
        } else if (mEnd == null) {
            // End is not set, set it to the tapped location then find the route
            setEndMarker(location);
        } else {
            // Both locations are set; re-set the start to the tapped location
            setStartMarker(location);
        }
    }

    private void setupMap() {
        if (mMapView != null) {

            String itemId = "c7cb6a2e5b4e4abfb3847013458bd3b7"; //map
            Portal portal = new Portal("https://www.arcgis.com", false);
            PortalItem portalItem = new PortalItem(portal, itemId);
            ArcGISMap map = new ArcGISMap(portalItem);

            String gardensID = "2e4b3df6ba4b44969a3bc9827de746b3";
            String waterID = "852b85de2c7d496bad0b784ec6e0f20b";
            PortalItem gardenLayerItem = new PortalItem(portal, gardensID);
            PortalItem waterLayerItem = new PortalItem(portal, waterID);
            //FeatureTable gardens = gardenLayer.getFeatureLayer()
            //

            mMapView.setMap(map);

            mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
                @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                    android.graphics.Point screenPoint = new android.graphics.Point(
                            Math.round(e.getX()),
                            Math.round(e.getY()));
                    Point mapPoint = mMapView.screenToLocation(screenPoint);
                    mapClicked(mapPoint);
                    return super.onSingleTapConfirmed(e);
                }
            });

            setupLocationDisplay();

        }
    }

    private void findPlaces(String placeCategory) {
        GeocodeParameters parameters = new GeocodeParameters();
        Point searchPoint;

        if (mMapView.getVisibleArea() != null) {
            searchPoint = mMapView.getVisibleArea().getExtent().getCenter();
            if (searchPoint == null) {
                return;
            }
        } else {
            return;
        }
        parameters.setPreferredSearchLocation(searchPoint);
        parameters.setMaxResults(50);

        List<String> outputAttributes = parameters.getResultAttributeNames();
        outputAttributes.add("Place_addr");
        outputAttributes.add("PlaceName");

        // Execute the search and add the places to the graphics overlay.
        final ListenableFuture<List<GeocodeResult>> results = locator.geocodeAsync(placeCategory, parameters);
        results.addDoneListener(() -> {
            try {
                ListenableList<Graphic> graphics = graphicsOverlay.getGraphics();
                graphics.clear();
                List<GeocodeResult> places = results.get();
                for (GeocodeResult result : places) {

                    // Add a graphic representing each location with a simple marker symbol.
                    SimpleMarkerSymbol placeSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, Color.MAGENTA, 10);
                    //placeSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.WHITE, 2));
                    Log.i("here: ", "i'm here");
                    Graphic graphic = new Graphic(result.getDisplayLocation(), placeSymbol);
                    java.util.Map<String, Object> attributes = result.getAttributes();

                    Log.i("attribute: ", attributes.toString());

                    // Store the location attributes with the graphic for later recall when this location is identified.
                    for (String key : attributes.keySet()) {
                        String value = attributes.get(key).toString();
                        Log.i("value: ", value);
                        graphic.getAttributes().put(key, value);

                    }
                    graphics.add(graphic);
                }
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void showCalloutAtLocation(Graphic graphic, Point mapPoint) {
        Callout callout = mMapView.getCallout();
        TextView calloutContent = new TextView(getApplicationContext());

        callout.setLocation(graphic.computeCalloutLocation(mapPoint, mMapView));
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setText(Html.fromHtml("<b>" + graphic.getAttributes().get("PlaceName").toString() + "</b><br>" + graphic.getAttributes().get("Place_addr").toString()));
        callout.setContent(calloutContent);
        callout.show();
    }

    private void setupSpinner() {
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                findPlaces(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        findPlaces(spinner.getSelectedItem().toString());
    }

    private void setupNavigationChangedListener() {
        mMapView.addNavigationChangedListener(navigationChangedEvent -> {
            if (!navigationChangedEvent.isNavigating()) {
                mMapView.getCallout().dismiss();
                findPlaces(spinner.getSelectedItem().toString());
            }
        });
    }

    private void setupPlaceTouchListener() {
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                // Dismiss a prior callout.
                mMapView.getCallout().dismiss();

                // get the screen point where user tapped
                final android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));

                System.out.println("User tapped map here." + mMapView.getAttributionText());

                // identify graphics on the graphics overlay
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false, 2);
                System.out.println();

                final ListenableList<GraphicsOverlay> allGraphics = mMapView.getGraphicsOverlays();

                identifyGraphic.addDoneListener(() -> {
                    try {
                        //get graphics for nearby markets
                        IdentifyGraphicsOverlayResult graphicsResult = identifyGraphic.get();
                        // get the list of graphics returned by identify graphic overlay
                        List<Graphic> graphicList = graphicsResult.getGraphics();

                        // get the first graphic selected and show its attributes with a callout
                        if (!graphicList.isEmpty()){
                            showCalloutAtLocation(graphicList.get(0), mMapView.screenToLocation(screenPoint));
                        }
                    } catch (InterruptedException | ExecutionException exception) {
                        exception.printStackTrace();
                    }
                });

                GraphicsOverlay staticResult = allGraphics.get(0);

                Log.w("User tapped: ", Integer.toString(allGraphics.size()));

                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyStaticGraphic = mMapView.identifyGraphicsOverlayAsync(
                        staticResult, screenPoint, 10.0, false, 2);


                identifyStaticGraphic.addDoneListener(() -> {
                    try {
                        //get graphics for nearby markets
                        IdentifyGraphicsOverlayResult graphicsResult = identifyStaticGraphic.get();
                        // get the list of graphics returned by identify graphic overlay
                        List<Graphic> graphicList = graphicsResult.getGraphics();

                        // get the first graphic selected and show its attributes with a callout
                        if (!graphicList.isEmpty()){
                            showCalloutAtLocation(graphicList.get(0), mMapView.screenToLocation(screenPoint));
                        }
                    } catch (InterruptedException | ExecutionException exception) {
                        exception.printStackTrace();
                    }
                });

                /*
                allGraphics.addListChangedListener(new ListChangedListener<GraphicsOverlay>() {
                    @Override
                    public void listChanged(ListChangedEvent<GraphicsOverlay> listChangedEvent) {
                        try {
                            //get graphics for nearby markets
                            // get the list of graphics returned by identify graphic overlay
                            //List<Graphic> graphicList = graphicsResult.getGraphics();

                            //get graphics for nearby markets
                            IdentifyGraphicsOverlayResult graphicsResult = identifyStaticGraphic.get();
                            // get the list of graphics returned by identify graphic overlay
                            List<Graphic> graphicList = graphicsResult.getGraphics();

                            // get the first graphic selected and show its attributes with a callout
                            if (!graphicList.isEmpty()){
                                showCalloutAtLocation(graphicList.get(0), mMapView.screenToLocation(screenPoint));
                            }
                        } catch (InterruptedException | ExecutionException exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                */

                return super.onSingleTapConfirmed(motionEvent);
            }
        });
    }

    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(MapActivity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MapActivity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MapActivity.this, requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(MapActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
        mLocationDisplay.startAsync();
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }

}
