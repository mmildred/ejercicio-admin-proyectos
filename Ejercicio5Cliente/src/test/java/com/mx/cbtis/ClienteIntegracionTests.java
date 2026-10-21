package com.mx.cbtis;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ClienteIntegracionTests {

    @Autowired
    private MockMvc mockMvc;

    // ==========================================
    // 4.1 CASOS AUTOMÁTICOS DE ÉXITO
    // ==========================================

    @Test
    @DisplayName("AUT-E01: Carga y consulta correcta de la vista principal (Home)")
    void testCargaHomeExito() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("AUT-E02: Formulario de registro carga correctamente con modelo preparado")
    void testFormularioRegistroExito() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("usuario"));
    }

    @Test
    @DisplayName("AUT-E03: Componente e interfaz de Inicio carga con modelo de proyectos")
    void testComponenteInicioExito() throws Exception {
        mockMvc.perform(get("/inicio"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("proyectos"));
    }

    // ==========================================
    // 4.2 CASOS AUTOMÁTICOS DE FRACASO
    // ==========================================

    @Test
    @DisplayName("AUT-F01: Solicitud a recurso inexistente es capturada con error 404 controlado")
    void testRecursoInexistenteFallo() throws Exception {
        mockMvc.perform(get("/recurso-inexistente-error"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("AUT-F02: Envío de formulario sin cuerpo requerido genera error 400 Bad Request")
    void testFormularioIncompletoFallo() throws Exception {
        // /eliminarProyecto requiere @RequestBody Proyectos; si se envía vacío, el framework impide el procesamiento
        mockMvc.perform(post("/eliminarProyecto")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AUT-F03: Invocación con método HTTP no permitido genera 405 Method Not Allowed")
    void testMetodoNoPermitidoFallo() throws Exception {
        // /eliminarProyecto solo acepta POST, invocarlo por GET debe ser rechazado de forma controlada
        mockMvc.perform(get("/eliminarProyecto"))
                .andExpect(status().isMethodNotAllowed());
    }
}
