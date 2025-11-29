import dao.ReservaDAO;
import model.Reserva;
import model.Espaco;
import exception.ReservaSobrepostaException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

public class ReservaService {
    private ReservaDAO reservaDAO = new ReservaDAO();

    public void criarReserva(Espaco espaco, LocalDateTime inicio, LocalDateTime fim) throws ReservaSobrepostaException {
        // validação: início deve ser antes do fim (início == fim é inválido)
        if (!inicio.isBefore(fim)) {
            throw new ReservaSobrepostaException("Data inválida.");
        }

        List<Reserva> reservas = reservaDAO.listarPorEspaco(espaco.getId());
        for (Reserva r : reservas) {
            // verifica sobreposição (intervalos abertos/fechados conforme sua regra)
            if (r.getDataFim().isAfter(inicio) && r.getDataInicio().isBefore(fim)) {
                throw new ReservaSobrepostaException("Reserva sobreposta.");
            }
        }

        // calcula duração em minutos e converte para horas (arredondando para cima p/ cobrar frações)
        long minutos = Duration.between(inicio, fim).toMinutes();
        int horas = (int) ((minutos + 59) / 60); // arredonda para cima
        if (horas <= 0) horas = 1; // se desejar garantir pelo menos 1 hora (ajuste conforme regra)

        double valor = espaco.calcularCustoReserva(horas);
        Reserva reserva = new Reserva(espaco, inicio, fim, valor);
        reservaDAO.salvar(reserva);
        espaco.setDisponivel(false);
    }

    public void cancelarReserva(int idReserva, LocalDateTime agora) {
        Reserva reserva = reservaDAO.buscarPorId(idReserva);
        if (reserva == null) {
            // opcional: lançar exceção ou apenas retornar
            return;
        }

        long horasAntes = Duration.between(agora, reserva.getDataInicio()).toHours();
        if (horasAntes < 24) {
            reserva.setValor(reserva.getValor() * 1.2); // Taxa 20%
        }

        reservaDAO.deletar(idReserva);
        reserva.getEspaco().setDisponivel(true);
    }
}