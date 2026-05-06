package repositories;

import contracts.IUserRepository;
import domain.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository implements IUserRepository {
    private static UserRepository instance;
    private final Map<Integer, User> users = new HashMap<>();

    private UserRepository() {}

    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public void remove(int id) {
        users.remove(id);
    }

    @Override
    public User getById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}
