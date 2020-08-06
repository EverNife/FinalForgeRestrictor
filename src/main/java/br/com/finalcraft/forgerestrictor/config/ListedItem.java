package br.com.finalcraft.forgerestrictor.config;

import org.bukkit.Material;

public class ListedItem {
	public Material material;
	public Short data;
	public String world;

	public ListedItem(){}

	public ListedItem(Material material, Short data, String world) {
		this.material = material;
		this.data = data;
		this.world = world;
	}

	public ListedItem(String serialized){
		String[] arr=serialized.split(":");
		if (arr.length<1) {
			throw new IllegalArgumentException();
		}
		
		this.material = (Material) Material.valueOf(arr[0]);
		if (arr.length>1) {
			this.data = (arr[1].equals("null")?null:Short.valueOf(arr[1]));
			if (arr.length>2) {
				this.world = (arr[2].equals("null")?null:arr[2]);
			}
		}
	}

	public String serialize() {
		return material+":"+(data==null&&world==null ? "" : (data==null ? ":*" : ":"+data)+(world==null ? "" : ":"+world));
	}

	public boolean match(Material material, Short data, String world) {
		boolean test = material==this.material && (this.data==null || this.data.equals(data)) && (this.world==null || this.world.equals(world));
		return test;
	}

	public boolean equals(Material material, Short data, String world) {
		boolean test = material==this.material && (this.data==data || (this.data!=null && this.data.equals(data))) && (this.world==world || (this.world!=null && this.world.equals(world)));
		return test;
	}

	@Override
	public String toString() {
		return material+":"+(data==null?"*":data)+(world==null ? "" : " ["+world+"]");
	}
}
