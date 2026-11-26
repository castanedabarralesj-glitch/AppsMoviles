package com.edy.enfocat;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private BaseDatosEnfocaT dbHelper;
    private SQLiteDatabase db;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        // Borrar base de datos previa para prueba limpia
        context.deleteDatabase("EnfocaT.db");
        dbHelper = new BaseDatosEnfocaT(context);
        db = dbHelper.getWritableDatabase();
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Test
    public void testDatabaseCreationAndTables() {
        // 1. Comprobar que se creó la base de datos y existen las 5 tablas
        assertNotNull(db);
        assertTrue(db.isOpen());

        String[] tablasEsperadas = {
                BaseDatosEnfocaT.TABLE_USUARIO,
                BaseDatosEnfocaT.TABLE_MATERIA,
                BaseDatosEnfocaT.TABLE_TAREA,
                BaseDatosEnfocaT.TABLE_RECORDATORIO,
                BaseDatosEnfocaT.TABLE_SESION_ESTUDIO
        };

        for (String tabla : tablasEsperadas) {
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tabla}
            );
            assertNotNull(cursor);
            assertTrue("La tabla " + tabla + " debería existir", cursor.moveToFirst());
            cursor.close();
        }
    }

    @Test
    public void testCompleteWorkflowAndRelations() {
        // 3. Registrar un usuario
        long idUsuario = dbHelper.registrarUsuario(
                "Carlos Pérez",
                "carlos.perez@example.com",
                "password123",
                120
        );
        assertNotEquals("El usuario debe registrarse correctamente", -1, idUsuario);

        // Verificar consulta por correo y hash
        Cursor cUsuario = dbHelper.consultarUsuarioPorCorreo("carlos.perez@example.com");
        assertNotNull(cUsuario);
        assertTrue(cUsuario.moveToFirst());
        String hash = cUsuario.getString(cUsuario.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_USUARIO_CONTRASENA_HASH));
        assertTrue(BaseDatosEnfocaT.verificarContrasena("password123", hash));
        cUsuario.close();

        // 4. Registrar una materia relacionada con el usuario
        long idMateria = dbHelper.insertarMateria(idUsuario, "Matemáticas Discretas");
        assertNotEquals("La materia debe insertarse correctamente", -1, idMateria);

        Cursor cMaterias = dbHelper.consultarMateriasDeUsuario(idUsuario);
        assertNotNull(cMaterias);
        assertTrue(cMaterias.moveToFirst());
        assertEquals("Matemáticas Discretas", cMaterias.getString(cMaterias.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_MATERIA_NOMBRE)));
        cMaterias.close();

        // 5. Registrar una tarea relacionada con usuario y materia
        long idTarea = dbHelper.insertarTarea(
                idUsuario,
                idMateria,
                "Entrega de Proyecto 1",
                "Entrega",
                "2026-06-15",
                "23:59",
                "Alta",
                "Completar documentación y código fuente."
        );
        assertNotEquals("La tarea debe insertarse correctamente", -1, idTarea);

        Cursor cTareas = dbHelper.consultarTareasDeUsuario(idUsuario);
        assertNotNull(cTareas);
        assertTrue(cTareas.moveToFirst());
        assertEquals("Entrega de Proyecto 1", cTareas.getString(cTareas.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_TAREA_TITULO)));
        cTareas.close();

        // 6. Agregar un recordatorio a la tarea
        long idRecordatorio = dbHelper.crearRecordatorio(idTarea, 1, 30);
        assertNotEquals("El recordatorio debe crearse correctamente", -1, idRecordatorio);

        Cursor cRecordatorio = dbHelper.consultarRecordatorioDeTarea(idTarea);
        assertNotNull(cRecordatorio);
        assertTrue(cRecordatorio.moveToFirst());
        assertEquals(30, cRecordatorio.getInt(cRecordatorio.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_RECORDATORIO_MINUTOS_ANTES)));
        cRecordatorio.close();

        // 7. Registrar una sesión de estudio
        long idSesion = dbHelper.registrarSesionEstudio(
                idUsuario,
                idMateria,
                "2026-06-01 10:00:00",
                25,
                5,
                1,
                25,
                1
        );
        assertNotEquals("La sesión de estudio debe registrarse correctamente", -1, idSesion);

        Cursor cSesiones = dbHelper.consultarHistorialSesionesDeUsuario(idUsuario);
        assertNotNull(cSesiones);
        assertTrue(cSesiones.moveToFirst());
        assertEquals(25, cSesiones.getInt(cSesiones.getColumnIndexOrThrow(BaseDatosEnfocaT.COL_SESION_MIN_ENFOQUE)));
        cSesiones.close();
    }
}
