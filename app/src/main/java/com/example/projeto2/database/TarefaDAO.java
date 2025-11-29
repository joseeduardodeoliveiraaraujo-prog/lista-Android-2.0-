package com.example.projeto2.database;

// Funções do DAO:
// - inserir tarefas
// - atualizar tarefas
// - deletar tarefa por ID
// - deletar todas tarefas concluídas
// - listar tarefas

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projeto2.model.Tarefa;
import com.example.projeto2.utils.Preferencias;

import java.util.ArrayList;

public class TarefaDAO {
    private final DBHelper dbHelper;
    private final Context context;

    public TarefaDAO(Context context){
        this.context = context;
        this.dbHelper = new DBHelper(context);
    }

    // INSERIR - criar nova tarefa
    public long inserir(Tarefa t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("titulo", t.getTitulo());
        valores.put("descricao", t.getDescricao());
        valores.put("data", t.getData());
        valores.put("prioridade", t.getPrioridade());
        valores.put("concluido", t.isConcluido() ? 1 : 0);

        long idGerado = db.insert("tarefas", null, valores);
        db.close();
        return idGerado;
    }

    // ATUALIZAR - atualizar tarefa existente
    public int atualizar(Tarefa t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues valores = new ContentValues();

        valores.put("titulo", t.getTitulo());
        valores.put("descricao", t.getDescricao());
        valores.put("data", t.getData());
        valores.put("prioridade", t.getPrioridade());
        valores.put("concluido", t.isConcluido() ? 1 : 0);

        int linhasAfetadas = db.update(
                "tarefas",
                valores,
                "id = ?",
                new String[]{String.valueOf(t.getId())}
        );

        db.close();
        return linhasAfetadas;
    }

    // DELETAR - remover tarefa por ID
    public int deletar(int id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int linhasAfetadas = db.delete(
                "tarefas",
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return linhasAfetadas;
    }

    // DELETAR todas tarefas concluídas
    public int excluirConcluidas() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = db.delete("tarefas", "concluido = ?", new String[]{"1"});
        db.close();
        return linhasAfetadas;
    }

    // LISTAR - obter todas tarefas (com filtros de preferência)
    public ArrayList<Tarefa> listar (){
        ArrayList<Tarefa> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Preferencias prefs = new Preferencias(context);
        boolean ocultarConcluidas = prefs.getOcultarConcluidas();
        int ordenacao = prefs.getOrdenacao();
        int prioridadeMinima = prefs.getPrioridadeMinima();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, titulo, descricao, data, prioridade, concluido FROM tarefas ");

        boolean whereAdicionado = false;

        if (ocultarConcluidas){
            sql.append("WHERE concluido = 0 ");
            whereAdicionado = true;
        }

        if (prioridadeMinima == 2 || prioridadeMinima == 3){
            sql.append(whereAdicionado ? "AND " : "WHERE ");
            sql.append("prioridade >= ").append(prioridadeMinima).append(" ");
            whereAdicionado = true;
        }

        switch (ordenacao){
            case 1:
                sql.append("ORDER BY titulo COLLATE NOCASE ASC");
                break;
            case 2:
                sql.append("ORDER BY (substr(data,7,4)||substr(data,4,2)||substr(data,1,2)) ASC");
                break;
            case 3:
                sql.append("ORDER BY prioridade DESC");
                break;
            case 0:
            default:
                sql.append("ORDER BY id ASC");
                break;
        }

        Cursor cursor = db.rawQuery(sql.toString(), null);

        if (cursor.moveToFirst()){
            do {
                Tarefa t = new Tarefa();
                t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                t.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                t.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
                t.setData(cursor.getString(cursor.getColumnIndexOrThrow("data")));
                t.setPrioridade(cursor.getInt(cursor.getColumnIndexOrThrow("prioridade")));
                t.setConcluido(cursor.getInt(cursor.getColumnIndexOrThrow("concluido")) == 1);

                lista.add(t);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }
}
