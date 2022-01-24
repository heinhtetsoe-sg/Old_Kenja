/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 5e8f4067f937df4898af1f0ba43961e7277e3928 $
 *
 * 作成日: 2019/06/17
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185C {

    private static final Log log = LogFactory.getLog(KNJD185C.class);

    private static final String SEMEALL = "9";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

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

        _hasData = false;
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);


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
        final List studentList = getList(db2);
        //下段の出欠
        for (final Iterator rit = _param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            Attendance.load(db2, _param, studentList, range);
        }
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //表紙
            printSvfHyoshi(db2, svf, student);

            //通知票
            printSvfMain(db2, svf, student);
        }
    }


    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	final String frmName;
    	if ("1".equals(_param._semester)) {
    		frmName = "KNJD185C_1_1.frm";
    	} else {
    		frmName = "KNJD185C_1_2.frm";
    	}
        svf.VrSetForm(frmName, 1);

        if(_param._semes3Flg) {

            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 18 ? "2" : "1";
            svf.VrsOut("NAME2_" + nameField, student._name); //氏名

        	svf.VrsOut("CERT_NAME", "修了証"); //修了証
            svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(student._gradeCd))); //学年
            final String date = KNJ_EditDate.h_format_SeirekiJP(_param._certDate);
            svf.VrsOut("DATE", date); //年度
            svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName); //学校名
            svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolJobName + " " + _param._certifSchoolPrincipalName); //校長名
            svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath); //捺印
        } else if ("2".equals(_param._semester)) {
            if (null != _param._whiteSpaceImagePath) {
                svf.VrsOut("BLANK", _param._whiteSpaceImagePath);
            }
        }

        final Semester semeswk = (Semester)_param._semesterMap.get(_param._semester);
        final String nendo = _param._loginYear + "年度" + semeswk._semestername;
        svf.VrsOut("NENDO", nendo); //年度
        svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath);
        final String studentImg = _param.getImageFilePath("P"+student._schregno + ".jpg");
        if (studentImg != null) {
            svf.VrsOut("PIC", studentImg); //生徒画像
        }
        svf.VrsOut("SCHOOL_NAME_ENG", _param._certifSchoolEngSchoolName); //英語の学校名称
        svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("HR_NAME", student._hrName + " " + String.valueOf(Integer.valueOf(student._attendno)) + "番"); //年組番
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "4" : KNJ_EditEdit.getMS932ByteLength(student._name) > 24 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 18 ? "2" : "1";
        svf.VrsOut("NAME1_" + nameField, student._name); //氏名
        final String pnameField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName.trim()) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName.trim()) > 18 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME1_" + pnameField, _param._certifSchoolPrincipalName.trim()); //校長名
        svf.VrsOut("STAFF_NAME" + pnameField, student._staffName); //校長名

        svf.VrEndPage();
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	final String frmName;
    	final boolean use56Flg;
    	if ("01".equals(student._gradeCd) || "02".equals(student._gradeCd)) {
    		frmName = "KNJD185C_2_1.frm";
    		use56Flg = false;
    	} else if ("03".equals(student._gradeCd) || "04".equals(student._gradeCd)) {
    		frmName = "KNJD185C_2_2.frm";
    		use56Flg = false;
    	} else {
    		frmName = "KNJD185C_2_3.frm";
    		use56Flg = true;
    	}

        svf.VrSetForm(frmName, 4);

        //明細部以外を印字
        printTitle(db2, svf, student);

//        //明細部
//        final List subclassList = subclassListRemoveD026();
//        Collections.sort(subclassList);

        int tblidx = 1;
        int colidx = 1;
        int outDetailMax = 6;

        for (Iterator itsc = student._printSubclassMap.keySet().iterator(); itsc.hasNext();) {
        	final String key = (String)itsc.next();
            final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(key);

            if (scoreData != null) {
                //教科名
                svf.VrsOut("CLASS_NAME" + colidx, StringUtils.defaultString(scoreData._classname, "")); //教科名
                //評定
                svf.VrsOut("VAL" + colidx, StringUtils.defaultString(scoreData.getScore(_param._semester), ""));
            }

            final List ViewList = (List)_param._jviewGradeMap.get(key);
            int restcnt = outDetailMax;
        	if (ViewList != null) {
                int idx = 1;
            	for (Iterator ite = ViewList.iterator();ite.hasNext();) {
            		JviewGrade jgWk = (JviewGrade)ite.next();
            		if (idx > 6) {
            			idx++;
            			continue;
            		}
            		final int vnlen = KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(jgWk._viewName, ""));
            		final String subIdxStr =  use56Flg ? (vnlen > 28 ? "_2" : "") : "";
            		final String baseIdxStr = use56Flg ? colidx + "_" : "";
            		//評価の観点
            		svf.VrsOut("VIEW_NAME" + baseIdxStr + idx + subIdxStr, StringUtils.defaultString(jgWk._viewName, ""));
            		//評価
            		if (student._printSubclsVScoreMap.containsKey(key)) {
            		    Map dGetWk = (Map)student._printSubclsVScoreMap.get(key);
            		    if (dGetWk.containsKey(jgWk._viewCd)) {
            			    SubclsVScore outWk = (SubclsVScore)dGetWk.get(jgWk._viewCd);
            			    svf.VrsOut("EVA" + baseIdxStr + idx, StringUtils.defaultString(outWk._status, ""));
            		    }
            		}

                    idx++;
                    restcnt--;
            	}
        	}
        	if (restcnt > 0) {
        		//空行出力
        	}
        	//5,6年は列の切替判定の上で改行。他はすぐ改行
        	if (use56Flg) {
        	    if (colidx == 1) {
        		    colidx = 2;
        	    } else {
        		    colidx = 1;
                	svf.VrEndRecord();
        	    }
        	} else {
            	svf.VrEndRecord();
        	}
        	tblidx++;
        }
    	if (use56Flg) {
    		if (colidx == 2) {
    			svf.VrEndRecord();
    		}
    	}

        svf.VrEndPage();
        _hasData = true;
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	//明細部以外を印字

        //ヘッダ,フッタ
        final Semester semeswk = (Semester)_param._semesterMap.get(_param._semester);
        final String nendo = _param._loginYear + "年度";
        svf.VrsOut("NENDO", nendo + "　" + semeswk._semestername); //タイトル
        svf.VrsOut("NAME", student._hrName + "：" + student._name); //氏名

        //■道徳
        final String cnField = ("05".equals(student._gradeCd) || "06".equals(student._gradeCd)) ? "3" : "2";
        svf.VrsOut("CLASS_NAME"+cnField, StringUtils.defaultString(_param._moralName, ""));
        List mlistwk = KNJ_EditKinsoku.getTokenList(student.getMoral(_param._semester), student.getDataInputKeta() * 2, student.getDataInputGyo());
        if (mlistwk != null && mlistwk.size() > 0 && null != (String) mlistwk.get(0)) {
            VrsOutnRenban(svf, "MORAL", mlistwk);
        }
        //■所見
        List rlistwk = KNJ_EditKinsoku.getTokenList(student.getRemark(_param._semester), 44 * 2, 7);
        if (rlistwk != null && rlistwk.size() > 0 && null != (String) rlistwk.get(0)) {
            VrsOutnRenban(svf, "COMM", rlistwk); //所見
        }

        //■出欠の記録
        printAttend(svf, student);
    }

//    private List subclassListRemoveD026() {
//        final List retList = new ArrayList(_param._subclassMstMap.values());
//        for (final Iterator it = retList.iterator(); it.hasNext();) {
//            final SubclassMst subclassMst = (SubclassMst) it.next();
//            if (_param._d026List.contains(subclassMst._subclasscd)) {
//                it.remove();
//            }
//            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
//                it.remove();
//            }
//        }
//        return retList;
//    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    // 出欠記録
    private void printAttend(final Vrw32alp svf, final Student student) {
        for (final Iterator it = _param._semesterMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next();
            final int line = getSemeLine(semester);
            final Attendance att = (Attendance) student._attendMap.get(semester);
            int lesson = 0;
            int suspend = 0;
            int absent = 0;
            int mourning = 0;
            int present = 0;
            int late = 0;
            int early = 0;
            if (null != att) {
                lesson = att._lesson;
                suspend = att._suspend;
                absent = att._absent;
                mourning = att._mourning;
                late = att._late;
                early = att._early;
            }
            if(line == 2 && !_param._semes2Flg) continue;
            if(line == 3 && !_param._semes3Flg) continue;
            svf.VrsOutn("LESSON", line, String.valueOf(lesson));     // 授業日数
            svf.VrsOutn("ABSENT", line, String.valueOf(absent));     // 欠席日数
            svf.VrsOutn("SUSPEND", line, String.valueOf(suspend));   // 出停日数
            svf.VrsOutn("MOURNING", line, String.valueOf(mourning)); // 忌引
            svf.VrsOutn("LATE", line, String.valueOf(late));         // 遅刻
            svf.VrsOutn("EARLY", line, String.valueOf(early));       // 早退
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 4;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            //log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String schoolKind = rs.getString("SCHOOL_KIND");
            	final String gradeHrClassCd = rs.getString("GRADE_HR_CLASS");
                final String grade = rs.getString("GRADE");
                final String gradeCd = StringUtils.defaultString(rs.getString("GRADE_CD"));
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
            	final String attendNo = rs.getString("ATTENDNO");
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String staffName = StringUtils.defaultString(rs.getString("STAFFNAME"));


//                final String moral1 = StringUtils.defaultString(rs.getString("MORAL1"));
//                final String moral2 = StringUtils.defaultString(rs.getString("MORAL2"));
//                final String moral3 = StringUtils.defaultString(rs.getString("MORAL3"));

                final String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));
                final String remark2 = StringUtils.defaultString(rs.getString("REMARK2"));
                final String remark3 = StringUtils.defaultString(rs.getString("REMARK3"));
                final Student student = new Student(schoolKind, gradeHrClassCd,grade,gradeCd,hrClass,hrName,attendNo,schregno,name,staffName,remark1,remark2,remark3);
                student.setSubclass(db2);
                student.setMoral(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.SCHREGNO, T1.ATTENDNO, T1.YEAR, T1.SEMESTER, T1.COURSECD, T1.MAJORCD ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        stb.append("        AND T1.SEMESTER = '"+_param._semester+"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._disp)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");

        //メイン表
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE || REGD.HR_CLASS AS GRADE_HR_CLASS, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     STAFF.STAFFNAME, ");
        //宗教・道徳
        stb.append("     HRDD1.REMARK1 AS MORAL1, ");
        stb.append("     HRDD2.REMARK1 AS MORAL2, ");
        stb.append("     HRDD3.REMARK1 AS MORAL3, ");
        //所見
        stb.append("     HRD1.COMMUNICATION AS REMARK1, ");
        stb.append("     HRD2.COMMUNICATION AS REMARK2, ");
        stb.append("     HRD3.COMMUNICATION AS REMARK3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
        stb.append("        ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("       ON REGD.YEAR     = REGDH.YEAR ");
        stb.append("      AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("      AND REGD.GRADE    = REGDH.GRADE ");
        stb.append("      AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("       ON REGD.YEAR  = GDAT.YEAR ");
        stb.append("      AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ");
        stb.append("       ON STAFF.STAFFCD = REGDH.TR_CD1 ");
        //1学期 宗教・道徳 , 所見
        stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD1 ");
        stb.append("       ON HRD1.YEAR     = REGD.YEAR ");
        stb.append("      AND HRD1.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      AND HRD1.SEMESTER = '1' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT HRDD1 ");
        stb.append("       ON HRDD1.YEAR     = HRD1.YEAR ");
        stb.append("      AND HRDD1.SCHREGNO = HRD1.SCHREGNO ");
        stb.append("      AND HRDD1.SEMESTER = HRD1.SEMESTER ");
        stb.append("      AND HRDD1.DIV = '01' ");
        stb.append("      AND HRDD1.CODE = '01' ");
        //2学期 宗教・道徳 , 所見
        stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD2 ");
        stb.append("       ON HRD2.YEAR     = REGD.YEAR ");
        stb.append("      AND HRD2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      AND HRD2.SEMESTER = '2' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT HRDD2 ");
        stb.append("       ON HRDD2.YEAR     = HRD2.YEAR ");
        stb.append("      AND HRDD2.SCHREGNO = HRD2.SCHREGNO ");
        stb.append("      AND HRDD2.SEMESTER = HRD2.SEMESTER ");
        stb.append("      AND HRDD2.DIV = '01' ");
        stb.append("      AND HRDD2.CODE = '01' ");
        //3学期 宗教・道徳 , 所見
        stb.append("     LEFT JOIN HREPORTREMARK_DAT HRD3 ");
        stb.append("       ON HRD3.YEAR     = REGD.YEAR ");
        stb.append("      AND HRD3.SCHREGNO = REGD.SCHREGNO ");
        stb.append("      AND HRD3.SEMESTER = '3' ");
        stb.append("     LEFT JOIN HREPORTREMARK_DETAIL_DAT HRDD3 ");
        stb.append("       ON HRDD3.YEAR     = HRD3.YEAR ");
        stb.append("      AND HRDD3.SCHREGNO = HRD3.SCHREGNO ");
        stb.append("      AND HRDD3.SEMESTER = HRD3.SEMESTER ");
        stb.append("      AND HRDD3.DIV = '01' ");
        stb.append("      AND HRDD3.CODE = '01' ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._loginYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        } else {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + _param._gradeHrClass + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        //final String sql = stb.toString();
        //log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        final String _gradeHrClass;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _schregno;
        final String _name;
        final String _staffName;

        String _moral1;
        String _moral2;
        String _moral3;
        String _dataInputSize;

        final String _remark1;
        final String _remark2;
        final String _remark3;

        final String _schoolKind;
        final String _attendno;

//        String _communication;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _printSubclsVScoreMap = new TreeMap();
//        final Map _attendSubClassMap = new HashMap();

        public Student(
        		final String schoolKind,
                final String gradeHrClass,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schregno,
                final String name,
                final String staffName,
                final String remark1,
                final String remark2,
                final String remark3
        		) {
        	_schoolKind = schoolKind;
        	_attendno = attendno;
            _gradeHrClass = gradeHrClass;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _schregno = schregno;
            _name = name;
            _staffName = staffName;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }
        private String getMoral(final String semester) {
        	String retStr = "";
        	if ("1".equals(_param._semester)) {
        		retStr = StringUtils.defaultString(_moral1, "");
        	} else if ("2".equals(_param._semester)) {
        		retStr = StringUtils.defaultString(_moral2, "");
        	} else if ("3".equals(_param._semester)) {
        		retStr = StringUtils.defaultString(_moral3, "");
        	}
        	return retStr;
        }
        private int getDataInputKeta() {
        	int retVal = 39; //デフォルト値
        	if ("".equals(_dataInputSize)) {
        		return retVal;
        	}
        	String cutwk[] = StringUtils.split(_dataInputSize, '*');
        	if (cutwk != null && cutwk.length < 2) {
        		return retVal;
        	}
        	retVal = Integer.parseInt(cutwk[0]);
    		return retVal;
        }
        private int getDataInputGyo() {
        	int retVal = 5; //デフォルト値
        	if ("".equals(_dataInputSize)) {
        		return retVal;
        	}
        	String cutwk[] = StringUtils.split(_dataInputSize, '*');
        	if (cutwk != null && cutwk.length < 2) {
        		return retVal;
        	}
        	retVal = Integer.parseInt(cutwk[1]);
    		return retVal;
        }
        private String getRemark(final String semester) {
        	String retStr = "";
        	if ("1".equals(_param._semester)) {
        		retStr = _remark1;
        	} else if ("2".equals(_param._semester)) {
        		retStr = _remark2;
        	} else if ("3".equals(_param._semester)) {
        		retStr = _remark3;
        	}
        	return retStr;
        }

        private void setMoral(final DB2UDB db2) {
            final String scoreSql = prestatementMoral();
            Map chkWkMap = new HashMap();
//          log.debug(" sql = " + scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            String delim = "";
            String datasize = "";
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
        	        final String semester = rs.getString("SEMESTER");
        	        datasize = rs.getString("DATA_SIZE");
        	        final String totalstudytime = rs.getString("TOTALSTUDYTIME");

        	        String concatwk = "";
        	        if (chkWkMap.containsKey(semester)) {
        	        	concatwk = (String)chkWkMap.get(semester);
        	        }
        	        concatwk += delim + StringUtils.defaultString(totalstudytime, "");
        	        delim = "".equals(concatwk) ? "" : " ";
        	        chkWkMap.put(semester, concatwk);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            if (chkWkMap.containsKey("1")) {
                _moral1 = (String)chkWkMap.get("1");
            } else {
            	_moral1 = "";
            }
            if (chkWkMap.containsKey("2")) {
                _moral2 = (String)chkWkMap.get("2");
            } else {
            	_moral2 = "";
            }
            if (chkWkMap.containsKey("3")) {
                _moral3 = (String)chkWkMap.get("3");
            } else {
            	_moral3 = "";
            }
            _dataInputSize = datasize;

        }

        private String prestatementMoral() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T2.SEMESTER, ");
            stb.append("   T1.DATA_SIZE, ");
            stb.append("   T2.TOTALSTUDYTIME ");
            stb.append(" FROM ");
            stb.append("   RECORD_TOTALSTUDYTIME_ITEM_MST T1 ");
            stb.append("   LEFT JOIN RECORD_TOTALSTUDYTIME_DAT T2 ");
            stb.append("     ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHOOL_KIND = '" + _param._schKind + "' ");
            stb.append("   AND T1.REMARK2 = '2' ");
            stb.append("   AND T2.YEAR = '" + _param._loginYear + "' ");
            stb.append("   AND T2.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append(" T2.SEMESTER, ");
            stb.append(" T2.SUBCLASSCD ");
            return stb.toString();
        }

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
//            log.debug(" sql = " + scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String classcd = rs.getString("CLASSCD");
                	final String classname = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                	final String schoolKind;
                	final String curriculum_Cd;
                    final String key;
                    if ("1".equals(_param._useCurriculumcd)) {
                        schoolKind = rs.getString("SCHOOL_KIND");
                        curriculum_Cd = rs.getString("CURRICULUM_CD");
                        key = classcd + "-" + schoolKind + "-" + curriculum_Cd + "-" + subclasscd;
                    } else {
                    	schoolKind = "";
                    	curriculum_Cd = "";
                    	key = subclasscd;
                    }
                    final String score1 = rs.getString("SCORE1");
                    final String score2 = rs.getString("SCORE2");
                    final String score3 = rs.getString("SCORE3");

                    if (!_printSubclassMap.containsKey(key)) {
                        ScoreData scoreData = new ScoreData(classcd, classname, schoolKind, curriculum_Cd, subclasscd, subclassname, score1, score2, score3);
                        _printSubclassMap.put(key, scoreData);
                        setSubclsVScore(db2, key);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUBCLASS AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T3.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append(" JVIEWNAME_GRADE_YDAT T1 ");
            stb.append(" INNER JOIN JVIEWNAME_GRADE_MST T4");
            stb.append("   ");
            stb.append("   ON T4.GRADE = T1.GRADE ");
            stb.append("  AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("  AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("  AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("  AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  AND T4.VIEWCD = T1.VIEWCD ");
            stb.append(" ,V_SUBCLASS_MST T2 ");
            stb.append(" ,V_CLASS_MST T3 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.YEAR = T3.YEAR ");
            stb.append("     AND T1.YEAR          = '" + _param._loginYear + "' ");
            stb.append("     AND T1.CLASSCD = T3.CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
            	stb.append("     AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND T1.CLASSCD       = T2.CLASSCD ");
                stb.append("     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
                stb.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("     AND T1.SUBCLASSCD    = T2.SUBCLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND   = '" + _param._schKind + "' ");
            stb.append("     AND T1.GRADE         = '" + _param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD ");
            stb.append(" ) ");

            //メイン取得
            stb.append(" SELECT DISTINCT ");
            stb.append("     SUB.CLASSCD, ");
            stb.append("     SUB.CLASSNAME, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     SUB.CURRICULUM_CD, ");
                stb.append("     SUB.SCHOOL_KIND, ");
            }
            stb.append("     SUB.SUBCLASSCD, ");
            stb.append("     SUB.SUBCLASSNAME, ");
            stb.append("     SCORE1.SCORE AS SCORE1, ");
            stb.append("     SCORE2.SCORE AS SCORE2, ");
            stb.append("     SCORE3.SCORE AS SCORE3 ");

            stb.append(" FROM ");
            stb.append("     SUBCLASS SUB ");
            //1学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE1 ");
            stb.append("       ON SCORE1.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE1.SEMESTER    = '1' ");
            stb.append("      AND SCORE1.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE1.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE1.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE1.SCHREGNO    = '" + _schregno + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      AND SCORE1.CLASSCD = SUB.CLASSCD ");
                stb.append("      AND SCORE1.SCHOOL_KIND = SUB.SCHOOL_KIND ");
                stb.append("      AND SCORE1.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            }
            stb.append("      AND SCORE1.SUBCLASSCD  = SUB.SUBCLASSCD ");
            //2学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE2 ");
            stb.append("       ON SCORE2.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE2.SEMESTER    = '2' ");
            stb.append("      AND SCORE2.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE2.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE2.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE2.SCHREGNO    = '" + _schregno + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      AND SCORE2.CLASSCD = SUB.CLASSCD ");
                stb.append("      AND SCORE2.SCHOOL_KIND = SUB.SCHOOL_KIND ");
                stb.append("      AND SCORE2.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            }
            stb.append("      AND SCORE2.SUBCLASSCD  = SUB.SUBCLASSCD ");
            //3学期 評定
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE3 ");
            stb.append("       ON SCORE3.YEAR        = SUB.YEAR ");
            stb.append("      AND SCORE3.SEMESTER    = '3' ");
            stb.append("      AND SCORE3.TESTKINDCD  = '99' ");
            stb.append("      AND SCORE3.TESTITEMCD  = '00' ");
            stb.append("      AND SCORE3.SCORE_DIV   = '08' ");
            stb.append("      AND SCORE3.SCHREGNO    = '" + _schregno + "' ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("      AND SCORE3.CLASSCD = SUB.CLASSCD ");
                stb.append("      AND SCORE3.SCHOOL_KIND = SUB.SCHOOL_KIND ");
                stb.append("      AND SCORE3.CURRICULUM_CD = SUB.CURRICULUM_CD ");
            }
            stb.append("      AND SCORE3.SUBCLASSCD  = SUB.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     SUB.CLASSCD, ");
            stb.append("     SUB.SUBCLASSCD ");

            return stb.toString();
        }

        private void setSubclsVScore(final DB2UDB db2, final String subClsCd) {
            final String sql = prestatementSubclsVScore(subClsCd);
//            log.debug(" setSubclsVScore = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String viewCd = rs.getString("VIEWCD");
                	final String status = rs.getString("STATUS");
                	final String score  = rs.getString("SCORE");

                	SubclsVScore addDataWk = new SubclsVScore(viewCd, status, score);
                	Map addwk;
                	if (!_printSubclsVScoreMap.containsKey(subClsCd)) {
                		addwk = new LinkedMap();
                	} else {
                		addwk = (Map)_printSubclsVScoreMap.get(subClsCd);
                	}
                	addwk.put(viewCd, addDataWk);
                	_printSubclsVScoreMap.put(subClsCd, addwk);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

        }

        private String prestatementSubclsVScore(final String subClsCd) {
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T1.VIEWCD, ");
        	stb.append("   T1.STATUS, ");
        	stb.append("   T1.SCORE ");
        	stb.append(" FROM ");
        	stb.append("   JVIEWSTAT_RECORD_DAT T1 ");
        	stb.append(" WHERE ");
        	stb.append("   T1.YEAR = '" + _param._loginYear + "' ");
        	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        	stb.append("   AND T1.SCHREGNO = '" +_schregno + "' ");
        	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subClsCd + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.VIEWCD ");
        	return stb.toString();
        }
    }

//    private class JviewRecord {
//        final String _subclassCd;
//        final String _semester;
//        final String _viewCd;
//        final String _status;
//        final String _statusName;
//        final String _score;
//        final String _hyouka;
//        private JviewRecord(
//                final String subclassCd,
//                final String semester,
//                final String viewCd,
//                final String status,
//                final String statusName,
//                final String score,
//                final String hyouka
//        ) {
//            _subclassCd = subclassCd;
//            _semester = semester;
//            _viewCd = viewCd;
//            _status = status;
//            _statusName = statusName;
//            _score = score;
//            _hyouka = hyouka;
//        }
//    }
//
//    private class RankSdiv {
//        final String _score;
//        final String _hyouka;
//        private RankSdiv(
//                final String score,
//                final String hyouka
//        ) {
//            _score = score;
//            _hyouka = hyouka;
//        }
//    }

    private class ScoreData {
    	private final String _classcd;
    	private final String _classname;
    	private final String _schoolkind;
    	private final String _curriculum_cd;
    	private final String _subclasscd;
    	private final String _subclassname;
        private final String _score1;
        private final String _score2;
        private final String _score3;
        private ScoreData(
                final String classcd,
                final String classname,
                final String schoolkind,
                final String curriculum_cd,
                final String subclasscd,
                final String subclassname,
                final String score1,
                final String score2,
                final String score3
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _schoolkind = schoolkind;
            _curriculum_cd = curriculum_cd;
            _score1 = score1;
            _score2 = score2;
            _score3 = score3;

        }
        private String getScore(final String semester) {
        	String retStr = "";
        	if ("1".equals(semester)) {
        		retStr = _score1;
        	} else if ("2".equals(semester)) {
        		retStr = _score2;
        	} else if ("3".equals(semester)) {
        		retStr = _score3;
        	} else {
        		retStr = _score3;
        	}
        	return retStr;
        }
        private String insertSp_ClassName(final int maxGyo, final int idx) {
        	String retStr = "";
        	String spStr = "";
        	if (maxGyo < _classname.length()) {
        		retStr = _classname.substring(idx, idx+1);
        	} else {
        	    int halfpt = (int)Math.floor(maxGyo / 2);
        	    int halfstr = (int)Math.floor(_classname.length() / 2);
        	    int strtpt = halfpt - halfstr;
        	    //出力開始位置か、判定
        	    if (strtpt <= idx && strtpt + _classname.length() <= idx) {
        	    	int subidx = idx - strtpt;
        	    	retStr = _classname.substring(subidx, subidx+1);
        	    } else {
        	    	//出力開始位置に満たない、または出力完了以降なら、空文字で返す。
        	    }
        	}
        	return retStr;
        }
    }

    private static class Attendance {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _abroad;
        final int _det006;
        final int _det007;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int abroad,
                final int det006,
                final int det007
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _abroad = abroad;
            _det006 = det006;
            _det007 = det007;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final List studentList,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;
            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            PreparedStatement psAtDetail = null;
            ResultSet rsAtDetail = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._loginYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
//                log.debug(" attend sql = " + sql);
                psAtSeme = db2.prepareStatement(sql);

                final String detailSql = getDetailSql(param, dateRange);
                psAtDetail = db2.prepareStatement(detailSql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    psAtDetail.setString(1, student._schregno);
                    psAtDetail.setString(2, student._schregno);
                    rsAtDetail = psAtDetail.executeQuery();

                    int set006 = 0;
                    int set007 = 0;
                    while (rsAtDetail.next()) {
                        set006 = rsAtDetail.getInt("CNT006");
                        set007 = rsAtDetail.getInt("CNT007");
                    }
                    DbUtils.closeQuietly(rsAtSeme);

                    psAtSeme.setString(1, student._schregno);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                rsAtSeme.getInt("LESSON"),
                                rsAtSeme.getInt("MLESSON"),
                                rsAtSeme.getInt("SUSPEND"),
                                rsAtSeme.getInt("MOURNING"),
                                rsAtSeme.getInt("SICK"),
                                rsAtSeme.getInt("PRESENT"),
                                rsAtSeme.getInt("LATE"),
                                rsAtSeme.getInt("EARLY"),
                                rsAtSeme.getInt("TRANSFER_DATE"),
                                set006,
                                set007
                        );
                        student._attendMap.put(dateRange._key, attendance);
                    }
                    DbUtils.closeQuietly(rsAtSeme);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }

        private static String getDetailSql(final Param param, final DateRange dateRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T(SCHREGNO) AS ( ");
            stb.append("     VALUES(CAST(? AS VARCHAR(8))) ");
            stb.append(" ), DET_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     COPYCD = '0' ");
            stb.append("     AND YEAR || MONTH BETWEEN '" + param._loginYear + "04' AND '" + (Integer.parseInt(param._loginYear) + 1) + "03' ");
            stb.append("     AND SEMESTER = '" + dateRange._key + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEQ IN ('006', '007') ");
            stb.append(" GROUP BY ");
            stb.append("     SEQ, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VALUE(DET006.CNT, 0) AS CNT006, ");
            stb.append("     VALUE(DET007.CNT, 0) AS CNT007 ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN DET_T DET006 ON SCH_T.SCHREGNO = DET006.SCHREGNO ");
            stb.append("          AND DET006.SEQ = '006' ");
            stb.append("     LEFT JOIN DET_T DET007 ON SCH_T.SCHREGNO = DET007.SCHREGNO ");
            stb.append("          AND DET007.SEQ = '007' ");

            return stb.toString();
        }

    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
        	if (!(o instanceof Semester)) {
        		return 0;
        	}
        	Semester s = (Semester) o;
        	return _semester.compareTo(s._semester);
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class JviewGrade {
        final String _subclassCd;
        final String _viewCd;
        final String _viewName;
        public JviewGrade(final String subclassCd, final String viewCd, final String viewName) {
            _subclassCd = subclassCd;
            _viewCd = viewCd;
            _viewName = viewName;
        }
    }

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
        	if (!(o instanceof SemesterDetail)) {
        		return 0;
        	}
    		SemesterDetail sd = (SemesterDetail) o;
    		int rtn;
        	rtn = _semester.compareTo(sd._semester);
        	if (rtn != 0) {
        		return rtn;
        	}
        	rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
        	return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private class SubclsVScore {
    	final String _viewCd;
    	final String _status;
    	final String _score;
    	SubclsVScore(final String viewCd, final String status, final String score) {
        	_viewCd = viewCd;
        	_status = status;
        	_score = score;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75444 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final String _schKind;
        final String _moralName;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolEngSchoolName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _subclassMstMap;
        private final Map _jviewGradeMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private final String _useCurriculumcd;

        private final String _use_school_detail_gcm_dat;
        private final List _attendTestKindItemList;
        private final List _attendSemesterDetailList;
        private String _certDate;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _semes2Flg = 2 <= Integer.parseInt(_semester);
            _semes3Flg = 3 <= Integer.parseInt(_semester);
            _certDate = request.getParameter("CERTIF_DATE");
            _schKind = "P";

            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");


            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);
            _jviewGradeMap = getJviewGradeMap(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");

            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();
            _moralName = getMoralName(db2);
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _loginYear + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getJviewGradeMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     YDAT.CLASSCD || '-' || YDAT.SCHOOL_KIND || '-' || YDAT.CURRICULUM_CD || '-' || YDAT.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     MST.VIEWCD, ");
            stb.append("     MST.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_YDAT YDAT ");
            stb.append("     LEFT JOIN JVIEWNAME_GRADE_MST MST ");
            stb.append("       ON MST.GRADE = YDAT.GRADE ");
            stb.append("      AND MST.CLASSCD = YDAT.CLASSCD ");
            stb.append("      AND MST.SCHOOL_KIND = YDAT.SCHOOL_KIND ");
            stb.append("      AND MST.CURRICULUM_CD = YDAT.CURRICULUM_CD ");
            stb.append("      AND MST.SUBCLASSCD = YDAT.SUBCLASSCD ");
            stb.append("      AND MST.VIEWCD = YDAT.VIEWCD ");
            stb.append(" WHERE ");
            stb.append("     YDAT.YEAR = '" + _loginYear + "' ");
            stb.append("     AND YDAT.GRADE       = '" + _grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VIEWCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                List jviewGradeList = null;
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = rs.getString("VIEWNAME");

                    final JviewGrade jviewGrade = new JviewGrade(subclassCd, viewCd, viewName);
                    if (retMap.containsKey(subclassCd)) {
                        jviewGradeList = (List) retMap.get(subclassCd);
                    } else {
                        jviewGradeList = new ArrayList();
                    }
                    jviewGradeList.add(jviewGrade);
                    retMap.put(subclassCd, jviewGradeList);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '117' ");
//            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
            _certifSchoolEngSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "REMARK6"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _loginYear+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                }
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T11.GRADE = T1.GRADE ");
                    stb.append("    AND T11.COURSECD = T1.COURSECD ");
                    stb.append("    AND T11.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _loginYear + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("    AND T1.GRADE = '00' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

//                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }

        private String getMoralName(final DB2UDB db2) {
            final String sql = "SELECT "
                                 + " CASE WHEN CLASSORDERNAME2 IS NULL THEN CLASSNAME ELSE CLASSORDERNAME2 END AS CLASSNAME "
            		             + " FROM CLASS_MST WHERE CLASSCD = '93' AND SCHOOL_KIND = '" + _schKind + "'";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            return KnjDbUtils.getString(row, "CLASSNAME");
        }

    }
}

// eof
