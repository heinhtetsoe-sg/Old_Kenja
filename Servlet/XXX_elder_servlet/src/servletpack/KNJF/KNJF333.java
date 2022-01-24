/*
 * $Id: df5cafb86dba2b3add1933ed7cae144906476b42 $
 *
 * 作成日: 2015/08/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJF333 {

    private static final Log log = LogFactory.getLog(KNJF333.class);

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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
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

    private static String getString(final String field, final Map map) {
        try {
            if (null == field || !map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        if (null == field) {
            return null;
        }
        return (String) map.get(field);
    }
    
    private static List getGroupList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final List dataListAll = Data.getDataList(db2, _param);
        if (dataListAll.size() == 0) {
            return;
        }
        
        final int maxLine = 20;
        final String form = "KNJF333.frm";
        
        final List pageList = getGroupList(dataListAll, maxLine);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("PAGE", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("TITLE", "長欠・保健室登校　入力Ｃシート"); // タイトル

            for (int li = 0; li < dataList.size(); li++) {
                final Data data = (Data) dataList.get(li);
                
                svf.VrsOut("AGE", data._ageName); // 年齢
                svf.VrsOut("SEX", data._sex); // 性別

                final String injury = getString("DATA001_01", data._data);
                final int ketaInjury = getMS932ByteLength(injury);
                svf.VrsOut("INJURY" + (ketaInjury > 50 ? "3" : ketaInjury > 36 ? "2" : "1"), injury); // けが
                final String sick = getString("DATA001_02", data._data);
                final int ketaSick = getMS932ByteLength(sick);
                svf.VrsOut("SICK" + (ketaSick > 50 ? "3" : ketaSick > 36 ? "2" : "1"), sick); // 病気

                svf.VrsOut("ABSENCE",       getString("DATA002_01", data._data)); // 欠席日数
                svf.VrsOut("CONTINUE_DIV1", getString("DATA002_02", data._data)); // 継続区分1
                svf.VrsOut("HOSPITAL",      getString("DATA003_01", data._data)); // 入院
                svf.VrsOut("HEALTH_REASON", getString("DATA004_01", data._data)); // 保健室登校理由
                svf.VrsOut("DOCTOR_JUDGE",  getString("DATA005_01", data._data)); // 医師の診断
                svf.VrsOut("HEALTH_DAYS",   getString("DATA006_01", data._data)); // 保健室登校日数
                svf.VrsOut("CONTINUE_DIV2", getString("DATA006_02", data._data)); // 継続区分2
                svf.VrEndRecord();
            }
            
            for (int i = 0; i < maxLine - dataList.size(); i++) {
                svf.VrsOut("AGE", "\n"); // 年齢
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private static class Data {
        final String _hrAttend;
        final String _name;
        final String _ageName;
        final String _sex;
        final String _sexName;
        final Map _data = new HashMap();

        Data(
            final String hrAttend,
            final String name,
            final String ageName,
            final String sex,
            final String sexName
        ) {
            _hrAttend = hrAttend;
            _name = name;
            _ageName = ageName;
            _sex = sex;
            _sexName = sexName;
        }

        public static List getDataList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String hrAttend = rs.getString("HR_ATTEND");
                    final String name = rs.getString("NAME");
                    final String ageName = rs.getString("AGE_NAME");
                    final String sex = rs.getString("SEX");
                    final String sexName = rs.getString("SEX_NAME");
                    final Data data = new Data(hrAttend, name, ageName, sex, sexName);
                    
                    for (int i = 0; i < param._dataFieldType.length; i++) {
                        final String divSeq = param._dataFieldType[i][0];
                        final String divType = param._dataFieldType[i][1];
                        final String val;
                        if (divType.equals("I")) {
                            val = rs.getString("IDATA" + divSeq + "");
                        } else {
                            val = rs.getString("CDATA" + divSeq + "");
                        }
                        data._data.put("DATA" + divSeq, val);
                    }
                    
                    list.add(data);
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
            stb.append(" WITH SCH_T AS ( ");
            stb.append("     SELECT ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         REGD.ATTENDNO, ");
            stb.append("         HOKEN.SCHREGNO, ");
            stb.append("         BASE.NAME, ");
            stb.append("         HOKEN.SEX, ");
            stb.append("         Z002.NAME2 AS SEX_NAME, ");
            stb.append("         HOKEN.AGE, ");
            stb.append("         RTRIM(CAST(HOKEN.AGE AS CHAR(4))) AS AGE_NAME ");
            stb.append("     FROM ");
            stb.append("         MEDEXAM_DISEASE_HOKENSITU_HDAT HOKEN ");
            stb.append("         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = HOKEN.YEAR ");
            stb.append("               AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("               AND REGD.SCHREGNO = HOKEN.SCHREGNO ");
            stb.append("         LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("               AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("               AND REGD.GRADE = REGDH.GRADE ");
            stb.append("               AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("         INNER JOIN SCHREG_BASE_MST BASE ON HOKEN.SCHREGNO = BASE.SCHREGNO ");
            stb.append("               AND BASE.BIRTHDAY IS NOT NULL ");
            stb.append("         INNER JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("               AND HOKEN.SEX = Z002.NAMECD2 ");
            stb.append("         LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ");
            stb.append("               AND REGD.GRADE = REGDG.GRADE ");
            stb.append("     WHERE ");
            stb.append("         HOKEN.EDBOARD_SCHOOLCD = '" + param._schoolcd + "' ");
            stb.append("         AND HOKEN.YEAR     = '" + param._ctrlYear + "' ");
            stb.append("         AND REGDG.SCHOOL_KIND = '" + param._schoolKind + "' ");
            stb.append("     GROUP BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         REGD.ATTENDNO, ");
            stb.append("         HOKEN.SCHREGNO, ");
            stb.append("         BASE.NAME, ");
            stb.append("         HOKEN.SEX, ");
            stb.append("         Z002.NAME2, ");
            stb.append("         HOKEN.AGE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.HR_NAME || '-' || T1.ATTENDNO || '番' AS HR_ATTEND, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.AGE_NAME, ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.SEX_NAME ");
            for (int i = 0; i < param._dataFieldType.length; i++) {
                final String divSeq = param._dataFieldType[i][0];
                final String divType = param._dataFieldType[i][1];
                if (divType.equals("I")) {
                    stb.append("     ,L" + divSeq + ".INT_VAL AS IDATA" + divSeq + " ");
                } else {
                    stb.append("     ,L" + divSeq + ".CHAR_VAL AS CDATA" + divSeq + " ");
                }
            }
            stb.append(" FROM ");
            stb.append("     SCH_T T1 ");
            for (int i = 0; i < param._dataFieldType.length; i++) {
                final String divSeq = param._dataFieldType[i][0];
                stb.append("     LEFT JOIN MEDEXAM_DISEASE_HOKENSITU_DAT L" + divSeq + " ON L" + divSeq + ".EDBOARD_SCHOOLCD = '" + param._schoolcd + "' ");
                stb.append("          AND L" + divSeq + ".YEAR = '" + param._ctrlYear + "' ");
                stb.append("          AND L" + divSeq + ".SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND L" + divSeq + ".DATA_DIV || '_' || L" + divSeq + ".SEQ = '" + divSeq + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _schoolKind;
        final String _schoolcd;
        final String[][] _dataFieldType;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            
            final String getZ010Abbv1 = getZ010Abbv1(db2);
            if ("1".equals(getZ010Abbv1) || "2".equals(getZ010Abbv1)) {
                _schoolcd = getSchoolcd(db2);
            } else {
                _schoolcd = "000000000000";
            }
            
            _dataFieldType = new String[][] {
                {"001_01", "C"},
                {"001_02", "C"},
                {"002_01", "I"},
                {"002_02", "I"},
                {"003_01", "I"},
                {"004_01", "I"},
                {"005_01", "I"},
                {"006_01", "I"},
                {"006_02", "I"},
            };
        }
        
        private String getSchoolcd(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT KYOUIKU_IINKAI_SCHOOLCD FROM V_SCHOOL_MST WHERE YEAR    = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("KYOUIKU_IINKAI_SCHOOLCD");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private String getZ010Abbv1(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("ABBV1");
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

