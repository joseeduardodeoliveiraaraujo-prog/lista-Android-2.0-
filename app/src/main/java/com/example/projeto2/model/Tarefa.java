package com.example.projeto2.model;

/**
 * ================================
 * Classe Tarefa
 * ================================
 *
 * Representa uma tarefa do aplicativo de ToDo.
 * Implementa Serializable para permitir envio via Intent (passagem de objetos entre Activities).
 */
public class Tarefa implements java.io.Serializable {

    // ================================
    // Atributos da classe
    // ================================

    private int id;             // Identificador único da tarefa no banco de dados
    private String titulo;      // Título da tarefa (obrigatório)
    private String descricao;   // Descrição detalhada da tarefa (opcional)
    private String data;        // Data de conclusão ou vencimento no formato dd/MM/yyyy
    private int prioridade;     // Prioridade da tarefa: 1 = Baixa, 2 = Média, 3 = Alta
    private boolean concluido;  // Status da tarefa: true = concluída, false = pendente

    // ================================
    // Construtores
    // ================================

    /**
     * Construtor vazio
     * Útil para inicializar o objeto antes de definir os campos individualmente
     */
    public Tarefa() {
        // Inicializa os atributos com valores padrão
    }

    /**
     * Construtor completo
     * Permite criar uma tarefa já com todos os atributos definidos
     *
     * @param id         Identificador único
     * @param titulo     Título da tarefa
     * @param descricao  Descrição detalhada
     * @param data       Data de conclusão/vencimento
     * @param prioridade Prioridade (1 = Baixa, 2 = Média, 3 = Alta)
     * @param concluido  Status da tarefa (true = concluída)
     */
    public Tarefa(int id, String titulo, String descricao, String data, int prioridade, boolean concluido) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.prioridade = prioridade;
        this.concluido = concluido;
    }

    // ================================
    // Getters e Setters
    // ================================

    /**
     * Retorna o ID da tarefa
     */
    public int getId() {
        return id;
    }

    /**
     * Define o ID da tarefa
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Retorna o título da tarefa
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Define o título da tarefa
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Retorna a descrição da tarefa
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define a descrição da tarefa
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a data da tarefa
     */
    public String getData() {
        return data;
    }

    /**
     * Define a data da tarefa
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Retorna a prioridade da tarefa
     */
    public int getPrioridade() {
        return prioridade;
    }

    /**
     * Define a prioridade da tarefa
     */
    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    /**
     * Retorna se a tarefa foi concluída
     */
    public boolean isConcluido() {
        return concluido;
    }

    /**
     * Define o status de conclusão da tarefa
     */
    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }
}
