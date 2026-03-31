package com.mypadelapp.matchpoint.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mypadelapp.matchpoint.R;
import com.mypadelapp.matchpoint.logic.PartidoPadel;

public class EstadisticasFragment extends Fragment {
    private PartidoPadel partido;
    private TextView txtTotalPartidos, txtPuntosGanados, txtPuntosPerdidos;
    private TextView txtVentajasP1, txtVentajasP2, txtDuracionMedia;
    public static EstadisticasFragment newInstance(PartidoPadel partido) {
        EstadisticasFragment fragment = new EstadisticasFragment();
        fragment.partido = partido;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        txtTotalPartidos = view.findViewById(R.id.txtTotalPartidos);
        txtPuntosGanados = view.findViewById(R.id.txtPuntosGanados);
        txtPuntosPerdidos = view.findViewById(R.id.txtPuntosPerdidos);
        txtVentajasP1 = view.findViewById(R.id.txtVentajasP1);
        txtVentajasP2 = view.findViewById(R.id.txtVentajasP2);
        txtDuracionMedia = view.findViewById(R.id.txtDuracionMedia);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Cada vez que se vuelve a estadísticas, se recargan los datos:
        if (txtTotalPartidos == null) return;

        txtTotalPartidos.setText("Partidos: " + partido.getDatabase().getTotalPartidos());
        txtPuntosGanados.setText("Puntos Ganados: " + partido.getDatabase().getPuntosGanados());
        txtPuntosPerdidos.setText("Puntos Perdidos: " + partido.getDatabase().getPuntosPerdidos());
        txtVentajasP1.setText("Ventajas P1: " + partido.getDatabase().getVentajasPareja1());
        txtVentajasP2.setText("Ventajas P2: " + partido.getDatabase().getVentajasPareja2());
        int duracionTotal = partido.getDatabase().getDuracionMedia();
        int minutos = duracionTotal / 60;
        int segundos = duracionTotal % 60;
        txtDuracionMedia.setText(String.format("Duración media: %02d:%02d",minutos, segundos));
    }
}