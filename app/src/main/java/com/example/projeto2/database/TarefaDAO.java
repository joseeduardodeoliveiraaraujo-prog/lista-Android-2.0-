package com.example.projeto2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projeto2.model.Tarefa;
import com.example.projeto2.utils.Preferencias;

import java.util.ArrayList;

/**
 * ================================
 * DAO — Data Access Object
 * ================================
 *
 * Esta classe gerencia todas as operações de banco de dados relacionadas à entidade Tarefa.
 * Funcionalidades principais:
 * - Inserir nova tarefa
 * - Atualizar tarefa existente
 * - Deletar tarefa por ID
 * - Deletar todas tarefas concluídas
 * - Listar tarefas, aplicando filtros de preferências
 */
public class TarefaDAO {

    private final DBHelper dbHelper; // Classe auxiliar que cria/abre o banco de dados
    private final Context context;   // Contexto da Activity que utiliza o DAO

    /**
     * Construtor
     * @param context Contexto da Activity
     */
    public TarefaDAO(Context context){
        this.context = context;
        this.dbHelper = new DBHelper(context); // inicializa o DBHelper
    }

    // ================================
    // INSERIR - criar nova tarefa
    // ================================
    public long inserir(Tarefa t){
        SQLiteDatabase db = dbHelper.getWritableDatabase(); // abre o banco para escrita
        ContentValues valores = new ContentValues();        // objeto para armazenar pares chave-valor

        // Preenche valores com os atributos da tarefa
        valores.put("titulo", t.getTitulo());
        valores.put("descricao", t.getDescricao());
        valores.put("data", t.getData());
        valores.put("prioridade", t.getPrioridade());
        valores.put("concluido", t.isConcluido() ? 1 : 0); // SQLite não tem boolean, usa 0 ou 1

        // Insere os valores na tabela "tarefas"
        long idGerado = db.insert("tarefas", null, valores);

        db.close(); // fecha o banco
        return idGerado; // retorna o ID gerado pelo banco
    }

    // ================================
    // ATUALIZAR - atualizar tarefa existente
    // ================================
    public int atualizar(Tarefa t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("titulo", t.getTitulo());
        valores.put("descricao", t.getDescricao());
        valores.put("data", t.getData());
        valores.put("prioridade", t.getPrioridade());
        valores.put("concluido", t.isConcluido() ? 1 : 0);

        // Atualiza a tarefa com base no ID
        int linhasAfetadas = db.update(
                "tarefas",
                valores,
                "id = ?",
                new String[]{String.valueOf(t.getId())} // parâmetro para o WHERE
        );

        db.close();
        return linhasAfetadas; // retorna o número de linhas alteradas
    }

    // ================================
    // DELETAR - remover tarefa por ID
    // ================================
    public int deletar(int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Deleta a tarefa específica
        int linhasAfetadas = db.delete(
                "tarefas",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return linhasAfetadas; // retorna número de linhas deletadas
    }

    // ================================
    // DELETAR - remover todas tarefas concluídas
    // ================================
    public int excluirConcluidas() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Deleta todas as tarefas cujo campo concluido = 1
        int linhasAfetadas = db.delete("tarefas", "concluido = ?", new String[]{"1"});

        db.close();
        return linhasAfetadas;
    }

    // ================================
    // LISTAR - obter todas tarefas, aplicando filtros
    // ================================
    public ArrayList<Tarefa> listar (){
        ArrayList<Tarefa> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // abre banco somente leitura

        // Recupera preferências do usuário
        Preferencias prefs = new Preferencias(context);
        boolean ocultarConcluidas = prefs.getOcultarConcluidas(); // ocultar tarefas concluídas?
        int ordenacao = prefs.getOrdenacao();                     // tipo de ordenação
        int prioridadeMinima = prefs.getPrioridadeMinima();       // prioridade mínima

        // Monta SQL básico
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, titulo, descricao, data, prioridade, concluido FROM tarefas ");

        boolean whereAdicionado = false; // flag para controlar cláusula WHERE

        // Filtro: ocultar concluídas
        if (ocultarConcluidas){
            sql.append("WHERE concluido = 0 ");
            whereAdicionado = true;
        }

        // Filtro: prioridade mínima
        if (prioridadeMinima == 2 || prioridadeMinima == 3){
            sql.append(whereAdicionado ? "AND " : "WHERE ");
            sql.append("prioridade >= ").append(prioridadeMinima).append(" ");
            whereAdicionado = true;
        }

        // Ordenação
        switch (ordenacao){
            case 1:
                sql.append("ORDER BY titulo COLLATE NOCASE ASC"); // ordem alfabética
                break;
            case 2:
                // ordena datas corretamente (converte dd/MM/yyyy → yyyyMMdd)
                sql.append("ORDER BY (substr(data,7,4)||substr(data,4,2)||substr(data,1,2)) ASC");
                break;
            case 3:
                sql.append("ORDER BY prioridade DESC"); // prioridade alta primeiro
                break;
            case 0:
            default:
                sql.append("ORDER BY id ASC"); // ordem de inserção
                break;
        }

        // Executa a query
        Cursor cursor = db.rawQuery(sql.toString(), null);

        // Percorre os resultados
        if (cursor.moveToFirst()){
            do {
                Tarefa t = new Tarefa();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                t.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
                t.setData(cursor.getString(cursor.getColumnIndexOrThrow("data")));
                t.setPrioridade(cursor.getInt(cursor.getColumnIndexOrThrow("prioridade")));
                t.setConcluido(cursor.getInt(cursor.getColumnIndexOrThrow("concluido")) == 1);

                lista.add(t); // adiciona à lista
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista; // retorna todas as tarefas filtradas
    }
}
