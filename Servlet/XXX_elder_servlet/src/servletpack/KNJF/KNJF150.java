// kanji=漢字
/*
 * $Id: 52c2f63190798dd5e263681a4199dedd5f06986e $
 *
 * 作成日: 2010/05/24 13:39:29 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJF150 {

    private static final Log log = LogFactory.getLog("KNJF150.class");

    private boolean _hasData;

    Param _param;

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
            closeDb(db2);
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

    private void setSvfForm(final Vrw32alp svf) {
    	final String miyaIdx = _param._isMiyagiken ? "" : "_2";
        String formFile = "KNJF150_" + _param._type + miyaIdx + ".frm";
        log.debug("フォーム：" + formFile);
        svf.VrSetForm(formFile, 1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List students = createStudents(db2);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            //フォーム
            setSvfForm(svf);
            //ヘッダ
            printHeader(svf, db2, student);
            //保健室来室記録
            if (_param.isPrintNurseoff()) {
                printNurseoff(db2, svf, student);
            }

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private void printHeader(final Vrw32alp svf, final DB2UDB db2, final Student student) {
        svf.VrsOut("DATE"       , _param.getDateWareki(db2, _param._ctrlDate));
        svf.VrsOut("HR_NAME"    , student.getHrName());
        svf.VrsOut("ATTENDNO"   , student.getAttendNo());
        svf.VrsOut("NAME"       , student._name);
    }

    private void printNurseoff(final DB2UDB db2, final Vrw32alp svf, final Student student) throws SQLException {
        final String sql = sqlNurseoff(student);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                printDeteil(svf, rs);
            }
        } catch (final Exception ex) {
            log.error("保健室来室記録のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlNurseoff(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        if (!_param._isMiyagiken) {
        	stb.append("   , T2.REMARK1 AS OCCUR_PLACE_TEXT2, ");
        	stb.append("     T2.REMARK2 AS RESTTIME_TEXT, ");
        	stb.append("     T2.REMARK3 AS OCCUR_CAUSE_TEXT, ");
        	stb.append("     T2.REMARK4 AS RESULT_RETCLS, ");
        	stb.append("     T2.REMARK5 AS RELATIONSHIP_TEXT ");
        }
        stb.append(" FROM ");
        stb.append("     NURSEOFF_VISITREC_DAT T1 ");
        if (!_param._isMiyagiken) {
        	stb.append("     LEFT JOIN NURSEOFF_VISITREC_DETAIL_DAT T2 ");
        	stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        	stb.append("      AND T2.VISIT_DATE = T1.VISIT_DATE ");
        	stb.append("      AND T2.VISIT_HOUR = T1.VISIT_HOUR ");
        	stb.append("      AND T2.VISIT_MINUTE = T1.VISIT_MINUTE ");
        	stb.append("      AND T2.TYPE = T1.TYPE");
        	stb.append("      AND T2.SEQ = '10' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("     T1.VISIT_DATE = date('" + _param._visitDate + "') AND ");
        stb.append("     T1.VISIT_HOUR = '" + _param._visitHour + "' AND ");
        stb.append("     T1.VISIT_MINUTE = '" + _param._visitMinute + "' AND ");
        stb.append("     T1.TYPE = '" + _param._type + "' ");
        return stb.toString();
    }

    private void printDeteil(final Vrw32alp svf, ResultSet rs) throws SQLException {
        //来室日時
        svf.VrsOut("VISIT_DATE"     , _param.getDateSeireki(rs.getString("VISIT_DATE")) );
        svf.VrsOut("VISIT_HOUR"     , rs.getString("VISIT_HOUR") );
        svf.VrsOut("VISIT_MINUTE"   , rs.getString("VISIT_MINUTE") );
        svf.VrsOut("VISIT_PERIODCD" , getNameMst("F700", rs.getString("VISIT_PERIODCD")) );
        //来室理由
        String namecd = "";
        if (_param.isNaika()) namecd = "F200";
        else if (_param.isGeka()) namecd = "F201";
        else if (_param.isSonota()) namecd = "F203";
        else if (_param.isSeitoIgai()) namecd = "F202";
        else if (_param.isKenkouSoudan()) namecd = "F219";
        svf.VrsOut("VISIT_REASON1"  , getNameMst(namecd, rs.getString("VISIT_REASON1")) + getTextData(rs.getString("VISIT_REASON1_TEXT")) );
        svf.VrsOut("VISIT_REASON2"  , getNameMst(namecd, rs.getString("VISIT_REASON2")) + getTextData(rs.getString("VISIT_REASON2_TEXT")) );
        svf.VrsOut("VISIT_REASON3"  , getNameMst(namecd, rs.getString("VISIT_REASON3")) + getTextData(rs.getString("VISIT_REASON3_TEXT")) );
        //処置
        namecd = "";
        if (_param.isNaika()) namecd = "F208";
        else if (_param.isGeka()) namecd = "F209";
        else if (_param.isSonota()) namecd = "F210";
        else if (_param.isSeitoIgai()) namecd = "F210";
        else if (_param.isKenkouSoudan()) namecd = "F220";
        svf.VrsOut("TREATMENT1"     , getNameMst(namecd, rs.getString("TREATMENT1")) + getTextData(rs.getString("TREATMENT1_TEXT")) );
        svf.VrsOut("TREATMENT2"     , getNameMst(namecd, rs.getString("TREATMENT2")) + getTextData(rs.getString("TREATMENT2_TEXT")) );
        svf.VrsOut("TREATMENT3"     , getNameMst(namecd, rs.getString("TREATMENT3")) + getTextData(rs.getString("TREATMENT3_TEXT")) );
        //休養・相談時間
        svf.VrsOut("RESTTIME"       , getNameMst("F212", rs.getString("RESTTIME")) + (!_param._isMiyagiken ? getTextData(rs.getString("RESTTIME_TEXT")) : "") );
        //退出時間
        svf.VrsOut("LEAVE_HOUR"     , rs.getString("LEAVE_HOUR") );
        svf.VrsOut("LEAVE_MINUTE"   , rs.getString("LEAVE_MINUTE") );
        svf.VrsOut("LEAVE_PERIODCD" , getNameMst("F700", rs.getString("LEAVE_PERIODCD")) );
        //処置結果
        svf.VrsOut("RESULT_REST"    , getResult(rs.getString("RESULT_REST")) );
        svf.VrsOut("RESULT_EARLY"   , getResult(rs.getString("RESULT_EARLY")) );
        svf.VrsOut("RESULT_MEDICAL" , getResult(rs.getString("RESULT_MEDICAL")) );
        if (!_param._isMiyagiken) {
            svf.VrsOut("RESULT_RETCLS" , getResult(rs.getString("RESULT_RETCLS")) );
        }
        //連絡
        svf.VrsOut("CONTACT"        , getNameMst("F213", rs.getString("CONTACT")) );
        if ((_param._isMiyagiken && (_param.isNaika() || _param.isGeka()))
        	 || !_param._isMiyagiken) {
            svf.VrsOut("CONTACT2"       , getNameMst("F213", rs.getString("CONTACT2")) );
            svf.VrsOut("CONTACT3"       , getNameMst("F213", rs.getString("CONTACT3")) );
        }
        //医療機関
        svf.VrsOut("HOSPITAL"       , rs.getString("HOSPITAL") );
        svf.VrsOut("COMPANION"      , rs.getString("COMPANION") );
        svf.VrsOut("COMPANION_DIV"  , getNameMst("F218", rs.getString("COMPANION_DIV")) );
        svf.VrsOut("DIAGNOSIS"      , rs.getString("DIAGNOSIS") );
        //特記事項
        svf.VrsOut("SPECIAL_NOTE"   , rs.getString("SPECIAL_NOTE") );
        //原因
        if (_param.isNaika()) {
            svf.VrsOut("OCCUR_CAUSE"    , getNameMst("F204", rs.getString("OCCUR_CAUSE")) + (!_param._isMiyagiken ? getTextData(rs.getString("OCCUR_CAUSE_TEXT")) : "") );
        }
        //体調
        if (_param.isNaika() || _param.isGeka() || _param.isSonota() || _param.isKenkouSoudan()) {
            svf.VrsOut("CONDITION1"     , getCondition("1", rs.getString("CONDITION1")) );
            svf.VrsOut("SLEEPTIME"      , rs.getString("SLEEPTIME") );
            svf.VrsOut("CONDITION3"     , getCondition("3", rs.getString("CONDITION3")) );
            svf.VrsOut("CONDITION4"     , getCondition("4", rs.getString("CONDITION4")) );
            svf.VrsOut("MEAL"           , rs.getString("MEAL") );
            svf.VrsOut("CONDITION5"     , getCondition("5", rs.getString("CONDITION5")) );
            svf.VrsOut("CONDITION6"     , rs.getString("CONDITION6") );
        }
        //体温
        if (_param.isNaika() || _param.isGeka() || _param.isSonota() || _param.isKenkouSoudan()) {
            svf.VrsOut("TEMPERATURE1"   , rs.getString("TEMPERATURE1") );
            svf.VrsOut("MEASURE_HOUR1"  , rs.getString("MEASURE_HOUR1") );
            svf.VrsOut("MEASURE_MINUTE1", rs.getString("MEASURE_MINUTE1") );
            svf.VrsOut("TEMPERATURE2"   , rs.getString("TEMPERATURE2") );
            svf.VrsOut("MEASURE_HOUR2"  , rs.getString("MEASURE_HOUR2") );
            svf.VrsOut("MEASURE_MINUTE2", rs.getString("MEASURE_MINUTE2") );
            svf.VrsOut("TEMPERATURE3"   , rs.getString("TEMPERATURE3") );
            svf.VrsOut("MEASURE_HOUR3"  , rs.getString("MEASURE_HOUR3") );
            svf.VrsOut("MEASURE_MINUTE3", rs.getString("MEASURE_MINUTE3") );
        }
        //発生（日時・場所・状況）
        if (_param.isNaika() || _param.isGeka()) {
            svf.VrsOut("OCCUR_DATE"     , _param.getDateSeireki(rs.getString("OCCUR_DATE")) );
            svf.VrsOut("OCCUR_HOUR"     , rs.getString("OCCUR_HOUR") );
            svf.VrsOut("OCCUR_MINUTE"   , rs.getString("OCCUR_MINUTE") );
            svf.VrsOut("OCCUR_PLACE"    , getNameMst("F206", rs.getString("OCCUR_PLACE"))  + (!_param._isMiyagiken ? getTextData(rs.getString("OCCUR_PLACE_TEXT2")) : "") );
            svf.VrsOut("OCCUR_SITUATION", rs.getString("OCCUR_SITUATION") );
        }
        //発生時の行動
        if (_param.isGeka()) {
            svf.VrsOut("OCCUR_ACT"          , getNameMst("F216", rs.getString("OCCUR_ACT")) );
            svf.VrsOut("OCCUR_ACT_DETAIL"   , getOccurActDetail(rs.getString("OCCUR_ACT"), rs.getString("OCCUR_ACT_DETAIL")) );
        }
        //けがの場所
        if (_param.isGeka()) {
            svf.VrsOut("INJURY_PART1"  , getNameMst("F207", rs.getString("INJURY_PART1")) + getTextData(rs.getString("INJURY_PART1_TEXT")) );
            svf.VrsOut("INJURY_PART2"  , getNameMst("F207", rs.getString("INJURY_PART2")) + getTextData(rs.getString("INJURY_PART2_TEXT")) );
            svf.VrsOut("INJURY_PART3"  , getNameMst("F207", rs.getString("INJURY_PART3")) + getTextData(rs.getString("INJURY_PART3_TEXT")) );
        }
        //相談者名・生徒との関係・相談方法
        if (_param.isSeitoIgai()) {
            svf.VrsOut("CONSULTATION_NAME"  , rs.getString("CONSULTATION_NAME") );
            svf.VrsOut("RELATIONSHIP"       , getNameMst("F214", rs.getString("RELATIONSHIP")) + (!_param._isMiyagiken ? getTextData(rs.getString("RELATIONSHIP_TEXT")) : "") );
            svf.VrsOut("CONSULTATION_METHOD", getNameMst("F215", rs.getString("CONSULTATION_METHOD")) );
        }
    }

    private String getTextData(final String textData) throws SQLException {
        String retTxt = StringUtils.defaultString(textData);
        if (!"".equals(retTxt)) {
            retTxt = "(" + retTxt + ")";
        }
        return retTxt;
    }

    private String getNameMst(final String namecd1, final String namecd2) {
        if (null == namecd1 || null == namecd2) return "";
        final String namecd = namecd1 + "-" + namecd2;
        if (_param._nameMstMap.containsKey(namecd)) return (String) _param._nameMstMap.get(namecd);
        return "";
    }

    private String getCondition(final String no, final String condition) {
        if ("1".equals(no)) {
            if ("1".equals(condition)) return "はい";
            else if ("2".equals(condition)) return "いいえ";
            else if ("3".equals(condition)) return "余り眠れない";
        }
        if ("3".equals(no)) {
            if ("1".equals(condition)) return "出た";
            else if ("2".equals(condition)) return "出ていない";
            else if ("3".equals(condition)) return "便秘";
        }
        if ("4".equals(no)) {
            if ("1".equals(condition)) return "食べた";
            else if ("2".equals(condition)) return "食べていない";
            else if ("3".equals(condition)) return "いつも食べない";
        }
        if ("5".equals(no)) {
            if ("1".equals(condition)) return "ある";
            else if ("2".equals(condition)) return "ない";
        }
        return "";
    }

    private String getResult(final String result) {
        return null != result ? "レ" : "";
    }

    private String getOccurActDetail(final String occurAct, final String occurActDetail) {
        if ("1".equals(getNameSpare("F216", occurAct))) {
            return getNameMst("B001", occurActDetail);
        }
        if ("2".equals(getNameSpare("F216", occurAct))) {
            return getClubMst(occurActDetail);
        }
        return getNameMst("F217", occurActDetail);
    }

    private String getNameSpare(final String namecd1, final String namecd2) {
        if (null == namecd1 || null == namecd2) return "";
        final String namecd = namecd1 + "-" + namecd2;
        if (_param._nameSpareMap.containsKey(namecd)) return (String) _param._nameSpareMap.get(namecd);
        return "";
    }

    private String getClubMst(final String clubcd) {
        if (null == clubcd) return "";
        if (_param._clubMstMap.containsKey(clubcd)) return (String) _param._clubMstMap.get(clubcd);
        return "";
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");

                final Student student = new Student(schregno, name, attendNo, hrName, grade, hrClass);
                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlStudents() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                                   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("                                   AND T3.GRADE = T1.GRADE ");
        stb.append("                                   AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + _param._schregno + "' AND ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._semester + "' ");
        return stb.toString();
    }

    private class Student {
        private final String _schregno;
        private final String _name;
        private final String _attendNo;
        private final String _hrName;
        private final String _grade;
        private final String _hrClass;

        public Student(
                final String schregno,
                final String name,
                final String attendNo,
                final String hrName,
                final String grade,
                final String hrClass
        ) {
            _schregno = schregno;
            _name = name;
            _attendNo = attendNo;
            _hrName = hrName;
            _grade = grade;
            _hrClass = hrClass;
        }

        public String getHrName() {
            return (_hrName != null) ? _hrName : "";
        }

        public String getAttendNo() {
            return String.valueOf(Integer.parseInt(_attendNo));
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 60595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schregno;
        private final String _visitDate;
        private final String _visitHour;
        private final String _visitMinute;
        private final String _type;
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final Map _nameMstMap;
        private final Map _clubMstMap;
        private Map _nameSpareMap;
        private final String _schName;
        private final boolean _isMiyagiken;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schregno = request.getParameter("SCHREGNO");
            String visitDate = request.getParameter("PRINT_VISIT_DATE");
            _visitDate = visitDate.replace('/', '-');
            _visitHour = request.getParameter("PRINT_VISIT_HOUR");
            _visitMinute = request.getParameter("PRINT_VISIT_MINUTE");
            _type = request.getParameter("TYPE");
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _nameMstMap = createNameMstMap(db2);
            _clubMstMap = createClubMstMap(db2);
            _schName = getNameMstZ010(db2);
            _isMiyagiken = "miyagiken".equals(_schName) ? true : false;
        }

        private boolean isNaika() {
            return "1".equals(_type);
        }

        private boolean isGeka() {
            return "2".equals(_type);
        }

        private boolean isSonota() {
            return "3".equals(_type);
        }

        private boolean isSeitoIgai() {
            return "4".equals(_type);
        }

        private boolean isKenkouSoudan() {
            return "5".equals(_type);
        }

        private boolean isPrintNurseoff() {
            return null != _visitDate && !"".equals(_visitDate);
        }

        private String getDateWareki(final DB2UDB db2, final String date) {
            if (null == date || "".equals(date)) {
                return "";
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String getDateSeireki(final String date) {
            if (null == date || "".equals(date)) {
                return "";
            }
            return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) + "(" + KNJ_EditDate.h_format_W(date) + ")";
        }

        private Map createNameMstMap(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            _nameSpareMap = new HashMap();
            final String sql = sqlNameMst();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd = rs.getString("NAMECD1") + "-" + rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final String namespare = rs.getString("NAMESPARE1");

                    rtn.put(namecd, name);
                    _nameSpareMap.put(namecd, namespare);
                }
            } catch (final Exception ex) {
                log.error("名称マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlNameMst() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1, ");
            stb.append("     NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     NAMECD1 in ('B001', ");
            stb.append("                 'F200', ");
            stb.append("                 'F201', ");
            stb.append("                 'F202', ");
            stb.append("                 'F203', ");
            stb.append("                 'F204', ");
            stb.append("                 'F206', ");
            stb.append("                 'F207', ");
            stb.append("                 'F208', ");
            stb.append("                 'F209', ");
            stb.append("                 'F210', ");
            stb.append("                 'F212', ");
            stb.append("                 'F213', ");
            stb.append("                 'F214', ");
            stb.append("                 'F215', ");
            stb.append("                 'F216', ");
            stb.append("                 'F217', ");
            stb.append("                 'F218', ");
            stb.append("                 'F219', ");
            stb.append("                 'F220', ");
            stb.append("                 'F700') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map createClubMstMap(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            final String sql = sqlClubMst();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String clubcd = rs.getString("CLUBCD");
                    final String clubName = rs.getString("CLUBNAME");

                    rtn.put(clubcd, clubName);
                }
            } catch (final Exception ex) {
                log.error("部活マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlClubMst() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     T2.CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     CLUB_YDAT T1 ");
            stb.append("     INNER JOIN CLUB_MST T2 ON T1.CLUBCD = T2.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLUBCD ");
            return stb.toString();
        }

        private String getNameMstZ010(final DB2UDB db2) throws SQLException {
        	String retStr = "";
            final String sql = sqlGetNameMstZ010();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                	retStr = rs.getString("NAME1");
                }
            } catch (final Exception ex) {
                log.error("名称マスタ(Z010)のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }
        private String sqlGetNameMstZ010() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND NAMECD1 = 'Z010' ");
            stb.append("     AND NAMECD2 = '00' ");
            return stb.toString();
        }
    }
}

// eof
