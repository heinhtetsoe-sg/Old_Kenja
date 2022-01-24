package beans;

public class AttendCheckListLine {

	private String lesson;
	private String district;
	private int noumber;
	private String schoolName;
	private String position;
	private String name;
	private String phonetic;
	private String gender;
	private String comment;

	public AttendCheckListLine(String lesson
			, String district, int noumber
			, String schoolName, String position
			, String name, String phonetic
			, String gender, String comment) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.lesson = lesson;
		this.district = district;
		this.noumber = noumber;
		this.schoolName = schoolName;
		this.position = position;
		this.name = name;
		this.phonetic = phonetic;
		this.gender = gender;
		this.comment = comment;
	}

	public String getLesson() {
		return lesson;
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

	public String getPhonetic() {
		return phonetic;
	}

	public String getGender() {
		return gender;
	}

	public String getComment() {
		return comment;
	}

}
