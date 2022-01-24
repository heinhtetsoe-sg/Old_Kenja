package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ２１１Ｃ＞  プレテスト申込者チェックリスト
 **/
public class KNJL211C {

    private static final Log log = LogFactory.getLog(KNJL211C.class);
    private Param _param;
    
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        log.fatal("$Revision: 70078 $ $Date: 2019-10-07 21:40:39 +0900 (月, 07 10 2019) $"); // CVSキーワードの取り扱いに注意
        _param = new Param(db2, request);
        
        //  print設定
        response.setContentType("application/pdf");

        //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ
        
        //SQL作成
        try {
            //  svf設定
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

            //SVF出力
            if (setSvfMain(db2, svf)) {
                nonedata = true;
            }
            if (!nonedata) { //  該当データ無し
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("DB2 prepareStatement set error!", ex);
        } finally {
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();
        }

    }//doGetの括り
    
    private static int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }
    
    private String getDateString(final DB2UDB db2, final String date) {
        if (null == date) {
            return null;
        }
        return _param._seirekiFlg ? date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) : KNJ_EditDate.h_format_JP(db2, date) ;        
    }

    /**
     *  svf print 印刷処理
     */
    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf)
    {
        //表紙
        svf.VrSetForm("KNJLCVR002C.frm", 1);
        svf.VrsOut("NENDO", _param.getNendo(db2));
        svf.VrsOut("APPLICANTDIV", _param._applicantdivname);
        svf.VrsOut("PRE_TESTDIV", _param._preTestdivname);
        svf.VrsOut("TITLE", "【" + _param.getTitle() + "】");
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));
        svf.VrEndPage();

        if (_param.isCollege()) {
            svf.VrSetForm("KNJL211C_NARACL.frm", 4);
        } else {
            svf.VrSetForm("KNJL211C.frm", 4);
        }
        svf.VrsOut("NENDO", _param.getNendo(db2));
        svf.VrsOut("TITLE", _param._applicantdivname);
        svf.VrsOut("PRE_TESTDIV", _param._preTestdivname);
        svf.VrsOut("DATE", getDateString(db2, _param._loginDate));
        
        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
        boolean nonedata = false;
        
        final List list = getExamineeList(db2);
        final int LINE_MAX = 25;
        final String pageMax = String.valueOf(list.size() / LINE_MAX + (list.size() % LINE_MAX == 0 ? 0 : 1));
        int c = 0;
        int bususerCountDiv1 = 0;
        int bususerCountDiv2 = 0;
        int bususerCountDiv3 = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final PretestExaminee e = (PretestExaminee) it.next();
            
            c += 1;
            svf.VrsOut("PAGE1", String.valueOf(c / LINE_MAX + (c % LINE_MAX == 0 ? 0 : 1)));
            svf.VrsOut("PAGE2", pageMax);
            
            svf.VrsOut("RECEPTDATE", String.valueOf(c));
            svf.VrsOut("NO", e._preReceptno);
            svf.VrsOut("EXAMCOURSE_NAME1", e._preExamTypeName);
            svf.VrsOut("NAME_KANA" + (getMS932ByteCount(e._namekana) > 50 ? "3" : getMS932ByteCount(e._namekana) > 20 ? "2" : "1"), e._namekana);
            svf.VrsOut("NAME" + (getMS932ByteCount(e._name) > 30 ? "3" : getMS932ByteCount(e._name) > 20 ? "2" : "1"), e._name);
            svf.VrsOut("SEX", e._sexname);
            svf.VrsOut("GURD_NAME_KANA" + (getMS932ByteCount(e._gkana) > 50 ? "3" : getMS932ByteCount(e._gkana) > 20 ? "2" : "1"), e._gkana);
            svf.VrsOut("GURD_NAME" + (getMS932ByteCount(e._gname) > 30 ? "3" : getMS932ByteCount(e._gname) > 20 ? "2" : "1"), e._gname);
            svf.VrsOut("PREFNO", e._zipcd);
            if (getMS932ByteCount(e._address1) > 36 || getMS932ByteCount(e._address2) > 36) {
                svf.VrsOut("ADDRESS1_2", e._address1);
                svf.VrsOut("ADDRESS2_2", e._address2);
            } else {
                svf.VrsOut("ADDRESS1", e._address1);
                svf.VrsOut("ADDRESS2", e._address2);
            }
            svf.VrsOut("TELNO", e._telno);
            svf.VrsOut("FINSCHOOL" + (getMS932ByteCount(e._finschoolName) > 20 ? "2" : "1"), e._finschoolName);
            svf.VrsOut("JUKU" + (getMS932ByteCount(e._prischoolName) > 35 ? "3" : getMS932ByteCount(e._prischoolName) > 20 ? "2" : ""), e._prischoolName);
            svf.VrsOut("CONNECT", "1".equals(e._psContact) ? "○" : "");
            
            svf.VrsOut("NO2", e._recomExamno);

            if ("1".equals(e._busUse)) {
                if ("1".equals(e._stationdiv)) { // 林間田園都市駅
                    svf.VrsOut("BUSUSE2", e._busUserCount);
                    if (NumberUtils.isDigits(e._busUserCount)) {
                        bususerCountDiv1 += Integer.parseInt(e._busUserCount);
                    }
                } else if ("2".equals(e._stationdiv)) { // 福神駅
                    svf.VrsOut("BUSUSE3", e._busUserCount);
                    if (NumberUtils.isDigits(e._busUserCount)) {
                        bususerCountDiv2 += Integer.parseInt(e._busUserCount);
                    }
                } else if ("3".equals(e._stationdiv)) { // JR五条駅
                    svf.VrsOut("BUSUSE1", e._busUserCount);
                    if (NumberUtils.isDigits(e._busUserCount)) {
                        bususerCountDiv3 += Integer.parseInt(e._busUserCount);
                    }
                }
            }
            svf.VrsOut("REMARK", e._remark);

            if (c == list.size()) {
                svf.VrsOut("TOTAL_NAME", "計");
                svf.VrsOut("TOTAL_BUSUSE1", String.valueOf(bususerCountDiv3)); // JR五条駅
                svf.VrsOut("TOTAL_BUSUSE2", String.valueOf(bususerCountDiv1)); // 林間田園都市駅
                svf.VrsOut("TOTAL_BUSUSE3", String.valueOf(bususerCountDiv2)); // 福神駅
            }
            svf.VrEndRecord();
            nonedata = true;
        }
        return nonedata;
    }

    private List getExamineeList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final String sql = sqlEntexamApplicantBasePreDat();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                final String preReceptdate = rs.getString("PRE_RECEPTDATE");
                final String preReceptno = rs.getString("PRE_RECEPTNO");
                final String preExamType = rs.getString("PRE_EXAM_TYPE");
                final String preExamTypeName = rs.getString("PRE_EXAM_TYPE_NAME");
                final String preReceptdiv = rs.getString("PRE_RECEPTDIV");
                final String namekana = rs.getString("NAME_KANA");
                final String name = rs.getString("NAME");
                final String sexname = rs.getString("SEX_NAME");
                final String gkana = rs.getString("GKANA");
                final String gname = rs.getString("GNAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String psContact = rs.getString("PS_CONTACT");
                final String bususe = rs.getString("BUS_USE");
                final String bususercount = rs.getString("BUS_USER_COUNT");
                final String stationdiv = rs.getString("STATIONDIV");
                final String prischoolName = rs.getString("PRISCHOOL_NAME");
                final String recomExamno = rs.getString("RECOM_EXAMNO");
                final String remark = rs.getString("REMARK");
                
                final PretestExaminee examinee = new PretestExaminee(preReceptdate, preReceptno, preExamType, preExamTypeName, preReceptdiv, namekana, name, sexname, gkana, gname, zipcd, address1, address2, telno, finschoolName, psContact, bususe, bususercount, stationdiv, prischoolName, recomExamno, remark);
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
        stb.append("     NML105.NAME1 AS PRE_EXAM_TYPE_NAME, ");
        stb.append("     T1.PRE_RECEPTDIV, ");
        stb.append("     T1.PRE_RECEPTDATE, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.SEX, ");
        stb.append("     NMZ002.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.GNAME, ");
        stb.append("     T1.GKANA, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.ADDRESS1, ");
        stb.append("     T1.ADDRESS2, ");
        stb.append("     T1.TELNO, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     L1.FINSCHOOL_NAME, ");
        stb.append("     T1.PS_CD, ");
        stb.append("     L2.PRISCHOOL_NAME, ");
        stb.append("     T1.PS_CONTACT, ");
        stb.append("     T1.BUS_USE, ");
        stb.append("     T1.STATIONDIV, ");
        stb.append("     CASE WHEN '3' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV3, ");
        stb.append("     CASE WHEN '1' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV1, ");
        stb.append("     CASE WHEN '2' = T1.STATIONDIV THEN 0 ELSE 1 END AS STATIONDIV2, ");
        stb.append("     T1.BUS_USER_COUNT, ");
        stb.append("     T1.RECOM_EXAMNO, ");
        stb.append("     T1.REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ");
        stb.append(" LEFT JOIN FINSCHOOL_MST L1 ON L1.FINSCHOOLCD = T1.FS_CD ");
        stb.append(" LEFT JOIN PRISCHOOL_MST L2 ON L2.PRISCHOOLCD = T1.PS_CD ");
        stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append(" LEFT JOIN NAME_MST NML105 ON NML105.NAMECD1 = 'L105' AND NML105.NAMECD2 = T1.PRE_EXAM_TYPE ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.PRE_TESTDIV = '" + _param._preTestdiv + "' ");
        stb.append(" ORDER BY ");
        for (int i = 0; i < _param._order.size(); i++) {
            stb.append(" " + _param._order.get(i) + ", ");
        }
        stb.append("     T1.PRE_RECEPTNO ");
        return stb.toString();
    }

    private static class PretestExaminee {
        final String _preReceptdate;
        final String _preReceptno;
        final String _preExamType;
        final String _preExamTypeName;
        final String _preReceptdiv;
        final String _namekana;
        final String _name;
        final String _sexname;
        final String _gkana;
        final String _gname;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _finschoolName;
        final String _psContact;
        final String _busUse;
        final String _busUserCount;
        final String _stationdiv;
        final String _prischoolName;
        final String _recomExamno;
        final String _remark;
        PretestExaminee(
                final String preReceptdate,
                final String preReceptno,
                final String preExamType,
                final String preExamTypeName,
                final String preReceptdiv,
                final String namekana,
                final String name,
                final String sexname,
                final String gkana,
                final String gname,
                final String zipcd,
                final String address1,
                final String address2,
                final String telno,
                final String finschoolName,
                final String psContact,
                final String bususe,
                final String bususercount,
                final String stationdiv,
                final String prischoolName,
                final String recomExamno,
                final String remark
        ) {
            _preReceptdate = preReceptdate;
            _preReceptno = preReceptno;
            _preExamType = preExamType;
            _preExamTypeName = preExamTypeName;
            _preReceptdiv = preReceptdiv;
            _namekana = namekana;
            _name = name;
            _sexname = sexname;
            _gkana = gkana;
            _gname = gname;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _finschoolName = finschoolName;
            _psContact = psContact;
            _busUse = bususe;
            _busUserCount = bususercount;
            _stationdiv = stationdiv;
            _prischoolName = prischoolName;
            _recomExamno = recomExamno;
            _remark = remark;
        }
    }

    private static class Param {
        final String _year;
        final String _applicantdiv;
        final String _applicantdivname;
        final String _preTestdiv;
        final String _preTestdivname;
        final String _loginDate;
        final boolean _seirekiFlg;
        final List _order;
        final String _z010SchoolCode;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _applicantdiv = "1";
            _applicantdivname = getNameMst(db2, "L103", _applicantdiv);
            _preTestdiv = request.getParameter("PRE_TESTDIV");
            _preTestdivname = getNameMst(db2, "L104", _preTestdiv);
            _loginDate = request.getParameter("LOGIN_DATE");
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _order = Arrays.asList(request.getParameterValues("CATEGORY_SELECTED"));
            _z010SchoolCode = getSchoolCode(db2);
        }
        
        public String getNendo(final DB2UDB db2) {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度";
        }
        
        private String getTitle() {
            return "申込者名簿";
        }
        
        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        /*
         * 年度と入試制度から学校名を返す
         */
        private String getSchoolName(DB2UDB db2) {
            String certifKindCd = null;
            if ("1".equals(_applicantdiv)) certifKindCd = "105";
            if ("2".equals(_applicantdiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;
            
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            String name = null;
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

        private String getSchoolCode(DB2UDB db2) {
            String schoolCode = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ");
                stb.append(" FROM ");
                stb.append("   NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.NAMECD1 = 'Z010' ");
                stb.append("   AND T1.NAMECD2 = '00' ");
                
                PreparedStatement ps = db2.prepareStatement(stb.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    schoolCode = rs.getString("SCHOOLCODE");
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSchoolCode Exception", e);
            }
            return schoolCode;
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
    }
}//クラスの括り
