// kanji=漢字
/*
 * $Id: 7d571b9f65b0f3e5fb242a8cc39dfefebe59c9cd $
 *
 * 作成日: 2009/10/19 14:22:51 - JST
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
 * @version $Id: 7d571b9f65b0f3e5fb242a8cc39dfefebe59c9cd $
 */
public class AftGradCourseDat {

    private static final Log log = LogFactory.getLog("AftGradCourseDat.class");

    public String _statKind;
    public String _senkouKind;
    public String _statCd;
    public String _statName;
    public String _schoolGroup;
    public String _jobtypeLcd;
    public String _jobtypeLname;
    public String _jobtypeMcd;
    public String _jobtypeMname;
    public String _jobtypeScd;
    public String _jobtypeSname;
    public String _schoolSort;
    public String _prefCd;
    public String _cityCd;
    public String _decision;
    public String _planstat;
    public String _introductionDiv;
    // 進学
    public String _statDate1;
    public String _statStime;
    public String _statEtime;
    public String _areaName;
    public String _statDate2;
    public String _contentexam;
    public String _reasonexam;
    public String _thinkexam;
    public CollegeDat _collegeDat;
    // 就職
    public String _jobDate1;
    public String _jobStime;
    public String _jobEtime;
    public String _jobRemark;
    public String _jobContent;
    public String _jobThink;
    public String _jobexDate1;
    public String _jobexStime;
    public String _jobexEtime;
    public String _jobexRemark;
    public String _jobexContent;
    public String _jobexThink;
    public CompanyMst _companyMst;

    /**
     * コンストラクタ。
     */
    public AftGradCourseDat(
            final String statKind,
            final String senkouKind,
            final String statCd,
            final String statName,
            final String schoolGroup,
            final String jobtypeLcd,
            final String jobtypeLname,
            final String jobtypeMcd,
            final String jobtypeMname,
            final String jobtypeScd,
            final String jobtypeSname,
            final String schoolSort,
            final String prefCd,
            final String cityCd,
            final String decision,
            final String planstat,
            final String introductionDiv,
            final String statDate1,
            final String statStime,
            final String statEtime,
            final String areaName,
            final String statDate2,
            final String contentexam,
            final String reasonexam,
            final String thinkexam,
            final String jobDate1,
            final String jobStime,
            final String jobEtime,
            final String jobRemark,
            final String jobContent,
            final String jobThink,
            final String jobexDate1,
            final String jobexStime,
            final String jobexEtime,
            final String jobexRemark,
            final String jobexContent,
            final String jobexThink
    ) {
        _statKind = statKind;
        _senkouKind = senkouKind;
        _statCd = statCd;
        _statName = statName;
        _schoolGroup = schoolGroup;
        _jobtypeLcd = jobtypeLcd;
        _jobtypeLname = jobtypeLname;
        _jobtypeMcd = jobtypeMcd;
        _jobtypeMname = jobtypeMname;
        _jobtypeScd = jobtypeScd;
        _jobtypeSname = jobtypeSname;
        _schoolSort = schoolSort;
        _prefCd = prefCd;
        _cityCd = cityCd;
        _decision = decision;
        _planstat = planstat;
        _introductionDiv = introductionDiv;
        // 進学
        _statDate1 = statDate1;
        _statStime = statStime;
        _statEtime = statEtime;
        _areaName = areaName;
        _statDate2 = statDate2;
        _contentexam = contentexam;
        _reasonexam = reasonexam;
        _thinkexam = thinkexam;
        // 就職
        _jobDate1 = jobDate1;
        _jobStime = jobStime;
        _jobEtime = jobEtime;
        _jobRemark = jobRemark;
        _jobContent = jobContent;
        _jobThink = jobThink;
        _jobexDate1 = jobexDate1;
        _jobexStime = jobexStime;
        _jobexEtime = jobexEtime;
        _jobexRemark = jobexRemark;
        _jobexContent = jobexContent;
        _jobexThink = jobexThink;
    }

    public void setCollegeDat(
            final String schoolCd,
            final String schoolName,
            final String facultyCd,
            final String facultyName,
            final String departmentCd,
            final String departmentName,
            final String schoolTelno
    ) {
        _collegeDat = new CollegeDat(
                schoolCd,
                schoolName,
                facultyCd,
                facultyName,
                departmentCd,
                departmentName,
                schoolTelno
                );
    }

    public void setCompanyMst(
            final String companyCd,
            final String companyName,
            final String shushokuAddr,
            final String shihonkin,
            final String soninzu,
            final String toninzu,
            final String industryLcd,
            final String industryLname,
            final String industryMcd,
            final String industryMname,
            final String companySort,
            final String targetSex,
            final String zipcd,
            final String addr1,
            final String addr2,
            final String telno,
            final String remark
    ) {
        _companyMst = new CompanyMst(
                companyCd,
                companyName,
                shushokuAddr,
                shihonkin,
                soninzu,
                toninzu,
                industryLcd,
                industryLname,
                industryMcd,
                industryMname,
                companySort,
                targetSex,
                zipcd,
                addr1,
                addr2,
                telno,
                remark
                );
    }
}

// eof
