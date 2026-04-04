/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypadelapp.matchpoint.logic;
import android.content.Context;
import java.util.Stack;

public final class PartidoPadel {
    //Clase para guardar el estado del partido:
    private static class EstadoPartido {
        int puntosP1, puntosP2;
        int juegosP1, juegosP2;
        int setsP1, setsP2;
        boolean ventajaP1, ventajaP2;
        boolean tieBreak;
        int puntosTieBreakP1, puntosTieBreakP2;
        
        EstadoPartido (int puntosP1, int puntosP2, int juegosP1, int juegosP2,
                int setsP1, int setsP2, boolean ventajaP1, boolean ventajaP2,
                boolean tieBreak, int puntosTieBreakP1, int puntosTieBreakP2) {
            this.puntosP1 = puntosP1;
            this.puntosP2 = puntosP2;
            this.juegosP1 = juegosP1;
            this.juegosP2 = juegosP2;
            this.setsP1 = setsP1;
            this.setsP2 = setsP2;
            this.ventajaP1 = ventajaP1;
            this.ventajaP2 = ventajaP2;
            this.tieBreak = tieBreak;
            this.puntosTieBreakP1 = puntosTieBreakP1;
            this.puntosTieBreakP2 = puntosTieBreakP2;
        }
    }
    
    //Control del inicio de partido:
    private boolean partidoIniciado = false;
    private boolean cronometroActivo = false;

    //Historial Para deshacer:
    private Stack<EstadoPartido> historial;

    //Puntos actuales en el juego:
    private int puntosPareja1, puntosPareja2;

    //Juegos ganados en el set actual:
    private int juegosPareja1, juegosPareja2;
    
    //Sets ganados en el partido:
    private int setsPareja1, setsPareja2;
    
    //Estado especial (ventajas):
    private boolean ventajaPareja1, ventajaPareja2;
    
    //TieBreak:
    private boolean tieBreak;
    private int puntosTieBreakPareja1, puntosTieBreakPareja2;
    
    //Base de datos:
    private final DatabaseManager db;
    private final LocationManager locationManager;
    private int idPartidoActual = -1;
    private int tiempoInicioPartido;
    private int duracionSegundos = 0;
    
    //Constructor: Se inicia en 0:
    public PartidoPadel(Context context) {
        db = new DatabaseManager(context);
        locationManager = new LocationManager(context);
        reiniciar();
    }
    
    //Reiniciar el partido completo:
    public void reiniciar() {
        //Si hay un partido en curso, se borra de la BBDD:
        if (idPartidoActual != -1 && !ganador()) {
            db.borrarPartido(idPartidoActual);
        }

        partidoIniciado = false;
        cronometroActivo = false;
        idPartidoActual = -1;
        
        puntosPareja1 = 0;
        puntosPareja2 = 0;
        juegosPareja1 = 0;
        juegosPareja2 = 0;
        setsPareja1 = 0;
        setsPareja2 = 0;
        ventajaPareja1 = false;
        ventajaPareja2 = false;
        tieBreak = false;
        puntosTieBreakPareja1 = 0;
        puntosTieBreakPareja2 = 0; 
        historial = new Stack<>();
        duracionSegundos = 0;
    }
    
    //Iniciar el partido con el cronómetro:
    public void iniciarPartido() {
        if (!partidoIniciado) {
            //Obtenemos la ubicación antes de empezar el partido:
            locationManager.ultimaUbicacion(() -> {
                try {
                    idPartidoActual = db.iniciarPartido("Pareja 1", "Pareja 2");
                    tiempoInicioPartido = (int) (System.currentTimeMillis() / 1000);
                    partidoIniciado = true;
                    System.out.println("Partido iniciado - ID: " + idPartidoActual);
                } catch (Exception e) {
                    System.err.println("Error al iniciar el partido: " + e.getMessage());
                }
            });
        }
    }

    //Activar/desactivar cronómetro:
    public void setCronometroActivo(boolean activo) {
        cronometroActivo = activo;
    }
    //Consulta si el cronómetro está activo:
    public boolean cronometroActivo() {
        return cronometroActivo;
    }

    //Setter para la duración del cronómetro:
    public void setDuracionSegundos(int segundos) {
        duracionSegundos = segundos;
    }

    //Verificamos si el partido está empezado:
    public boolean partidoEmpezado() {
        return partidoIniciado;
    }
    
    //Puntuación pareja 1:
    public void addPuntoPareja1() {
        //Estado del partido:

        
        //Si estamos en Tie-Break:
        if (tieBreak) {
            puntosTieBreakPareja1++;
            //Comprobamos si el tie lo gana la pareja 1 (7 puntos y con 2 de diferencia):
            if (puntosTieBreakPareja1 >= 7 && puntosTieBreakPareja1 >= puntosTieBreakPareja2 + 2) {
                ganarSetPareja1TieBreak();
            }
            guardarEstado();
            guardarPuntosBD(1);
            return;
        }
        
        //Si estamos en iguales (40-40):
        if (puntosPareja1 >= 3 && puntosPareja2 >= 3) {
            if (ventajaPareja2){
                ventajaPareja2 = false;
            } else if (ventajaPareja1){
                ganarJuegoPareja1();
            } else {
                ventajaPareja1 = true;
            }
        } else {
            //Contamos la puntuación normal del juego:
            puntosPareja1++;
            if (puntosPareja1 >= 4 && puntosPareja1 >= puntosPareja2 + 2) {
                ganarJuegoPareja1();
            }
        }
        guardarEstado();
        guardarPuntosBD(1);
    }
    
    //Puntuación pareja 2:
    public void addPuntoPareja2() {
        //Estado del partido:
        guardarEstado();
        guardarPuntosBD(2);
        
        //Si estamos en Tie-Break:
        if (tieBreak) {
            puntosTieBreakPareja2++;
            //Comprobamos si el tie lo gana la pareja 2 (7 puntos y con 2 de diferencia):
            if (puntosTieBreakPareja2 >= 7 && puntosTieBreakPareja2 >= puntosTieBreakPareja1 + 2) {
                ganarSetPareja2TieBreak();
            } 
            return;
        }
        
        //Si estamos en iguales (40-40):
        if (puntosPareja2 >= 3 && puntosPareja1 >= 3) {
            if (ventajaPareja1){
                ventajaPareja1 = false;
            } else if (ventajaPareja2) {
                ganarJuegoPareja2();
            } else {
                ventajaPareja2 = true;
            }
        } else {
            //Contamos la puntuación normal del juego:
            puntosPareja2++;
            if (puntosPareja2 >= 4 && puntosPareja2 >= puntosPareja1 + 2) {
                ganarJuegoPareja2();
            }
        }
    }
    
    //Pareja 1 gana un juego:
    private void ganarJuegoPareja1() {
        juegosPareja1++;
        reiniciarPuntos();
        verificarGanadorSet();
    }
    
    //Pareja 2 gana un juego:
    private void ganarJuegoPareja2() {
        juegosPareja2++;
        reiniciarPuntos();
        verificarGanadorSet();
    }
    
    //Reiniciar los puntos del juego actual:
    private void reiniciarPuntos() {
        puntosPareja1 = 0;
        puntosPareja2 = 0;
        ventajaPareja1 = false;
        ventajaPareja2 = false;
    }
    
    //Verificamos quien ganó el set:
    private void verificarGanadorSet() {
        //Comprobamos que se ganó con 6 juegos y al menos 2 de diferencia:
        if (juegosPareja1 >= 6 && juegosPareja1 >= juegosPareja2 + 2){
            setsPareja1++;
            reiniciarJuegos();
        } else if (juegosPareja2 >= 6 && juegosPareja2 >= juegosPareja1 + 2){
            setsPareja2++;
            reiniciarJuegos();
        } 
        //Si se llega a 6-6, se juega tie-break:
        else if (juegosPareja1 == 6 && juegosPareja2 ==6) {
            tieBreak = true;
            reiniciarPuntos();
        }  
    }
    
    //Reiniciamos los juegos del set actual:
    private void reiniciarJuegos() {
        juegosPareja1 = 0;
        juegosPareja2 = 0;
    }
    
    //Puntuación en formato texto:
    public String getPuntuacion() {
        //Si estamos en tie-break:
        if (tieBreak){
            return puntosTieBreakPareja1 + " - " + puntosTieBreakPareja2;
        }
        
        //Si estamos en ventajas:
        if (puntosPareja1 >= 3 && puntosPareja2 >= 3){
            if (ventajaPareja1){
                return "V - 40";
            } else if (ventajaPareja2){
                return "40 - V";
            }
        }
        
        //Puntuación normal:
        return convertirPuntos(puntosPareja1) + " - " + convertirPuntos(puntosPareja2);
    }
 
    //Convertimos los puntos en formato padel (0, 15, 30, 40):
    public String convertirPuntos(int puntos) {
        return switch (puntos) {
            case 0 -> "0";
            case 1 -> "15";
            case 2 -> "30";
            case 3 -> "40";
            default -> "40";
        };
    }
    
    //Añadimos los getters para mostrar en la interfaz:
    public int getJuegosPareja1() {
        return juegosPareja1;
    }
    public int getJuegosPareja2() {
        return juegosPareja2;
    }
    public int getsetsPareja1() {
        return setsPareja1;
    }
    public int getsetsPareja2() {
        return setsPareja2;
    }

    //Verificamos si tenemos historial disponible:
    public boolean historial() {
        return !historial.isEmpty();
    }

    //Obtención de referencia a la BBDD:
    public DatabaseManager getDatabase() {
        return db;
    }

    //Verificamos el ganador del partido:
    public boolean ganador() {
        return setsPareja1 >= 2 || setsPareja2 >= 2;
    }

    public String getGanador() {
        if (setsPareja1 >= 2) {
            return "Pareja 1 gana!";
        } else if (setsPareja2 >= 2) {
            return "Pareja 2 gana!";
        }
        return "";
    }
    
    //Si la pareja1 gana el set en tie-break:
    private void ganarSetPareja1TieBreak() {
        setsPareja1++;
        tieBreak = false;
        puntosTieBreakPareja1 = 0;
        puntosTieBreakPareja2 = 0;
        reiniciarJuegos();
    }
    
    //Si la pareja2 gana el set en tie-break:
    private void ganarSetPareja2TieBreak() {
        setsPareja2++;
        tieBreak = false;
        puntosTieBreakPareja1 = 0;
        puntosTieBreakPareja2 = 0;
        reiniciarJuegos();
    }

    //Guardado del estado actual del partido en el historial:
    private void guardarEstado() {
        EstadoPartido estado = new EstadoPartido(puntosPareja1, puntosPareja2,
            juegosPareja1, juegosPareja2, setsPareja1, setsPareja2, ventajaPareja1,
            ventajaPareja2, tieBreak, puntosTieBreakPareja1, puntosTieBreakPareja2
        );
        historial.push(estado);
    }
    
    //Deshacer el último punto:
    public void deshacer() {
        if (historial.isEmpty()) {
            return; //En este caso, no deshará nada
        }
        
        EstadoPartido estado = historial.pop();
        //Restauramos el estado/punto:
        puntosPareja1 = estado.puntosP1;
        puntosPareja2 = estado.puntosP2;
        juegosPareja1 = estado.juegosP1;
        juegosPareja2 = estado.juegosP2;
        setsPareja1 = estado.setsP1;
        setsPareja2 = estado.setsP2;
        ventajaPareja1 = estado.ventajaP1;
        ventajaPareja2 = estado.ventajaP2;
        tieBreak = estado.tieBreak;
        puntosTieBreakPareja1 = estado.puntosTieBreakP1;
        puntosTieBreakPareja2 = estado.puntosTieBreakP2;
    }

    //Guardar puntos en la BBDD:
    private void guardarPuntosBD(int parejaGanadora) {
        //Guardamos solo en caso de que el partido esté empezado:
        if (!partidoIniciado || idPartidoActual == -1) {
            return;
        }
        try {
            int timestamp = (int) (System.currentTimeMillis() / 1000) - tiempoInicioPartido;
            db.guardarPuntos(idPartidoActual, 
                timestamp, 
                parejaGanadora, 
                puntosPareja1, 
                puntosPareja2, 
                juegosPareja1, 
                juegosPareja2, 
                setsPareja1, 
                setsPareja2, 
                tieBreak
            );
        } catch (Exception e) {
            System.err.println("Error al guardar puntos: " + e.getMessage());
        }
    }

    //Finalizar partido en la BBDD:
    public void finalizarPartidoBD() {
        //Solo acabamos el partido si el partido se ha empezado con el cronómetro:
        if (!partidoIniciado || idPartidoActual == -1) {
            return;
        }
        try {
            int ganador = setsPareja1 >= 2 ? 1 : 2;
            db.finalizarPartido(idPartidoActual, 
                duracionSegundos,
                setsPareja1, 
                setsPareja2, 
                ganador
            );

            String[] fechas = db.getFechasPartido(idPartidoActual);

            //Subimos a Firestore:
            FirebaseManager.subirPartido(db, idPartidoActual, duracionSegundos, ganador,
                    locationManager.getLatitud(), locationManager.getLongitud(), fechas[0], fechas[1]
            );

        } catch (Exception e) {
            System.err.println("Error al finalizar el partido: " + e.getMessage());
        }
    }
}