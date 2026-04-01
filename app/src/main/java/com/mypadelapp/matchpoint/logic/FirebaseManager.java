package com.mypadelapp.matchpoint.logic;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    //Usuario predeterminado en la app (yo):
    private static final String EMAIL = "juanjo.carrizales98@gmail.com";
    private static final String PASSWORD = "123456";

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Login automático al iniciar la app y registrar los datos:
    public static void loginAutomatico(Runnable onExito, Runnable onError) {
        if(auth.getCurrentUser() != null) {
            onExito.run();
            return;
        }
        auth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnSuccessListener(result -> onExito.run())
                .addOnFailureListener(e -> {
                    System.err.println("Error del login: " + e.getMessage());
                    onError.run();
                });
    }

    //Subimos el partido acabado a Firestore:
    public static void subirPartido(int duracion, int setsP1, int setsP2, int ganador, int puntosGanados, int puntosPerdidos,
                                    double latitud, double longitud) {
        if(auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        //Hora y día de la semana:
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int horaInicio = cal.get(Calendar.HOUR_OF_DAY);
        int minutosInicio = cal.get(Calendar.MINUTE);
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);

        //Duración media de los puntos:
        int totalPuntos = puntosGanados + puntosPerdidos;
        int duracionMediaPuntos = totalPuntos > 0 ? duracion / totalPuntos : 0;

        Map<String, Object> partido = new HashMap<>();
        partido.put("fecha", System.currentTimeMillis());
        partido.put("duracion_total", duracion);
        partido.put("setst_pareja1", setsP1);
        partido.put("setst_pareja2", setsP2);
        partido.put("ganador", ganador);
        partido.put("puntos_ganados", puntosGanados);
        partido.put("puntos_perdidos", puntosPerdidos);
        partido.put("duracion_media_punto", duracionMediaPuntos);
        partido.put("hora_inicio", horaInicio);
        partido.put("minutos_inicio", minutosInicio);
        partido.put("dia_semana", diaSemana);
        partido.put("latitud", latitud);
        partido.put("longitud", longitud);

        db.collection("usuarios")
                .document(uid).collection("partidos").add(partido)
                .addOnSuccessListener(ref -> System.out.println("Partido subido: " + ref.getId()))
                .addOnFailureListener(e -> System.out.println("Error al subir el partido: " + e.getMessage()));
    }
}
