package domain;

import java.util.ArrayList;
import java.util.List;

public class Client extends User {
    private List<Outfit> ownedOutfits;

    public Client(int id, String name) {
        super(id, name);
        this.ownedOutfits = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return "Client";
    }

    public List<Outfit> getOwnedOutfits() {
        return ownedOutfits;
    }
}
