package view;

import dao.ReservaDAO;
import model.Reserva;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RelatorioView {
    private final ReservaDAO reservaDAO;

    public RelatorioView(ReservaDAO reservaDAO) {
        this.reservaDAO = Objects.requireNonNull(reservaDAO, "reservaDAO não pode ser nulo");
    }

    // Método que retorna reservas para o período (inclusive)
    public List<Reserva> getReservasPorPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Datas não podem ser nulas.");
        }
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior ou igual à data final.");
        }

        return reservaDAO.listarTodos().stream()
                .filter(r -> r != null && r.getDataInicio() != null && r.getDataFim() != null)
                .filter(r -> {
                    LocalDate dInicio = r.getDataInicio().toLocalDate();
                    LocalDate dFim = r.getDataFim().toLocalDate();
                    // intervalo inclusivo: dInicio/fim intersecta [inicio, fim]
                    return !dFim.isBefore(inicio) && !dInicio.isAfter(fim);
                })
                .collect(Collectors.toList());
    }

    // Método utilitário para exibir no console o relatório (usa getReservasPorPeriodo)
    public void exibirReservasPorPeriodo(LocalDate inicio, LocalDate fim) {
        List<Reserva> reservas = getReservasPorPeriodo(inicio, fim);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        System.out.println("Reservas no período " + inicio + " a " + fim + ": " + reservas.size());
        for (Reserva r : reservas) {
            String espaco = r.getEspaco() != null ? r.getEspaco().getNome() : "N/A";
            String inicioStr = r.getDataInicio() != null ? r.getDataInicio().format(fmt) : "N/A";
            String fimStr = r.getDataFim() != null ? r.getDataFim().format(fmt) : "N/A";
            System.out.printf("ID:%d | Espaço:%s | Início:%s | Fim:%s | Valor:%.2f%n",
                    r.getId(), espaco, inicioStr, fimStr, r.getValor());
        }
    }
}