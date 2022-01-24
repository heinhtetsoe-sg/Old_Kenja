/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 76dc95645e80913492f799ad4d47e5a1ed34fab5 $
 *
 * 作成日: 2019/03/29
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

public class KNJD426 {

    private static final Log log = LogFactory.getLog(KNJD426.class);

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

    final int FIRST_PAGE_MAXLINE = 36;
    final int PAGE_MAXLINE = 54;
    boolean IS_FIRST_PRINT = false;
    int LINE_CNT = 0;
    final String PRINT_FRM1 = "KNJD426_1.frm";
    final String PRINT_FRM2 = "KNJD426_2.frm";
    String PRINT_FRM_NOW = "";

    private Param _param;

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);
        final List subTitleList = new ArrayList();
        subTitleList.add("学部");
        subTitleList.add("年・組");
        subTitleList.add("障害種別");
        subTitleList.add("作成日");
        subTitleList.add("作成者");
        subTitleList.add("氏名");
        subTitleList.add("性別");
        subTitleList.add("生年月日");
        subTitleList.add("障害名等");
        subTitleList.add("障害の概要");
        subTitleList.add("検査");
        subTitleList.add("検査日");
        subTitleList.add("検査名");
        subTitleList.add("検査機関");
        subTitleList.add("検査日");
        subTitleList.add("検査名");
        subTitleList.add("検査機関");
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            svf.VrSetForm("KNJD426_1.frm", 4);
            PRINT_FRM_NOW = "KNJD426_1.frm";
            final Student student = (Student) iterator.next();
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);

            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, "PAINT=(0,85,2)");
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
            //学部
            svf.VrsOut("DEPARTMENT_NAME", student._gakubuName);
            //年組
            svf.VrsOut("GRADE_NAME", student.getHrName());
            //氏名
            int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String nameField = nameLen > 30 ? "3" : nameLen > 20 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, student._name);
            //かな
            int kanaLen = KNJ_EditEdit.getMS932ByteLength(student._nameKana);
            final String kanaField = kanaLen > 30 ? "3" : kanaLen > 20 ? "2" : "1";
            svf.VrsOut("KANA" + kanaField, student._nameKana);
            //性別
            svf.VrsOut("SEX", student._sexName);
            //生年月日
            if (!StringUtils.isEmpty(student._birthDay)) {
                final String[] birthArray = StringUtils.split(student._birthDay, "-");
                final String birthGengouAlphabet = KNJ_EditDate.gengouAlphabetMark(db2, Integer.parseInt(birthArray[0]), Integer.parseInt(birthArray[1]), Integer.parseInt(birthArray[2]));
                final String[] gengouArray = KNJ_EditDate.tate_format4(db2, student._birthDay);
                svf.VrsOut("BIRTHDAY", birthGengouAlphabet + gengouArray[1] + "." + gengouArray[2] + "." + gengouArray[3]);
            }

            //各ブロックを印字
            boolean printRecord = false;
            for (Iterator itPrintBlock = _param._printBlockList.iterator(); itPrintBlock.hasNext();) {
                final String kindNo = (String) itPrintBlock.next();
                if (student._blockMap.containsKey(kindNo)) {
                    final Block block = (Block) student._blockMap.get(kindNo);
                    if (null != block) {
                        block.printOut(svf, student);
                        if (LINE_CNT > 0) {
                        	printRecord = true;
                        	_hasData = true;
                        }
                    }
                }
            }
            if (!printRecord) {
            	log.warn(" no block printed : schregno = " + student._schregNo);
            	svf.VrEndRecord();
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String ghrCd = rs.getString("GHR_CD");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String gakubuName = rs.getString("GAKUBU_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String ghrName = rs.getString("GHR_NAME");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthDay = rs.getString("BIRTHDAY");
                final String sexName = rs.getString("SEX_NAME");
                final String guidCourse = rs.getString("GUID_COURSE");
                final String createDate = rs.getString("CREATEDATE");
                final String createUser = rs.getString("CREATEUSER");

                final Student student = new Student(schregNo, schoolKind, ghrCd, grade, hrClass, gakubuName, hrName,
                        ghrName, name, nameKana, birthDay, sexName, guidCourse, createDate, createUser);

                student.setPaternTitleMap(db2);
                student.setSubclassSeqMap(db2);
                student.setKindName(db2);
                student.setBlock1(db2);
                student.setBlockOterMain(db2);
                student.setBlock8(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHRH.GHR_NAME, ");
        stb.append("     A023.ABBV1 AS GAKUBU_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     Z002.NAME2 AS SEX_NAME, ");
        //stb.append("     GUID_R1.REMARK AS GUID_COURSE, ");
        stb.append("     CASE WHEN D091.NAME1 IS NULL THEN GUID_R1.REMARK ELSE D091.NAME1 END AS GUID_COURSE, ");
        stb.append("     GUID_R2.REMARK AS CREATEDATE, ");
        stb.append("     STF.STAFFNAME AS CREATEUSER ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_BASE_MST BASE ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("                                  AND REGD.GRADE = GDAT.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("                                  AND REGD.SEMESTER = REGDH.SEMESTER  ");
        stb.append(
                "                                  AND REGD.GRADE || REGD.HR_CLASS = REGDH.GRADE || REGDH.HR_CLASS  ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ON GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("                                     AND GHR.YEAR =REGD.YEAR ");
        stb.append("                                     AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ON GHRH.YEAR = GHR.YEAR ");
        stb.append("                                      AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("                                      AND GHRH.GHR_CD = GHR.GHR_CD ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R1 ON REGD.YEAR = GUID_R1.YEAR ");
        stb.append("                                      AND GUID_R1.SEMESTER = '9' ");
        stb.append("                                      AND GUID_R1.RECORD_DATE = '9999-03-31' ");
        stb.append("                                      AND REGD.SCHREGNO = GUID_R1.SCHREGNO ");
        stb.append("                                      AND GUID_R1.DIV = '01' ");
        stb.append("                                      AND GUID_R1.SEQ = 1 ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R2 ON REGD.YEAR = GUID_R2.YEAR ");
        stb.append("                                      AND GUID_R2.SEMESTER = '9' ");
        stb.append("                                      AND GUID_R2.RECORD_DATE = '9999-03-31' ");
        stb.append("                                      AND REGD.SCHREGNO = GUID_R2.SCHREGNO ");
        stb.append("                                      AND GUID_R2.DIV = '01' ");
        stb.append("                                      AND GUID_R2.SEQ = 2 ");
        stb.append("     LEFT JOIN HREPORT_GUIDANCE_SCHREG_REMARK_DAT GUID_R3 ON REGD.YEAR = GUID_R3.YEAR ");
        stb.append("                                      AND GUID_R3.SEMESTER = '9' ");
        stb.append("                                      AND GUID_R3.RECORD_DATE = '" + _param._recordDate + "' ");
        stb.append("                                      AND REGD.SCHREGNO = GUID_R3.SCHREGNO ");
        stb.append("                                      AND GUID_R3.DIV = '01' ");
        stb.append("                                      AND GUID_R3.SEQ = 3 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = GUID_R3.REMARK ");
        stb.append("     LEFT JOIN V_NAME_MST A023 ON A023.YEAR = REGD.YEAR ");
        stb.append("                            AND A023.NAMECD1 = 'A023' ");
        stb.append("                            AND A023.NAME1 = GDAT.SCHOOL_KIND ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON REGD.YEAR = Z002.YEAR ");
        stb.append("                            AND Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN V_NAME_MST D091 ON D091.YEAR = REGD.YEAR ");
        stb.append("                            AND D091.NAMECD1 = 'D091' ");
        stb.append("                            AND D091.NAMECD2 = GUID_R1.REMARK ");
        stb.append(" WHERE ");
        stb.append("     BASE.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("     AND REGD.YEAR     = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _schoolKind;
        final String _ghrCd;
        final String _grade;
        final String _hrClass;
        final String _gakubuName;
        final String _hrName;
        final String _ghrName;
        final String _name;
        final String _nameKana;
        final String _birthDay;
        final String _sexName;
        final String _guidCourse;
        final String _createDate;
        final String _createUser;
        final Map _paternTitleMap;
        final Map _subclassMap;
        final Map _kindNameMap;
        final Map _blockMap;

        public Student(final String schregNo, final String schoolKind, final String ghrCd, final String grade,
                final String hrClass, final String gakubuName, final String hrName, final String ghrName,
                final String name, final String nameKana, final String birthDay, final String sexName,
                final String guidCourse, final String createDate, final String createUser) {
            _schregNo = schregNo;
            _schoolKind = schoolKind;
            _ghrCd = StringUtils.isEmpty(ghrCd) ? "00" : ghrCd;
            _grade = grade;
            _hrClass = hrClass;
            _gakubuName = gakubuName;
            _hrName = hrName;
            _ghrName = ghrName;
            _name = name;
            _nameKana = nameKana;
            _birthDay = birthDay;
            _sexName = sexName;
            _guidCourse = guidCourse;
            _createDate = createDate;
            _createUser = createUser;
            _subclassMap = new LinkedMap();
            _kindNameMap = new HashMap();
            _paternTitleMap = new LinkedMap();
            _blockMap = new HashMap();
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hrName;
            } else {
                return _ghrName;
            }
        }

        private void setPaternTitleMap(final DB2UDB db2) {
            setTitleMap(db2, "SCHREG");
            if (_paternTitleMap.size() == 0) {
                setTitleMap(db2, "GRADE");
            }
            if (_paternTitleMap.size() == 0) {
                setTitleMap(db2, "ALL");
            }
        }

        private void setTitleMap(final DB2UDB db2, final String patern) {
            final String titleSql = getPaternTitleSql(patern);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kindNo = rs.getString("KIND_NO");
                    final String kindName = rs.getString("KIND_NAME");
                    _paternTitleMap.put(kindNo, kindName);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getPaternTitleSql(final String patern) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.KIND_NO, ");
            stb.append("     T2.KIND_NAME ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_DAT T1 ");
            stb.append("     LEFT JOIN HREPORT_GUIDANCE_KIND_NAME_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.KIND_NO = T1.KIND_NO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            if ("SCHREG".equals(patern)) {
                stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            } else if ("GRADE".equals(patern)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + _grade + "-000' ");
                stb.append("     AND T1.SCHREGNO = '00000000' ");
            } else {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '00-000' ");
                stb.append("     AND T1.SCHREGNO = '00000000' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.SHOWORDER ");

            return stb.toString();
        }

        private void setSubclassSeqMap(final DB2UDB db2) {
            final String subclassSql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                int setSeq = 1;
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final Subclass subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, "");
                    _subclassMap.put(String.valueOf(setSeq), subclass);
                    setSeq++;
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
            stb.append("     T3.SUBCLASSCD, ");
            stb.append("     T3.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     LEFT JOIN GRADE_KIND_COMP_GROUP_DAT T2 ");
            stb.append("          ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("          AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T2.GHR_CD = T1.GHR_CD ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("          AND T2.CONDITION = T1.CONDITION ");
            stb.append("          AND T2.GROUPCD = T1.GROUPCD ");
            stb.append("     LEFT JOIN V_SUBCLASS_MST T3 ");
            stb.append("          ON T3.YEAR = T2.YEAR ");
            stb.append("          AND T3.CLASSCD = T2.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("          AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("          AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _grade + _hrClass + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     T3.SCHOOL_KIND, ");
            stb.append("     T3.CURRICULUM_CD, ");
            stb.append("     T3.SUBCLASSCD ");

            return stb.toString();
        }

        public void setKindName(final DB2UDB db2) {
            final String kindNameSql = getKindNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(kindNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kindNo = rs.getString("KIND_NO");
                    final int kindSeq = rs.getInt("KIND_SEQ");
                    final String kindRemark = rs.getString("KIND_REMARK");
                    final KindNameDat kindNameDat = new KindNameDat(kindNo, String.valueOf(kindSeq), kindRemark);
                    final Map setKindSeqMap;
                    if (_kindNameMap.containsKey(kindNo)) {
                        setKindSeqMap = (Map) _kindNameMap.get(kindNo);
                    } else {
                        setKindSeqMap = new HashMap();
                    }
                    setKindSeqMap.put(String.valueOf(kindSeq), kindNameDat);
                    _kindNameMap.put(kindNo, setKindSeqMap);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getKindNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     KIND_NO, ");
            stb.append("     KIND_SEQ, ");
            stb.append("     KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");

            return stb.toString();
        }

        private void setBlock1(final DB2UDB db2) {
            Block1 block1 = null;
            final String aPaternSql = getBlock1Sql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String challengedNames = rs.getString("CHALLENGED_NAMES");
                    final String challengedStatus = rs.getString("CHALLENGED_STATUS");
                    final int nslaCnt1 = simpleChrCount(rs.getString("CHECK_DATE1"), '-');
                    final String checkDate1 = nslaCnt1 > 1 ? StringUtils.substring(rs.getString("CHECK_DATE1"), 1, 7): rs.getString("CHECK_DATE1");
                    final String checkName1 = "".equals(StringUtils.defaultString(rs.getString("CHECK_NAME1"))) ? "" : StringUtils.replace(rs.getString("CHECK_NAME1"),"\r\n", " ");
                    final String checkCenterText1 = rs.getString("CHECK_CENTER_TEXT1");
                    final int nslaCnt2 = simpleChrCount(rs.getString("CHECK_DATE2"), '-');
                    final String checkDate2 = nslaCnt2 > 1 ? StringUtils.substring(rs.getString("CHECK_DATE2"), 1, 7): rs.getString("CHECK_DATE2");
                    final String checkName2 = "".equals(StringUtils.defaultString(rs.getString("CHECK_NAME2"))) ? "" : StringUtils.replace(rs.getString("CHECK_NAME2"), "\r\n", " ");
                    final String checkCenterText2 = rs.getString("CHECK_CENTER_TEXT2");
                    block1 = new Block1(challengedNames, challengedStatus, checkDate1, checkName1, checkCenterText1,
                            checkDate2, checkName2, checkCenterText2);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _blockMap.put("01", block1);
        }

        private String getBlock1Sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_DAY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MAX(RECORD_DATE) AS RECORD_DATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     ASSESS_MAIN.CHALLENGED_NAMES, ");
            stb.append("     ASSESS_MAIN.CHALLENGED_STATUS, ");
            stb.append("     ASSESS_CHECK1.CHECK_DATE AS CHECK_DATE1, ");
            stb.append("     ASSESS_CHECK1.CHECK_NAME AS CHECK_NAME1, ");
            stb.append("     ASSESS_CHECK1.CHECK_CENTER_TEXT AS CHECK_CENTER_TEXT1, ");
            stb.append("     ASSESS_CHECK2.CHECK_DATE AS CHECK_DATE2, ");
            stb.append("     ASSESS_CHECK2.CHECK_NAME AS CHECK_NAME2, ");
            stb.append("     ASSESS_CHECK2.CHECK_CENTER_TEXT AS CHECK_CENTER_TEXT2 ");
            stb.append(" FROM ");
            stb.append("     MAX_DAY, ");
            stb.append("     SCHREG_CHALLENGED_ASSESSMENT_MAIN_DAT ASSESS_MAIN ");
            stb.append("     LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT ASSESS_CHECK1 ");
            stb.append("           ON ASSESS_MAIN.YEAR = ASSESS_CHECK1.YEAR ");
            stb.append("          AND ASSESS_MAIN.SCHREGNO = ASSESS_CHECK1.SCHREGNO ");
            stb.append("          AND ASSESS_MAIN.RECORD_DATE = ASSESS_CHECK1.RECORD_DATE ");
            stb.append("          AND ASSESS_CHECK1.RECORD_SEQ = 1 ");
            stb.append("     LEFT JOIN SCHREG_CHALLENGED_ASSESSMENT_CHECK_RECORD_DAT ASSESS_CHECK2 ");
            stb.append("           ON ASSESS_MAIN.YEAR = ASSESS_CHECK2.YEAR ");
            stb.append("          AND ASSESS_MAIN.SCHREGNO = ASSESS_CHECK2.SCHREGNO ");
            stb.append("          AND ASSESS_MAIN.RECORD_DATE = ASSESS_CHECK2.RECORD_DATE ");
            stb.append("          AND ASSESS_CHECK2.RECORD_SEQ = 2 ");
            stb.append(" WHERE ");
            stb.append("     MAX_DAY.YEAR = ASSESS_MAIN.YEAR ");
            stb.append("     AND MAX_DAY.SCHREGNO = ASSESS_MAIN.SCHREGNO ");
            stb.append("     AND MAX_DAY.RECORD_DATE = ASSESS_MAIN.RECORD_DATE ");

            return stb.toString();
        }

        private int simpleChrCount(String str, char target){
        	int count = 0;

        	if (str == null || "".equals(str)) return count;

        	for(int nn = 0;nn < str.length();nn++){
        		if(str.charAt(nn) == target){
        			count++;
        		}
        	}

        	return count;
        }
        private void setBlockOterMain(final DB2UDB db2) {
            setBlockOter(db2, "02");
            setBlockOter(db2, "03");
            setBlockOter(db2, "04");
            setBlockOter(db2, "05");
            setBlockOter(db2, "06");
            setBlockOter(db2, "07");
            setBlockOter(db2, "09");
            setBlockOter(db2, "10");
        }

        private void setBlockOter(final DB2UDB db2, final String kindNo) {
            final BlockOther block;
            if ("02".equals(kindNo)) {
                block = new Block2();
            } else if ("03".equals(kindNo)) {
                block = new Block3();
            } else if ("04".equals(kindNo)) {
                block = new Block4();
            } else if ("05".equals(kindNo)) {
                block = new Block5();
            } else if ("06".equals(kindNo)) {
                block = new Block6();
            } else if ("07".equals(kindNo)) {
                block = new Block7();
            } else if ("09".equals(kindNo)) {
                block = new Block9();
            } else {
                //10まで
                block = new Block10();
            }
            block.setRemarkSeqMap(db2, _schregNo);
            _blockMap.put(kindNo, block);
        }

        private void setBlock8(final DB2UDB db2) {
            Block8 block8 = null;
            final String aPaternSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(aPaternSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String patern = rs.getString("PATERN");
                    final String useSemes = rs.getString("USE_SEMES");
                    final String guidancePattern = rs.getString("GUIDANCE_PATTERN");
                    final String gakubuSchoolKind = rs.getString("GAKUBU_SCHOOL_KIND");
                    final String condition = rs.getString("CONDITION");
                    final String groupCd = rs.getString("GROUPCD");
                    final String ghrCd = rs.getString("GHR_CD");
                    block8 = new Block8(patern, useSemes, guidancePattern, gakubuSchoolKind, condition, groupCd, ghrCd);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null != block8) {
                block8.setTitle(db2);
                block8.setSubclassMap(db2, _schregNo, _grade, _hrClass);
            }
            _blockMap.put("08", block8);
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     SUBSTR(A035.NAMESPARE1, 2, 1) AS PATERN, ");
            stb.append("     A035.NAMESPARE2 AS USE_SEMES, ");
            stb.append("     L1.GUIDANCE_PATTERN, ");
            stb.append("     L1.GAKUBU_SCHOOL_KIND, ");
            stb.append("     L1.CONDITION, ");
            stb.append("     T1.GROUPCD, ");
            stb.append("     T1.GHR_CD ");
            stb.append(" FROM ");
            stb.append("     GRADE_KIND_SCHREG_GROUP_DAT T1 ");
            stb.append("     INNER JOIN GRADE_KIND_COMP_GROUP_YMST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("          AND T1.SEMESTER    = L1.SEMESTER ");
            stb.append("          AND T1.GAKUBU_SCHOOL_KIND = L1.GAKUBU_SCHOOL_KIND ");
            stb.append("          AND T1.GHR_CD      = L1.GHR_CD ");
            stb.append("          AND T1.GRADE       = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS    = L1.HR_CLASS ");
            stb.append("          AND T1.CONDITION   = L1.CONDITION ");
            stb.append("          AND T1.GROUPCD     = L1.GROUPCD ");
            stb.append("     INNER JOIN NAME_MST A035 ON A035.NAMECD1 = 'A035' ");
            stb.append("           AND L1.GUIDANCE_PATTERN = A035.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '9' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }
    }

    abstract private class Block {
        protected boolean _pageChange;
        protected String _kindNo;
        protected String _formId;
        protected int _maxLen;
        protected int _maxLine;
        protected int _pageMaxLine;

        abstract void printOut(final Vrw32alp svf, final Student student);

        /** 改ページブロックなら改ページ */
        final void pageChange(final Vrw32alp svf) {
            if (_pageChange) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            } else if (PRINT_FRM2.equals(_formId) && !PRINT_FRM_NOW.equals(PRINT_FRM1) && !PRINT_FRM_NOW.equals(_formId)) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            }
        }

        /** チェックして改ページ */
        final void checkLineAndPageChange(final Vrw32alp svf, final int cnt) {
            final int checkMaxLine = IS_FIRST_PRINT ? PAGE_MAXLINE : FIRST_PAGE_MAXLINE;
            if (LINE_CNT + cnt > checkMaxLine) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
                IS_FIRST_PRINT = true;
            }
        }

        /** チェックして改ページ */
        final void checkLineAndPageChangeBlock8(final Vrw32alp svf, final int cnt) {
            if (LINE_CNT + cnt > _pageMaxLine) {
                svf.VrSetForm(_formId, 4);
                PRINT_FRM_NOW = _formId;
                LINE_CNT = 0;
            }
        }
    }

    abstract private class BlockOther extends Block {
        protected Map _remarkSeqMap;

        /** 印字データセット */
        public void setRemarkSeqMap(final DB2UDB db2, final String schregNo) {
            final String remarkSql = getRemarkSql(schregNo);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(remarkSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String seq = rs.getString("SEQ");
                    final String remark = rs.getString("REMARK");
                    final GuidanceRemark guidanceRemark = new GuidanceRemark(seq, remark);
                    _remarkSeqMap.put(seq, guidanceRemark);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getRemarkSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_SCHREG_REMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' AND ");
            stb.append("     SEMESTER = '9' AND ");
            stb.append("     SCHREGNO = '" + schregNo + "' AND ");
            stb.append("     DIV = '" + _kindNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEQ ");

            return stb.toString();
        }

        /** 標準印刷処理 */
        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            boolean titlePrint = false;
            final String title = (String) student._paternTitleMap.get(_kindNo);
            int grp = 1;
            for (Iterator itRemark = _remarkSeqMap.keySet().iterator(); itRemark.hasNext();) {
                final String seq = (String) itRemark.next();
                final GuidanceRemark guidanceRemark = (GuidanceRemark) _remarkSeqMap.get(seq);
                final Map kindSeqMap = (Map) student._kindNameMap.get(_kindNo);
                final KindNameDat kindNameDat = (KindNameDat) kindSeqMap.get(seq);
                final List setTitleList = KNJ_EditKinsoku.getTokenList(kindNameDat._kindName, 10);
                final List setRemarkList = KNJ_EditKinsoku.getTokenList(guidanceRemark._remark, _maxLen);
                final int maxLine = setRemarkList.size() > setTitleList.size() ? setRemarkList.size() : setTitleList.size();
                if (!titlePrint) {
                    checkLineAndPageChange(svf, maxLine + 1);
                    svf.VrAttribute("TITLE1", "PAINT=(0,85,2)");
                    svf.VrsOut("TITLE1", title);
                    svf.VrEndRecord();
                    LINE_CNT++;
                    titlePrint = true;
                } else {
                    checkLineAndPageChange(svf, maxLine);
                }
                for (int i = 0; i < maxLine; i++) {
                    svf.VrsOut("GRP1_1", String.valueOf(grp));
                    svf.VrsOut("GRP1_2", String.valueOf(grp));
                    if (i < setTitleList.size()) {
                        svf.VrAttribute("DIV1", "PAINT=(0,85,2)");
                        svf.VrsOut("DIV1", (String) setTitleList.get(i));
                    }
                    if (i < setRemarkList.size()) {
                        svf.VrsOut("CONTENT1", (String) setRemarkList.get(i));
                    }
                    svf.VrEndRecord();
                    LINE_CNT++;
                }
                grp++;
            }
        }
    }

    private class Block1 extends Block {
        final String _challengedNames;
        final String _challengedStatus;
        final String _checkDate1;
        final String _checkName1;
        final String _checkCenterText1;
        final String _checkDate2;
        final String _checkName2;
        final String _checkCenterText2;

        public Block1(final String challengedNames, final String challengedStatus, final String checkDate1,
                final String checkName1, final String checkCenterText1, final String checkDate2,
                final String checkName2, final String checkCenterText2) {
            _challengedNames = StringUtils.defaultString(challengedNames);
            _challengedStatus = StringUtils.defaultString(challengedStatus);
            _checkDate1 = StringUtils.defaultString(checkDate1);
            _checkName1 = StringUtils.defaultString(checkName1);
            _checkCenterText1 = StringUtils.defaultString(checkCenterText1);
            _checkDate2 = StringUtils.defaultString(checkDate2);
            _checkName2 = StringUtils.defaultString(checkName2);
            _checkCenterText2 = StringUtils.defaultString(checkCenterText2);
            _pageChange = false;
            _kindNo = "01";
            _formId = "KNJD426_1.frm";
        }

        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            //障害種別
            svf.VrsOut("MARK1", student._guidCourse);
            //作成日
            svf.VrsOut("MAKE_DATE", student._createDate);
            //作成者
            final String MakerField = KNJ_EditEdit.getMS932ByteLength(student._createUser) > 10 ? "2" : "1";
            svf.VrsOut("MAKER" + MakerField, student._createUser);
            //障害名等
            final String[] challengeNameArray = KNJ_EditEdit.get_token(_challengedNames, 30, 6);
            if (null != challengeNameArray) {
            	for (int i = 0; i < challengeNameArray.length; i++) {
            		svf.VrsOutn("DIAG_NAME", i + 1, challengeNameArray[i]);
            	}
            }
            //障害の概要
            final String[] challengeStatusArray = KNJ_EditEdit.get_token(_challengedStatus, 100, 3);
            if (null != challengeStatusArray) {
            	for (int i = 0; i < challengeStatusArray.length; i++) {
            		svf.VrsOutn("DIAG_NAME1", i + 1, challengeStatusArray[i]);
            	}
            }
            //検査日1
            svf.VrsOut("INSPECT_DATE1", _checkDate1.replace('-', '/'));
            //検査機関1
            svf.VrsOut("INSPECT_AGENT1", _checkCenterText1);
            //検査名1
            svf.VrsOut("INSPECT_ITEM1", _checkName1);
            //検査日2
            svf.VrsOut("INSPECT_DATE2", _checkDate2.replace('-', '/'));
            //検査機関2
            svf.VrsOut("INSPECT_AGENT2", _checkCenterText2);
            //検査名2
            svf.VrsOut("INSPECT_ITEM2", _checkName2);

            //空行１ページ目のレコードがない時の対策
            svf.VrsOut("BLANK", "1");
            svf.VrEndRecord();
        }
    }

    private class Block2 extends BlockOther {

        public Block2() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "02";
            _formId = "KNJD426_2.frm";
            _maxLen = 80;
            _maxLine = 25;
        }
    }

    private class Block3 extends BlockOther {

        public Block3() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "03";
            _formId = "KNJD426_2.frm";
            _maxLen = 80;
            _maxLine = 25;
        }
    }

    private class Block4 extends BlockOther {

        public Block4() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "04";
            _formId = "KNJD426_2.frm";
            _maxLen = 88;
            _maxLine = 25;
        }

        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            boolean titlePrint = false;
            final String title = (String) student._paternTitleMap.get(_kindNo);
            for (Iterator itRemark = _remarkSeqMap.keySet().iterator(); itRemark.hasNext();) {
                final String seq = (String) itRemark.next();
                final GuidanceRemark guidanceRemark = (GuidanceRemark) _remarkSeqMap.get(seq);
                final List setRemarkList = KNJ_EditKinsoku.getTokenList(guidanceRemark._remark, _maxLen);
                final int maxLine = setRemarkList.size();
                if (!titlePrint) {
                    checkLineAndPageChange(svf, maxLine + 1);
                    svf.VrAttribute("TITLE1", "PAINT=(0,85,2)");
                    svf.VrsOut("TITLE1", title);
                    svf.VrEndRecord();
                    LINE_CNT++;
                    titlePrint = true;
                } else {
                    checkLineAndPageChange(svf, maxLine);
                }
                for (int i = 0; i < maxLine; i++) {
                    svf.VrsOut("GRP2", String.valueOf(1));
                    svf.VrsOut("CONTENT2", (String) setRemarkList.get(i));
                    svf.VrEndRecord();
                    LINE_CNT++;
                }
            }
        }
    }

    private class Block5 extends BlockOther {

        public Block5() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "05";
            _formId = "KNJD426_2.frm";
            _maxLen = 80;
            _maxLine = 25;
        }
    }

    private class Block6 extends BlockOther {

        public Block6() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "06";
            _formId = "KNJD426_2.frm";
            _maxLen = 80;
            _maxLine = 25;
        }
    }

    private class Block7 extends BlockOther {

        public Block7() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = true;
            _kindNo = "07";
            _formId = "KNJD426_2.frm";
            _maxLen = 76;
            _maxLine = 25;
        }

        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            boolean titlePrint = false;
            final String title = (String) student._paternTitleMap.get(_kindNo);
            log.debug(title + " : " + LINE_CNT);
            int grp = 1;
            int kyoukaCnt = 1;
            for (Iterator itRemark = _remarkSeqMap.keySet().iterator(); itRemark.hasNext();) {
                final String seq = (String) itRemark.next();
                final GuidanceRemark guidanceRemark = (GuidanceRemark) _remarkSeqMap.get(seq);
                final Subclass subclass = (Subclass) student._subclassMap.get(seq);
                if (null == subclass) {
                	continue;
                }
                final List setKyoukaNadoList = KNJ_EditKinsoku.getTokenList("教科等", 2);
                final List setTitleList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, 6);
                final List setRemarkList = KNJ_EditKinsoku.getTokenList(guidanceRemark._remark, _maxLen);
                final int maxLine;
                if (setRemarkList.size() > setTitleList.size() && setRemarkList.size() > setKyoukaNadoList.size()) {
                    maxLine = setRemarkList.size();
                } else if (setTitleList.size() > setKyoukaNadoList.size()) {
                    maxLine = setTitleList.size();
                } else {
                    maxLine = setKyoukaNadoList.size();
                }
                if (!titlePrint) {
                    checkLineAndPageChange(svf, maxLine + 1);
                    svf.VrAttribute("TITLE1", "PAINT=(0,85,2)");
                    svf.VrsOut("TITLE1", title);
                    svf.VrEndRecord();
                    LINE_CNT++;
                    titlePrint = true;
                } else {
                    checkLineAndPageChange(svf, maxLine);
                }
                for (int i = 0; i < maxLine; i++) {
                    svf.VrsOut("GRP4_1", String.valueOf(1));
                    svf.VrsOut("GRP4_2", String.valueOf(grp));
                    svf.VrsOut("GRP4_3", String.valueOf(grp));
                    svf.VrAttribute("CLASS_TITLE", "PAINT=(0,85,2)");
                    if (i < setKyoukaNadoList.size() && kyoukaCnt < 4) {
                        svf.VrsOut("CLASS_TITLE", (String) setKyoukaNadoList.get(i));
                        kyoukaCnt++;
                    }
                    if (i < setTitleList.size()) {
                    	svf.VrAttribute("CLASS_NAME1", "PAINT=(0,85,2)");
                        svf.VrsOut("CLASS_NAME1", (String) setTitleList.get(i));
                    }
                    if (i < setRemarkList.size()) {
                        svf.VrsOut("HOPE1", (String) setRemarkList.get(i));
                    }
                    svf.VrEndRecord();
                    LINE_CNT++;
                }
                grp++;
            }
        }
    }

    private class Block8 extends Block {
        final String _patern;
        final String _useSemes;
        final String _guidancePattern;
        final String _gakubuSchoolKind;
        final String _condition;
        final String _groupCd;
        final String _ghrCd;
        String _itemRemark1;
        String _itemRemark2;
        String _itemRemark3;
        String _itemRemark4;
        String _itemRemark5;
        Map _subclassMap;

        public Block8(final String patern, final String useSemes, final String guidancePattern,
                final String gakubuSchoolKind, final String condition, final String groupCd,
                final String ghrCd) {
            _patern = patern;
            _useSemes = useSemes;
            _guidancePattern = guidancePattern;
            _gakubuSchoolKind = gakubuSchoolKind;
            _condition = condition;
            _groupCd = groupCd;
            _ghrCd = ghrCd;
            _subclassMap = new LinkedMap();
            _pageChange = true;
            _kindNo = "08";
            if ("A".equals(patern)) {
                _formId = "KNJD426_3_1.frm";
                _pageMaxLine = 41;
            } else if ("B".equals(patern)) {
                _formId = "KNJD426_3_2.frm";
                _pageMaxLine = 51;
            } else if ("C".equals(patern)) {
                _formId = "KNJD426_3_3.frm";
                _pageMaxLine = 50;
            } else {
                _formId = "KNJD426_3_4.frm";
                _pageMaxLine = 50;
            }
        }

        public void setTitle(final DB2UDB db2) {
            final String titleSql = getBlock8PaternSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(titleSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _itemRemark1 = rs.getString("ITEM_REMARK1");
                    _itemRemark2 = rs.getString("ITEM_REMARK2");
                    _itemRemark3 = rs.getString("ITEM_REMARK3");
                    _itemRemark4 = rs.getString("ITEM_REMARK4");
                    _itemRemark5 = rs.getString("ITEM_REMARK5");
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8PaternSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     GUID_ITEM.* ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_ITEM_NAME_DAT GUID_ITEM ");
            stb.append(" WHERE ");
            stb.append("     GUID_ITEM.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND GUID_ITEM.SEMESTER = '9' ");
            stb.append("     AND GUID_ITEM.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append("     AND GUID_ITEM.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("     AND GUID_ITEM.CONDITION = '" + _condition + "' ");

            return stb.toString();
        }

        public void setSubclassMap(final DB2UDB db2, final String schregNo, final String grade,
                final String hrClass) {
            final String subclassSql = getBlock8SubclassSql(schregNo, grade, hrClass);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String unitCd = rs.getString("UNITCD");
                    final String unitName = rs.getString("UNITNAME");
                    final String guidancePattern = rs.getString("GUIDANCE_PATTERN");
                    final String seq = rs.getString("SEQ");
                    final String remark = rs.getString("REMARK");
                    final Subclass subclass;
                    final String key = classCd + schoolKind + curriculumCd + subclassCd;
                    if (_subclassMap.containsKey(key)) {
                        subclass = (Subclass) _subclassMap.get(key);
                    } else {
                        subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, unitCd);
                    }
                    subclass.setSemeUnitMap(semester, unitCd, unitName, guidancePattern, seq, remark);
                    _subclassMap.put(key, subclass);
                }
            } catch (SQLException ex) {
                log.error("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getBlock8SubclassSql(final String schregNo, final String grade,
                final String hrClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GUID_SUB.CLASSCD, ");
            stb.append("     GUID_SUB.SCHOOL_KIND, ");
            stb.append("     GUID_SUB.CURRICULUM_CD, ");
            stb.append("     GUID_SUB.SUBCLASSCD, ");
            stb.append("     GUID_SUB.SEMESTER, ");
            stb.append("     GUID_SUB.UNITCD, ");
            stb.append("     UNIT_M.UNITNAME, ");
            stb.append("     GUID_SUB.GUIDANCE_PATTERN, ");
            stb.append("     GUID_SUB.SEQ, ");
            stb.append("     GUID_SUB.REMARK, ");
            stb.append("     SUBM.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT GUID_SUB ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON GUID_SUB.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND GUID_SUB.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND GUID_SUB.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND GUID_SUB.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN GRADE_KIND_UNIT_GROUP_YMST UNIT_M ON GUID_SUB.YEAR = UNIT_M.YEAR ");
            stb.append("          AND UNIT_M.SEMESTER = '9' ");
            stb.append("          AND UNIT_M.GAKUBU_SCHOOL_KIND = '" + _gakubuSchoolKind + "' ");
            stb.append("          AND UNIT_M.GHR_CD = '" + _ghrCd + "' ");
            stb.append("          AND UNIT_M.GRADE = '" + grade + "' ");
            stb.append("          AND UNIT_M.HR_CLASS = '" + hrClass + "' ");
            stb.append("          AND UNIT_M.CONDITION = '" + _condition + "' ");
            stb.append("          AND UNIT_M.GROUPCD = '" + _groupCd + "' ");
            stb.append("          AND GUID_SUB.UNITCD = UNIT_M.UNITCD ");
            stb.append("          AND GUID_SUB.CLASSCD = UNIT_M.CLASSCD ");
            stb.append("          AND GUID_SUB.SCHOOL_KIND = UNIT_M.SCHOOL_KIND ");
            stb.append("          AND GUID_SUB.CURRICULUM_CD = UNIT_M.CURRICULUM_CD ");
            stb.append("          AND GUID_SUB.SUBCLASSCD = UNIT_M.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     GUID_SUB.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND GUID_SUB.SCHREGNO = '" + schregNo + "' ");
            stb.append("     AND GUID_SUB.GUIDANCE_PATTERN = '" + _guidancePattern + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GUID_SUB.CLASSCD, ");
            stb.append("     GUID_SUB.SCHOOL_KIND, ");
            stb.append("     GUID_SUB.CURRICULUM_CD, ");
            stb.append("     GUID_SUB.SUBCLASSCD, ");
            stb.append("     GUID_SUB.SEMESTER, ");
            stb.append("     GUID_SUB.UNITCD, ");
            stb.append("     GUID_SUB.SEQ ");
            return stb.toString();
        }

        void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            if ("A".equals(_patern)) {
                printOutA(svf, student);
            } else if ("B".equals(_patern)) {
                printOutB(svf, student);
            } else if ("C".equals(_patern)) {
                printOutC(svf, student);
            } else {
                printOutD(svf, student);
            }
        }

        public void printOutA(final Vrw32alp svf, final Student student) {
            pageChange(svf);

            setHeadA(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            int grp4 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = 2;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                final Map printSemester = new HashMap();
                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    //単元
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                        //登録データ1
                        final String remark1 = (String) unitData._unitSeqMap.get("1");
                        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 70);

                        int maxLine = setRemarkList1.size();
                        if (!printSubclass.containsKey(subclassCd) && setSubclassList.size() > maxLine) {
                            maxLine = setSubclassList.size();
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        checkLineAndPageChangeBlock8(svf, maxLine);
                        setHeadA(svf, student);
                        for (int i = 0; i < maxLine; i++) {
                            final String fieldName;
                            if (subclass._isUnit) {
                                fieldName = "1";
                                svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                grp4 = grp2;
                                if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                    final String unitField = "UNIT" + fieldName;
                                    svf.VrAttribute(unitField, "PAINT=(0,85,2)");
                                    svf.VrsOut(unitField, (String) setUnitList.get(i));
                                }
                            } else {
                                fieldName = "2";
                                grp4 = grp3;
                            }
                            final String classNameField = "CLASS_NAME" + fieldName;
                            svf.VrAttribute(classNameField, "PAINT=(0,85,2)");
                            if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                svf.VrsOut(classNameField, (String) setSubclassList.get(i));
                            }
                            final String semeField = "SEMESTER" + fieldName;
                            svf.VrAttribute(semeField, "PAINT=(0,85,2)");
                            if (!printSemester.containsKey(semester) && i == 0) {
                                svf.VrsOut(semeField, (String) _param._semesterMap.get(semester));
                            }
                            svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp4));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("CONTENT" + fieldName, (String) setRemarkList1.get(i));
                            }
                            svf.VrEndRecord();
                            LINE_CNT++;
                        }
                        printSemester.put(semester, semester);
                        printUnit.put(unitCd, unitCd);
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadA(final Vrw32alp svf, final Student student) {
            svf.VrAttribute("HEADER_NAME1", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
            final List subTitleList = new ArrayList();
            subTitleList.add("教科・領域");
            subTitleList.add("学期");
            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, "PAINT=(0,85,2)");
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
        }

        public void printOutB(final Vrw32alp svf, final Student student) {
            pageChange(svf);

            setHeadB(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            int grp4 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = subclass._isUnit ? 6 : 12;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                final Map printSemester = new HashMap();
                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    //単元
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                        //登録データ1
                        final String remark1 = (String) unitData._unitSeqMap.get("1");
                        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 20);
                        //登録データ2
                        final String remark2 = (String) unitData._unitSeqMap.get("2");
                        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 24);
                        //登録データ3
                        final String remark3 = (String) unitData._unitSeqMap.get("3");
                        final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 36);

                        int maxLine = setRemarkList1.size();
                        if (setRemarkList2.size() > maxLine) {
                            maxLine = setRemarkList2.size();
                        }
                        if (setRemarkList3.size() > maxLine) {
                            maxLine = setRemarkList3.size();
                        }
                        if (setSubclassList.size() > maxLine) {
                            maxLine = setSubclassList.size();
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        checkLineAndPageChangeBlock8(svf, maxLine);
                        setHeadB(svf, student);
                        for (int i = 0; i < maxLine; i++) {
                            final String fieldName;
                            if (subclass._isUnit) {
                                fieldName = "1";
                                svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                grp4 = grp2;
                                if (i < setUnitList.size()) {
                                    final String unitField = "UNIT" + fieldName;
                                    svf.VrAttribute(unitField, "PAINT=(0,85,2)");
                                    svf.VrsOut(unitField, (String) setUnitList.get(i));
                                }
                            } else {
                                fieldName = "2";
                                grp4 = grp3;
                            }
                            final String classNameField = "CLASS_NAME" + fieldName;
                            svf.VrAttribute(classNameField, "PAINT=(0,85,2)");
                            if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                svf.VrsOut(classNameField, (String) setSubclassList.get(i));
                            }
                            final String semeField = "SEMESTER" + fieldName;
                            svf.VrAttribute(semeField, "PAINT=(0,85,2)");
                            if (!printSemester.containsKey(semester) && i == 0) {
                                svf.VrsOut(semeField, (String) _param._semesterMap.get(semester));
                            }
                            svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp4));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                            }
                            if (i < setRemarkList2.size()) {
                                svf.VrsOut("METHOD" + fieldName, (String) setRemarkList2.get(i));
                            }
                            if (i < setRemarkList3.size()) {
                                svf.VrsOut("VAL" + fieldName, (String) setRemarkList3.get(i));
                            }
                            svf.VrEndRecord();
                            LINE_CNT++;
                        }
                        printSemester.put(semester, semester);
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadB(final Vrw32alp svf, final Student student) {
            svf.VrAttribute("HEADER_NAME1", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrAttribute("HEADER_NAME2", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME2", _itemRemark2);
            svf.VrAttribute("HEADER_NAME3", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME3", _itemRemark3);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
            final List subTitleList = new ArrayList();
            subTitleList.add("教科・領域");
            subTitleList.add("学期");
            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, "PAINT=(0,85,2)");
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
        }

        public void printOutC(final Vrw32alp svf, final Student student) {
            pageChange(svf);

            setHeadC(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            int grp4 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = 6;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                final Map printSemester = new HashMap();
                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    //単元
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                        //登録データ1
                        final String remark1 = (String) unitData._unitSeqMap.get("1");
                        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 18);
                        //登録データ2
                        final String remark2 = (String) unitData._unitSeqMap.get("2");
                        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 8);
                        //登録データ3
                        final String remark3 = (String) unitData._unitSeqMap.get("3");
                        final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 22);
                        //登録データ4
                        final String remark4 = (String) unitData._unitSeqMap.get("4");
                        final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(remark4, 32);

                        int maxLine = setRemarkList1.size();
                        if (setRemarkList2.size() > maxLine) {
                            maxLine = setRemarkList2.size();
                        }
                        if (setRemarkList3.size() > maxLine) {
                            maxLine = setRemarkList3.size();
                        }
                        if (setRemarkList4.size() > maxLine) {
                            maxLine = setRemarkList4.size();
                        }
                        if (setSubclassList.size() > maxLine) {
                            maxLine = setSubclassList.size();
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        checkLineAndPageChangeBlock8(svf, maxLine);
                        setHeadC(svf, student);
                        for (int i = 0; i < maxLine; i++) {
                            final String fieldName;
                            if (subclass._isUnit) {
                                fieldName = "1";
                                svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                grp4 = grp2;
                                if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                    final String unitField = "UNIT" + fieldName;
                                    svf.VrAttribute(unitField, "PAINT=(0,85,2)");
                                    svf.VrsOut(unitField, (String) setUnitList.get(i));
                                }
                            } else {
                                fieldName = "2";
                                grp4 = grp3;
                            }
                            final String classNameField = "CLASS_NAME" + fieldName;
                            svf.VrAttribute(classNameField, "PAINT=(0,85,2)");
                            if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                svf.VrsOut(classNameField, (String) setSubclassList.get(i));
                            }
                            final String semeField = "SEMESTER" + fieldName;
                            svf.VrAttribute(semeField, "PAINT=(0,85,2)");
                            if (!printSemester.containsKey(semester) && i == 0) {
                                svf.VrsOut(semeField, (String) _param._semesterMap.get(semester));
                            }
                            svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_7", String.valueOf(grp4));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                            }
                            if (i < setRemarkList2.size()) {
                                svf.VrsOut("CAREER" + fieldName, (String) setRemarkList2.get(i));
                            }
                            if (i < setRemarkList3.size()) {
                                svf.VrsOut("METHOD" + fieldName, (String) setRemarkList3.get(i));
                            }
                            if (i < setRemarkList4.size()) {
                                svf.VrsOut("VAL" + fieldName, (String) setRemarkList4.get(i));
                            }
                            svf.VrEndRecord();
                            LINE_CNT++;
                        }
                        printSemester.put(semester, semester);
                        printUnit.put(unitCd, unitCd);
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadC(final Vrw32alp svf, final Student student) {
            svf.VrAttribute("HEADER_NAME1", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            final List setHead2List = KNJ_EditKinsoku.getTokenList(_itemRemark2, 8);
            int head2Cnt = 1;
            for (Iterator itHead2 = setHead2List.iterator(); itHead2.hasNext();) {
                final String setRemark = (String) itHead2.next();
                svf.VrAttribute("HEADER_NAME2_" + head2Cnt, "PAINT=(0,85,2)");
                svf.VrsOut("HEADER_NAME2_" + head2Cnt, setRemark);
                head2Cnt++;
            }
            svf.VrAttribute("HEADER_NAME3", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME3", _itemRemark3);
            svf.VrAttribute("HEADER_NAME4", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME4", _itemRemark4);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
            final List subTitleList = new ArrayList();
            subTitleList.add("教科・領域");
            subTitleList.add("学期");
            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, "PAINT=(0,85,2)");
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
        }

        public void printOutD(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            setHeadD(svf, student);

            int grp1 = 1;
            int grp2 = 1;
            int grp3 = 1;
            int grp4 = 1;
            final Map printSubclass = new HashMap();
            //科目
            for (Iterator itRemark = _subclassMap.keySet().iterator(); itRemark.hasNext();) {
                final String subclassCd = (String) itRemark.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclassCd);
                final int subclassLen = subclass._isUnit? 6 : 12;
                final List setSubclassList = KNJ_EditKinsoku.getTokenList(subclass._subclassName, subclassLen);

                final Map printSemester = new HashMap();
                //学期
                for (Iterator itSemeUnit = subclass._semeUnitMap.keySet().iterator(); itSemeUnit.hasNext();) {
                    final String semester = (String) itSemeUnit.next();
                    final Map unitMap = (Map) subclass._semeUnitMap.get(semester);

                    final Map printUnit = new HashMap();
                    //単元
                    for (Iterator itUnit = unitMap.keySet().iterator(); itUnit.hasNext();) {
                        final String unitCd = (String) itUnit.next();
                        final UnitData unitData = (UnitData) unitMap.get(unitCd);

                        //単元名
                        final List setUnitList = KNJ_EditKinsoku.getTokenList(unitData._unitName, 6);
                        //登録データ1
                        final String remark1 = (String) unitData._unitSeqMap.get("1");
                        final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(remark1, 16);
                        //登録データ2
                        final String remark2 = (String) unitData._unitSeqMap.get("2");
                        final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(remark2, 16);
                        //登録データ3
                        final String remark3 = (String) unitData._unitSeqMap.get("3");
                        final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(remark3, 6);
                        //登録データ4
                        final String remark4 = (String) unitData._unitSeqMap.get("4");
                        final List setRemarkList4 = KNJ_EditKinsoku.getTokenList(remark4, 20);
                        //登録データ5
                        final String remark5 = (String) unitData._unitSeqMap.get("5");
                        final List setRemarkList5 = KNJ_EditKinsoku.getTokenList(remark5, 32);

                        int maxLine = setRemarkList1.size();
                        if (setRemarkList2.size() > maxLine) {
                            maxLine = setRemarkList2.size();
                        }
                        if (setRemarkList3.size() > maxLine) {
                            maxLine = setRemarkList3.size();
                        }
                        if (setRemarkList4.size() > maxLine) {
                            maxLine = setRemarkList4.size();
                        }
                        if (setRemarkList5.size() > maxLine) {
                            maxLine = setRemarkList5.size();
                        }
                        if (setSubclassList.size() > maxLine) {
                            maxLine = setSubclassList.size();
                        }
                        if (setUnitList.size() > maxLine) {
                            maxLine = setUnitList.size();
                        }

                        checkLineAndPageChangeBlock8(svf, maxLine);
                        setHeadD(svf, student);
                        for (int i = 0; i < maxLine; i++) {
                            final String fieldName;
                            if (subclass._isUnit) {
                                fieldName = "1";
                                svf.VrsOut("GRP" + fieldName + "_2", String.valueOf(grp2));
                                grp4 = grp2;
                                if (!printUnit.containsKey(unitCd) && i < setUnitList.size()) {
                                    final String unitField = "UNIT" + fieldName;
                                    svf.VrAttribute(unitField, "PAINT=(0,85,2)");
                                    svf.VrsOut(unitField, (String) setUnitList.get(i));
                                }
                            } else {
                                fieldName = "2";
                                grp4 = grp3;
                            }
                            final String classNameField = "CLASS_NAME" + fieldName;
                            svf.VrAttribute(classNameField, "PAINT=(0,85,2)");
                            if (!printSubclass.containsKey(subclassCd) && i < setSubclassList.size()) {
                                svf.VrsOut(classNameField, (String) setSubclassList.get(i));
                            }
                            final String semeField = "SEMESTER" + fieldName;
                            svf.VrAttribute(semeField, "PAINT=(0,85,2)");
                            if (!printSemester.containsKey(semester) && i == 0) {
                                svf.VrsOut(semeField, (String) _param._semesterMap.get(semester));
                            }
                            svf.VrsOut("GRP" + fieldName + "_1", String.valueOf(grp1));
                            svf.VrsOut("GRP" + fieldName + "_3", String.valueOf(grp3));
                            svf.VrsOut("GRP" + fieldName + "_4", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_5", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_6", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_7", String.valueOf(grp4));
                            svf.VrsOut("GRP" + fieldName + "_8", String.valueOf(grp4));
                            if (i < setRemarkList1.size()) {
                                svf.VrsOut("HOPE" + fieldName, (String) setRemarkList1.get(i));
                            }
                            if (i < setRemarkList2.size()) {
                                svf.VrsOut("LEAD_CONTENT" + fieldName, (String) setRemarkList2.get(i));
                            }
                            if (i < setRemarkList3.size()) {
                                svf.VrsOut("LEAD_FORM" + fieldName, (String) setRemarkList3.get(i));
                            }
                            if (i < setRemarkList4.size()) {
                                svf.VrsOut("METHOD" + fieldName, (String) setRemarkList4.get(i));
                            }
                            if (i < setRemarkList5.size()) {
                                svf.VrsOut("VAL" + fieldName, (String) setRemarkList5.get(i));
                            }
                            svf.VrEndRecord();
                            LINE_CNT++;
                        }
                        printSemester.put(semester, semester);
                        printUnit.put(unitCd, unitCd);
                        printSubclass.put(subclassCd, subclassCd);
                        grp2++;
                    }
                    grp3++;
                }
                grp1++;
            }
        }

        private void setHeadD(final Vrw32alp svf, final Student student) {
            svf.VrsOut("SEMESTER", (String) _param._semesterMap.get(_param._semester));

            svf.VrAttribute("HEADER_NAME1", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME1", _itemRemark1);
            svf.VrAttribute("HEADER_NAME2", "PAINT=(0,85,2)");
            svf.VrsOut("HEADER_NAME2", _itemRemark2);
            final List setHead3List = KNJ_EditKinsoku.getTokenList(_itemRemark3, 4);
            int head3Cnt = 1;
            for (Iterator itHead3 = setHead3List.iterator(); itHead3.hasNext();) {
                final String setRemark = (String) itHead3.next();
                svf.VrsOut("HEADER_NAME3_" + head3Cnt, setRemark);
                head3Cnt++;
            }
            svf.VrsOut("HEADER_NAME4", _itemRemark4);
            svf.VrsOut("HEADER_NAME5", _itemRemark5);
            svf.VrsOut("NAME", "氏名 (" + student._name + ")");
            final List subTitleList = new ArrayList();
            subTitleList.add("教科・領域");
            subTitleList.add("学期");
            int titleCnt = 1;
            for (Iterator itTitle = subTitleList.iterator(); itTitle.hasNext();) {
                final String subTitleName = (String) itTitle.next();
                final String titleField = "SUB_TITLE" + titleCnt;
                svf.VrAttribute(titleField, "PAINT=(0,85,2)");
                svf.VrsOut(titleField, subTitleName);
                titleCnt++;
            }
        }
    }

    private class Block9 extends BlockOther {

        public Block9() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = true;
            _kindNo = "09";
            _formId = "KNJD426_2.frm";
            _maxLen = 88;
            _maxLine = 25;
        }

        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            boolean titlePrint = false;
            final String title = (String) student._paternTitleMap.get(_kindNo);
            for (Iterator itRemark = _remarkSeqMap.keySet().iterator(); itRemark.hasNext();) {
                final String seq = (String) itRemark.next();
                final GuidanceRemark guidanceRemark = (GuidanceRemark) _remarkSeqMap.get(seq);
                final List setRemarkList = KNJ_EditKinsoku.getTokenList(guidanceRemark._remark, _maxLen);
                final int maxLine = setRemarkList.size();
                if (!titlePrint) {
                    checkLineAndPageChange(svf, maxLine + 1);
                    svf.VrAttribute("TITLE1", "PAINT=(0,85,2)");
                    svf.VrsOut("TITLE1", title);
                    svf.VrEndRecord();
                    LINE_CNT++;
                    titlePrint = true;
                } else {
                    checkLineAndPageChange(svf, maxLine);
                }
                for (int i = 0; i < maxLine; i++) {
                    svf.VrsOut("CONTENT2", (String) setRemarkList.get(i));
                    svf.VrEndRecord();
                    LINE_CNT++;
                }
            }
        }
    }

    private class Block10 extends BlockOther {
        final int _maxLen2;

        public Block10() {
            _remarkSeqMap = new LinkedMap();
            _pageChange = false;
            _kindNo = "10";
            _formId = "KNJD426_2.frm";
            _maxLen = 30;
            _maxLen2 = 20;
            _maxLine = 25;
        }

        public void printOut(final Vrw32alp svf, final Student student) {
            pageChange(svf);
            boolean titlePrint = false;;
            final String title = (String) student._paternTitleMap.get(_kindNo);

            final Map kindSeqMap = (Map) student._kindNameMap.get(_kindNo);
            final KindNameDat kindNameDat1 = (KindNameDat) kindSeqMap.get("1");
            final KindNameDat kindNameDat2 = (KindNameDat) kindSeqMap.get("2");
            final KindNameDat kindNameDat3 = (KindNameDat) kindSeqMap.get("3");

            if (null == _remarkSeqMap) {
                return;
            }
            final GuidanceRemark guidanceRemark1 = (GuidanceRemark) _remarkSeqMap.get("1");
            final GuidanceRemark guidanceRemark2 = (GuidanceRemark) _remarkSeqMap.get("2");
            final GuidanceRemark guidanceRemark3 = (GuidanceRemark) _remarkSeqMap.get("3");

            final List setRemarkList1 = KNJ_EditKinsoku.getTokenList(null != guidanceRemark1 ? guidanceRemark1._remark : "", _maxLen);
            final List setRemarkList2 = KNJ_EditKinsoku.getTokenList(null != guidanceRemark2 ? guidanceRemark2._remark : "", _maxLen);
            final List setRemarkList3 = KNJ_EditKinsoku.getTokenList(null != guidanceRemark3 ? guidanceRemark3._remark : "", _maxLen2);

            final int maxLine;
            if (setRemarkList1.size() > setRemarkList2.size() && setRemarkList1.size() > setRemarkList3.size()) {
                maxLine = setRemarkList1.size();
            } else if (setRemarkList2.size() > setRemarkList3.size()) {
                maxLine = setRemarkList2.size();
            } else {
                maxLine = setRemarkList3.size();
            }
            if (!titlePrint) {
                checkLineAndPageChange(svf, maxLine + 1);
                svf.VrAttribute("TITLE1", "PAINT=(0,85,2)");
                svf.VrsOut("TITLE1", title);
                svf.VrEndRecord();
                LINE_CNT++;
                titlePrint = true;

                svf.VrAttribute("CONTENT3_1", "PAINT=(0,85,2)");
                svf.VrsOut("CONTENT3_1", kindNameDat1._kindName);
                svf.VrAttribute("CONTENT3_2", "PAINT=(0,85,2)");
                svf.VrsOut("CONTENT3_2", kindNameDat2._kindName);
                svf.VrAttribute("CONTENT3_3_2", "PAINT=(0,85,2)");
                svf.VrsOut("CONTENT3_3_2", kindNameDat3._kindName);
                svf.VrAttribute("CONTENT3_3_3", "PAINT=(0,85,2)");
                svf.VrsOut("CONTENT3_3_3", "(関係機関からの情報など)");
                svf.VrEndRecord();
            } else {
                checkLineAndPageChange(svf, maxLine);
            }
            for (int i = 0; i < maxLine; i++) {
                svf.VrsOut("GRP3_1", String.valueOf(1));
                svf.VrsOut("GRP3_2", String.valueOf(1));
                svf.VrsOut("GRP3_3", String.valueOf(1));
                if (i < setRemarkList1.size()) {
                    svf.VrsOut("CONTENT3_1", (String) setRemarkList1.get(i));
                }
                if (i < setRemarkList2.size()) {
                    svf.VrsOut("CONTENT3_2", (String) setRemarkList2.get(i));
                }
                if (i < setRemarkList3.size()) {
                    svf.VrsOut("CONTENT3_3", (String) setRemarkList3.get(i));
                }
                svf.VrEndRecord();
                LINE_CNT++;
            }
        }
    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final boolean _isUnit;
        final Map _semeUnitMap;

        public Subclass(final String classCd, final String schoolKind, final String curriculumCd,
                final String subclassCd, final String subclassName, final String unitCd) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _isUnit = !"00".equals(unitCd);
            _semeUnitMap = new LinkedMap();
        }

        public void setSemeUnitMap(final String semester, final String unitCd, final String unitName,
                final String guidancePattern, final String seq, final String remark
        ) {
            UnitData unitData;
            final Map unitDataMap;
            if (_semeUnitMap.containsKey(semester)) {
                unitDataMap = (Map) _semeUnitMap.get(semester);
            } else {
                unitDataMap = new LinkedMap();
                _semeUnitMap.put(semester, unitDataMap);
            }
            if (unitDataMap.containsKey(unitCd)) {
                unitData = (UnitData) unitDataMap.get(unitCd);
            } else {
                unitData = new UnitData(unitCd, unitName, guidancePattern);
                unitDataMap.put(unitCd, unitData);
            }
            unitData._unitSeqMap.put(seq, remark);
        }

    }

    private class GuidanceRemark {
        final String _seq;
        final String _remark;

        public GuidanceRemark(
                final String seq,
                final String remark
        ) {
            _seq = seq;
            _remark = remark;
        }
    }

    private class UnitData {
        final String _unitCd;
        final String _unitName;
        final String _guidancePattern;
        final Map _unitSeqMap;

        public UnitData(final String unitCd, final String unitName, final String guidancePattern) {
            _unitCd = unitCd;
            _unitName = unitName;
            _guidancePattern = guidancePattern;
            _unitSeqMap = new HashMap();
        }
    }

    private class KindNameDat {
        final String _kindNo;
        final String _kindSeq;
        final String _kindName;

        public KindNameDat(
                final String kindNo,
                final String kindSeq,
                final String kindName
        ) {
            _kindNo = kindNo;
            _kindSeq = kindSeq;
            _kindName = kindName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71130 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;
        final String _schoolKind;
        final String _ghrCd;
        final String[] _categorySelected;
        final List _printBlockList;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _documentroot;
        final String _selectGhr;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _schoolName;
        final String _recordDate;
        final Map _semesterMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _printBlockList = new ArrayList();
            for (int i = 1; i <= 10; i++) {
                final String setVal = request.getParameter("CHK_PRINTBLOCK" + i);
                if (!StringUtils.isEmpty(setVal)) {
                    _printBlockList.add(setVal);
                }

            }
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _selectGhr = request.getParameter("SELECT_GHR");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _schoolName = getSchoolName(db2);
            _recordDate = "9999-03-31";
            _semesterMap = getSemesterMap(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            String retSchoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' ");

            final Map retSemesterMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retSemesterMap.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (SQLException ex) {
                log.error("certif_school_dat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemesterMap;
        }

    }
}

// eof
