package contracts;

import domain.User;
import java.util.List;

public interface IUserRepository {
    void save(User user);
    void remove(int id);
    User getById(int id);
    List<User> getAll();
}
