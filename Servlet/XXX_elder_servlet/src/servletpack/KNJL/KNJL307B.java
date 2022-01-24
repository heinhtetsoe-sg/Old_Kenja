/*
 * $Id: 1bfebc418e8bed33b5a172eeea2bd76e540ede82 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３０７Ｂ＞  志願者情報確認表
 **/
public class KNJL307B {

    private static final Log log = LogFactory.getLog(KNJL307B.class);

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
    
    public void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = Applicant.load(db2, _param);
        
        final String form = "KNJL307B.frm";
        final int maxLine = 20;
        final List pageList = getPageList(list, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List applicantList = (List) pageList.get(pi);

            svf.VrSetForm(form, 1);
            
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度"); // 年度
            svf.VrsOut("KIND", _param._testdivAbbv1); // 入試制度
            // svf.VrsOut("HOPE_COURSE", null); // 志望所属
            svf.VrsOut("TITLE", "志願者情報確認表"); // タイトル
            svf.VrsOut("DATE", _param._dateStr); // 作成日
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
            
            for (int j = 0; j < applicantList.size(); j++) {
                final Applicant appl = (Applicant) applicantList.get(j);
                final int line = j + 1;
                svf.VrsOutn("RECEPT_DATE", line, null == appl._receptdate ? null : appl._receptdate.replace('-', '.')); // 受付日
                svf.VrsOutn("EXAM_NO", line, appl._examno); // 受験番号
                svf.VrsOutn("KANA", line, appl._kana); // かな
                svf.VrsOutn("NAME1", line, appl._name); // 氏名
                svf.VrsOutn("SEX", line, appl._sexName); // 性別
                svf.VrsOutn("BIRTHDAY", line, _param.formatDateMarkDot(appl._birthday)); // 生年月日
                svf.VrsOutn("ZIPNO", line, appl._fsCd); // 学校コード
                svf.VrsOutn("SCHOOL_NAME1", line, appl._finschoolName); // 学校名
                svf.VrsOutn("ACTIVE", line, appl._fsGrdDivName); // 現浪
                svf.VrsOutn("AVERAGE", line, appl._averageAll); // 評定平均
                svf.VrsOutn("SUBCLASS1", line, appl._confidentialRpt01); // 教科
                svf.VrsOutn("SUBCLASS2", line, appl._confidentialRpt02); // 教科
                svf.VrsOutn("SUBCLASS3", line, appl._confidentialRpt03); // 教科
                svf.VrsOutn("SUBCLASS4", line, appl._confidentialRpt04); // 教科
                svf.VrsOutn("SUBCLASS5", line, appl._confidentialRpt05); // 教科
                svf.VrsOutn("SUBCLASS6", line, appl._confidentialRpt06); // 教科
                svf.VrsOutn("SUBCLASS7", line, appl._confidentialRpt07); // 教科
                svf.VrsOutn("SUBCLASS8", line, appl._confidentialRpt08); // 教科
                svf.VrsOutn("SUBCLASS9", line, appl._confidentialRpt09); // 教科
                svf.VrsOutn("KIHON", line, appl._baseFlg); // 基本
                svf.VrsOutn("KENKOU", line, appl._healthFlg); // 健康
                svf.VrsOutn("JISHU", line, appl._activeFlg); // 自主
                svf.VrsOutn("SEKININ", line, appl._responsibleFlg); // 責任
                svf.VrsOutn("SOUI", line, appl._originalFlg); // 創意
                svf.VrsOutn("OMOI", line, appl._mindFlg); // 思い
                svf.VrsOutn("SHIZEN", line, appl._natureFlg); // 自然
                svf.VrsOutn("KINROU", line, appl._workFlg); // 勤労
                svf.VrsOutn("KOUSEI", line, appl._justiceFlg); // 公正
                svf.VrsOutn("KOUKYOU", line, appl._publicFlg); // 公共
                svf.VrsOutn("ABSENT1", line, appl._absenceDays); // 欠席
                svf.VrsOutn("ABSENT2", line, appl._absenceDays2); // 欠席
                svf.VrsOutn("ABSENT3", line, appl._absenceDays3); // 欠席
                svf.VrsOutn("REASON1", line, appl._absenceRemark); // 欠席理由
                svf.VrsOutn("REASON2", line, appl._absenceRemark2); // 欠席理由
                svf.VrsOutn("REASON3", line, appl._absenceRemark3); // 欠席理由
                svf.VrsOutn("CONSUL_POINT", line, StringUtils.defaultString(appl._detail4Remark3) + ":" + StringUtils.defaultString(appl._detail4Remark4)); // 入試相談点
                svf.VrsOutn("PROMISE", line, appl._promiseCourseAbbv); // 確約区分
                svf.VrsOutn("RECOMMEND", line, appl._detail4Remark2); // 推薦理由
                svf.VrsOutn("REMARK1", line, appl._remark1); // 備考
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static List getPageList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private static class Applicant {
        final String _receptdate;
        final String _examno;
        final String _kana;
        final String _name;
        final String _sexName;
        final String _birthday;
        final String _fsCd;
        final String _finschoolZipcd;
        final String _finschoolName;
        final String _fsGrdDivName;
        final String _averageAll;
        final String _confidentialRpt01;
        final String _confidentialRpt02;
        final String _confidentialRpt03;
        final String _confidentialRpt04;
        final String _confidentialRpt05;
        final String _confidentialRpt06;
        final String _confidentialRpt07;
        final String _confidentialRpt08;
        final String _confidentialRpt09;
        final String _baseFlg;
        final String _healthFlg;
        final String _activeFlg;
        final String _responsibleFlg;
        final String _originalFlg;
        final String _mindFlg;
        final String _natureFlg;
        final String _workFlg;
        final String _justiceFlg;
        final String _publicFlg;
        final String _absenceDays;
        final String _absenceRemark;
        final String _absenceDays2;
        final String _absenceRemark2;
        final String _absenceDays3;
        final String _absenceRemark3;
        final String _detail4Remark3;
        final String _detail4Remark4;
        final String _promiseCourseAbbv;
        final String _detail4Remark2;
        final String _remark1;

        Applicant(
            final String receptdate,
            final String examno,
            final String kana,
            final String name,
            final String sexName,
            final String birthday,
            final String fsCd,
            final String finschoolZipcd,
            final String finschoolName,
            final String fsGrdDivName,
            final String averageAll,
            final String confidentialRpt01,
            final String confidentialRpt02,
            final String confidentialRpt03,
            final String confidentialRpt04,
            final String confidentialRpt05,
            final String confidentialRpt06,
            final String confidentialRpt07,
            final String confidentialRpt08,
            final String confidentialRpt09,
            final String baseFlg,
            final String healthFlg,
            final String activeFlg,
            final String responsibleFlg,
            final String originalFlg,
            final String mindFlg,
            final String natureFlg,
            final String workFlg,
            final String justiceFlg,
            final String publicFlg,
            final String absenceDays,
            final String absenceRemark,
            final String absenceDays2,
            final String absenceRemark2,
            final String absenceDays3,
            final String absenceRemark3,
            final String detail4Remark3,
            final String detail4Remark4,
            final String promiseCourseAbbv,
            final String detail4Remark2,
            final String remark1
        ) {
            _receptdate = receptdate;
            _examno = examno;
            _kana = kana;
            _name = name;
            _sexName = sexName;
            _birthday = birthday;
            _fsCd = fsCd;
            _finschoolZipcd = finschoolZipcd;
            _finschoolName = finschoolName;
            _fsGrdDivName = fsGrdDivName;
            _averageAll = averageAll;
            _confidentialRpt01 = confidentialRpt01;
            _confidentialRpt02 = confidentialRpt02;
            _confidentialRpt03 = confidentialRpt03;
            _confidentialRpt04 = confidentialRpt04;
            _confidentialRpt05 = confidentialRpt05;
            _confidentialRpt06 = confidentialRpt06;
            _confidentialRpt07 = confidentialRpt07;
            _confidentialRpt08 = confidentialRpt08;
            _confidentialRpt09 = confidentialRpt09;
            _baseFlg = baseFlg;
            _healthFlg = healthFlg;
            _activeFlg = activeFlg;
            _responsibleFlg = responsibleFlg;
            _originalFlg = originalFlg;
            _mindFlg = mindFlg;
            _natureFlg = natureFlg;
            _workFlg = workFlg;
            _justiceFlg = justiceFlg;
            _publicFlg = publicFlg;
            _absenceDays = absenceDays;
            _absenceRemark = absenceRemark;
            _absenceDays2 = absenceDays2;
            _absenceRemark2 = absenceRemark2;
            _absenceDays3 = absenceDays3;
            _absenceRemark3 = absenceRemark3;
            _detail4Remark3 = detail4Remark3;
            _detail4Remark4 = detail4Remark4;
            _promiseCourseAbbv = promiseCourseAbbv;
            _detail4Remark2 = detail4Remark2;
            _remark1 = remark1;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptdate = rs.getString("RECEPTDATE");
                    final String examno = rs.getString("EXAMNO");
                    final String kana = rs.getString("NAME_KANA");
                    final String name = rs.getString("NAME");
                    final String sexName = rs.getString("SEX_NAME");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String fsCd = rs.getString("FS_CD");
                    final String finschoolZipcd = rs.getString("FINSCHOOL_ZIPCD");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    final String fsGrdDivName = rs.getString("FS_GRD_DIV_NAME");
                    final String averageAll = rs.getString("AVERAGE_ALL");
                    final String confidentialRpt01 = rs.getString("CONFIDENTIAL_RPT01");
                    final String confidentialRpt02 = rs.getString("CONFIDENTIAL_RPT02");
                    final String confidentialRpt03 = rs.getString("CONFIDENTIAL_RPT03");
                    final String confidentialRpt04 = rs.getString("CONFIDENTIAL_RPT04");
                    final String confidentialRpt05 = rs.getString("CONFIDENTIAL_RPT05");
                    final String confidentialRpt06 = rs.getString("CONFIDENTIAL_RPT06");
                    final String confidentialRpt07 = rs.getString("CONFIDENTIAL_RPT07");
                    final String confidentialRpt08 = rs.getString("CONFIDENTIAL_RPT08");
                    final String confidentialRpt09 = rs.getString("CONFIDENTIAL_RPT09");
                    final String baseFlg = rs.getString("BASE_FLG");
                    final String healthFlg = rs.getString("HEALTH_FLG");
                    final String activeFlg = rs.getString("ACTIVE_FLG");
                    final String responsibleFlg = rs.getString("RESPONSIBLE_FLG");
                    final String originalFlg = rs.getString("ORIGINAL_FLG");
                    final String mindFlg = rs.getString("MIND_FLG");
                    final String natureFlg = rs.getString("NATURE_FLG");
                    final String workFlg = rs.getString("WORK_FLG");
                    final String justiceFlg = rs.getString("JUSTICE_FLG");
                    final String publicFlg = rs.getString("PUBLIC_FLG");
                    final String absenceDays = rs.getString("ABSENCE_DAYS");
                    final String absenceRemark = rs.getString("ABSENCE_REMARK");
                    final String absenceDays2 = rs.getString("ABSENCE_DAYS2");
                    final String absenceRemark2 = rs.getString("ABSENCE_REMARK2");
                    final String absenceDays3 = rs.getString("ABSENCE_DAYS3");
                    final String absenceRemark3 = rs.getString("ABSENCE_REMARK3");
                    final String detail4Remark3 = rs.getString("DETAIL4_REMARK3");
                    final String detail4Remark4 = rs.getString("DETAIL4_REMARK4");
                    final String promiseCourseAbbv = rs.getString("PROMISE_COURSE_ABBV");
                    final String detail4Remark2 = rs.getString("DETAIL4_REMARK2");
                    final String remark1 = rs.getString("REMARK1");
                    final Applicant applicant = new Applicant(receptdate, examno, kana, name, sexName, birthday, fsCd, finschoolZipcd, finschoolName, fsGrdDivName, averageAll, confidentialRpt01, confidentialRpt02, confidentialRpt03, confidentialRpt04, confidentialRpt05, confidentialRpt06, confidentialRpt07, confidentialRpt08, confidentialRpt09, baseFlg, healthFlg, activeFlg, responsibleFlg, originalFlg, mindFlg, natureFlg, workFlg, justiceFlg, publicFlg, absenceDays, absenceRemark, absenceDays2, absenceRemark2, absenceDays3, absenceRemark3, detail4Remark3, detail4Remark4, promiseCourseAbbv, detail4Remark2, remark1);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     BASE.RECEPTDATE, ");
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.NAME, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     BASE.FS_CD, ");
            stb.append("     FIN.FINSCHOOL_ZIPCD, ");
            stb.append("     FIN.FINSCHOOL_NAME, ");
            stb.append("     NML016.NAME1 AS FS_GRD_DIV_NAME, ");
            stb.append("     CONFRPT.AVERAGE_ALL, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
            stb.append("     CONFRPT.BASE_FLG, ");
            stb.append("     CONFRPT.HEALTH_FLG, ");
            stb.append("     CONFRPT.ACTIVE_FLG, ");
            stb.append("     CONFRPT.RESPONSIBLE_FLG, ");
            stb.append("     CONFRPT.ORIGINAL_FLG, ");
            stb.append("     CONFRPT.MIND_FLG, ");
            stb.append("     CONFRPT.NATURE_FLG, ");
            stb.append("     CONFRPT.WORK_FLG, ");
            stb.append("     CONFRPT.JUSTICE_FLG, ");
            stb.append("     CONFRPT.PUBLIC_FLG, ");
            stb.append("     CONFRPT.ABSENCE_DAYS, ");
            stb.append("     CONFRPT.ABSENCE_REMARK, ");
            stb.append("     CONFRPT.ABSENCE_DAYS2, ");
            stb.append("     CONFRPT.ABSENCE_REMARK2, ");
            stb.append("     CONFRPT.ABSENCE_DAYS3, ");
            stb.append("     CONFRPT.ABSENCE_REMARK3, ");
            stb.append("     DETAIL4.REMARK3 AS DETAIL4_REMARK3, ");
            stb.append("     DETAIL4.REMARK4 AS DETAIL4_REMARK4, ");
            stb.append("     CRSJDG.PROMISE_COURSE_ABBV, ");
            stb.append("     DETAIL4.REMARK2 AS DETAIL4_REMARK2, ");
            stb.append("     CONFRPT.REMARK1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CONFRPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL4 ON DETAIL4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND DETAIL4.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND DETAIL4.SEQ = '004' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJDG ON CRSJDG.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND CRSJDG.JUDGMENT_DIV = DETAIL4.REMARK8 ");
            stb.append("     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ");
            stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ");
            stb.append("         AND NMZ002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST NML016 ON NML016.NAMECD1 = 'L016' ");
            stb.append("         AND NML016.NAMECD2 = BASE.FS_GRDDIV ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if (null != param._examnoFrom && !"".equals(param._examnoFrom)) {
                stb.append("     AND BASE.EXAMNO >= '" + param._examnoFrom + "' ");
            }
            if (null != param._examnoTo && !"".equals(param._examnoTo)) {
                stb.append("     AND BASE.EXAMNO <= '" + param._examnoTo + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;
        String _examnoFrom;
        String _examnoTo;

        final String _testdivAbbv1;
        final String _dateStr;
        final List _nameMstL007;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_date);
            _examnoFrom = request.getParameter("EXAMNO_FROM");
            _examnoTo = request.getParameter("EXAMNO_TO");
            
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _nameMstL007 = getNameMstL007(db2);
        }
        
        private String getDateStr(final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(date);
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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
        
        public String formatDateMarkDot(final String date) {
            if (null != date) {
                final DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                try {
                    final Date d = java.sql.Date.valueOf(date);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    int nen = -1;
                    final int tuki = cal.get(Calendar.MONTH) + 1;
                    final int hi = cal.get(Calendar.DAY_OF_MONTH);
                    
                    String mark = " ";
                    for (final Iterator it = _nameMstL007.iterator(); it.hasNext();) {
                        final Map m = (Map) it.next();
                        final String namespare2 = (String) m.get("NAMESPARE2");
                        if (null != namespare2) {
                            final Calendar dcal = Calendar.getInstance();
                            dcal.setTime(df.parse(namespare2.replace('/', '-')));
                            if (dcal.before(cal)) {
                                mark = StringUtils.defaultString((String) m.get("ABBV1"), " ");
                                nen = cal.get(Calendar.YEAR) - dcal.get(Calendar.YEAR) + 1;
                                break;
                            }
                        }
                    }
                    return mark + keta(nen, 2) + "." + keta(tuki, 2) + "." + keta(hi, 2);
                } catch (Exception e) {
                    log.error("format exception! date = " + date, e);
                }
            }
            return null;
        }
        
        public String keta(final int n, final int keta) {
            return StringUtils.repeat(" ", keta - String.valueOf(n).length()) + String.valueOf(n);
        }
        
        private List getNameMstL007(final DB2UDB db2) {
            final List list = new ArrayList();
            final String sql = " SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' ORDER BY NAMECD2 DESC ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnName(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    list.add(m);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof

