package com.example.projeto2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projeto2.adapter.TarefaAdapter;
import com.example.projeto2.database.TarefaDAO;
import com.example.projeto2.model.Tarefa;
import com.example.projeto2.ui.ConfiguracoesActivity;
import com.example.projeto2.ui.FormTarefaActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewTarefas;
    private FloatingActionButton fabAdicionar;
    private FloatingActionButton btnExcluirConcluidas;

    private TextView txtTotal;
    private TextView txtConcluidas;
    private TextView txtPendentes;

    private ArrayList<Tarefa> listaTarefas;
    private TarefaAdapter adapter;
    private TarefaDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dao = new TarefaDAO(this);

        listViewTarefas = findViewById(R.id.listViewTarefas);
        fabAdicionar = findViewById(R.id.fabAdicionar);
        btnExcluirConcluidas = findViewById(R.id.btnExcluirConcluidas);

        txtTotal = findViewById(R.id.txtTotal);
        txtConcluidas = findViewById(R.id.txtConcluidas);
        txtPendentes = findViewById(R.id.txtPendentes);

        // Botão + para adicionar tarefa
        fabAdicionar.setOnClickListener(v -> abrirFormulario(null));

        // Botão 🗑️ EXCLUIR CONCLUÍDAS
        btnExcluirConcluidas.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Excluir tarefas concluídas")
                    .setMessage("Deseja realmente excluir TODAS as tarefas concluídas?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirTarefasConcluidas())
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void mostrarOpcoesTarefa(Tarefa tarefa) {
        CharSequence[] opcoes = {"Editar", "Excluir", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle(tarefa.getTitulo())
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        abrirFormulario(tarefa);
                    } else if (which == 1) {
                        confirmarExclusao(tarefa);
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_configuracoes) {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    private void carregarLista() {
        listaTarefas = dao.listar();

        adapter = new TarefaAdapter(this, listaTarefas);

        adapter.setOnTarefaLongClickListener(this::mostrarOpcoesTarefa);

        adapter.setOnStatusChangeListener(this::atualizarContadores);

        listViewTarefas.setAdapter(adapter);

        atualizarContadores();
    }

    private void atualizarContadores() {
        int total = listaTarefas != null ? listaTarefas.size() : 0;
        int concluidas = 0;

        if (listaTarefas != null) {
            for (Tarefa t : listaTarefas) {
                if (t.isConcluido()) concluidas++;
            }
        }

        int pendentes = Math.max(0, total - concluidas);

        txtTotal.setText("Total: " + total);
        txtConcluidas.setText("Concluídas: " + concluidas);
        txtPendentes.setText("Pendentes: " + pendentes);
    }

    private void abrirFormulario(Tarefa tarefa) {
        Intent intent = new Intent(this, FormTarefaActivity.class);
        if (tarefa != null) {
            intent.putExtra("tarefa", tarefa);
        }
        startActivity(intent);
    }

    private void confirmarExclusao(Tarefa tarefa) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir tarefa")
                .setMessage("Deseja realmente excluir esta tarefa?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    dao.deletar(tarefa.getId());
                    carregarLista();
                    Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    // 🔥 NOVO MÉTODO — EXCLUI TODAS AS TAREFAS CONCLUÍDAS
    private void excluirTarefasConcluidas() {
        dao.excluirConcluidas();
        carregarLista();
        Toast.makeText(this, "Tarefas concluídas excluídas!", Toast.LENGTH_SHORT).show();
    }
}
