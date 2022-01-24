/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 7321b5d363eeb0c38009d4719db8050df9f2459a $
 *
 * 作成日: 2018/07/24
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJH441 {

    private static final Log log = LogFactory.getLog(KNJH441.class);

    final static String SUNDAI = "00000001";
    final static String BENE = "00000002";
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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = createStudents(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {

            final Student student = (Student) iterator.next();

            if (student._mockRankRangeList.size() > 0) {
                svf.VrSetForm("KNJH441.frm", 4);
            } else {
                svf.VrSetForm("KNJH441.frm", 1);
            }
            svf.VrsOut("TITLE", _param._gradeName + "模試成績個人票");
            svf.VrsOut("SUBTITLE", "模試結果（全国偏差）");
            svf.VrsOut("HR_NAME", student._hrName + " " + student._attendNo + "番");
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);

            //科目名
            int mockSubCnt = 1;
            for (Iterator itSubclass = student._mockSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String mockSub = (String) itSubclass.next();
                final MockSubclass mockSubclass = (MockSubclass) student._mockSubclassMap.get(mockSub);
                svf.VrsOutn("SUBCLASS_NAME1", mockSubCnt, mockSubclass._subclassname);
                mockSubCnt++;
            }

            //模試
            String befMockCd = "";
            for (Iterator itMockRange = student._mockRankRangeList.iterator(); itMockRange.hasNext();) {
                final MockRangeRank mockRangeRank = (MockRangeRank) itMockRange.next();
                if (!"".equals(befMockCd) && !befMockCd.equals(mockRangeRank._mockCd)) {
                    svf.VrEndRecord();
                }
                svf.VrsOut("MOCK_NAME1", mockRangeRank._mockName);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP_MD(mockRangeRank._mosiDate));
                if (student._mockSubclassMap.containsKey(mockRangeRank._mockSubclassCd)) {
                    final MockSubclass mockSubclass = (MockSubclass) student._mockSubclassMap.get(mockRangeRank._mockSubclassCd);

                    svf.VrsOut("SCORE" + mockSubclass._fieldNo, mockRangeRank._score);
                    svf.VrsOut("DEVI" + mockSubclass._fieldNo, mockRangeRank._deviation);
                    if (!"".equals(mockRangeRank._inoutRank)) {
                        svf.VrsOut("IO_RANK" + mockSubclass._fieldNo, mockRangeRank._inoutRank + "/" + mockRangeRank._inoutRankCnt);
                    }
                    if (!"".equals(mockRangeRank._rank)) {
                        svf.VrsOut("G_RANK" + mockSubclass._fieldNo, mockRangeRank._rank + "/" + mockRangeRank._rankCnt);
                    }
                }
                //志望校
                if (SUNDAI.equals(mockRangeRank._companyCd)) {
                    printHope(svf, student._sundaiHopeMap, mockRangeRank._mockCd);
                } else if (BENE.equals(mockRangeRank._companyCd)) {
                    printHope(svf, student._beneHopeMap, mockRangeRank._mockCd);
                } else {
                    printHope(svf, student._sonotaHopeMap, mockRangeRank._mockCd);
                }
                befMockCd = mockRangeRank._mockCd;
            }
            if (student._mockRankRangeList.size() > 0) {
                svf.VrEndRecord();
            } else {
                svf.VrEndPage();
            }
            _hasData = true;
        }
    }

    private void printHope(final Vrw32alp svf, final Map hopeMap, final String mockCd) {
        final List hopeList = (List) hopeMap.get(mockCd);
        if (null != hopeList) {
        	int hopeCnt = 1;
        	for (Iterator itHopeData = hopeList.iterator(); itHopeData.hasNext();) {
        		final HopeData hopeData = (HopeData) itHopeData.next();
        		svf.VrsOut("HOPE" + hopeCnt, hopeData._schoolName + " " + hopeData._gakubuName + " " + hopeData._gakkaName);
        		svf.VrsOut("JUDGE" + hopeCnt, hopeData._allJudge);
        		hopeCnt++;
        	}
        }
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List retList = new LinkedList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGD.ATTENDNO  ");

            //log.debug(" regd sql = " + sql);
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrclass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");
                final String majorName = rs.getString("MAJORNAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        hrName,
                        attendno,
                        name,
                        coursecd,
                        majorcd,
                        coursecode,
                        majorName,
                        courseCodeName
                );
                student.setMockRangeRank(db2);
                retList.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final List _mockRankRangeList;
        final Map _mockSubclassMap;
        final Map _beneHopeMap;
        final Map _sundaiHopeMap;
        final Map _sonotaHopeMap;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _mockRankRangeList = new ArrayList();
            _mockSubclassMap = new TreeMap();
            _beneHopeMap = new TreeMap();
            _sundaiHopeMap = new TreeMap();
            _sonotaHopeMap = new TreeMap();
        }

        public void setMockRangeRank(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MRANGE.MOCKCD, ");
            stb.append("     MMST.MOCKNAME1, ");
            stb.append("     MMST.MOSI_DATE, ");
            stb.append("     MMST.COMPANYCD, ");
            stb.append("     MRANGE.MOCK_SUBCLASS_CD, ");
            stb.append("     MSUBM.SUBCLASS_NAME, ");
            stb.append("     MRANGE.SCORE, ");
            stb.append("     MRANGE.DEVIATION, ");
            stb.append("     MRANGE.RANK, ");
            stb.append("     MRANGE.CNT AS RANK_CNT, ");
            stb.append("     MRANGE3.RANK AS INOUT_RANK, ");
            stb.append("     MRANGE3.CNT AS INOUT_RANK_CNT ");
            stb.append(" FROM ");
            stb.append("     MOCK_RANK_RANGE_DAT MRANGE ");
            stb.append("     LEFT JOIN MOCK_MST MMST ON MRANGE.MOCKCD = MMST.MOCKCD ");
            stb.append("     LEFT JOIN MOCK_SUBCLASS_MST MSUBM ON MRANGE.MOCK_SUBCLASS_CD = MSUBM.MOCK_SUBCLASS_CD ");
            stb.append("     LEFT JOIN MOCK_RANK_RANGE_DAT MRANGE3 ON MRANGE.YEAR = MRANGE3.YEAR ");
            stb.append("          AND MRANGE.MOCKCD = MRANGE3.MOCKCD ");
            stb.append("          AND MRANGE.SCHREGNO = MRANGE3.SCHREGNO ");
            stb.append("          AND MRANGE.MOCK_SUBCLASS_CD = MRANGE3.MOCK_SUBCLASS_CD ");
            stb.append("          AND MRANGE3.RANK_RANGE = '3' ");
            stb.append("          AND MRANGE3.RANK_DIV = '02' ");
            stb.append("          AND MRANGE3.MOCKDIV = '1' ");
            stb.append(" WHERE ");
            stb.append("     MRANGE.YEAR = '" + _param._year + "' ");
            stb.append("     AND MRANGE.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND MRANGE.RANK_RANGE = '2' ");
            stb.append("     AND MRANGE.RANK_DIV = '02' ");
            stb.append("     AND MRANGE.MOCKDIV = '1' ");
            stb.append(" ORDER BY ");
            stb.append("     MRANGE.MOCKCD, ");
            stb.append("     MRANGE.MOCK_SUBCLASS_CD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String mockCd = rs.getString("MOCKCD");
                    final String mockName = rs.getString("MOCKNAME1");
                    final String mosiDate = StringUtils.defaultString(rs.getString("MOSI_DATE"));
                    final String companyCd = rs.getString("COMPANYCD");
                    final String mockSubclassCd = StringUtils.defaultString(rs.getString("MOCK_SUBCLASS_CD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASS_NAME"));
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final String deviation = StringUtils.defaultString(rs.getString("DEVIATION"));
                    final String rank = StringUtils.defaultString(rs.getString("RANK"));
                    final String rankCnt = StringUtils.defaultString(rs.getString("RANK_CNT"));
                    final String inoutRank = StringUtils.defaultString(rs.getString("INOUT_RANK"));
                    final String inoutRankCnt = StringUtils.defaultString(rs.getString("INOUT_RANK_CNT"));

                    final MockSubclass mockSubclass = new MockSubclass(
                            mockSubclassCd,
                            subclassname
                    );
                    _mockSubclassMap.put(mockSubclassCd, mockSubclass);

                    final MockRangeRank mockRangeRank = new MockRangeRank(
                            mockCd,
                            mockName,
                            mosiDate,
                            companyCd,
                            mockSubclassCd,
                            subclassname,
                            score,
                            deviation,
                            rank,
                            rankCnt,
                            inoutRank,
                            inoutRankCnt
                    );
                    _mockRankRangeList.add(mockRangeRank);
                }
            } catch (final SQLException e) {
                log.error("模試データ読込でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            int mockFieldNo = 1;
            for (Iterator iterator = _mockSubclassMap.keySet().iterator(); iterator.hasNext();) {
                final String msubKey = (String) iterator.next();
                final MockSubclass mockSubclass = (MockSubclass) _mockSubclassMap.get(msubKey);
                mockSubclass._fieldNo = mockFieldNo;
                mockFieldNo++;
            }

            //BENE
            final StringBuffer stbBene = new StringBuffer();
            stbBene.append(" SELECT ");
            stbBene.append("     HOPEH.MOCKCD, ");
            stbBene.append("     HOPED.SEQ, ");
            stbBene.append("     HOPED.SCHOOL_CD, ");
            stbBene.append("     HOPED.SCHOOL_NAME, ");
            stbBene.append("     HOPED.GAKUBU_NAME, ");
            stbBene.append("     HOPED.GAKKA_NAME, ");
            stbBene.append("     HOPED.ALL_JUDGE ");
            stbBene.append(" FROM ");
            stbBene.append("     MOCK_CSV_BENE_HOPE_HDAT HOPEH ");
            stbBene.append("     INNER JOIN MOCK_CSV_BENE_HOPE_DAT HOPED ON HOPEH.YEAR = HOPED.YEAR ");
            stbBene.append("           AND HOPEH.KYOUZAICD = HOPED.KYOUZAICD ");
            stbBene.append("           AND HOPEH.BENEID = HOPED.BENEID ");
            stbBene.append(" WHERE ");
            stbBene.append("     HOPEH.YEAR = '" + _param._year + "' ");
            stbBene.append("     AND HOPEH.SCHREGNO = '" + _schregno + "' ");
            stbBene.append(" ORDER BY ");
            stbBene.append("     HOPEH.MOCKCD, ");
            stbBene.append("     HOPED.SEQ ");

            PreparedStatement psBeneHope = null;
            ResultSet rsBeneHope = null;
            try {
                psBeneHope = db2.prepareStatement(stbBene.toString());
                rsBeneHope = psBeneHope.executeQuery();
                String befMock = "";
                while (rsBeneHope.next()) {
                    final String mockCd = rsBeneHope.getString("MOCKCD");
                    final String seq = rsBeneHope.getString("SEQ");
                    final String schoolCd = rsBeneHope.getString("SCHOOL_CD");
                    final String schoolName = StringUtils.defaultString(rsBeneHope.getString("SCHOOL_NAME"));
                    final String gakubuName = StringUtils.defaultString(rsBeneHope.getString("GAKUBU_NAME"));
                    final String gakkaName = StringUtils.defaultString(rsBeneHope.getString("GAKKA_NAME"));
                    final String allJudge = StringUtils.defaultString(rsBeneHope.getString("ALL_JUDGE"));
                    if (!befMock.equals(mockCd)) {
                        final List setList = new ArrayList();
                        _beneHopeMap.put(mockCd, setList);
                    }
                    final List beneList = (List) _beneHopeMap.get(mockCd);
                    final HopeData hobeData = new HopeData(mockCd, seq, schoolCd, schoolName, gakubuName, gakkaName, allJudge);
                    beneList.add(hobeData);

                    befMock = mockCd;
                }
            } catch (final SQLException e) {
                log.error("BENEデータ読込でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psBeneHope, rsBeneHope);
            }

            //SUNDAI
            final StringBuffer stbSundai = new StringBuffer();
            stbSundai.append(" SELECT ");
            stbSundai.append("     HOPEH.MOCKCD, ");
            stbSundai.append("     HOPED.SEQ, ");
            stbSundai.append("     HOPED.SCHOOL_CD, ");
            stbSundai.append("     HOPED.SCHOOL_NAME, ");
            stbSundai.append("     HOPED.JUDGE_HYOUKA ");
            stbSundai.append(" FROM ");
            stbSundai.append("     MOCK_CSV_SUNDAI_HDAT HOPEH ");
            stbSundai.append("     INNER JOIN MOCK_CSV_SUNDAI_HOPE_DAT HOPED ON HOPEH.YEAR = HOPED.YEAR ");
            stbSundai.append("           AND HOPEH.MOSI_CD = HOPED.MOSI_CD ");
            stbSundai.append("           AND HOPEH.EXAMNO = HOPED.EXAMNO ");
            stbSundai.append(" WHERE ");
            stbSundai.append("     HOPEH.YEAR = '" + _param._year + "' ");
            stbSundai.append("     AND HOPEH.SCHREGNO = '" + _schregno + "' ");
            stbSundai.append(" ORDER BY ");
            stbSundai.append("     HOPEH.MOCKCD, ");
            stbSundai.append("     HOPED.SEQ ");

            PreparedStatement psSundai = null;
            ResultSet rsSundai = null;
            try {
                psSundai = db2.prepareStatement(stbSundai.toString());
                rsSundai = psSundai.executeQuery();
                String befMock = "";
                while (rsSundai.next()) {
                    final String mockCd = rsSundai.getString("MOCKCD");
                    final String seq = rsSundai.getString("SEQ");
                    final String schoolCd = rsSundai.getString("SCHOOL_CD");
                    final String schoolName = StringUtils.defaultString(rsSundai.getString("SCHOOL_NAME"));
                    final String judgeHyouka = StringUtils.defaultString(rsSundai.getString("JUDGE_HYOUKA"));
                    if (!befMock.equals(mockCd)) {
                        final List setList = new ArrayList();
                        _sundaiHopeMap.put(mockCd, setList);
                    }
                    final List sundaiList = (List) _sundaiHopeMap.get(mockCd);
                    final HopeData hobeData = new HopeData(mockCd, seq, schoolCd, schoolName, "", "", judgeHyouka);
                    sundaiList.add(hobeData);

                    befMock = mockCd;
                }
            } catch (final SQLException e) {
                log.error("SUNDAIデータ読込でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSundai, rsSundai);
            }

            //SONOTA
            final StringBuffer stbSonota = new StringBuffer();
            stbSonota.append(" SELECT ");
            stbSonota.append("     HOPEH.MOCKCD, ");
            stbSonota.append("     HOPED.SEQ, ");
            stbSonota.append("     HOPED.SCHOOL_CD, ");
            stbSonota.append("     HOPED.SCHOOL_NAME, ");
            stbSonota.append("     HOPED.JUDGE_HYOUKA ");
            stbSonota.append(" FROM ");
            stbSonota.append("     MOCK_CSV_ZKAI_HDAT HOPEH ");
            stbSonota.append("     INNER JOIN MOCK_CSV_ZKAI_HOPE_DAT HOPED ON HOPEH.YEAR = HOPED.YEAR ");
            stbSonota.append("           AND HOPEH.MOSI_CD = HOPED.MOSI_CD ");
            stbSonota.append("           AND HOPEH.HR_CLASS = HOPED.HR_CLASS ");
            stbSonota.append("           AND HOPEH.ATTENDNO = HOPED.ATTENDNO ");
            stbSonota.append(" WHERE ");
            stbSonota.append("     HOPEH.YEAR = '" + _param._year + "' ");
            stbSonota.append("     AND '0' || SUBSTR(HOPEH.MOCKCD, 7, 1) = '" + _grade + "' ");
            stbSonota.append("     AND RIGHT('000' || HOPEH.HR_CLASS, 3) = '" + _hrClass + "' ");
            stbSonota.append("     AND HOPEH.ATTENDNO = '" + _attendNo + "' ");
            stbSonota.append(" ORDER BY ");
            stbSonota.append("     HOPEH.MOCKCD, ");
            stbSonota.append("     HOPED.SEQ ");

            PreparedStatement psSonota = null;
            ResultSet rsSonota = null;
            try {
                psSonota = db2.prepareStatement(stbSonota.toString());
                rsSonota = psSonota.executeQuery();
                String befMock = "";
                while (rsSonota.next()) {
                    final String mockCd = rsSonota.getString("MOCKCD");
                    final String seq = rsSonota.getString("SEQ");
                    final String schoolCd = rsSonota.getString("SCHOOL_CD");
                    final String schoolName = StringUtils.defaultString(rsSonota.getString("SCHOOL_NAME"));
                    final String judgeHyouka = StringUtils.defaultString(rsSonota.getString("JUDGE_HYOUKA"));
                    if (!befMock.equals(mockCd)) {
                        final List setList = new ArrayList();
                        _sonotaHopeMap.put(mockCd, setList);
                    }
                    final List sonotaList = (List) _sonotaHopeMap.get(mockCd);
                    final HopeData hobeData = new HopeData(mockCd, seq, schoolCd, schoolName, "", "", judgeHyouka);
                    sonotaList.add(hobeData);

                    befMock = mockCd;
                }
            } catch (final SQLException e) {
                log.error("SONOTAデータ読込でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSonota, rsSonota);
            }
        }
    }

    private class MockRangeRank {
        final String _mockCd;
        final String _mockName;
        final String _mosiDate;
        final String _companyCd;
        final String _mockSubclassCd;
        final String _subclassname;
        final String _score;
        final String _deviation;
        final String _rank;
        final String _rankCnt;
        final String _inoutRank;
        final String _inoutRankCnt;
        public MockRangeRank(
                final String mockCd,
                final String mockName,
                final String mosiDate,
                final String companyCd,
                final String mockSubclassCd,
                final String subclassname,
                final String score,
                final String deviation,
                final String rank,
                final String rankCnt,
                final String inoutRank,
                final String inoutRankCnt
        ) {
            _mockCd = mockCd;
            _mockName = mockName;
            _mosiDate = mosiDate;
            _companyCd = companyCd;
            _mockSubclassCd = mockSubclassCd;
            _subclassname = subclassname;
            _score = score;
            _deviation = deviation;
            _rank = rank;
            _rankCnt = rankCnt;
            _inoutRank = inoutRank;
            _inoutRankCnt = inoutRankCnt;
        }

    }

    private class MockSubclass {
        final String _mockSubclassCd;
        final String _subclassname;
        int _fieldNo;
        public MockSubclass(
                final String mockSubclassCd,
                final String subclassname
        ) {
            _mockSubclassCd = mockSubclassCd;
            _subclassname = subclassname;
        }

    }

    private class HopeData {
        final String _mockCd;
        final String _seq;
        final String _schoolCd;
        final String _schoolName;
        final String _gakubuName;
        final String _gakkaName;
        final String _allJudge;
        public HopeData(
                final String mockCd,
                final String seq,
                final String schoolCd,
                final String schoolName,
                final String gakubuName,
                final String gakkaName,
                final String allJudge
        ) {
            _mockCd = mockCd;
            _seq = seq;
            _schoolCd = schoolCd;
            _schoolName = schoolName;
            _gakubuName = gakubuName;
            _gakkaName = gakkaName;
            _allJudge = allJudge;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63311 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _year;
        final String _semester;
        final String _testcd;
        final String _grade;
        final String _gradeName;
        final String _hrClass;
        final String[] _categorySelected;
        final String _loginDate;
        final String _prgid;
        final String _usecurriculumcd;
        final String _useclassdetaildat;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _testcd = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useclassdetaildat = request.getParameter("useClassDetailDat");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _gradeName = getGradeName(db2);
        }

        private String getGradeName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     GRADE_NAME1 ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND GRADE = '" + _grade + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("GRADE_NAME1"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return retStr;
        }

    }
}

// eof
