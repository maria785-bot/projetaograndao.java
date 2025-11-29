import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Espaco;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class EspacoDAO implements Persistencia<Espaco> {
    private static final String FILE_PATH = "espacos.json";
    private final Gson gson = new Gson();

    @Override
    public synchronized void salvar(Espaco espaco) {
        List<Espaco> espacos = listarTodos();

        // Se precisar garantir id único, atribui um id auto-increment simples quando id <= 0
        try {
            if (espaco.getId() <= 0) {
                int nextId = espacos.stream()
                        .mapToInt(Espaco::getId)
                        .max()
                        .orElse(0) + 1;
                // supondo que exista setId; se não existir, remova essa parte
                espaco.setId(nextId);
            }
        } catch (Exception ignored) {
            // se Espaco não possuir getId/setId compatível, ignora essa lógica
        }

        espacos.add(espaco);
        salvarListaAtomicamente(espacos);
    }

    @Override
    public synchronized List<Espaco> listarTodos() {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Espaco>>() {}.getType();
            List<Espaco> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo de espaços: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Espaco buscarPorId(int id) {
        for (Espaco e : listarTodos()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    @Override
    public synchronized void atualizar(Espaco espaco) {
        List<Espaco> espacos = listarTodos();
        boolean atualizado = false;
        for (int i = 0; i < espacos.size(); i++) {
            if (espacos.get(i).getId() == espaco.getId()) {
                espacos.set(i, espaco);
                atualizado = true;
                break;
            }
        }
        if (!atualizado) {
            espacos.add(espaco);
        }
        salvarListaAtomicamente(espacos);
    }

    @Override
    public synchronized void deletar(int id) {
        List<Espaco> espacos = listarTodos();
        List<Espaco> filtrado = espacos.stream()
                .filter(e -> e.getId() != id)
                .collect(Collectors.toList());
        salvarListaAtomicamente(filtrado);
    }

    // Escrita atômica: grava em arquivo temporário e move para o destino
    private void salvarListaAtomicamente(List<Espaco> espacos) {
        Path target = Paths.get(FILE_PATH);
        try {
            // garante diretório pai
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // cria temp file na mesma pasta (melhora chance de move atômico)
            Path tempFile = Files.createTempFile(parent, "espacos", ".json.tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                gson.toJson(espacos, writer);
            }

            // tenta mover de forma atômica, se não suportado, substitui
            try {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar dados: " + e.getMessage(), e);
        }
    }
}