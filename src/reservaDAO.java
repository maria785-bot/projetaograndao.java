import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.Reserva;
import model.Espaco;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReservaDAO implements Persistencia<Reserva> {
    private static final String FILE_PATH = "reservas.json";
    private final Gson gson;

    public ReservaDAO() {
        GsonBuilder builder = new GsonBuilder();
        // (De)serializador simples para LocalDateTime usando ISO format
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        builder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                LocalDateTime.parse(json.getAsString(), fmt));
        builder.registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (ldt, type, ctx) ->
                new JsonPrimitive(ldt.format(fmt)));
        this.gson = builder.create();
    }

    @Override
    public synchronized void salvar(Reserva reserva) {
        List<Reserva> reservas = listarTodos();

        // atribui id simples se necessário (supondo id int <=0 indica sem id)
        try {
            if (reserva.getId() <= 0) {
                int nextId = reservas.stream()
                        .mapToInt(Reserva::getId)
                        .max()
                        .orElse(0) + 1;
                reserva.setId(nextId); // remova se sua model não permitir setId
            }
        } catch (Exception ignored) {
            // se Reserva não expõe getId/setId, ignore a lógica de id
        }

        reservas.add(reserva);
        salvarListaAtomicamente(reservas);
    }

    @Override
    public synchronized List<Reserva> listarTodos() {
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Reserva>>() {}.getType();
            List<Reserva> lista = gson.fromJson(reader, listType);
            return lista != null ? lista : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo de reservas: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Reserva buscarPorId(int id) {
        for (Reserva r : listarTodos()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public synchronized List<Reserva> listarPorEspaco(int idEspaco) {
        return listarTodos().stream()
                .filter(r -> {
                    Espaco e = r.getEspaco();
                    if (e == null) return false;
                    try {
                        return e.getId() == idEspaco;
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void atualizar(Reserva reserva) {
        List<Reserva> reservas = listarTodos();
        boolean atualizado = false;
        for (int i = 0; i < reservas.size(); i++) {
            if (reservas.get(i).getId() == reserva.getId()) {
                reservas.set(i, reserva);
                atualizado = true;
                break;
            }
        }
        if (!atualizado) {
            reservas.add(reserva);
        }
        salvarListaAtomicamente(reservas);
    }

    @Override
    public synchronized void deletar(int id) {
        List<Reserva> reservas = listarTodos();
        List<Reserva> filtrado = reservas.stream()
                .filter(r -> r.getId() != id)
                .collect(Collectors.toList());
        salvarListaAtomicamente(filtrado);
    }

    // gravação atômica para reduzir risco de corrupção, se vier ao caso.
    private void salvarListaAtomicamente(List<Reserva> reservas) {
        Path target = Paths.get(FILE_PATH);
        try {
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            Path tempFile = Files.createTempFile(parent, "reservas", ".json.tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                gson.toJson(reservas, writer);
            }

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