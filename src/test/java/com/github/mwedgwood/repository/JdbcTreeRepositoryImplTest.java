package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import com.github.mwedgwood.service.PersistenceServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;

import static org.junit.Assert.*;

public class JdbcTreeRepositoryImplTest {

    private final DBI dbi = PersistenceServiceImpl.getInstance().getDbi();
    private Integer rootId;

    @Before
    public void setUp() throws Exception {
        Handle handle = dbi.open();
        reCreateTreeTable(handle);

        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "root", null, 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child1", getId(handle, "root"), 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child2", getId(handle, "root"), 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child1.1", getId(handle, "child1"), 0);
        handle.execute("insert into tree (name, parent_id, children_order) values (?, ?, ?)", "child2.1", getId(handle, "child2"), 0);

        rootId = getId(handle, "root");
        handle.close();
    }

    @Test
    public void testFindEntireTree() throws Exception {
        JdbcTreeRepositoryImpl repository = new JdbcTreeRepositoryImpl(dbi);

        Tree tree = repository.findEntireTree(rootId);
        System.out.println(tree.prettyPrint());

        assertNotNull(tree);
        assertEquals("root", tree.getNode().getName());
        assertEquals(2, tree.getChildren().size());

        Tree childOne = tree.getChildren().get(0);
        assertEquals("child1", childOne.getNode().getName());
        assertEquals(1, childOne.getChildren().size());

        Tree childTwo = tree.getChildren().get(1);
        assertEquals("child2", childTwo.getNode().getName());
        assertEquals(1, childTwo.getChildren().size());
    }

    @Test
    public void testFindByIdForDepth() throws Exception {
        JdbcTreeRepositoryImpl repository = new JdbcTreeRepositoryImpl(dbi);

        Tree tree = repository.findByIdForDepth(rootId, 2);
        System.out.println(tree.prettyPrint());

        assertNotNull(tree);
        assertEquals("root", tree.getNode().getName());
        assertEquals(2, tree.getChildren().size());

        Tree childOne = tree.getChildren().get(0);
        assertEquals("child1", childOne.getNode().getName());
        assertEquals(0, childOne.getChildren().size());

        Tree childTwo = tree.getChildren().get(1);
        assertEquals("child2", childTwo.getNode().getName());
        assertEquals(0, childTwo.getChildren().size());
    }

    @Test
    public void testFindById() throws Exception {
        JdbcTreeRepositoryImpl repository = new JdbcTreeRepositoryImpl(dbi);

        Tree tree = repository.findById(getId("child1"));
        System.out.println(tree.prettyPrint());

        assertNotNull(tree);
        assertEquals("child1", tree.getNode().getName());
        assertEquals(0, tree.getChildren().size());
    }

    @Test
    public void testSave() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        JdbcTreeRepositoryImpl jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Tree treeFromDb = jdbcTreeRepository.findById(getId("new node"));
        assertNotNull(treeFromDb);
        assertEquals("new node", treeFromDb.getNode().getName());
        assertEquals("description", treeFromDb.getNode().getDescription());
        assertEquals(0, treeFromDb.getNode().getParentId().intValue());
        assertEquals(0, treeFromDb.getNode().getOrder().intValue());
    }

    @Test
    public void testDelete() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        JdbcTreeRepositoryImpl jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Integer id = getId("new node");

        Tree treeFromDb = jdbcTreeRepository.findById(id);
        jdbcTreeRepository.delete(treeFromDb);

        Tree deletedTree = jdbcTreeRepository.findById(id);
        assertNull(deletedTree);
    }

    @Test
    public void testUpdate() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        JdbcTreeRepositoryImpl jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Integer id = getId("new node");

        Tree treeFromDb = jdbcTreeRepository.findById(id);
        treeFromDb.getNode().setName("updated node");
        jdbcTreeRepository.update(treeFromDb);

        treeFromDb = jdbcTreeRepository.findById(id);
        assertEquals("updated node", treeFromDb.getNode().getName());
        assertEquals("description", treeFromDb.getNode().getDescription());
        assertEquals(0, treeFromDb.getNode().getParentId().intValue());
        assertEquals(0, treeFromDb.getNode().getOrder().intValue());
    }

    private Integer getId(String name) {
        Handle handle = dbi.open();
        Integer id = getId(handle, name);
        handle.close();
        return id;
    }

    private Integer getId(Handle handle, String name) {
        return handle.createQuery("select id from tree where name = :name")
                .bind("name", name)
                .map(IntegerMapper.FIRST)
                .first();
    }

    private void reCreateTreeTable(Handle handle) {
        handle.execute("DROP TABLE IF EXISTS tree");

        handle.execute("CREATE TABLE tree ( \n" +
                "\tid serial primary key, \n" +
                "\tdescription character varying(255), \n" +
                "\tname character varying(255) NOT NULL, \n" +
                "\tparent_id integer, \n" +
                "\tchildren_order integer)");
    }

}
