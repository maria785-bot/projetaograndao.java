import java.util.list;
public interface PersistenciaDao<T> {
    void salvar(T entidade);
    T buscarPorId(int id);
    List<T> listarTodos();
    void atualizar(T entidade);
    void deletar(int id);
}
