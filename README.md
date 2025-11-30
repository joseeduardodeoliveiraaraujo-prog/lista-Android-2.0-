# lista-Android-2.0-
# Lista de Tarefas Android – ToDo v2.0

## 📋 O que é este projeto?

Esse projeto é um aplicativo Android para gerenciar tarefas (to-do list), permitindo ao usuário criar, editar, concluir, excluir e filtrar tarefas. O armazenamento é feito localmente com SQLite, e o app também oferece configurações de exibição, prioridade e ocultação de tarefas concluídas.

---

## 🚀 Funcionalidades Principais

- Criar nova tarefa: título, descrição, data, prioridade (Baixa / Média / Alta) e status concluída ou não.
- Editar tarefa  
- Excluir tarefa individual.  
- Marcar tarefa como concluída.  
- Excluir todas as tarefas concluídas de uma vez.  
- Indicar tarefas vencidas (data passada): tarefa marcada visualmente como atrasada.  
- Filtro de exibição: ocultar tarefas concluídas via configurações.  
- Filtrar por prioridade mínima.  
- Ordenação da lista de tarefas por vários critérios: inserção, título, data ou prioridade.  
- Contadores visíveis na tela principal: total de tarefas, concluídas e pendentes.  
- Interface simples com ListView + Adapter personalizado para exibir corretamente cada tarefa com cores, status e ícones.

---

## 🛠️ Tecnologia e Estrutura do Projeto

- Linguagem: Java — padrão Android clássico.  
- Persistência: SQLite — utilizando `SQLiteOpenHelper` (classe DBHelper).  
- Armazenamento de preferências: SharedPreferences via classe utilitária `Preferencias`.  
- UI: Activities Android + ListView com Adapter customizado.  
- Organização do código em pacotes: `model`, `database`, `ui`, `adapter`.  

---

## 📁 Estrutura de Diretórios 

app/src/main/java/com/example/projeto2
├── model/          → classe Tarefa
├── database/       → DBHelper (banco), TarefaDAO (CRUD)
├── ui/             → Activities:
│   ├── MainActivity
│   ├── FormTarefaActivity
│   └── ConfiguracoesActivity
└── adapter/        → TarefaAdapter (ListView personalizado)



