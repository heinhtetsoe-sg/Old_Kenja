// kanji=漢字
/*
 * $Id: 77d4fbef52f24b6cdbc461329bc406e081473f6a $
 *
 * 作成日: 2009/10/19 14:25:20 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 77d4fbef52f24b6cdbc461329bc406e081473f6a $
 */
public class CourseHopeDat {

    private static final Log log = LogFactory.getLog("CourseHopeDat.class");

    public String _courseKind;
    public String _questionnaireCd;
    public String _schoolGroup1;
    public String _facultyGroup1;
    public String _departmentGroup1;
    public String _schoolCd1;
    public String _facultyCd1;
    public String _departmentCd1;
    public String _howtoexam1;
    public String _schoolGroup2;
    public String _facultyGroup2;
    public String _departmentGroup2;
    public String _schoolCd2;
    public String _facultyCd2;
    public String _departmentCd2;
    public String _howtoexam2;
    public String _jobtypeLCd1;
    public String _jobtypeMCd1;
    public String _jobtypeSCd1;
    public String _workArea1;
    public String _introductionDiv1;
    public String _jobtypeLCd2;
    public String _jobtypeMCd2;
    public String _jobtypeSCd2;
    public String _workArea2;
    public String _introductionDiv2;
    public String _remark;
    public String _year;

    public CourseHopeDat(
            final String courseKind,
            final String questionnaireCd,
            final String schoolGroup1,
            final String facultyGroup1,
            final String departmentGroup1,
            final String schoolCd1,
            final String facultyCd1,
            final String departmentCd1,
            final String howtoexam1,
            final String schoolGroup2,
            final String facultyGroup2,
            final String departmentGroup2,
            final String schoolCd2,
            final String facultyCd2,
            final String departmentCd2,
            final String howtoexam2,
            final String jobtypeLCd1,
            final String jobtypeMCd1,
            final String jobtypeSCd1,
            final String workArea1,
            final String introductionDiv1,
            final String jobtypeLCd2,
            final String jobtypeMCd2,
            final String jobtypeSCd2,
            final String workArea2,
            final String introductionDiv2,
            final String remark,
            final String year
    ) {
        _courseKind = courseKind;
        _questionnaireCd = questionnaireCd;
        _schoolGroup1 = schoolGroup1;
        _facultyGroup1 = facultyGroup1;
        _departmentGroup1 = departmentGroup1;
        _schoolCd1 = schoolCd1;
        _facultyCd1 = facultyCd1;
        _departmentCd1 = departmentCd1;
        _howtoexam1 = howtoexam1;
        _schoolGroup2 = schoolGroup2;
        _facultyGroup2 = facultyGroup2;
        _departmentGroup2 = departmentGroup2;
        _schoolCd2 = schoolCd2;
        _facultyCd2 = facultyCd2;
        _departmentCd2 = departmentCd2;
        _howtoexam2 = howtoexam2;
        _jobtypeLCd1 = jobtypeLCd1;
        _jobtypeMCd1 = jobtypeMCd1;
        _jobtypeSCd1 = jobtypeSCd1;
        _workArea1 = workArea1;
        _introductionDiv1 = introductionDiv1;
        _jobtypeLCd2 = jobtypeLCd2;
        _jobtypeMCd2 = jobtypeMCd2;
        _jobtypeSCd2 = jobtypeSCd2;
        _workArea2 = workArea2;
        _introductionDiv2 = introductionDiv2;
        _remark = remark;
        _year = year;
    }
}

// eof
