package beans;

public class CorseAttendDirectorLine {

	private String district;
	private int noumber;
	private String schoolName;
	private String position;
	private String name;
	private String comment;

	public CorseAttendDirectorLine(String district, int noumber
			, String schoolName, String position
			, String name, String comment) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.district = district;
		this.noumber = noumber;
		this.schoolName = schoolName;
		this.position = position;
		this.name = name;
		this.comment = comment;
	}

	public String getDistrict() {
		return district;
	}

	public int getNoumber() {
		return noumber;
	}

	public String getSchoolName() {
		return schoolName;
	}

	public String getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

}
