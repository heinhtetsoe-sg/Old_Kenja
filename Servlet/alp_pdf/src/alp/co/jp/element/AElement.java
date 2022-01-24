package alp.co.jp.element;

import java.io.Serializable;
import java.util.ArrayList;

public class AElement implements Serializable, Cloneable {

	/**
	 * シリアルバージョン
	 */
	private static final long serialVersionUID = 1L;
	private String name, value;
	private ArrayList<AElement> element_ = new ArrayList<AElement>();

	public AElement clone(){
        try{
            return (AElement)super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e.toString());
        }
    }

	public AElement() {
		// TODO 自動生成されたコンストラクター・スタブ
		this.name = "";
		this.value = "";
	}
	public AElement(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean equalName(String name) {
		return this.name.equals(name);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean eqalValue(String value) {
		return this.value.equals(value);
	}
	public ArrayList<AElement> getElement_() {
		return element_;
	}
}
