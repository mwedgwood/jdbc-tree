package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.IntegerMapper;
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
    public Tree findEntireTree(final Integer rootId) {
        return dbi.withHandle(new HandleCallback<Tree>() {
            @Override
            public Tree withHandle(Handle handle) throws Exception {
                String sql = BASE_RECURSIVE_QUERY +
                        "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                        "FROM children t\n";

                List<Node> nodes = handle.createQuery(sql)
                        .bind("id", rootId)
                        .map(new NodeMapper())
                        .list();

                return Tree.fromList(nodes);
            }
        });
    }

    @Override
    public Tree findByIdForDepth(final Integer id, final Integer depth) {
        return dbi.withHandle(new HandleCallback<Tree>() {
            @Override
            public Tree withHandle(Handle handle) throws Exception {
                String sql = BASE_RECURSIVE_QUERY +
                        "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                        "FROM children t\n" +
                        "WHERE depth <= :depth";

                List<Node> nodes = handle.createQuery(sql)
                        .bind("id", id)
                        .bind("depth", depth)
                        .map(new NodeMapper())
                        .list();

                return Tree.fromList(nodes);
            }
        });
    }

    @Override
    public Tree findById(final Integer id) {
        return dbi.withHandle(new HandleCallback<Tree>() {
            @Override
            public Tree withHandle(Handle handle) throws Exception {
                String sql = "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                        "FROM tree t\n" +
                        "WHERE t.id = :id";

                List<Node> nodes = handle.createQuery(sql)
                        .bind("id", id)
                        .map(new NodeMapper())
                        .list();

                return Tree.fromList(nodes);
            }
        });
    }

    @Override
    public RepositoryResult<Tree> findAll() {
        throw new UnsupportedOperationException("Method not supported for tree data structures");
    }

    @Override
    public void save(final Tree entity) {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                save(entity, handle);
                return null;
            }
        });
    }

    void save(Tree entity, Handle handle) {
        Node node = entity.getNode();

        Integer parentId = node.getParentId() == null && entity.getParent() != null ?
                entity.getParent().getNode().getId() :
                node.getParentId();

        Integer id = handle.createStatement("insert into tree (name, description, parent_id, children_order) values (:name, :description, :parentId, :childrenOrder)")
                .bind("name", node.getName())
                .bind("description", node.getDescription())
                .bind("parentId", parentId)
                .bind("childrenOrder", node.getOrder())
                .executeAndReturnGeneratedKeys(IntegerMapper.FIRST).first();

        entity.getNode().setId(id);

        for (Tree child : entity.getChildren()) {
            save(child, handle);
        }
    }

    @Override
    public void delete(final Tree entity) {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                handle.createStatement("delete from tree where id = :id")
                        .bind("id", entity.getNode().getId())
                        .execute();
                return null;
            }
        });
    }

    @Override
    public void update(final Tree entity) {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                final Node node = entity.getNode();

                handle.createStatement("update tree set name = :name, description = :description, parent_id = :parentId, children_order = :order")
                        .bind("name", node.getName())
                        .bind("description", node.getDescription())
                        .bind("parentId", node.getParentId())
                        .bind("order", node.getOrder())
                        .execute();

                return null;
            }
        });
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
