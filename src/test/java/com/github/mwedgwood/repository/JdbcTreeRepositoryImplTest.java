package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import com.github.mwedgwood.service.PersistenceServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.IntegerMapper;

import static org.junit.Assert.*;

public class JdbcTreeRepositoryImplTest {

    private final DBI dbi = PersistenceServiceImpl.getInstance().getDbi();
    private Integer rootId;
    private Integer childOneId;

    @Before
    public void setUp() throws Exception {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                reCreateTreeTable(handle);

                rootId = insertTree(handle, "root", null, 0);
                childOneId = insertTree(handle, "child1", rootId, 0);
                Integer childTwoId = insertTree(handle, "child2", rootId, 1);
                insertTree(handle, "child1.1", childOneId, 0);
                insertTree(handle, "child2.1", childTwoId, 0);
                return null;
            }
        });
    }

    private Integer insertTree(Handle handle, String root, Integer parentId, Integer value) {
        return handle.createStatement("insert into tree (name, parent_id, children_order) values (:name, :parentId, :childrenOrder)")
                .bind("name", root)
                .bind("parentId", parentId)
                .bind("childrenOrder", value)
                .executeAndReturnGeneratedKeys(IntegerMapper.FIRST).first();
    }


    @Test
    public void testFindEntireTree() throws Exception {
        TreeRepository repository = new JdbcTreeRepositoryImpl(dbi);

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
        TreeRepository repository = new JdbcTreeRepositoryImpl(dbi);

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
        TreeRepository repository = new JdbcTreeRepositoryImpl(dbi);

        Tree tree = repository.findById(childOneId);
        System.out.println(tree.prettyPrint());

        assertNotNull(tree);
        assertEquals("child1", tree.getNode().getName());
        assertEquals(0, tree.getChildren().size());
    }

    @Test
    public void testSave() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        TreeRepository jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Tree treeFromDb = jdbcTreeRepository.findById(newTree.getId());
        assertNotNull(treeFromDb);
        assertEquals("new node", treeFromDb.getNode().getName());
        assertEquals("description", treeFromDb.getNode().getDescription());
        assertEquals(0, treeFromDb.getNode().getParentId().intValue());
        assertEquals(0, treeFromDb.getNode().getOrder().intValue());
    }

    @Test
    public void testDelete() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        TreeRepository jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Tree treeFromDb = jdbcTreeRepository.findById(newTree.getId());
        jdbcTreeRepository.delete(treeFromDb);

        Tree deletedTree = jdbcTreeRepository.findById(newTree.getId());
        assertNull(deletedTree);
    }

    @Test
    public void testUpdate() throws Exception {
        Tree newTree = new Tree(new Node().setName("new node").setDescription("description").setParentId(null).setOrder(0));

        TreeRepository jdbcTreeRepository = new JdbcTreeRepositoryImpl(dbi);
        jdbcTreeRepository.save(newTree);

        Tree treeFromDb = jdbcTreeRepository.findById(newTree.getId());
        treeFromDb.getNode().setName("updated node");
        jdbcTreeRepository.update(treeFromDb);

        treeFromDb = jdbcTreeRepository.findById(newTree.getId());
        assertEquals("updated node", treeFromDb.getNode().getName());
        assertEquals("description", treeFromDb.getNode().getDescription());
        assertEquals(0, treeFromDb.getNode().getParentId().intValue());
        assertEquals(0, treeFromDb.getNode().getOrder().intValue());
    }

    static void reCreateTreeTable(Handle handle) {
        handle.execute("DROP TABLE IF EXISTS tree");

        handle.execute("CREATE TABLE tree ( \n" +
                "\tid serial primary key, \n" +
                "\tdescription character varying(255), \n" +
                "\tname character varying(255) NOT NULL, \n" +
                "\tparent_id integer, \n" +
                "\tchildren_order integer)");
    }

}
