package org.chargecar.algodev;

public class KdTreeNode {
    private final KnnPoint value;
    private final KdTreeNode leftSubtree;
    private final KdTreeNode rightSubtree;
    private final int splitType;
    
    public KnnPoint getValue() {
        return value;
    }

    public KdTreeNode getLeftSubtree() {
        return leftSubtree;
    }

    public KdTreeNode getRightSubtree() {
        return rightSubtree;
    }

    public int getSplitType(){
	return splitType;
    }
    
    public KdTreeNode(KnnPoint value, KdTreeNode leftSubtree,
	    KdTreeNode rightSubtree, int splitType) {
	this.value = value;
	this.leftSubtree = leftSubtree;
	this.rightSubtree = rightSubtree;
	this.splitType = splitType;
    }
}
