/*
 * $Id: d9b42f5477e97db57d91a83225367883b8508336 $
 *
 * 作成日: 2014/04/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJG;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*
 *  学校教育システム 賢者 [事務管理] 証明書
 */
public class KNJG046D {

    private static final Log log = LogFactory.getLog(KNJG046D.class);

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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

            printMain(svf, db2);
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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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

    private void printMain(final Vrw32alp svf, final DB2UDB db2) throws ParseException {
        svf.VrSetForm("KNJG046D.frm", 1);

        final List schoolDiaryList = getSchoolDiaryList(db2);
        for (final Iterator it = schoolDiaryList.iterator(); it.hasNext();) {
            final SchoolDiary schoolDiary = (SchoolDiary) it.next();
            schoolDiary.load(db2, _param);
            int row = 1;
            int col = 1;

            //svf.VrsOut("DATE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度"); //年度

            final String[] date = KNJ_EditDate.tate_format4(db2, schoolDiary._diaryDate);

            final String youbi = KNJ_EditDate.h_format_W(schoolDiary._diaryDate);
            svf.VrsOut("DATE", date[0] + date[1] + "年" + date[2] + "月" + date[3] + "日　" + youbi + "曜"); //日付
            svf.VrsOut("WEATHER", schoolDiary._weather); //天気
            svf.VrsOut("JOB_NAME1", (String)_param._nameMapD055.get("01")); //役職名
            svf.VrsOut("JOB_NAME2", (String)_param._nameMapD055.get("02")); //役職名
            svf.VrsOut("JOB_NAME3", (String)_param._nameMapD055.get("03")); //役職名

            VrsOutnRenban(svf, "ARTICLE", knjobj.retDividString(schoolDiary._news, 100, 11)); //記事
            VrsOutnRenban(svf, "VISIT_NAME1", knjobj.retDividString(schoolDiary._visitName, 50, 4)); //来校者
            VrsOutnRenban(svf, "VISIT_MATTER1", knjobj.retDividString(schoolDiary._visitMatter, 50, 4)); //来校者用件
            VrsOutnRenban(svf, "ETC", knjobj.retDividString(schoolDiary._etc, 100, 4)); //その他

            row = 1;
            for (final Iterator it2 = schoolDiary._shutchoList.iterator(); it2.hasNext();) {
                final SchoolDiaryDetailSeq shutcho = (SchoolDiaryDetailSeq) it2.next();
                svf.VrsOutn("TRIP_PERSON", row, shutcho._remark1); //出張者
                svf.VrsOutn("TRIP_PLACE", row, shutcho._remark2); //出張地
                svf.VrsOutn("MATTER", row, shutcho._remark3); //用件
                row++;
            }

            row = 1;
            col = 1;
            for (final Iterator it2 = schoolDiary._kyukaList.iterator(); it2.hasNext();) {
                final SchoolDiaryDetailSeq kyuka = (SchoolDiaryDetailSeq) it2.next();
                svf.VrsOutn("STAFF_NAME" + String.valueOf(col), row, kyuka ._remark1); //職員名
                svf.VrsOutn("KIND_NAME" + String.valueOf(col), row, kyuka ._remark2); //種類
                svf.VrsOutn("REASON" + String.valueOf(col), row, kyuka ._remark3); //事由等
                svf.VrsOutn("TIME" + String.valueOf(col), row, kyuka ._remark4); //時間

                col = row >= 10 ? col+1 : col;
                row = row >= 10 ? 1 : row+1;
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }
    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    private List getSchoolDiaryList(final DB2UDB db2) {
        final List schoolDiaryList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchoolDiarySql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String diaryDate = rs.getString("DIARY_DATE");
                final String weather = rs.getString("WEATHER");
                final String weather2 = rs.getString("WEATHER2");
                final String news = rs.getString("NEWS");

                final String visitName = rs.getString("VISIT_NAME");
                final String visitMatter = rs.getString("VISIT_MATTER");
                final String etc = rs.getString("ETC");

                final SchoolDiary schoolDiary = new SchoolDiary(diaryDate, weather, weather2, news, visitName, visitMatter, etc);
                schoolDiaryList.add(schoolDiary);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return schoolDiaryList;
    }

    private String getSchoolDiarySql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.DIARY_DATE, ");
        stb.append("     A006_1.NAME1 AS WEATHER, ");
        stb.append("     A006_2.NAME1 AS WEATHER2, ");
        stb.append("     T1.NEWS, ");
        stb.append("     L1.REMARK1 AS VISIT_NAME, ");
        stb.append("     L1.REMARK2 AS VISIT_MATTER, ");
        stb.append("     L2.REMARK3 AS ETC ");
        stb.append(" FROM ");
        stb.append("     SCHOOL_DIARY_DAT T1 ");
        stb.append("     LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L1 ");
        stb.append("            ON L1.SCHOOLCD    = T1.SCHOOLCD ");
        stb.append("           AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("           AND L1.DIARY_DATE  = T1.DIARY_DATE ");
        stb.append("           AND L1.SEQ         = '001' ");
        stb.append("     LEFT JOIN SCHOOL_DIARY_DETAIL_SEQ_DAT L2 ");
        stb.append("            ON L2.SCHOOLCD    = T1.SCHOOLCD ");
        stb.append("           AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("           AND L2.DIARY_DATE  = T1.DIARY_DATE ");
        stb.append("           AND L2.SEQ         = '002' ");
        stb.append("     LEFT JOIN NAME_MST A006_1 ");
        stb.append("            ON A006_1.NAMECD2 = T1.WEATHER ");
        stb.append("           AND A006_1.NAMECD1 = 'A006' ");
        stb.append("     LEFT JOIN NAME_MST A006_2 ");
        stb.append("            ON A006_2.NAMECD2 = T1.WEATHER2 ");
        stb.append("           AND A006_2.NAMECD1 = 'A006' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD        = '" + param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("     AND T1.DIARY_DATE BETWEEN '" + param._sdate + "' AND '" + param._edate + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.DIARY_DATE ");
        return stb.toString();
    }

    /**
     * 学校日誌
     */
    private static class SchoolDiary {


        final String _diaryDate;
        final String _weather;
        final String _weather2;
        final String _news;
        final String _visitName;
        final String _visitMatter;
        final String _etc;

        List _shutchoList = Collections.EMPTY_LIST; //出張者
        List _kyukaList = Collections.EMPTY_LIST; //休暇等

        public SchoolDiary(final String diaryDate, final String weather, final String weather2, final String news, final String visitName, final String visitMatter, final String etc) {
            _diaryDate = diaryDate;
            _weather = weather;
            _weather2 = weather2;
            _news = news;
            _visitName = visitName;
            _visitMatter = visitMatter;
            _etc = etc;
        }

        public void load(final DB2UDB db2, final Param param) {
            _shutchoList = SchoolDiaryDetailSeq.getSchoolDiaryDetailSeqList(db2, param, _diaryDate, "shutcho");
            _kyukaList = SchoolDiaryDetailSeq.getSchoolDiaryDetailSeqList(db2, param, _diaryDate, "kyuka");
        }

    }

    /**
     * 出張者、休暇等
     */
    private static class SchoolDiaryDetailSeq {

        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;

        SchoolDiaryDetailSeq(final String remark1, final String remark2, final String remark3, final String remark4) {
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
        }

        public static List getSchoolDiaryDetailSeqList(final DB2UDB db2, final Param param, final String diaryDate, final String flg) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchoolDiaryDetaiSeqSql(param, diaryDate, flg);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");

                    final SchoolDiaryDetailSeq schoolDiaryDetailSeq = new SchoolDiaryDetailSeq(remark1, remark2, remark3, remark4);

                    list.add(schoolDiaryDetailSeq);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getSchoolDiaryDetaiSeqSql(final Param param, final String diaryDate, final String flg) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFF.STAFFNAME_SHOW AS REMARK1, ");
            if("kyuka".equals(flg)) {
                stb.append("     G100.ABBV1 AS REMARK2, ");
            } else {
                stb.append("     T1.REMARK2, ");
            }
            stb.append("     T1.REMARK3, ");
            stb.append("     T1.REMARK4 ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_DIARY_DETAIL_SEQ_DAT T1 ");
            stb.append("     LEFT JOIN V_STAFF_MST STAFF ");
            stb.append("            ON STAFF.STAFFCD = T1.REMARK1 ");
            stb.append("           AND STAFF.YEAR    = '" + param._year + "' ");
            stb.append("     LEFT JOIN V_NAME_MST G100 ");
            stb.append("            ON G100.NAMECD2 = T1.REMARK2 ");
            stb.append("           AND G100.NAMECD1 = 'G100' ");
            stb.append("           AND G100.YEAR    = '" + param._year + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOLCD        = '" + param._schoolCd + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     AND T1.DIARY_DATE  = '" + diaryDate + "' ");
            if("shutcho".equals(flg)) {
                stb.append("     AND T1.SEQ BETWEEN '101' AND '120' "); //SEQ '101' ～ '120' 出張者
            }else if("kyuka".equals(flg)) {
                stb.append("     AND T1.SEQ BETWEEN '201' AND '220' "); //SEQ '201' ～ '220' 休暇等
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.DIARY_DATE, ");
            stb.append("     T1.SEQ ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(request, db2);
        log.fatal("$Revision: 67510 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /**
     * パラメータクラス
     */
    private static class Param {

        final String _year;
        final String _semester;
        final String _loginDate;
        final String _schoolCd;
        final String _schoolKind;
        final String _prgId;

        final String _sdate;
        final String _edate;

        /** 教育課程コードを使用するか */
        final String SSEMESTER = "1";
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;

        final Map _nameMapD055;

        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _prgId = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _edate = request.getParameter("DATE_TO").replace('/', '-');
            _sdate = request.getParameter("DATE_FROM").replace('/', '-');
            _loginDate = request.getParameter("LOGIN_DATE").replace('/', '-');
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _nameMapD055 = getMapNameMst(db2, "NAME1", "D055");
        }

        private Map getMapNameMst(final DB2UDB db2, final String field, final String namecd1) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1 + "' ");
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

