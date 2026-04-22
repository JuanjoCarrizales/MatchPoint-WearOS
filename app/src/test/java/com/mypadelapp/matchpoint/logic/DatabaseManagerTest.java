package com.mypadelapp.matchpoint.logic;

import static org.junit.Assert.*;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

// Integration tests de DatabaseManager

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class DatabaseManagerTest {

    private DatabaseManager db;

    // Setup
    @Before
    public void crearDb() {
        Context context = RuntimeEnvironment.getApplication();
        db = new DatabaseManager(context);
    }

    @After
    public void cerrarDb() {
        db.close();
    }

    // Helpers
    /** Crear partido finalizado, con n puntos por pareja, y devolver ID */
    private int crearPartidoFinalizado(
            int nPuntosP1, int nPuntosP2,int setsP1, int setsP2, int duracion, int ganador
    ) {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        for (int i = 0; i < nPuntosP1; i++) {
            db.guardarPuntos(id, i, 1, 1, 0, 0, 0, 0, 0, false);
        }
        for (int i = 0; i < nPuntosP2; i++) {
            db.guardarPuntos(id, nPuntosP1 + i, 2, 0, 1, 0, 0, 0, 0, false);
        }
        db.finalizarPartido(id, duracion, setsP1, setsP2, ganador);
        return id;
    }

    // Tests 1 - iniciarPartido

    @Test
    public void TI_1a_iniciarPartido_devuelve_id_positivo() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        assertTrue("El id debe ser > 0", id > 0);
    }

    @Test
    public void TI_1b_iniciarPartido_crea_registro_en_tabla() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT id, nombre_pareja1, nombre_pareja2, partido_finalizado " +
                "FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});

        assertTrue("Debe existir la fila", c.moveToFirst());
        assertEquals("Pareja 1", c.getString(1));
        assertEquals("Pareja 2", c.getString(2));
        assertEquals(0, c.getInt(3));
        c.close();
    }

    @Test
    public void TI_1c_iniciarPartido_guarda_fecha_inicio_no_nula() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT fecha_inicio FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertNotNull("fecha_inicio no debe ser null", c.getString(0));
        assertFalse("fecha_inicio no debe estar vacía", c.getString(0).isEmpty());
        c.close();
    }

    @Test
    public void TI_1d_dos_partidos_tienen_ids_distintos() {
        int id1 = db.iniciarPartido("Pareja 1", "Pareja 2");
        int id2 = db.iniciarPartido("Pareja 1", "Pareja 2");
        assertNotEquals("Los ids deben ser distintos", id1, id2);
    }

    // Tests 2 - guardarPuntos

    @Test
    public void TI_2a_guardarPuntos_crea_fila_en_tabla_puntos() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 10, 1, 1, 0, 0, 0, 0, 0, false);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(1, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_2b_guardarPuntos_guarda_todos_los_campos() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 42, 1, 2, 1, 3, 2, 1, 0, true);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT timestamp, pareja_ganadora, puntos_pareja1, puntos_pareja2, " +
                "juegos_pareja1, juegos_pareja2, sets_pareja1, sets_pareja2, tiebreak " +
                "FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(42, c.getInt(0));
        assertEquals(1, c.getInt(1));
        assertEquals(2, c.getInt(2));
        assertEquals(1, c.getInt(3));
        assertEquals(3, c.getInt(4));
        assertEquals(2, c.getInt(5));
        assertEquals(1, c.getInt(6));
        assertEquals(0, c.getInt(7));
        assertEquals(1, c.getInt(8));
        c.close();
    }

    @Test
    public void TI_2c_guardarPuntos_multiples_filas_para_mismo_partido() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 1, 0, 0, 0, 0, 0, false);
        db.guardarPuntos(id, 2, 2, 1, 1, 0, 0, 0, 0, false);
        db.guardarPuntos(id, 3, 1, 2, 1, 0, 0, 0, 0, false);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(3, c.getInt(0));
        c.close();
    }

    // Tests 3 - deshacerPunto

    @Test
    public void TI_3a_deshacerPunto_elimina_el_ultimo_punto() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 1, 0, 0, 0, 0, 0, false);
        db.guardarPuntos(id, 2, 2, 1, 1, 0, 0, 0, 0, false);
        db.deshacerPunto(id);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals("Solo debe quedar 1 punto", 1, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_3b_deshacerPunto_no_afecta_a_otro_partido() {
        int id1 = db.iniciarPartido("Pareja 1", "Pareja 2");
        int id2 = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id1, 1, 1, 1, 0, 0, 0, 0, 0, false);
        db.guardarPuntos(id2, 1, 1, 1, 0, 0, 0, 0, 0, false);
        db.deshacerPunto(id1);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id2)});
        c.moveToFirst();
        assertEquals("El otro partido no debe verse afectado", 1, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_3c_deshacerPunto_en_tabla_vacia_no_lanza_excepcion() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        try {
            db.deshacerPunto(id);
        } catch (Exception e) {
            fail("deshacerPunto en tabla vacía lanzó: " + e.getMessage());
        }
    }

    // Tests 4 - finalizarPartido

    @Test
    public void TI_4a_finalizarPartido_marca_partido_como_finalizado() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.finalizarPartido(id, 3600, 2, 1, 1);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT partido_finalizado FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(1, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_4b_finalizarPartido_guarda_duracion_y_ganador() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.finalizarPartido(id, 5400, 2, 0, 1);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT duracion_total, ganador, sets_pareja1, sets_pareja2 " +
                "FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(5400, c.getInt(0));
        assertEquals(1, c.getInt(1));
        assertEquals(2, c.getInt(2));
        assertEquals(0, c.getInt(3));
        c.close();
    }

    @Test
    public void TI_4c_finalizarPartido_guarda_fecha_fin_no_nula() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.finalizarPartido(id, 1000, 2, 1, 1);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT fecha_fin FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertNotNull("fecha_fin no debe ser null tras finalizar", c.getString(0));
        c.close();
    }

    // Tests 5 - borrarPartido

    @Test
    public void TI_5a_borrarPartido_elimina_la_fila_del_partido() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.borrarPartido(id);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals(0, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_5b_borrarPartido_elimina_puntos_del_partido() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 1, 0, 0, 0, 0, 0, false);
        db.guardarPuntos(id, 2, 2, 1, 1, 0, 0, 0, 0, false);
        db.borrarPartido(id);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM puntos WHERE id_partido = ?",
                new String[]{String.valueOf(id)});
        c.moveToFirst();
        assertEquals("Los puntos deben borrarse con el partido", 0, c.getInt(0));
        c.close();
    }

    @Test
    public void TI_5c_borrarPartido_no_borra_otros_partidos() {
        int id1 = db.iniciarPartido("Pareja 1", "Pareja 2");
        int id2 = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.borrarPartido(id1);

        SQLiteDatabase raw = db.getReadableDatabase();
        Cursor c = raw.rawQuery(
                "SELECT COUNT(*) FROM partidos WHERE id = ?",
                new String[]{String.valueOf(id2)});
        c.moveToFirst();
        assertEquals("El segundo partido no debe borrarse", 1, c.getInt(0));
        c.close();
    }

    // Tests 6 - getTotalPartidos

    @Test
    public void TI_6a_getTotalPartidos_sin_partidos_acabados() {
        assertEquals(0, db.getTotalPartidos());
    }

    @Test
    public void TI_6b_getTotalPartidos_partidos_sin_finalizar() {
        db.iniciarPartido("Pareja 1", "Pareja 2");
        assertEquals(0, db.getTotalPartidos());
    }

    @Test
    public void TI_6c_getTotalPartidos_varios_partidos() {
        crearPartidoFinalizado(10, 5, 2, 1, 3600, 1);
        crearPartidoFinalizado(8,  9, 1, 2, 4200, 2);
        crearPartidoFinalizado(12, 6, 2, 0, 2800, 1);
        assertEquals(3, db.getTotalPartidos());
    }

    // Tests 7 - getPuntosGanados / getPuntosPerdidos

    @Test
    public void TI_7a_getPuntosGanados_sin_partidos_acabados() {
        assertEquals(0, db.getPuntosGanados());
    }

    @Test
    public void TI_7b_getPuntosGanados_cuenta_puntos_P1_en_ultimo_partido() {
        crearPartidoFinalizado(7, 3, 2, 1, 3600, 1);
        assertEquals(7, db.getPuntosGanados());
    }

    @Test
    public void TI_7c_getPuntosGanados_usa_solo_el_ultimo_partido_finalizado() {
        crearPartidoFinalizado(5, 5, 2, 1, 3600, 1); // older
        crearPartidoFinalizado(9, 2, 2, 0, 2700, 1); // latest
        assertEquals("Debe devolver los puntos del último partido", 9, db.getPuntosGanados());
    }

    @Test
    public void TI_7d_getPuntosPerdidos_devuelve_0_sin_partidos_acabados() {
        assertEquals(0, db.getPuntosPerdidos());
    }

    @Test
    public void TI_7e_getPuntosPerdidos_cuenta_puntos_P2_en_ultimo_partido() {
        crearPartidoFinalizado(7, 4, 2, 1, 3600, 1);
        assertEquals(4, db.getPuntosPerdidos());
    }

    // Tests 8 - getVentajasPareja1 / getVentajasPareja2

    @Test
    public void TI_8a_getVentajasPareja1_devuelve_0_sin_ventajas() {
        // Points are all 1-0 or 0-1, never 3-3 → no advantages possible
        crearPartidoFinalizado(5, 3, 2, 1, 3600, 1);
        assertEquals(0, db.getVentajasPareja1());
    }

    @Test
    public void TI_8b_getVentajas_cuenta_solo_ventajas_reales() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 3, 3, 0, 0, 0, 0, false); // 30-40 a 40-40 - no ventaja
        db.guardarPuntos(id, 2, 1, 3, 3, 0, 0, 0, 0, false); // P1 hace punto (V-40) - ventaja P1
        db.guardarPuntos(id, 3, 2, 3, 3, 0, 0, 0, 0, false); // P2 hace punto (40-40) - no ventaja
        db.finalizarPartido(id, 3600, 2, 1, 1);

        assertEquals("P1 debe tener 1 ventaja real", 1, db.getVentajasPareja1()); // P1 hace 1 ventaja
        assertEquals("P2 debe tener 1 ventaja real", 0, db.getVentajasPareja2()); // P3 no hace ninguna ventaja
    }

    @Test
    public void TI_8c_getVentajas_no_acumula_sobre_todos_los_partidos() {
        // Primer partido (1 ventaja de P1)
        int id1 = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id1, 1, 1, 3, 3, 0, 0, 0, 0, false);
        db.guardarPuntos(id1, 2, 1, 3, 3, 0, 0, 0, 0, false);
        db.finalizarPartido(id1, 3600, 2, 1, 1);
        // Segundo partido (1 ventaja de P1)
        int id2 = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id2, 1, 1, 3, 3, 0, 0, 0, 0, false);
        db.guardarPuntos(id2, 2, 1, 3, 3, 0, 0, 0, 0, false);
        db.finalizarPartido(id2, 2700, 2, 1, 1);

        // Contar solo ventajas del último partido (1)
        assertEquals("Solo debe contar el último partido", 1, db.getVentajasPareja1());
    }

    @Test
    public void TI_8d_llegar_a_iguales_no_es_ventaja() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 3, 3, 0, 0, 0, 0, false); // 30-40 -> 40-40
        db.finalizarPartido(id, 3600, 2, 1, 1);

        assertEquals("Llegar a iguales no es una ventaja", 0, db.getVentajasPareja1());
        assertEquals(0, db.getVentajasPareja2());
    }

    @Test
    public void TI_8e_multiples_iguales_en_un_juego_se_cuentan_correctamente() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.guardarPuntos(id, 1, 1, 3, 3, 0, 0, 0, 0, false); // iguales
        db.guardarPuntos(id, 2, 1, 3, 3, 0, 0, 0, 0, false); // ventaja p1
        db.guardarPuntos(id, 3, 2, 3, 3, 0, 0, 0, 0, false); // iguales
        db.guardarPuntos(id, 4, 1, 3, 3, 0, 0, 0, 0, false); // ventaja p1
        db.guardarPuntos(id, 5, 2, 3, 3, 0, 0, 0, 0, false); // iguales
        db.guardarPuntos(id, 6, 1, 3, 3, 0, 0, 0, 0, false); // ventaja p1

        db.finalizarPartido(id, 3600, 2, 1, 1);

        assertEquals("P1 debe tener 3 ventajas", 3, db.getVentajasPareja1());
        assertEquals("P2 debe tener 0 ventajas", 0, db.getVentajasPareja2());
    }

    // Tests 9 - getDuracionMedia

    @Test
    public void TI_9a_getDuracionMedia_sin_partidos_acabados() {
        assertEquals(0, db.getDuracionMedia());
    }

    @Test
    public void TI_9b_getDuracionMedia_con_un_partido() {
        crearPartidoFinalizado(5, 3, 2, 1, 3600, 1);
        assertEquals(3600, db.getDuracionMedia());
    }

    @Test
    public void TI_9c_getDuracionMedia_con_dos_partidos() {
        crearPartidoFinalizado(5, 3, 2, 1, 3000, 1);
        crearPartidoFinalizado(4, 6, 1, 2, 5000, 2);
        // AVG(3000, 5000) = 4000
        assertEquals(4000, db.getDuracionMedia());
    }

    @Test
    public void TI_9d_getDuracionMedia_ignora_partidos_no_finalizados() {
        crearPartidoFinalizado(5, 3, 2, 1, 3600, 1);
        db.iniciarPartido("Pareja 1", "Pareja 2");
        assertEquals(3600, db.getDuracionMedia());
    }

    // Tests 10 - getFechasPartido

    @Test
    public void TI_10a_getFechasPartido_fecha_inicio_no_es_nula() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        String[] fechas = db.getFechasPartido(id);
        assertNotNull("fecha_inicio no debe ser null", fechas[0]);
        assertFalse(fechas[0].isEmpty());
    }

    @Test
    public void TI_10b_getFechasPartido_fecha_fin_es_nula_antes_de_finalizar() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        String[] fechas = db.getFechasPartido(id);
        assertNull("fecha_fin debe ser null antes de finalizar", fechas[1]);
    }

    @Test
    public void TI_10c_getFechasPartido_fecha_fin_no_es_nula_tras_finalizar() {
        int id = db.iniciarPartido("Pareja 1", "Pareja 2");
        db.finalizarPartido(id, 3600, 2, 1, 1);
        String[] fechas = db.getFechasPartido(id);
        assertNotNull("fecha_fin debe existir tras finalizar", fechas[1]);
    }
}