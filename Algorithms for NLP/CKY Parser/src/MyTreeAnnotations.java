package edu.berkeley.nlp.assignments.parsing.student;

import edu.berkeley.nlp.ling.Tree;
import edu.berkeley.nlp.ling.Trees;
import edu.berkeley.nlp.util.Filter;

import  java.util.*;

public class MyTreeAnnotations {
    public static Tree<String> annotateTreeLosslessBinarization(Tree<String> unAnnotatedTree) {

        return binarizeTree(unAnnotatedTree,"ROOT");
    }

    private static Tree<String> binarizeTree(Tree<String> tree, String parent) {
        String label = tree.getLabel();
        if (tree.isLeaf()) return new Tree<String>(label);
        if (tree.getChildren().size() == 1) {
            return new Tree<String>(label + "^" + parent, Collections.singletonList(binarizeTree(tree.getChildren().get(0), label)));
        }
        // otherwise, it's a binary-or-more local tree, so decompose it into a sequence of binary and unary trees.

        if(tree.getChildren().size()==2){
            Tree<String> twokids = binarizeTreeHelperForTwo(tree);
            return new Tree<String>(label + "^" + parent,twokids.getChildren());
        }

        String intermediateLabel = "@" + label + "^" + parent + "->";
        //String intermediateLabel = "@" + label;
        Tree<String> intermediateTree = binarizeTreeHelper(tree, 0, intermediateLabel);
        return new Tree<String>(label + "^" + parent, intermediateTree.getChildren());
    }

    private static Tree<String> binarizeTreeHelperForTwo(Tree<String> tree){
        List<Tree<String>> children = new ArrayList<Tree<String>>();

        Tree<String> leftTree = tree.getChildren().get(0);
        Tree<String> rightTree = tree.getChildren().get(1);
        children.add(binarizeTree(leftTree,tree.getLabel()));
        children.add(binarizeTree(rightTree,tree.getLabel()));

        return new Tree<String>(" ", children); //overwritten by last line in binarizeTree() // The top

    }


    private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
                                                   String intermediateLabel) {
        Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
        List<Tree<String>> children = new ArrayList<Tree<String>>();
        children.add(binarizeTree(leftTree, tree.getLabel()));
        if (numChildrenGenerated < tree.getChildren().size() - 1) {
            Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel);
            children.add(rightTree);
        }
        if (numChildrenGenerated == 0) {
            return new Tree<String>(" ", children);  //overwritten by last line in binarizeTree() // The top
        } else if(numChildrenGenerated == 1) {
            return new Tree<String>(intermediateLabel + "_" + tree.getChildren().get(1).getLabel(), children);
        } else {
            return new Tree<String>(intermediateLabel + "_" + tree.getChildren().get(numChildrenGenerated - 2).getLabel() + "_" + tree.getChildren().get(numChildrenGenerated - 1).getLabel(), children);
        }
    }

/*  F1=80.04
    private static Tree<String> binarizeTreeHelper(Tree<String> tree, int numChildrenGenerated,
                                                   String intermediateLabel) {
        Tree<String> leftTree = tree.getChildren().get(numChildrenGenerated);
        List<Tree<String>> children = new ArrayList<Tree<String>>();
        children.add(binarizeTree(leftTree, tree.getLabel()));
        if (numChildrenGenerated < tree.getChildren().size() - 1) {
            Tree<String> rightTree = binarizeTreeHelper(tree, numChildrenGenerated + 1, intermediateLabel);
            children.add(rightTree);
        }
        if (numChildrenGenerated == 0) {
            return new Tree<String>(" ", children);  //overwritten by last line in binarizeTree() // The top
        } else if(numChildrenGenerated == 1) {
            return new Tree<String>(intermediateLabel + "_" + tree.getChildren().get(1).getLabel(), children);
        } else if(numChildrenGenerated == 2){
            return new Tree<String>(intermediateLabel + "_" + tree.getChildren().get(numChildrenGenerated - 2).getLabel() + "_" + tree.getChildren().get(numChildrenGenerated - 1).getLabel(), children);
        } else{
            return new Tree<String>(intermediateLabel + "_" +
                    tree.getChildren().get(numChildrenGenerated - 3).getLabel() + "_"+
                    tree.getChildren().get(numChildrenGenerated - 2).getLabel() + "_" +
                    tree.getChildren().get(numChildrenGenerated - 1).getLabel(), children);
        }
    }

*/
    public static Tree<String> unAnnotateTree(Tree<String> annotatedTree) {
        // Remove intermediate nodes (labels beginning with "@"
        // Remove all material on node labels which follow their base symbol (cuts anything after <,>,^,=,_ or ->)
        // Examples: a node with label @NP->DT_JJ will be spliced out, and a node with label NP^S will be reduced to NP
        Tree<String> debinarizedTree = Trees.spliceNodes(annotatedTree, new Filter<String>()
        {
            public boolean accept(String s) {
                return s.startsWith("@");
            }
        });
        Tree<String> unAnnotatedTree = (new Trees.LabelNormalizer()).transformTree(debinarizedTree);
        return unAnnotatedTree;
    }
}
