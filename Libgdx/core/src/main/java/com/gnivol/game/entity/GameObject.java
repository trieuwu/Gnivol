package com.gnivol.game.entity;

import com.badlogic.gdx.math.Vector2;
import com.gnivol.game.component.Component;
import java.util.ArrayList;
import java.util.List;

public class GameObject {
    private String name;
    private Vector2 position; // tọa độ x,y của từng object
    private final List<Component> components; // chức năng của object
    private boolean active; // bật tắt object

    public GameObject(String name, float x, float y) {
        this.name = name;
        this.position = new Vector2(x, y);
        this.components = new ArrayList<>();
        this.active = true;
    }

    /** Kiểm tra object có component loại T không */
    public <T extends Component> boolean hasComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) return true;
        }
        return false;
    }


    /** Lấy component theo type */
    public <T extends Component> T getComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) return (T) c;
        }
        return null;
    }

    public void addComponent(Component c) { components.add(c); }

    public void removeComponent(Class<? extends Component> type) {
        components.removeIf(type::isInstance);
    }

    public void update(float dt) {
        if (!active) return;
        for (Component c : components) {
            if (c.isEnabled()) c.update(dt);
        }
    }

    // Getters/Setters
    public String getName() { return name; }
    public Vector2 getPosition() { return position; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<Component> getComponents() { return components; }
}
