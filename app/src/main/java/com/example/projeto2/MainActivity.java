package com.example.projeto2;

// Importações de componentes do Android e bibliotecas auxiliares
import android.app.AlertDialog;               // Para criar caixas de diálogo
import android.content.Intent;                // Para navegar entre Activities
import android.os.Bundle;                     // Para ciclo de vida da Activity
import android.view.Menu;                     // Para menu da toolbar
import android.view.MenuItem;                 // Para itens do menu
import android.widget.ListView;               // Lista de itens
import android.widget.Toast;                  // Mensagem rápida na tela
import android.widget.TextView;               // Para mostrar texto

import androidx.activity.EdgeToEdge;          // Ajuste de layout para "edge to edge"
import androidx.appcompat.app.AppCompatActivity; // Activity compatível com AppCompat
import androidx.core.graphics.Insets;         // Para ajustar margens do sistema
import androidx.core.view.ViewCompat;         // Utilitário para views
import androidx.core.view.WindowInsetsCompat; // Para pegar barras de sistema (status/nav)

import com.example.projeto2.adapter.TarefaAdapter; // Adapter customizado da lista
import com.example.projeto2.database.TarefaDAO;    // Classe de acesso ao banco de dados
import com.example.projeto2.model.Tarefa;         // Modelo da tarefa
import com.example.projeto2.ui.ConfiguracoesActivity; // Tela de configurações
import com.example.projeto2.ui.FormTarefaActivity;   // Tela de cadastro/edição de tarefa
import com.google.android.material.appbar.MaterialToolbar; // Toolbar do Material Design
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Botão flutuante

import java.util.ArrayList; // Lista dinâmica para armazenar tarefas

// ================================
// MainActivity — tela principal do app
// ================================
public class MainActivity extends AppCompatActivity {

    // ================================
    // Componentes da interface
    // ================================
    private ListView listViewTarefas;           // Lista que exibirá as tarefas
    private FloatingActionButton fabAdicionar;  // Botão flutuante para adicionar nova tarefa
    private FloatingActionButton btnExcluirConcluidas; // Botão flutuante para excluir todas tarefas concluídas

    private TextView txtTotal;       // Texto mostrando total de tarefas
    private TextView txtConcluidas;  // Texto mostrando quantidade de tarefas concluídas
    private TextView txtPendentes;   // Texto mostrando quantidade de tarefas pendentes

    // ================================
    // Objetos auxiliares
    // ================================
    private ArrayList<Tarefa> listaTarefas; // Lista de tarefas carregadas do banco
    private TarefaAdapter adapter;           // Adapter para preencher o ListView
    private TarefaDAO dao;                   // Acesso ao banco de dados

    // ================================
    // Ciclo de vida — onCreate
    // ================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajusta layout "edge-to-edge" (conteúdo aparece atrás das barras do sistema)
        EdgeToEdge.enable(this);

        // Define o layout XML da Activity
        setContentView(R.layout.activity_main);

        // Aplica padding para não sobrepor barras do sistema (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ================================
        // Configuração da toolbar
        // ================================
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ================================
        // Inicializa DAO (acesso ao banco)
        // ================================
        dao = new TarefaDAO(this);

        // ================================
        // Referências dos componentes do layout
        // ================================
        listViewTarefas = findViewById(R.id.listViewTarefas);
        fabAdicionar = findViewById(R.id.fabAdicionar);
        btnExcluirConcluidas = findViewById(R.id.btnExcluirConcluidas);

        txtTotal = findViewById(R.id.txtTotal);
        txtConcluidas = findViewById(R.id.txtConcluidas);
        txtPendentes = findViewById(R.id.txtPendentes);

        // ================================
        // Clique no botão + (adicionar tarefa)
        // ================================
        fabAdicionar.setOnClickListener(v -> abrirFormulario(null)); // null indica nova tarefa

        // ================================
        // Clique no botão 🗑️ para excluir tarefas concluídas
        // ================================
        btnExcluirConcluidas.setOnClickListener(v -> {
            // Abre um AlertDialog para confirmação
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Excluir tarefas concluídas")
                    .setMessage("Deseja realmente excluir TODAS as tarefas concluídas?")
                    .setPositiveButton("Sim", (dialog, which) -> excluirTarefasConcluidas()) // chama o método
                    .setNegativeButton("Cancelar", null) // cancela a ação
                    .show();
        });
    }

    // ================================
    // Mostra opções de ação para cada tarefa (Editar / Excluir / Cancelar)
    // ================================
    private void mostrarOpcoesTarefa(Tarefa tarefa) {
        CharSequence[] opcoes = {"Editar", "Excluir", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle(tarefa.getTitulo()) // título do diálogo é o título da tarefa
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) { // Editar
                        abrirFormulario(tarefa);
                    } else if (which == 1) { // Excluir
                        confirmarExclusao(tarefa);
                    }
                    // "Cancelar" não precisa de ação
                })
                .show();
    }

    // ================================
    // Menu da toolbar
    // ================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // inflar menu XML
        return true;
    }

    // ================================
    // Ação ao clicar em itens do menu
    // ================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_configuracoes) {
            // Abre a Activity de configurações
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ================================
    // onResume: atualizar lista quando Activity volta para foreground
    // ================================
    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    // ================================
    // Carrega lista de tarefas do banco e atualiza ListView
    // ================================
    private void carregarLista() {
        // Pega todas as tarefas do banco
        listaTarefas = dao.listar();

        // Cria o adapter passando a lista de tarefas
        adapter = new TarefaAdapter(this, listaTarefas);

        // Listener para clique longo em uma tarefa
        adapter.setOnTarefaLongClickListener(this::mostrarOpcoesTarefa);

        // Listener para mudança de status (concluída/pendente)
        adapter.setOnStatusChangeListener(this::atualizarContadores);

        // Associa o adapter ao ListView
        listViewTarefas.setAdapter(adapter);

        // Atualiza os contadores de tarefas (total/concluídas/pendentes)
        atualizarContadores();
    }

    // ================================
    // Atualiza contadores no topo da tela
    // ================================
    private void atualizarContadores() {
        int total = listaTarefas != null ? listaTarefas.size() : 0;
        int concluidas = 0;

        if (listaTarefas != null) {
            for (Tarefa t : listaTarefas) {
                if (t.isConcluido()) concluidas++; // conta tarefas concluídas
            }
        }

        int pendentes = Math.max(0, total - concluidas); // calcula tarefas pendentes

        // CORREÇÃO: Define APENAS O NÚMERO nas TextViews
        txtTotal.setText(String.valueOf(total));
        txtConcluidas.setText(String.valueOf(concluidas));
        txtPendentes.setText(String.valueOf(pendentes));
    }

    // ================================
    // Abre a Activity de formulário (edição ou criação)
    // ================================
    private void abrirFormulario(Tarefa tarefa) {
        Intent intent = new Intent(this, FormTarefaActivity.class);

        if (tarefa != null) {
            // Se for edição, passa a tarefa para o formulário
            intent.putExtra("tarefa", tarefa);
        }

        startActivity(intent);
    }

    // ================================
    // Confirma exclusão de uma tarefa individual
    // ================================
    private void confirmarExclusao(Tarefa tarefa) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir tarefa")
                .setMessage("Deseja realmente excluir esta tarefa?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    dao.deletar(tarefa.getId()); // remove do banco
                    carregarLista();             // atualiza lista
                    Toast.makeText(this, "Tarefa excluída!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null) // cancelar
                .show();
    }

    // ================================
    // 🔥 NOVO MÉTODO — Exclui todas as tarefas concluídas
    // ================================
    private void excluirTarefasConcluidas() {
        dao.excluirConcluidas(); // chama método no DAO (remover todas concluídas)
        carregarLista();          // atualiza a lista após exclusão
        Toast.makeText(this, "Tarefas concluídas excluídas!", Toast.LENGTH_SHORT).show();
    }
}
