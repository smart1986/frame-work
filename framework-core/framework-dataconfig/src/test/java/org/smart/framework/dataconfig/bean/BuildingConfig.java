package org.smart.framework.dataconfig.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Strings;
import org.smart.framework.dataconfig.IConfigBean;
import org.smart.framework.dataconfig.annotation.DataFile;
import org.smart.framework.util.IdentifyKey;

@DataFile(fileName = "building_config")
public class BuildingConfig implements IConfigBean {
	private int buildingId;
	private int type;
	private String buidingName;
	private String desc;
	private String img;
	private int areaId;
	private int deskId;
	private String buyGold;
	private int glamour;
	private int floorX;
	private int floorY;

	public BuildingConfig() {
	}

	public int getBuildingId() {
		return this.buildingId;
	}

	public int getType() {
		return this.type;
	}

	public String getBuidingName() {
		return this.buidingName;
	}

	public String getDesc() {
		return this.desc;
	}

	public String getImg() {
		return this.img;
	}

	public int getAreaId() {
		return this.areaId;
	}

	public int getDeskId() {
		return this.deskId;
	}

	public String getBuyGold() {
		return this.buyGold;
	}

	public int getGlamour() {
		return this.glamour;
	}

	public int getFloorX() {
		return this.floorX;
	}

	public int getFloorY() {
		return this.floorY;
	}

	@Override
	public void initialize() {
	}

	@Override
	public IdentifyKey findIdentifyKey() {
		return IdentifyKey.build(this.buildingId);
	}


}