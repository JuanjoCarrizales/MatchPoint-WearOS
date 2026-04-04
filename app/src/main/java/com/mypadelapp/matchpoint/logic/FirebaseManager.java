package com.mypadelapp.matchpoint.logic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
        if (auth.getCurrentUser() != null) {
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
    public static void subirPartido(DatabaseManager localDb, int idPartido, int duracion, int ganador,
                                    double latitud, double longitud, String fechaInicio, String fechaFin) {

        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        //Hora y día de la semana:
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);

        //Datos del partido:
        Map<String, Object> partido = new HashMap<>();
        partido.put("fecha_inicio", fechaInicio);
        partido.put("fecha_fin", fechaFin);
        partido.put("duracion_total", duracion);
        partido.put("ganador", ganador);
        partido.put("dia_semana", diaSemana);
        partido.put("latitud", latitud);
        partido.put("longitud", longitud);

        //Subimos el partido y sus puntos:
        db.collection("usuarios")
                .document(uid).collection("partidos").add(partido)
                .addOnSuccessListener(docRef -> {
                    System.out.println("Partido subido: " + docRef.getId());
                    //Puntos como subcolección del partido:
                    subirPuntos(localDb, docRef.getId(), uid, idPartido);
                }).addOnFailureListener(e -> System.out.println("Error al subir el partido: " + e.getMessage()));
    }

    //Subimos los puntos del partido:
    private static void subirPuntos(DatabaseManager localDb, String idPartidoFirestore, String uid, int idPartidoLocal) {
        //Lectura de los puntos de SQLite:
        SQLiteDatabase sqlDb = localDb.getReadableDatabase();
        Cursor cursor = sqlDb.rawQuery(
                "SELECT timestamp, pareja_ganadora, puntos_pareja1, puntos_pareja2," +
                    "juegos_pareja1, juegos_pareja2, sets_pareja1, sets_pareja2, tiebreak " +
                    "FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(idPartidoLocal)}
        );

        while (cursor.moveToNext()) {
            Map<String, Object> punto = new HashMap<>();
            punto.put("timestamp",       cursor.getInt(0));
            punto.put("pareja_ganadora", cursor.getInt(1));
            punto.put("puntos_pareja1",  cursor.getInt(2));
            punto.put("puntos_pareja2",  cursor.getInt(3));
            punto.put("juegos_pareja1",  cursor.getInt(4));
            punto.put("juegos_pareja2",  cursor.getInt(5));
            punto.put("sets_pareja1",    cursor.getInt(6));
            punto.put("sets_pareja2",    cursor.getInt(7));
            punto.put("tiebreak",        cursor.getInt(8) == 1);

            db.collection("usuarios")
                    .document(uid).collection("partidos").document(idPartidoFirestore)
                    .collection("puntos").add(punto)
                    .addOnFailureListener(e -> System.err.println("Error al subir punto: " + e.getMessage()));
            punto.clear();
        }

        int total = cursor.getCount();
        cursor.close();
        System.out.println("Puntos subidos: " + total);
    }
}
