// kanji=漢字
/*
 * $Id: 04b7fe1585477cbc2a2d61db8077f765fbf21027 $
 *
 * 作成日: 2011/10/07 15:05:15 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 04b7fe1585477cbc2a2d61db8077f765fbf21027 $
 */
public class KNJL212C {

    private static final Log log = LogFactory.getLog("KNJL212C.class");

    private boolean _hasData;
    private final String FRM_NAME = "KNJL212C.frm";
    private final String FRM_NAME2 = "KNJL212C_2.frm";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if ("1".equals(_param._pretest_bus_Not_Hyouji)) {
            svf.VrSetForm(FRM_NAME2, 1);
        } else {
            svf.VrSetForm(FRM_NAME, 1);
        }
        final List subclassList = getSubclassList(db2);
        final List list = getExamineeList(db2);
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PretestExaminee e = (PretestExaminee) it.next();
            //ヘッダ
            svf.VrsOut("DATE", _param.getDate(db2));
            svf.VrsOut("ZIPCD", e._zipcd);
            svf.VrsOut("ADDR1" + (getMS932ByteCount(e._address1) > 40 ? "_2" : ""), e._address1);
            svf.VrsOut("ADDR2" + (getMS932ByteCount(e._address2) > 40 ? "_2" : ""), e._address2);
            svf.VrsOut("ADDRESSEE" + (getMS932ByteCount(e._gname) > 24 ? "2" : ""), e._gname);
            svf.VrsOut("HR_ATTNO_NAME", e._name);
            svf.VrsOut("SCHOOLNAME", _param._schoolName);
            svf.VrsOut("NENDO", _param.getYear(db2));
            svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
            svf.VrsOut("NAME" + (getMS932ByteCount(e._name) > 30 ? "3" : getMS932ByteCount(e._name) > 20 ? "2" : "1"), e._name);
            svf.VrsOut("NO", e._preReceptno);
            svf.VrsOut("FINSCHOOL" + (getMS932ByteCount(e._finschoolName) > 20 ? "2" : "1"), e._finschoolName);
            svf.VrsOut("EXAMCOURSE_NAME1", e._preExamTypeName);
            if ("1".equals(e._busUse)) {
                if ("1".equals(e._stationdiv)) {
                    svf.VrsOut("BUSONOFF", "南海林間田園都市駅");
                } else if ("2".equals(e._stationdiv)) {
                    svf.VrsOut("BUSONOFF", "近鉄福神駅");
                } else if ("3".equals(e._stationdiv)) {
                    svf.VrsOut("BUSONOFF", "JR五条駅");
                }
                svf.VrsOut("BUSUSE1", e._busUserCount);
            }
            svf.VrsOut("TRAINSTART1", _param._sTimeFukujin);
            svf.VrsOut("TRAINSTART2", _param._sTimeRinkan);
            svf.VrsOut("TRAINSTART3", _param._sTimeGojou);
            svf.VrsOut("TRAINSTART4", _param._sTimeGakuen);
            svf.VrsOut("EXAMDATE", _param.getExamDate(db2));
            svf.VrsOut("LIMITTIME", _param.getLimitTime());
            if ("2".equals(_param._preTestDiv)) {
                svf.VrsOut("COMMENT", "");
            } else {
                svf.VrsOut("COMMENT", "＊Ⅱ型受験者は退室して下さい。");
            }

            int subcnt = 1;
            for (final Iterator itSub = subclassList.iterator(); itSub.hasNext();) {
                final SubclassData subclassData = (SubclassData) itSub.next();
                final String cd = String.valueOf(subcnt);

                svf.VrsOut("EXAMTIME" + cd, subclassData.getExamTime());
                svf.VrsOut("CLASS" + cd, subclassData._subclassName);

                subcnt++;
            }

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getSubclassList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NAMECD2, ");
        stb.append("     NAME1, ");
        stb.append("     NAMESPARE1, ");
        stb.append("     NAMESPARE2 ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND NAMECD1 = 'L109' ");
        stb.append("     AND ABBV3 = '" + _param._preTestDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     NAMECD2 ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd = rs.getString("NAMECD2");
                final String subclassName = rs.getString("NAME1");
                final String sTime = rs.getString("NAMESPARE1");
                final String eTime = rs.getString("NAMESPARE2");

                final SubclassData subclassData = new SubclassData(subclassCd, subclassName, sTime, eTime);
                retList.add(subclassData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private class SubclassData {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _sTime;
        private final String _eTime;

        public SubclassData(
                final String subclassCd,
                final String subclassName,
                final String sTime,
                final String eTime
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _sTime = sTime;
            _eTime = eTime;
        }

        private String getExamTime() {
            if (null == _sTime || null == _eTime) return "";
            return _sTime + "\uFF5E" + _eTime;
        }

        public String toString() {
            return _subclassCd + " : " + _subclassName;
        }
    }

    private List getExamineeList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = sqlEntexamApplicantBasePreDat();
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String preReceptno = rs.getString("PRE_RECEPTNO");
                final String preExamType = rs.getString("PRE_EXAM_TYPE");
                final String preExamTypeName = rs.getString("PRE_EXAM_TYPE_NAME");
                final String name = rs.getString("NAME");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String bususe = rs.getString("BUS_USE");
                final String bususercount = rs.getString("BUS_USER_COUNT");
                final String stationdiv = rs.getString("STATIONDIV");

                final PretestExaminee examinee = new PretestExaminee(preReceptno, preExamType, preExamTypeName, name, gname, zipcd, address1, address2, finschoolName, bususe, bususercount, stationdiv);
                list.add(examinee);
            }

        } catch (Exception ex) {
            log.error("setSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String sqlEntexamApplicantBasePreDat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.PRE_RECEPTNO, ");
        stb.append("     T1.PRE_EXAM_TYPE, ");
        stb.append("     N1.NAME1 AS PRE_EXAM_TYPE_NAME, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.GNAME, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDRESS1, ");
        stb.append("     T1.ADDRESS2, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     L1.FINSCHOOL_NAME, ");
        stb.append("     T1.BUS_USE, ");
        stb.append("     T1.BUS_USER_COUNT, ");
        stb.append("     T1.STATIONDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L105' AND N1.NAMECD2 = T1.PRE_EXAM_TYPE ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.PRE_TESTDIV = '" + _param._preTestDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.PRE_RECEPTNO ");
        return stb.toString();
    }

    private class PretestExaminee {
        final String _preReceptno;
        final String _preExamType;
        final String _preExamTypeName;
        final String _name;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _finschoolName;
        final String _busUse;
        final String _busUserCount;
        final String _stationdiv;

        PretestExaminee(
                final String preReceptno,
                final String preExamType,
                final String preExamTypeName,
                final String name,
                final String gname,
                final String zipcd,
                final String address1,
                final String address2,
                final String finschoolName,
                final String bususe,
                final String bususercount,
                final String stationdiv
        ) {
            _preReceptno = preReceptno;
            _preExamType = preExamType;
            _preExamTypeName = preExamTypeName;
            _name = name;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _finschoolName = finschoolName;
            _busUse = bususe;
            _busUserCount = bususercount;
            _stationdiv = stationdiv;
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
        log.fatal("$Revision: 70093 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _applicantdiv;
        private final String _ctrlDate;
        private final String _preTestDiv;
        private final String _sTimeFukujin;
        private final String _sTimeRinkan;
        private final String _sTimeGojou;
        private final String _sTimeGakuen;
        private final boolean _seirekiFlg;
        private final String _schoolName;
        private String _applicantdivname;
        private String _examDate;
        private String _limitTime;
        private final String _pretest_bus_Not_Hyouji;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantdiv = "1";
            _ctrlDate = request.getParameter("CTRL_DATE");
            _preTestDiv = request.getParameter("PRE_TESTDIV");
            _sTimeRinkan = getStationTime(request.getParameter("RINKAN_HOUR"), request.getParameter("RINKAN_MINUTE"));
            _sTimeFukujin = getStationTime(request.getParameter("FUKUJIN_HOUR"), request.getParameter("FUKUJIN_MINUTE"));
            _sTimeGojou = getStationTime(request.getParameter("GOJOU_HOUR"), request.getParameter("GOJOU_MINUTE"));
            _sTimeGakuen = getStationTime(request.getParameter("GAKUEN_HOUR"), request.getParameter("GAKUEN_MINUTE"));
            _seirekiFlg = getSeirekiFlg(db2);
            _schoolName = getSchoolName(db2);
            _pretest_bus_Not_Hyouji = request.getParameter("Pretest_bus_Not_Hyouji");
            setExamDate(db2);
        }
        /**
         * 日付表示の和暦(年号)/西暦使用フラグ
         * @param db2
         * @return
         */
        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getYear(DB2UDB db2) {
            return _seirekiFlg ? _year + "年度": KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度";
        }

        /**
         * 日付の名称を得る
         * @return 日付の名称
         */
        private String getDate(DB2UDB db2) {
            return _seirekiFlg ?
                    (_ctrlDate.substring(0,4) + "年" + KNJ_EditDate.h_format_JP_MD(_ctrlDate)) : (KNJ_EditDate.h_format_JP(db2, _ctrlDate));
        }

        /*
         * 年度と入試制度から学校名を返す
         */
        private String getSchoolName(DB2UDB db2) {
            String name = null;

            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '105' ");
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error(ex);
            }

            return name;
        }

        private void setExamDate(DB2UDB db2) {
            _applicantdivname = null;
            _examDate = null;
            _limitTime = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1,NAMESPARE1,NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'L103' AND NAMECD2 = '" + _applicantdiv + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _applicantdivname = rs.getString("NAME1");
                    _examDate = rs.getString("NAMESPARE1");
                    _limitTime = rs.getString("NAMESPARE2");
                }
            } catch (Exception e) {
                log.error("getSubclassName Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getExamDate(DB2UDB db2) {
            if (null == _examDate) return "";
            String date = "";
            String dateArray[] = KNJ_EditDate.tate2_format(KNJ_EditDate.h_format_JP(db2, _examDate));
            for (int ia = 0; ia < dateArray.length; ia++) {
                if (ia == 1 || ia == 3 || ia == 5) {
                    date = date + repSuuji(dateArray[ia]);
                } else {
                    date = date + dateArray[ia];
                }
            }
            String week = "（" + KNJ_EditDate.h_format_W(_examDate) + "）";
            return date + week;
        }

        private String getLimitTime() {
            if (null == _limitTime) return "";
            return _limitTime;
        }

        private String getStationTime(String hour, String minute) {
            if (null == hour || null == minute) return "";
            return repSuuji(hour) + "：" + repSuuji(minute);
        }

        /**
         * 数字を半角から全角に変換
         * @return 全角数字
         */
        private String repSuuji(String suuji) {
            StringBuffer stb = new StringBuffer(suuji);
            String moji = new String();
            String zenkaku[] = {"０","１","２","３","４","５","６","７","８","９"};

            for (int ia = 0; ia < stb.length(); ia++) {
                moji = stb.substring(ia, ia+1);
                if (null != moji) {
                    stb.replace(ia, ia+1, zenkaku[Integer.parseInt(moji)]);
                }
            }

            return stb.toString();
        }//replace_charの括り

    }
}

// eof
