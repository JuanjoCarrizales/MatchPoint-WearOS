package com.mypadelapp.matchpoint.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mypadelapp.matchpoint.R;
import com.mypadelapp.matchpoint.logic.PartidoPadel;

public class MarcadorFragment extends Fragment {

    private PartidoPadel partido;

    private Button botonP1Ref, botonP2Ref, botonDeshacerRef;
    private Runnable actualizarUIRef;

    public static MarcadorFragment newInstance(PartidoPadel partido) {
        MarcadorFragment fragment = new MarcadorFragment();
        fragment.partido = partido;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marcador, container, false);

        TextView txtJuegos  = view.findViewById(R.id.txtJuegos);
        TextView txtSets    = view.findViewById(R.id.txtSets);
        Button botonP1        = view.findViewById(R.id.botonPuntoP1);
        Button botonP2        = view.findViewById(R.id.botonPuntoP2);
        Button botonDeshacer  = view.findViewById(R.id.botonDeshacer);

        //Actualiza la UI con el estado actual del partido:
        Runnable actualizarUI = () -> {
            botonP1.setText(partido.getPuntuacion().split(" - ")[0]);
            botonP2.setText(partido.getPuntuacion().split(" - ")[1]);

            txtJuegos.setText(partido.getJuegosPareja1() + " JUEGOS " + partido.getJuegosPareja2());
            txtSets.setText(partido.getsetsPareja1() + " SETS " + partido.getsetsPareja2());

            if (partido.ganador()){
                partido.setCronometroActivo(false);

                //Muestra ventana con el ganador. Efectuar reinicio automático:
                new AlertDialog.Builder(getContext())
                        .setTitle("Partido acabado!")
                        .setMessage(partido.getGanador())
                        .setCancelable(false)//Evita que se toque fuera
                        .setPositiveButton("Nueva partida", (dialog, which) -> {
                            partido.finalizarPartidoBD();
                            partido.reiniciar();
                            if (actualizarUIRef != null) actualizarUIRef.run();
                        })
                        .show();
            }

            //Botones bloqueados si el cronómetro no está activo:
            boolean activo = partido.cronometroActivo();
            botonP1.setEnabled(activo);
            botonP2.setEnabled(activo);
            botonDeshacer.setEnabled(activo && partido.historial());
        };

        botonP1Ref = botonP1;
        botonP2Ref = botonP2;
        botonDeshacerRef = botonDeshacer;
        actualizarUIRef = actualizarUI;

        botonP1.setOnClickListener(v -> {
            partido.addPuntoPareja1();
            actualizarUI.run();
        });

        botonP2.setOnClickListener(v -> {
            partido.addPuntoPareja2();
            actualizarUI.run();
        });

        botonDeshacer.setOnClickListener(v -> {
            partido.deshacer();
            actualizarUI.run();
        });

        actualizarUI.run();
        return view;
    }

    //Refresca los botones al volver a esta página:
    @Override
    public void onResume() {
        super.onResume();
        if (actualizarUIRef != null) {
            actualizarUIRef.run();
        }
    }
}
