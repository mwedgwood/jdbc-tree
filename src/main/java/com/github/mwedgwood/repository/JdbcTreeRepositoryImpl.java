package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTreeRepositoryImpl implements TreeRepository {

    private static final String BASE_RECURSIVE_QUERY = "" +
            "  WITH RECURSIVE children AS (\n" +
            "    SELECT t.id, t.description, t.name, t.parent_id, t.children_order, 1 AS depth\n" +
            "    FROM tree t\n" +
            "    WHERE t.id = :id\n" +
            "  UNION ALL\n" +
            "    SELECT a.id, a.description, a.name, a.parent_id, a.children_order, depth+1\n" +
            "    FROM tree a\n" +
            "    JOIN children b ON (a.parent_id = b.id)\n" +
            ")\n";

    private final DBI dbi;

    public JdbcTreeRepositoryImpl(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public Tree findEntireTree(Integer rootId) {
        String sql = BASE_RECURSIVE_QUERY +
                "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                "FROM children t\n";

        Handle handle = dbi.open();

        List<Node> nodes = handle.createQuery(sql)
                .bind("id", rootId)
                .map(new NodeMapper())
                .list();

        handle.close();

        return Tree.fromList(nodes);
    }

    @Override
    public Tree findByIdForDepth(Integer id, Integer depth) {
        String sql = BASE_RECURSIVE_QUERY +
                "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                "FROM children t\n" +
                "WHERE depth <= :depth";

        Handle handle = dbi.open();

        List<Node> nodes = handle.createQuery(sql)
                .bind("id", id)
                .bind("depth", depth)
                .map(new NodeMapper())
                .list();

        handle.close();

        return Tree.fromList(nodes);
    }

    @Override
    public Tree findById(Integer id) {
        String sql = "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                "FROM tree t\n" +
                "WHERE t.id = :id";

        Handle handle = dbi.open();

        List<Node> nodes = handle.createQuery(sql)
                .bind("id", id)
                .map(new NodeMapper())
                .list();

        handle.close();

        return Tree.fromList(nodes);
    }

    @Override
    public RepositoryResult<Tree> findAll() {
        throw new UnsupportedOperationException("Method not supported for tree data structures");
    }

    @Override
    public void save(Tree entity) {
        Handle handle = dbi.open();
        save(entity, handle);
        handle.close();
    }

    void save(Tree entity, Handle handle) {
        Node node = entity.getNode();
        handle.execute("insert into tree (name, description, parent_id, children_order) values (?, ?, ?, ?)",
                node.getName(), node.getDescription(), node.getParentId(), node.getOrder());

        for (Tree child : entity.getChildren()) {
            save(child, handle);
        }
    }

    @Override
    public void delete(Tree entity) {
        Handle handle = dbi.open();
        handle.execute("delete from tree where id = ?", entity.getNode().getId());
        handle.close();
    }

    @Override
    public void update(Tree entity) {
        Handle handle = dbi.open();

        Node node = entity.getNode();
        handle.execute("update tree set name = ?, description = ?, parent_id = ?, children_order = ?",
                node.getName(), node.getDescription(), node.getParentId(), node.getOrder());

        handle.close();
    }

    public static class NodeMapper extends TypedMapper<Node> {

        @Override
        protected Node extractByName(ResultSet r, String name) throws SQLException {
            return new Node().setId(r.getInt("id"))
                    .setName(r.getString("name"))
                    .setDescription(r.getString("description"))
                    .setParentId(r.getInt("parent_id"))
                    .setOrder(r.getInt("children_order"));
        }

        @Override
        protected Node extractByIndex(ResultSet r, int index) throws SQLException {
            return new Node().setId(r.getInt(1))
                    .setName(r.getString(2))
                    .setDescription(r.getString(3))
                    .setParentId(r.getInt(4))
                    .setOrder(r.getInt(5));
        }
    }
}
