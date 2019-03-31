package com.greenla.app.greenla;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class BusinessActivity extends AppCompatActivity {

    private SceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //load 3D map
        mSceneView = findViewById(R.id.sceneView);
        setupScene();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupScene() {
        if (mSceneView != null) {
            Basemap.Type basemapType = Basemap.Type.IMAGERY_WITH_LABELS;
            ArcGISScene scene = new ArcGISScene(basemapType);
            mSceneView.setScene(scene);
            addGreenLayer();

            setElevationSource(scene);
        }
    }

    private void addGreenLayer() {
        String url = "https://services3.arcgis.com/GVgbJbqm8hXASVYi/arcgis/rest/services/Parks_and_Open_Space/FeatureServer/0";
        final ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(url);
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

        mSceneView.getScene().getOperationalLayers().add(featureLayer);

        Camera camera = new Camera(
                33.950896,
                -118.525341,
                16000.0,
                0.0,
                50.0,
                0);
        mSceneView.setViewpointCamera(camera);
    }

    private void setElevationSource(ArcGISScene scene) {
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        scene.getBaseSurface().getElevationSources().add(elevationSource);
    }

}
