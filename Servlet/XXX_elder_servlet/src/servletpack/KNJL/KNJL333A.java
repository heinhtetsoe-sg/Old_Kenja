// kanji=漢字
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id:$
 */
public class KNJL333A {

    private static final Log log = LogFactory.getLog("KNJL333A.class");

    private final String JUDGE_PASS = "1";
    private final String JUDGE_UNPASS = "0";

    private final String SCHOOLKIND_J = "J";
    private final String SCHOOLKIND_H = "H";

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

            if("1".equals(_param._docType)) {
                printKoutsuKikan(db2, svf);
            } else if("2".equals(_param._docType)) {
                printShinkensya(db2, svf);
            } else if("3".equals(_param._docType)) {
                printKojinHoukoku(db2, svf);
            } else if("4".equals(_param._docType)) {
                printJuryouSyo(db2, svf);
            } else if("5".equals(_param._docType)) {
                printSoufuNegai(db2, svf);
            }
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

    //1:交通機関利用届
    private void printKoutsuKikan(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map printMap = getKoutsuKikanMap(db2); //入学者Map
        if(printMap.size() == 0) {
            return;
        }

        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final  String key = (String)ite.next();
            final KoutsuKikan printData = (KoutsuKikan)printMap.get(key);
            svf.VrSetForm("KNJL333A_1.frm", 1);
            svf.VrsOut("EXAM_NO", printData._receptno);
            svf.VrsOut("KANA", printData._name_Kana);
            final String sex = "1".equals(printData._sex) ? "男" : "女";
            svf.VrsOut("NAME", printData._name + "　(" + sex + ")");
            if(printData._birthday != null) {
                final String date[] = KNJ_EditDate.tate_format4(db2, printData._birthday) ;
                final String wareki = date[0] + date[1] + "年 " + date[2] + "月" + date[3] + "日生";
                svf.VrsOut("BIRTHDAY", wareki);
            }
            svf.VrsOut("ZIP_NO", "〒" + printData._zipcd);
            final String addr2 = StringUtils.defaultString(printData._address2, "");
            final String addrData = StringUtils.defaultString(printData._address1, "") + ("".equals(addr2) ? "" : "　" + addr2);
            final int addrLen = KNJ_EditEdit.getMS932ByteLength(addrData);
            String addrfield = addrLen > 100 ? "ADDR5" : addrLen > 80 ? "ADDR4" : addrLen > 60 ? "ADDR3" : addrLen > 54 ? "ADDR2" : "ADDR1";
            svf.VrsOut(addrfield, addrData);
            svf.VrsOut("TEL_NO", printData._telno);

            svf.VrEndPage();
        }

         _hasData = true;
    }

    //2:親権者登録 高校のみ
    private void printShinkensya(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        if(!"2".equals(_param._applicantDiv)) {
            log.error("高校のみ印刷");
            return;
        }

        final Map printMap = getShinkensyaMap(db2); //入学者Map
        if(printMap.size() == 0) {
            return;
        }

        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final  String key = (String)ite.next();
            final Shinkensya printData = (Shinkensya)printMap.get(key);
            svf.VrSetForm("KNJL333A_2.frm", 1);
            svf.VrsOut("EXAM_NO", printData._receptno);
            svf.VrsOut("KANA", printData._name_Kana);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("ZIP_NO", "〒" + printData._zipcd);
            final String addr2 = StringUtils.defaultString(printData._address2, "");
            final String addrData = StringUtils.defaultString(printData._address1, "") + ("".equals(addr2) ? "" : "　" + addr2);
            final int addrLen = KNJ_EditEdit.getMS932ByteLength(addrData);
            String addrfield = addrLen > 100 ? "ADDR5" : addrLen > 80 ? "ADDR4" : addrLen > 60 ? "ADDR3" : addrLen > 54 ? "ADDR2" : "ADDR1";
            svf.VrsOut(addrfield, addrData);
            svf.VrsOut("TEL_NO", printData._telno);

            svf.VrEndPage();
        }

         _hasData = true;
    }

    //3:個人報告書の受領書
    private void printKojinHoukoku(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map printMap = getKojinHoukokuMap(db2); //学校Map
        if(printMap.size() == 0) {
            return;
        }
        final String gengou = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._examYear + "' AND NAMECD1 = 'L007' AND NAMECD2 = '" + _param._printEraCd + "' "));
        final String kindCd = "1".equals(_param._applicantDiv) ? "105" : "106";
        final String principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '" + kindCd + "'"));
        final String schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '" + kindCd + "'"));

        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            svf.VrSetForm("KNJL333A_3.frm", 1);
            final  String key = (String)ite.next();
            final KojinHoukoku printData = (KojinHoukoku)printMap.get(key);

            svf.VrsOut("ERA_NAME", gengou + _param._printEraY);
            final String type = "3".equals(printData._finschoolType) ? "中学校" : "2".equals(printData._finschoolType) ? "小学校" : "";
            svf.VrsOut("FINSCHOOL_NAME", printData._finschoolName + type + "長 殿");
            svf.VrsOut("SCHOOL_NAME", schoolName);
            svf.VrsOut("STAFF_NAME", "校長　" + principalName);
            if(_param._stampFilePath != null) {
                svf.VrsOut("SCHOOLSTAMP",  _param._stampFilePath); //校長印
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    //4:指導要録等受領書
    private void printJuryouSyo(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final int maxCnt = 20;
        final Map printMap = getPrintDataMap(db2); //入学者Map
        if(printMap.size() == 0) {
            return;
        }
        final String kindCd = "1".equals(_param._applicantDiv) ? "105" : "106";
        final String principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '" + kindCd + "'"));
        final String schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '" + kindCd + "'"));


        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final  String key = (String)ite.next();
            final PrintData printData = (PrintData)printMap.get(key);
            int cnt = 0; //繰り返し回数
            int num = 0; //合計人数
            for(Iterator iteName = printData._nameMap.keySet().iterator(); iteName.hasNext();) {
                if(cnt > maxCnt || cnt == 0) {
                    if(cnt > maxCnt) svf.VrEndPage();
                    svf.VrSetForm("KNJL333A_5.frm", 4);
                    if("2".equals(_param._applicantDiv)) svf.VrsOut("FINSCHOOL_CD", printData._finschoolCd);
                    final String type = "3".equals(printData._finschoolType) ? "中学校" : "2".equals(printData._finschoolType) ? "小学校" : "";
                    svf.VrsOut("FINSCHOOL_NAME", printData._finschoolName + type + "長 殿");
                    svf.VrsOut("SCHOOL_NAME", schoolName);
                    svf.VrsOut("STAFF_NAME", "校長　" + principalName);
                    if(_param._stampFilePath != null) {
                        svf.VrsOut("SCHOOLSTAMP", _param._stampFilePath); //校長印
                    }
                    cnt = 1;
                }
                final String keyName = (String)iteName.next();
                final String name = (String)printData._nameMap.get(keyName) + "　様";
                final int keta = KNJ_EditEdit.getMS932ByteLength(name);
                final String field = keta <= 24 ? "1" : keta <= 34 ? "2" : "3";
                svf.VrsOut("NAME" + field, name);
                svf.VrEndRecord();
                num++;
                cnt++;
            }
            svf.VrsOut("NUM", String.valueOf(num));
            svf.VrEndRecord();
            svf.VrEndPage();
        }
        svf.VrEndPage();
         _hasData = true;
    }

    //5:入学に伴う書類送付願い 中学のみ
    private void printSoufuNegai(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final int maxCnt = 10;
        final Map printMap = getPrintDataMap(db2); //入学者Map
        if(printMap.size() == 0) {
            return;
        }
        final String gengou = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _param._ctrlYear + "' AND NAMECD1 = 'L007' AND NAMECD2 = '" + _param._printEraCd + "' "));
        final String principalName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '105'"));
        final String schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '105'"));
        final String zipCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT REMARK2 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '105'"));
        final String addr = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT REMARK4 FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _param._ctrlYear + "' AND CERTIF_KINDCD = '105'"));

        for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
            final  String key = (String)ite.next();
            final PrintData printData = (PrintData)printMap.get(key);
            int cnt = 0; //繰り返し回数
            for(Iterator iteName = printData._nameMap.keySet().iterator(); iteName.hasNext();) {
                if(cnt > maxCnt || cnt == 0) {
                    if(cnt > maxCnt) svf.VrEndPage();
                    svf.VrSetForm("KNJL333A_4.frm", 4);
                    final String eraName = (!"".equals(_param._printEraY)) ? gengou + _param._printEraY : "";
                    svf.VrsOut("ERA_NAME", eraName);
                    svf.VrsOut("MONTH", _param._printEraM);
                    svf.VrsOut("DAY", _param._printEraD);
                    final String type = "3".equals(printData._finschoolType) ? "中学校" : "2".equals(printData._finschoolType) ? "小学校" : "";
                    svf.VrsOut("FINSCHOOL_NAME", printData._finschoolName + type + "長 殿");
                    svf.VrsOut("SCHOOL_NAME", schoolName);
                    svf.VrsOut("STAFF_NAME", "校長　" + principalName);
                    if(_param._stampFilePath != null) {
                        svf.VrsOut("SCHOOLSTAMP", _param._stampFilePath); //校長印
                    }
                    cnt = 1;
                }
                final String keyName = (String)iteName.next();
                final String name = (String)printData._nameMap.get(keyName) + "　様";
                final int keta = KNJ_EditEdit.getMS932ByteLength(name);
                final String field = keta <= 24 ? "1" : keta <= 34 ? "2" : "3";
                svf.VrsOut("NAME" + field, name);
                svf.VrEndRecord();
                cnt++;
            }
            svf.VrsOut("ZIP_NO", "〒" + zipCd);
            svf.VrsOut("ADDR", addr);
            svf.VrsOut("SEND", schoolName + " 事務室");
            svf.VrEndRecord();
            svf.VrEndPage();
        }
        svf.VrEndPage();
         _hasData = true;
    }

    private Map getKoutsuKikanMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   BASE.EXAMNO, ");
            stb.append("   REC.RECEPTNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.NAME_KANA, ");
            stb.append("   BASE.BIRTHDAY, ");
            stb.append("   BASE.PROCEDUREDIV, ");
            stb.append("   BD022.REMARK2 AS JITAI_FLG, ");
            stb.append("   ADDR.ZIPCD, ");
            stb.append("   ADDR.ADDRESS1, ");
            stb.append("   ADDR.ADDRESS2, ");
            stb.append("   ADDR.TELNO, ");
            stb.append("   BASE.SEX ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT REC ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("    ON BASE.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
            stb.append("   AND BASE.APPLICANTDIV = REC.APPLICANTDIV ");
            stb.append("   AND BASE.EXAMNO       = REC.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD022 ");
            stb.append("    ON BD022.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("   AND BD022.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("   AND BD022.EXAMNO       = BASE.EXAMNO ");
            stb.append("   AND BD022.SEQ          = '022' ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTADDR_DAT ADDR ");
            stb.append("    ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("   AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("   AND ADDR.EXAMNO       = BASE.EXAMNO ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
            stb.append("          ON R006.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("         AND R006.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("         AND R006.TESTDIV        = REC.TESTDIV ");
            stb.append("         AND R006.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("         AND R006.RECEPTNO       = REC.RECEPTNO ");
            stb.append("         AND R006.SEQ            = '006' ");
            stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
            stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
            stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
            stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
            stb.append(" WHERE ");
            stb.append("   REC.ENTEXAMYEAR   = '" + _param._examYear + "' ");
            stb.append("   AND REC.APPLICANTDIV  = '" + _param._applicantDiv + "' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("   AND REC.TESTDIV       = '" + _param._testDiv + "' ");
            }

            //受験番号絞り込み
            if(_param._receptNoFrom.length() > 0 && _param._receptNoTo.length() > 0) {
                stb.append("  AND REC.RECEPTNO BETWEEN '" + _param._receptNoFrom + "' AND '" + _param._receptNoTo + "'");
            } else if(_param._receptNoFrom.length() > 0) {
                stb.append("  AND REC.RECEPTNO >= '" + _param._receptNoFrom + "'");
            } else if(_param._receptNoTo.length() > 0) {
                stb.append("  AND REC.RECEPTNO <= '" + _param._receptNoTo + "'");
            }
            //合格者のみ
            stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
            stb.append(" ORDER BY ");
            stb.append("   REC.RECEPTNO ");

            log.debug(" transportation sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String sex = rs.getString("SEX");

                if(!retMap.containsKey(receptno)) {
                    final KoutsuKikan koutsuKikan = new KoutsuKikan(examno, receptno, name, name_Kana, birthday, zipcd, address1, address2, telno, sex);
                    retMap.put(receptno, koutsuKikan);
                }
            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;

    }

    private Map getShinkensyaMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   BASE.EXAMNO, ");
            stb.append("   REC.RECEPTNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.NAME_KANA, ");
            stb.append("   BASE.PROCEDUREDIV, ");
            stb.append("   BD022.REMARK2 AS JITAI_FLG, ");
            stb.append("   ADDR.ZIPCD, ");
            stb.append("   ADDR.ADDRESS1, ");
            stb.append("   ADDR.ADDRESS2, ");
            stb.append("   ADDR.TELNO ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_RECEPT_DAT REC ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("    ON BASE.ENTEXAMYEAR  = REC.ENTEXAMYEAR ");
            stb.append("   AND BASE.APPLICANTDIV = REC.APPLICANTDIV ");
            stb.append("   AND BASE.EXAMNO       = REC.EXAMNO ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT BD022 ");
            stb.append("    ON BD022.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("   AND BD022.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("   AND BD022.EXAMNO       = BASE.EXAMNO ");
            stb.append("   AND BD022.SEQ          = '022' ");
            stb.append(" LEFT JOIN ");
            stb.append("   ENTEXAM_APPLICANTADDR_DAT ADDR ");
            stb.append("    ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("   AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("   AND ADDR.EXAMNO       = BASE.EXAMNO ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
            stb.append("          ON R006.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("         AND R006.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("         AND R006.TESTDIV        = REC.TESTDIV ");
            stb.append("         AND R006.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("         AND R006.RECEPTNO       = REC.RECEPTNO ");
            stb.append("         AND R006.SEQ            = '006' ");
            stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
            stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
            stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
            stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
            stb.append(" WHERE ");
            stb.append("   REC.ENTEXAMYEAR   = '" + _param._examYear + "' ");
            stb.append("   AND REC.APPLICANTDIV  = '" + _param._applicantDiv + "' ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("   AND REC.TESTDIV       = '" + _param._testDiv + "' ");
            }

            //受験番号絞り込み
            if(_param._receptNoFrom.length() > 0 && _param._receptNoTo.length() > 0) {
                stb.append("  AND REC.RECEPTNO BETWEEN '" + _param._receptNoFrom + "' AND '" + _param._receptNoTo + "'");
            } else if(_param._receptNoFrom.length() > 0) {
                stb.append("  AND REC.RECEPTNO >= '" + _param._receptNoFrom + "'");
            } else if(_param._receptNoTo.length() > 0) {
                stb.append("  AND REC.RECEPTNO <= '" + _param._receptNoTo + "'");
            }
            //合格者のみ
            stb.append("     AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
            stb.append(" ORDER BY ");
            stb.append("   REC.RECEPTNO ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String name_Kana = rs.getString("NAME_KANA");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");

                if(!retMap.containsKey(receptno)) {
                    final Shinkensya shinkensya = new Shinkensya(examno, receptno, name, name_Kana, zipcd, address1, address2, telno);
                    retMap.put(receptno, shinkensya);
                }
            }

        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private Map getKojinHoukokuMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   FINSCHOOLCD, ");
            stb.append("   FINSCHOOL_TYPE, ");
            stb.append("   FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("   FINSCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("   FINSCHOOLCD IN " + SQLUtils.whereIn(true, _param._groupSelected));
            stb.append(" ORDER BY ");
            stb.append("   FINSCHOOLCD ");

            log.debug(" kojinHoukoku sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String finschoolCd = rs.getString("FINSCHOOLCD");
                final String finschoolType = rs.getString("FINSCHOOL_TYPE");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");

                if(!retMap.containsKey(finschoolCd)) {
                    final KojinHoukoku kojinHoukou = new KojinHoukoku(finschoolName, finschoolType);
                    retMap.put(finschoolCd, kojinHoukou);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private Map getPrintDataMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PrintData printData = null;

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   BASE.FS_CD, ");
            stb.append("   BASE.EXAMNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   SCHOOL.FINSCHOOL_TYPE, ");
            stb.append("   SCHOOL.FINSCHOOL_NAME ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append(" INNER JOIN FINSCHOOL_MST SCHOOL ");
            stb.append("    ON SCHOOL.FINSCHOOLCD = BASE.FS_CD ");
            stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD022 ");
            stb.append("    ON BD022.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
            stb.append("   AND BD022.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("   AND BD022.EXAMNO       = BASE.EXAMNO ");
            stb.append("   AND BD022.SEQ          = '022' ");

            stb.append(" WHERE ");
            stb.append("   BASE.ENTEXAMYEAR   = '" + _param._examYear + "' ");
            stb.append("   AND BASE.APPLICANTDIV  = '" + _param._applicantDiv + "' ");
            stb.append("   AND SCHOOL.FINSCHOOLCD IN " + SQLUtils.whereIn(true, _param._groupSelected));
            //中学入学に伴う書類送付願い
            if ("1".equals(_param._applicantDiv) && "5".equals(_param._docType)) {
                stb.append("   AND BASE.ENTDIV IS NOT NULL ");
                stb.append("   AND BASE.PROCEDUREDIV = '1' ");
                stb.append("   AND VALUE(BD022.REMARK2,'0') <> '1' ");
            }
            //受領書の場合は入学者が対象
            if ("4".equals(_param._docType)) {
                stb.append("   AND BASE.ENTDIV IS NOT NULL ");
            }
            stb.append("   AND BASE.EXAMNO IN ( ");
            stb.append("     SELECT ");
            stb.append("       REC.EXAMNO ");
            stb.append("     FROM ");
            stb.append("       ENTEXAM_RECEPT_DAT REC ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT R006 ");
            stb.append("              ON R006.ENTEXAMYEAR    = REC.ENTEXAMYEAR ");
            stb.append("             AND R006.APPLICANTDIV   = REC.APPLICANTDIV ");
            stb.append("             AND R006.TESTDIV        = REC.TESTDIV ");
            stb.append("             AND R006.EXAM_TYPE      = REC.EXAM_TYPE ");
            stb.append("             AND R006.RECEPTNO       = REC.RECEPTNO ");
            stb.append("             AND R006.SEQ            = '006' ");
            stb.append("     LEFT JOIN V_NAME_MST L013_1 ");
            stb.append("          ON L013_1.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_1.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_1.NAMECD2  = R006.REMARK8 "); //専願合格コース
            stb.append("     LEFT JOIN V_NAME_MST L013_2 ");
            stb.append("          ON L013_2.YEAR     = R006.ENTEXAMYEAR ");
            stb.append("         AND L013_2.NAMECD1  = 'L" + _param._schoolkind + "13' ");
            stb.append("         AND L013_2.NAMECD2  = R006.REMARK9 "); //併願合格コース
            stb.append("     WHERE ");
            stb.append("           REC.ENTEXAMYEAR   = '" + _param._examYear + "' ");
            stb.append("           AND REC.APPLICANTDIV  = '" + _param._applicantDiv + "' ");
            //合格者のみ
            stb.append("           AND (L013_1.NAMESPARE1 = '" + JUDGE_PASS + "' OR L013_2.NAMESPARE1 = '" + JUDGE_PASS + "') ");
            if(!"ALL".equals(_param._testDiv)) {
                stb.append("        AND REC.TESTDIV       = '" + _param._testDiv + "' ");
            }
            stb.append("    ) ");
            stb.append(" ORDER BY ");
            stb.append("   BASE.FS_CD, BASE.EXAMNO ");

            log.debug(" printData sql =" + stb.toString());

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String finschoolCd = rs.getString("FS_CD");
                final String finschoolType = rs.getString("FINSCHOOL_TYPE");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");

                if(retMap.containsKey(finschoolCd)) {
                    printData = (PrintData)retMap.get(finschoolCd);
                } else {
                    printData = new PrintData(finschoolCd, finschoolName, finschoolType);
                    retMap.put(finschoolCd, printData);
                }

                if(!printData._nameMap.containsKey(examno)){
                    printData._nameMap.put(examno, name);
                }
            }
        } catch (final SQLException e) {
            log.error("志願者の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private class KoutsuKikan {
        final String _examno;
        final String _receptno;
        final String _name;
        final String _name_Kana;
        final String _birthday;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;
        final String _sex;

        public KoutsuKikan(final String examno, final String receptno, final String name, final String name_Kana,final String birthday,
                final String zipcd, final String address1, final String address2, final String telno, final String sex) {
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _name_Kana = name_Kana;
            _birthday = birthday;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
            _sex = sex;
        }
    }

    private class Shinkensya {
        final String _examno;
        final String _receptno;
        final String _name;
        final String _name_Kana;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _telno;

        public Shinkensya(final String examno, final String receptno, final String name, final String name_Kana,
                final String zipcd, final String address1, final String address2, final String telno) {
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _name_Kana = name_Kana;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _telno = telno;
        }
    }

    private class KojinHoukoku {
        final String _finschoolName;
        final String _finschoolType;

        public KojinHoukoku(final String finschoolName, final String finschoolType) {
            _finschoolName = finschoolName;
            _finschoolType = finschoolType;
        }
    }

    private class PrintData {
        final String _finschoolCd;
        final String _finschoolName;
        final String _finschoolType;
        final Map _nameMap;

        public PrintData(final String finschoolCd, final String finschoolName, final String finschoolType) {
            _finschoolCd = finschoolCd;
            _finschoolName = finschoolName;
            _finschoolType = finschoolType;
            _nameMap = new LinkedMap();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id:$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear; //入試年度
        final String _ctrlYear; //ログイン年度
        final String _applicantDiv; //入試制度
        final String _schoolkind; //校種
        final String _testDiv; //入試区分
        final String _date;
        final String _docType; //帳票区分
        final String _receptNoFrom; //受験番号From
        final String _receptNoTo; //受験番号To
        final String _printEraCd; //元号
        final String _printEraY; //年度
        final String _printEraM; //月
        final String _printEraD; //日付
        final String _groupSelected[];
        final String _imagepath;
        final String _documentroot;
        final String _stampFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _schoolkind = ("1".equals(_applicantDiv)) ? SCHOOLKIND_J : SCHOOLKIND_H;
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("LOGIN_DATE");
            _docType = request.getParameter("DOC_TYPE");
            _receptNoFrom = request.getParameter("RECEPTNO_FROM");
            _receptNoTo = request.getParameter("RECEPTNO_TO");
            _printEraCd = request.getParameter("PRINT_ERACD");
            _printEraY = request.getParameter("PRINT_ERAY");
            _printEraM = request.getParameter("PRINT_EMONTH");
            _printEraD = request.getParameter("PRINT_EDAY");
            _groupSelected = request.getParameterValues("GROUP_SELECTED");
            _documentroot = request.getParameter("DOCUMENTROOT");

            if ("3".equals(_docType)|| "4".equals(_docType) || "5".equals(_docType)) {
                KNJ_Control imagepath_extension = new KNJ_Control(); //取得クラスのインスタンス作成
                KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
                _imagepath = returnval.val4; //写真データ格納フォルダ
                if ("1".equals(_applicantDiv)) {
                    _stampFilePath = getImageFilePath("SCHOOLSTAMP_J.bmp");//校長印
                } else {
                    _stampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");//校長印
                }
            } else {
                _imagepath = null;
                _stampFilePath = null;
            }

        }
        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }
    }
}

// eof
