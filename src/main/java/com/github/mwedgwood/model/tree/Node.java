package com.github.mwedgwood.model.tree;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonSubTypes({
        @JsonSubTypes.Type(value = ComplexNode.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
public class Node {

    private Integer id;
    private Integer parentId;
    private Integer order;

    private String name;
    private String description;

    public Integer getId() {
        return id;
    }

    public Node setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Node setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Node setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getParentId() {
        return parentId;
    }

    public Node setParentId(Integer parentId) {
        this.parentId = parentId;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public Node setOrder(Integer order) {
        this.order = order;
        return this;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", order=" + order +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
