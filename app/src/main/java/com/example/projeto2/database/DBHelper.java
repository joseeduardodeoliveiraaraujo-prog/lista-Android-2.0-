package com.example.projeto2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ================================
 * DBHelper
 * ================================
 *
 * Classe responsável por criar e gerenciar o banco de dados SQLite do aplicativo.
 * Ela herda de SQLiteOpenHelper, que fornece métodos para criar e atualizar o BD.
 *
 * Funções principais:
 *  - Criar o banco e suas tabelas
 *  - Gerenciar versões do banco (atualizações)
 */
public class DBHelper extends SQLiteOpenHelper {

    // ================================
    // Constantes do banco
    // ================================
    private static final String NOME_BANCO = "tarefa.db"; // nome do arquivo do BD
    private static final int VERSAO = 1;                  // versão do BD (para upgrades)

    /**
     * Construtor
     * @param context Contexto da Activity ou Application
     */
    public DBHelper(Context context) {
        // Chama o construtor da classe pai (SQLiteOpenHelper)
        // 3º parâmetro é cursor factory (null = padrão)
        super(context, NOME_BANCO, null, VERSAO);
    }

    /**
     * Método chamado na primeira vez que o banco é criado
     * @param db Instância do SQLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Comando SQL para criar a tabela "tarefas"
        // Campos:
        // id         → chave primária autoincrement
        // titulo     → texto obrigatório
        // descricao  → texto opcional
        // data       → texto (dd/MM/yyyy)
        // prioridade → inteiro (1=Baixa, 2=Média, 3=Alta)
        // concluido  → inteiro (0 ou 1, padrão 0)
        String sql = "CREATE TABLE tarefas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titulo TEXT NOT NULL," +
                "descricao TEXT," +
                "data TEXT," +
                "prioridade INTEGER," +
                "concluido INTEGER DEFAULT 0" + // corrigido "INTERGE" → "INTEGER"
                ");";

        // Executa o SQL no banco
        db.execSQL(sql);
    }

    /**
     * Método chamado quando a versão do banco é incrementada
     * Serve para atualizar a estrutura do BD (ex: adicionar colunas)
     *
     * @param db Instância do SQLiteDatabase
     * @param oldVersion Versão antiga do banco
     * @param newVersion Versão nova do banco
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Descarta a tabela antiga e recria (simples, mas destrutivo)
        db.execSQL("DROP TABLE IF EXISTS tarefas");
        onCreate(db);
    }
}
