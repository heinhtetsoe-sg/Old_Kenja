/*
 * 作成日: 2009/10/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditDate.L007Class;

public class KNJC210 {

    private static final Log log = LogFactory.getLog(KNJC210.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = getPetitionHdat(db2);

        svf.VrSetForm("KNJC210.frm", 4);
        svf.VrsOut("PEREOD", KNJ_EditDate.h_format_JP(_param._fromDate) + "〜" + KNJ_EditDate.h_format_JP(_param._toDate));
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        final int countPerPage = 20;
        int count = 0;
        String oldCheckDate = null;

        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Record rec = (Record) it.next();
            count += 1;

            if (!_param.isCheckFirstdate()) {
                // "欠席・遅刻・早退日"チェック時は開始日付ごとに改ページする。
                if (oldCheckDate != null && !oldCheckDate.equals(rec._checkdate)) {
                    for (int i = count % countPerPage; i <= countPerPage; i++) {
                        svf.VrEndRecord();
                        count += 1;
                    }
                }
                svf.VrsOut("ACCEPTDATE", "・欠席・遅刻・早退日：" + KNJ_EditDate.h_format_JP(rec._checkdate));
            }
            int page = count / countPerPage + (count % countPerPage == 0 ? 0 : 1);
            svf.VrsOut("PAGE", String.valueOf(page));

            svf.VrsOut("FIRSTDATE", rec._firstDate);
            svf.VrsOut("HR_NAME", rec._hrname);
            svf.VrsOut("ATTENO_NO", Integer.valueOf(rec._attendno).toString());
            svf.VrsOut("NAME_SHOW", rec._name);

            svf.VrsOut("SEX", rec._sex);
            svf.VrsOut("TR_CD1", rec._trname1);
            svf.VrsOut("FIRSTREGISTER", rec._firstRegister);

            svf.VrsOut("CONTACTDIV", rec._contacterName);
            svf.VrsOut("FROMDATE", rec._from);
            svf.VrsOut("TODATE", rec._to);
            svf.VrsOut("DI", rec._diName);
            svf.VrsOut("DI_REMARK_CD_1", rec.getDiRemark());

            svf.VrsOut("REMARK", rec._remark);

            if (!_param.isCheckFirstdate()) {
                oldCheckDate = rec._checkdate;
            }
            svf.VrEndRecord();

            _hasData = true;
        }
    }

    private List getPetitionHdat(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List list = new ArrayList();
        try {
            String sql = sqlAttendPetitionHdat();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final Map seqnoRecMap = new TreeMap();

            while (rs.next()) {

                final String checkdate = (_param.isCheckFirstdate()) ? null : rs.getString("CHECKDATE");
                final Integer seqno = Integer.valueOf(rs.getString("SEQNO"));
                final String firstDate = getFormattedDateStr(rs.getString("FIRSTDATE"), db2);
                final String hrname = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String contacterName = getContacterName(rs.getString("CONTACTERDIV"));
                final String from = getFormattedDateStr(rs.getString("FROMDATE"), db2) + ":" + _param.getPeriodName(rs.getString("FROMPERIOD"));
                final String to = getFormattedDateStr(rs.getString("TODATE"), db2) + ":" + _param.getPeriodName(rs.getString("TOPERIOD"));
                final String callback = rs.getString("CALLBACK");
                final String remark = "返電:" + ("0".equals(callback) ? "不要" : "1".equals(callback) ? "必要" : "");
                final String diName = _param.getDiName(rs.getString("DI_CD"));

                final List diRemarkList = new ArrayList(); // nullではない勤怠備考のリスト
                try {
                    for (int i = 1; i <= 10; i++) {
                        String diRemark = _param.getDiRemark(rs.getString("DI_REMARK_CD" + i));
                        if (diRemark == null) continue;
                        diRemarkList.add(diRemark);
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                }

                final String firstRegister = rs.getString("FIRSTREGISTER_NAME");
                final String sex = rs.getString("SEX");
                final String trname1 = rs.getString("TR_NAME1");

//                log.debug(firstDate + " , " + rs.getString("HR_NAME") + " , " + attendno + " , "  + rs.getString("NAME") + " , "  + rs.getString("SEX") + " , "  + rs.getString("TR_NAME1") + " , "  + rs.getString("FIRSTREGISTER_NAME") + " , "  +  contacterName + ", ");
//                log.debug(from + " , "  +  to + " , "  + getDiRemark(diRemarkList) + " , " + remark);

                final Record rec = new Record(checkdate, seqno, firstDate, hrname, attendno, name, contacterName, from, to, callback, remark, diName, diRemarkList, firstRegister, sex, trname1);
                list.add(rec);

                seqnoRecMap.put(seqno, rec);
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps ,rs);

            if (!seqnoRecMap.isEmpty()) {
                final String sqlAttendPetitionDat = sqlAttendPetitiondat(seqnoRecMap.keySet());
                log.debug(" sqlAttendPetitionDat = " + sqlAttendPetitionDat);

                ps = db2.prepareStatement(sqlAttendPetitionDat);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Record rec = (Record) seqnoRecMap.get(Integer.valueOf(rs.getString("SEQNO")));

                    rec._datDiRemark.add(rs.getString("DI_REMARK"));
                }

                db2.commit();
                DbUtils.closeQuietly(null, ps ,rs);
            }

        } catch (SQLException e) {
            log.error("exception!", e);
        }
        return list;
    }

    /**
     * yyyy-MM-ddフォーマット日付を、表示するフォーマットI.yy.MM.ddに変換した値を得る。(例 2009-10-16 => H.09.10.16)
     * @param dateStr yyyy-MM-ddの日付フォーマット文字列
     * @return 表示するフォーマットに変換した値
     */
    private String getFormattedDateStr(String dateStr, final DB2UDB db2) {
        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(".MM.dd");

        L007Class l007 = KNJ_EditDate.getL007ofDate(db2, dateStr);
        String gengouAbbv = l007.getAbbv1();
        String wareki = gengouAbbv + String.valueOf(l007.calcNen()) + sdf.format(java.sql.Date.valueOf(dateStr));

        return wareki;
    }

    /**
     * 連絡先名称を得る
     * @param contactDiv 連絡先区分
     * @return 連絡先名称
     */
    private String getContacterName(String contactDiv) {
        if ("0".equals(contactDiv)) {
            return "保護者";
        } else if ("1".equals(contactDiv)) {
            return "生徒";
        } else if ("2".equals(contactDiv)) {
            return "その他";
        }
        return "";
    }

    /** 対象のSQL */
    private String sqlAttendPetitionHdat() {
        StringBuffer stb = new StringBuffer();

        if (!_param.isCheckFirstdate()) {
            stb.append(" WITH ");
            stb.append(_param.getDatesFromToSql());
        }
        stb.append(" SELECT");
        if (!_param.isCheckFirstdate()) {
            stb.append("     DATE(T0.CHECKDATE) AS CHECKDATE,");
        }
        stb.append("     DATE(T1.FIRSTDATE) AS FIRSTDATE");
        stb.append("     ,T1.SEQNO, T1.SCHREGNO, T4.HR_NAME, T3.ATTENDNO, T2.NAME, T8.NAME2 AS SEX ");
        stb.append("     ,T5.STAFFNAME AS TR_NAME1, T7.STAFFNAME AS FIRSTREGISTER_NAME ");
        stb.append("     ,T1.CONTACTERDIV, T1.FROMDATE, T1.FROMPERIOD, T1.TODATE, T1.TOPERIOD");
        stb.append("     ,T1.DI_CD, T6.NAME1 AS DI_NAME");
        stb.append("     ,T1.DI_REMARK_CD1, T1.DI_REMARK_CD2, T1.DI_REMARK_CD3");
        stb.append("     ,T1.DI_REMARK_CD4, T1.DI_REMARK_CD5, T1.DI_REMARK_CD6");
        stb.append("     ,T1.DI_REMARK_CD7, T1.DI_REMARK_CD8, T1.DI_REMARK_CD9, T1.DI_REMARK_CD10");
        stb.append("     ,T1.CALLBACK");
        stb.append(" FROM");
        if (!_param.isCheckFirstdate()) {
            stb.append("     DATES T0 ");
            stb.append("     INNER JOIN ATTEND_PETITION_HDAT T1 ON DATE(T0.CHECKDATE) BETWEEN T1.FROMDATE AND T1.TODATE ");
        } else {
            stb.append("     ATTEND_PETITION_HDAT T1");
        }
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON");
        stb.append("         T2.SCHREGNO = T1.SCHREGNO");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ON");
        stb.append("         T3.SCHREGNO = T1.SCHREGNO");
        stb.append("         AND T3.YEAR = T1.YEAR");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T4 ON");
        stb.append("         T4.YEAR = T3.YEAR");
        stb.append("         AND T4.SEMESTER = T3.SEMESTER");
        stb.append("         AND T4.GRADE = T3.GRADE");
        stb.append("         AND T4.HR_CLASS = T3.HR_CLASS");
        stb.append("     INNER JOIN NAME_MST T8 ON");
        stb.append("         T8.NAMECD1 = 'Z002'");
        stb.append("         AND T8.NAMECD2 = T2.SEX");
        stb.append("     LEFT JOIN STAFF_MST T5 ON");
        stb.append("         T4.TR_CD1 = T5.STAFFCD");
        stb.append("     LEFT JOIN NAME_MST T6 ON");
        stb.append("         T6.NAMECD1 = 'C001'");
        stb.append("         AND T6.NAMECD2 = T1.DI_CD");
        stb.append("     LEFT JOIN STAFF_MST T7 ON");
        stb.append("         T1.FIRSTREGISTER = T7.STAFFCD");
        stb.append("     LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + _param._year + "' AND ATDD.DI_CD = T1.DI_CD ");
        stb.append(" WHERE");
        stb.append("     T3.SEMESTER = '" + _param._semester + "'");
        if (_param.isCheckFirstdate()) {
            stb.append("   AND DATE(T1.FIRSTDATE) BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
        } else {
            stb.append("   AND ATDD.REP_DI_CD IN ('4', '5', '6', '11', '12', '13', '15', '16', '23', '24') ");
        }
        stb.append(" ORDER BY");
        if (_param.isCheckFirstdate()) {
            stb.append("     T1.FIRSTDATE, T3.GRADE, T3.HR_CLASS, T3.ATTENDNO");
        } else {
            stb.append("     T0.CHECKDATE, T3.GRADE, T3.HR_CLASS, T3.ATTENDNO, T1.FROMPERIOD ");
        }
        return stb.toString();
    }

    /** 対象のSQL */
    private String sqlAttendPetitiondat(final Collection seqnos) {


        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT T1.SEQNO, VALUE(T2.NAME1, T1.DI_REMARK) AS DI_REMARK ");
        stb.append(" FROM ATTEND_PETITION_DAT T1 ");
        stb.append(" LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'C900' AND T2.NAMECD2 = T1.DI_REMARK_CD ");
        stb.append(" WHERE ");
        stb.append("  YEAR = '" + _param._year + "' ");
        stb.append("  AND T1.INPUT_FLG = '1' ");
        stb.append("  AND T1.SEQNO IN (");

        String comma = "";
        for (final Iterator it = seqnos.iterator(); it.hasNext();) {
            final Integer seqno = (Integer) it.next();
            stb.append(comma).append(seqno);
            comma = ",";
        }
        stb.append("  ) ");
        stb.append(" GROUP BY ");
        stb.append("   T1.SEQNO, VALUE(T2.NAME1, T1.DI_REMARK) ");
        stb.append(" ORDER BY ");
        stb.append("   MIN(T1.PERIODCD) ");
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Record {
        final String _checkdate;
        final Integer _seqno;
        final String _firstDate;
        final String _hrname;
        final String _attendno;
        final String _name;
        final String _contacterName;
        final String _from;
        final String _to;
        final String _callback;
        final String _remark;
        final String _diName;
        final List _diRemarkList;
        final String _firstRegister;
        final String _sex;
        final String _trname1;
        final List _datDiRemark;
        public Record(
                final String checkdate,
                final Integer seqno,
                final String firstDate,
                final String hrname,
                final String attendno,
                final String name,
                final String contacterName,
                final String from,
                final String to,
                final String callback,
                final String remark,
                final String diName,
                final List diRemarkList,
                final String firstRegister,
                final String sex,
                final String trname1) {
            _checkdate = checkdate;
            _seqno = seqno;
            _firstDate = firstDate;
            _hrname = hrname;
            _attendno = attendno;
            _name = name;
            _contacterName = contacterName;
            _from = from;
            _to = to;
            _callback = callback;
            _remark = remark;
            _diName = diName;
            _diRemarkList = diRemarkList;
            _firstRegister = firstRegister;
            _sex = sex;
            _trname1 = trname1;
            _datDiRemark = new ArrayList();
        }
        private List allDiRemarkList() {
            final List thisList = new ArrayList(_diRemarkList);
            final String sonota = getDatDiRemark();
            if (null != sonota) {
                thisList.add(sonota);
            }
            return thisList;
        }
        private List getPrintDiRemarkList() {
            final List list = new ArrayList();
            final int fieldSize = 60 * 2;

            final int etcCommentSize = " 他 件".getBytes().length;
            int restSize = fieldSize - etcCommentSize; // 残りフィールドサイズ

            final List allDiRemarkList = allDiRemarkList();
            int restCount = allDiRemarkList.size(); // 残り件数

            String slash = "";
            for (final Iterator it = allDiRemarkList.iterator(); it.hasNext();) {
                final String diRemark = (String) it.next();

                final String a = slash + diRemark;
                int asize;
                try {
                    asize = a.getBytes("MS932").length;
                } catch (Exception e) {
                    asize = a.getBytes().length;
                }
                restSize -= asize;

                //log.debug(a + " asize = " + asize + " , restSize = " + restSize + " , restCount = " + restCount);

                if (restSize >= 0) {
                    list.add(diRemark);
                    slash = "／";
                    restCount -= 1;
                } else if (restSize < 0 && restCount == 1 && 0 <= restSize + etcCommentSize) {
                    // 残り1個で "他1件"の文字列を省いて範囲に収まるなら含める。
                    list.add(diRemark);
                    slash = "／";
                    restCount -= 1;
                } else {
                    break;
                }
            }
            return list;
        }


        /**
         * listに含まれる症状理由1〜10をフォーマットした文字列を得る。
         * @param list
         * @return 症状理由1〜10をフォーマットした文字列
         */
        public String getDiRemark() {

            final StringBuffer stb = new StringBuffer();
            final List printDiRemarkList = getPrintDiRemarkList();
            String slash = "";

            for (final Iterator it = printDiRemarkList.iterator(); it.hasNext();) {
                final String diRemark = (String) it.next();

                stb.append(slash).append(diRemark);
                slash = "／";
            }

            final List allDiRemarkList = allDiRemarkList();
            if (allDiRemarkList.size() - printDiRemarkList.size() > 0) {
                stb.append(" 他" + (allDiRemarkList.size() - printDiRemarkList.size()) + "件");
            }

            return stb.toString();
        }
        public String getDatDiRemark() {
            final StringBuffer stb = new StringBuffer();
            final String iniSep = "";
            String sep = iniSep;

            for (final Iterator it = _datDiRemark.iterator(); it.hasNext();) {
                final String datDiRemark = (String) it.next();
                if (null == datDiRemark) {
                    continue;
                }
                if (iniSep.equals(sep)) {
                    stb.append("その他（");
                }
                stb.append(sep).append(datDiRemark);
                sep = "／";
            }
            return iniSep.equals(sep) ? null : stb.append("）").toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _fromDate;
        private final String _toDate;
        private final String _loginDate;
        private final String _hiduke; // 1:欠席・遅刻・早退日 2:受付日

        /** 勤怠備考のコードと名称のマップ */
        private Map _diRemarkMap;

        /** 校時のコードと名称のマップ */
        private Map _periodNameMap;

        /** 勤怠のコードと名称のマップ */
        private Map _diNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _fromDate = request.getParameter("SDATE").replace('/', '-');
            _toDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE");
            _hiduke = request.getParameter("HIDUKE");

            _diRemarkMap = setDiRemarkMap(db2);
            _periodNameMap = setPeriodNameMap(db2);
            _diNameMap = setDiNameMap(db2);
        }

        /**
         * _fromDateから_toDateまでの日付をunionしたSQLを得る。
         * @return
         */
        public String getDatesFromToSql() {

            Date dateFrom = Date.valueOf(_fromDate);
            Date dateTo = Date.valueOf(_toDate);
            if (dateFrom.after(dateTo)) {
                Date temp = dateFrom;
                dateFrom = dateTo;
                dateTo = temp;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFrom);

            final StringBuffer stb = new StringBuffer();
            stb.append("DATES (CHECKDATE) AS (");
            String union = "";
            while (cal.getTime().compareTo(dateTo) <= 0) {
                stb.append(union).append("VALUES('" + sdf.format(cal.getTime()) + "')");
                union = " UNION ";
                cal.add(Calendar.DATE, 1);
            }
            stb.append(")");
            return stb.toString();
        }

        /**
         * 画面で"受付日"を選択しているか
         * @return
         */
        public boolean isCheckFirstdate() {
            return "2".equals(_param._hiduke);
        }

        public String getPeriodName(String periodCd) {
            return _periodNameMap.containsKey(periodCd) ? (String) _periodNameMap.get(periodCd) : "";
        }

        public String getDiRemark(String diRemarkCd) {
            return (String) _diRemarkMap.get(diRemarkCd);
        }

        public String getDiName(String diCd) {
            return (String) _diNameMap.get(diCd);
        }

        private Map setDiRemarkMap(DB2UDB db2) {
            final Map diRemarkMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2 AS DI_REMARK_CD, NAME1 AS DI_REMARK_NAME FROM NAME_MST WHERE NAMECD1 = 'C900' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String diRemarkCd = rs.getString("DI_REMARK_CD");
                    final String diRemarkName = rs.getString("DI_REMARK_NAME");
                    diRemarkMap.put(diRemarkCd, diRemarkName);
                }
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return diRemarkMap;
        }

        private Map setPeriodNameMap(DB2UDB db2) {
            final Map diRemarkMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2 AS PERIODCD, NAME1 AS PERIODNAME FROM NAME_MST WHERE NAMECD1 = 'B001' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    diRemarkMap.put(rs.getString("PERIODCD"), rs.getString("PERIODNAME"));
                }
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return diRemarkMap;
        }

        private Map setDiNameMap(DB2UDB db2) {
            final Map diNameMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT DI_CD, DI_NAME1 FROM ATTEND_DI_CD_DAT WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    diNameMap.put(rs.getString("DI_CD"), rs.getString("DI_NAME1"));
                }
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return diNameMap;
        }
    }
}

// eof
