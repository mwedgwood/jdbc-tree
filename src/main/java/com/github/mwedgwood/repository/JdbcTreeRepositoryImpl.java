package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.TypedMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTreeRepositoryImpl implements TreeRepository {

    private final Handle handle;

    protected JdbcTreeRepositoryImpl(Handle handle) {
        this.handle = handle;
    }

    @Override
    public Tree findEntireTree(Integer rootId) {
        String sql =
                "  WITH RECURSIVE children AS (\n" +
                "    SELECT t.id, t.description, t.name, t.parent_id, t.children_order, 1 AS depth\n" +
                "    FROM tree t\n" +
                "    WHERE t.id = :id\n" +
                "  UNION ALL\n" +
                "    SELECT a.id, a.description, a.name, a.parent_id, a.children_order, depth+1\n" +
                "    FROM tree a\n" +
                "    JOIN children b ON (a.parent_id = b.id)\n" +
                ")\n" +
                "SELECT t.id, t.name, t.description, t.parent_id, t.children_order, 1 AS depth\n" +
                "FROM children t\n" +
                "ORDER BY t.parent_id, t.depth, t.children_order;";

        List<Node> nodes = handle.createQuery(sql)
                .bind("id", rootId)
                .map(new NodeMapper())
                .list();

        handle.close();

        return Tree.fromList(nodes);
    }

    @Override
    public Tree findByIdForDepth(Integer id, Integer depth) {
        return null;
    }

    @Override
    public Tree findById(Integer id) {
        return null;
    }

    @Override
    public RepositoryResult<Tree> findAll() {
        return null;
    }

    @Override
    public void save(Tree entity) {

    }

    @Override
    public void delete(Tree entity) {

    }

    @Override
    public void update(Tree entity) {

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
