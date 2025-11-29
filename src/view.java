public interface inter {

import dao.ReservaDAO;
import model.Reserva;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

    public class ConsoleRelatorioView implements RelatorioView {
        private final ReservaDAO reservaDAO;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        public ConsoleRelatorioView(ReservaDAO reservaDAO) {
            this.reservaDAO = Objects.requireNonNull(reservaDAO, "reservaDAO não pode ser nulo");
        }

        @Override
        public List<Reserva> getReservasPorPeriodo(LocalDate inicio, LocalDate fim) {
            if (inicio == null || fim == null) throw new IllegalArgumentException("Datas não podem ser nulas.");
            if (inicio.isAfter(fim)) throw new IllegalArgumentException("Data início deve ser <= data fim.");

            return reservaDAO.listarTodos().stream()
                    .filter(r -> r != null && r.getDataInicio() != null && r.getDataFim() != null)
                    .filter(r -> {
                        LocalDate dInicio = r.getDataInicio().toLocalDate();
                        LocalDate dFim = r.getDataFim().toLocalDate();
                        // intersecta [inicio, fim]
                        return !dFim.isBefore(inicio) && !dInicio.isAfter(fim);
                    })
                    .collect(Collectors.toList());
        }

        @Override
        public void exibirReservasPorPeriodo(LocalDate inicio, LocalDate fim) {
            List<Reserva> reservas = getReservasPorPeriodo(inicio, fim);
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
}
