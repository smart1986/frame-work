package org.smart.framework.dataconfig.bean;

import org.smart.framework.dataconfig.IConfigBean;
import org.smart.framework.dataconfig.annotation.DataFile;
import org.smart.framework.util.IdentifyKey;

@DataFile(fileName = "items_config")
public class ItemsConfig implements IConfigBean {
	private int itemId;
	private int type;
	private String itemName;
	private String desc;
	private int img;
	private int areaId;
	/**
	 * 星级
	 */
	private int value;
	private int salePrice;
	private int acquireTip;
	private int page;
	private int expValue;
	private int foodPrice;
	private int consumeEnergy;
	private int madeTime;
	private String composeNum;
	private int menuId;

	public ItemsConfig(){}
	public int getItemId(){
		return this.itemId;
	}
	public int getType(){
		return this.type;
	}
	public String getItemName(){
		return this.itemName;
	}
	public String getDesc(){
		return this.desc;
	}
	public int getImg(){
		return this.img;
	}

	public int getAreaId() {
		return areaId;
	}

	public int getValue(){
		return this.value;
	}
	public int getSalePrice(){
		return this.salePrice;
	}
	public int getAcquireTip(){
		return this.acquireTip;
	}
	public int getPage(){
		return this.page;
	}
	public int getExpValue(){
		return this.expValue;
	}
	public int getFoodPrice(){
		return this.foodPrice;
	}
	public int getConsumeEnergy(){
		return this.consumeEnergy;
	}
	public int getMadeTime(){
		return this.madeTime;
	}
	public String getComposeNum(){
		return this.composeNum;
	}
	public int getMenuId(){
		return this.menuId;
	}
	@Override
	public void initialize() {
	}

	@Override
	public IdentifyKey findIdentifyKey() {
		return IdentifyKey.build(this.itemId);
	}
}