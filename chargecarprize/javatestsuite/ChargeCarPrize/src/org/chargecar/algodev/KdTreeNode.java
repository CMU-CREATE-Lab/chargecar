package org.chargecar.algodev;

public class KdTreeNode {
    private final KnnPoint value;
    private final KdTreeNode leftSubtree;
    private final KdTreeNode rightSubtree;
    private KdTreeNode parent;
    private final boolean splitType;
    
    public void setParent(KdTreeNode parent){
	this.parent=parent;
    }
    public KnnPoint getValue() {
        return value;
    }

    public KdTreeNode getLeftSubtree() {
        return leftSubtree;
    }

    public KdTreeNode getRightSubtree() {
        return rightSubtree;
    }

    public KdTreeNode getParent() {
        return parent;
    }
    
    public boolean getSplitType(){
	return splitType;
    }
    
    public double getSplitValue(){
	if(splitType)
	    return value.getFeatures().getLatitude();
	else 
	    return value.getFeatures().getLongitude();
	    
    }
    
    public KdTreeNode(KnnPoint value, KdTreeNode leftSubtree,
	    KdTreeNode rightSubtree, boolean splitType) {
	this.value = value;
	this.leftSubtree = leftSubtree;
	this.rightSubtree = rightSubtree;
	this.splitType = splitType;
    }
}
