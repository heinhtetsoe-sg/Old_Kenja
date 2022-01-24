/*
 * $Id: 98e131bb1f69ccd236cd31db3069a02ec639fd78 $
 *
 * 作成日: 2012/12/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * 学習状況通知
 */
public class KNJM500W {

    private static final Log log = LogFactory.getLog(KNJM500W.class);

    private static DecimalFormat zero = new DecimalFormat("00");
    private static DecimalFormat space = new DecimalFormat("##");

    private static final String SEITO = "1";
    private static final String SONOTA = "2";
    private static final String TSUUNEN = "0";
    private static final String HOGOSYA_MONGON = "保護者・保証人　様";

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

    private static String substringMS932(final String name, final int bytelength) {
        StringBuffer stb = new StringBuffer();
        if (null != name) {
            int maxlen = bytelength;
            try {
                for (int i = 0; i < name.length(); i++) {
                    final String sb = name.substring(i, i + 1);
                    maxlen -= sb.getBytes("MS932").length;
                    if (maxlen < 0) {
                        break;
                    }
                    stb.append(sb);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return stb.toString();
    }

    private static String subtract(final String name, final String sub) {
        if (null == name || null == sub || -1 == name.indexOf(sub)) {
            return null;
        }
        return name.substring(sub.length());
    }

    private String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final int month = Integer.parseInt(date.substring(5, 7));
        final int day = Integer.parseInt(date.substring(8));
        return space.format(month) + "/" + zero.format(day);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int TOUKOU = 1;
        final int HOUSOU = 2;
        final int TEISHUTSUBI = 3;
        final int HENSOUBI = 4;
        final int HYOUKA = 5;
        final int repCntMax = 15;

        final List<Student> list = Student.getStudentList(db2, _param);
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";

        for (final Student student : list) {

            if (_param._isHyoushiPrint) {
                printHyoushi(svf, student);
            }
            final String form = "KNJM500W.frm";
            svf.VrSetForm(form, 4);

			final String title = nendo + "　" + _param._schoolName1 + "　学習状況通知書";
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
            final String stdNameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "";
            svf.VrsOut("NAME" + stdNameField, student._name); // 氏名
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._kijun) + "現在"); // 日付

            svf.VrsOut("ATTEND_TOTAL", String.valueOf(student._totalToukou)); // 出校日数
            svf.VrsOut("SP_TOTAL", StringUtils.defaultString(student._totalYearCreditTime, "0") + "／" + StringUtils.defaultString(student._baseRemark2Name1)); // 特別活動時数合計
            svf.VrsOut("LAST_CREDIT", StringUtils.defaultString(student._beforeYearCreditTime, "0")); // 前年度までの計
            svf.VrsOut("THIS_YEAR_TOTAL", StringUtils.defaultString(student._thisYearCreditTime, "0")); // 今年度の計
            svf.VrsOut("LAST_YEAR_TOTAL", StringUtils.defaultString(student._beforeYearCredit)); // 前年度の計
            svf.VrsOut("GET_TOTAL", String.valueOf(student._allGetCredit)); // 修得済単位

            for (int i = 1; i <= 6; i++) {
                final String remarkId = String.valueOf(i);
                final String remark = _param._hreportRemarkTDat.get(remarkId);
                svf.VrsOut("TEXT" + remarkId, remark); // 文言
            }

            boolean printrecord = false;
            for (final Subclass subclass : student._subclassList) {

                if (!_param._takesemes.equals(subclass._takeSemes) && !TSUUNEN.equals(subclass._takeSemes)) {
                    continue;
                }
                final String subclassname = StringUtils.defaultString(subclass._subclassname);
                final int line1len = 9 * 2;
                if (KNJ_EditEdit.getMS932ByteLength(subclassname) <= line1len) {
                    svf.VrsOut("SUBCLASS_NAME2_1", subclassname); // 科目名
                } else {
                    int spaceIdx = subclassname.indexOf(" ");
                    if (-1 == spaceIdx) {
                        spaceIdx = subclassname.indexOf("　");
                    }
                    if (-1 == spaceIdx || 8 < spaceIdx) {
                        svf.VrsOut("SUBCLASS_NAME2_2", substringMS932(subclassname, line1len)); // 科目名
                        svf.VrsOut("SUBCLASS_NAME2_3", subtract(subclassname, substringMS932(subclassname, line1len))); // 科目名
                    } else {
                        svf.VrsOut("SUBCLASS_NAME2_2", subclassname.substring(0, spaceIdx)); // 科目名
                        if (spaceIdx <= subclassname.length()) {
                            svf.VrsOut("SUBCLASS_NAME2_3", subclassname.substring(spaceIdx + 1)); // 科目名
                        }
                    }
                }

                svf.VrsOut("CREDIT", subclass._credits);
                svf.VrsOut("SCH_REP_MARK1", "Ｓ"); // スクーリングorレポート
                svf.VrsOut("SCH_REP_MARK2", "Ｒ"); // スクーリングorレポート
                svf.VrsOut("REG_COUNT1", subclass._schSeqMin); // 規定数
                svf.VrsOut("REG_COUNT2", subclass._repSeqAll); // 規定数


                svf.VrsOutn("COUNT_NAME", TOUKOU, "登校");
                svf.VrsOutn("COUNT_NAME", HOUSOU, "放送");
                svf.VrsOutn("COUNT_NAME", TEISHUTSUBI, "提出日");
                svf.VrsOutn("COUNT_NAME", HENSOUBI, "返送日");
                svf.VrsOutn("COUNT_NAME", HYOUKA, "評価");

                final String toukou = StringUtils.defaultString(subclass.getAttendCount(false), "0");
                svf.VrsOut("COUNT1", toukou); // 出校回数
                final String housou = StringUtils.defaultString(subclass.getAttendCount(true), "0");
                svf.VrsOut("COUNT2", housou); // 通信回数

                final int totalRep = Integer.parseInt(toukou) + Integer.parseInt(housou);
                svf.VrsOut("REP_TOTAL_S", String.valueOf(totalRep) + "/" + StringUtils.defaultString(subclass._schSeqMin, "0"));

                final List<String> toukouList = subclass.getAttendList(false);
                for (int ati = 0; ati < toukouList.size(); ati++) {
                    final String date = toukouList.get(ati);
                    if (ati < repCntMax) {  //15回以降は出力しない。
                        svf.VrsOutn("REP" + (ati + 1), TOUKOU, formatDate(date)); // 登校
                    }
                }
                final List<String> housouList = subclass.getAttendList(true);
                for (int ati = 0; ati < housouList.size(); ati++) {
                    final String date = housouList.get(ati);
                    if (ati < repCntMax) {  //15回以降は出力しない。
                        svf.VrsOutn("REP" + (ati + 1), HOUSOU, formatDate(date)); // 放送
                    }
                }

                int count = 0;

                // log.debug(" kijun = " + _param._kijun + " (" + subclass._subclasscd + " : " + subclass._subclassname + ")");
                for (int repi = 0; repi < subclass._reportList.size(); repi++) {
                    final Report report = subclass._reportList.get(repi);
                    if (!NumberUtils.isDigits(report._standardSeq)) {
                        continue;
                    }
                    final int i = Integer.parseInt(report._standardSeq);
                    final String teishutubi = formatDate(report._receiptDate);
                    final String hensouMark = NumberUtils.isDigits(report._representSeq) && Integer.parseInt(report._representSeq) >= 1 ? "*" : "";

                    if (null == report._repStandardSeq) { // レポートが未提出
                        final String hyouka;
                        if (null == report._standardDate) {
                            // log.debug(" standardSeq = " + report._standardSeq + ":  _ standardDate = " + report._standardDate + "");
                            hyouka = null;
                        } else if (report._standardDate.compareTo(_param._kijun) < 0) { // 未提出 && 提出期限を超えた
                            // log.debug(" standardSeq = " + report._standardSeq + ":  x standardDate = " + report._standardDate + "");
                            hyouka = "×";
                        } else { // 未提出 && 提出期限を超えていない
                            // log.debug(" standardSeq = " + report._standardSeq + ":  n standardDate = " + report._standardDate + "");
                            hyouka = null;
                        }
                        svf.VrsOutn("REP" + i, TEISHUTSUBI, teishutubi); // 提出日
                        svf.VrsOutn("REP" + i, HENSOUBI, hensouMark); // 返送日
                        svf.VrsOutn("REP" + i, HYOUKA, hyouka); // 評価
                    } else if (null == report._gradValue ||
                                null != report._gradDate && _param._kijun.compareTo(report._gradDate) < 0
                            ) {
                        // log.debug(" standardSeq = " + report._standardSeq + ":  r receiiptDate = " + report._receiptDate + "");
                        svf.VrsOutn("REP" + i, TEISHUTSUBI, teishutubi); // 提出日
                        svf.VrsOutn("REP" + i, HENSOUBI, hensouMark); // 返送日
                        svf.VrsOutn("REP" + i, HYOUKA, "受"); // 評価
                    } else {
                        // log.debug(" standardSeq = " + report._standardSeq + ":  g gradValue = " + report._gradValueName + "");
                        svf.VrsOutn("REP" + i, TEISHUTSUBI, teishutubi); // 提出日
                        svf.VrsOutn("REP" + i, HENSOUBI, formatDate(report._gradDate)+ hensouMark); // 返送日
                        svf.VrsOutn("REP" + i, HYOUKA, report._gradValueName); // 評価
                        if ("1".equals(report._namespare1)) {
                            count += 1;
                        }
                    }
                }

                svf.VrsOut("GET_CREDIT", subclass._getCredit); // 修得
                svf.VrsOut("COMP_CREDIT", subclass._compCredit); // 履修
                svf.VrsOut("VAL", subclass._hyotei); // 評定
                final List<String> testcdList = subclass.getTestcdList();
                boolean isPrint2ndOnly = false;
                if (testcdList.size() == 1) {
                	final String testcd0 = testcdList.get(0);
                	final String[] split = testcd0.split("-");
                	final String testsemester = split[0];
					final String testkindcd = split[1];
                	if ("02".equals(testkindcd)) {
                		// 期末テストのみ
                        if ("1".equals(subclass._takeSemes) && "1".equals(testsemester)) {
                        	// 前期科目で前期期末テストのみ
                        	isPrint2ndOnly = true;
                        } else if ("2".equals(subclass._takeSemes) && "2".equals(testsemester)) {
                        	// 後期科目で後期期末テストのみ
                        	isPrint2ndOnly = true;
                        } else if (TSUUNEN.equals(subclass._takeSemes) && "2".equals(testsemester)) {
                        	// 通年科目で通年期末テストのみ
                        	isPrint2ndOnly = true;
                        }
                	}
                }
                // log.info(" subclass " + subclass._subclasscd + ", takesemes = " + subclass._takeSemes + ", testcd = " + testcdList + ", isPrint2ndOnly = " + isPrint2ndOnly);
                int locStart = 1;
                if (isPrint2ndOnly) {
                	locStart = 2;
                }
                for (int loc = locStart, ti = 0; loc <= 2 && ti < testcdList.size(); loc++, ti++) {
                	final String testcd = testcdList.get(ti);
                	final String score = subclass._testScoreMap.get(testcd);
                	svf.VrsOut("TEST" + String.valueOf(loc) + "_S", score); // テスト
                	if (null != score) {
                		final String gouhi = Integer.parseInt(score) >= Integer.parseInt(_param._passScore) ? "合" : "否";
                		svf.VrsOut("TEST" + String.valueOf(loc) + "_R", gouhi); // テスト
                	}
                }
                
                svf.VrsOut("COUNT4", String.valueOf(count)); // レポート合格回数
                svf.VrsOut("REP_TOTAL_R", String.valueOf(count) + "/" + StringUtils.defaultString(subclass._repSeqAll, "0"));

                svf.VrEndRecord();
                printrecord = true;
            }
            if (!printrecord) {
            	for (int i = 0; i < 8; i++) {
            		svf.VrsOut("SUBCLASS_NAME2_1", ""); // 科目名
            		svf.VrEndRecord();
            	}
            }
            _hasData = true;
        }
    }

    private void printHyoushi(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJM500W_ADDR.frm", 1);
        svf.VrsOut("ZIPCODE1", student._hyoushiZipCd);

        final int addr1Len = KNJ_EditEdit.getMS932ByteLength(student._hyoushiAddr1);
        final int addr2Len = KNJ_EditEdit.getMS932ByteLength(student._hyoushiAddr2);
        final int addrLen = addr1Len > addr2Len ? addr1Len : addr2Len;
        final String addrField = addrLen > 50 ? "3" : addrLen > 40 ? "2" : "1";
        svf.VrsOut("ADDRESS1_1_" + addrField, student._hyoushiAddr1);
        svf.VrsOut("ADDRESS1_2_" + addrField, student._hyoushiAddr2);

        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._hyoushiName) > 40 ? "2" : "1";
        svf.VrsOut("NAME1_" + nameField, student._hyoushiName);

        if (_param._isPrintHogosya) {
            final String gardNameField = KNJ_EditEdit.getMS932ByteLength(HOGOSYA_MONGON) > 40 ? "2" : "1";
            svf.VrsOut("GUARD_NAME1_" + gardNameField, HOGOSYA_MONGON);
        }

        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("SCHREGNO", student._schregno);
        svf.VrEndPage();
    }

    private static String getDispNum(final BigDecimal bd) {
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Student {
        final String _schregno;
        final String _inoutcd;
        final String _hrName;
        final String _name;
        final String _baseRemark1;
        final String _thisYearCreditTime;
        final String _beforeYearCreditTime;
        final String _totalYearCreditTime;
        final String _baseRemark2Name1;
        final String _beforeYearCredit;

        String _hyoushiZipCd;
        String _hyoushiAddr1;
        String _hyoushiAddr2;
        String _hyoushiName;

        int _totalToukou = 0;
        int _allGetCredit = 0;
        final List<Subclass> _subclassList = new ArrayList();
        final Map<String, Subclass> _subclassMap = new HashMap();

        Student(
                final String schregno,
                final String inoutcd,
                final String hrName,
                final String name,
                final String baseRemark1,
                final String thisYearCreditTime,
                final String beforeYearCreditTime,
                final String totalYearCreditTime,
                final String baseRemark2Name1,
                final String beforeYearCredit
        ) {
            _schregno = schregno;
            _inoutcd = inoutcd;
            _hrName = hrName;
            _name = name;
            _baseRemark1 = baseRemark1;
            _thisYearCreditTime = thisYearCreditTime;
            _beforeYearCreditTime = beforeYearCreditTime;
            _totalYearCreditTime = totalYearCreditTime;
            _baseRemark2Name1 = baseRemark2Name1;
            _beforeYearCredit = beforeYearCredit;
        }

        private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final List<Student> list = new ArrayList();
            final Map<String, Student> studentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
            	final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String inoutcd = rs.getString("INOUTCD");
                        final String name = rs.getString("NAME");
                        final String hrName = rs.getString("HR_NAME");
                        final String baseRemark1 = rs.getString("BASE_REMARK1");

                        final String thisYearCreditTime = null == rs.getString("THIS_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("THIS_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("THIS_YEAR_CREDIT_TIME"));
                        final String beforeYearCreditTime = null == rs.getString("BEFORE_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("BEFORE_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("BEFORE_YEAR_CREDIT_TIME"));
                        final String totalYearCreditTime = null == rs.getString("TOTAL_YEAR_CREDIT_TIME") || !NumberUtils.isNumber(rs.getString("TOTAL_YEAR_CREDIT_TIME")) ? null : getDispNum(rs.getBigDecimal("TOTAL_YEAR_CREDIT_TIME"));
                        final String baseRemark2Name1 = StringUtils.defaultString(rs.getString("BASE_REMARK2_NAME1"));
                        final String beforeYearCredit = rs.getString("BEFORE_YEAR_CREDIT");

                        final Student student = new Student(schregno, inoutcd, hrName, name, baseRemark1, thisYearCreditTime, beforeYearCreditTime, totalYearCreditTime, baseRemark2Name1, beforeYearCredit);
                        student.setAddr(db2, param);
                        list.add(student);
                        studentMap.put(schregno, student);
                    }
                    final Student student = studentMap.get(schregno);

                    if (null != rs.getString("CLASSCD") && Integer.parseInt(rs.getString("CLASSCD")) <= 90) {
                    	final String year = rs.getString("YEAR");
                    	final String chaircd = rs.getString("CHAIRCD");
                    	final String takeSemes = rs.getString("TAKESEMES");
                    	final String subclassname = rs.getString("SUBCLASSNAME");
                    	final String repSeqAll = rs.getString("REP_SEQ_ALL");
                    	final String schSeqAll = rs.getString("SCH_SEQ_ALL");
                    	final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                    	final String credits = rs.getString("CREDITS");

                    	final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-"  + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    	final Subclass subclass = new Subclass(year, takeSemes, chaircd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin, credits);
                    	student._subclassList.add(subclass);
                    	student._subclassMap.put(subclass._subclasscd, subclass);
                    }
                }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }

            try {
                ps = db2.prepareStatement(getSchAttendSql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = studentMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-"  + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    final Subclass subclass = student._subclassMap.get(subclasscd);
                    if (null == subclass) {
                        continue;
                    }
                    subclass._attendList.add(new Attend(rs.getString("SCHOOLINGKINDCD"), rs.getString("NAMESPARE1"), rs.getString("EXECUTEDATE"), rs.getString("PERIODCD"), rs.getBigDecimal("CREDIT_TIME")));
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }

           try {
               final String reportSql = getReportSql(param);
               ps = db2.prepareStatement(reportSql);
               rs = ps.executeQuery();
               while (rs.next()) {
                   final Student student = studentMap.get(rs.getString("SCHREGNO"));
                   if (null == student) {
                       continue;
                   }
                   final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-"  + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                   final Subclass subclass = student._subclassMap.get(subclasscd);
                   if (null == subclass) {
                       continue;
                   }
                   subclass._reportList.add(new Report(rs.getString("STANDARD_SEQ"), rs.getString("STANDARD_DATE"), rs.getString("TESTCD"), rs.getString("REP_STANDARD_SEQ"), rs.getString("NAMESPARE1"), rs.getString("REPRESENT_SEQ"), rs.getString("RECEIPT_DATE"), rs.getString("GRAD_DATE"), rs.getString("GRAD_VALUE"), rs.getString("GRAD_VALUE_NAME")));
               }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }

           try {
               final String sql = getSchAttendToukouAll(param);
               ps = db2.prepareStatement(sql);
               rs = ps.executeQuery();
               while (rs.next()) {
                   final Student student = studentMap.get(rs.getString("SCHREGNO"));
                   if (null == student) {
                       continue;
                   }
                   student._totalToukou += Integer.parseInt(StringUtils.defaultString(rs.getString("TOUKOU_CNT"), "0"));
               }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }

           try {
               final String sql = getStudyRecCredit(param);
               ps = db2.prepareStatement(sql);
               rs = ps.executeQuery();
               while (rs.next()) {
                   final Student student = studentMap.get(rs.getString("SCHREGNO"));
                   if (null == student) {
                       continue;
                   }
                   student._allGetCredit += Integer.parseInt(StringUtils.defaultString(rs.getString("GET_CREDIT"), "0"));
               }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }

           try {
               final String sql = getRecordScoreHistDatSql(param);
               ps = db2.prepareStatement(sql);
               rs = ps.executeQuery();
               while (rs.next()) {
                   final Student student = studentMap.get(rs.getString("SCHREGNO"));
                   if (null == student) {
                       continue;
                   }
                   final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-"  + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                   final Subclass subclass = student._subclassMap.get(subclasscd);
                   if (null == subclass) {
                       continue;
                   }
                   final String testcd = rs.getString("SEMESTER") + "-" + rs.getString("TESTKINDCD") + "-" + rs.getString("TESTITEMCD") + "-" + rs.getString("SCORE_DIV");
                   if ("9-99-00-09".equals(testcd)) {
                       subclass._getCredit = rs.getString("GET_CREDIT");
                       subclass._compCredit = rs.getString("COMP_CREDIT");
                       subclass._hyotei = rs.getString("VALUE");
                       student._allGetCredit += Integer.parseInt(StringUtils.defaultString(rs.getString("GET_CREDIT"), "0"));
                   }
                   subclass._testScoreMap.put(testcd, rs.getString("SCORE"));
               }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SPECIAL_ATTEND AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO,  ");
            stb.append("         VALUE(CASE WHEN YEAR = '" + param._year + "' THEN 'THIS' ELSE 'BEFORE' END, 'TOTAL') AS DIV, ");
            stb.append("         SUM(CREDIT_TIME) AS CREDIT_TIME ");
            stb.append("     FROM ");
            stb.append("         SPECIALACT_ATTEND_DAT ");
            stb.append("     WHERE ");
            stb.append("         (YEAR < '" + param._year + "' OR YEAR = '" + param._year + "' AND ATTENDDATE <= '" + param._kijun + "') ");
            stb.append("         AND CLASSCD IN ('93', '94') ");
            stb.append("     GROUP BY ");
            stb.append("         GROUPING SETS ( ");
            stb.append("             (SCHREGNO), ");
            stb.append("             (SCHREGNO, ");
            stb.append("              CASE WHEN YEAR = '" + param._year + "' THEN 'THIS' ELSE 'BEFORE' END)) ");
            stb.append(" ) ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         REGD.YEAR, ");
            stb.append("         REGD.SCHREGNO, ");
            stb.append("         BASE.INOUTCD, ");
            stb.append("         BASE.NAME, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         CHAIR.CHAIRCD, ");
            stb.append("         CHAIR.TAKESEMES, ");
            stb.append("         CHAIR.CLASSCD, ");
            stb.append("         CHAIR.SCHOOL_KIND, ");
            stb.append("         CHAIR.CURRICULUM_CD, ");
            stb.append("         CHAIR.SUBCLASSCD, ");
            stb.append("         SUBM.SUBCLASSNAME, ");
            stb.append("         COR.REP_SEQ_ALL, ");
            stb.append("         COR.SCH_SEQ_ALL, ");
            stb.append("         COR.SCH_SEQ_MIN, ");
            stb.append("         CREDM.CREDITS, ");
            stb.append("         T7.BASE_REMARK1, ");
            stb.append("         T9.CREDIT_TIME AS THIS_YEAR_CREDIT_TIME, ");
            stb.append("         T10.CREDIT_TIME AS BEFORE_YEAR_CREDIT_TIME, ");
            stb.append("         T11.CREDIT_TIME AS TOTAL_YEAR_CREDIT_TIME, ");
            stb.append("         M013.NAME1 AS BASE_REMARK2_NAME1, ");
            stb.append("         CASE WHEN STBEF.ADD_CREDIT IS NULL THEN STBEF.GET_CREDIT ELSE VALUE(STBEF.GET_CREDIT, 0) + VALUE(STBEF.ADD_CREDIT, 0) END AS BEFORE_YEAR_CREDIT ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("         AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("         AND REGD.GRADE = REGDH.GRADE ");
            stb.append("         AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND T9 ON T9.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND T9.DIV = 'THIS' ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND T10 ON T10.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND T10.DIV = 'BEFORE' ");
            stb.append("     LEFT JOIN SPECIAL_ATTEND T11 ON T11.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND T11.DIV = 'TOTAL' ");
            stb.append("     LEFT JOIN SCHREG_BASE_DETAIL_MST T12 ON T12.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND T12.BASE_SEQ = '004' ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND T7.YEAR = REGD.YEAR ");
            stb.append("         AND T7.BASE_SEQ = '001' ");
            stb.append("     LEFT JOIN NAME_MST M013 ON M013.NAMECD1 = 'M013' ");
            stb.append("         AND M013.NAMECD2 = T12.BASE_REMARK2 ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, SUM(GET_CREDIT) AS GET_CREDIT, SUM(ADD_CREDIT) AS ADD_CREDIT ");
            stb.append("                FROM SCHREG_STUDYREC_DAT ");
            stb.append("                WHERE YEAR < '" + param._year + "' ");
            stb.append("                GROUP BY SCHREGNO) STBEF ON STBEF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN CHAIR_STD_DAT CHSTD ON CHSTD.YEAR = REGD.YEAR ");
            stb.append("         AND CHSTD.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND CHSTD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = CHSTD.YEAR ");
            stb.append("         AND CHAIR.SEMESTER = CHSTD.SEMESTER ");
            stb.append("         AND CHAIR.CHAIRCD = CHSTD.CHAIRCD ");
            stb.append("     LEFT JOIN CHAIR_CORRES_DAT COR ON COR.YEAR = CHAIR.YEAR ");
            stb.append("         AND COR.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     LEFT JOIN CREDIT_MST CREDM ON CREDM.YEAR = CHAIR.YEAR ");
            stb.append("         AND CREDM.COURSECD = REGD.COURSECD ");
            stb.append("         AND CREDM.GRADE = REGD.GRADE ");
            stb.append("         AND CREDM.MAJORCD = REGD.MAJORCD ");
            stb.append("         AND CREDM.COURSECODE = REGD.COURSECODE ");
            stb.append("         AND CREDM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("         AND CREDM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("         AND CREDM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("         AND CREDM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = CHAIR.CLASSCD ");
            stb.append("         AND SUBM.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("         AND SUBM.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("         AND SUBM.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.SCHREGNO, CHAIR.CLASSCD, CHAIR.SCHOOL_KIND, CHAIR.CURRICULUM_CD, CHAIR.SUBCLASSCD ");
            return stb.toString();
        }

        private static String getReportSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_REPRESENT_SEQ AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO ");
            stb.append(" ), MAX_RECEIPT_DATE AS ( ");
            stb.append("     SELECT  ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ ");
            stb.append(" ), PRINT_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T3.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T3.ABBV1 AS GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         REP_PRESENT_DAT T1 ");
            stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
            stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
            stb.append("     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
            stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
            stb.append("     WHERE ");
            stb.append("         T2.RECEIPT_DATE <= '" + param._kijun + "' ");
            stb.append(" ), SUBCLASS_SCHREGNO AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SUBCLASS_STD_SELECT_DAT T1 ");
            stb.append("         INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ), MAIN AS ( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.STANDARD_SEQ, ");
            stb.append("         T1.STANDARD_DATE, ");
            stb.append("         NMM002.NAMESPARE1 AS TESTCD, ");
            stb.append("         T2.SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         REP_STANDARDDATE_DAT T1 ");
            stb.append("     INNER JOIN SUBCLASS_SCHREGNO T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' AND NMM002.NAMECD2 = T1.REPORTDIV ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         T0.YEAR, ");
            stb.append("         T0.CLASSCD, ");
            stb.append("         T0.SCHOOL_KIND, ");
            stb.append("         T0.CURRICULUM_CD, ");
            stb.append("         T0.SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T0.STANDARD_DATE, ");
            stb.append("         T0.TESTCD, ");
            stb.append("         T0.SCHREGNO, ");
            stb.append("         T1.STANDARD_SEQ AS REP_STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ, ");
            stb.append("         T1.RECEIPT_DATE, ");
            stb.append("         T1.NAMESPARE1, ");
            stb.append("         T1.GRAD_DATE, ");
            stb.append("         T1.GRAD_VALUE, ");
            stb.append("         T1.GRAD_VALUE_NAME ");
            stb.append("     FROM ");
            stb.append("         MAIN T0 ");
            stb.append("     LEFT JOIN PRINT_DATA T1 ON T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.CLASSCD = T0.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("         AND T1.STANDARD_SEQ = T0.STANDARD_SEQ ");
            stb.append("         AND T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("     ORDER BY ");
            stb.append("         T0.CLASSCD, ");
            stb.append("         T0.SCHOOL_KIND, ");
            stb.append("         T0.CURRICULUM_CD, ");
            stb.append("         T0.SUBCLASSCD, ");
            stb.append("         T0.STANDARD_SEQ, ");
            stb.append("         T1.REPRESENT_SEQ ");
            return stb.toString();
        }

        private static String getSchAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T2.SEMESTER, ");
            stb.append("         T3.CLASSCD, ");
            stb.append("         T3.SCHOOL_KIND, ");
            stb.append("         T3.CURRICULUM_CD, ");
            stb.append("         T3.SUBCLASSCD, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.SCHOOLINGKINDCD, ");
            stb.append("         T4.NAMESPARE1, ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CREDIT_TIME ");
            stb.append("     FROM ");
            stb.append("         SCH_ATTEND_DAT T1 ");
            stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER <> '9' ");
            stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("         LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
            stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.EXECUTEDATE <= '" + param._kijun + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     ORDER BY T2.SEMESTER, T1.SCHREGNO, T3.CLASSCD, T3.SCHOOL_KIND, T3.SUBCLASSCD, T1.EXECUTEDATE, T1.PERIODCD ");
            return stb.toString();
        }

        private static String getSchAttendToukouAll(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     COUNT(*) AS TOUKOU_CNT ");
            stb.append(" FROM ");
            stb.append("     ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.EXECUTEDATE ");
            stb.append("     FROM ");
            stb.append("         SCH_ATTEND_DAT T1 ");
            stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER <> '9' ");
            stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("         INNER JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
            stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
            stb.append("             AND T4.NAMESPARE1 = '1' ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.EXECUTEDATE <= '" + param._kijun + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.EXECUTEDATE ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.ATTENDDATE AS EXECUTEDATE ");
            stb.append("     FROM ");
            stb.append("         SPECIALACT_ATTEND_DAT T1 ");
            stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER <> '9' ");
            stb.append("             AND T1.ATTENDDATE BETWEEN T2.SDATE AND T2.EDATE ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.ATTENDDATE <= '" + param._kijun + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.ATTENDDATE ");
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            return stb.toString();
        }

        private static String getStudyRecCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(GET_CREDIT, 0) + VALUE(ADD_CREDIT, 0)) AS GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR < '" + param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            return stb.toString();
        }

        private static String getRecordScoreHistDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         V_RECORD_SCORE_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            return stb.toString();
        }

        private void setAddr(final DB2UDB db2, final Param param) {
            _hyoushiName  = "";
            _hyoushiZipCd = "";
            _hyoushiAddr1 = "";
            _hyoushiAddr2 = "";

            final String addrSql = getAddrSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(addrSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String name  = StringUtils.defaultString(rs.getString("NAME"));
                    final String zipCd = StringUtils.defaultString(rs.getString("ZIPCD"));
                    final String addr1 = StringUtils.defaultString(rs.getString("ADDR1"));
                    final String addr2 = StringUtils.defaultString(rs.getString("ADDR2"));
                    _hyoushiName  = KNJ_EditEdit.getMS932ByteLength(name) > 0 ? name + "　様" : "";
                    _hyoushiZipCd = zipCd;
                    _hyoushiAddr1 = addr1;
                    _hyoushiAddr2 = addr2;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAddrSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if (SEITO.equals(param._sendDiv)) {
                stb.append(" SELECT ");
                stb.append("     '" + _name + "' AS NAME, ");
                stb.append("     ADDR.ZIPCD, ");
                stb.append("     ADDR.ADDR1, ");
                stb.append("     ADDR.ADDR2 ");
                stb.append(" FROM ");
                stb.append("     SCHREG_ADDRESS_DAT ADDR, ");
                stb.append("     (SELECT ");
                stb.append("         ADDR2.SCHREGNO, ");
                stb.append("         MAX(ADDR2.ISSUEDATE) AS ISSUEDATE ");
                stb.append("      FROM ");
                stb.append("         SCHREG_ADDRESS_DAT ADDR2 ");
                stb.append("      WHERE ");
                stb.append("         ADDR2.SCHREGNO = '" + _schregno + "' ");
                stb.append("      GROUP BY ");
                stb.append("         ADDR2.SCHREGNO ");
                stb.append("     ) ADDR_MAX ");
                stb.append(" WHERE ");
                stb.append("     ADDR.SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
                stb.append("     AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            } else {
                stb.append(" SELECT ");
                stb.append("     SEND_NAME AS NAME, ");
                stb.append("     SEND_ZIPCD AS ZIPCD, ");
                stb.append("     SEND_ADDR1 AS ADDR1, ");
                stb.append("     SEND_ADDR2 AS ADDR2 ");
                stb.append(" FROM ");
                stb.append("     SCHREG_SEND_ADDRESS_DAT ");
                stb.append(" WHERE ");
                stb.append("     SCHREGNO = '" + _schregno + "' ");
                stb.append("     AND DIV = '1' ");
            }

            return stb.toString();
        }
    }

    private static class Subclass {
        final String _year;
        final String _takeSemes;
        final String _chaircd;
        final String _subclasscd;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        final String _credits;
        final List<Report> _reportList = new ArrayList();
        final List<Attend> _attendList = new ArrayList();
        final Map<String, String> _testScoreMap = new HashMap();
        String _getCredit;
        String _compCredit;
        String _hyotei;

        Subclass(
                final String year,
                final String takeSemes,
                final String chaircd,
                final String subclasscd,
                final String subclassname,
                final String repSeqAll,
                final String schSeqAll,
                final String schSeqMin,
                final String credits
        ) {
            _year = year;
            _takeSemes = takeSemes;
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
            _credits = credits;
        }

        public String getAttendCount(final boolean isHousou) {
            final List set = new ArrayList();
            boolean addval = false;
            BigDecimal n = new BigDecimal(0);
            for (final Iterator it = _attendList.iterator(); it.hasNext();) {
                final Attend at = (Attend) it.next();
                if (isHousou && "2".equals(at._schoolingkindcd)) {
                    if (null != at._creditTime) {
                        addval = true;
                        n = n.add(at._creditTime);
                    }
                } else if (!isHousou && "1".equals(at._namespare1)) {
                    if ("1".equals(at._schoolingkindcd)) {
                        set.add(at._executedate);
                    } else {
                        if (null != at._creditTime) {
                            addval = true;
                            n = n.add(at._creditTime);
                        }
                    }
                }

            }
            if (!set.isEmpty()) {
                addval = true;
                n = n.add(new BigDecimal(set.size()));
            }
            return addval ? getDispNum(n) : !isHousou ? "0" : null;
        }

        public List<String> getAttendList(final boolean isHousou) {
            final ArrayList<String> set = new ArrayList();
            for (final Attend at : _attendList) {
                if (isHousou && !"1".equals(at._namespare1) && null != at._executedate) {
                    for (int cnt = 1;cnt <= at._creditTime.intValue();cnt++) {
                	    set.add(at._executedate);
                    }
                } else if (!isHousou && "1".equals(at._namespare1) && null != at._executedate) {
                    for (int cnt = 1;cnt <= at._creditTime.intValue();cnt++) {
                        set.add(at._executedate);
                    }
                }
            }
            return set;
        }

        public List<String> getTestcdList() {
            final List<String> list = new ArrayList();
            for (final Report rep : _reportList) {
                if (null != rep._testcd && !list.contains(rep._testcd)) {
                    list.add(rep._testcd);
                }
            }
            return list;
        }
    }

    private static class Report {
        final String _standardSeq; // REP_STANDARDDATE_DAT.STANDARD_SEQ
        final String _standardDate;
        final String _testcd;
        final String _repStandardSeq; // REP_REPORT_DAT.STANDARD_SEQ
        final String _namespare1;
        final String _representSeq;
        final String _receiptDate;
        final String _gradDate;
        final String _gradValue;
        final String _gradValueName;
        public Report(final String standardSeq, final String standardDate, final String testcd, final String repStandardSeq, final String namespare1, final String representSeq, final String receiptDate, final String gradDate, final String gradValue, final String gradValueName) {
            _standardSeq = standardSeq;
            _standardDate = standardDate;
            _testcd = testcd;
            _repStandardSeq = repStandardSeq;
            _namespare1 = namespare1;
            _representSeq = representSeq;
            _receiptDate = receiptDate;
            _gradDate = gradDate;
            _gradValue = gradValue;
            _gradValueName = null == gradValue ? "受" : gradValueName;
        }
    }

    private static class Attend {
        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;
        public Attend(final String schoolingkindcd, final String namespare1, final String executedate, final String periodcd, final BigDecimal creditTime) {
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75968 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _kijun;
        final boolean _isHyoushiPrint;
        final String _sendDiv;
        final boolean _isPrintHogosya;
        final String _takesemes;
        final String _schoolName1;
        final String _loginDate;
        final Map<String, String> _hreportRemarkTDat;
        final String _passScore;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _kijun = request.getParameter("KIJUN").replace('/', '-');
            _isHyoushiPrint = "1".equals(request.getParameter("HYOUSHI_PRINT"));
            _sendDiv = request.getParameter("SEND_DIV");
            _isPrintHogosya = "1".equals(request.getParameter("HOGOSYA_MONGON"));
            _takesemes = request.getParameter("TAKESEMES");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolName1 = getSchoolName1(db2);
            _hreportRemarkTDat = getHreportRemarkTDatMap(db2);
            _passScore = getM028(db2);
        }

        private String getSchoolName1(final DB2UDB db2) {
            String schoolName1 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolName1 = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName1;
        }

        private Map<String, String> getHreportRemarkTDatMap(final DB2UDB db2) {
            Map<String, String> rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT REMARKID, REMARK FROM HREPORTREMARK_T_DAT WHERE REMARKID IN ('1', '2', '3', '4', '5', '6') ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("REMARKID"), rs.getString("REMARK"));
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getM028(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'M028' AND NAMECD2 = '01' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = StringUtils.defaultString(rs.getString("NAME1"));
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return StringUtils.defaultString(retStr, "30");
        }
    }
}

// eof

