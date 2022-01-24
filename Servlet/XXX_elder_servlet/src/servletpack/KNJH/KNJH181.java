/*
 * $Id: 713e4cb39f1202eee65c094a72eac18218f98fb9 $
 *
 * 作成日: 2016/01/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJH181 {

    private static final Log log = LogFactory.getLog(KNJH181.class);

    private static final String csv = "csv";

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
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if (csv.equals(_param._cmd)) {
                String setTitle = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　";
                setTitle += _param._rosenName;
                setTitle += "　利用生徒名簿";
                final String filename = setTitle + ".csv";
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2), csvParam);
            } else {
                response.setContentType("application/pdf");

                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                _hasData = false;

                printMain(db2, svf);
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (csv.equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }

    }

    private void setTitle(final Vrw32alp svf) {
        String setTitle = KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　";
        setTitle += _param._rosenName;
        setTitle += "　利用生徒名簿";
        svf.VrsOut("TITLE", setTitle);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
    }
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJH181.frm", 1);
        setTitle(svf);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > 45) {
                    svf.VrEndPage();
                    setTitle(svf);
                    lineCnt = 1;
                }
                int train1Cnt = 0;
                int train2Cnt = 0;
                int buscnt = 0;
                String train1 = "";
                String train2 = "";
                String train2sep = "";
                String bus = "";
                String gesya = "";
                String gesyaSep = "";
                final Map injiMap = new HashMap();
//                final String schregno = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final int commuteHours = rs.getInt("COMMUTE_HOURS");
                final int commuteMinutes = rs.getInt("COMMUTE_MINUTES");
                for (int n = 1; n <= 7; n++) {
                    final String sn = String.valueOf(n);
                    final String josya1 = rs.getString("JOSYA_" + sn);
                    final String rosen1 = rs.getString("ROSEN_" + sn);
                    final String gesya1 = rs.getString("GESYA_" + sn);
                    final String flg1 = rs.getString("FLG_" + sn);
                    final InjiData injiData1 = new InjiData(josya1, rosen1, gesya1, flg1);
                    injiMap.put(sn, injiData1);
                }

                svf.VrsOutn("HR_NAME", lineCnt, hrName);
                svf.VrsOutn("NO", lineCnt, attendno);
                final String nameField = getMS932ByteLength(name) > 30 ? "3" : getMS932ByteLength(name) > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, name);
                svf.VrsOutn("SEX", lineCnt, sex);
                final int setTime = commuteHours * 60 + commuteMinutes;
                svf.VrsOutn("TIME", lineCnt, String.valueOf(setTime));

                String lastjosya = "";
                for (int i = 7; i > 0; i--) {
                    final InjiData injiData = (InjiData) injiMap.get(String.valueOf(i));
                    if (buscnt == 0 && "2".equals(injiData._flg)) {
                        bus = injiData._rosen;
                        train1Cnt++;
                        buscnt++;
                    }
                    if (train1Cnt == 0 && "1".equals(injiData._flg)) {
                        train1 = injiData._rosen;
                        train1Cnt++;
                    } else if (train2Cnt < 2 && "1".equals(injiData._flg)) {
                        if (train1Cnt > 0) {
                            train2 += train2sep + injiData._rosen;
                            train2Cnt++;
                            train2sep = "・";
                        }
                    }
                    if (null != injiData._flg && !"".equals(injiData._flg)) {
                        gesya += gesyaSep + injiData._gesya;
                        gesyaSep = "\uFF5E";
                        lastjosya = injiData._josya;
                    }

                }
                final String train1Field = getMS932ByteLength(train1) > 16 ? "3" : getMS932ByteLength(train1) > 10 ? "2" : "1";
                svf.VrsOutn("TRAIN1_" + train1Field, lineCnt, train1);
                final String train2Field = getMS932ByteLength(train2) > 50 ? "3" : getMS932ByteLength(train2) > 30 ? "2" : "1";
                svf.VrsOutn("TRAIN2_" + train2Field, lineCnt, train2);
                final String busField = getMS932ByteLength(bus) > 16 ? "3" : getMS932ByteLength(bus) > 10 ? "2" : "1";
                svf.VrsOutn("BUS" + busField, lineCnt, bus);
                gesya = "".equals(lastjosya) ?  gesya : gesya + gesyaSep + lastjosya;
                final String remarkField = getMS932ByteLength(gesya) > 80 ? "3" : getMS932ByteLength(gesya) > 50 ? "2" : "1";
                svf.VrsOutn("GOTO_SCHOOL" + remarkField, lineCnt, gesya);

                lineCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndPage();
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ROSEN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     MAIN.SCHREGNO ");
        stb.append(" FROM ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_1 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_1 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_2 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_2 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_3 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_3 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_4 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_4 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_5 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_5 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_6 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_6 = '" + _param._rosen + "' ");
        stb.append("     UNION ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO AS SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_ENVIR_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.FLG_7 = '" + _param._flg + "' ");
        stb.append("         AND T1.UP_DOWN = '" + _param._upDown + "' ");
        stb.append("         AND T1.ROSEN_7 = '" + _param._rosen + "' ");
        stb.append("     ) MAIN ");
        stb.append(" GROUP BY ");
        stb.append("     MAIN.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.EMERGENCYTELNO, ");
        stb.append("     NM1.NAME2 AS SEX, ");
        stb.append("     VALUE(T1.COMMUTE_HOURS, '0') AS COMMUTE_HOURS, ");
        stb.append("     VALUE(T1.COMMUTE_MINUTES, '0') AS COMMUTE_MINUTES, ");
        //stb.append("     T1.JOSYA_1, ");
        stb.append("     CASE WHEN T1.FLG_1 = '1' ");
        stb.append("          THEN JSTATION1.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_1 ");
        stb.append("     END AS JOSYA_1, ");
        stb.append("     CASE WHEN T1.FLG_1 = '1' ");
        stb.append("          THEN TLINE1.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_1 ");
        stb.append("     END AS ROSEN_1, ");
        stb.append("     CASE WHEN T1.FLG_1 = '1' ");
        stb.append("          THEN STATION1.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_1 ");
        stb.append("     END AS GESYA_1, ");
        stb.append("     T1.FLG_1, ");
        //stb.append("     T1.JOSYA_2, ");
        stb.append("     CASE WHEN T1.FLG_2 = '1' ");
        stb.append("          THEN JSTATION2.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_2 ");
        stb.append("     END AS JOSYA_2, ");
        stb.append("     CASE WHEN T1.FLG_2 = '1' ");
        stb.append("          THEN TLINE2.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_2 ");
        stb.append("     END AS ROSEN_2, ");
        stb.append("     CASE WHEN T1.FLG_2 = '1' ");
        stb.append("          THEN STATION2.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_2 ");
        stb.append("     END AS GESYA_2, ");
        stb.append("     T1.FLG_2, ");
        //stb.append("     T1.JOSYA_3, ");
        stb.append("     CASE WHEN T1.FLG_3 = '1' ");
        stb.append("          THEN JSTATION3.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_3 ");
        stb.append("     END AS JOSYA_3, ");
        stb.append("     CASE WHEN T1.FLG_3 = '1' ");
        stb.append("          THEN TLINE3.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_3 ");
        stb.append("     END AS ROSEN_3, ");
        stb.append("     CASE WHEN T1.FLG_3 = '1' ");
        stb.append("          THEN STATION3.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_3 ");
        stb.append("     END AS GESYA_3, ");
        stb.append("     T1.FLG_3, ");
        //stb.append("     T1.JOSYA_4, ");
        stb.append("     CASE WHEN T1.FLG_4 = '1' ");
        stb.append("          THEN JSTATION4.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_4 ");
        stb.append("     END AS JOSYA_4, ");
        stb.append("     CASE WHEN T1.FLG_4 = '1' ");
        stb.append("          THEN TLINE4.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_4 ");
        stb.append("     END AS ROSEN_4, ");
        stb.append("     CASE WHEN T1.FLG_4 = '1' ");
        stb.append("          THEN STATION4.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_4 ");
        stb.append("     END AS GESYA_4, ");
        stb.append("     T1.FLG_4, ");
        //stb.append("     T1.JOSYA_5, ");
        stb.append("     CASE WHEN T1.FLG_5 = '1' ");
        stb.append("          THEN JSTATION5.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_5 ");
        stb.append("     END AS JOSYA_5, ");
        stb.append("     CASE WHEN T1.FLG_5 = '1' ");
        stb.append("          THEN TLINE5.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_5 ");
        stb.append("     END AS ROSEN_5, ");
        stb.append("     CASE WHEN T1.FLG_5 = '1' ");
        stb.append("          THEN STATION5.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_5 ");
        stb.append("     END AS GESYA_5, ");
        stb.append("     T1.FLG_5, ");
        //stb.append("     T1.JOSYA_6, ");
        stb.append("     CASE WHEN T1.FLG_6 = '1' ");
        stb.append("          THEN JSTATION6.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_6 ");
        stb.append("     END AS JOSYA_6, ");
        stb.append("     CASE WHEN T1.FLG_6 = '1' ");
        stb.append("          THEN TLINE6.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_6 ");
        stb.append("     END AS ROSEN_6, ");
        stb.append("     CASE WHEN T1.FLG_6 = '1' ");
        stb.append("          THEN STATION6.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_6 ");
        stb.append("     END AS GESYA_6, ");
        stb.append("     T1.FLG_6, ");
        //stb.append("     T1.JOSYA_7, ");
        stb.append("     CASE WHEN T1.FLG_7 = '1' ");
        stb.append("          THEN JSTATION7.STATION_NAME ");
        stb.append("          ELSE T1.JOSYA_7 ");
        stb.append("     END AS JOSYA_7, ");
        stb.append("     CASE WHEN T1.FLG_7 = '1' ");
        stb.append("          THEN TLINE7.LINE_NAME ");
        stb.append("          ELSE T1.ROSEN_7 ");
        stb.append("     END AS ROSEN_7, ");
        stb.append("     CASE WHEN T1.FLG_7 = '1' ");
        stb.append("          THEN STATION7.STATION_NAME ");
        stb.append("          ELSE T1.GESYA_7 ");
        stb.append("     END AS GESYA_7, ");
        stb.append("     T1.FLG_7 ");
        stb.append(" FROM ");
        stb.append("     ROSEN_T ");
        stb.append("     INNER JOIN SCHREG_ENVIR_DAT T1 ON ROSEN_T.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE1 ON T1.ROSEN_1 = TLINE1.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE2 ON T1.ROSEN_2 = TLINE2.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE3 ON T1.ROSEN_3 = TLINE3.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE4 ON T1.ROSEN_4 = TLINE4.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE5 ON T1.ROSEN_5 = TLINE5.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE6 ON T1.ROSEN_6 = TLINE6.LINE_CD ");
        stb.append("     LEFT JOIN TRAIN_LINE_MST TLINE7 ON T1.ROSEN_7 = TLINE7.LINE_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION1 ON T1.JOSYA_1 = JSTATION1.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION2 ON T1.JOSYA_2 = JSTATION2.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION3 ON T1.JOSYA_3 = JSTATION3.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION4 ON T1.JOSYA_4 = JSTATION4.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION5 ON T1.JOSYA_5 = JSTATION5.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION6 ON T1.JOSYA_6 = JSTATION6.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST JSTATION7 ON T1.JOSYA_7 = JSTATION7.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION1 ON T1.GESYA_1 = STATION1.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION2 ON T1.GESYA_2 = STATION2.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION3 ON T1.GESYA_3 = STATION3.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION4 ON T1.GESYA_4 = STATION4.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION5 ON T1.GESYA_5 = STATION5.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION6 ON T1.GESYA_6 = STATION6.STATION_CD ");
        stb.append("     LEFT JOIN STATION_NETMST STATION7 ON T1.GESYA_7 = STATION7.STATION_CD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST NM1 ON NM1.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = NM1.NAMECD2 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("           AND T1.SCHREGNO = REGD.SCHREGNO ");
        if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
            stb.append("   AND REGDG.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("           AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("           AND REGD.GRADE = REGDH.GRADE ");
        stb.append("           AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sortType)) {
            stb.append("     CASE WHEN COALESCE(T1.GESYA_1, '') || COALESCE(T1.GESYA_2, '') || COALESCE(T1.GESYA_3, '') || COALESCE(T1.GESYA_4, '') || COALESCE(T1.GESYA_5, '') || COALESCE(T1.GESYA_6, '') || COALESCE(T1.GESYA_7, '') IS NULL THEN 0 ELSE 1 END DESC, ");
            stb.append("     COALESCE(T1.GESYA_1, '') || COALESCE(T1.GESYA_2, '') || COALESCE(T1.GESYA_3, '') || COALESCE(T1.GESYA_4, '') || COALESCE(T1.GESYA_5, '') || COALESCE(T1.GESYA_6, '') || COALESCE(T1.GESYA_7, '') ASC, ");
        }
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private List nextLine(final List lines) {
        final List line = new ArrayList();
        lines.add(line);
        return line;
    }

    private List getCsvOutputLine(final DB2UDB db2) {

        final List lines = new ArrayList();

        nextLine(lines).addAll(Arrays.asList(new String[] {"年組", "番号", "学籍番号", "氏名", "急用連絡先電話番号"}));

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String emergencytelno = rs.getString("EMERGENCYTELNO");

                nextLine(lines).addAll(Arrays.asList(new String[] {hrName, attendno, schregno, name, emergencytelno}));

            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return lines;
    }

    private class InjiData {
        final String _josya;
        final String _rosen;
        final String _gesya;
        final String _flg;
        public InjiData(
                final String josya,
                final String rosen,
                final String gesya,
                final String flg
                ) {
            _josya = josya;
            _rosen = rosen;
            _gesya = gesya;
            _flg = flg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 69244 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _flg;
        final String _upDown;
        final String _rosen;
        final String _rosenName;
        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _cmd;
        private String _useSchool_KindField;
        private String _SCHOOLKIND;
        final String _sortType;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _flg = request.getParameter("FLG");
            _upDown = request.getParameter("UP_DOWN");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            String setRosen = request.getParameter("ROSEN");
            if ("2".equals(_flg)) {
                setRosen = getRosen(db2, setRosen);
            }
            _rosen = setRosen;
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _cmd = request.getParameter("cmd");
            _rosenName = getRosenName(db2);
            _sortType = StringUtils.defaultString(request.getParameter("SORTFLG"), "1");
        }

        private String getRosen(final DB2UDB db2, final String setRosen) throws SQLException {
            String retStr = "";
            final String[] rosenArray = StringUtils.split(setRosen, "_");
            final String sql = "SELECT ROSEN_" + rosenArray[0] + " FROM SCHREG_ENVIR_DAT WHERE SCHREGNO = '" + rosenArray[1] + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("ROSEN_" + rosenArray[0]);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getRosenName(final DB2UDB db2) throws SQLException {
            if ("2".equals(_flg)) {
                return _rosen;
            }
            String retStr = "";
            String sql = " SELECT * FROM TRAIN_LINE_MST WHERE LINE_CD = '" + _rosen + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("LINE_NAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof

