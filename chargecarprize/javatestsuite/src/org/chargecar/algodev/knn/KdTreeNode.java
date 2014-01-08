package org.chargecar.algodev.knn;



public class KdTreeNode {
    private final KnnPoint value;
    private KdTreeNode leftSubtree;
    private KdTreeNode rightSubtree;
    private final int splitType;
    
    public KnnPoint getValue() {
        return value;
    }

    public void addChild(KnnPoint point, KdTreeFeatureSet featureSet){
	double pointAxisValue = featureSet.getValue(point.getFeatures(), getSplitType());	
	double nodeAxisValue = featureSet.getValue(value.getFeatures(), getSplitType());
	boolean leftBranch = pointAxisValue < nodeAxisValue;
	if(leftBranch){
	    if(leftSubtree == null) leftSubtree = new KdTreeNode(point, null, null, (splitType+1)%featureSet.getFeatureCount());		    
	    else leftSubtree.addChild(point, featureSet);		
	}
	else{
	    if(rightSubtree == null) rightSubtree = new KdTreeNode(point, null, null, (splitType+1)%featureSet.getFeatureCount());		    
	    else rightSubtree.addChild(point, featureSet);		
	}	
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
    
    public int countNodes(){
	int nodes = 1;
	if(rightSubtree != null) nodes += rightSubtree.countNodes();
	if(leftSubtree != null) nodes += leftSubtree.countNodes();
	return nodes;
	
    }
}
