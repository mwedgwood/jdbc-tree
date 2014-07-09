package com.github.mwedgwood.repository;

import com.github.javafaker.Faker;
import com.github.mwedgwood.model.tree.Node;
import com.github.mwedgwood.model.tree.Tree;
import com.github.mwedgwood.service.PersistenceServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.util.Locale;

public class JdbcTreeRepositoryBenchmark {

    private final DBI dbi = PersistenceServiceImpl.getInstance().getDbi();
    private final Faker faker = new Faker(Locale.US);

    private JdbcTreeRepositoryImpl repository;
    private Integer rootId;

    @Before
    public void setUp() throws Exception {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                JdbcTreeRepositoryImplTest.reCreateTreeTable(handle);

                Tree tree = makeTree(5, 5);
                repository = new JdbcTreeRepositoryImpl(dbi);

                long start = System.currentTimeMillis();
                repository.save(tree);
                System.out.println("Time to save tree: " + (System.currentTimeMillis() - start) + " ms");

                rootId = tree.getId();
                return null;
            }
        });
    }

    @Test
    public void testFindEntireTree() throws Exception {
        long start = System.currentTimeMillis();
        Tree entireTree = repository.findEntireTree(rootId);
        System.out.println("Time to find tree: " + (System.currentTimeMillis() - start) + " ms");

//        System.out.println(entireTree.prettyPrint());
    }

    private Tree makeTree(int depth, int maxWidth) {
        Tree root = Tree.createRoot("root", null, Node.class);
        makeTree(depth, maxWidth, root);
        return root;
    }

    private Tree makeTree(int maxDepth, int maxWidth, Tree tree) {
        Integer depth = tree.getDepth();
        for (int i = 0; i < maxWidth && depth < maxDepth; i++) {
            tree.addNode(new Node().setName(faker.lastName().toLowerCase() + i));
            makeTree(maxDepth, maxWidth, tree.getChildren().get(i));
        }
        return tree;
    }


}
