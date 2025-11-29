import view.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        EspacoView espacoView = new EspacoView();
        ReservaView reservaView = new ReservaView();
        // Menu loop
        while (true) {
            System.out.println("1. Cadastrar Espaço\n2. Fazer Reserva\n...");
            int op = scanner.nextInt();
            switch (op) {
                case 1: espacoView.cadastrar(); break;
            }
        }
    }
}
