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

public class CronometroFragment extends Fragment {

    private PartidoPadel partido;
    private boolean corriendo = false;
    private int segundos = 0;
    private Handler handler;
    private Runnable runnable;
    private TextView txtTiempoRef;
    private TextView txtKcalRef;
    private Button botonPlayPausaRef;

    public static CronometroFragment newInstance(PartidoPadel partido) {
        CronometroFragment fragment = new CronometroFragment();
        fragment.partido = partido;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cronometro, container, false);

        txtTiempoRef   = view.findViewById(R.id.txtTiempo);
        txtKcalRef     = view.findViewById(R.id.txtKcal);
        botonPlayPausaRef  = view.findViewById(R.id.botonPlayPausa);
        Button botonReiniciar  = view.findViewById(R.id.botonReiniciar);

        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (corriendo) {
                    segundos++;
                    partido.setDuracionSegundos(segundos);
                    // Formato MM:SS:
                    int min = segundos / 60;
                    int seg = segundos % 60;
                    txtTiempoRef.setText(String.format("%02d:%02d", min, seg));

                    // Cálculo de kcal (300 kcal/hora = 5 kcal/min):
                    int kcal = (int)(segundos * 300.0 / 3600);
                    txtKcalRef.setText(kcal + " kcal");

                    handler.postDelayed(this, 1000);
                }
            }
        };

        botonPlayPausaRef.setOnClickListener(v -> {
            if (!corriendo) {
                // Iniciar:
                corriendo = true;
                botonPlayPausaRef.setText("||");
                partido.iniciarPartido();
                partido.setCronometroActivo(true);
                handler.postDelayed(runnable, 1000);
            } else {
                // Pausar:
                corriendo = false;
                botonPlayPausaRef.setText("▶");
                partido.setCronometroActivo(false);
            }
        });

        botonReiniciar.setOnClickListener(v -> {
            // Confirmación antes de reiniciar:
            new AlertDialog.Builder(getContext())
                    .setTitle("Reiniciar partido")
                    .setMessage("¿Seguro que quieres reiniciar?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        corriendo = false;
                        segundos = 0;
                        botonPlayPausaRef.setText("▶");
                        txtTiempoRef.setText("00:00");
                        txtKcalRef.setText("0 kcal");
                        partido.finalizarPartidoBD();
                        partido.reiniciar();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Si el partido acabó, reseteamos la pantalla:
        if (!partido.partidoEmpezado() && !partido.cronometroActivo()) {
            corriendo = false;
            segundos = 0;
            if (txtTiempoRef != null) txtTiempoRef.setText("00:00");
            if (txtKcalRef != null) txtKcalRef.setText("0 kcal");
            if (botonPlayPausaRef != null) botonPlayPausaRef.setText("▶");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnable);
    }
}