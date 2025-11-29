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
 * Tela de cadastro/edição de tarefa.
 *
 * Responsabilidades:
 *   - Permitir criar uma nova tarefa
 *   - Permitir editar uma tarefa existente (recebida via Intent)
 *   - Aplicar máscara no campo de data (formato dd/MM/yyyy)
 *   - Controlar prioridade via Spinner (Baixa, Média, Alta)
 *   - Permitir marcar a tarefa como concluída já no formulário
 */
public class FormTarefaActivity extends AppCompatActivity {

    // ================================
    // Componentes de interface
    // ================================
    private EditText edtTitulo;
    private EditText edtDescricao;
    private EditText edtData;
    private Spinner spinnerPrioridade;
    private CheckBox chkConcluidaForm;
    private Button btnSalvar;

    // ================================
    // Objetos auxiliares
    // ================================
    private Tarefa tarefaEdicao = null;  // Se não for null → edição
    private TarefaDAO dao;               // Acesso ao banco

    // ================================
    // Ciclo de vida — onCreate
    // ================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tarefa);

        // Inicializa DAO
        dao = new TarefaDAO(this);

        // Referências dos componentes da tela
        edtTitulo        = findViewById(R.id.edtTitulo);
        edtDescricao     = findViewById(R.id.edtDescricao);
        edtData          = findViewById(R.id.edtData);
        spinnerPrioridade = findViewById(R.id.spinnerPrioridade);
        chkConcluidaForm = findViewById(R.id.chkConcluidaForm);
        btnSalvar        = findViewById(R.id.btnSalvar);

        // Aplica máscara de data (dd/MM/yyyy)
        aplicarMascaraData(edtData);

        // Preenche o Spinner de prioridade
        configurarSpinner();

        // Verificar se veio uma tarefa para edição
        if (getIntent().hasExtra("tarefa")) {
            tarefaEdicao = (Tarefa) getIntent().getSerializableExtra("tarefa");
            preencherCamposEdicao();
        }

        // Clique do botão Salvar
        btnSalvar.setOnClickListener(v -> salvar());
    }

    // ================================
    // Configuração do Spinner de prioridade
    //  - Posições:
    //    0 = Baixa
    //    1 = Média
    //    2 = Alta
    // ================================
    private void configurarSpinner() {
        String[] prioridades = {"Baixa", "Média", "Alta"};

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                prioridades
        );
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridade.setAdapter(adaptador);
    }

    // ================================
    // Preenche os campos quando a tela é aberta em modo de edição
    // ================================
    private void preencherCamposEdicao() {
        if (tarefaEdicao == null) return;

        edtTitulo.setText(tarefaEdicao.getTitulo());
        edtDescricao.setText(tarefaEdicao.getDescricao());
        edtData.setText(tarefaEdicao.getData());

        // A prioridade é salva como:
        // 1 = Baixa, 2 = Média, 3 = Alta
        // O Spinner utiliza posições 0, 1, 2.
        int prioridade = tarefaEdicao.getPrioridade();
        int posicaoSpinner = 0; // padrão: Baixa

        if (prioridade >= 1 && prioridade <= 3) {
            posicaoSpinner = prioridade - 1;
        }
        spinnerPrioridade.setSelection(posicaoSpinner);

        chkConcluidaForm.setChecked(tarefaEdicao.isConcluido());
    }

    // ================================
    // Salvar ou atualizar a tarefa
    // ================================
    private void salvar() {

        // 1) Validação do título (obrigatório)
        String titulo = edtTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            Toast.makeText(this, "O título é obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) Demais campos
        String descricao = edtDescricao.getText().toString().trim();
        String data      = edtData.getText().toString().trim();

		// 2.1) Validação da data no formato dd/MM/yyyy.
		// Critérios mínimos:
		// - Tamanho 10
		// - Barras nas posições 2 e 5
		// - Dia entre 1 e 31; Mês entre 1 e 12
		// Observação: validação básica (não verifica meses com 30/31 dias ou ano bissexto).
		if (!isDataValida(data)) {
			Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
			return;
		}

        // Spinner retorna posição 0,1,2 → convertemos para 1,2,3
        int posicaoSpinner = spinnerPrioridade.getSelectedItemPosition();
        int prioridade = posicaoSpinner + 1;

        boolean concluida = chkConcluidaForm.isChecked();

        // 3) Inserção ou atualização
        if (tarefaEdicao == null) {
            // ---------------------------
            // Nova tarefa
            // ---------------------------
            Tarefa nova = new Tarefa(0, titulo, descricao, data, prioridade, concluida);
            dao.inserir(nova);
            Toast.makeText(this, "Tarefa adicionada!", Toast.LENGTH_SHORT).show();

        } else {
            // ---------------------------
            // Atualizar tarefa existente
            // ---------------------------
            tarefaEdicao.setTitulo(titulo);
            tarefaEdicao.setDescricao(descricao);
            tarefaEdicao.setData(data);
            tarefaEdicao.setPrioridade(prioridade);
            tarefaEdicao.setConcluido(concluida);

            dao.atualizar(tarefaEdicao);
            Toast.makeText(this, "Tarefa atualizada!", Toast.LENGTH_SHORT).show();
        }

        // Fecha a tela e retorna para a MainActivity
        finish();
    }

	// ================================
	// Validação simples de data dd/MM/yyyy
	// ================================
    private boolean isDataValida(String data) {
        if (data == null || data.length() != 10) return false;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false); // valida estritamente a data
        try {
            sdf.parse(data); // tenta converter para Date
            return true;     // se não lançar exceção → data válida
        } catch (ParseException e) {
            return false;    // data inválida
        }
    }

    // ================================
    // Aplica máscara de data no formato dd/MM/yyyy
    // enquanto o usuário digita
    // ================================
    private void aplicarMascaraData(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não utilizado
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Evita recursão infinita ao atualizar o texto
                if (isUpdating) {
                    isUpdating = false;
                    return;
                }

                // Remove qualquer coisa que não seja dígito
                String clean = s.toString().replaceAll("[^0-9]", "");

                // Limita a 8 dígitos (ddMMyyyy)
                if (clean.length() > 8) {
                    clean = clean.substring(0, 8);
                }

                int length = clean.length();
                StringBuilder builder = new StringBuilder();

                // Monta "dd"
                if (length >= 1) {
                    builder.append(clean.substring(0, Math.min(2, length)));
                    if (length > 2) builder.append("/");
                }

                // Monta "MM"
                if (length >= 3) {
                    builder.append(clean.substring(2, Math.min(4, length)));
                    if (length > 4) builder.append("/");
                }

                // Monta "yyyy"
                if (length >= 5) {
                    builder.append(clean.substring(4, Math.min(8, length)));
                }

                isUpdating = true;
                editText.setText(builder.toString());
                editText.setSelection(editText.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Não utilizado
            }
        });
    }
}