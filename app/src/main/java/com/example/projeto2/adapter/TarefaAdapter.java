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

import com.example.projeto2.R;
import com.example.projeto2.database.TarefaDAO;
import com.example.projeto2.model.Tarefa;
import com.example.projeto2.utils.Preferencias;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import android.widget.ImageView;


public class TarefaAdapter extends ArrayAdapter<Tarefa> {

    private LayoutInflater inflater;
    private TarefaDAO dao;
    private Preferencias prefs;

    // LISTA ORIGINAL (para contagem perfeita)
    private List<Tarefa> listaOriginal;

    // LISTA FILTRADA (lista exibida)
    private List<Tarefa> listaFiltrada;

    // Listener para clique longo
    public interface OnTarefaLongClickListener {
        void onTarefaLongClick(Tarefa tarefa);
    }

    private OnTarefaLongClickListener longClickListener;

    public void setOnTarefaLongClickListener(OnTarefaLongClickListener listener) {
        this.longClickListener = listener;
    }

    // Listener para atualizar contador em tempo real
    public interface OnStatusChangeListener {
        void onStatusChanged();
    }

    private OnStatusChangeListener statusListener;

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        this.statusListener = listener;
    }

    public TarefaAdapter(Context context, List<Tarefa> lista) {
        super(context, 0, lista);

        inflater = LayoutInflater.from(context);
        dao = new TarefaDAO(context);
        prefs = new Preferencias(context);

        // listas internas
        listaOriginal = new ArrayList<>(lista);
        listaFiltrada = new ArrayList<>(lista);
    }

    private static class ViewHolder {
        TextView txtTitulo;
        TextView txtData;
        CheckBox chkConcluida;
        ImageView imgAtrasada;
    }

    // ALTERADO → usamos listaFiltrada
    @Override
    public int getCount() {
        return listaFiltrada.size();
    }

    // ALTERADO → usamos listaFiltrada
    @Override
    public Tarefa getItem(int position) {
        return listaFiltrada.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_tarefa, parent, false);

            holder = new ViewHolder();
            holder.txtTitulo = convertView.findViewById(R.id.txtTitulo);
            holder.txtData = convertView.findViewById(R.id.txtData);
            holder.chkConcluida = convertView.findViewById(R.id.chkConcluida);
            holder.imgAtrasada = convertView.findViewById(R.id.imgAtrasada);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Tarefa tarefa = getItem(position);
        if (tarefa == null) return convertView;

        holder.txtTitulo.setText(tarefa.getTitulo());
        holder.txtData.setText(tarefa.getData());

        holder.chkConcluida.setOnCheckedChangeListener(null);
        holder.chkConcluida.setChecked(tarefa.isConcluido());

        // ----------------------------
        //      NÃO CONCLUÍDA
        // ----------------------------
        if (!tarefa.isConcluido()) {

            aplicarCorPrioridade(holder, tarefa.getPrioridade());
            holder.txtTitulo.setAlpha(1f);
            holder.txtData.setAlpha(1f);

            if (isVencida(tarefa.getData())) {
                holder.imgAtrasada.setVisibility(View.VISIBLE);
                holder.imgAtrasada.setAlpha(1f);

                holder.txtData.setTypeface(Typeface.DEFAULT_BOLD);
                holder.txtData.getPaint().setUnderlineText(true);

            } else {
                holder.imgAtrasada.setVisibility(View.GONE);

                holder.txtData.setTypeface(Typeface.DEFAULT);
                holder.txtData.getPaint().setUnderlineText(false);
            }

        }
        // ----------------------------
        //         CONCLUÍDA
        // ----------------------------
        else {
            holder.txtTitulo.setAlpha(0.4f);
            holder.txtData.setAlpha(0.4f);
            holder.txtTitulo.setTextColor(Color.GRAY);
            holder.txtData.setTextColor(Color.GRAY);

            // ícone some mesmo se vencida
            holder.imgAtrasada.setVisibility(View.GONE);

            if (isVencida(tarefa.getData())) {
                holder.txtData.setTypeface(Typeface.DEFAULT_BOLD);
                holder.txtData.getPaint().setUnderlineText(true);
            } else {
                holder.txtData.setTypeface(Typeface.DEFAULT);
                holder.txtData.getPaint().setUnderlineText(false);
            }
        }

        // ----------------------------
        //    CHECKBOX CLICK
        // ----------------------------
        holder.chkConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {

            tarefa.setConcluido(isChecked);
            dao.atualizar(tarefa);

            aplicarFiltroOcultarConcluidas(prefs.getOcultarConcluidas());

            if (statusListener != null) {
                statusListener.onStatusChanged();
            }
        });

        convertView.setOnClickListener(v ->
                holder.chkConcluida.setChecked(!holder.chkConcluida.isChecked())
        );

        convertView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTarefaLongClick(tarefa);
                return true;
            }
            return false;
        });

        return convertView;
    }


    // NOVA FUNÇÃO DE FILTRO
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

        notifyDataSetChanged();
    }


    private void aplicarCorPrioridade(ViewHolder holder, int prioridade) {
        switch (prioridade) {
            case 1:
                holder.txtTitulo.setTextColor(Color.parseColor("#2E7D32"));
                holder.txtData.setTextColor(Color.parseColor("#2E7D32"));
                break;

            case 2:
                holder.txtTitulo.setTextColor(Color.parseColor("#F9A825"));
                holder.txtData.setTextColor(Color.parseColor("#F9A825"));
                break;

            case 3:
                holder.txtTitulo.setTextColor(Color.parseColor("#C62828"));
                holder.txtData.setTextColor(Color.parseColor("#C62828"));
                break;

            default:
                holder.txtTitulo.setTextColor(Color.BLACK);
                holder.txtData.setTextColor(Color.DKGRAY);
                break;
        }
    }

    private boolean isVencida(String dataStr) {
        if (dataStr == null || dataStr.length() != 10) return false;
        if (dataStr.charAt(2) != '/' || dataStr.charAt(5) != '/') return false;

        try {
            int dia = Integer.parseInt(dataStr.substring(0, 2));
            int mes = Integer.parseInt(dataStr.substring(3, 5));
            int ano = Integer.parseInt(dataStr.substring(6, 10));

            Calendar c = Calendar.getInstance();
            int hoje = c.get(Calendar.YEAR) * 10000
                    + (c.get(Calendar.MONTH) + 1) * 100
                    + c.get(Calendar.DAY_OF_MONTH);

            int tarefaData = ano * 10000 + mes * 100 + dia;

            return tarefaData < hoje;

        } catch (Exception e) {
            return false;
        }
    }
}
