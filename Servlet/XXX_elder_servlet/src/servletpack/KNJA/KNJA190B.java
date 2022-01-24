/*
 * $Id: 861f5efa5478efb19f74fdddab7e930ebdabd35b $
 *
 * 作成日: 2016/03/11
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;

public class KNJA190B {

    private Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    private DB2UDB    db2;                  // Databaseクラスを継承したクラス
    private boolean nonedata;           // 該当データなしフラグ
    private static final Log log = LogFactory.getLog(KNJA190B.class);

    private Param _param;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {

    // print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    // svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);    //PDFファイル名の設定

    // ＤＢ接続
        db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJA190B]DB2 open error!");
        }

        // パラメータの取得
        _param = createParam(db2, request);

        nonedata = false;

        printData();

        //該当データ無しフォーム出力
        if (nonedata == false) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    // 終了処理
        db2.close();        // DBを閉じる
        svf.VrQuit();
        outstrm.close();    // ストリームを閉じる

    }   //doGetの括り

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

    public void printData()
                     throws ServletException, IOException
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH STUDENT AS ( ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._choice)) {
                stb.append("     AND SCHREGNO IN " + _param._schregInState + " ");
            } else {
                stb.append("     AND GRADE || HR_CLASS IN " + _param._schregInState + " ");
            }
            stb.append(" ), ADDR_MAX AS ( ");
            stb.append(" SELECT ");
            stb.append("     ADDR.SCHREGNO, ");
            stb.append("     MAX(ADDR.ISSUEDATE) AS ISSUEDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ADDRESS_DAT ADDR ");
            stb.append(" WHERE ");
            stb.append("     ADDR.ISSUEDATE <= '" + _param._semesterEdate + "' ");
            stb.append("     AND (ADDR.EXPIREDATE IS NULL OR ADDR.EXPIREDATE >= '" + _param._semesterSdate + "') ");
            stb.append("     AND ADDR.SCHREGNO IN ( ");
            stb.append("         SELECT ");
            stb.append("             I1.SCHREGNO ");
            stb.append("         FROM ");
            stb.append("             STUDENT I1 ");
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     ADDR.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGDH.GRADE, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     BUS.BUS_NAME, ");
            stb.append("     ENVIR.JOSYA_2, ");
            stb.append("     Z002.NAME2 AS SEX1, ");
            stb.append("     BASE.EMERGENCYRELA_NAME AS SEX2, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDR1, ");
            stb.append("     ADDR.ADDR2, ");
            stb.append("     ADDR.TELNO, ");
            stb.append("     BASE.EMERGENCYTELNO, ");
            stb.append("     BASE.BIRTHDAY ");
            stb.append(" FROM ");
            stb.append("     STUDENT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGDH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
            stb.append("     LEFT JOIN SCHREG_ENVIR_DAT ENVIR ON REGD.SCHREGNO = ENVIR.SCHREGNO ");
            stb.append("          AND ENVIR.HOWTOCOMMUTECD = '1' ");
            stb.append("          AND ENVIR.FLG_2 = '3' ");
            stb.append("     LEFT JOIN CHILDCARE_BUS_YMST BUS ON REGD.YEAR = BUS.YEAR ");
            stb.append("          AND ENVIR.ROSEN_2 = BUS.COURSE_CD ");
            stb.append("     INNER JOIN ( ");
            stb.append("         SELECT ");
            stb.append("             LADDR.SCHREGNO, ");
            stb.append("             LADDR.ZIPCD, ");
            stb.append("             LADDR.ADDR1, ");
            stb.append("             LADDR.ADDR2, ");
            stb.append("             LADDR.TELNO ");
            stb.append("         FROM ");
            stb.append("             SCHREG_ADDRESS_DAT LADDR, ");
            stb.append("             ADDR_MAX ");
            stb.append("         WHERE ");
            stb.append("             LADDR.SCHREGNO = ADDR_MAX.SCHREGNO ");
            stb.append("             AND LADDR.ISSUEDATE = ADDR_MAX.ISSUEDATE ");
            stb.append("     ) ADDR ON REGD.SCHREGNO = ADDR.SCHREGNO ");
            if ("1".equals(_param._grdDiv)) {
                stb.append(" WHERE ");
                stb.append("     NOT ((BASE.GRD_DIV IS NOT NULL AND BASE.GRD_DIV <> '4') AND BASE.GRD_DATE < '" + _param._ctrlDate + "' ) ");
            }
            stb.append(" ORDER BY ");
            if (_param._outPut2.equals("1")) {
                stb.append("     REGD.SCHREGNO ");
            } else {
                stb.append("     REGD.GRADE, ");
                stb.append("     REGD.HR_CLASS, ");
                stb.append("     REGD.ATTENDNO ");
            }

            log.debug("[KNJA190B]set_detail2 sql=" + stb.toString());
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hr_name = rs.getString("HR_NAME");
                final String bus_name = rs.getString("BUS_NAME");
                final String josya_2 = rs.getString("JOSYA_2");
                final String sex1 = rs.getString("SEX1");
                final String sex2 = rs.getString("SEX2");
                final String name = rs.getString("NAME");
                final String name_kana = rs.getString("NAME_KANA");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String telno = rs.getString("EMERGENCYTELNO");
                final String birthday = rs.getString("BIRTHDAY");

                final Map m = new HashMap();
                list.add(m);
                m.put("GRADE", grade);
                m.put("HR_NAME", hr_name);
                m.put("BUS_NAME", bus_name );
                m.put("JOSYA_2", josya_2);
                m.put("SEX1", sex1);
                m.put("SEX2", sex2);
                m.put("NAME", name);
                m.put("NAME_KANA", name_kana);
                m.put("ZIPCD", zipcd);
                m.put("ADDR1", addr1);
                m.put("ADDR2", addr2);
                m.put("TELNO", telno);
                m.put("BIRTHDAY", birthday);
            }
        } catch (Exception ex) {
            log.error("[KNJA190B]set_detail2 read error!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        printList(list, 2);

    }

    private void printList(final List list, final int flg) {
        try {
            final String frmFile = "KNJA190B.frm";
            svf.VrSetForm(frmFile, 1);

            int ia = Integer.parseInt(_param._poRow);    //行
            final int iaMax = 6;
            int ib = Integer.parseInt(_param._poCol);    //列
            boolean hasData = false;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String grade      = (String) m.get("GRADE");
                final String hr_name    = (String) m.get("HR_NAME");
                final String bus_name   = (String) m.get("BUS_NAME");
                final String josya_2    = (String) m.get("JOSYA_2");
                final String sex1       = (String) m.get("SEX1");
                final String sex2       = (String) m.get("SEX2");
                final String name       = addSpace((String) m.get("NAME"));
                final String name_kana  = addSpace((String) m.get("NAME_KANA"));
                final String zipcd      = (String) m.get("ZIPCD");
                final String addr1      = (String) m.get("ADDR1");
                final String addr2      = (String) m.get("ADDR2");
                final String telno      = (String) m.get("TELNO");
                final String birthday   = (String) m.get("BIRTHDAY");

                if (ib > 3) {
                    ib = 1;
                    ia++;
                    if(ia > iaMax){
                        if (hasData) {
                            svf.VrEndPage();
                            nonedata = true;
                        }
                        ia = 1;
                    }
                }

                svf.VrsOutn("HR_NAME" + ib, ia, hr_name);
                svf.VrsOutn("BUS_COURSE" + ib, ia, bus_name);
                svf.VrsOutn("BUS_STOP_NAME" + ib + (getMS932ByteLength(josya_2) > 20 ? "_2" : "_1"), ia, josya_2);
                if ("K".equals(_param._schoolKindMap.get(grade))) {
                    svf.VrsOutn("SEX" + ib + "_2", ia, sex1);
                } else {
                    svf.VrsOutn("SEX" + ib, ia, sex1);
                }
                svf.VrsOutn("NAME" + ib + (getMS932ByteLength(name) > 30 ? "_2" : "_1"), ia, name);
                svf.VrsOutn("KANA" + ib + (getMS932ByteLength(name_kana) > 30 ? "_2" : "_1"), ia, name_kana);
                if (!StringUtils.isBlank(zipcd)) {
                    svf.VrsOutn("ZIPCODE"    + ib, ia, "〒" + zipcd);
                }

                final int check_len = getMS932ByteLength(addr1);
                final int check_len2 = getMS932ByteLength(addr2);
                if ("1".equals(_param._useAddrField2) && (check_len > 50 || check_len2 > 50)) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_3" , ia, addr1);
                    svf.VrsOutn("ADDRESS" + ib + "_2_3" , ia, addr2);
                } else if (check_len > 40 || check_len2 > 40) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_2" , ia, addr1);
                    svf.VrsOutn("ADDRESS" + ib + "_2_2" , ia, addr2);
                } else if (check_len > 0 || check_len2 > 0) {
                    svf.VrsOutn("ADDRESS" + ib + "_1_1" , ia, addr1);
                    svf.VrsOutn("ADDRESS" + ib + "_2_1" , ia, addr2);
                }
                if (null != sex2 && sex2.length() >= 1) {
                    svf.VrsOutn("GURD_MARK" + ib, ia, sex2.substring(0, 1));
                }
                svf.VrsOutn("TEL_NO" + ib, ia, telno);
                svf.VrsOutn("BIRTHDAY" + ib, ia, KNJ_EditDate.h_format_JP(birthday));

                hasData = true;

                ib++;
            }
            if (hasData) {
                svf.VrEndPage();
                nonedata = true;
            }
        } catch (Exception ex) {
            log.error("[KNJA190B]set_detail2 read error!", ex);
        }

    }

    private String addSpace(final String name) {
        if (null != name) {
            final String zenkaku = "　";
            final String hankaku = " ";
            final String hankaku2 = hankaku + hankaku;
            int idx = name.indexOf(zenkaku);
            if (-1 != idx) {
                return name.substring(0, idx) + zenkaku + name.substring(idx);
            } else {
                idx = name.indexOf(hankaku2);
                if (-1 != idx) {
                    return name.substring(0, idx) + zenkaku + name.substring(idx);
                } else {
                    idx = name.indexOf(hankaku);
                    if (-1 != idx) {
                        return name.substring(0, idx) + zenkaku + name.substring(idx);
                    }
                }
            }
        }
        return name;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _poRow;
        final String _poCol;
        final String _outPut;
        final String _outPut2;
        final String _check;
        final String _ctrlDate;
        final String _grdDiv;
        final String _choice; // 1:個人指定 2:クラス指定
        final String[] _classSelected;
        final String _prgId;
        final String _sama;
        final String _san;
        final String _schregInState;
        final String _semesterSdate;
        final String _semesterEdate;
        final String _useAddrField2;
        final String _hukusikiKirikae;
        final String _tableRegdDat;
        final String _tableRegdHDat;
        final String _useSpecial_Support_Hrclass;
        final String _useFi_Hrclass;
        final Map _schoolKindMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _prgId = request.getParameter("PRGID");
            _outPut = request.getParameter("OUTPUT");
            _outPut2 = request.getParameter("OUTPUT2");
            if ("2".equals(_outPut)){
                _check = request.getParameter("CHECK2");              // 学籍番号印刷
            }else {
                _check = request.getParameter("CHECK1");              // 生徒名印刷
            }
            _ctrlDate = request.getParameter("CTRL_DATE");   // 日付
            _grdDiv = request.getParameter("GRDDIV");   // 出力条件
            _choice = request.getParameter("CHOICE");
            //対象学籍番号の編集
            _classSelected = request.getParameterValues("category_name");   // 学籍番号
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _classSelected.length; ia++){
                if (_classSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                int i = _classSelected[ia].indexOf("-");
                if (-1 < i) {
                    sbx.append(_classSelected[ia].substring(0,i));
                } else {
                    sbx.append(_classSelected[ia]);
                }
                sbx.append("'");
            }
            sbx.append(")");
            _schregInState = sbx.toString();

            KNJ_Semester semester = new KNJ_Semester();                     //クラスのインスタンス作成
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2, _year, _semester);
            _semesterSdate = returnval.val2;                                          //学期開始日
            _semesterEdate = returnval.val3;                                          //学期終了日
            _useAddrField2 = request.getParameter("useAddrField2");
            _hukusikiKirikae = request.getParameter("HUKUSIKI_KIRIKAE");
            _useSpecial_Support_Hrclass = request.getParameter("useSpecial_Support_Hrclass");
            _useFi_Hrclass = request.getParameter("useFi_Hrclass");
            if ("2".equals(_hukusikiKirikae) && "1".equals(_useSpecial_Support_Hrclass)) {
                _tableRegdDat = "SCHREG_REGD_GHR_DAT";
                _tableRegdHDat = "SCHREG_REGD_GHR_HDAT";
            } else if ("2".equals(_hukusikiKirikae) && "1".equals(_useFi_Hrclass)) {
                _tableRegdDat = "SCHREG_REGD_FI_DAT";
                _tableRegdHDat = "SCHREG_REGD_FI_HDAT";
            } else {
                _tableRegdDat = "SCHREG_REGD_DAT";
                _tableRegdHDat = "SCHREG_REGD_HDAT";
            }
            _sama = "様";
            _san = "さん";
            _schoolKindMap = getSchoolKindMap(db2);
        }

        private Map getSchoolKindMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtn = new HashMap();
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        GRADE, SCHOOL_KIND ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn.put(rs.getString("GRADE"), rs.getString("SCHOOL_KIND"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

