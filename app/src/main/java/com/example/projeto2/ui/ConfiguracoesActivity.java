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
 * Activity de configurações gerais do aplicativo.
 *
 * Funções implementadas:
 *   - Carregar preferências salvas (SharedPreferences)
 *   - Alterar a opção "Ocultar tarefas concluídas"
 *   - Salvar as configurações ao clicar no botão
 *
 * Observações:
 *   - O recurso de ordenação foi removido conforme solicitado.
 *   - A Activity retorna para a MainActivity, que faz o recarregamento
 *     da lista automaticamente no onResume().
 */
public class ConfiguracoesActivity extends AppCompatActivity {

    // ================================
    // Componentes da interface
    // ================================
    private CheckBox chkOcultarConcluidas;
    private Button btnSalvarConfig;
	private Spinner spinnerOrdenacao;
	private Spinner spinnerPrioridadeMinima;

    // Classe de preferências (SharedPreferences)
    private Preferencias prefs;

    // ================================
    // Ciclo de vida — onCreate
    // ================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        // Inicializar controlador de preferências
        prefs = new Preferencias(this);

        // Recuperar componentes da interface
        chkOcultarConcluidas = findViewById(R.id.chkOcultarConcluidas);
        btnSalvarConfig = findViewById(R.id.btnSalvarConfig);
		spinnerOrdenacao = findViewById(R.id.spinnerOrdenacao);
		spinnerPrioridadeMinima = findViewById(R.id.spinnerPrioridadeMinima);

		// Popular spinners
		configurarSpinnerOrdenacao();
		configurarSpinnerPrioridadeMinima();

        // Carregar estado salvo anteriormente
        carregarPreferencias();

        // Botão de salvar
        btnSalvarConfig.setOnClickListener(v -> salvar());
    }

    // ================================
    // Carregar os valores salvos no SharedPreferences
    // ================================
    private void carregarPreferencias() {
        // Define se tarefas concluídas serão ocultadas
        chkOcultarConcluidas.setChecked(prefs.getOcultarConcluidas());

		// Ordenação: seleciona a posição conforme valor salvo (0..3)
		int ordenacao = prefs.getOrdenacao();
		if (ordenacao >= 0 && ordenacao <= 3) {
			spinnerOrdenacao.setSelection(ordenacao);
		} else {
			spinnerOrdenacao.setSelection(0);
		}

		// Prioridade mínima: mapeia valor (0,2,3) para posições (0,1,2)
		int prioridadeMinima = prefs.getPrioridadeMinima();
		int posPrioridade = 0;
		if (prioridadeMinima == 2) posPrioridade = 1;
		if (prioridadeMinima == 3) posPrioridade = 2;
		spinnerPrioridadeMinima.setSelection(posPrioridade);
    }

    // ================================
    // Salvar alterações feitas pelo usuário
    // ================================
    private void salvar() {

        // Salva valor da checkbox
        prefs.setOcultarConcluidas(chkOcultarConcluidas.isChecked());

		// Salva ordenação (posição do spinner já é o valor 0..3)
		int ordenacaoSel = spinnerOrdenacao.getSelectedItemPosition();
		prefs.setOrdenacao(ordenacaoSel);

		// Salva prioridade mínima: posições 0,1,2 → valores 0,2,3
		int posPrioridade = spinnerPrioridadeMinima.getSelectedItemPosition();
		int valorPrioridade = 0;
		if (posPrioridade == 1) valorPrioridade = 2;
		if (posPrioridade == 2) valorPrioridade = 3;
		prefs.setPrioridadeMinima(valorPrioridade);

        // Feedback rápido ao usuário
        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();

        // Finaliza esta tela e retorna para a MainActivity
        finish();
    }

	// ================================
	// Adapters dos Spinners
	// ================================
	private void configurarSpinnerOrdenacao() {
		String[] opcoes = new String[]{
				"Ordem padrão (inserção)",
				"Título (A–Z)",
				"Data (mais antigas primeiro)",
				"Prioridade (Alta → Baixa)"
		};
		ArrayAdapter<String> adaptador = new ArrayAdapter<>(
				this,
				android.R.layout.simple_spinner_item,
				opcoes
		);
		adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerOrdenacao.setAdapter(adaptador);
	}

	private void configurarSpinnerPrioridadeMinima() {
		String[] opcoes = new String[]{
				"Mostrar todas as prioridades",
				"Apenas Média e Alta",
				"Apenas Alta"
		};
		ArrayAdapter<String> adaptador = new ArrayAdapter<>(
				this,
				android.R.layout.simple_spinner_item,
				opcoes
		);
		adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerPrioridadeMinima.setAdapter(adaptador);
	}
}