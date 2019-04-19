package org.smart.framework.util.rank.cache;

public class PageSegmentable {
	protected int pageSement = 10;
	
	public PageSegmentable() {
	}
	
	
    public PageSegmentable(int pageSement) {
		super();
		this.pageSement = pageSement;
	}

	public int findPageSegment(long rank) {
        if(rank % findPageSegment() == 0){
            return (int)(rank/findPageSegment());
        }
        return  (int)(rank/findPageSegment()) + 1;
    }

    public boolean hasInTop(long rank){
        return rank <= findPageSegment();
    }

    public int findPageSegment(){
        return pageSement;
    }

}
