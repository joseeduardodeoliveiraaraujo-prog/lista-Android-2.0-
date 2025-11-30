package com.example.projeto2.utils;

import android.content.Context;
import android.content.SharedPreferences;

// SharedPreferences
// - Armazena dados simples
// - usado para configuracoes, preferencias
// - Não é um BD.

public class Preferencias {
    private static final String NOME_ARQUIVO = "configuracoes_app";
    private SharedPreferences prefs;

    public Preferencias(Context context){
        prefs = context.getSharedPreferences(NOME_ARQUIVO,Context.MODE_PRIVATE);
    }
    public void setOcultarConcluidas( boolean valor ){
        prefs.edit().putBoolean("ocultarConcluidas", valor).apply();
    }
    public boolean getOcultarConcluidas(){
        return prefs.getBoolean("ocultarConcluidas", false);
    }

    // Ordenação da lista de tarefas
    // Possíveis valores:
    // 0 = ordem padrão (por id/inserção)
    // 1 = por título (A–Z)
    // 2 = por data (mais antigas primeiro)
    // 3 = por prioridade (da mais alta para a mais baixa)
    public void setOrdenacao(int tipo){
        prefs.edit().putInt("ordenacao", tipo).apply();
    }
    public int getOrdenacao(){
        return prefs.getInt("ordenacao", 0);
    }

    // Filtro de prioridade mínima:
    // 0 = mostrar todas
    // 2 = média em diante (2 e 3)
    // 3 = apenas alta
    public void setPrioridadeMinima(int prioridadeMinima){
        prefs.edit().putInt("prioridadeMinima", prioridadeMinima).apply();
    }
    public int getPrioridadeMinima(){
        return prefs.getInt("prioridadeMinima", 0);
    }


}
