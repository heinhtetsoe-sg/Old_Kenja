/*
 * $Id$
 *
 * 作成日: 2017/06/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL316P {

    private static final Log log = LogFactory.getLog(KNJL316P.class);

    private static final String CYUGAKU = "1";
    private static final String KOUKOU = "2";

    private static final String KENGAI_ZENKI = "1";
    private static final String KENNAI_ZENKI = "2";
    private static final String KENNAI_KOUKI = "3";

    private static final String KENNAI_SUISEN = "1";
    private static final String KENNAI_IPPAN = "2";

    private static final String MENSETU_NASHI = "1";
    private static final String MENSETU_ARI = "2";

    private static final String ORDER_EXAM = "1";
    private static final String ORDER_SCORE = "2";

    private static final String SCORE_DAT = "1";
    private static final String INTERVIEW_DAT = "2";

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

        final List studentList = getStudentList(db2);
        if (CYUGAKU.equals(_param._applicantDiv)) {
            if (KENGAI_ZENKI.equals(_param._testDiv)) {
                svf.VrSetForm("KNJL316P_1.frm", 1);
                printOut_J(svf, 1, studentList);
            } else if (KENNAI_ZENKI.equals(_param._testDiv)) {
                svf.VrSetForm("KNJL316P_2.frm", 1);
                printOut_J(svf, 2, studentList);
            } else { // if (KENNAI_KOUKI.equals(_param._testDiv)) {
                svf.VrSetForm("KNJL316P_3.frm", 1);
                printOut_J(svf, 3, studentList);
            }
        } else if (KOUKOU.equals(_param._applicantDiv)) {
            if (KENNAI_IPPAN.equals(_param._testDiv)) {
                svf.VrSetForm("KNJL316P_4.frm", 1);
                printOutFrm316P_4(svf, studentList);
            } else {
                svf.VrSetForm("KNJL316P_5.frm", 1);
                printOutFrm316P_5(svf, studentList);
            }
        }
    }

    private void printOut_J(final Vrw32alp svf, final int formFlg, final List studentList) {
        final int maxCnt;
        final int allPage;
        if (formFlg == 1) {
            maxCnt = 75;
            final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
            final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
            allPage = pageCnt + pageAmari;
        } else if (formFlg == 2) {
            maxCnt = 45;
            final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
            final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
            allPage = pageCnt + pageAmari;
        } else if (formFlg == 3) {
            maxCnt = 45;
            final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
            final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
            allPage = pageCnt + pageAmari;
        } else {
            return;
        }
        setTitle(svf);

        int noCnt = 1;
        int lineCnt = 1;
        final int maxLine = maxCnt;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOutn("NO", lineCnt, String.valueOf(noCnt));
            if (_param._isOutPutGohi) {
                svf.VrsOutn("JUDGE", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE1", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE2", lineCnt, student._judgeName);
            }
            svf.VrsOutn("EXAM_NO1", lineCnt, student._examNo);
            final String[] nameArray = StringUtils.split(student._name, "　");
            final String nameSeiField = getMS932ByteLength(nameArray[0]) > 12 ? "1_2" : "1_1";
            svf.VrsOutn("NAME" + nameSeiField, lineCnt, nameArray[0]);
            final String nameMeiField = getMS932ByteLength(nameArray[1]) > 12 ? "2_2" : "2_1";
            svf.VrsOutn("NAME" + nameMeiField, lineCnt, nameArray[1]);

            if (formFlg == 1) {
                final String priSchoolField = getMS932ByteLength(nameArray[1]) > 30 ? "2" : "1";
                svf.VrsOutn("PRISCHOOL_NAME" + priSchoolField, lineCnt, student._juku + "　" + student._kyoushitsu);
            }
            int subCnt = 1;
            for (Iterator itSubclass = student._scoreSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._scoreSubclassMap.get(subclassCd);
                svf.VrsOutn("POINT" + subCnt, lineCnt, subclass._score);
                subCnt++;
            }

            if (formFlg == 1) {
                svf.VrsOutn("POINT_ALL", lineCnt, student._total4);
            } else if (formFlg == 2) {
                final Subclass scoreInterView = (Subclass) student._scoreInterViewMap.get("A");
                svf.VrsOutn("INTERVIEW", lineCnt, scoreInterView._score);
                svf.VrsOutn("POINT_ALL1", lineCnt, student._total4);
                svf.VrsOutn("POINT_ALL2", lineCnt, student._total2);
            } else if (formFlg == 3) {
                final Subclass scoreInterView = (Subclass) student._scoreInterViewMap.get("A");
                svf.VrsOutn("INTERVIEW", lineCnt, scoreInterView._score);
                svf.VrsOutn("POINT_ALL1", lineCnt, student._total4);
                svf.VrsOutn("POINT_ALL2", lineCnt, student._total2);
            }

            subCnt = 1;
            for (Iterator itSubclass = student._devSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._devSubclassMap.get(subclassCd);
                svf.VrsOutn("DEVI" + subCnt, lineCnt, subclass._dev);
                subCnt++;
            }
            if (formFlg == 1) {
                svf.VrsOutn("DEVI_ALL", lineCnt, student._deviation4);

                svf.VrsOutn("RANK1", lineCnt, student._totalRank4);
            } else if (formFlg == 2) {
                final Subclass devInterView = (Subclass) student._devInterViewMap.get("A");
                svf.VrsOutn("DEVI5", lineCnt, devInterView._dev);
                svf.VrsOutn("DEVI6", lineCnt, student._deviation4);
                svf.VrsOutn("DEVI_ALL", lineCnt, student._deviation2);

                svf.VrsOutn("RANK1_1", lineCnt, student._totalRank4);
                svf.VrsOutn("RANK1_2", lineCnt, student._totalRank2);
                svf.VrsOutn("GOOD", lineCnt, student._goodTreat);
                svf.VrsOutn("KYOUDAI_SIMAI", lineCnt, student._kyoudai);
            } else if (formFlg == 3) {
                final Subclass devInterView = (Subclass) student._devInterViewMap.get("A");
                svf.VrsOutn("DEVI5", lineCnt, devInterView._dev);
                svf.VrsOutn("DEVI6", lineCnt, student._deviation4);
                svf.VrsOutn("DEVI_ALL", lineCnt, student._deviation2);

                svf.VrsOutn("RANK1_1", lineCnt, student._totalRank4);
                svf.VrsOutn("RANK1_2", lineCnt, student._totalRank2);
                svf.VrsOutn("RANK2_1", lineCnt, student._zenkiRank4);
                svf.VrsOutn("RANK2_2", lineCnt, student._zenkiRank2);
                svf.VrsOutn("GOOD", lineCnt, student._goodTreat);
                svf.VrsOutn("KYOUDAI_SIMAI", lineCnt, student._kyoudai);
            }
            noCnt++;
            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void printOutFrm316P_4(final Vrw32alp svf, final List studentList) {
        final int maxCnt = 45;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int noCnt = 1;
        int lineCnt = 1;
        final int maxLine = 45;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOutn("NO", lineCnt, String.valueOf(noCnt));
            if (_param._isOutPutGohi) {
                svf.VrsOutn("JUDGE", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE1", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE2", lineCnt, student._judgeName);
            }
            svf.VrsOutn("EXAM_NO1", lineCnt, student._examNo);
            final String[] nameArray = StringUtils.split(student._name, "　");
            final String nameSeiField = getMS932ByteLength(nameArray[0]) > 12 ? "1_2" : "1_1";
            svf.VrsOutn("NAME" + nameSeiField, lineCnt, nameArray[0]);
            final String nameMeiField = getMS932ByteLength(nameArray[1]) > 12 ? "2_2" : "2_1";
            svf.VrsOutn("NAME" + nameMeiField, lineCnt, nameArray[1]);

            int subCnt = 1;
            for (Iterator itSubclass = student._scoreSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._scoreSubclassMap.get(subclassCd);
                svf.VrsOutn("POINT" + subCnt, lineCnt, subclass._score);
                subCnt++;
            }
            final Subclass scoreInterView = (Subclass) student._scoreInterViewMap.get("A");
            svf.VrsOutn("INTERVIEW", lineCnt, scoreInterView._score);
            svf.VrsOutn("POINT_ALL1", lineCnt, student._total4);
            svf.VrsOutn("POINT_ALL2", lineCnt, student._total2);

            subCnt = 1;
            for (Iterator itSubclass = student._devSubclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Subclass subclass = (Subclass) student._devSubclassMap.get(subclassCd);
                svf.VrsOutn("DEVI" + subCnt, lineCnt, subclass._dev);
                subCnt++;
            }
            final Subclass devInterView = (Subclass) student._devInterViewMap.get("A");
            svf.VrsOutn("DEVI5", lineCnt, devInterView._dev);
            svf.VrsOutn("DEVI6", lineCnt, student._deviation4);
            svf.VrsOutn("DEVI_ALL", lineCnt, student._deviation2);

            svf.VrsOutn("RANK1_1", lineCnt, student._totalRank4);
            svf.VrsOutn("RANK1_2", lineCnt, student._totalRank2);
            svf.VrsOutn("GOOD", lineCnt, student._goodTreat);
            svf.VrsOutn("KYOUDAI_SIMAI", lineCnt, student._kyoudai);
            noCnt++;
            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void printOutFrm316P_5(final Vrw32alp svf, final List studentList) {
        final int maxCnt = 15;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int lineCnt = 1;
        final int maxLine = 15;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            //満点は固定
            svf.VrsOut("PERFECT1", "(180)");
            svf.VrsOut("PERFECT2", "(100)");
            svf.VrsOut("PERFECT3", "(60)");
            svf.VrsOut("PERFECT4", "(60)");
            svf.VrsOut("PERFECT_ALL2", "(340)");
            svf.VrsOut("PERFECT_ALL3", "(400)");

            final Student student = (Student) itStudent.next();
            if (_param._isOutPutGohi) {
                svf.VrsOutn("JUDGE", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE1", lineCnt, student._judgeName);
                svf.VrsOutn("JUDGE2", lineCnt, student._judgeName);
            }
            svf.VrsOutn("EXAM_NO1", lineCnt, student._examNo);

            final String nameField = getMS932ByteLength(student._name) > 16 ? "1_2" : "1_1";
            svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
            final String kanaField = getMS932ByteLength(student._kana) > 12 ? "2_2" : "2_1";
            svf.VrsOutn("NAME" + kanaField, lineCnt, student._kana);
            final String finSchoolField = getMS932ByteLength(student._finSchoolName) > 12 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL" + finSchoolField, lineCnt, student._finSchoolName);

            svf.VrsOutn("POINT1", lineCnt, student._tyousasyo);
            svf.VrsOutn("POINT2", lineCnt, student._sakubun);
            svf.VrsOutn("POINT3", lineCnt, student._jisseki);
            svf.VrsOutn("RESULT1", lineCnt, student._jissekiJyoukyou);

            final Subclass scoreInterView = (Subclass) student._scoreInterViewMap.get("A");
            if (null != scoreInterView) {
                svf.VrsOutn("INTERVIEW", lineCnt, scoreInterView._score);
            }
            svf.VrsOutn("POINT_ALL1", lineCnt, student._total4);
            svf.VrsOutn("POINT_ALL2", lineCnt, student._total2);

            svf.VrsOutn("RANK1", lineCnt, student._totalRank4);
            svf.VrsOutn("RANK2", lineCnt, student._totalRank2);

            svf.VrsOutn("RANK3", lineCnt, student._classJuni);
            svf.VrsOutn("CLASS_NUM", lineCnt, student._classNinzu);

            svf.VrsOutn("NOTICE", lineCnt, student._kesseki);
            svf.VrsOutn("LATE", lineCnt, student._tikoku);
            svf.VrsOutn("EARLY", lineCnt, student._soutai);

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        final String mensetsu = MENSETU_NASHI.equals(_param._mensetu) ? "（面接なし）" : MENSETU_ARI.equals(_param._mensetu) ? "（面接あり）" : "";
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　試験結果" + mensetsu);
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));

        int subclassCnt = 1;
        for (Iterator itSubclass = _param._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
            svf.VrsOut("CLASS_NAME1_" + subclassCnt, subclass._subclassName);
            svf.VrsOut("CLASS_NAME2_" + subclassCnt, subclass._subclassName);
            if (null != subclass._avg) {
                svf.VrsOut("AVE" + subclassCnt, String.valueOf(subclass._avg));
            }
            subclassCnt++;
        }
        final Subclass interViewSubA = (Subclass) _param._interViewSubclassMap.get("A");
        final Subclass interViewSubB = (Subclass) _param._interViewSubclassMap.get("B");
        final Subclass interViewSubC = (Subclass) _param._interViewSubclassMap.get("C");
        if (null != interViewSubB) {
            svf.VrsOut("AVE_ALL", String.valueOf(interViewSubB._avg));
            svf.VrsOut("INTERVIEW_AVE1", String.valueOf(interViewSubB._avg));
        }
        if (null != interViewSubA) {
            svf.VrsOut("INTERVIEW_AVE", String.valueOf(interViewSubA._avg));
        }
        if (null != interViewSubC) {
            svf.VrsOut("INTERVIEW_AVE2", String.valueOf(interViewSubC._avg));
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = studentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String kana = rs.getString("NAME_KANA");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME");
                final String juku = rs.getString("JUKU");
                final String kyoushitsu = rs.getString("KYOUSHITSU");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String judgeName = rs.getString("JUDGENAME");
                final String total4 = rs.getString("TOTAL4");
                final String total2 = rs.getString("TOTAL2");
                final String deviation4 = rs.getString("DEVIATION4");
                final String deviation2 = rs.getString("DEVIATION2");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final String totalRank2 = rs.getString("TOTAL_RANK2");
                final String zenkiExamNo = rs.getString("ZENKI_EXAMNO");
                final String zenkiRank4 = rs.getString("ZENKI_RANK4");
                final String zenkiRank2 = rs.getString("ZENKI_RANK2");
                final String goodTreat = rs.getString("GOOD_TREAT");
                final String kyoudai = rs.getString("KYOUDAI");

                final Student student = new Student(receptNo, examNo, name, kana, finSchoolName, juku, kyoushitsu, judgeDiv, judgeName, total4, total2, deviation4, deviation2, totalRank4, totalRank2, zenkiExamNo, zenkiRank4, zenkiRank2, goodTreat, kyoudai);
                student.setScoreMap(db2);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String studentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     VALUE(PRI_M.PRISCHOOL_NAME, '') AS JUKU, ");
        stb.append("     VALUE(PRI_CM.PRISCHOOL_NAME, '') AS KYOUSHITSU, ");
        stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     L013.NAME1 AS JUDGENAME, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL2, ");
        stb.append("     RECEPT.JUDGE_DEVIATION AS DEVIATION4, ");
        stb.append("     RECEPT.LINK_JUDGE_DEVIATION AS DEVIATION2, ");
        stb.append("     RECEPT.TOTAL_RANK4, ");
        stb.append("     RECEPT.TOTAL_RANK2, ");
        stb.append("     BASE_D012.REMARK1 AS ZENKI_EXAMNO, ");
        stb.append("     RECEPT_ZENKI.TOTAL_RANK4 AS ZENKI_RANK4, ");
        stb.append("     RECEPT_ZENKI.TOTAL_RANK2 AS ZENKI_RANK2, ");
        stb.append("     CASE WHEN BASE_D014.REMARK1 = '1' THEN 'レ' ELSE '' END AS GOOD_TREAT, ");
        stb.append("     BASE_D014.REMARK2 AS KYOUDAI ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D008 ON RECEPT.ENTEXAMYEAR = BASE_D008.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE_D008.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE_D008.EXAMNO ");
        stb.append("          AND BASE_D008.SEQ = '008' ");
        stb.append("     LEFT JOIN PRISCHOOL_MST PRI_M ON BASE_D008.REMARK1 = PRI_M.PRISCHOOLCD ");
        stb.append("     LEFT JOIN PRISCHOOL_CLASS_MST PRI_CM ON BASE_D008.REMARK1 = PRI_CM.PRISCHOOLCD ");
        stb.append("          AND BASE_D008.REMARK3 = PRI_CM.PRISCHOOL_CLASS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D012 ON RECEPT.ENTEXAMYEAR = BASE_D012.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE_D012.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE_D012.EXAMNO ");
        stb.append("          AND BASE_D012.SEQ = '012' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D014 ON RECEPT.ENTEXAMYEAR = BASE_D014.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE_D014.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE_D014.EXAMNO ");
        stb.append("          AND BASE_D014.SEQ = '014' ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT_ZENKI ON RECEPT.ENTEXAMYEAR = RECEPT_ZENKI.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = RECEPT_ZENKI.APPLICANTDIV ");
        stb.append("          AND RECEPT_ZENKI.TESTDIV = '" + KENNAI_ZENKI + "' ");
        stb.append("          AND BASE_D012.REMARK1 = RECEPT_ZENKI.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._orderDiv)) {
            stb.append("     RECEPT.EXAMNO ");
        } else if ("1".equals(_param._mensetu)) {
            stb.append("     RECEPT.TOTAL_RANK4 ");
        } else {
            stb.append("     RECEPT.TOTAL_RANK2 ");
        }

        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58089 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String _entexamYear;
        final boolean _isOutPutGohi;
        final String _mensetu;
        final String _orderDiv;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _subclassMap;
        final Map _interViewSubclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _isOutPutGohi = "1".equals(request.getParameter("GOHI"));
            _mensetu = request.getParameter("MENSETU");
            _orderDiv = request.getParameter("ORDERDIV");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _subclassMap = getSubclassMap(db2);
            _interViewSubclassMap = getInterViewSubclassMap(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String subclassName = "1".equals(_applicantDiv) ? "NAME1" : "NAME2";
                final String sql = getNameMstSql(subclassName);
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nameCd2 = rs.getString("NAMECD2");
                    final String name = rs.getString(subclassName);
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    if (null != name && !"".equals(name)) {
                        final Subclass subclass = new Subclass(nameCd2, name, cnt, avg);
                        retMap.put(nameCd2, subclass);
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getNameMstSql(final String subclassName) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     L009.NAMECD2, ");
            stb.append("     L009." + subclassName + ", ");
            stb.append("     AVG_DAT.COUNT, ");
            stb.append("     AVG_DAT.AVARAGE_TOTAL AS AVG ");
            stb.append(" FROM ");
            stb.append("     NAME_MST L009 ");
            stb.append("     LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT AVG_DAT ON AVG_DAT.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("          AND AVG_DAT.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("          AND AVG_DAT.TESTDIV = '" + _testDiv + "' ");
            stb.append("          AND AVG_DAT.EXAM_TYPE = '1' ");
            stb.append("          AND L009.NAMECD2 = AVG_DAT.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     L009.NAMECD1 = 'L009' ");
            stb.append("     AND L009." + subclassName + " IS NOT NULL ");
            if ("1".equals(_applicantDiv) && "5".equals(_testDiv)) {
                stb.append("     AND L009.NAMECD2 IN (SELECT TESTSUBCLASSCD FROM ENTEXAM_PERFECT_MST  ");
                stb.append("                          WHERE ENTEXAMYEAR = '" + _entexamYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv +"'  ");
                stb.append("                          ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     L009.NAMECD2 ");
            return stb.toString();
        }

        private Map getInterViewSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getInterViewAvgSql();
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String nameCd2 = rs.getString("TESTSUBCLASSCD");
                    final String name = rs.getString("SUBCLASSNAME");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final Subclass subclass = new Subclass(nameCd2, name, cnt, avg);
                    retMap.put(nameCd2, subclass);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getInterViewAvgSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG_DAT.TESTSUBCLASSCD, ");
            stb.append("     CASE WHEN AVG_DAT.TESTSUBCLASSCD = 'A' ");
            stb.append("          THEN '面接' ");
            stb.append("          WHEN AVG_DAT.TESTSUBCLASSCD = 'B' ");
            stb.append("          THEN '面接なし' ");
            stb.append("          ELSE '面接あり' ");
            stb.append("     END AS SUBCLASSNAME, ");
            stb.append("     AVG_DAT.COUNT, ");
            stb.append("     AVG_DAT.AVARAGE_TOTAL AS AVG ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT AVG_DAT ");
            stb.append(" WHERE ");
            stb.append("     AVG_DAT.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("     AND AVG_DAT.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND AVG_DAT.TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND AVG_DAT.EXAM_TYPE = '1' ");
            stb.append("     AND AVG_DAT.TESTSUBCLASSCD IN ('A', 'B', 'C') ");

            return stb.toString();
        }

    }

    /** 生徒クラス */
    private class Student {
        final String _receptNo;
        final String _examNo;
        final String _name;
        final String _kana;
        final String _finSchoolName;
        final String _juku;
        final String _kyoushitsu;
        final String _judgeDiv;
        final String _judgeName;
        final String _total4;
        final String _total2;
        final String _deviation4;
        final String _deviation2;
        final String _totalRank4;
        final String _totalRank2;
        final String _zenkiExamNo;
        final String _zenkiRank4;
        final String _zenkiRank2;
        final String _goodTreat;
        final String _kyoudai;
        Map _scoreSubclassMap;
        Map _scoreInterViewMap;
        Map _devSubclassMap;
        Map _devInterViewMap;
        String _tyousasyo = "";
        String _sakubun = "";
        String _jisseki = "";
        String _jissekiJyoukyou = "";
        String _classNinzu = "";
        String _classJuni= "";
        String _kesseki = "";
        String _tikoku = "";
        String _soutai = "";

        public Student(
                final String receptNo,
                final String examNo,
                final String name,
                final String kana,
                final String finSchoolName,
                final String juku,
                final String kyoushitsu,
                final String judgeDiv,
                final String judgeName,
                final String total4,
                final String total2,
                final String deviation4,
                final String deviation2,
                final String totalRank4,
                final String totalRank2,
                final String zenkiExamNo,
                final String zenkiRank4,
                final String zenkiRank2,
                final String goodTreat,
                final String kyoudai
        ) throws SQLException {
            _receptNo = receptNo;
            _examNo = examNo;
            _name = name;
            _kana = kana;
            _finSchoolName = finSchoolName;
            _juku = juku;
            _kyoushitsu = kyoushitsu;
            _judgeDiv = judgeDiv;
            _judgeName = judgeName;
            _total4 = total4;
            _total2 = total2;
            _totalRank4 = totalRank4;
            _totalRank2 = totalRank2;
            _deviation4 = deviation4;
            _deviation2 = deviation2;
            _zenkiExamNo = zenkiExamNo;
            _zenkiRank4 = zenkiRank4;
            _zenkiRank2 = zenkiRank2;
            _goodTreat = goodTreat;
            _kyoudai = kyoudai;
            _scoreSubclassMap = setSubclassMap(_param._subclassMap);
            _scoreInterViewMap = setSubclassMap(_param._interViewSubclassMap);
            _devSubclassMap = setSubclassMap(_param._subclassMap);
            _devInterViewMap = setSubclassMap(_param._interViewSubclassMap);
        }

        private Map setSubclassMap(final Map subclassMap) {
            final Map retMap = new TreeMap();
            for (Iterator itSubMap = subclassMap.keySet().iterator(); itSubMap.hasNext();) {
                final String subKey = (String) itSubMap.next();
                final Subclass paraSub = (Subclass) subclassMap.get(subKey);
                final Subclass setSub = new Subclass(paraSub._subclassCd, paraSub._subclassName, paraSub._cnt, paraSub._avg);
                retMap.put(subKey, setSub);
            }
            return retMap;
        }

        public void setScoreMap(final DB2UDB db2) throws SQLException {
            setScoreData(db2, SCORE_DAT, _scoreSubclassMap, _devSubclassMap);
            setScoreData(db2, INTERVIEW_DAT, _scoreInterViewMap, _devInterViewMap);
            set316p_5Data(db2);
            log.debug("");
        }

        private void set316p_5Data(final DB2UDB db2) throws SQLException {

            PreparedStatement psTyousa = null;
            ResultSet rsTyousa = null;

            try {
                String sql = getTyousaSql();
                log.debug(" sql =" + sql);

                psTyousa = db2.prepareStatement(sql);
                rsTyousa = psTyousa.executeQuery();
                while (rsTyousa.next()) {
                    _tyousasyo = rsTyousa.getString("TYOUSA");
                    _sakubun = rsTyousa.getString("SAKUBUN");
                    _jisseki = rsTyousa.getString("JISSEKI");
                    _jissekiJyoukyou = rsTyousa.getString("JISSEKI_JYOUKYOU");
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psTyousa, rsTyousa);
            }

            PreparedStatement psClassInfo = null;
            ResultSet rsClassInfo = null;

            try {
                String sql = getClassInfoSql();
                log.debug(" sql =" + sql);

                psClassInfo = db2.prepareStatement(sql);
                rsClassInfo = psClassInfo.executeQuery();
                while (rsClassInfo.next()) {
                    _classNinzu = rsClassInfo.getString("REMARK1");
                    _classJuni = rsClassInfo.getString("REMARK2");
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psClassInfo, rsClassInfo);
            }

            PreparedStatement psSyukketsu = null;
            ResultSet rsSyukketsu = null;

            try {
                String sql = getSyukketsuSql();
                log.debug(" sql =" + sql);

                psSyukketsu = db2.prepareStatement(sql);
                rsSyukketsu = psSyukketsu.executeQuery();
                while (rsSyukketsu.next()) {
                    _kesseki = rsSyukketsu.getString("KESSEKI");
                    _tikoku = rsSyukketsu.getString("TIKOKU");
                    _soutai = rsSyukketsu.getString("SOUTAI");
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, psSyukketsu, rsSyukketsu);
            }

        }

        private String getTyousaSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CONFRPT.TOTAL_ALL AS TYOUSA, ");
            stb.append("     SAKUBUN.TOTAL AS SAKUBUN, ");
            stb.append("     JISSEKI.SCORE AS JISSEKI, ");
            stb.append("     JISSEKI.REMARK1 AS JISSEKI_JYOUKYOU ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON RECEPT.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = CONFRPT.APPLICANTDIV ");
            stb.append("          AND RECEPT.EXAMNO = CONFRPT.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_SAKUBUN_DAT SAKUBUN ON RECEPT.ENTEXAMYEAR = SAKUBUN.ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = SAKUBUN.APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = SAKUBUN.TESTDIV ");
            stb.append("          AND RECEPT.EXAMNO = SAKUBUN.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_JISSEKI_DAT JISSEKI ON RECEPT.ENTEXAMYEAR = JISSEKI.ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = JISSEKI.APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = JISSEKI.TESTDIV ");
            stb.append("          AND RECEPT.EXAMNO = JISSEKI.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND RECEPT.RECEPTNO = '" + _receptNo + "' ");
            return stb.toString();
        }

        private String getClassInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CONFRPT004.REMARK1, ");
            stb.append("     CONFRPT004.REMARK2 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT004 ");
            stb.append(" WHERE ");
            stb.append("     CONFRPT004.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND CONFRPT004.APPLICANTDIV= '" + _param._applicantDiv + "' ");
            stb.append("     AND CONFRPT004.EXAMNO = '" + _examNo + "' ");
            stb.append("     AND CONFRPT004.SEQ = '004' ");
            return stb.toString();
        }

        private String getSyukketsuSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CAST(VALUE(CONFRPT006.REMARK2, '0') AS INTEGER) + CAST(VALUE(CONFRPT007.REMARK2, '0') AS INTEGER) + CAST(VALUE(CONFRPT008.REMARK2, '0') AS INTEGER) AS KESSEKI, ");
            stb.append("     CAST(VALUE(CONFRPT006.REMARK3, '0') AS INTEGER) + CAST(VALUE(CONFRPT007.REMARK3, '0') AS INTEGER) + CAST(VALUE(CONFRPT008.REMARK3, '0') AS INTEGER) AS TIKOKU, ");
            stb.append("     CAST(VALUE(CONFRPT006.REMARK4, '0') AS INTEGER) + CAST(VALUE(CONFRPT007.REMARK4, '0') AS INTEGER) + CAST(VALUE(CONFRPT008.REMARK4, '0') AS INTEGER) AS SOUTAI ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT006 ON CONFRPT.ENTEXAMYEAR = CONFRPT006.ENTEXAMYEAR ");
            stb.append("          AND CONFRPT.APPLICANTDIV = CONFRPT006.APPLICANTDIV ");
            stb.append("          AND CONFRPT.EXAMNO = CONFRPT006.EXAMNO ");
            stb.append("          AND CONFRPT006.SEQ = '006' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT007 ON CONFRPT.ENTEXAMYEAR = CONFRPT007.ENTEXAMYEAR ");
            stb.append("          AND CONFRPT.APPLICANTDIV = CONFRPT007.APPLICANTDIV ");
            stb.append("          AND CONFRPT.EXAMNO = CONFRPT007.EXAMNO ");
            stb.append("          AND CONFRPT007.SEQ = '007' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT008 ON CONFRPT.ENTEXAMYEAR = CONFRPT008.ENTEXAMYEAR ");
            stb.append("          AND CONFRPT.APPLICANTDIV = CONFRPT008.APPLICANTDIV ");
            stb.append("          AND CONFRPT.EXAMNO = CONFRPT008.EXAMNO ");
            stb.append("          AND CONFRPT008.SEQ = '008' ");
            stb.append(" WHERE ");
            stb.append("     CONFRPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND CONFRPT.APPLICANTDIV= '" + _param._applicantDiv + "' ");
            stb.append("     AND CONFRPT.EXAMNO = '" + _examNo + "' ");

            return stb.toString();
        }

        private void setScoreData(final DB2UDB db2, final String tableDiv, final Map scoreMap, final Map devMap) throws SQLException {

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                String sql = "";
                if (SCORE_DAT.equals(tableDiv)) {
                    sql = getScoreSql();
                } else {
                    sql = getInterViewSql();
                }
                log.debug(" sql =" + sql);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String stdScore = rs.getString("STD_SCORE");
                    if (scoreMap.containsKey(subclassCd)) {
                        final Subclass subclass = (Subclass) scoreMap.get(subclassCd);
                        subclass._score = score;
                    }
                    if (devMap.containsKey(subclassCd)) {
                        final Subclass subclass = (Subclass) devMap.get(subclassCd);
                        subclass._dev = stdScore;
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getScoreSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCORE.TESTSUBCLASSCD, ");
            stb.append("     SCORE.SCORE, ");
            stb.append("     DECIMAL(ROUND(SCORE.STD_SCORE*10,0)/10,5,1) AS STD_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT SCORE ");
            stb.append(" WHERE ");
            stb.append("     SCORE.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND SCORE.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND SCORE.EXAM_TYPE = '1' ");
            stb.append("     AND SCORE.RECEPTNO = '" + _receptNo + "' ");
            return stb.toString();
        }

        private String getInterViewSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     'A' AS TESTSUBCLASSCD, ");
            stb.append("     INTERVIEW.SCORE, ");
            stb.append("     INTERVIEW.STD_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_INTERVIEW_DAT INTERVIEW ");
            stb.append(" WHERE ");
            stb.append("     INTERVIEW.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND INTERVIEW.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND INTERVIEW.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND INTERVIEW.EXAMNO = '" + _examNo + "' ");
            return stb.toString();
        }

    }

    /** 科目 */
    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final String _cnt;
        final String _avg;
        String _score;
        String _dev;

        public Subclass(
                final String subclassCd,
                final String subclassName,
                final String cnt,
                final String avg
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _cnt = cnt;
            _avg = avg;
        }
    }
}

// eof

