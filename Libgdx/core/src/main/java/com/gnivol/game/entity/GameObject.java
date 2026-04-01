package com.gnivol.game.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

/**
 * Wrapper quanh Ashley Entity, dai dien cho mot vat the trong game.
 * Moi GameObject co id, type va chua cac Component (Transform, Bounds, ItemInfo, v.v.)
 */
public class GameObject {

    private final Entity entity;
    private String id;
    private String type;

    public GameObject() {
        this.entity = new Entity();
    }

    public GameObject(String id, String type) {
        this.entity = new Entity();
        this.id = id;
        this.type = type;
    }

    /** Them component vao entity */
    public GameObject addComponent(Component component) {
        entity.add(component);
        return this;
    }

    /** Lay component theo class */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return ComponentMapper.getFor(componentClass).get(entity);
    }

    /** Kiem tra entity co component nay khong */
    public <T extends Component> boolean hasComponent(Class<T> componentClass) {
        return ComponentMapper.getFor(componentClass).has(entity);
    }

    /** Xoa component khoi entity */
    public void removeComponent(Class<? extends Component> componentClass) {
        entity.remove(componentClass);
    }

    /** Tra ve Ashley Entity ben trong (de add vao Engine) */
    public Entity getEntity() {
        return entity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
