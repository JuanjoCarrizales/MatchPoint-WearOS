package com.mypadelapp.matchpoint.logic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
public class LocationManager {
    private final FusedLocationProviderClient fusedClient;
    private final Context context;
    private double latitud = 0;
    private double longitud = 0;

    public LocationManager(Context context) {
        this.context = context;
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    //Obtención de la última ubicación conocida:
    public void ultimaUbicacion(Runnable onExito) {
        //Ubicaciones de prueba:
        latitud = 41.6182;
        longitud = 2.2686;
        System.out.println("Ubicación obtenida: " + latitud + ", " + longitud);
        onExito.run();
        /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
               latitud = location.getLatitude();
                longitud = location.getLongitude();
                System.out.println("Ubicación obtenida: " + latitud + ", " + longitud);
            }
            onExito.run();
        });*/
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }
}
