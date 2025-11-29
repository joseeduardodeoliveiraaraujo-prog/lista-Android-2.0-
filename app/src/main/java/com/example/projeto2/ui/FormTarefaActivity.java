package com.example.projeto2.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.example.projeto2.R;
import com.example.projeto2.database.TarefaDAO;
import com.example.projeto2.model.Tarefa;

/**
 * ================================
 * FormTarefaActivity
 * ================================
 *
 * Esta Activity é responsável por:
 *   - Criar novas tarefas
 *   - Editar tarefas existentes
 *   - Aplicar máscara de data no formato dd/MM/yyyy
 *   - Validar dados do formulário antes de salvar
 *   - Selecionar prioridade e marcar tarefa como concluída
 */
public class FormTarefaActivity extends AppCompatActivity {

    // ================================
    // Objetos auxiliares (DAO e Tarefa)
    // ================================
    private Tarefa tarefaEdicao = null;  // Objeto da tarefa caso seja edição
    private TarefaDAO dao;               // Objeto de acesso ao banco de dados

    // ================================
    // Ciclo de vida: onCreate
    // ================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define qual layout XML esta Activity vai usar
        setContentView(R.layout.activity_form_tarefa);

        // Inicializa DAO para acesso ao banco
        dao = new TarefaDAO(this);

        // ================================
        // Componentes da interface (variáveis locais)
        // ================================
        EditText edtTitulo = findViewById(R.id.edtTitulo);           // Campo de título da tarefa
        EditText edtDescricao = findViewById(R.id.edtDescricao);     // Campo de descrição
        EditText edtData = findViewById(R.id.edtData);               // Campo de data
        Spinner spinnerPrioridade = findViewById(R.id.spinnerPrioridade); // Spinner de prioridade
        CheckBox chkConcluidaForm = findViewById(R.id.chkConcluidaForm); // Checkbox concluída
        Button btnSalvar = findViewById(R.id.btnSalvar);             // Botão salvar

        // ================================
        // Aplica máscara de data (dd/MM/yyyy) no campo de data
        // ================================
        aplicarMascaraData(edtData);

        // ================================
        // Configuração do Spinner de prioridade
        // ================================
        String[] prioridades = {"Baixa", "Média", "Alta"}; // Valores do Spinner
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                prioridades
        );
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridade.setAdapter(adaptador);

        // ================================
        // Preenchimento de campos caso seja edição
        // ================================
        if (getIntent().hasExtra("tarefa")) {
            // Recupera a tarefa passada via Intent
            tarefaEdicao = (Tarefa) getIntent().getSerializableExtra("tarefa");

            // Preenche os campos com os dados da tarefa existente
            edtTitulo.setText(tarefaEdicao.getTitulo());
            edtDescricao.setText(tarefaEdicao.getDescricao());
            edtData.setText(tarefaEdicao.getData());

            // Converte prioridade salva (1,2,3) para posição do Spinner (0,1,2)
            int prioridade = tarefaEdicao.getPrioridade();
            int posicaoSpinner = (prioridade >= 1 && prioridade <= 3) ? prioridade - 1 : 0;
            spinnerPrioridade.setSelection(posicaoSpinner);

            // Define se a tarefa está marcada como concluída
            chkConcluidaForm.setChecked(tarefaEdicao.isConcluido());
        }

        // ================================
        // Clique no botão Salvar
        // ================================
        btnSalvar.setOnClickListener(v -> {

            // 1) Validação do título (não pode estar vazio)
            String titulo = edtTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(this, "O título é obrigatório.", Toast.LENGTH_SHORT).show();
                return; // Interrompe salvamento
            }

            // 2) Recupera os outros campos
            String descricao = edtDescricao.getText().toString().trim();
            String data = edtData.getText().toString().trim();

            // Validação da data usando método isDataValida
            if (!isDataValida(data)) {
                Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                return; // Interrompe salvamento
            }

            // Recupera prioridade do Spinner e converte para valores 1,2,3
            int prioridade = spinnerPrioridade.getSelectedItemPosition() + 1;

            // Recupera se a tarefa está marcada como concluída
            boolean concluida = chkConcluidaForm.isChecked();

            // ================================
            // Inserção ou atualização no banco
            // ================================
            if (tarefaEdicao == null) {
                // Se não houver tarefaEdicao → nova tarefa
                Tarefa nova = new Tarefa(0, titulo, descricao, data, prioridade, concluida);
                dao.inserir(nova); // Salva no banco
                Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show();
            } else {
                // Atualiza a tarefa existente
                tarefaEdicao.setTitulo(titulo);
                tarefaEdicao.setDescricao(descricao);
                tarefaEdicao.setData(data);
                tarefaEdicao.setPrioridade(prioridade);
                tarefaEdicao.setConcluido(concluida);

                dao.atualizar(tarefaEdicao); // Atualiza no banco
                Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();
            }

            // Fecha a Activity e retorna à tela anterior
            finish();
        });
    }

    // ================================
    // Método de validação de data rigorosa (dd/MM/yyyy)
    // ================================
    private boolean isDataValida(String data) {
        if (data == null || data.length() != 10) return false;

        // Cria objeto SimpleDateFormat com formato de data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // Faz validação estrita (não permite datas inválidas como 31/02/2025)

        try {
            sdf.parse(data); // Tenta converter para Date
            return true;     // Sucesso → data válida
        } catch (ParseException e) {
            return false;    // Erro → data inválida
        }
    }

    // ================================
    // Método para aplicar máscara de data no campo
    // ================================
    private void aplicarMascaraData(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating; // Flag para evitar loop infinito de atualização

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Evita recursão ao atualizar o texto
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                // Remove tudo que não seja número
                String clean = s.toString().replaceAll("[^0-9]", "");

                // Limita máximo de 8 dígitos (ddMMyyyy)
                if (clean.length() > 8) clean = clean.substring(0, 8);

                int length = clean.length();
                StringBuilder builder = new StringBuilder();

                // Formata "dd"
                if (length >= 1) {
                    builder.append(clean.substring(0, Math.min(2, length)));
                    if (length > 2) builder.append("/");
                }

                // Formata "MM"
                if (length >= 3) {
                    builder.append(clean.substring(2, Math.min(4, length)));
                    if (length > 4) builder.append("/");
                }

                // Formata "yyyy"
                if (length >= 5) {
                    builder.append(clean.substring(4, Math.min(8, length)));
                }

                // Atualiza campo de texto com a máscara aplicada
                isUpdating = true;
                editText.setText(builder.toString());
                editText.setSelection(editText.getText().length()); // coloca cursor no final
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
