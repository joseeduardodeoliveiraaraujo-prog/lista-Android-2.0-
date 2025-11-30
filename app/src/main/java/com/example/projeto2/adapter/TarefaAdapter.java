package com.example.projeto2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.graphics.Typeface;
import android.widget.ImageView;

import com.example.projeto2.R;
import com.example.projeto2.database.TarefaDAO;
import com.example.projeto2.model.Tarefa;
import com.example.projeto2.utils.Preferencias;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * ================================
 * TarefaAdapter
 * ================================
 *
 * Adapter customizado para ListView de tarefas.
 * Responsável por exibir cada item com:
 *  - Título
 *  - Data
 *  - CheckBox de concluído
 *  - Ícone de atrasada
 *  - Cores conforme prioridade
 *  - Ocultar tarefas concluídas
 *
 * Contém listeners para:
 *  - Clique longo → opções Editar/Excluir
 *  - Alteração de status (concluído) → atualizar contadores
 */
public class TarefaAdapter extends ArrayAdapter<Tarefa> {

    // ================================
    // Objetos auxiliares
    // ================================
    private LayoutInflater inflater;    // Para inflar o layout dos itens
    private TarefaDAO dao;              // Acesso ao banco para atualizar tarefas
    private Preferencias prefs;         // Preferências do usuário (SharedPreferences)

    // LISTA ORIGINAL → todas as tarefas, sem filtro
    private List<Tarefa> listaOriginal;

    // LISTA FILTRADA → tarefas exibidas (pode ocultar concluídas)
    private List<Tarefa> listaFiltrada;

    // ================================
    // Listener para clique longo
    // ================================
    public interface OnTarefaLongClickListener {
        void onTarefaLongClick(Tarefa tarefa);
    }

    private OnTarefaLongClickListener longClickListener;

    public void setOnTarefaLongClickListener(OnTarefaLongClickListener listener) {
        this.longClickListener = listener;
    }

    // ================================
    // Listener para atualizar contador em tempo real
    // ================================
    public interface OnStatusChangeListener {
        void onStatusChanged();
    }

    private OnStatusChangeListener statusListener;

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusListener = listener;
    }

    // ================================
    // Construtor do Adapter
    // ================================
    public TarefaAdapter(Context context, List<Tarefa> lista) {
        super(context, 0, lista);

        inflater = LayoutInflater.from(context); // inflador de layout
        dao = new TarefaDAO(context);            // DAO para atualizar tarefas
        prefs = new Preferencias(context);       // preferências

        // Inicializa listas internas
        listaOriginal = new ArrayList<>(lista);
        listaFiltrada = new ArrayList<>(lista);
    }

    // ================================
    // ViewHolder → cache de views para performance
    // ================================
    private static class ViewHolder {
        TextView txtTitulo;
        TextView txtData;
        CheckBox chkConcluida;
        ImageView imgAtrasada;

    }

    // ================================
    // Adapter Overrides
    // ================================
    @Override
    public int getCount() {
        // Retorna tamanho da lista filtrada (itens exibidos)
        return listaFiltrada.size();
    }

    @Override
    public Tarefa getItem(int position) {
        // Retorna tarefa da posição na lista filtrada
        return listaFiltrada.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            // Inflar layout do item
            convertView = inflater.inflate(R.layout.item_tarefa, parent, false);

            // Inicializar ViewHolder
            holder = new ViewHolder();
            holder.txtTitulo = convertView.findViewById(R.id.txtTitulo);
            holder.txtData = convertView.findViewById(R.id.txtData);
            holder.chkConcluida = convertView.findViewById(R.id.chkConcluida);
            holder.imgAtrasada = convertView.findViewById(R.id.imgAtrasada);

            convertView.setTag(holder);
        } else {
            // Recuperar holder existente
            holder = (ViewHolder) convertView.getTag();
        }

        Tarefa tarefa = getItem(position);
        if (tarefa == null) return convertView;

        // ================================
        // Preenche os dados básicos
        // ================================
        holder.txtTitulo.setText(tarefa.getTitulo());
        holder.txtData.setText(tarefa.getData());

        // Remove listener anterior para evitar bug ao reciclar view
        holder.chkConcluida.setOnCheckedChangeListener(null);
        holder.chkConcluida.setChecked(tarefa.isConcluido());

        // ================================
        // TAREFA NÃO CONCLUÍDA
        // ================================
        if (!tarefa.isConcluido()) {

            aplicarCorPrioridade(holder, tarefa.getPrioridade()); // aplica cor conforme prioridade
            holder.txtTitulo.setAlpha(1f);
            holder.txtData.setAlpha(1f);

            if (isVencida(tarefa.getData())) {
                // Ícone visível se tarefa atrasada
                holder.imgAtrasada.setVisibility(View.VISIBLE);
                holder.imgAtrasada.setAlpha(1f);

                // Data em negrito e sublinhada
                holder.txtData.setTypeface(Typeface.DEFAULT_BOLD);
                holder.txtData.getPaint().setUnderlineText(true);

            } else {
                holder.imgAtrasada.setVisibility(View.GONE);
                holder.txtData.setTypeface(Typeface.DEFAULT);
                holder.txtData.getPaint().setUnderlineText(false);
            }
        }
        // ================================
        // TAREFA CONCLUÍDA
        // ================================
        else {
            holder.txtTitulo.setAlpha(0.4f);
            holder.txtData.setAlpha(0.4f);
            holder.txtTitulo.setTextColor(Color.GRAY);
            holder.txtData.setTextColor(Color.GRAY);

            // Ícone some mesmo se vencida
            holder.imgAtrasada.setVisibility(View.GONE);

            if (isVencida(tarefa.getData())) {
                holder.txtData.setTypeface(Typeface.DEFAULT_BOLD);
                holder.txtData.getPaint().setUnderlineText(true);
            } else {
                holder.txtData.setTypeface(Typeface.DEFAULT);
                holder.txtData.getPaint().setUnderlineText(false);
            }
        }

        // ================================
        // CLICK NO CHECKBOX
        // ================================
        holder.chkConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Atualiza status da tarefa
            tarefa.setConcluido(isChecked);
            dao.atualizar(tarefa);

            // Aplica filtro se a opção de ocultar concluídas estiver ativa
            aplicarFiltroOcultarConcluidas(prefs.getOcultarConcluidas());

            // Atualiza contadores
            if (statusListener != null) {
                statusListener.onStatusChanged();
            }
        });

        // Clique no item → alterna checkbox
        convertView.setOnClickListener(v ->
                holder.chkConcluida.setChecked(!holder.chkConcluida.isChecked())
        );

        // Clique longo → chama listener externo
        convertView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTarefaLongClick(tarefa);
                return true;
            }
            return false;
        });

        return convertView;
    }

    // ================================
    // FILTRO DE TAREFAS CONCLUÍDAS
    // ================================
    public void aplicarFiltroOcultarConcluidas(boolean ocultar) {

        listaFiltrada.clear();

        if (ocultar) {
            for (Tarefa t : listaOriginal) {
                if (!t.isConcluido()) {
                    listaFiltrada.add(t);
                }
            }
        } else {
            listaFiltrada.addAll(listaOriginal);
        }

        notifyDataSetChanged(); // atualiza ListView
    }

    // ================================
    // APLICA COR CONFORME PRIORIDADE
    // ================================
    private void aplicarCorPrioridade(ViewHolder holder, int prioridade) {
        switch (prioridade) {
            case 1: // baixa
                holder.txtTitulo.setTextColor(Color.parseColor("#4CAF50"));
                holder.txtData.setTextColor(Color.parseColor("#4CAF50"));
                break;
            case 2: // média
                holder.txtTitulo.setTextColor(Color.parseColor("#FFC107"));
                holder.txtData.setTextColor(Color.parseColor("#FFC107"));
                break;
            case 3: // alta
                holder.txtTitulo.setTextColor(Color.parseColor("#E53935"));
                holder.txtData.setTextColor(Color.parseColor("#E53935"));
                break;
            default: // padrão
                holder.txtTitulo.setTextColor(Color.BLACK);
                holder.txtData.setTextColor(Color.DKGRAY);
                break;
        }
    }

    // ================================
    // VERIFICA SE A TAREFA ESTÁ VENCIDA
    // ================================
    private boolean isVencida(String dataStr) {
        if (dataStr == null || dataStr.length() != 10) return false;
        if (dataStr.charAt(2) != '/' || dataStr.charAt(5) != '/') return false;

        try {
            int dia = Integer.parseInt(dataStr.substring(0, 2));
            int mes = Integer.parseInt(dataStr.substring(3, 5));
            int ano = Integer.parseInt(dataStr.substring(6, 10));

            // Data de hoje no formato AAAAMMDD
            Calendar c = Calendar.getInstance();
            int hoje = c.get(Calendar.YEAR) * 10000
                    + (c.get(Calendar.MONTH) + 1) * 100
                    + c.get(Calendar.DAY_OF_MONTH);

            // Data da tarefa no mesmo formato
            int tarefaData = ano * 10000 + mes * 100 + dia;

            return tarefaData < hoje; // true se vencida

        } catch (Exception e) {
            return false;
        }
    }
}
