// kanji=漢字
/*
 * $Id: 4420c68e87f611d0a92e5441ce880a216fb79d68 $
 *
 * 作成日: 2008/06/25 11:26:38 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.KNJD234.HomeRoom;
import servletpack.KNJD.KNJD234.Param;
import servletpack.KNJD.KNJD234.Record;
import servletpack.KNJD.KNJD234.Student;
import servletpack.KNJD.KNJD234.SubClass;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 4420c68e87f611d0a92e5441ce880a216fb79d68 $
 */
public abstract class KNJD234DetailAbstract {

    /** 成績不審者 */
    private static final String BAD = "BAD";
    /** 成績優良者 */
    private static final String GOOD = "GOOD";

    protected boolean _hasData;
    protected DB2UDB _db2;
    protected Param _param;
    protected Vrw32alp _svf;
    protected List _pageList;
    protected Map _subclassMap;
    protected int _maxLine;
    protected int _maxRetu;
    private static final String SSEMESTER = "1";
    private static final String ATTEND_OBJ_SEM = "9";
    KNJDefineCode definecode;       //各学校における定数等設定
    private KNJDefineSchool defineSchoolCode;       //各学校における定数等設定
    private KNJSchoolMst _knjSchoolMst;


    static final Log log = LogFactory.getLog(KNJD234DetailAbstract.class);

    protected KNJD234DetailAbstract(final DB2UDB db2, final Vrw32alp svf, final Param param) throws IOException {
        _svf = svf;
        _db2 = db2;
        _param = param;
        _pageList = new ArrayList();
        _subclassMap = new HashMap();

        defineSchoolCode = new KNJDefineSchool();
        defineSchoolCode.defineCode(_db2, _param.getYear());
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, _param.getYear());
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }
    }

    /** 印字処理 */
    abstract protected boolean printOut();

    public void setPrintData(final String setDiv, final int maxLine, final int maxRetu) throws SQLException, ParseException {
        _maxLine = maxLine;
        _maxRetu = maxRetu;
        int cnt = 0;
        int renBan = 1;
        List studentList = new ArrayList();

        for (final Iterator it = _param.getGradeClass().iterator(); it.hasNext();) {
            final HomeRoom hr = (HomeRoom) it.next();
            log.debug(hr);

            final List students = new ArrayList(hr.getStudents());
            Collections.sort(students);

            for (final Iterator it1 = students.iterator(); it1.hasNext();) {
                final Student student = (Student) it1.next();
                // 対象者判定
                if (isObject(setDiv, student)) {
                    cnt++;
                    if (cnt > _maxLine) {
                        _pageList.add(studentList);
                        studentList = new ArrayList();
                        cnt = 1;
                    }
//                    log.debug(student);

                    final String schregno = student._schregno;
                    final String hrClass = hr.getName();
                    final String room = hr.getRoom();
                    final String attendno = student._attendNo;
                    final int totalCredit = student.calcStudyRecCredit();
                    final int totalCompCredit = student.calcStudyRecCompCredit();
                    final String name = student._abbv;
                    final String entName = student._entName;
                    final double creditAvg = student.getGradValueAvg(1, BigDecimal.ROUND_HALF_UP);
                    final Map records = student.getRecords();

                    PrintDataStudent printDataStudent = new PrintDataStudent(schregno,
                                                                             room,
                                                                             hrClass,
                                                                             attendno,
                                                                             name,
                                                                             entName,
                                                                             String.valueOf(creditAvg),
                                                                             renBan,
                                                                             records,
                                                                             totalCredit,
                                                                             totalCompCredit);

                    if (setDiv.equals(BAD)) {
                        printDataStudent.setAttend();
                    }
                    studentList.add(printDataStudent);

                    for (final Iterator it2 = records.keySet().iterator(); it2.hasNext();) {
                        final String subClassCd = (String) it2.next();
                        final Record rec = (Record) records.get(subClassCd);
                        final SubClass subClass = rec.getSubclass();
                        final Map subclassInfo = subClass.getSubclassInfo();
                        final String subclassCd = (String) subclassInfo.get("CODE");
                        if (!_subclassMap.containsKey(subclassCd)) {
                            _subclassMap.put(subclassCd, subClass);
                        }
                    }
                    renBan++;
                }
            }
            
        }
        if (cnt > 1) {
            _pageList.add(studentList);
        }
    }

    private boolean isObject(final String setDiv, final Student student) {
        if (setDiv.equals(GOOD)) {
            return student.isGood();
        } else {
            return student.isBadAvgUnder(_param.getLowerLine()) ||
            student.isBadCountUnder(_param.getLowerCount()) ||
            student.isBadUnStudy(_param.getLowerUnStudyCount());
        }
    }

    public Collection getSortSubclass() {
        return _subclassMap.values();
    }

    protected class PrintDataStudent {
        final String _schregno;
        final String _room;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final String _entName;
        final String _creditAvg;
        final int _renBan;
        final Map _records;
        final int _totalCredit;
        final int _totalCompCredit;
        Map _attendInfo;

        public PrintDataStudent(
                final String schregno,
                final String room,
                final String hrClass,
                final String attendNo,
                final String name,
                final String entName,
                final String creditAvg,
                final int renBan,
                final Map records,
                final int totalCredit,
                final int totalCompCredit
        ) {
            _schregno = schregno;
            _room = room;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
            _entName = entName;
            _creditAvg = creditAvg;
            _renBan = renBan;
            _records = records;
            _totalCredit = totalCredit;
            _totalCompCredit = totalCompCredit;
            _attendInfo = new HashMap();
        }

        public void setAttend() throws SQLException, ParseException {
            _attendInfo = setAttendSubclass(_schregno, _param.getGrade(), _room);
        }

        public String toString() {
            return _hrClass + " " + _attendNo + ":" + _name;
        }
    }

    public Map setAttendSubclass(
            final String schregno,
            final String grade,
            final String hrClass
    ) throws SQLException, ParseException {
        Map rtnMap = new HashMap();
        final String year = _param.getYear();
        final String semester = _param.getSemester();

        PreparedStatement ps = null;
        ResultSet rsAttend = null;
        try {
            final String z010 = setNameMst(_db2, year, "Z010", "00");
            final Map attendSemAllMap = AttendAccumulate.getAttendSemesMap(_db2, z010, year);
            final String periodInState = AttendAccumulate.getPeiodValue(_db2, definecode, year, SSEMESTER, semester);
            final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemAllMap, year + "-04-01", _param.getDate());
            final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();
            final String attendSql = AttendAccumulate.getAttendSubclassSql(
                                                    semesFlg,
                                                    definecode,
                                                    defineSchoolCode,
                                                    _knjSchoolMst,
                                                    year,
                                                    SSEMESTER,
                                                    semester,
                                                    (String) hasuuMap.get("attendSemesInState"),
                                                    periodInState,
                                                    (String) hasuuMap.get("befDayFrom"),
                                                    (String) hasuuMap.get("befDayTo"),
                                                    (String) hasuuMap.get("aftDayFrom"),
                                                    (String) hasuuMap.get("aftDayTo"),
                                                    grade,
                                                    hrClass,
                                                    schregno,
                                                    "1",
                                                    _param._useCurriculumcd,
                                                    _param._useVirus,
                                                    _param._useKoudome);
            ps = _db2.prepareStatement(attendSql);
            rsAttend = ps.executeQuery();
            while (rsAttend.next()) {
                final String subclassCd = StringUtils.replace(rsAttend.getString("SUBCLASSCD"), "-", "");
                final int rsAbsent = rsAttend.getInt("ABSENT_SEM");
                if (!rtnMap.containsKey(subclassCd)) {
                    rtnMap.put(subclassCd, new Integer(0));
                }

                int totalAbsent = ((Integer) rtnMap.get(subclassCd)).intValue();
                if (ATTEND_OBJ_SEM.equals(rsAttend.getString("SEMESTER"))) {
                    totalAbsent += rsAbsent;
                }
                rtnMap.put(subclassCd, new Integer(totalAbsent));
            }
        } finally {
            _db2.commit();
            DbUtils.closeQuietly(null, ps, rsAttend);
        }
        return rtnMap;
    }

    private String setNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2) throws SQLException {
        String rtnSt = "";
        final String sql = getNameMst(year, namecd1, namecd2);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnSt = rs.getString("NAME1");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnSt;
    }

    private String getNameMst(final String year, final String namecd1, final String namecd2) {
        final String rtnSql = " SELECT "
                            + "     * "
                            + " FROM "
                            + "     V_NAME_MST "
                            + " WHERE "
                            + "     YEAR = '" + year + "' "
                            + "     AND NAMECD1 = '" + namecd1 + "' "
                            + "     AND NAMECD2 = '" + namecd2 + "'";
        return rtnSql;
    }
}
 // KNJD234BadStudents

// eof
