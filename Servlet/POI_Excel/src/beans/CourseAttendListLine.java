package beans;

public class CourseAttendListLine {

	private String year;
	private String lessonType;
	private String lessonName;
	private int dayCount;
	private String lessonDate;
	private String accredit;

	public CourseAttendListLine(String year, String lessonType
			, String lessonName, int dayCount, String lessonDate
			, String accredit) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.year = year;
		this.lessonType = lessonType;
		this.lessonName = lessonName;
		this.dayCount = dayCount;
		this.lessonDate = lessonDate;
		this.accredit = accredit;
	}

	public String getYear() {
		return year;
	}

	public String getLessonType() {
		return lessonType;
	}

	public String getLessonName() {
		return lessonName;
	}

	public int getDayCount() {
		return dayCount;
	}

	public String getLessonDate() {
		return lessonDate;
	}

	public String getAccredit() {
		return accredit;
	}

}
