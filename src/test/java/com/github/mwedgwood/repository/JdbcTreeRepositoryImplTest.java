package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Tree;
import com.github.mwedgwood.service.PersistenceServiceImpl;
import junit.framework.TestCase;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

public class JdbcTreeRepositoryImplTest extends TestCase {

    private Handle handle;

    @Override
    public void setUp() throws Exception {
        handle = PersistenceServiceImpl.getInstance().getDbi().open();
        reCreateTreeTable();

        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "root", null, 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child1", findId("root"), 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child1.2", findId("child1"), 0);
    }

    @Override
    public void tearDown() throws Exception {
        handle.close();
        super.tearDown();
    }

    public void testFindEntireTree() throws Exception {
        JdbcTreeRepositoryImpl repository = new JdbcTreeRepositoryImpl(handle);

        Integer rootId = findId("root");
        Tree entireTree = repository.findEntireTree(rootId);

        System.out.println(entireTree.prettyPrint());
    }

    private Integer findId(String name) {
        return handle.createQuery("select id from tree where name = :name")
                .bind("name", name)
                .map(IntegerMapper.FIRST)
                .first();
    }

    private void reCreateTreeTable() {
        handle.execute("DROP TABLE tree IF EXISTS;");

        String sql = "CREATE TABLE tree (\n" +
                "    id serial primary key,\n" +
                "    description character varying(255),\n" +
                "    name character varying(255) NOT NULL,\n" +
                "    parent_id integer,\n" +
                "    children_order integer\n" +
                ");";

        handle.execute(sql);
    }

}
