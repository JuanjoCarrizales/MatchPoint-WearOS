package com.mypadelapp.matchpoint.logic;

import static org.junit.Assert.*;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

//Unit tests de PartidoPadel:
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class PartidoPadelTest {

    private PartidoPadel partido;

    // Setup
    @Before
    public void crearPartido() {
        Context context = RuntimeEnvironment.getApplication();
        partido = new PartidoPadel(context);
    }

    // Helpers
    /** Llamar función addPuntoPareja1() n veces. */
    private void puntoP1(int n) {
        for (int i = 0; i < n; i++) {
            partido.addPuntoPareja1();
        }
    }

    /** Llamar función addPuntoPareja2() n veces. */
    private void puntoP2(int n) {
        for (int i = 0; i < n; i++) {
            partido.addPuntoPareja2();
        }
    }

    /** Pareja 1 gana un juego (4 puntos) */
    private void juegoP1() {
        puntoP1(4);
    }

    /** Pareja 2 gana un juego (4 puntos) */
    private void juegoP2() {
        puntoP2(4);
    }

    /** Pareja 1 gana un set entero 6-0 */
    private void setP1_60() {
        for (int i = 0; i < 6; i++) {
            juegoP1();
        }
    }

    /** Pareja 2 gana un set entero 0-6 */
    private void setP2_06() {
        for (int i = 0; i < 6; i++) {
            juegoP2();
        }
    }

    /** Simular tiebreak 6-6 */
    private void llegarTiebreak() {
        for (int i = 0; i < 6; i++) {
            juegoP1();
            juegoP2();
        }
    }

    // Tests 1 - función convertirPuntos
    @Test
    public void TU_1a_convertirPuntos_0_devuelve_0() {
        assertEquals("0", partido.convertirPuntos(0));
    }

    @Test
    public void TU_1b_convertirPuntos_1_devuelve_15() {
        assertEquals("15", partido.convertirPuntos(1));
    }

    @Test
    public void TU_1c_convertirPuntos_2_devuelve_30() {
        assertEquals("30", partido.convertirPuntos(2));
    }

    @Test
    public void TU_1d_convertirPuntos_3_devuelve_40() {
        assertEquals("40", partido.convertirPuntos(3));
    }

    // Tests 2 - función getPuntuacion

    @Test
    public void TU_2a_puntuacion_inicial() {
        assertEquals("0 - 0", partido.getPuntuacion());
    }

    @Test
    public void TU_2b_puntuacion_1_0() {
        puntoP1(1);
        assertEquals("15 - 0", partido.getPuntuacion());
    }

    @Test
    public void TU_2c_puntuacion_2_0() {
        puntoP1(2);
        assertEquals("30 - 0", partido.getPuntuacion());
    }

    @Test
    public void TU_2d_puntuacion_2_1() {
        puntoP1(2);
        puntoP2(1);
        assertEquals("30 - 15", partido.getPuntuacion());
    }

    @Test
    public void TU_2e_puntuacion_3_3() {
        puntoP1(3);
        puntoP2(3);
        assertEquals("40 - 40", partido.getPuntuacion());
    }

    // Tests 3 - Iguales y ventajas

    @Test
    public void TU_3a_ventaja_P1() {
        puntoP1(3); puntoP2(3); // 40-40
        puntoP1(1); // ventaja P1
        assertEquals("V - 40", partido.getPuntuacion());
    }

    @Test
    public void TU_3b_ventaja_P2() {
        puntoP1(3); puntoP2(3); // 40-40
        puntoP2(1); // ventaja P2
        assertEquals("40 - V", partido.getPuntuacion());
    }

    @Test
    public void TU_3c_P2_iguala() {
        puntoP1(3); puntoP2(3);
        puntoP1(1); // V-40
        puntoP2(1); // 40-40
        assertEquals("40 - 40", partido.getPuntuacion());
    }

    @Test
    public void TU_3d_P1_iguala() {
        puntoP1(3); puntoP2(3);
        puntoP2(1); // 40-V
        puntoP1(1); // 40-40
        assertEquals("40 - 40", partido.getPuntuacion());
    }

    @Test
    public void TU_3e_P1_gana_ventaja() {
        puntoP1(3); puntoP2(3);
        puntoP1(1); // V-40
        puntoP1(1); // P1 gana
        assertEquals(1, partido.getJuegosPareja1());
        assertEquals(0, partido.getJuegosPareja2());
    }

    @Test
    public void TU_3f_P2_gana_ventaja() {
        puntoP1(3); puntoP2(3);
        puntoP2(1); // 40-V
        puntoP2(1); // P2 gana
        assertEquals(0, partido.getJuegosPareja1());
        assertEquals(1, partido.getJuegosPareja2());
    }

    // Tests 4 - Final de juego y resets

    @Test
    public void TU_4a_P1_gana_juego_suma_1() {
        juegoP1();
        assertEquals(1, partido.getJuegosPareja1());
        assertEquals(0, partido.getJuegosPareja2());
    }

    @Test
    public void TU_4b_P1_gana_juego_reset_puntos() {
        juegoP1();
        assertEquals("0 - 0", partido.getPuntuacion());
    }

    @Test
    public void TU_4c_P2_gana_juego_suma_1() {
        juegoP2();
        assertEquals(0, partido.getJuegosPareja1());
        assertEquals(1, partido.getJuegosPareja2());
    }

    // Tests 5 - Final de set

    @Test
    public void TU_5a_P1_gana_set() {
        setP1_60();
        assertEquals(1, partido.getSetsPareja1());
        assertEquals(0, partido.getSetsPareja2());
        assertEquals(0, partido.getJuegosPareja1());
    }

    @Test
    public void TU_5b_set_no_termina_6_5() {
        for (int i = 0; i < 5; i++) {
            juegoP1();
            juegoP2();
        }
        juegoP1(); // 6-5
        assertEquals(0, partido.getSetsPareja1()); // No ha terminado el set
    }

    @Test
    public void TU_5c_set_termina_7_5() {
        for (int i = 0; i < 5; i++) {
            juegoP1();
            juegoP2();
        }
        juegoP1(); // 6-5
        juegoP1(); // 7-5
        assertEquals(1, partido.getSetsPareja1());
    }

    @Test
    public void TU_5d_set_P2_gana_set() {
        setP2_06();
        assertEquals(0, partido.getSetsPareja1());
        assertEquals(1, partido.getSetsPareja2());
    }

    // Tests 6 - Tie-break

    @Test
    public void TU_6a_tiebreak_se_activa_en_6_6_juegos() {
        llegarTiebreak();
        assertEquals("0 - 0", partido.getPuntuacion());
        assertEquals(6, partido.getJuegosPareja1());
        assertEquals(6, partido.getJuegosPareja2());
    }

    @Test
    public void TU_6b_tiebreak_P1_gana_7_0() {
        llegarTiebreak();
        puntoP1(7);
        assertEquals(1, partido.getSetsPareja1());
        assertEquals(0, partido.getSetsPareja2());
    }

    @Test
    public void TU_6c_tiebreak_no_termina_en_7_6() {
        llegarTiebreak();
        puntoP1(6); puntoP2(6); // 7-6
        puntoP1(1);
        assertEquals(0, partido.getSetsPareja1()); // no termina el set
    }

    @Test
    public void TU_6d_tiebreak_termina_en_8_6() {
        llegarTiebreak();
        puntoP1(6); puntoP2(6); // 7-6
        puntoP1(2); // 8-6
        assertEquals(1, partido.getSetsPareja1());
    }

    @Test
    public void TU_6e_tiebreak_P2_gana_7_0() {
        llegarTiebreak();
        puntoP2(7);
        assertEquals(0, partido.getSetsPareja1());
        assertEquals(1, partido.getSetsPareja2());
    }

    // Tests 7 - Final de partido

    @Test
    public void TU_7a_partido_no_terminado_inicio() {
        assertFalse(partido.ganador());
        assertEquals("", partido.getGanador());
    }

    @Test
    public void TU_7b_partido_no_terminado_mitad() {
        setP1_60();
        assertFalse(partido.ganador());
    }

    @Test
    public void TU_7c_P1_gana_partido_2_0() {
        setP1_60(); // 1-0
        setP1_60(); // 1-1
        assertTrue(partido.ganador());
        assertEquals("Pareja 1 gana!", partido.getGanador());
    }

    @Test
    public void TU_7d_P2_gana_partido_2_0() {
        setP2_06(); // 1-0
        setP2_06(); // 1-1
        assertTrue(partido.ganador());
        assertEquals("Pareja 2 gana!", partido.getGanador());
    }

    @Test
    public void TU_7e_P1_gana_partido_2_1() {
        setP1_60(); // 1-0
        setP2_06(); // 1-1
        setP1_60(); // 2-1
        assertTrue(partido.ganador());
        assertEquals("Pareja 1 gana!", partido.getGanador());
    }

    // Tests 8 - Deshacer

    @Test
    public void TU_8a_historial_vacio_inicio() {
        assertFalse(partido.historial());
    }

    @Test
    public void TU_8b_historial_disponible_tras_un_punto() {
        puntoP1(1);
        assertTrue(partido.historial());
    }

    @Test
    public void TU_8c_deshacer_un_punto_P1_restaura_0_0() {
        puntoP1(1); // 15-0
        partido.deshacer();
        assertEquals("0 - 0", partido.getPuntuacion());
        assertFalse(partido.historial());
    }

    @Test
    public void TU_8d_deshacer_desde_ventaja_vuelve_a_40_40() {
        puntoP1(3); puntoP2(3); puntoP1(1); // V-40
        partido.deshacer();
        assertEquals("40 - 40", partido.getPuntuacion());
    }

    @Test
    public void TU_8e_deshacer_sin_historial() {
        try {
            partido.deshacer();
        } catch (Exception e) {
            fail("deshacer() on empty history threw: " + e.getMessage());
        }
    }

    @Test
    public void TU_8f_deshacer_tras_ganar_juego() {
        puntoP1(3); // 40-0
        puntoP1(1); // juegos P1 = 1
        partido.deshacer();
        assertEquals(0, partido.getJuegosPareja1());
    }

    // Tests 9  reiniciar

    @Test
    public void TU_9a_reiniciar_resetea_todos_los_contadores() {
        // simular mitad de partido
        setP1_60();
        puntoP1(2); puntoP2(1);
        partido.reiniciar();
        assertEquals("0 - 0", partido.getPuntuacion());
        assertEquals(0, partido.getJuegosPareja1());
        assertEquals(0, partido.getJuegosPareja2());
        assertEquals(0, partido.getSetsPareja1());
        assertEquals(0, partido.getSetsPareja2());
        assertFalse(partido.ganador());
    }

    @Test
    public void TU_9b_reiniciar_limpia_historial() {
        puntoP1(3);
        partido.reiniciar();
        assertFalse(partido.historial());
    }

    @Test
    public void TU_9c_partido_no_empezado_tras_reiniciar() {
        partido.reiniciar();
        assertFalse(partido.partidoEmpezado());
    }
}