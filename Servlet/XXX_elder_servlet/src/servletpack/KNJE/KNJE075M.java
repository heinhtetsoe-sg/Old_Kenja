/*
 * $Id: b3408e943d4f560ab343ef5f40e2336f4081952b $
 *
 * 作成日: 2009/12/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJE075M {

    private static final Log log = LogFactory.getLog(KNJE075M.class);

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
        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            
            printMain(db2, svf, getStudents(db2));

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, List students) {
        
        for (final Iterator it = students.iterator(); it.hasNext();) {
            svf.VrSetForm("KNJE075M.frm", 1);

            Student student = (Student) it.next();
            String nameField = student._name != null && student._name.length() > 10 ? "NAME1" : "NAME";
            svf.VrsOut(nameField, student._name);
            svf.VrsOut("KANA", student._kana);
            if (student._birthday != null) {
                String[] dateStr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(student._birthday));
                String nengo = (dateStr[0] == null) ? "" : dateStr[0];
                String nendo = (dateStr[1] == null) ? "" : dateStr[1];
                svf.VrsOut("BIRTH_YEAR", nengo + "　" + nendo);
                svf.VrsOut("BIRTH_MONTH", dateStr[2]);
                svf.VrsOut("BIRTH_DAY", dateStr[3]);
            }
            svf.VrsOut("SEX", student._sex);
            if (student._entDate != null) {
                String[] dateStr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(student._entDate));
                String nengo = (dateStr[0] == null) ? "" : dateStr[0];
                String nendo = (dateStr[1] == null) ? "" : dateStr[1];
                svf.VrsOut("MOVE_YEAR", nengo + "　" + nendo);
                svf.VrsOut("MOVE_MONTH", dateStr[2]);
            }
            String[] gradDateStr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(String.valueOf(Integer.parseInt(_param._year) + 1) + "-01-01"));
            String nengo = (gradDateStr[0] == null) ? "" : gradDateStr[0];
            String nendo = (gradDateStr[1] == null) ? "" : gradDateStr[1];
            svf.VrsOut("GRAD_YEAR", nengo + "　" + nendo);
            svf.VrsOut("GRAD_MONTH", "3");
            svf.VrsOut("MUST_DAY", student._requirePresent);
            svf.VrsOut("ABSENCE_DAY", student._absent);

            String[] a_str;
            // 総合的な学習の時間の内容
            a_str = KNJ_EditEdit.get_token( student._totalStudyAct, 82 , 2);
            if (a_str != null) {
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("TOTAL_STUDY1", i+1, a_str[i]);
                }
            }
            // 総合的な学習の時間の評価
            a_str = KNJ_EditEdit.get_token( student._totalStudyVal, 82 , 3);
            if (a_str != null) {
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("TOTAL_STUDY2", i+1,  a_str[i]);
                }
            }
            // 諸活動の記録
            a_str = KNJ_EditEdit.get_token( student._trainRef, 82 , 5);
            if (a_str != null) {
                for (int i = 0; i < a_str.length; i++) {
                    svf.VrsOutn("ACTIVE_LOG", i+1,  a_str[i]);
                }
            }
            
            // 学習の記録
            for (Iterator itJview = student.jviews.keySet().iterator(); itJview.hasNext();) {
                String viewCd = (String) itJview.next();
                JViewStat jviewStat = (JViewStat) student.jviews.get(viewCd);
                if (jviewStat.isHyotei()) {
                    // 評定
                    svf.VrsOut("RATE" + _param.getHanteiIndex(jviewStat), jviewStat._status);
                } else {
                    // 評価
                    svf.VrsOutn("EVALUATION", _param.getHyokaIndex(jviewStat), jviewStat._status);
                }
            }
            
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrsOut("SCHOOL_KANA", _param._schoolNameKana);
            svf.VrsOut("ADDRESS1", _param._schoolLocation);
            svf.VrsOut("TEL_NO", _param._schoolPhoneNumber);
            svf.VrsOut("PRINCIPAL_NAME", _param._principalName);
            svf.VrsOut("DEC_NAME", _param._descName);
            if (_param._date != null) {
                
                String[] dateStr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_param._date));
                String decNengo = (dateStr[0] == null) ? "" : dateStr[0];
                String decNendo = (dateStr[1] == null) ? "" : dateStr[1];
                svf.VrsOut("DEC_YEAR", decNengo + "　" + decNendo);
                svf.VrsOut("DEC_MONTH", dateStr[2]);
                svf.VrsOut("DEC_DAY", dateStr[3]);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    /**
     * 生徒のリストを得る
     * @param db2
     * @return
     */
    private List getStudents(final DB2UDB db2) {
        
        List rtn = new LinkedList();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sqlSchregRegdDat(_param._categorySelected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                String schregno = rs.getString("SCHREGNO");
                
                Student student = new Student(schregno,
                        rs.getString("NAME"),
                        rs.getString("NAME_KANA"),
                        rs.getString("ATTENDNO"),
                        rs.getString("BIRTHDAY"),
                        rs.getString("SEX_NAME"),
                        rs.getString("ENT_DATE")
                );
                
                student._requirePresent = rs.getString("REQUIREPRESENT");
                student._absent = rs.getString("ABSENT");
                student._totalStudyAct = rs.getString("TOTALSTUDYACT");
                student._totalStudyVal = rs.getString("TOTALSTUDYVAL");
                student._trainRef = rs.getString("TRAIN_REF");
                
                rtn.add(student);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        // 観点の設定
        try {
            final String sql = sqlJviewStatDat();
            log.debug(" sql jviewStatDat =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                String schregno = rs.getString("SCHREGNO");
                Student student = getStudent(schregno, rtn);
                
                if (student == null) {
                    continue;
                }
                
                JViewStat jviewStat = new JViewStat(rs.getString("VIEWCD"), rs.getString("STATUS"));
                
                student.addJviewStat(jviewStat);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
       
        return rtn;
    }

    private String sqlSchregRegdDat(String[] selected) {
        final StringBuffer stb = new StringBuffer();        
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     T1.SEX, ");
        stb.append("     T3.NAME2 AS SEX_NAME, ");
        stb.append("     T1.ENT_DATE, ");
        stb.append("     T4.REQUIREPRESENT, ");
        stb.append("     T4.SICK + T4.ACCIDENTNOTICE + T4.NOACCIDENTNOTICE AS ABSENT, ");
        stb.append("     T5.TOTALSTUDYACT, ");
        stb.append("     T5.TOTALSTUDYVAL, ");
        stb.append("     T6.TRAIN_REF ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("         T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST T3 ON ");
        stb.append("         T3.NAMECD1 = 'Z002' AND T3.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN SCHREG_ATTENDREC_DAT T4 ON ");
        stb.append("         T4.YEAR = T2.YEAR ");
        stb.append("         AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T4.ANNUAL = T2.ANNUAL ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_HDAT T5 ON ");
        stb.append("         T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN HEXAM_ENTREMARK_DAT T6 ON ");
        stb.append("         T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T6.YEAR = T2.YEAR ");
        stb.append(" WHERE");
        stb.append("     T2.YEAR = '" + _param._year + "'");
        stb.append("     AND T2.SEMESTER = '" + _param._semester +"'");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(false, _param._categorySelected) );
        stb.append(" ORDER BY ");
        stb.append("     T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");

        return stb.toString();
    }

    private String sqlJviewStatDat() {
        final StringBuffer stb = new StringBuffer();
        final String table = (Integer.parseInt(_param._year) < 2012) ? "JVIEWSTAT_DAT" : "JVIEWSTAT_SUB_DAT";
        stb.append(" WITH T_JVIEWSTAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         VIEWCD, ");
        stb.append("         STATUS ");
        stb.append("     FROM ");
        stb.append("         " + table + " ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         SEMESTER = '9' AND ");
        stb.append("         SCHREGNO IN " + SQLUtils.whereIn(false, _param._categorySelected) + " AND ");
        stb.append("         VIEWCD NOT LIKE '%99' ");
        if ("JVIEWSTAT_SUB_DAT".equals(table)) {
            stb.append("         AND SUBCLASSCD LIKE '%0001' ");
        }
        stb.append("     ) ");
        stb.append(" , T_STUDYREC AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.CLASSCD || '99' AS VIEWCD, ");
        stb.append("         rtrim(char(max(T1.VALUATION))) AS STATUS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_STUDYREC_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' AND ");
        stb.append("         (T1.SCHREGNO, T1.CLASSCD) ");
        stb.append("         IN ( ");
        stb.append("             SELECT ");
        stb.append("                 J1.SCHREGNO, ");
        stb.append("                 SUBSTR(J1.VIEWCD,1,2) AS CLASSCD ");
        stb.append("             FROM ");
        stb.append("                 T_JVIEWSTAT J1 ");
        stb.append("             GROUP BY ");
        stb.append("                 J1.SCHREGNO, ");
        stb.append("                 SUBSTR(J1.VIEWCD,1,2) ");
        stb.append("             ) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.CLASSCD ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     VIEWCD, ");
        stb.append("     STATUS ");
        stb.append(" FROM ");
        stb.append("     T_JVIEWSTAT ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     VIEWCD, ");
        stb.append("     STATUS ");
        stb.append(" FROM ");
        stb.append("     T_STUDYREC ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     VIEWCD ");

        return stb.toString();
    }

    private Student getStudent(String schregno, List students) {
        for (Iterator it = students.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _kana;
        final String _attendNo;
        final String _birthday;
        final String _sex;
        final String _entDate;
        
        /** 観点別学習状況 */
        Map jviews = new HashMap();

        /** 出席すべき日数 */
        public String _requirePresent;

        /** 欠席日数 */
        public String _absent;

        /** 総合的な学習の時間 内容*/
        public String _totalStudyAct;
        
        /** 総合的な学習の時間 評価*/
        public String _totalStudyVal;

        /** 諸活動の記録 */
        public String _trainRef;

        Student(final String schregno, 
                final String name,
                final String kana,
                final String attendNo,
                final String birthday,
                final String sex,
                final String entDate) {
            _schregno = schregno;
            _name = name;
            _kana = kana;
            _attendNo = attendNo;
            _birthday = birthday;
            _sex = sex;
            _entDate = entDate;
        }
        
        void addJviewStat(JViewStat jviewStat) {
            jviews.put(jviewStat._viewCd, jviewStat);
        }
    }
    
    private class JViewStat {
        String _viewCd;
        String _status;
        JViewStat(String viewCd, String status) {
            _viewCd = viewCd;
            _status = status;
        }
        boolean isHyotei() {
            return "99".equals(getHyokaId());
        }
        String getClassCode() {
            return _viewCd != null ? _viewCd.substring(0, 2) : "";
        }
        String getHyokaId() {
            return _viewCd != null ? _viewCd.substring(2) : "";
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
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrClass;
        private final String[] _categorySelected;
        private final String _date;
        private final Map _hanteiIndice;
        private final Map _hyokaOffsets;

        private String _schoolName;
        private String _schoolNameKana;
        private String _schoolLocation;
        private String _schoolPhoneNumber;
        private String _principalName;
        private String _descName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            
            _categorySelected = request.getParameterValues("category_selected");
            _date = request.getParameter("DATE").replace('/', '-');
            
            loadCertifSchool(db2);
            loadDescriptor(db2, request.getParameter("SEKI"));
            
            _hanteiIndice = new HashMap();
            _hanteiIndice.put("02", Integer.valueOf("1")); // 国語
            _hanteiIndice.put("04", Integer.valueOf("2")); // 社会
            _hanteiIndice.put("06", Integer.valueOf("3")); // 数学
            _hanteiIndice.put("08", Integer.valueOf("4")); // 理科
            _hanteiIndice.put("10", Integer.valueOf("5")); // 音楽
            _hanteiIndice.put("12", Integer.valueOf("6")); // 美術
            _hanteiIndice.put("16", Integer.valueOf("7")); // 保健体育
            _hanteiIndice.put("18", Integer.valueOf("8")); // 技術・家庭
            _hanteiIndice.put("22", Integer.valueOf("9")); // 外国語（英語

            _hyokaOffsets = new HashMap();
            _hyokaOffsets.put("02", Integer.valueOf("0")); // 国語
            _hyokaOffsets.put("04", Integer.valueOf("5")); // 社会
            _hyokaOffsets.put("06", Integer.valueOf("9")); // 数学
            _hyokaOffsets.put("08", Integer.valueOf("13")); // 理科
            _hyokaOffsets.put("10", Integer.valueOf("17")); // 音楽
            _hyokaOffsets.put("12", Integer.valueOf("21")); // 美術
            _hyokaOffsets.put("16", Integer.valueOf("25")); // 保健体育
            _hyokaOffsets.put("18", Integer.valueOf("29")); // 技術・家庭
            _hyokaOffsets.put("22", Integer.valueOf("33")); // 外国語（英語
        }
        
        private void loadCertifSchool(DB2UDB db2) {
            
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     REMARK3 AS SCHOOL_NAME_KANA, ");
            stb.append("     REMARK4 AS SCHOOL_LOCATION, ");
            stb.append("     REMARK5 AS SCHOOL_PHONE_NUMBER, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND CERTIF_KINDCD = '115' ");

            String sqlCertifSchool = stb.toString();
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                ps = db2.prepareStatement(sqlCertifSchool);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _schoolNameKana = rs.getString("SCHOOL_NAME_KANA");
                    _schoolLocation = rs.getString("SCHOOL_LOCATION");
                    _schoolPhoneNumber = rs.getString("SCHOOL_PHONE_NUMBER");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
        
        private void loadDescriptor(DB2UDB db2, String staffCd) {
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            try {
                ps = db2.prepareStatement(" SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + staffCd + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    _descName = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.error("Exception:", e);
            }
        }
        
        public  int getHanteiIndex(JViewStat jviewStat) {
            final Integer i = (Integer) _hanteiIndice.get(jviewStat.getClassCode());
            if (i != null) {
                return i.intValue();
            }
            return -1;
        }

        public  int getHyokaIndex(JViewStat jviewStat) {
            final Integer offset = (Integer) _hyokaOffsets.get(jviewStat.getClassCode());
            if (offset != null && !"".equals(jviewStat.getHyokaId())) {
                try {
                    int id = Integer.parseInt(jviewStat.getHyokaId());
                    int index = offset.intValue() + id;
                    return index;
                } catch (Exception e) {
                    log.error("Exception:", e);
                }
            }
            return -1;
        }
    }
}

// eof

