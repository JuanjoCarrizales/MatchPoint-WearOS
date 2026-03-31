/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypadelapp.matchpoint.logic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.time.LocalDateTime;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String DB_NAME = "padelapp.db";
    private static final int DB_VERSION = 1;
    
    //Constructor: Conecta a la BBDD y crea tablas si no existen:
    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        getJugadorPrincipal();
    }

    //Creación de las tablas si no existen:
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS partidos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha_inicio TEXT NOT NULL,
                fecha_fin TEXT,
                duracion_total INTEGER,
                nombre_pareja1 TEXT DEFAULT 'Pareja 1',
                nombre_pareja2 TEXT DEFAULT 'Pareja 2',
                sets_pareja1 INTEGER DEFAULT 0,
                sets_pareja2 INTEGER DEFAULT 0,
                ganador INTEGER,
                partido_finalizado INTEGER DEFAULT 0
            )
        """);
        
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS puntos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_partido INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                pareja_ganadora INTEGER NOT NULL,
                puntos_pareja1 INTEGER,
                puntos_pareja2 INTEGER,
                juegos_pareja1 INTEGER,
                juegos_pareja2 INTEGER,
                sets_pareja1 INTEGER,
                sets_pareja2 INTEGER,
                ganador INTEGER,
                tiebreak INTEGER,
                FOREIGN KEY (id_partido) REFERENCES partidos(id)
            )
        """);
        
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS jugadores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                email TEXT UNIQUE,
                password_hash TEXT,
                creado_desde_app INTEGER,
                fecha_creacion TEXT
            )
        """);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //Uso para futuras versiones
    }

    //Inicio de nuevo partido (devuelve id_partido):
    public int iniciarPartido(String nombreP1, String nombreP2) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha_inicio", LocalDateTime.now().toString());
        values.put("nombre_pareja1", nombreP1);
        values.put("nombre_pareja2", nombreP2);

        long id = db.insert("partidos", null, values);
        return (int) id;
    }
    
    //Guardado de puntos:
    public void guardarPuntos(int idPartido, int timestamp, int parejaGanadora,
            int puntosP1, int puntosP2, int juegosP1, int juegosP2, int setsP1,
            int setsP2, boolean tiebreak) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_partido", idPartido);
        values.put("timestamp", timestamp);
        values.put("pareja_ganadora", parejaGanadora );
        values.put("puntos_pareja1", puntosP1);
        values.put("puntos_pareja2", puntosP2);
        values.put("juegos_pareja1", juegosP1);
        values.put("juegos_pareja2", juegosP2);
        values.put("sets_pareja1", setsP1);
        values.put("sets_pareja2",setsP2);
        values.put("tiebreak", tiebreak);

        db.insert("puntos", null, values);
    }
    
    //Fin del partido:
    public void finalizarPartido(int idPartido, int duracionTotal, int setsP1,
            int setsP2, int ganador) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha_fin", LocalDateTime.now().toString());
        values.put("duracion_total", duracionTotal);
        values.put("sets_pareja1", setsP1);
        values.put("sets_pareja2", setsP2);
        values.put("ganador", ganador);
        values.put("partido_finalizado", 1);

        db.update("partidos", values, "id = ?",
                new String[]{String.valueOf(idPartido)});
    }

    //Elimina el partido y sus puntos si se reinicia manualmente:
    public void borrarPartido(int idPartido) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("puntos", "id_partido = ?", new String[]{String.valueOf(idPartido)});
        db.delete("partidos", "id = ?", new String[]{String.valueOf(idPartido)});
    }

    //Creación-verificación del jugador principal(yo):
    public int getJugadorPrincipal() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM jugadores WHERE id = 1", null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return 1;
        }
        cursor.close();

        //Si no existe, creamos el jugador principal:
        ContentValues values = new ContentValues();
        values.put("id", 1);
        values.put("nombre", "Jugador Principal");
        values.put("creado_desde_app", 1);
        values.put("fecha_creacion", LocalDateTime.now().toString());

        db.insert("jugadores", null, values);
        return 1;
    }

    //Obtenemos el total de los partidos jugados:
    public int getTotalPartidos() {
        return consulta(
                "SELECT COUNT(*) FROM partidos WHERE partido_finalizado = 1");
    }
  
    //Obtenemos Nº de victorias de la pareja 1:
    public int getVictoriasPareja1() {
        return consulta(
                "SELECT COUNT(*) FROM partidos WHERE partido_finalizado = 1 AND ganador = 1");
    }
    
    //Obtenemos Nº de derrotas de la pareja 1:
    public int getDerrotasPareja1() {
        return consulta(
                "SELECT COUNT(*) FROM partidos WHERE partido_finalizado = 1 AND ganador = 2");
    }

    //Puntos ganados por la pareja 1:
    public int getPuntosGanados() {
        SQLiteDatabase db = getReadableDatabase();
        //Buscamos id del último partido acabado:
        Cursor c = db.rawQuery(
                "SELECT MAX(id) FROM partidos WHERE partido_finalizado = 1", null);
        //Si no hay partido acabado, devuelve 0:
        if (!c.moveToFirst() || c.isNull(0)) {
            c.close();
            return 0;
        }
        int idUltimo = c.getInt(0);
        c.close();
        //Contamos los puntos ganados por la pareja 1:
        return consulta(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = " +
                    "(SELECT MAX(id) FROM partidos WHERE partido_finalizado = 1) " +
                    "AND pareja_ganadora = 1");
    }

    //Puntos perdidos por la pareja 1:
    public int getPuntosPerdidos() {
        SQLiteDatabase db = getReadableDatabase();
        //Buscamos id del último partido acabado:
        Cursor c = db.rawQuery(
                "SELECT MAX(id) FROM partidos WHERE partido_finalizado = 1", null);
        //Si no hay partido acabado, devuelve 0:
        if (!c.moveToFirst() || c.isNull(0)) {
            c.close();
            return 0;
        }
        int idUltimo = c.getInt(0);
        c.close();
        //Contamos los puntos ganados por la pareja 2:
        return consulta(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = " +
                    "(SELECT MAX(id) FROM partidos WHERE partido_finalizado = 1) " +
                    "AND pareja_ganadora = 2");
    }

    //Ventajas de la pareja 1:
    public int getVentajasPareja1() {
        return consulta(
                "SELECT COUNT(*) FROM puntos WHERE pareja_ganadora = 1 AND " +
                    "puntos_pareja1 = 3 AND puntos_pareja2 = 3 ");
    }

    //Ventajas de la pareja 2:
    public int getVentajasPareja2() {
        return consulta(
                "SELECT COUNT(*) FROM puntos WHERE pareja_ganadora = 2 AND " +
                    "puntos_pareja1 = 3 AND puntos_pareja2 = 3 ");
    }

    //Obtención de la duración media de los partidos:
    public int getDuracionMedia() {
        return consulta(
                "SELECT AVG(duracion_total) FROM partidos WHERE partido_finalizado = 1");
    }

    //Devolución de las queries:
    private int consulta(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        int resultado = 0;
        if (cursor.moveToFirst()) {
            resultado = cursor.getInt(0);
        }
        cursor.close();
        return resultado;
    }
}