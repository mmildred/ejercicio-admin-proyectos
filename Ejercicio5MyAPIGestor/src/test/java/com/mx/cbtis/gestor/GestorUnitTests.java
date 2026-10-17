package com.mx.cbtis.gestor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mx.cbtis.gestor.controller.AsignacionesController;
import com.mx.cbtis.gestor.controller.ComentariosController;
import com.mx.cbtis.gestor.controller.ProyectosController;
import com.mx.cbtis.gestor.controller.TareasController;
import com.mx.cbtis.gestor.controller.UsuarioController;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;
import com.mx.cbtis.gestor.repository.ComentariosRepository;
import com.mx.cbtis.gestor.repository.ProtectosRepository;
import com.mx.cbtis.gestor.repository.TareasRepository;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class GestorUnitTests {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioController usuarioController;

    @Mock
    private ProtectosRepository proyectosRepository;

    @InjectMocks
    private ProyectosController proyectosController;

    @Mock
    private TareasRepository tareasRepository;

    @InjectMocks
    private TareasController tareasController;

    @Mock
    private ComentariosRepository comentariosRepository;

    @InjectMocks
    private ComentariosController comentariosController;

    @Mock
    private AsignacionesRepository asignacionesRepository;

    @InjectMocks
    private AsignacionesController asignacionesController;

    // ==========================================
    // PRUEBAS DE USUARIO CONTROLLER (UT-01 a UT-06)
    // ==========================================

    @Test
    @DisplayName("UT-01: Validar login con credenciales correctas")
    void testGetUsuarioLoginExitoso() {
        Usuario mockUser = new Usuario(131, "Ana Paola", "ana@test.com", "1234", "Estudiante", "#70FAB7");
        when(usuarioRepository.findByCorreoPass("ana@test.com", "1234")).thenReturn(Optional.of(mockUser));

        Usuario param = new Usuario();
        param.setCorreo_electronico("ana@test.com");
        param.setContrasena("1234");

        Usuario resultado = usuarioController.getUsuarioLogin(param);

        assertNotNull(resultado);
        assertEquals("Ana Paola", resultado.getNombre());
        assertEquals(131, resultado.getId_usuario());
    }

    @Test
    @DisplayName("UT-02: Validar login con credenciales incorrectas (Retorna nombre '000')")
    void testGetUsuarioLoginFallido() {
        when(usuarioRepository.findByCorreoPass("invalido@test.com", "wrong")).thenReturn(Optional.empty());

        Usuario param = new Usuario();
        param.setCorreo_electronico("invalido@test.com");
        param.setContrasena("wrong");

        Usuario resultado = usuarioController.getUsuarioLogin(param);

        assertNotNull(resultado);
        assertEquals("000", resultado.getNombre());
    }

    @Test
    @DisplayName("UT-03: Buscar usuario por ID existente")
    void testBuscarUsuarioPorIdExistente() {
        Usuario mockUser = new Usuario(131, "Ana Paola", "ana@test.com", "1234", "Estudiante", "#70FAB7");
        when(usuarioRepository.findById(131)).thenReturn(Optional.of(mockUser));

        Usuario resultado = usuarioController.getUsuario(131);

        assertNotNull(resultado);
        assertEquals(131, resultado.getId_usuario());
        assertEquals("Ana Paola", resultado.getNombre());
    }

    @Test
    @DisplayName("UT-04: Buscar usuario por ID inexistente")
    void testBuscarUsuarioPorIdInexistente() {
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        Usuario resultado = usuarioController.getUsuario(999);

        assertNotNull(resultado);
        assertNull(resultado.getId_usuario());
    }

    @Test
    @DisplayName("UT-05: Registrar nuevo usuario exitosamente")
    void testAddUsuarioValido() {
        Usuario nuevo = new Usuario(null, "Carlos Ruiz", "carlos@cbtis75.edu.mx", "pwd123", "Docente", "#FFFFFF");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevo);

        int resultado = usuarioController.addUsuario(nuevo);

        assertEquals(1, resultado);
        verify(usuarioRepository, times(1)).save(nuevo);
    }

    @Test
    @DisplayName("UT-06: Actualizar usuario existente y eliminar usuario")
    void testUpdateAndDeleteUsuario() {
        Usuario user = new Usuario(131, "Ana Paola Modificado", "ana@test.com", "1234", "Estudiante", "#70FAB7");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

        int resUpdate = usuarioController.updateUsuario(user);
        assertEquals(1, resUpdate);

        int resDelete = usuarioController.deleteUsuario(user);
        assertEquals(1, resDelete);
        verify(usuarioRepository, times(1)).delete(user);

        when(usuarioRepository.findAll()).thenReturn(Collections.singletonList(user));
        List<Usuario> lista = usuarioController.getUsuarios();
        assertEquals(1, lista.size());
    }

    // ==========================================
    // PRUEBAS DE PROYECTOS CONTROLLER (UT-07 a UT-09)
    // ==========================================

    @Test
    @DisplayName("UT-07: Buscar proyecto por ID existente e inexistente")
    void testBuscarProyectoPorId() {
        Proyectos mockP = new Proyectos(197, "My API Rest", "Desarrollo API Rest", "2024-05-31", "2024-06-29", "En proceso", "img.gif");
        when(proyectosRepository.findById(197)).thenReturn(Optional.of(mockP));
        when(proyectosRepository.findById(999)).thenReturn(Optional.empty());

        Proyectos res = proyectosController.getProyecto(197);
        assertNotNull(res);
        assertEquals("My API Rest", res.getNombre());

        Proyectos resEmpty = proyectosController.getProyecto(999);
        assertNotNull(resEmpty);
        assertNull(resEmpty.getId_proyecto());
    }

    @Test
    @DisplayName("UT-08: Guardar, actualizar y eliminar proyecto")
    void testCrudProyecto() {
        Proyectos nuevo = new Proyectos(null, "Gestor Escolar", "Proyecto de gestion", "2024-06-01", "2024-07-01", "Planificado", "bg.jpg");
        when(proyectosRepository.save(any(Proyectos.class))).thenReturn(nuevo);

        int flagAdd = proyectosController.addProyecto(nuevo);
        assertEquals(1, flagAdd);

        int flagUpdate = proyectosController.updateProyecto(nuevo);
        assertEquals(1, flagUpdate);

        int flagDelete = proyectosController.deleteProyecto(nuevo);
        assertEquals(1, flagDelete);
        verify(proyectosRepository, times(1)).delete(nuevo);
    }

    @Test
    @DisplayName("UT-09: Listar todos los proyectos")
    void testBuscarProyectosLista() {
        List<Proyectos> proyectos = Arrays.asList(
            new Proyectos(197, "My API Rest", "Desarrollo", "2024-05-31", "2024-06-29", "En proceso", "img.gif")
        );
        when(proyectosRepository.findAll()).thenReturn(proyectos);

        List<Proyectos> resultado = proyectosController.getProyectos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("My API Rest", resultado.get(0).getNombre());
    }

    // ==========================================
    // PRUEBAS DE TAREAS CONTROLLER (UT-10 a UT-12)
    // ==========================================

    @Test
    @DisplayName("UT-10: Buscar tareas asociadas a un ID de proyecto y listar todas")
    void testGetTareasPorProyecto() {
        List<Tareas> tareas = Arrays.asList(
            new Tareas(199, "Base de datos", "Crear tablas", "2024-06-13", "terminado", "danger", 197)
        );
        when(tareasRepository.findByIdProyecto(197)).thenReturn(tareas);
        when(tareasRepository.findAll()).thenReturn(tareas);

        Tareas param = new Tareas();
        param.setId_proyecto(197);

        List<Tareas> resultado = tareasController.getTarea(param);
        assertEquals(1, resultado.size());

        List<Tareas> todas = tareasController.getTareas();
        assertEquals(1, todas.size());
    }

    @Test
    @DisplayName("UT-11: Buscar tarea individual por ID existente e inexistente")
    void testBuscarTareaPorId() {
        Tareas mockT = new Tareas(199, "Base de datos", "Crear tablas", "2024-06-13", "terminado", "danger", 197);
        when(tareasRepository.findById(199)).thenReturn(Optional.of(mockT));
        when(tareasRepository.findById(999)).thenReturn(Optional.empty());

        Tareas resultado = tareasController.getTarea(199);
        assertNotNull(resultado);
        assertEquals(199, resultado.getId_tarea());

        Tareas noEncontrada = tareasController.getTarea(999);
        assertNull(noEncontrada.getId_tarea());
    }

    @Test
    @DisplayName("UT-12: Registrar, actualizar y eliminar tarea")
    void testCrudTareas() {
        Tareas nueva = new Tareas(null, "Documentacion", "Escribir informe", "2024-06-20", "pendiente", "info", 197);
        when(tareasRepository.save(any(Tareas.class))).thenReturn(nueva);

        int flagAdd = tareasController.addTareas(nueva);
        assertEquals(1, flagAdd);

        int flagUpdate = tareasController.updateTareas(nueva);
        assertEquals(1, flagUpdate);

        int flagDelete = tareasController.deleteTareas(nueva);
        assertEquals(1, flagDelete);
        verify(tareasRepository, times(1)).delete(nueva);
    }

    // ==========================================
    // PRUEBAS DE COMENTARIOS Y ASIGNACIONES (UT-13 a UT-15)
    // ==========================================

    @Test
    @DisplayName("UT-13: CRUD y consultas de Comentarios")
    void testComentariosController() {
        Comentarios com = new Comentarios();
        com.setId_comentario(201);
        com.setTexto_comentario("Excelente avance");
        com.setId_tarea(199);
        com.setId_usuario("Ana Paola");

        when(comentariosRepository.save(any(Comentarios.class))).thenReturn(com);
        when(comentariosRepository.findById(201)).thenReturn(Optional.of(com));
        when(comentariosRepository.findByTarea(199)).thenReturn(Collections.singletonList(com));
        when(comentariosRepository.findAll()).thenReturn(Collections.singletonList(com));

        assertEquals(1, comentariosController.addComentario(com));
        assertEquals(1, comentariosController.updateComentario(com));
        assertEquals(1, comentariosController.deleteComentario(com));
        assertEquals(201, comentariosController.getComentario(201).getId_comentario());
        assertEquals(1, comentariosController.getComentario(com).size());
        assertEquals(1, comentariosController.getComentarios().size());
    }

    @Test
    @DisplayName("UT-14: CRUD y consultas de Asignaciones")
    void testAsignacionesController() {
        Asignaciones asig = new Asignaciones();
        asig.setId_asignacion(1);
        asig.setId_usuario(131);
        asig.setId_tarea(199);

        when(asignacionesRepository.save(any(Asignaciones.class))).thenReturn(asig);
        when(asignacionesRepository.findById(1)).thenReturn(Optional.of(asig));
        when(asignacionesRepository.findAll()).thenReturn(Collections.singletonList(asig));

        assertEquals(1, asignacionesController.addAsignacion(asig));
        assertEquals(1, asignacionesController.updateAsignacion(asig));
        assertEquals(1, asignacionesController.deleteAsignacion(asig));
        assertEquals(1, asignacionesController.getAsignacion(1).getId_asignacion());
        assertEquals(1, asignacionesController.getAsignaciones().size());
    }

    // ==========================================
    // PRUEBAS DE MODELOS / GETTERS Y SETTERS (UT-15)
    // ==========================================

    @Test
    @DisplayName("UT-15: Cobertura de entidades del modelo (Usuario, Proyectos, Tareas, etc.)")
    void testModelosEntidades() {
        Usuario u = new Usuario();
        u.setId_usuario(1);
        u.setNombre("Test");
        u.setCorreo_electronico("test@correo.com");
        u.setContrasena("pass");
        u.setRol("Admin");
        u.setImagen_usuario("avatar.png");
        assertEquals(1, u.getId_usuario());
        assertEquals("Test", u.getNombre());
        assertEquals("test@correo.com", u.getCorreo_electronico());
        assertEquals("pass", u.getContrasena());
        assertEquals("Admin", u.getRol());
        assertEquals("avatar.png", u.getImagen_usuario());

        Proyectos p = new Proyectos();
        p.setId_proyecto(10);
        p.setNombre("P1");
        p.setDescripcion("D1");
        p.setFecha_inicio("2024-01-01");
        p.setFecha_finalizacion("2024-02-01");
        p.setEstado_proyecto("Activo");
        p.setFondo("bg.png");
        assertEquals(10, p.getId_proyecto());
        assertEquals("P1", p.getNombre());
        assertEquals("D1", p.getDescripcion());
        assertEquals("2024-01-01", p.getFecha_inicio());
        assertEquals("2024-02-01", p.getFecha_finalizacion());
        assertEquals("Activo", p.getEstado_proyecto());
        assertEquals("bg.png", p.getFondo());

        Tareas t = new Tareas();
        t.setId_tarea(5);
        t.setNombre("T1");
        t.setDescripcion("D1");
        t.setFecha_vencimiento("2024-03-01");
        t.setLista("Backlog");
        t.setEstado("Pendiente");
        t.setId_proyecto(10);
        assertEquals(5, t.getId_tarea());
        assertEquals("T1", t.getNombre());
        assertEquals("D1", t.getDescripcion());
        assertEquals("2024-03-01", t.getFecha_vencimiento());
        assertEquals("Backlog", t.getLista());
        assertEquals("Pendiente", t.getEstado());
        assertEquals(10, t.getId_proyecto());

        Comentarios c = new Comentarios();
        c.setId_comentario(1);
        c.setId_usuario("User1");
        c.setId_tarea(5);
        c.setTexto_comentario("Texto");
        c.setFecha_hora("2024-01-01");
        assertEquals(1, c.getId_comentario());
        assertEquals("Texto", c.getTexto_comentario());
        assertEquals("2024-01-01", c.getFecha_hora());
        assertEquals("User1", c.getId_usuario());
        assertEquals(5, c.getId_tarea());

        Asignaciones a = new Asignaciones(1, 1, 5, "2024-01-01");
        assertEquals(1, a.getId_asignacion());
        assertEquals(1, a.getId_usuario());
        assertEquals(5, a.getId_tarea());
        assertEquals("2024-01-01", a.getFecha_asignacion());
    }
}
