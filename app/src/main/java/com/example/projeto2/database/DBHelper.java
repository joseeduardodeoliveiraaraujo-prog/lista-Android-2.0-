package com.example.projeto2.database;
// Resposavel por criar e atualizar o DB
// Funções principais:
// - criar BD e as tabelas
// - Gerenciar as versões do BD

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String NOME_BANCO = "tarefa.db";
    private static final int VERSAO = 1;

    public DBHelper(Context context) {
        super(context, NOME_BANCO, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // comandos SQL
        String sql = "CREATE TABLE tarefas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titulo TEXT NOT NULL," +
                "descricao TEXT," +
                "data TEXT," +
                "prioridade INTEGER," +
                "concluido INTERGE DEFAULT 0" +
                ");";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tarefas");
        onCreate(db);
    }
}
