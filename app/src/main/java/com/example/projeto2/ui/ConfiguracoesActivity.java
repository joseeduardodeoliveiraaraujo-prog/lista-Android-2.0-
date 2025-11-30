package com.example.projeto2.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projeto2.R;
import com.example.projeto2.utils.Preferencias;

/**
 * ================================
 * ConfiguracoesActivity
 * ================================
 *
 * Esta Activity gerencia as configurações do aplicativo:
 *   - Ocultar tarefas concluídas
 *   - Ordenação da lista de tarefas
 *   - Prioridade mínima a exibir
 *
 * As configurações são salvas usando SharedPreferences via classe Preferencias.
 */
public class ConfiguracoesActivity extends AppCompatActivity {

    // ================================
    // Componentes da interface
    // ================================
    private CheckBox chkOcultarConcluidas;       // Checkbox para ocultar tarefas concluídas
    private Button btnSalvarConfig;              // Botão para salvar as configurações
    private Spinner spinnerOrdenacao;            // Spinner para selecionar tipo de ordenação
    private Spinner spinnerPrioridadeMinima;     // Spinner para selecionar prioridade mínima

    // ================================
    // Classe de preferências (SharedPreferences)
    // ================================
    private Preferencias prefs;                  // Gerencia leitura e gravação de preferências

    // ================================
    // Ciclo de vida — onCreate
    // ================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define qual layout XML será usado por esta Activity
        setContentView(R.layout.activity_configuracoes);

        // Inicializa a classe Preferencias (SharedPreferences)
        prefs = new Preferencias(this);

        // Recupera referências dos componentes de interface
        chkOcultarConcluidas = findViewById(R.id.chkOcultarConcluidas);
        btnSalvarConfig = findViewById(R.id.btnSalvarConfig);
        spinnerOrdenacao = findViewById(R.id.spinnerOrdenacao);
        spinnerPrioridadeMinima = findViewById(R.id.spinnerPrioridadeMinima);

        // Configura os Spinners com suas opções
        configurarSpinnerOrdenacao();
        configurarSpinnerPrioridadeMinima();

        // Carrega valores salvos anteriormente (SharedPreferences)
        carregarPreferencias();

        // Define ação do botão Salvar
        btnSalvarConfig.setOnClickListener(v -> salvar());
    }

    // ================================
    // Carrega os valores salvos no SharedPreferences
    // ================================
    private void carregarPreferencias() {

        // Recupera e aplica estado do checkbox (ocultar tarefas concluídas)
        chkOcultarConcluidas.setChecked(prefs.getOcultarConcluidas());

        // ================================
        // Spinner Ordenação
        // ================================
        // Recupera valor salvo de ordenação (0..3)
        int ordenacao = prefs.getOrdenacao();
        if (ordenacao >= 0 && ordenacao <= 3) {
            spinnerOrdenacao.setSelection(ordenacao); // Define posição do spinner
        } else {
            spinnerOrdenacao.setSelection(0); // Padrão caso valor inválido
        }

        // ================================
        // Spinner Prioridade mínima
        // ================================
        // Recupera valor salvo da prioridade mínima
        int prioridadeMinima = prefs.getPrioridadeMinima();
        int posPrioridade = 0;

        // Converte valor salvo (0,2,3) para posição do spinner (0,1,2)
        if (prioridadeMinima == 2) posPrioridade = 1;
        if (prioridadeMinima == 3) posPrioridade = 2;

        // Aplica posição no spinner
        spinnerPrioridadeMinima.setSelection(posPrioridade);
    }

    // ================================
    // Salvar alterações feitas pelo usuário
    // ================================
    private void salvar() {

        // ================================
        // Salva estado do checkbox
        // ================================
        prefs.setOcultarConcluidas(chkOcultarConcluidas.isChecked());

        // ================================
        // Salva ordenação
        // ================================
        int ordenacaoSel = spinnerOrdenacao.getSelectedItemPosition();
        prefs.setOrdenacao(ordenacaoSel); // Valor 0..3 diretamente salvo

        // ================================
        // Salva prioridade mínima
        // ================================
        int posPrioridade = spinnerPrioridadeMinima.getSelectedItemPosition();
        int valorPrioridade = 0;

        // Converte posição do spinner (0,1,2) para valor real (0,2,3)
        if (posPrioridade == 1) valorPrioridade = 2;
        if (posPrioridade == 2) valorPrioridade = 3;

        prefs.setPrioridadeMinima(valorPrioridade);

        // Feedback rápido ao usuário
        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();

        // Fecha esta Activity e retorna para a MainActivity
        finish();
    }

    // ================================
    // Configuração do Spinner de Ordenação
    // ================================
    private void configurarSpinnerOrdenacao() {

        // Opções de ordenação disponíveis
        String[] opcoes = new String[]{
                "Ordem padrão (inserção)",          // Posição 0
                "Título (A–Z)",                     // Posição 1
                "Data (mais antigas primeiro)",     // Posição 2
                "Prioridade (Alta → Baixa)"         // Posição 3
        };

        // Cria adapter para spinner
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
        );

        // Layout para dropdown
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Associa adapter ao spinner
        spinnerOrdenacao.setAdapter(adaptador);
    }

    // ================================
    // Configuração do Spinner de Prioridade mínima
    // ================================
    private void configurarSpinnerPrioridadeMinima() {

        // Opções disponíveis
        String[] opcoes = new String[]{
                "Mostrar todas as prioridades",    // Posição 0
                "Apenas Média e Alta",             // Posição 1
                "Apenas Alta"                      // Posição 2
        };

        // Cria adapter para spinner
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcoes
        );

        // Layout para dropdown
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Associa adapter ao spinner
        spinnerPrioridadeMinima.setAdapter(adaptador);
    }
}
