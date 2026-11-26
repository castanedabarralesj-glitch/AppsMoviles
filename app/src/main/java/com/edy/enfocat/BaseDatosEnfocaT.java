package com.edy.enfocat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;

public class BaseDatosEnfocaT extends SQLiteOpenHelper {

    private static final String TAG = "BaseDatosEnfocaT";
    private static final String DATABASE_NAME = "EnfocaT.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USUARIO = "usuario";
    public static final String TABLE_MATERIA = "materia";
    public static final String TABLE_TAREA = "tarea";
    public static final String TABLE_RECORDATORIO = "recordatorio";
    public static final String TABLE_SESION_ESTUDIO = "sesion_estudio";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Table 1: usuario columns
    public static final String COL_USUARIO_ID = "id_usuario";
    public static final String COL_USUARIO_NOMBRE = "nombre";
    public static final String COL_USUARIO_CORREO = "correo";
    public static final String COL_USUARIO_CONTRASENA_HASH = "contrasena_hash";
    public static final String COL_USUARIO_META_SEMANAL = "meta_semanal_min";
    public static final String COL_USUARIO_FECHA_REGISTRO = "fecha_registro";

    // Table 2: materia columns
    public static final String COL_MATERIA_ID = "id_materia";
    public static final String COL_MATERIA_ID_USUARIO = "id_usuario";
    public static final String COL_MATERIA_NOMBRE = "nombre";

    // Table 3: tarea columns
    public static final String COL_TAREA_ID = "id_tarea";
    public static final String COL_TAREA_ID_USUARIO = "id_usuario";
    public static final String COL_TAREA_ID_MATERIA = "id_materia";
    public static final String COL_TAREA_TITULO = "titulo";
    public static final String COL_TAREA_TIPO = "tipo"; // 'Entrega' o 'Examen'
    public static final String COL_TAREA_FECHA_ENTREGA = "fecha_entrega"; // YYYY-MM-DD
    public static final String COL_TAREA_HORA_ENTREGA = "hora_entrega"; // HH:MM
    public static final String COL_TAREA_PRIORIDAD = "prioridad"; // 'Baja', 'Media', 'Alta'
    public static final String COL_TAREA_NOTAS = "notas";
    public static final String COL_TAREA_COMPLETADA = "completada"; // 0 o 1
    public static final String COL_TAREA_FECHA_COMPLETADA = "fecha_completada";
    public static final String COL_TAREA_FECHA_CREACION = "fecha_creacion";

    // Table 4: recordatorio columns
    public static final String COL_RECORDATORIO_ID = "id_recordatorio";
    public static final String COL_RECORDATORIO_ID_TAREA = "id_tarea";
    public static final String COL_RECORDATORIO_ACTIVO = "activo"; // 0 o 1
    public static final String COL_RECORDATORIO_MINUTOS_ANTES = "minutos_antes";

    // Table 5: sesion_estudio columns
    public static final String COL_SESION_ID = "id_sesion";
    public static final String COL_SESION_ID_USUARIO = "id_usuario";
    public static final String COL_SESION_ID_MATERIA = "id_materia";
    public static final String COL_SESION_FECHA_INICIO = "fecha_inicio";
    public static final String COL_SESION_MIN_ENFOQUE = "min_enfoque";
    public static final String COL_SESION_MIN_PAUSA = "min_pausa";
    public static final String COL_SESION_NUMERO_CICLO = "numero_ciclo";
    public static final String COL_SESION_MIN_ESTUDIADOS = "min_estudiados";
    public static final String COL_SESION_COMPLETADA = "completada"; // 0 o 1

    public BaseDatosEnfocaT(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys = ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creando tablas de la base de datos EnfocaT.db");

        // Tabla 1: usuario
        String CREATE_TABLA_USUARIO = "CREATE TABLE " + TABLE_USUARIO + " (" +
                COL_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USUARIO_NOMBRE + " TEXT NOT NULL, " +
                COL_USUARIO_CORREO + " TEXT NOT NULL UNIQUE, " +
                COL_USUARIO_CONTRASENA_HASH + " TEXT NOT NULL, " +
                COL_USUARIO_META_SEMANAL + " INTEGER, " +
                COL_USUARIO_FECHA_REGISTRO + " TEXT NOT NULL" +
                ");";

        // Tabla 2: materia
        String CREATE_TABLA_MATERIA = "CREATE TABLE " + TABLE_MATERIA + " (" +
                COL_MATERIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MATERIA_ID_USUARIO + " INTEGER NOT NULL, " +
                COL_MATERIA_NOMBRE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COL_MATERIA_ID_USUARIO + ") REFERENCES " + TABLE_USUARIO + "(" + COL_USUARIO_ID + ") ON DELETE CASCADE" +
                ");";

        // Tabla 3: tarea
        String CREATE_TABLA_TAREA = "CREATE TABLE " + TABLE_TAREA + " (" +
                COL_TAREA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TAREA_ID_USUARIO + " INTEGER NOT NULL, " +
                COL_TAREA_ID_MATERIA + " INTEGER NOT NULL, " +
                COL_TAREA_TITULO + " TEXT NOT NULL, " +
                COL_TAREA_TIPO + " TEXT NOT NULL CHECK(" + COL_TAREA_TIPO + " IN ('Entrega', 'Examen')), " +
                COL_TAREA_FECHA_ENTREGA + " TEXT NOT NULL, " +
                COL_TAREA_HORA_ENTREGA + " TEXT NOT NULL, " +
                COL_TAREA_PRIORIDAD + " TEXT NOT NULL CHECK(" + COL_TAREA_PRIORIDAD + " IN ('Baja', 'Media', 'Alta')), " +
                COL_TAREA_NOTAS + " TEXT, " +
                COL_TAREA_COMPLETADA + " INTEGER NOT NULL DEFAULT 0, " +
                COL_TAREA_FECHA_COMPLETADA + " TEXT, " +
                COL_TAREA_FECHA_CREACION + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COL_TAREA_ID_USUARIO + ") REFERENCES " + TABLE_USUARIO + "(" + COL_USUARIO_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COL_TAREA_ID_MATERIA + ") REFERENCES " + TABLE_MATERIA + "(" + COL_MATERIA_ID + ") ON DELETE CASCADE" +
                ");";

        // Tabla 4: recordatorio
        String CREATE_TABLA_RECORDATORIO = "CREATE TABLE " + TABLE_RECORDATORIO + " (" +
                COL_RECORDATORIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECORDATORIO_ID_TAREA + " INTEGER NOT NULL UNIQUE, " +
                COL_RECORDATORIO_ACTIVO + " INTEGER NOT NULL DEFAULT 1, " +
                COL_RECORDATORIO_MINUTOS_ANTES + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COL_RECORDATORIO_ID_TAREA + ") REFERENCES " + TABLE_TAREA + "(" + COL_TAREA_ID + ") ON DELETE CASCADE" +
                ");";

        // Tabla 5: sesion_estudio
        String CREATE_TABLA_SESION_ESTUDIO = "CREATE TABLE " + TABLE_SESION_ESTUDIO + " (" +
                COL_SESION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESION_ID_USUARIO + " INTEGER NOT NULL, " +
                COL_SESION_ID_MATERIA + " INTEGER NOT NULL, " +
                COL_SESION_FECHA_INICIO + " TEXT NOT NULL, " +
                COL_SESION_MIN_ENFOQUE + " INTEGER NOT NULL DEFAULT 25, " +
                COL_SESION_MIN_PAUSA + " INTEGER NOT NULL DEFAULT 5, " +
                COL_SESION_NUMERO_CICLO + " INTEGER NOT NULL, " +
                COL_SESION_MIN_ESTUDIADOS + " INTEGER NOT NULL, " +
                COL_SESION_COMPLETADA + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + COL_SESION_ID_USUARIO + ") REFERENCES " + TABLE_USUARIO + "(" + COL_USUARIO_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COL_SESION_ID_MATERIA + ") REFERENCES " + TABLE_MATERIA + "(" + COL_MATERIA_ID + ") ON DELETE CASCADE" +
                ");";

        db.execSQL(CREATE_TABLA_USUARIO);
        db.execSQL(CREATE_TABLA_MATERIA);
        db.execSQL(CREATE_TABLA_TAREA);
        db.execSQL(CREATE_TABLA_RECORDATORIO);
        db.execSQL(CREATE_TABLA_SESION_ESTUDIO);

        // Índices para claves foráneas y búsquedas frecuentes
        db.execSQL("CREATE INDEX idx_materia_usuario ON " + TABLE_MATERIA + "(" + COL_MATERIA_ID_USUARIO + ");");
        db.execSQL("CREATE INDEX idx_tarea_usuario ON " + TABLE_TAREA + "(" + COL_TAREA_ID_USUARIO + ");");
        db.execSQL("CREATE INDEX idx_tarea_materia ON " + TABLE_TAREA + "(" + COL_TAREA_ID_MATERIA + ");");
        db.execSQL("CREATE INDEX idx_recordatorio_tarea ON " + TABLE_RECORDATORIO + "(" + COL_RECORDATORIO_ID_TAREA + ");");
        db.execSQL("CREATE INDEX idx_sesion_usuario ON " + TABLE_SESION_ESTUDIO + "(" + COL_SESION_ID_USUARIO + ");");
        db.execSQL("CREATE INDEX idx_sesion_materia ON " + TABLE_SESION_ESTUDIO + "(" + COL_SESION_ID_MATERIA + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Actualizando base de datos de versión " + oldVersion + " a " + newVersion + ". Migración segura.");
        // Implementación de migración segura sin perder datos de usuario si se incrementa la versión en el futuro.
        // Para la versión 1 inicial, se pueden aplicar cambios incrementales con ALTER TABLE según sea necesario.
    }

    // ==========================================
    // MÉTODOS DE UTILIDAD Y SEGURIDAD (HASH)
    // ==========================================

    public static String hashContrasena(String contrasenaPlana) {
        try {
            int iterations = 10000;
            char[] chars = contrasenaPlana.toCharArray();
            byte[] salt = getSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return iterations + ":" + toHex(salt) + ":" + toHex(hash);
        } catch (Exception e) {
            Log.e(TAG, "Error al generar hash de contraseña", e);
            return null;
        }
    }

    public static boolean verificarContrasena(String contrasenaPlana, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = fromHex(parts[1]);
            byte[] hash = fromHex(parts[2]);

            PBEKeySpec spec = new PBEKeySpec(contrasenaPlana.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar contraseña", e);
            return false;
        }
    }

    private static byte[] getSalt() throws Exception {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format(Locale.getDefault(), "%0" + paddingLength + "d", 0) + hex;
        }
        return hex;
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private String obtenerFechaHoraActual() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ==========================================
    // MÉTODOS CRUD / OPERACIONES DE LA BD
    // ==========================================

    // 1. Registrar un usuario
    public long registrarUsuario(String nombre, String correo, String contrasenaPlana, int metaSemanalMin) {
        if (correoExiste(correo)) {
            return -1; // El correo ya está registrado
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USUARIO_NOMBRE, nombre);
        values.put(COL_USUARIO_CORREO, correo);
        values.put(COL_USUARIO_CONTRASENA_HASH, hashContrasena(contrasenaPlana));
        values.put(COL_USUARIO_META_SEMANAL, metaSemanalMin);
        values.put(COL_USUARIO_FECHA_REGISTRO, obtenerFechaHoraActual());

        return db.insert(TABLE_USUARIO, null, values);
    }

    // 2. Consultar un usuario por su correo
    public Cursor consultarUsuarioPorCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDataSource();
        return db.query(TABLE_USUARIO,
                null,
                COL_USUARIO_CORREO + " = ?",
                new String[]{correo},
                null, null, null);
    }

    // 3. Verificar si un correo ya está registrado
    public boolean correoExiste(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIO,
                new String[]{COL_USUARIO_ID},
                COL_USUARIO_CORREO + " = ?",
                new String[]{correo},
                null, null, null);
        boolean existe = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return existe;
    }

    private SQLiteDatabase getReadableDataSource() {
        return this.getReadableDatabase();
    }

    // 4. Insertar una materia
    public long insertarMateria(long idUsuario, String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MATERIA_ID_USUARIO, idUsuario);
        values.put(COL_MATERIA_NOMBRE, nombre);
        return db.insert(TABLE_MATERIA, null, values);
    }

    // 5. Consultar las materias de un usuario
    public Cursor consultarMateriasDeUsuario(long idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MATERIA,
                null,
                COL_MATERIA_ID_USUARIO + " = ?",
                new String[]{String.valueOf(idUsuario)},
                null, null, COL_MATERIA_NOMBRE + " ASC");
    }

    // 6. Insertar una tarea
    public long insertarTarea(long idUsuario, long idMateria, String titulo, String tipo,
                              String fechaEntrega, String horaEntrega, String prioridad, String notas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAREA_ID_USUARIO, idUsuario);
        values.put(COL_TAREA_ID_MATERIA, idMateria);
        values.put(COL_TAREA_TITULO, titulo);
        values.put(COL_TAREA_TIPO, tipo);
        values.put(COL_TAREA_FECHA_ENTREGA, fechaEntrega);
        values.put(COL_TAREA_HORA_ENTREGA, horaEntrega);
        values.put(COL_TAREA_PRIORIDAD, prioridad);
        values.put(COL_TAREA_NOTAS, notas);
        values.put(COL_TAREA_COMPLETADA, 0);
        values.put(COL_TAREA_FECHA_CREACION, obtenerFechaHoraActual());
        return db.insert(TABLE_TAREA, null, values);
    }

    // 7. Consultar todas las tareas de un usuario
    public Cursor consultarTareasDeUsuario(long idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TAREA,
                null,
                COL_TAREA_ID_USUARIO + " = ?",
                new String[]{String.valueOf(idUsuario)},
                null, null, COL_TAREA_FECHA_ENTREGA + " ASC");
    }

    // 8. Consultar tareas por materia
    public Cursor consultarTareasPorMateria(long idMateria) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TAREA,
                null,
                COL_TAREA_ID_MATERIA + " = ?",
                new String[]{String.valueOf(idMateria)},
                null, null, COL_TAREA_FECHA_ENTREGA + " ASC");
    }

    // 9. Actualizar una tarea
    public int actualizarTarea(long idTarea, String titulo, String tipo, String fechaEntrega,
                               String horaEntrega, String prioridad, String notas) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAREA_TITULO, titulo);
        values.put(COL_TAREA_TIPO, tipo);
        values.put(COL_TAREA_FECHA_ENTREGA, fechaEntrega);
        values.put(COL_TAREA_HORA_ENTREGA, horaEntrega);
        values.put(COL_TAREA_PRIORIDAD, prioridad);
        values.put(COL_TAREA_NOTAS, notas);
        return db.update(TABLE_TAREA, values, COL_TAREA_ID + " = ?", new String[]{String.valueOf(idTarea)});
    }

    // 10. Marcar una tarea como completada
    public int marcarTareaCompletada(long idTarea, int completada) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TAREA_COMPLETADA, completada);
        values.put(COL_TAREA_FECHA_COMPLETADA, completada == 1 ? obtenerFechaHoraActual() : null);
        return db.update(TABLE_TAREA, values, COL_TAREA_ID + " = ?", new String[]{String.valueOf(idTarea)});
    }

    // 11. Eliminar una tarea
    public int eliminarTarea(long idTarea) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TAREA, COL_TAREA_ID + " = ?", new String[]{String.valueOf(idTarea)});
    }

    // 12. Crear recordatorio (una tarea tiene 0 o 1 recordatorio, UNIQUE en id_tarea)
    public long crearRecordatorio(long idTarea, int activo, int minutosAntes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RECORDATORIO_ID_TAREA, idTarea);
        values.put(COL_RECORDATORIO_ACTIVO, activo);
        values.put(COL_RECORDATORIO_MINUTOS_ANTES, minutosAntes);
        return db.insertWithOnConflict(TABLE_RECORDATORIO, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // 13. Consultar recordatorio de una tarea
    public Cursor consultarRecordatorioDeTarea(long idTarea) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RECORDATORIO,
                null,
                COL_RECORDATORIO_ID_TAREA + " = ?",
                new String[]{String.valueOf(idTarea)},
                null, null, null);
    }

    // 14. Registrar sesiones de estudio
    public long registrarSesionEstudio(long idUsuario, long idMateria, String fechaInicio,
                                      int minEnfoque, int minPausa, int numeroCiclo,
                                      int minEstudiados, int completada) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SESION_ID_USUARIO, idUsuario);
        values.put(COL_SESION_ID_MATERIA, idMateria);
        values.put(COL_SESION_FECHA_INICIO, fechaInicio);
        values.put(COL_SESION_MIN_ENFOQUE, minEnfoque);
        values.put(COL_SESION_MIN_PAUSA, minPausa);
        values.put(COL_SESION_NUMERO_CICLO, numeroCiclo);
        values.put(COL_SESION_MIN_ESTUDIADOS, minEstudiados);
        values.put(COL_SESION_COMPLETADA, completada);
        return db.insert(TABLE_SESION_ESTUDIO, null, values);
    }

    // 15. Consultar el historial de sesiones de estudio de un usuario
    public Cursor consultarHistorialSesionesDeUsuario(long idUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SESION_ESTUDIO,
                null,
                COL_SESION_ID_USUARIO + " = ?",
                new String[]{String.valueOf(idUsuario)},
                null, null, COL_SESION_FECHA_INICIO + " DESC");
    }
}
