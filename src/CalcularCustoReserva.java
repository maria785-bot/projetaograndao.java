package model;

public abstract class Espaco {
    private int id;
    private String nome;
    private int capacidade;
    private boolean disponivel;
    private double precoPorHora;

    public Espaco(int id, String nome, int capacidade, double precoPorHora) {
        this.id = id;
        this.nome = nome;
        this.capacidade = capacidade;
        this.disponivel = true;
        this.precoPorHora = precoPorHora;
    }

    // assinatura que seu serviço espera — subclasses devem sobrescrever
    public abstract double calcularCustoReserva(int horas);

    // sobrecarga de conveniência que aceita opções extras; por default delega
    public double calcularCustoReserva(int horas, boolean... opcoesExtras) {
        return calcularCustoReserva(horas);
    }

    // getters / setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getCapacidade() { return capacidade; }
    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
    public double getPrecoPorHora() { return precoPorHora; }
    public void setPrecoPorHora(double precoPorHora) { this.precoPorHora = precoPorHora; }
}