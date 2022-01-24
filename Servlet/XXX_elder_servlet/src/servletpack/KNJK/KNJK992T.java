// kanji=漢字
/*
 * $Id: 268b94f5948f160186e7ea1ba5ea6f50a4744e0e $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJK;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;


/*
 *  学校教育システム 賢者 [進路情報管理] 高校用調査書
 */
public class KNJK992T {
    private static final Log log = LogFactory.getLog(KNJK992T.class);
    
    private static int OUTPUT_TYOUSASHO_SHINGAKU = 1;
    private static int OUTPUT_TYOUSASHO_SHUSHOKU = 2;
    private static int OUTPUT_SEISEKI_SHOMEISHO_JPN = 3;
    private static int OUTPUT_SEISEKI_SHOMEISHO_ENG = 4;
    private static int OUTPUT_TANNI_SHUTOKU_SHOMEISHO = 5;

    private boolean _hasData;
    
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final Param param = createParam(request);
        
        // ＤＢ接続
        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2); //Databaseクラスを継承したクラス
        try {
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        response.setContentType("application/pdf");

        final Vrw32alp svf = new Vrw32alp();
        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());

        param.load(db2);

        try {
            printMain(db2, svf, param);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            svf.VrQuit();

            db2.commit();
            db2.close();
        }
    }

    private String[] castb(byte[] bytes) {
        final String[] rtn = new String[bytes.length];
        final String tab = "0123456789ABCDEF";
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            int n = (int) b + (b < 0 ? 256 : 0);
            int u = n / 16;
            int l = n % 16;
            rtn[i] = String.valueOf(tab.charAt(u)) + String.valueOf(tab.charAt(l));
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) throws UnsupportedEncodingException {
        final String form = "KNJK992T.frm";
        final int maxLine = 25;
        
        for (int yi = 0; yi < param._yearSelected.length; yi++) {
            final String year = param._yearSelected[yi];

            final String title = KenjaProperties.gengou(Integer.parseInt(year)) + "年度 転退学生及び卒業生索引簿";

            final List pageList = getPageList(getRegdList(db2, year), maxLine);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List regdList = (List) pageList.get(pi);
                
                svf.VrSetForm(form, 4);

                svf.VrsOut("TITLE", title);
                svf.VrsOut("PAGE1", String.valueOf(pi + 1));
                svf.VrsOut("PAGE2", String.valueOf(pageList.size()));

                for (int i = 0; i < regdList.size(); i++) {
                    final Map regd = (Map) regdList.get(i);
                    
                    //log.debug(" (" + pi + ", " + i + ") regd = " + regd);

                    final String grade = KnjDbUtils.getString(regd, "GRADE");
                    final String nameKana = KnjDbUtils.getString(regd, "NAME_KANA");
                    final String hrName = KnjDbUtils.getString(regd, "HR_NAME");
                    final String schregno = KnjDbUtils.getString(regd, "SCHREGNO");
                    final String name = KnjDbUtils.getString(regd, "NAME");

                    svf.VrsOut("HEAD", getHead(param, nameKana));
                    svf.VrsOut("HR_NAME", hrName);
                    svf.VrsOut("SCHREGNO", schregno);
                    svf.VrsOut("NAME", name);

                    final StringBuffer checkedfilename = new StringBuffer();
                    for (int ni = 0; ni < name.length(); ni++) {
                        final String ch = String.valueOf(name.charAt(ni));

//                        if (isConvertTarget(ch)) {
//                            log.info(" 文字化置換 " + schregno + " " + name + " at " + ni);
//                            checkedfilename.append("？");
//                        } else {
                            checkedfilename.append(ch);
//                        }
                    }

                    if (NumberUtils.isDigits(grade) && Integer.parseInt(grade) >= 3) {
                        svf.VrsOut("FOLDER1", getPdfPath(param._kindname1, year + "年度", hrName, schregno, checkedfilename.toString()));
                        svf.VrsOut("FOLDER2", getPdfPath(param._kindname2, year + "年度", hrName, schregno, checkedfilename.toString()));
                    }
                    svf.VrsOut("FOLDER3", getPdfPath(param._kindname3, year + "年度", hrName, schregno, checkedfilename.toString()));
                    svf.VrsOut("FOLDER4", getPdfPath(param._kindname4, year + "年度", hrName, schregno, checkedfilename.toString()));
                    svf.VrsOut("FOLDER5", getPdfPath(param._kindname5, year + "年度", hrName, schregno, checkedfilename.toString()));

                    svf.VrEndRecord();
                }
                
                _hasData = true;
                svf.VrEndPage();
            }
        }
    }

//    private boolean isConvertTarget(String ch) {
//        boolean isTarget = false;
//        try {
//            final String[] _3f = {"3F"};
//            final String[] serverEnc = castb(ch.getBytes());
//            final String[] ms932Enc = castb(ch.getBytes("MS932"));
//            int ms932EncInt = 0;
//            for (int i = 0; i < ms932Enc.length; i++) {
//                ms932EncInt += (16^i) * Integer.parseInt(ms932Enc[i], 16);
//            }
//            final boolean isGaiji = 0xF040 <= ms932EncInt && ms932EncInt < 0xFA30;
//            log.info(" gaiji ? " + ms932EncInt + " / " + Integer.toString(ms932EncInt, 16) + " => " + isGaiji);
//            isTarget = ArrayUtils.isEquals(serverEnc, _3f) || ArrayUtils.isEquals(ms932Enc, _3f);
//        } catch (Exception e) {
//            log.error("exception!", e);
//        }
//        return isTarget;
//    }

    private String getPdfPath(final String kind, final String nendo, final String hrName, final String schregno, final String name) {
        return kind + nendo + "\\" + StringUtils.defaultString(hrName) + "\\" + schregno + " " + name;
    }

    private String getHead(final Param param, String nameKana) {
        if (null == nameKana || nameKana.length() < 1) {
            return null;
        }

        final String conv = (String) param._initialConvMap.get(nameKana.substring(0, 1));
        if (null != conv) {
            return conv;
        }
        return nameKana.substring(0, 1);
    }

    private List getRegdList(final DB2UDB db2, final String year) {
        String sql = "";
        sql += " SELECT T1.GRADE, T1.HR_CLASS, HDAT.HR_NAME, BASE.NAME, BASE.NAME_KANA, T1.SCHREGNO, T2.SEMESTER ";
        sql += " FROM GRD_REGD_DAT T1 ";
        
        sql += " INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM GRD_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        sql += "     AND T2.YEAR = T1.YEAR ";
        sql += "     AND T2.SEMESTER = T1.SEMESTER ";
        
        sql += " INNER JOIN GRD_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ";
        sql += "     AND HDAT.SEMESTER = T1.SEMESTER ";
        sql += "     AND HDAT.GRADE = T1.GRADE ";
        sql += "     AND HDAT.HR_CLASS = T1.HR_CLASS ";

        sql += " INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ";

        sql += " WHERE FISCALYEAR(BASE.GRD_DATE) = '" + year + "' AND T1.YEAR = '" + year + "' ";
        
        sql += " ORDER BY BASE.NAME_KANA, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";
        
        log.debug(" sql = " + sql);

        List rtn = KnjDbUtils.query(db2, sql);
        return rtn;
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
    
    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 57559 $ $Date: 2017-12-18 20:26:51 +0900 (月, 18 12 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private static class Param {
        final String[] _yearSelected;
        final Map _initialConvMap;

        String _kindname1;
        String _kindname2;
        String _kindname3;
        String _kindname4;
        String _kindname5;
        
        final String _DOCUMENTROOT;

        private boolean _isDebug = false;

        public Param(final HttpServletRequest request) {

            _yearSelected = request.getParameterValues("YEAR_SELECTED");
            
            _kindname1 = "調査書進学用";
            _kindname2 = "調査書就職用";
            _kindname3 = "成績証明書和文";
            _kindname4 = "成績証明書英文";
            _kindname5 = "単位修得証明書";
            
            _DOCUMENTROOT = request.getParameter("DOCUMENTROOT");

            _initialConvMap = new HashMap();
            _initialConvMap.put("が", "か");
            _initialConvMap.put("ぎ", "き");
            _initialConvMap.put("ぐ", "く");
            _initialConvMap.put("げ", "け");
            _initialConvMap.put("ご", "こ");
            _initialConvMap.put("ざ", "さ");
            _initialConvMap.put("じ", "し");
            _initialConvMap.put("ず", "す");
            _initialConvMap.put("ぜ", "せ");
            _initialConvMap.put("ぞ", "そ");
            _initialConvMap.put("だ", "た");
            _initialConvMap.put("ぢ", "ち");
            _initialConvMap.put("づ", "つ");
            _initialConvMap.put("で", "て");
            _initialConvMap.put("ど", "と");
            _initialConvMap.put("ば", "は");
            _initialConvMap.put("び", "ひ");
            _initialConvMap.put("ぶ", "ふ");
            _initialConvMap.put("べ", "へ");
            _initialConvMap.put("ぼ", "ほ");
            _initialConvMap.put("ぱ", "は");
            _initialConvMap.put("ぴ", "ひ");
            _initialConvMap.put("ぷ", "ふ");
            _initialConvMap.put("ぺ", "へ");
            _initialConvMap.put("ぽ", "ほ");
            _initialConvMap.put("ゃ", "や");
            _initialConvMap.put("ゅ", "ゆ");
            _initialConvMap.put("ょ", "よ");

        }
        
        public void load(final DB2UDB db2) {
            _isDebug = "1".equals(getDbPrginfoProperties(db2, "debug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJK992T' AND NAME = '" + propName + "' "));
        }
    }
}
