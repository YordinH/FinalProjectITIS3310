package contracts;

import domain.User;
import java.util.List;

public interface IUserRepository {
    void save(User user);
    User getById(int id);
    List<User> getAll();
}
