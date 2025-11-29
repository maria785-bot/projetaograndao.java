package model; // ajuste o package conforme seu projeto

public class SalaDeReuniao extends Espaco {
    private static final double TAXA_PROJETOR = 15.0;

    public SalaDeReuniao(int id, String nome, int capacidade, double precoPorHora) {
        super(id, nome, capacidade, precoPorHora);
    }

    @Override
    public double calcularCustoReserva(int horas) {
        // comportamento padrão sem extras
        return calcularCustoReserva(horas, false);
    }

    // sobrecarga que permite especificar uso de projetor (boolean simples)
    public double calcularCustoReserva(int horas, boolean usarProjetor) {
        double custo = horas * getPrecoPorHora();
        if (usarProjetor) {
            custo += TAXA_PROJETOR;
        }
        return custo;
    }
}