/*
 * $Id: ebc5a168012c29117d17d4816ea52fdf4b7afc9a $
 *
 * 作成日: 2018/12/25
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL320R {

    private static final Log log = LogFactory.getLog(KNJL320R.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL320R.frm", 1);

        final List printStudentList = getList(db2);
        final int listSize = printStudentList.size();

        int thisPage = 1;
        final int maxLine = 50;
        final int maxPage = (int)Math.ceil((double)listSize / (double)maxLine);
        setTitle(db2, svf, maxPage, thisPage);
        int lineCnt = 1;
        for (Iterator iterator = printStudentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (lineCnt > maxLine) {
                svf.VrEndPage();
                lineCnt = 1;
                thisPage++;
                setTitle(db2, svf, maxPage, thisPage);
            }
            //NO
            svf.VrsOutn("NO", lineCnt, student._dNo);
            //EXAM_NO1
            svf.VrsOutn("EXAM_NO1", lineCnt, student._receptNo);
            //NAME1/2(>14)
            final int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 14 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
            //KANA
            svf.VrsOutn("KANA", lineCnt, student._nameKana);
            //SEX
            svf.VrsOutn("SEX", lineCnt, student._sexName);
            //BIRTHDAY
            svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(db2, student._birthday));
            //FINSCHOOL_NAME
            if (KNJ_EditEdit.getMS932ByteLength(student._fsName) <= 20) {
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, student._fsName);
            } else if (KNJ_EditEdit.getMS932ByteLength(student._fsName) <= 25) {
                svf.VrsOutn("FINSCHOOL_NAME2", lineCnt, student._fsName);
            } else {
                svf.VrsOutn("FINSCHOOL_NAME3", lineCnt, student._fsName);
            }
            final String psName = StringUtils.defaultString(student._psName) + StringUtils.defaultString(student._psClassName);
            if (KNJ_EditEdit.getMS932ByteLength(psName) <= 20) {
                svf.VrsOutn("PRISCHOOL_NAME", lineCnt, psName);
            } else if (KNJ_EditEdit.getMS932ByteLength(psName) <= 25) {
                svf.VrsOutn("PRISCHOOL_NAME2", lineCnt, psName);
            } else {
                svf.VrsOutn("PRISCHOOL_NAME3", lineCnt, psName);
            }
            //SHDIV
            svf.VrsOutn("SHDIV", lineCnt, student._shDiv);
            if ("1".equals(_param._testDiv)) {
                //SCORE1
                svf.VrsOutn("SCORE1", lineCnt, student._s1_1_score);
                //SCORE2
                svf.VrsOutn("SCORE2", lineCnt, student._s1_2_score);
                //SCORE3
                svf.VrsOutn("SCORE3", lineCnt, student._s1_4_score);
                //SCORE4
                svf.VrsOutn("SCORE4", lineCnt, student._s1_5_score);
            } else {
                //SCORE1
                svf.VrsOutn("SCORE1", lineCnt, student._s1_6_score);
                //SCORE2
                svf.VrsOutn("SCORE2", lineCnt, student._s1_7_score);
                //SCORE3
            }
            //TOTAL1
            svf.VrsOutn("TOTAL1", lineCnt, student._totalA);
            //TOTAL2
            svf.VrsOutn("TOTAL2", lineCnt, student._totalB);
            //ADD1
            svf.VrsOutn("ADD1", lineCnt, student._katen1);
            //ADD2
            svf.VrsOutn("ADD2", lineCnt, student._katen2);
            //RANK
            svf.VrsOutn("RANK", lineCnt, student._totalRankB);
            //INTERVIEW
            svf.VrsOutn("INTERVIEW", lineCnt, student._interviewVal);
            //JUDGE1
            final String judge1Field = KNJ_EditEdit.getMS932ByteLength(student._jDivName) > 6 ? "_2" : "";
            svf.VrsOutn("JUDGE1" + judge1Field, lineCnt, student._jDivName);
            //CONSENT
            svf.VrsOutn("CONSENT", lineCnt, student._naidaku);
            //EXAM_NO2
            svf.VrsOutn("EXAM_NO2", lineCnt, student._otherReceptNo);
            //JUDGE2
            final String judge2Field = KNJ_EditEdit.getMS932ByteLength(student._otherJDivName) > 6 ? "_2" : "";
            svf.VrsOutn("JUDGE2" + judge2Field, lineCnt, student._otherJDivName);
            //REMARK1
            svf.VrsOutn("REMARK1", lineCnt, student._remark1);
            //REMARK2
            //svf.VrsOutn("REMARK2", lineCnt, student._remark2);
            //NOTICE
            svf.VrsOutn("NOTICE", lineCnt, student._notice);

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int maxPage, final int pageNo) {
    	//TITLE
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01") + "度　" + _param._applicantName + "入試判定会議資料");
        final String subTitle = "1".equals(_param._sortType) ? "-受験番号順-" : "-成績順-";
        final String setKaten = "on".equals(_param._incKansan) ? " (加点あり) " : " (加点なし)";
        final String setSubTitle = subTitle + ("2".equals(_param._sortType) ? setKaten : "");
        svf.VrsOut("SUBTITLE", _param._testdivName + setSubTitle);
        //SUBTITLE
        final Calendar cal = Calendar.getInstance();
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._ctrlDate, 0) + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        //DATE
        svf.VrsOut("DATE", printDateTime);
        //PAGE1/PAGE2
        svf.VrsOut("PAGE1", String.valueOf(pageNo));
        svf.VrsOut("PAGE2", String.valueOf(maxPage));

        //入試区分によって、表の項目名称の出力を分ける。
        if ("1".equals(_param._testDiv)) {
        	//国語,数学,理科社会
        	setTblTtlStr(svf, "1", "CLASS_NAME1");
        	setTblTtlStr(svf, "2", "CLASS_NAME2");
        	setTblTtlStr(svf, "4", "CLASS_NAME3");
        	setTblTtlStr(svf, "5", "CLASS_NAME4");
        } else {
        	//適正1/適正2,(3個目は空き)
        	setTblTtlStr(svf, "6", "CLASS_NAME1");
        	setTblTtlStr(svf, "7", "CLASS_NAME2");
        }
        //(選択した入試区分とは別の)入試区分名称
        svf.VrsOut("EXAM_NAME", _param._otherTestdivName);
    }

    private void setTblTtlStr(final Vrw32alp svf, final String srchMapStr, final String fieldBaseStr) {
    	final String classNameStr = (String)_param._subclsMap.get(srchMapStr);
    	final int classNameLen = KNJ_EditEdit.getMS932ByteLength(classNameStr);
    	if (classNameLen > 6) {
    		//2/3
            svf.VrsOut(fieldBaseStr + "_2", StringUtils.substring(classNameStr, 0, 2)); //3文字まで
            svf.VrsOut(fieldBaseStr + "_3", StringUtils.substring(classNameStr, 2));    //3文字目以降
    	} else {
    		//1
            svf.VrsOut(fieldBaseStr + "_1", classNameStr);
    	}
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String dNo = rs.getString("NO");
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sexName = rs.getString("SEX_NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final String fsName = rs.getString("FINSCHOOL_NAME");
                final String psName = rs.getString("PRISCHOOL_NAME");
                final String psClassName = rs.getString("PRISCHOOL_CLASS_NAME");
                final String shDiv = rs.getString("SHDIV");
                final String s1_1_score = rs.getString("S1_1_SCORE");
                final String s1_2_score = rs.getString("S1_2_SCORE");
                final String s1_4_score = rs.getString("S1_4_SCORE");
                final String s1_5_score = rs.getString("S1_5_SCORE");
                final String s1_6_score = rs.getString("S1_6_SCORE");
                final String s1_7_score = rs.getString("S1_7_SCORE");
                final String katen1 = rs.getString("KATEN1");
                final String katen2 = rs.getString("KATEN2");
                final String totalA = rs.getString("TOTAL_A");
                final String totalB = rs.getString("TOTAL_B");
                final String totalRankA = rs.getString("TOTAL_RANK_A");
                final String totalRankB = rs.getString("TOTAL_RANK_B");
                final String interviewVal = rs.getString("INTERVIEW_VALUE");
                final String innerPromise = rs.getString("INNER_PROMISE");
                final String otherReceptNo = rs.getString("OTHER_RECEPTNO");
                final String jDiv = rs.getString("JUDGEDIV");
                final String jDivName = rs.getString("JDIVNAME");
                final String otherJDiv = rs.getString("OTHER_JUDGEDIV");
                final String otherJDivName = rs.getString("OTHER_JDIVNAME");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String naidaku = rs.getString("NAIDAKU");
                final String notice = rs.getString("NOTICE");

                final Student student = new Student(dNo, receptNo, name, nameKana, sexName, birthday, fsName, psName, psClassName, shDiv, s1_1_score, s1_2_score,
                		                              s1_4_score, s1_5_score, s1_6_score, s1_7_score, katen1, katen2, totalA, totalB, totalRankA, totalRankB, interviewVal,
                		                              innerPromise, otherReceptNo, jDiv, jDivName, otherJDiv, otherJDivName, remark1, remark2,
                		                              naidaku, notice);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  row_number() over( ");
        stb.append(" ORDER BY ");
        // ソート順が成績順の場合に色々変化。傾斜配点/加算点含むの選択によって4パターン。
        if (!"1".equals(_param._sortType)) {
        	if ("on".equals(_param._incKansan)) {
        		if ("1".equals(_param._outKeisya)) {
        			//傾斜配点する
                    stb.append("  VALUE(R1.TOTAL3, -1) DESC, ");
        		} else {
        			//傾斜配点しない
                    stb.append("  VALUE(R1.TOTAL1, -1) DESC, ");
        		}
        	} else {
        		if ("1".equals(_param._outKeisya)) {
        			//傾斜配点する
                    stb.append("  VALUE(R1.TOTAL4, -1) DESC, ");
        		} else {
        			//傾斜配点しない
                    stb.append("  VALUE(R1.TOTAL2, -1) DESC, ");
        		}
        	}
        }
        stb.append("  R1.RECEPTNO, ");
        stb.append("  V1.EXAMNO ");
        stb.append("  ) AS NO, ");
        stb.append("  R1.RECEPTNO, ");
        stb.append("  B1.NAME, ");
        stb.append("  B1.NAME_KANA, ");
        stb.append("  B1.BIRTHDAY, ");
        stb.append("  FM.FINSCHOOL_NAME, ");
        stb.append("  FM.FINSCHOOL_NAME_ABBV, ");
        stb.append("  PM.PRISCHOOL_NAME, ");
//        stb.append("  PCM.PRISCHOOL_NAME AS PRISCHOOL_CLASS_NAME, ");
        stb.append("  '' AS PRISCHOOL_CLASS_NAME, ");
        if ("1".equals(_param._testDiv)) {
            stb.append("  CASE WHEN BD2_013.REMARK1 = '1' THEN '○' ELSE '' END AS SHDIV, ");
        }else {
            stb.append("  CASE WHEN BD2_013.REMARK2 = '1' THEN '○' ELSE '' END AS SHDIV, ");
        }
		if ("1".equals(_param._outKeisya)) {
			//傾斜配点する
            stb.append("  S1_1.SCORE2 AS S1_1_SCORE, ");
            stb.append("  S1_2.SCORE2 AS S1_2_SCORE, ");
            stb.append("  S1_4.SCORE2 AS S1_4_SCORE, ");
            stb.append("  S1_5.SCORE2 AS S1_5_SCORE, ");
            stb.append("  S1_6.SCORE2 AS S1_6_SCORE, ");
            stb.append("  S1_7.SCORE2 AS S1_7_SCORE, ");
            stb.append("  R1.TOTAL4 AS TOTAL_A, ");
            stb.append("  R1.TOTAL3 AS TOTAL_B, ");
            stb.append("  R1.TOTAL_RANK4 AS TOTAL_RANK_A, ");
            stb.append("  R1.TOTAL_RANK3 AS TOTAL_RANK_B, ");
		} else {
			//傾斜配点しない
            stb.append("  S1_1.SCORE AS S1_1_SCORE, ");
            stb.append("  S1_2.SCORE AS S1_2_SCORE, ");
            stb.append("  S1_4.SCORE AS S1_4_SCORE, ");
            stb.append("  S1_5.SCORE AS S1_5_SCORE, ");
            stb.append("  S1_6.SCORE AS S1_6_SCORE, ");
            stb.append("  S1_7.SCORE AS S1_7_SCORE, ");
            stb.append("  R1.TOTAL2 AS TOTAL_A, ");
            stb.append("  R1.TOTAL1 AS TOTAL_B, ");
            stb.append("  R1.TOTAL_RANK2 AS TOTAL_RANK_A, ");
            stb.append("  R1.TOTAL_RANK1 AS TOTAL_RANK_B, ");
		}
        stb.append(" RD1.REMARK1 AS KATEN1, ");
        stb.append(" RD1.REMARK2 AS KATEN2, ");
        stb.append("  INT1.INTERVIEW_VALUE, ");
        stb.append("  R1.JUDGEDIV, ");
        stb.append("  L013.NAME1 AS JDIVNAME, ");
        stb.append("  Z002.ABBV1 AS SEX_NAME, ");
        stb.append("  VB1.INNER_PROMISE, ");                 // ★VB1のテーブルは無い
        stb.append("  VB1_R.RECEPTNO AS OTHER_RECEPTNO, ");  // ★VB1のテーブルは無い
        stb.append("  R1_R.JUDGEDIV AS OTHER_JUDGEDIV, ");
        stb.append("  L013R.NAME1 AS OTHER_JDIVNAME, ");
        stb.append("  B1.REMARK1, ");
        stb.append("  B1.REMARK2, ");
        stb.append("  VALUE(NML064.NAME1, '') AS NAIDAKU, ");
        stb.append("  VALUE(INTEGER(BD1_006.REMARK5),0) + VALUE(INTEGER(BD1_006.REMARK6),0) AS NOTICE ");
        stb.append(" FROM ");
        stb.append("  V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V1");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("    ON B1.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND B1.APPLICANTDIV = V1.APPLICANTDIV ");
        stb.append("   AND B1.EXAMNO = V1.EXAMNO ");
        stb.append("  LEFT JOIN FINSCHOOL_MST FM ");
        stb.append("    ON FM.FINSCHOOLCD = B1.FS_CD ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DAT R1 ");
        stb.append("    ON R1.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND R1.APPLICANTDIV = V1.APPLICANTDIV ");
        stb.append("   AND R1.TESTDIV = V1.TESTDIV ");
        stb.append("   AND R1.EXAMNO = V1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_1 ");
        stb.append("    ON S1_1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_1.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_1.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_1.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_1.TESTSUBCLASSCD = '1' ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_2 ");
        stb.append("    ON S1_2.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_2.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_2.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_2.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_2.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_2.TESTSUBCLASSCD = '2' ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_4 ");
        stb.append("    ON S1_4.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_4.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_4.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_4.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_4.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_4.TESTSUBCLASSCD = '4' ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_5 ");
        stb.append("    ON S1_5.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_5.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_5.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_5.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_5.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_5.TESTSUBCLASSCD = '5' ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_6 ");
        stb.append("    ON S1_6.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_6.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_6.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_6.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_6.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_6.TESTSUBCLASSCD = '6' ");
        stb.append("  LEFT JOIN ENTEXAM_SCORE_DAT S1_7 ");
        stb.append("    ON S1_7.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND S1_7.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND S1_7.TESTDIV = R1.TESTDIV ");
        stb.append("   AND S1_7.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND S1_7.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND S1_7.TESTSUBCLASSCD = '7' ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD1 ");
        stb.append("    ON RD1.ENTEXAMYEAR = R1.ENTEXAMYEAR ");
        stb.append("   AND RD1.APPLICANTDIV = R1.APPLICANTDIV ");
        stb.append("   AND RD1.TESTDIV = R1.TESTDIV ");
        stb.append("   AND RD1.EXAM_TYPE = R1.EXAM_TYPE ");
        stb.append("   AND RD1.RECEPTNO = R1.RECEPTNO ");
        stb.append("   AND RD1.SEQ = '008' ");
        stb.append("  LEFT JOIN ENTEXAM_INTERVIEW_DAT INT1 ");
        stb.append("    ON INT1.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND INT1.APPLICANTDIV = V1.APPLICANTDIV ");
        stb.append("   AND INT1.TESTDIV = V1.TESTDIV ");
        stb.append("   AND INT1.EXAMNO = V1.EXAMNO ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DAT R1_R ");
        stb.append("    ON R1_R.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND R1_R.APPLICANTDIV = V1.APPLICANTDIV ");
        //常に反対のTESTDIVで取得(2の時は1、1の時は2)
        if ("1".equals(_param._testDiv)) {
            stb.append("   AND R1_R.TESTDIV = '2' ");
        } else {
            stb.append("   AND R1_R.TESTDIV = '1' ");
        }
        stb.append("   AND R1_R.EXAMNO = V1.EXAMNO ");
        stb.append("  LEFT JOIN NAME_MST L013 ");
        stb.append("    ON L013.NAMECD1 = 'L013' ");
        stb.append("   AND L013.NAMECD2 = R1.JUDGEDIV ");
        stb.append("  LEFT JOIN NAME_MST L013R ");
        stb.append("    ON L013R.NAMECD1 = 'L013' ");
        stb.append("   AND L013R.NAMECD2 = R1_R.JUDGEDIV ");
        stb.append("  LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VB1 ");
        stb.append("    ON VB1.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND VB1.APPLICANTDIV = V1.APPLICANTDIV ");
        stb.append("   AND VB1.EXAMNO = V1.EXAMNO ");
        stb.append("   AND VB1.TESTDIV = V1.TESTDIV ");
        stb.append("  LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VB1_R ");
        stb.append("    ON VB1_R.ENTEXAMYEAR = V1.ENTEXAMYEAR ");
        stb.append("   AND VB1_R.APPLICANTDIV = V1.APPLICANTDIV ");
        stb.append("   AND VB1_R.EXAMNO = V1.EXAMNO ");
        //常に反対のTESTDIVで取得(2の時は1、1の時は2)
        if ("1".equals(_param._testDiv)) {
            stb.append("   AND VB1_R.TESTDIV = '2' ");
        } else {
            stb.append("   AND VB1_R.TESTDIV = '1' ");
        }
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_013 ON BD2_013.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
        stb.append("   AND BD2_013.APPLICANTDIV = B1.APPLICANTDIV  ");
        stb.append("   AND BD2_013.EXAMNO       = B1.EXAMNO  ");
        stb.append("   AND BD2_013.SEQ          = '013'  ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BD2_014 ON BD2_014.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
        stb.append("   AND BD2_014.APPLICANTDIV = B1.APPLICANTDIV  ");
        stb.append("   AND BD2_014.EXAMNO       = B1.EXAMNO  ");
        stb.append("   AND BD2_014.SEQ          = '014'  ");
        if("1".equals(_param._testDiv)) {
            stb.append("     LEFT JOIN NAME_MST NML064  ");
            stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK1 ");
            stb.append("         AND NML064.NAMECD1 = 'L064'  ");
        }else {
            stb.append("     LEFT JOIN NAME_MST NML064  ");
            stb.append("          ON NML064.NAMECD2 = BD2_014.REMARK2 ");
            stb.append("         AND NML064.NAMECD1 = 'L064'  ");
        }
        stb.append("  LEFT JOIN NAME_MST Z002 ");
        stb.append("          ON Z002.NAMECD2 = B1.SEX ");
        stb.append("         AND Z002.NAMECD1 = 'Z002' ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD08 ON BD08.ENTEXAMYEAR = B1.ENTEXAMYEAR ");
        stb.append("   AND BD08.APPLICANTDIV    = B1.APPLICANTDIV  ");
        stb.append("   AND BD08.EXAMNO          = B1.EXAMNO  ");
        stb.append("   AND BD08.SEQ             = '008'  ");
        stb.append("  LEFT JOIN PRISCHOOL_MST PM ");
        stb.append("    ON PM.PRISCHOOLCD = BD08.REMARK1 ");
//        stb.append("  LEFT JOIN PRISCHOOL_CLASS_MST PCM ON PCM.PRISCHOOLCD = B1.PRISCHOOLCD ");
//        stb.append("   AND PCM.PRISCHOOL_CLASS_CD = BD08.REMARK1 ");
        stb.append("  LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT BD1_006 ON BD1_006.ENTEXAMYEAR = B1.ENTEXAMYEAR  ");
        stb.append("   AND BD1_006.APPLICANTDIV = B1.APPLICANTDIV  ");
        stb.append("   AND BD1_006.EXAMNO       = B1.EXAMNO  ");
        stb.append("   AND BD1_006.SEQ          = '006' ");
        stb.append(" WHERE ");
        stb.append("  V1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("  AND V1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("  AND V1.TESTDIV = '" + _param._testDiv + "' ");
        //欠席者以外
        //stb.append("  AND NOT(VALUE(R1.JUDGEDIV, '') = '3' OR VALUE(R1.JUDGEDIV, '') = '4') ");
        stb.append(" ORDER BY ");
        // ソート順が成績順の場合に色々変化。傾斜配点/加算点含むの選択によって4パターン。
        if (!"1".equals(_param._sortType)) {
        	if ("on".equals(_param._incKansan)) {
        		if ("1".equals(_param._outKeisya)) {
        			//傾斜配点する
                    stb.append("  VALUE(R1.TOTAL3, -1) DESC, ");
        		} else {
        			//傾斜配点しない
                    stb.append("  VALUE(R1.TOTAL1, -1) DESC, ");
        		}
        	} else {
        		if ("1".equals(_param._outKeisya)) {
        			//傾斜配点する
                    stb.append("  VALUE(R1.TOTAL4, -1) DESC, ");
        		} else {
        			//傾斜配点しない
                    stb.append("  VALUE(R1.TOTAL2, -1) DESC, ");
        		}
        	}
        }
        stb.append("  R1.RECEPTNO, ");
        stb.append("  V1.EXAMNO ");
        return stb.toString();
    }

    private class Student {
    	final String _dNo;
    	final String _receptNo;
    	final String _name;
    	final String _nameKana;
    	final String _sexName;
    	final String _birthday;
    	final String _fsName;
    	final String _psName;
    	final String _psClassName;
    	final String _shDiv;
    	final String _s1_1_score;
    	final String _s1_2_score;
    	final String _s1_4_score;
    	final String _s1_5_score;
    	final String _s1_6_score;
    	final String _s1_7_score;
    	final String _katen1;
    	final String _katen2;
    	final String _totalA;
    	final String _totalB;
    	final String _totalRankA;
    	final String _totalRankB;
    	final String _interviewVal;
    	final String _innerPromise;
    	final String _otherReceptNo;
    	final String _jDiv;
    	final String _jDivName;
    	final String _otherJDiv;
    	final String _otherJDivName;
    	final String _remark1;
    	final String _remark2;
    	final String _naidaku;
    	final String _notice;
        public Student(
        		final String dNo, final String receptNo, final String name, final String nameKana, final String sexName, final String birthday,
        		final String fsName, final String psName, final String psClassName, final String shDiv, final String s1_1_score, final String s1_2_score,
        		final String s1_4_score, final String s1_5_score, final String s1_6_score, final String s1_7_score, final String katen1, final String katen2,
        		final String totalA, final String totalB, final String totalRankA, final String totalRankB, final String interviewVal, final String innerPromise, final String otherReceptNo,
        		final String jDiv, final String jDivName, final String otherJDiv, final String otherJDivName,
        		final String remark1, final String remark2, final String naidaku, final String notice
        ) {
        	_dNo = dNo;
        	_receptNo = receptNo;
        	_name = name;
        	_nameKana = nameKana;
        	_sexName = sexName;
        	_birthday = birthday;
        	_fsName = fsName;
        	_psName = psName;
        	_psClassName = psClassName;
        	_shDiv = shDiv;
        	_s1_1_score = s1_1_score;
        	_s1_2_score = s1_2_score;
        	_s1_4_score = s1_4_score;
        	_s1_5_score = s1_5_score;
        	_s1_6_score = s1_6_score;
        	_s1_7_score = s1_7_score;
        	_katen1 = katen1;
        	_katen2 = katen2;
        	_totalA = totalA;
        	_totalB = totalB;
        	_totalRankA = totalRankA;
        	_totalRankB = totalRankB;
        	_interviewVal = interviewVal;
        	_innerPromise = innerPromise;
        	_otherReceptNo = otherReceptNo;
        	_jDiv = jDiv;
        	_jDivName = jDivName;
        	_otherJDiv = otherJDiv;
        	_otherJDivName = otherJDivName;
        	_remark1 = remark1;
        	_remark2 = remark2;
        	_naidaku = naidaku;
        	_notice = notice;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65233 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamyear;
        final String _applicantDiv;
        final String _testDiv;
        final String _sortType;
        final String _outKeisya;
        final String _incKansan;
        final String _ctrlDate;
        final String _applicantName;
        final String _testdivName;
        final String _otherTestdivName;
        final Map _subclsMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _outKeisya = request.getParameter("OUTKEISYA");
            _sortType = request.getParameter("OUTPUT");
            _incKansan = request.getParameter("INC_KASAN");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            if ("2".equals(_applicantDiv)) {
                _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L024", _testDiv));
                if ("1".equals(_testDiv)) {
                    _otherTestdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L024", "2"));
                } else {
                    _otherTestdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L024", "1"));
                }
            } else {
                _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
                if ("1".equals(_testDiv)) {
                    _otherTestdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", "2"));
                } else {
                    _otherTestdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", "1"));
                }
            }
            _subclsMap = getMapNameMst(db2, "NAME2", "L009");
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map getMapNameMst(final DB2UDB db2, final String field, final String namecd1) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
