package com.github.mwedgwood.repository;

import com.github.mwedgwood.model.tree.Tree;

public interface TreeRepository extends Repository<Tree> {

    Tree findEntireTree(Integer rootId);

    Tree findByIdForDepth(Integer id, Integer depth);
}
