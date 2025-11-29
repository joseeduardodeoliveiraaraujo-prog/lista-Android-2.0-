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

public class FormTarefaActivity extends AppCompatActivity {

    // ================================
    // Objetos auxiliares
    // ================================
    private Tarefa tarefaEdicao = null;  // Se não for null → edição
    private TarefaDAO dao;               // Acesso ao banco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tarefa);

        // Inicializa DAO
        dao = new TarefaDAO(this);

        // ================================
        // Componentes de interface (variáveis locais)
        // ================================
        EditText edtTitulo = findViewById(R.id.edtTitulo);
        EditText edtDescricao = findViewById(R.id.edtDescricao);
        EditText edtData = findViewById(R.id.edtData);
        Spinner spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        CheckBox chkConcluidaForm = findViewById(R.id.chkConcluidaForm);
        Button btnSalvar = findViewById(R.id.btnSalvar);

        // Aplica máscara de data
        aplicarMascaraData(edtData);

        // Preenche o Spinner de prioridade
        String[] prioridades = {"Baixa", "Média", "Alta"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                prioridades
        );
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridade.setAdapter(adaptador);

        // Verificar se veio uma tarefa para edição
        if (getIntent().hasExtra("tarefa")) {
            tarefaEdicao = (Tarefa) getIntent().getSerializableExtra("tarefa");

            // Preenche campos em modo edição
            edtTitulo.setText(tarefaEdicao.getTitulo());
            edtDescricao.setText(tarefaEdicao.getDescricao());
            edtData.setText(tarefaEdicao.getData());

            int prioridade = tarefaEdicao.getPrioridade();
            int posicaoSpinner = (prioridade >= 1 && prioridade <= 3) ? prioridade - 1 : 0;
            spinnerPrioridade.setSelection(posicaoSpinner);

            chkConcluidaForm.setChecked(tarefaEdicao.isConcluido());
        }

        // Clique do botão Salvar
        btnSalvar.setOnClickListener(v -> {
            // 1) Validação do título
            String titulo = edtTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                Toast.makeText(this, "O título é obrigatório.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2) Demais campos
            String descricao = edtDescricao.getText().toString().trim();
            String data = edtData.getText().toString().trim();

            if (!isDataValida(data)) {
                Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
                return;
            }

            int prioridade = spinnerPrioridade.getSelectedItemPosition() + 1;
            boolean concluida = chkConcluidaForm.isChecked();

            // 3) Inserção ou atualização
            if (tarefaEdicao == null) {
                Tarefa nova = new Tarefa(0, titulo, descricao, data, prioridade, concluida);
                dao.inserir(nova);
                Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show();
            } else {
                tarefaEdicao.setTitulo(titulo);
                tarefaEdicao.setDescricao(descricao);
                tarefaEdicao.setData(data);
                tarefaEdicao.setPrioridade(prioridade);
                tarefaEdicao.setConcluido(concluida);

                dao.atualizar(tarefaEdicao);
                Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();
            }

            // Fecha a tela
            finish();
        });
    }

    // ================================
    // Validação rigorosa de data dd/MM/yyyy
    // ================================
    private boolean isDataValida(String data) {
        if (data == null || data.length() != 10) return false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // valida estritamente a data
        try {
            sdf.parse(data);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // ================================
    // Aplica máscara de data
    // ================================
    private void aplicarMascaraData(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                String clean = s.toString().replaceAll("[^0-9]", "");
                if (clean.length() > 8) clean = clean.substring(0, 8);

                int length = clean.length();
                StringBuilder builder = new StringBuilder();

                if (length >= 1) {
                    builder.append(clean.substring(0, Math.min(2, length)));
                    if (length > 2) builder.append("/");
                }
                if (length >= 3) {
                    builder.append(clean.substring(2, Math.min(4, length)));
                    if (length > 4) builder.append("/");
                }
                if (length >= 5) {
                    builder.append(clean.substring(4, Math.min(8, length)));
                }

                isUpdating = true;
                editText.setText(builder.toString());
                editText.setSelection(editText.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
