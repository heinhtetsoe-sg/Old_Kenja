// kanji=漢字
/*
 * $Id: 031f6a564682c55c995a476d32e0f8f73d0f7c7a $
 *
 * 作成日: 2009/10/28 22:58:15 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [生徒指導情報システム]
 *
 *                  ＜ＫＮＪＨ１１３Ａ＞ 取得資格別一覧表
 *
 * @author m-yama
 * @version $Id: 031f6a564682c55c995a476d32e0f8f73d0f7c7a $
 */
public class KNJH113A {

    private static final Log log = LogFactory.getLog("KNJH113A.class");

    private static final String FORMID = "KNJH113A.frm";
    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        int page = 0;

        for (int i = 0; i < _param._qualifiedSelected.length; i++) {
            final String qualifiedSelected = _param._qualifiedSelected[i];
            final QualifiedMst qualifiedMst = _param.getQualifiedMst(qualifiedSelected);
            final List printStudents = getPrintStudent(db2, qualifiedSelected);

            setHead(db2, svf, qualifiedMst);

            int count = 0;
            int manCount = 0;
            int womanCount = 0;

            boolean hasQualifiedData = false;
            for (final Iterator itPrint = printStudents.iterator(); itPrint.hasNext();) {
                final Student student = (Student) itPrint.next();

                for (Iterator it = student._qualifiedDats.iterator(); it.hasNext();) {
                    QualifiedDat qualifiedDat = (QualifiedDat) it.next();

                    if (count != 0) {
                        svf.VrEndRecord();
                    }
                    if (count % 50 == 0) {
                        page += 1;
                    }

                    svf.VrsOut("PAGE", String.valueOf(page));
                    svf.VrsOut("HR_CLASS", student._hrName);
                    svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)));
                    svf.VrsOut("NAME", student._name);
                    svf.VrsOut("SEX", student._sexName);
                    final String rankField = KNJ_EditEdit.getMS932ByteLength(qualifiedDat._rank) > 16 ? "3" : KNJ_EditEdit.getMS932ByteLength(qualifiedDat._rank) > 8 ? "2" : "";
                    svf.VrsOut("RANK" + rankField, qualifiedDat._rank);
                    svf.VrsOut("SCORE", qualifiedDat._score);
                    svf.VrsOut("REGDDATE", KNJ_EditDate.h_format_JP(db2, qualifiedDat._regddate));
                    svf.VrsOut("REMARK", qualifiedDat._remark);
                    final String receiptstr = "1".equals(qualifiedDat._certificate) ? "済" : "";
                    svf.VrsOut("RECEIPT", receiptstr);

                    log.debug(" name = " + student._name);
                    count += 1;
                    if ("1".equals(student._sex)) {
                        manCount ++;
                    } else if ("2".equals(student._sex)) {
                        womanCount ++;
                    }
                    hasQualifiedData = true;
                }
            }
            if (hasQualifiedData) {
                _hasData = true;
                svf.VrsOut("TOTAL", "計" + count + "名（男" + manCount + "名 女" + womanCount + "名）");
                svf.VrEndRecord();
            }
        }
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf, final QualifiedMst qualifiedMst) {
        svf.VrSetForm(FORMID, 4);
        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrlDate));

        if (qualifiedMst != null) {
            svf.VrsOut("LICENSE", qualifiedMst._conditionName);
            svf.VrsOut("CONTENTS", qualifiedMst._qualifiedName);
            svf.VrsOut("ORGANIZER", qualifiedMst._promoter);
        }
        if (_param._sDate != null && _param._eDate != null) {
            svf.VrsOut("PERIOD", KNJ_EditDate.getAutoFormatDate(db2,_param._sDate) + "〜" + KNJ_EditDate.getAutoFormatDate(db2,_param._eDate));
        }
    }

    private List getPrintStudent(final DB2UDB db2, final String qualifiedSelected) {
        final List rtnList = new ArrayList();

        final String studentSql = getStudentSql(qualifiedSelected);
        log.debug(" sql = " + studentSql);
        for (final Iterator it = KnjDbUtils.query(db2, studentSql).iterator(); it.hasNext();) {
        	final Map row = (Map) it.next();
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final String grade = KnjDbUtils.getString(row, "GRADE");
            final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
            final String hrName = KnjDbUtils.getString(row, "HR_NAME");
            final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
            final String name = KnjDbUtils.getString(row, "NAME");
            final String sex = KnjDbUtils.getString(row, "SEX");
            final String sexName = KnjDbUtils.getString(row, "SEXNAME");
            final Student student = new Student(schregno,
                                                grade,
                                                hrClass,
                                                hrName,
                                                attendno,
                                                name,
                                                sex,
                                                sexName);
            rtnList.add(student);
        }

        setQualifiedStudentList(db2, qualifiedSelected, rtnList);
        return rtnList;
    }

    private String getStudentSql(final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     HR.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.SEX, ");
        stb.append("     N1.NAME1 AS SEXNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_HOBBY_DAT AS T0 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T1 ON T0.SCHREGNO = T1.SCHREGNO ");
        if ("1".equals(_param.use_prg_schoolkind)) {
            if (!StringUtils.isBlank(_param.selectSchoolKind)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append("        AND T2.SCHOOL_KIND IN " + _param.selectSchoolKindSql + "  ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("   AND T2.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT AS HR ON T1.YEAR = HR.YEAR ");
        stb.append("          AND T1.SEMESTER = HR.SEMESTER ");
        stb.append("          AND T1.GRADE = HR.GRADE ");
        stb.append("          AND T1.HR_CLASS = HR.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = N1.NAMECD2 ");
        stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON T1.COURSECODE = COURSEC.COURSECODE ");
        stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON T1.COURSECD = MAJOR.COURSECD ");
        stb.append("          AND T1.MAJORCD = MAJOR.MAJORCD ");
        stb.append("     LEFT JOIN COURSE_MST AS COURSE ON T1.COURSECD = COURSE.COURSECD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND T0.QUALIFIED_CD = '" + selected + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    public class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _sexName;

        List _qualifiedDats;

        Student(final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String name,
                final String sex,
                final String sexName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _name = name;
            _sex = sex;
            _sexName = sexName;

            _qualifiedDats = new ArrayList();
        }

        public String toString() {
            return _schregno + " : " + _name;
        }

        public void addQualifiedDat(QualifiedDat qualifiedDat) {
            _qualifiedDats.add(qualifiedDat);
        }
    }

    public void setQualifiedStudentList(final DB2UDB db2, final String qualifiedCd, final List students) {
        final StringBuffer sql = new StringBuffer();
        sql.append(" SELECT ");
        sql.append("   T1.*, ");
        sql.append("   NMH311.NAME1 AS CONDITION_NAME, ");
        sql.append("   NMH312.NAME1 AS RANK_NAME");
        sql.append(" FROM ");
        sql.append("   SCHREG_QUALIFIED_HOBBY_DAT T1 ");
        sql.append("   LEFT JOIN NAME_MST NMH311 ON NMH311.NAMECD1 = 'H311' AND NMH311.NAMECD2 = CONDITION_DIV ");
        sql.append("   LEFT JOIN NAME_MST NMH312 ON NMH312.NAMECD1 = 'H312' AND NMH312.NAMECD2 = RANK ");
        sql.append(" WHERE ");
        sql.append("   QUALIFIED_CD = '" + qualifiedCd + "' ");
        if (_param._sDate != null && _param._eDate != null) {
            sql.append("   AND REGDDATE BETWEEN '" + _param._sDate + "' AND '" + _param._eDate + "' ");
        }
        sql.append(" ORDER BY ");
        sql.append("   T1.REGDDATE, T1.SEQ");

        for (final Iterator rit = KnjDbUtils.query(db2, sql.toString()).iterator(); rit.hasNext();) {
        	final Map row = (Map) rit.next();
            final String year = KnjDbUtils.getString(row, "YEAR");
            final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
            final String seq = KnjDbUtils.getString(row, "SEQ");
            final String regddate = KnjDbUtils.getString(row, "REGDDATE");
            final String subclasscd;
            if ("1".equals(_param._useCurriculumcd)) {
                subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
            } else {
                subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            }
            final String conditionDiv = KnjDbUtils.getString(row, "CONDITION_NAME");
            final String contents = KnjDbUtils.getString(row, "CONTENTS");
            final String remark = KnjDbUtils.getString(row, "REMARK");
            final String credits = KnjDbUtils.getString(row, "CREDITS");
            final String rank = KnjDbUtils.getString(row, "RANK_NAME");
            final String score = KnjDbUtils.getString(row, "SCORE");
            final String certificate = KnjDbUtils.getString(row, "CERTIFICATE");

            final QualifiedDat qualifiedDat = new QualifiedDat(
                    year,
                    seq,
                    regddate,
                    subclasscd,
                    conditionDiv,
                    contents,
                    remark,
                    credits,
                    rank,
                    score,
                    certificate);

            Student student = null;

            for (Iterator it = students.iterator(); it.hasNext();) {
                Student s = (Student) it.next();
                if (s._schregno != null && s._schregno.equals(schregno)) {
                    student = s;
                    break;
                }
            }

            if (student == null) {
                continue;
            }
            log.debug(" student : " + student + " add " + qualifiedDat);

            student.addQualifiedDat(qualifiedDat);
        }
    }

    public class QualifiedDat {
        final String _year;
        final String _seq;
        final String _regddate;
        final String _subclasscd;
        final String _conditionDiv;
        final String _contents;
        final String _remark;
        final String _credits;
        final String _rank;
        final String _score;
        final String _certificate;

        public QualifiedDat(
                final String year,
                final String seq,
                final String regddate,
                final String subclasscd,
                final String conditionDiv,
                final String contents,
                final String remark,
                final String credits,
                final String rank,
                final String score,
                final String certificate
        ) {
            _year = year;
            _seq = seq;
            _regddate = regddate;
            _subclasscd = subclasscd;
            _conditionDiv = conditionDiv;
            _contents =contents;
            _remark = remark;
            _credits = credits;
            _rank = rank;
            _score = score;
            _certificate = certificate;
        }

        public String toString() {
            return "[" + _year + " , " + _regddate + " , " + _contents + " , " + _rank + "]";
        }
    }

    public class QualifiedMst {
        final String _qualifiedCd;
        final String _conditionDiv;
        final String _conditionName;
        final String _qualifiedName;
        final String _promoter;

        public QualifiedMst(
                final String qualifiedCd,
                final String conditionDiv,
                final String conditionName,
                final String qualifiedName,
                final String promoter) {
            _qualifiedCd = qualifiedCd;
            _conditionDiv = conditionDiv;
            _conditionName = conditionName;
            _qualifiedName = qualifiedName;
            _promoter = promoter;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74913 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String[] _qualifiedSelected;
        private final String _sDate;
        private final String _eDate;
        private final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String use_prg_schoolkind;
        final String selectSchoolKind;
        private String selectSchoolKindSql = null;
        private Map _qualifiedMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _qualifiedSelected = request.getParameterValues("CATEGORY_SELECTED");
            final String sdate = request.getParameter("SDATE");
            final String edate = request.getParameter("EDATE");
            _sDate = (sdate == null) ? null : sdate.replace('/', '-');
            _eDate = (edate == null) ? null : edate.replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                StringBuffer stb = new StringBuffer("('");
                final String[] split = StringUtils.split(selectSchoolKind, ":");
                if (null != split) {
                    for (int i = 0; i < split.length; i++) {
                        stb.append(split[i] + "', '");
                    }
                }
                selectSchoolKindSql = stb.append("')").toString();
            }
            loadQualifiedMst(db2);
        }

        private void loadQualifiedMst(DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   T1.*, ");
            sql.append("   NMH311.NAME1 AS CONDITION_NAME ");
            sql.append(" FROM ");
            sql.append("   QUALIFIED_MST T1 ");
            sql.append("   LEFT JOIN NAME_MST NMH311 ON NMH311.NAMECD1 = 'H311' AND NMH311.NAMECD2 = T1.CONDITION_DIV ");
            _qualifiedMstMap = new HashMap();

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String qualifiedCd = KnjDbUtils.getString(row, "QUALIFIED_CD");
                final String conditionDiv = KnjDbUtils.getString(row, "CONDITION_DIV");
                final String conditionName = KnjDbUtils.getString(row, "CONDITION_NAME");
                final String qualifiedName = KnjDbUtils.getString(row, "QUALIFIED_NAME");
                final String promoter = KnjDbUtils.getString(row, "PROMOTER");

                QualifiedMst qualifiedMst = new QualifiedMst(qualifiedCd, conditionDiv, conditionName, qualifiedName, promoter);

                _qualifiedMstMap.put(qualifiedCd, qualifiedMst);
            }
        }

        public QualifiedMst getQualifiedMst(String qualifiedCd) {
            return (QualifiedMst) _qualifiedMstMap.get(qualifiedCd);
        }
    }
}

// eof
