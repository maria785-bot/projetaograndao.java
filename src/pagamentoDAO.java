public interface pagamentoDAO {
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Espaco;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

    public class EspacoDAO implements Persistencia<Espaco> {
        private static final String FILE_PATH = "espacos.json";
        private Gson gson = new Gson();

        public void salvar(Espaco espaco) {
            List<Espaco> espacos = listarTodos();
            espacos.add(espaco);
            salvarLista(espacos);
        }

        public List<Espaco> listarTodos() {
            try (FileReader reader = new FileReader(FILE_PATH)) {
                Type listType = new TypeToken<List<Espaco>>(){}.getType();
                return gson.fromJson(reader, listType);
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }

        // Implementei buscarPorId, atualizar, deletar similarmente
        private void salvarLista(List<Espaco> espacos) {
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                gson.toJson(espacos, writer);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao salvar dados.");
            }
        }
    }
}
