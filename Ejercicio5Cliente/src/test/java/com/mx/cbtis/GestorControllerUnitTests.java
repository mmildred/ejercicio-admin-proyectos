package com.mx.cbtis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.mx.cbtis.controller.GestorController;
import com.mx.cbtis.modelo.Comentarios;
import com.mx.cbtis.modelo.Proyectos;
import com.mx.cbtis.modelo.Tareas;
import com.mx.cbtis.modelo.Usuario;

@ExtendWith(MockitoExtension.class)
public class GestorControllerUnitTests {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GestorController controller;

    private Model model;

    @BeforeEach
    void setUp() {
        controller.setRestTemplate(restTemplate);
        controller.setBackendUrl("http://localhost:8081");
        model = new ConcurrentModel();
    }

    @Test
    @DisplayName("UT-C01: Vistas estáticas simples (Home, Tutorial, Manual, Login)")
    void testVistasEstaticas() {
        assertEquals("home", controller.getHome());
        assertEquals("tutorial", controller.getTutorial());
        assertEquals("manual", controller.getManual());
        assertEquals("login", controller.getLogin());
        assertNotNull(controller.getRestTemplate());
        assertEquals("http://localhost:8081", controller.getBackendUrl());
    }

    @Test
    @DisplayName("UT-C02: Vista de Registro prepara modelo con usuario vacío")
    void testGetRegister() {
        String view = controller.getRegister(model);
        assertEquals("register", view);
        assertTrue(model.containsAttribute("usuario"));
        assertNotNull(model.getAttribute("usuario"));
    }

    @Test
    @DisplayName("UT-C03: Guardar usuario redirige a /login")
    void testSetSaveUsuario() {
        Usuario usuario = new Usuario(1, "Mildred", "mildred@test.com", "pass123", "Admin", "#ffffff");
        ResponseEntity<Integer> mockResp = new ResponseEntity<>(1, HttpStatus.OK);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addUsuario"), eq(usuario), eq(Integer.class)))
                .thenReturn(mockResp);

        ModelAndView mav = controller.setSaveUsuario(usuario);
        assertNotNull(mav);
        assertEquals("redirect:/login", mav.getViewName());
        verify(restTemplate, times(1)).postForEntity(anyString(), eq(usuario), eq(Integer.class));
    }

    @Test
    @DisplayName("UT-C04: Vista de Inicio prepara modelo con proyecto vacío")
    void testGetInicio() {
        String view = controller.getInicio(model);
        assertEquals("inicio", view);
        assertTrue(model.containsAttribute("proyectos"));
    }

    @Test
    @DisplayName("UT-C05: Guardar proyecto redirige a /inicio")
    void testSetSaveProyecto() {
        Proyectos proyecto = new Proyectos(10, "Proyecto Test", "Descripcion", null, null, "Activo", "fondo.png");
        ResponseEntity<Integer> mockResp = new ResponseEntity<>(1, HttpStatus.OK);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addProyecto"), eq(proyecto), eq(Integer.class)))
                .thenReturn(mockResp);

        ModelAndView mav = controller.setSaveProyecto(proyecto);
        assertNotNull(mav);
        assertEquals("redirect:/inicio", mav.getViewName());
    }

    @Test
    @DisplayName("UT-C06: Consultar proyectos exitoso y caso nulo")
    void testConsultarProyectos() {
        Proyectos p1 = new Proyectos(1, "P1", "Desc", null, null, "Activo", "fondo.png");
        Proyectos[] mockArray = new Proyectos[]{p1};
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarProyectos"), any(), eq(Proyectos[].class)))
                .thenReturn(mockArray);

        ResponseEntity<Object> resp = controller.consultarProyectos(model);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(model.containsAttribute("listaProyectos"));

        // Caso donde la respuesta del API es nula
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarProyectos"), any(), eq(Proyectos[].class)))
                .thenReturn(null);
        ResponseEntity<Object> respNull = controller.consultarProyectos(model);
        assertEquals(HttpStatus.OK, respNull.getStatusCode());
    }

    @Test
    @DisplayName("UT-C07: Eliminar proyecto invoca delete y redirige a /inicio")
    void testEliminarProyecto() {
        Proyectos proyecto = new Proyectos(1, "P1", "Desc", null, null, "Activo", "fondo.png");
        ResponseEntity<Integer> mockResp = new ResponseEntity<>(1, HttpStatus.OK);
        when(restTemplate.exchange(eq("http://localhost:8081/deleteProyecto"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Integer.class)))
                .thenReturn(mockResp);

        ModelAndView mav = controller.eliminarProyecto(proyecto);
        assertEquals("redirect:/inicio", mav.getViewName());
    }

    @Test
    @DisplayName("UT-C08: Actualizar proyecto busca datos por ID")
    void testUpdateProyecto() {
        Proyectos proyecto = new Proyectos(5, "P5", "Desc", null, null, "Activo", "fondo.png");
        when(restTemplate.getForObject(eq("http://localhost:8081/buscarProyecto?id_proyecto={id_proyecto}"), eq(Proyectos.class), any(Map.class)))
                .thenReturn(proyecto);

        ResponseEntity<Object> resp = controller.updateProyecto(proyecto);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    @DisplayName("UT-C09: Verificar login de usuario")
    void testVerificarUsuario() {
        Usuario usuario = new Usuario(1, "Mildred", "mildred@test.com", "pass", "Admin", "#fff");
        ResponseEntity<Usuario> mockResp = new ResponseEntity<>(usuario, HttpStatus.OK);
        when(restTemplate.postForEntity(eq("http://localhost:8081/getUsuarioLogin"), eq(usuario), eq(Usuario.class)))
                .thenReturn(mockResp);

        ResponseEntity<Usuario> resp = controller.verificarUsuario(usuario);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Mildred", resp.getBody().getNombre());
    }

    @Test
    @DisplayName("UT-C10: Consultar usuarios con respuesta válida y nula")
    void testConsultarUsuarios() {
        Usuario u1 = new Usuario(1, "User1", "u1@test.com", "pass", "Dev", "#000");
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(), eq(Usuario[].class)))
                .thenReturn(new Usuario[]{u1});

        ResponseEntity<Object> resp = controller.consultarUsuarios(model);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(model.containsAttribute("listaUsuarios"));

        // Caso nulo
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(), eq(Usuario[].class)))
                .thenReturn(null);
        ResponseEntity<Object> respNull = controller.consultarUsuarios(model);
        assertEquals(HttpStatus.OK, respNull.getStatusCode());
    }

    @Test
    @DisplayName("UT-C11: Obtener tareas por proyecto")
    void testObtenerTareas() {
        Tareas t1 = new Tareas();
        t1.setId_tarea(100);
        t1.setNombre("Test Task");
        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(Tareas.class), eq(Tareas[].class)))
                .thenReturn(new Tareas[]{t1});

        ResponseEntity<List<Tareas>> resp = controller.obtenerTareas(t1, model);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());

        // Caso nulo
        when(restTemplate.postForObject(eq("http://localhost:8081/getTarea"), any(Tareas.class), eq(Tareas[].class)))
                .thenReturn(null);
        ResponseEntity<List<Tareas>> respNull = controller.obtenerTareas(t1, model);
        assertTrue(respNull.getBody().isEmpty());
    }

    @Test
    @DisplayName("UT-C12: Obtener comentarios")
    void testObtenerComentarios() {
        Comentarios c1 = new Comentarios();
        c1.setId_comentario(10);
        c1.setTexto_comentario("Comentario Test");
        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(Comentarios.class), eq(Comentarios[].class)))
                .thenReturn(new Comentarios[]{c1});

        ResponseEntity<List<Comentarios>> resp = controller.obtenerComentarios(c1, model);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().size());

        // Caso nulo
        when(restTemplate.postForObject(eq("http://localhost:8081/getComentario"), any(Comentarios.class), eq(Comentarios[].class)))
                .thenReturn(null);
        ResponseEntity<List<Comentarios>> respNull = controller.obtenerComentarios(c1, model);
        assertTrue(respNull.getBody().isEmpty());
    }

    @Test
    @DisplayName("UT-C13: Vistas de Proyecto y Tarea")
    void testVistasProyectoYTarea() {
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarUsuarios"), any(), eq(Usuario[].class)))
                .thenReturn(new Usuario[]{});

        String viewProyecto = controller.getProyecto(model);
        assertEquals("proyecto", viewProyecto);
        assertTrue(model.containsAttribute("tarea"));
        assertTrue(model.containsAttribute("comentarios"));
        assertTrue(model.containsAttribute("listaUsuarios"));

        String viewTarea = controller.getTarea(model);
        assertEquals("proyecto", viewTarea);
        assertTrue(model.containsAttribute("tareas"));
    }

    @Test
    @DisplayName("UT-C14: Guardar tarea y comentario")
    void testSaveTareaYComentario() {
        Tareas tarea = new Tareas();
        tarea.setId_proyecto(99);
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), eq(tarea), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mav = controller.setSaveTareas(tarea);
        assertEquals("redirect:/proyecto", mav.getViewName());
        assertEquals(99, mav.getModel().get("id_proyecto"));

        // Caso tareas null
        ModelAndView mavNull = controller.setSaveTareas(null);
        assertEquals("redirect:/proyecto", mavNull.getViewName());

        Comentarios comentario = new Comentarios();
        when(restTemplate.postForEntity(eq("http://localhost:8081/addComentario"), eq(comentario), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));
        String viewComentario = controller.setSaveComentario(comentario);
        assertEquals("proyecto", viewComentario);
    }

    @Test
    @DisplayName("UT-C15: Actualizar y eliminar tarea")
    void testUpdateYEliminarTarea() {
        Tareas tarea = new Tareas();
        tarea.setId_tarea(50);
        when(restTemplate.getForObject(eq("http://localhost:8081/buscarTarea?id_tarea={id_tarea}"), eq(Tareas.class), any(Map.class)))
                .thenReturn(tarea);

        ResponseEntity<Object> respUpdate = controller.updateTarea(tarea);
        assertEquals(HttpStatus.OK, respUpdate.getStatusCode());

        when(restTemplate.exchange(eq("http://localhost:8081/deleteTareas"), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ModelAndView mavDelete = controller.eliminarTarea(tarea);
        assertEquals("redirect:/proyecto", mavDelete.getViewName());
    }

    @Test
    @DisplayName("UT-C16: Verificar y cambios en tareas")
    void testVerificarYCambiosTareas() {
        Tareas t = new Tareas();
        when(restTemplate.postForEntity(eq("http://localhost:8081/addTareas"), eq(t), eq(Integer.class)))
                .thenReturn(new ResponseEntity<>(1, HttpStatus.OK));

        ResponseEntity<Integer> r1 = controller.verificarTarea(t);
        assertEquals(1, r1.getBody());

        ResponseEntity<Integer> r2 = controller.cambiosTareas(t);
        assertEquals(1, r2.getBody());
    }

    @Test
    @DisplayName("UT-C17: Consultar todas las tareas y comentarios con datos y nulos")
    void testConsultarTareasYComentarios() {
        when(restTemplate.postForObject(eq("http://localhost:8081/buscarTareas"), any(), eq(Tareas[].class)))
                .thenReturn(new Tareas[]{new Tareas()});
        ResponseEntity<Object> respTareas = controller.consultarTareas();
        assertEquals(HttpStatus.OK, respTareas.getStatusCode());

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarTareas"), any(), eq(Tareas[].class)))
                .thenReturn(null);
        ResponseEntity<Object> respTareasNull = controller.consultarTareas();
        assertEquals(HttpStatus.OK, respTareasNull.getStatusCode());

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarComentarios"), any(), eq(Comentarios[].class)))
                .thenReturn(new Comentarios[]{new Comentarios()});
        ResponseEntity<Object> respCom = controller.consultarComentarios();
        assertEquals(HttpStatus.OK, respCom.getStatusCode());

        when(restTemplate.postForObject(eq("http://localhost:8081/buscarComentarios"), any(), eq(Comentarios[].class)))
                .thenReturn(null);
        ResponseEntity<Object> respComNull = controller.consultarComentarios();
        assertEquals(HttpStatus.OK, respComNull.getStatusCode());
    }
}
