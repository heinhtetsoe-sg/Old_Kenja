// kanji=漢字
/*
 * $Id: f5e2c93309e7333d8c7bf09c9cd5cdc524c0aac9 $
 *
 * 作成日: 2010/03/24 10:24:21 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: f5e2c93309e7333d8c7bf09c9cd5cdc524c0aac9 $
 */
public class KNJA143R {

    private static final Log log = LogFactory.getLog("KNJA143R.class");

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
            log.fatal("$Revision: 58779 $");
            KNJServletUtils.debugParam(request, log);

            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            final Param param = new Param(db2, request);

            _param = param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);
        if ("1".equals(_param._output)) {
            printHogoshaKakuninCard(studentList, svf);
        } else if ("2".equals(_param._output)) {
            printHikiwatashiKakuninCard(studentList, svf);
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
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

    private String getCdColorSortNum(final int setno) {
        final String[] retnumlist = {"01", "02", "03", "04", "05", "06"};

        if (!_param._rotate_bg) {
            return retnumlist[setno];
        } else {
            final int rotatewk = (Integer.parseInt(_param._year) % 6) - 1;
            final int rotatenum = rotatewk < 0 ? 6 + rotatewk : rotatewk;
            log.debug(setno + ":" + retnumlist[((setno + rotatenum) % 6)]);
            return retnumlist[((setno + rotatenum) % 6)];
        }
    }

    // 保護者確認カード
    private void printHogoshaKakuninCard(final List studentList, final Vrw32alp svf) {
        final int maxLine = 4;
        final int maxRow = 2;
        final List pageList = getPageList(studentList, maxLine * maxRow);
        final String form = "KNJA143R_1.frm";

        final String colorGreen = "PAINT=(2,0,1)";
        final String colorBlue  = "PAINT=(12,0,1)";
        final String colorPink  = "PAINT=(13,0,1)";
        final String colorYello = "PAINT=(11,0,1)";
        final String colorRed   = "PAINT=(9,0,1)";
        final String colorGray  = "PAINT=(4,0,1)";
        final Map gradeCdColorAttrMap = new HashMap();
        gradeCdColorAttrMap.put(getCdColorSortNum(0), colorGreen);
        gradeCdColorAttrMap.put(getCdColorSortNum(1), colorBlue);
        gradeCdColorAttrMap.put(getCdColorSortNum(2), colorPink);
        gradeCdColorAttrMap.put(getCdColorSortNum(3), colorYello);
        gradeCdColorAttrMap.put(getCdColorSortNum(4), colorRed);
        gradeCdColorAttrMap.put(getCdColorSortNum(5), colorGray);

        for (int pi = 0; pi < pageList.size(); pi++) {

            svf.VrSetForm(form, 1);

            final List dataList = (List) pageList.get(pi);

            final List lineList = getPageList(dataList, maxRow);

            for (int li = 0; li < lineList.size(); li++) {
                final List row = (List) lineList.get(li);

                final int line = li + 1;
                for (int ri = 0; ri < row.size(); ri++) {
                    final String ssi = String.valueOf(ri + 1);
                    final Student student = (Student) row.get(ri);

                    svf.VrsOutn("TITLE" + ssi, line, "保護者確認カード");
                    final String colorAttribute = (String) gradeCdColorAttrMap.get(student._gradeCd);
                    if (null != colorAttribute) {
                        svf.VrAttributen("TITLE" + ssi, line, colorAttribute);
                    }
                    if (null != _param._schoollogoFilePath) {
                        svf.VrsOutn("SCHOOL_LOGO" + ssi, line, _param._schoollogoFilePath); //
                    }
                    svf.VrsOutn("SCHOOL_NAME" + ssi, line, _param._certifSchoolSchoolName); // 学校名
                    svf.VrsOutn("SCHREG_NO" + ssi, line, student._schregno); // 学籍番号

                    final int ketaName1_ = getMS932ByteLength(student._name);
                    svf.VrsOutn("NAME" + ssi + "_" + (ketaName1_ <= 20 ? "1" : ketaName1_ <= 30 ? "2" : "3"), line, student._name);

                    final int ketaGuard_name1_ = getMS932ByteLength(student._guardName);
                    svf.VrsOutn("GUARD_NAME" + ssi + "_" + (ketaGuard_name1_ <= 20 ? "1" : ketaGuard_name1_ <= 30 ? "2" : "3"), line, student._guardName);
                }
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    // 引き渡し確認カード
    private void printHikiwatashiKakuninCard(final List studentList, final Vrw32alp svf) {
        final int maxLine = 3;
        final List pageList = getPageList(studentList, maxLine);
        final String form = "KNJA143R_2.frm";

        for (int pi = 0; pi < pageList.size(); pi++) {

            svf.VrSetForm(form, 1);

            final List dataList = (List) pageList.get(pi);
            for (int j = 0; j < dataList.size(); j++) {
                final int line = j + 1;

                final Student student = (Student) dataList.get(j);

                // 上段
                svf.VrsOutn("NENDO", line, KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
                svf.VrsOutn("SCHOOL_NAME1", line, _param._certifSchoolRemark3); // 学校名
                svf.VrsOutn("SCHREG_NO1", line, student._schregno); // 学籍番号
                svf.VrsOutn("HR_NAME", line, student.getPrintHrnameAttendno()); // 年組番
                svf.VrsOutn("SEX", line, student._sexname); // 性別
                svf.VrsOutn(getMS932ByteLength(student._name) <= 10 ? "NAME1" : getMS932ByteLength(student._name) <= 20 ? "NAME2" : "NAME3", line, student._name);
                svf.VrsOutn("KANA", line, student._nameKana); // かな
                svf.VrsOutn("STUDENT_DIV", line, "K".equals(student._schoolKind) ? "園児名" : "P".equals(student._schoolKind) ? "児童名" : "生徒名");
                svf.VrsOutn("BIRTHDAY1", line, KNJ_EditDate.h_format_JP_N(student._birthday)); // 生年月日
                svf.VrsOutn("BIRTHDAY2", line, KNJ_EditDate.h_format_JP_MD(student._birthday)); // 生年月日
                svf.VrsOutn("ZIP_NO", line, student._zipcd); // 郵便番号
                svf.VrsOutn("TEL_NO", line, student._telno); // 電話番号

                svf.VrsOutn("ADDR1", line, student._addr1); //
                svf.VrsOutn("ADDR2", line, student._addr2); //
                svf.VrsOutn("BLOOD1", line, student._bloodtype); // 血液型
                svf.VrsOutn("BLOOD2", line, student._bloodRh); // 血液型

                svf.VrsOutn(getMS932ByteLength(student._guardName) <= 20 ? "GUARD_NAME1_1" : getMS932ByteLength(student._guardName) <= 30 ? "GUARD_NAME1_2" : "GUARD_NAME1_3", line, student._guardName);
                svf.VrsOutn("GUARD_COMPANY1", line, student._guardWorkName); // 保護者勤務先
                svf.VrsOutn("GUARD_RELA1", line, student._relationshipName); // 保護者続柄名
                svf.VrsOutn("GUARD_TEL_NO1_1", line, student._guardTelno); // 保護者電話番号
                svf.VrsOutn("GUARD_TEL_NO1_2", line, student._guardWorkTelno); // 保護者電話番号

                svf.VrsOutn(getMS932ByteLength(student._guardName2) <= 20 ? "GUARD_NAME2_1" : getMS932ByteLength(student._guardName2) <= 30 ? "GUARD_NAME2_2" : "GUARD_NAME2_3", line, student._guardName2);
                svf.VrsOutn("GUARD_COMPANY2", line, student._guardWorkName2); // 保護者勤務先
                svf.VrsOutn("GUARD_RELA2", line, student._relationshipName2); // 保護者続柄名
                svf.VrsOutn("GUARD_TEL_NO2_1", line, student._guardTelno2); // 保護者電話番号
                svf.VrsOutn("GUARD_TEL_NO2_2", line, student._guardWorkTelno2); // 保護者電話番号

                // 下段
                for (int si = 0; si < Math.min(student._relaList.size(), 2); si++) {
                    final String ssi = String.valueOf(si + 1);
                    final Student.Rela rela = (Student.Rela) student._relaList.get(si);
                    svf.VrsOutn("BRO_GRADE" + ssi, line, rela._relaGradename1); // 兄弟姉妹学年
                    svf.VrsOutn("BRO_CLASS" + ssi, line, rela._relaHrClassName1); // 兄弟姉妹組
                    svf.VrsOutn("BRO_NAME" + ssi + "_" + (getMS932ByteLength(rela._relaname) <= 24 ? "1" : "2"), line, rela._relaname);
                    svf.VrsOutn("BRO_TEACHER_NAME" + ssi + "_" + (getMS932ByteLength(rela._relaStaffname1) <= 10 ? "1" : "2"), line, rela._relaStaffname1);
                }

                svf.VrsOutn(getMS932ByteLength(student._hikitori1Name) <= 14 ? "TAKEBACK_NAME1_1" : "TAKEBACK_NAME1_2", line, student._hikitori1Name);
                svf.VrsOutn("TAKEBACK_TEL_NO1", line, student._hikitori1Telno); // 引き取り者電話番号
                svf.VrsOutn(getMS932ByteLength(student._hikitori2Name) <= 14 ? "TAKEBACK_NAME2_1" : "TAKEBACK_NAME2_2", line, student._hikitori2Name);
                svf.VrsOutn("TAKEBACK_TEL_NO2", line, student._hikitori2Telno); // 引き取り者電話番号
                svf.VrsOutn(getMS932ByteLength(student._hikitori3Name) <= 14 ? "TAKEBACK_NAME3_1" : "TAKEBACK_NAME3_2", line, student._hikitori3Name);
                svf.VrsOutn("TAKEBACK_TEL_NO3", line, student._hikitori3Telno); // 引き取り者電話番号

                svf.VrsOutn("EM_ADDR1", line, StringUtils.defaultString(student._kinkyuhinan1Addr1) + StringUtils.defaultString(student._kinkyuhinan1Addr2)); // 緊急連絡先住所
                svf.VrsOutn("EM_NAME1", line, student._kinkyuhinan1Name); // 緊急連絡先名
                svf.VrsOutn("EM_TEL_NO1", line, student._kinkyuhinan1Telno); // 緊急連絡先電話番号
                svf.VrsOutn("EM_ADDR2", line, StringUtils.defaultString(student._kinkyuhinan2Addr1) + StringUtils.defaultString(student._kinkyuhinan2Addr2)); // 緊急連絡先住所
                svf.VrsOutn("EM_NAME2", line, student._kinkyuhinan2Name); // 緊急連絡先名
                svf.VrsOutn("EM_TEL_NO2", line, student._kinkyuhinan2Telno); // 緊急連絡先電話番号

                svf.VrsOutn("REMARK_NAME1", line, student._bikoName1); // 備考氏名
                svf.VrsOutn("REMARK_BELONG1", line, student._bikoShozoku1); // 備考所属
                svf.VrsOutn("REMARK_NAME2", line, student._bikoName2); // 備考氏名
                svf.VrsOutn("REMARK_BELONG2", line, student._bikoShozoku2); // 備考所属
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private static class Student {
        String _schregno;
        String _grade;
        String _hrClass;
        String _attendno;
        String _schoolKind;
        String _gradeCd;
        String _gradeName1;
        String _hrClassName1;
        String _name;
        String _nameKana;
        String _bloodtype;
        String _bloodRh;
        String _sex;
        String _sexname;
        String _birthday;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _telno;
        String _guardName;
        String _relationship;
        String _relationshipName;
        String _guardTelno;
        String _guardWorkName;
        String _guardWorkTelno;
        String _guardName2;
        String _relationship2;
        String _relationshipName2;
        String _guardTelno2;
        String _guardWorkName2;
        String _guardWorkTelno2;
        String _hikitori1Name;
        String _hikitori1Telno;
        String _hikitori2Name;
        String _hikitori2Telno;
        String _hikitori3Name;
        String _hikitori3Telno;
        String _kinkyuhinan1Name;
        String _kinkyuhinan1Addr1;
        String _kinkyuhinan1Addr2;
        String _kinkyuhinan1Telno;
        String _kinkyuhinan2Name;
        String _kinkyuhinan2Addr1;
        String _kinkyuhinan2Addr2;
        String _kinkyuhinan2Telno;
        String _bikoName1;
        String _bikoShozoku1;
        String _bikoName2;
        String _bikoShozoku2;
        final List _relaList = new ArrayList();

        private static class Rela {

            String _relaSchregno;
            String _relano;
            String _relaGrade;
            String _relaHrClass;
            String _relaAttendno;
            String _relaGradename1;
            String _relaHrClassName1;
            String _relaname;
            String _relaStaffname1;
        }


        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map schregnoMap = new HashMap();
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null == schregnoMap.get(rs.getString("SCHREGNO"))) {
                        final Student student = new Student();
                        student._schregno = rs.getString("SCHREGNO");
                        student._grade = rs.getString("GRADE");
                        student._hrClass = rs.getString("HR_CLASS");
                        student._attendno = rs.getString("ATTENDNO");
                        student._schoolKind = rs.getString("SCHOOL_KIND");
                        student._gradeCd = rs.getString("GRADE_CD");
                        student._gradeName1 = rs.getString("GRADE_NAME1");
                        student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        student._name = rs.getString("NAME");
                        student._nameKana = rs.getString("NAME_KANA");
                        student._bloodtype = rs.getString("BLOODTYPE");
                        student._bloodRh = rs.getString("BLOOD_RH");
                        student._sex = rs.getString("SEX");
                        student._sexname = rs.getString("SEX_NAME");
                        student._birthday = rs.getString("BIRTHDAY");
                        student._zipcd = rs.getString("ZIPCD");
                        student._addr1 = rs.getString("ADDR1");
                        student._addr2 = rs.getString("ADDR2");
                        student._telno = rs.getString("TELNO");
                        student._guardName = rs.getString("GUARD_NAME");
                        student._relationship = rs.getString("RELATIONSHIP");
                        student._relationshipName = rs.getString("RELATIONSHIP_NAME");
                        student._guardTelno = rs.getString("GUARD_TELNO");
                        student._guardWorkName = rs.getString("GUARD_WORK_NAME");
                        student._guardWorkTelno = rs.getString("GUARD_WORK_TELNO");
                        student._guardName2 = rs.getString("GUARD_NAME2");
                        student._relationship2 = rs.getString("RELATIONSHIP2");
                        student._relationshipName2 = rs.getString("RELATIONSHIP_NAME2");
                        student._guardTelno2 = rs.getString("GUARD_TELNO2");
                        student._guardWorkName2 = rs.getString("GUARD_WORK_NAME2");
                        student._guardWorkTelno2 = rs.getString("GUARD_WORK_TELNO2");
                        student._hikitori1Name = rs.getString("HIKITORI1_NAME");
                        student._hikitori1Telno = rs.getString("HIKITORI1_TELNO");
                        student._hikitori2Name = rs.getString("HIKITORI2_NAME");
                        student._hikitori2Telno = rs.getString("HIKITORI2_TELNO");
                        student._hikitori3Name = rs.getString("HIKITORI3_NAME");
                        student._hikitori3Telno = rs.getString("HIKITORI3_TELNO");
                        student._kinkyuhinan1Name = rs.getString("KINKYUHINAN1_NAME");
                        student._kinkyuhinan1Addr1 = rs.getString("KINKYUHINAN1_ADDR1");
                        student._kinkyuhinan1Addr2 = rs.getString("KINKYUHINAN1_ADDR2");
                        student._kinkyuhinan1Telno = rs.getString("KINKYUHINAN1_TELNO");
                        student._kinkyuhinan2Name = rs.getString("KINKYUHINAN2_NAME");
                        student._kinkyuhinan2Addr1 = rs.getString("KINKYUHINAN2_ADDR1");
                        student._kinkyuhinan2Addr2 = rs.getString("KINKYUHINAN2_ADDR2");
                        student._kinkyuhinan2Telno = rs.getString("KINKYUHINAN2_TELNO");
                        student._bikoName1 = rs.getString("BIKO_NAME1");
                        student._bikoShozoku1 = rs.getString("BIKO_SHOZOKU1");
                        student._bikoName2 = rs.getString("BIKO_NAME2");
                        student._bikoShozoku2 = rs.getString("BIKO_SHOZOKU2");
                        list.add(student);
                        schregnoMap.put(student._schregno, student);
                    }

                    if (!"1".equals(param._useFamilyDat)) {
                        if (null != rs.getString("RELA_SCHREGNO")) {
                            final Rela rela = new Rela();
                            rela._relaSchregno = rs.getString("RELA_SCHREGNO");
                            rela._relano = rs.getString("RELANO");
                            rela._relaGrade = rs.getString("RELA_GRADE");
                            rela._relaHrClass = rs.getString("RELA_HR_CLASS");
                            rela._relaAttendno = rs.getString("RELA_ATTENDNO");
                            rela._relaGradename1 = rs.getString("RELA_GRADENAME1");
                            rela._relaHrClassName1 = rs.getString("RELA_HR_CLASS_NAME1");
                            rela._relaname = rs.getString("RELANAME");
                            rela._relaStaffname1 = rs.getString("RELA_STAFFNAME1");

                            final Student student = (Student) schregnoMap.get(rs.getString("SCHREGNO"));
                            student._relaList.add(rela);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            if ("1".equals(param._useFamilyDat)) {

                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH T_RELA AS ( ");
                stb.append("     SELECT ");
                stb.append("         FD2.RELANO ");
                stb.append("       , FD2.RELA_SCHREGNO ");
                stb.append("     FROM ");
                stb.append("         SCHREG_BASE_MST T1 ");
                stb.append("         INNER JOIN SCHREG_BASE_DETAIL_MST L2 ON L2.SCHREGNO = T1.SCHREGNO AND L2.BASE_SEQ = '009' ");
                stb.append("         INNER JOIN FAMILY_DAT FD2 ON FD2.FAMILY_NO = L2.BASE_REMARK1 AND FD2.RELA_SCHREGNO <> t1.SCHREGNO "); // 自身以外
                stb.append("     WHERE ");
                stb.append("         T1.SCHREGNO = ? ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T2.RELANO ");
                stb.append("   , REGD.SCHREGNO ");
                stb.append("   , REGD.GRADE ");
                stb.append("   , REGD.HR_CLASS ");
                stb.append("   , REGD.ATTENDNO ");
                stb.append("   , BASE.NAME ");
                stb.append("   , GDAT.GRADE_NAME1 ");
                stb.append("   , REGDH.HR_CLASS_NAME1 ");
                stb.append("   , STF.STAFFNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT REGD ");
                stb.append("     INNER JOIN T_RELA T2 ON T2.RELA_SCHREGNO = REGD.SCHREGNO ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("                                      AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
                stb.append("     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = REGDH.TR_CD1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
                stb.append(" WHERE ");
                stb.append("     REGD.YEAR = '" + param._year + "' ");
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append(" ORDER BY ");
                stb.append("     T2.RELANO ");
                stb.append("   , REGD.GRADE ");
                stb.append("   , REGD.HR_CLASS ");
                stb.append("   , REGD.ATTENDNO ");

                try {
                    ps = db2.prepareStatement(stb.toString());

                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {

                            final Rela rela = new Rela();
                            rela._relaSchregno = rs.getString("SCHREGNO");
                            rela._relano = rs.getString("RELANO");
                            rela._relaGrade = rs.getString("GRADE");
                            rela._relaHrClass = rs.getString("HR_CLASS");
                            rela._relaAttendno = rs.getString("ATTENDNO");
                            rela._relaGradename1 = rs.getString("GRADE_NAME1");
                            rela._relaHrClassName1 = rs.getString("HR_CLASS_NAME1");
                            rela._relaname = rs.getString("NAME");
                            rela._relaStaffname1 = rs.getString("STAFFNAME");

                            student._relaList.add(rela);
                        }
                        DbUtils.closeQuietly(rs);
                    }

                } catch (Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(ps);
                    db2.commit();
                }
            }
            return list;
        }

        public String getPrintHrnameAttendno() {
            final StringBuffer stb = new StringBuffer();
            stb.append(StringUtils.defaultString(_gradeName1) + "年");
            stb.append(StringUtils.defaultString(_hrClassName1) + "組");
            stb.append((NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno)) + "番");
            return stb.toString();
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("       BASE.SCHREGNO ");
            stb.append("     , REGD.GRADE ");
            stb.append("     , REGD.HR_CLASS ");
            stb.append("     , REGD.ATTENDNO ");
            stb.append("     , REGDG.SCHOOL_KIND ");
            stb.append("     , REGDG.GRADE_CD ");
            stb.append("     , REGDG.GRADE_NAME1 ");
            stb.append("     , REGDH.HR_CLASS_NAME1 ");
            stb.append("     , BASE.NAME ");
            stb.append("     , BASE.NAME_KANA ");
            stb.append("     , BASE.BLOODTYPE ");
            stb.append("     , BASE.BLOOD_RH ");
            stb.append("     , BASE.SEX ");
            stb.append("     , Z002.NAME2 AS SEX_NAME ");
            stb.append("     , BASE.BIRTHDAY ");
            stb.append("     , ADDR.ZIPCD ");
            stb.append("     , ADDR.ADDR1 ");
            stb.append("     , ADDR.ADDR2 ");
            stb.append("     , ADDR.TELNO ");
            stb.append("     , GURD.GUARD_NAME ");
            stb.append("     , GURD.RELATIONSHIP ");
            stb.append("     , H201.NAME1 AS RELATIONSHIP_NAME ");
            stb.append("     , GURD.GUARD_TELNO ");
            stb.append("     , GURD.GUARD_WORK_NAME ");
            stb.append("     , GURD.GUARD_WORK_TELNO ");
            stb.append("     , GURD2.GUARD_NAME AS GUARD_NAME2 ");
            stb.append("     , GURD2.RELATIONSHIP AS RELATIONSHIP2 ");
            stb.append("     , H201_2.NAME1 AS RELATIONSHIP_NAME2 ");
            stb.append("     , GURD2.GUARD_TELNO AS GUARD_TELNO2 ");
            stb.append("     , GURD2.GUARD_WORK_NAME AS GUARD_WORK_NAME2 ");
            stb.append("     , GURD2.GUARD_WORK_TELNO AS GUARD_WORK_TELNO2 ");
            stb.append("     , ENVDET1.REMARK1 AS HIKITORI1_NAME ");
            stb.append("     , ENVDET1.REMARK3 AS HIKITORI1_TELNO ");
            stb.append("     , ENVDET2.REMARK1 AS HIKITORI2_NAME ");
            stb.append("     , ENVDET2.REMARK3 AS HIKITORI2_TELNO ");
            stb.append("     , ENVDET3.REMARK1 AS HIKITORI3_NAME ");
            stb.append("     , ENVDET3.REMARK3 AS HIKITORI3_TELNO ");
            stb.append("     , ENVDET4.REMARK1 AS KINKYUHINAN1_NAME ");
            stb.append("     , ENVDET4.REMARK3 AS KINKYUHINAN1_ADDR1 ");
            stb.append("     , ENVDET4.REMARK4 AS KINKYUHINAN1_ADDR2 ");
            stb.append("     , ENVDET4.REMARK5 AS KINKYUHINAN1_TELNO ");
            stb.append("     , ENVDET5.REMARK1 AS KINKYUHINAN2_NAME ");
            stb.append("     , ENVDET5.REMARK3 AS KINKYUHINAN2_ADDR1 ");
            stb.append("     , ENVDET5.REMARK4 AS KINKYUHINAN2_ADDR2 ");
            stb.append("     , ENVDET5.REMARK5 AS KINKYUHINAN2_TELNO ");
            stb.append("     , ENVDET6.REMARK1 AS BIKO_NAME1 ");
            stb.append("     , ENVDET6.REMARK2 AS BIKO_SHOZOKU1 ");
            stb.append("     , ENVDET6.REMARK3 AS BIKO_NAME2 ");
            stb.append("     , ENVDET6.REMARK4 AS BIKO_SHOZOKU2 ");
            if (!"1".equals(param._useFamilyDat)) {
                stb.append("     , RLREGD.SCHREGNO AS RELA_SCHREGNO ");
                stb.append("     , RELA.RELANO ");
                stb.append("     , RLREGD.GRADE AS RELA_GRADE ");
                stb.append("     , RLREGD.HR_CLASS AS RELA_HR_CLASS ");
                stb.append("     , RLREGD.ATTENDNO AS RELA_ATTENDNO ");
                stb.append("     , RLREGDG.GRADE_NAME1 AS RELA_GRADENAME1 ");
                stb.append("     , RLREGDH.HR_CLASS_NAME1 AS RELA_HR_CLASS_NAME1 ");
                stb.append("     , RELA.RELANAME ");
                stb.append("     , RLREGDSTF.STAFFNAME AS RELA_STAFFNAME1 ");
            }
            stb.append(" FROM SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND REGDH.GRADE = REGD.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
            stb.append("         AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("                   FROM SCHREG_ADDRESS_DAT  ");
            stb.append("                   GROUP BY SCHREGNO) ADDR_MAX ON ADDR_MAX.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT ADDR ON ADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
            stb.append("         AND ADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("     LEFT JOIN GUARDIAN_DAT GURD ON GURD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN GUARDIAN2_DAT GURD2 ON GURD2.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET1 ON ENVDET1.SCHREGNO = REGD.SCHREGNO AND ENVDET1.SEQ = '001' ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET2 ON ENVDET2.SCHREGNO = REGD.SCHREGNO AND ENVDET2.SEQ = '002' ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET3 ON ENVDET3.SCHREGNO = REGD.SCHREGNO AND ENVDET3.SEQ = '003' ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET4 ON ENVDET4.SCHREGNO = REGD.SCHREGNO AND ENVDET4.SEQ = '004' ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET5 ON ENVDET5.SCHREGNO = REGD.SCHREGNO AND ENVDET5.SEQ = '005' ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DETAIL_DAT ENVDET6 ON ENVDET6.SCHREGNO = REGD.SCHREGNO AND ENVDET6.SEQ = '006' ");
            stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("         AND Z002.NAMECD2 = BASE.SEX ");
            stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
            stb.append("         AND H201.NAMECD2 = GURD.RELATIONSHIP ");
            stb.append("     LEFT JOIN NAME_MST H201_2 ON H201_2.NAMECD1 = 'H201' ");
            stb.append("         AND H201_2.NAMECD2 = GURD2.RELATIONSHIP ");
            if (!"1".equals(param._useFamilyDat)) {
                stb.append("     LEFT JOIN SCHREG_RELA_DAT RELA ON RELA.SCHREGNO = REGD.SCHREGNO ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT RLREGD ON RLREGD.SCHREGNO = RELA.RELA_SCHREGNO ");
                stb.append("         AND RLREGD.YEAR = REGD.YEAR ");
                stb.append("         AND RLREGD.SEMESTER = REGD.SEMESTER ");
                stb.append("     LEFT JOIN SCHREG_REGD_GDAT RLREGDG ON RLREGDG.YEAR = RLREGD.YEAR ");
                stb.append("         AND RLREGDG.GRADE = RLREGD.GRADE ");
                stb.append("     LEFT JOIN SCHREG_REGD_HDAT RLREGDH ON RLREGDH.YEAR = RLREGD.YEAR ");
                stb.append("         AND RLREGDH.SEMESTER = RLREGD.SEMESTER ");
                stb.append("         AND RLREGDH.GRADE = RLREGD.GRADE ");
                stb.append("         AND RLREGDH.HR_CLASS = RLREGD.HR_CLASS ");
                stb.append("     LEFT JOIN STAFF_MST RLREGDSTF ON RLREGDSTF.STAFFCD = RLREGDH.TR_CD1 ");
            }
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._disp)) {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            } else if ("2".equals(param._disp)) {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._category_selected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("      REGD.GRADE ");
            stb.append("    , REGD.HR_CLASS ");
            stb.append("    , REGD.ATTENDNO ");
            if (!"1".equals(param._useFamilyDat)) {
                stb.append("    , RELA.RELANO ");
            }
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _output; // 1:保護者確認カード 2:引き渡し確認カード
        final String _disp; // 1:個人,2:クラス
        final String[] _category_selected;
        private String _termSdate;
        final String _documentroot;
        final String _useFamilyDat;
//        private String _extension;
        private String _imagepass;
//        private boolean _isSeireki;
        private String _certifSchoolSchoolName;
        private String _certifSchoolSchoolAddress;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark3;
        private String _schoollogoFilePath;

        private final String _useAddrField2;

        private final boolean _rotate_bg;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _output = request.getParameter("OUTPUT");
            _disp = request.getParameter("DISP");
            _category_selected = request.getParameterValues("category_selected");
            for (int i = 0; i < _category_selected.length; i++) {
                _category_selected[i] = StringUtils.split(_category_selected[i], "-")[0];
            }
            _termSdate = StringUtils.defaultString(request.getParameter("TERM_SDATE")).replace('/', '-');
            _documentroot = request.getParameter("DOCUMENTROOT");
            _useFamilyDat = request.getParameter("useFamilyDat");

//            setSeirekiFlg(db2);
            setCertifSchool(db2);
            setControlMst(db2);
            _useAddrField2 = request.getParameter("useAddrField2");
            _schoollogoFilePath = getImageFilePath("SCHOOLLOGO.jpg");
            _rotate_bg = true;
        }

//        private void setSeirekiFlg(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                _isSeireki = false;
//                String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    if ("2".equals(rs.getString("NAME1"))) {
//                        _isSeireki = true; //西暦
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//        }

        private void setCertifSchool(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                _certifSchoolSchoolName = "";
                _certifSchoolSchoolAddress = "";
                _certifSchoolJobName = "";
                _certifSchoolPrincipalName = "";
                String sql = "SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK1, REMARK3 " +
                             "FROM CERTIF_SCHOOL_DAT " +
                             "WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '141' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolSchoolAddress = rs.getString("REMARK1");
                    _certifSchoolJobName = rs.getString("JOB_NAME");
                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolRemark3 = rs.getString("REMARK3");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setControlMst(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
//                _extension = "";
                String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _imagepass = rs.getString("IMAGEPATH");
//                    _extension = rs.getString("EXTENSION");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }


        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepass || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepass).append("/").append(filename);
            final File file = new File(path.toString());
            log.info(" file " + path + " exists? " + file.exists());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}

// eof
