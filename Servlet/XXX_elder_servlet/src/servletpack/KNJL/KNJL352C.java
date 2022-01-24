package servletpack.KNJL;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * 学校・塾別成績一覧表
 *
 */

public class KNJL352C {

    private static final Log log = LogFactory.getLog(KNJL352C.class);
    private Param _param;
    
    private String TESTDIV_ZENKI_KOUKI = "X";
    private String TESTDIV_HENNYU_PLUS_ALPHA = "B";
    private String TESTDIV_ALL = "ALL";
        
    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }
        _param = new Param(db2, request);
        
        String[] testDivPrint = {""};
        if ("9".equals(_param._testDiv)) {
            if ("1".equals(_param._applicantDiv)) {
                testDivPrint = new String[]{"1","2"};
            } else if ("2".equals(_param._applicantDiv)) {
                if (_param.isGojoOnly() && 2 == _param.getSchoolDiv()) {
                    testDivPrint = new String[]{TESTDIV_ALL};
                } else {
                    testDivPrint = new String[]{TESTDIV_HENNYU_PLUS_ALPHA,"5"};
                }
            }
        } else {
            testDivPrint = new String[]{_param._testDiv};
        }

        //  print設定
        response.setContentType("application/pdf");
        
        //  svf設定
        svf.VrInit();                             //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                               //該当データなしフラグ

        //SQL作成
        try {
            for(int i=0; i<testDivPrint.length; i++) {
                String testDiv = testDivPrint[i];
                String sql = preStat1(testDiv);
                log.debug("preStat1 sql="+sql);
                ps1 = db2.prepareStatement(sql);        //受験者一覧preparestatement

                //SVF出力
                if (setSvfMain(db2, svf, ps1, testDiv)) nonedata = true;  //帳票出力のメソッド
            }
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        db2.commit();
        db2.close();                //DBを閉じる
    }//doGetの括り

    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String testDiv
    ) {
        if (!_param._printScore) {
            svf.VrSetForm("KNJL352C_1.frm", 1);
        } else {
            svf.VrSetForm("KNJL352C_3.frm", 1);
        }
        if (TESTDIV_ALL.equals(testDiv)) {
            svf.VrAttribute("TITLE",  "Edit=");
            svf.VrsOut("TITLE", _param.getNendo() + "　" + _param.getNameMst(db2, "L003", _param._applicantDiv) + "　全て　" + StringUtils.defaultString(_param.getSchoolDivName()) + "別一覧表");
        } else {
            svf.VrsOut("DIV", _param.getSchoolDivName());
            svf.VrsOut("NENDO", _param.getNendo());
            svf.VrsOut("APPLICANTDIV", _param.getNameMst(db2, "L003", _param._applicantDiv));// 画面から入試制度
            if (TESTDIV_HENNYU_PLUS_ALPHA.equals(testDiv)) {
                if (_param.isGojo()) {
                    svf.VrsOut("TESTDIV",  "編入・特進文系コース");
                } else {
                    svf.VrsOut("TESTDIV",  "編入・スポーツコース");
                }
            } else if (TESTDIV_ZENKI_KOUKI.equals(testDiv)) { //和歌山中学前期後期
                svf.VrsOut("TESTDIV",  "前期・後期");
            } else {
                svf.VrsOut("TESTDIV",  _param.getNameMst(db2, "L004", testDiv));// 画面から入試区分
            }
        }
        svf.VrsOut("SCHOOLNAME", _param.getSchoolName(db2));// 学校名
        svf.VrsOut("DATE", _param.getDateString());
        
        // テスト科目名取得
        _param.setTestSubclassName(db2, testDiv);
        svfOutSubclassName(svf);
        // 最後のページをセット
        _param.setTotalpageMap(db2, testDiv);
        
        //  ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("TESTDIV","FF=1");
    }

    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "9" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */
    private boolean setSvfMain(
            DB2UDB db2,
            Vrw32alp svf,
            PreparedStatement ps1,
            String testDiv
        ) {
            boolean nonedata = false;

            try {
                if (setSvfout(db2, svf, ps1, testDiv)) nonedata = true; //帳票出力のメソッド
            } catch( Exception ex ) {
                log.error("setSvfMain set error!"+ex);
            }
            return nonedata;
        }
    

    /**帳票出力（受験者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        PreparedStatement ps1,
        String testDiv
    ) {
        boolean nonedata = false;
        List testRecordList = new ArrayList();
        ResultSet rs = null;
        try {
//            int reccnt_man      = 0;    //男レコード数カウント用
//            int reccnt_woman    = 0;    //女レコード数カウント用
//            int reccnt = 0;             //合計レコード数
            rs = ps1.executeQuery();
            String currentExamno = "-";
            Student tr = null;
            while( rs.next() ){
                if (!currentExamno.equals(rs.getString("EXAMNO"))) {
                    currentExamno = rs.getString("EXAMNO");
                    //明細
                    tr = new Student(
                           rs.getString("EXAMNO"),
                           rs.getString("NAME"),
                           rs.getString("SEX_NAME"),
                           rs.getString("FS_CD"),
                           nvlT(rs.getString("FINSCHOOL_NAME")),
                           rs.getString("FINSCHOOL_PREF"),
                           rs.getString("FIN_DISTRICT"),
                           rs.getString("FINSCHOOL_TELNO"),
                           rs.getString("PRISCHOOLCD"),
                           rs.getString("GRP_PRISCHOOLCD"),
                           nvlT(rs.getString("GRP_PRISCHOOL_NAME")),
                           rs.getString("GRP_PRI_DISTRICT"),
                           rs.getString("GRP_PRISCHOOL_TELNO"),
                           nvlT(rs.getString("PRISCHOOL_NAME")),
                           rs.getString("PRISCHOOL_PREF"),
                           rs.getString("PRI_DISTRICT"),
                           rs.getString("PRISCHOOL_TELNO"),
                           rs.getString("TOTAL4"),
                           rs.getString("AVERAGE4"),
                           rs.getString("TOTAL_RANK4"),
                           rs.getString("JUDGE_NAME"),
                           nvlT(rs.getString("REMARK1"))+nvlT(rs.getString("REMARK2")),
                           rs.getString("RECOM_EXAMNO"),
                           rs.getString("OTHER_TESTDIV_NAME"),
                           rs.getString("SHIFT_DESIRE_FLG")
                    );
                    testRecordList.add(tr);
                    
//                    //レコード数カウント
//                    reccnt++;
//                    if (rs.getString("SEX") != null) {
//                        if (rs.getString("SEX").equals("1")) reccnt_man++;
//                        if (rs.getString("SEX").equals("2")) reccnt_woman++;
//                    }
                }
                if (null != rs.getString("TESTSUBCLASSCD") && null !=rs.getString("SCORE"))
                    tr.addTestSubclass(
                            Integer.valueOf(rs.getString("TESTSUBCLASSCD")).intValue(),
                            Integer.valueOf(rs.getString("SCORE")).intValue(),
                            Integer.valueOf(rs.getString("SHOWORDER")).intValue())
                            ;
            }

        } catch( Exception ex ) {
            log.error("setSvfout set error! =",ex);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        if (testRecordList.size() == 0) {
            return nonedata;
        }
        
        final int max = 25;
        final List pageList = getPageList(testRecordList, max);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final Map m = (Map) pageList.get(pi);
            final List testRecordList1 = getMappedList(m, "LIST");
            
            setHeader(db2, svf, testDiv);

            final Student student0 = (Student) testRecordList1.get(0);
            int schoolDiv = _param.getSchoolDiv();
            svf.VrsOut("DISTRICT", student0.getTitleDistrictName(schoolDiv));
            svf.VrsOut("PRI_CD", student0.getTitleSchoolCode(schoolDiv));
            svf.VrsOut("PRI_NAME", student0.getTitleSchoolName(schoolDiv));
            svf.VrsOut("PRI_TELNO", student0.getTitleSchoolTelno(schoolDiv));

            svf.VrsOut("PAGE", (String) m.get("PAGE"));      //現在ページ数
            svf.VrsOut("TOTAL_PAGE", String.valueOf(_param.getTotalpage(student0.getSchoolCode(_param.getSchoolDiv()))));

            for (int gi = 0; gi < testRecordList1.size(); gi++) {
                Student student = (Student) testRecordList1.get(gi);
                final int gyo = gi + 1;

                svf.VrsOutn("NUMBER", gyo, String.valueOf(gyo));
                svf.VrsOutn("EXAMNO", gyo, student._examno);       //受験番号
                svf.VrsOutn(setformatArea("NAME", 20, student._name), gyo, student._name);//名前
                svf.VrsOutn("SEX", gyo, student._sexname);     //性別
                
                svf.VrsOutn("PREF", gyo, student.getPref(_param.getSchoolDiv())); // 都道府県
                svf.VrsOutn("JUDGE", gyo, student._judgeName); // 判定
                svf.VrsOutn(setformatArea("FINSCHOOL", 26, student.getSchoolName(_param.getSchoolDiv())), gyo, student.getSchoolName(_param.getSchoolDiv())); // 出身校名

                String otherTestDivName = null != student._otherTestDivName ? student._otherTestDivName + " " : "";
                String kibou = _param.isGojo() ? "カレッジ併願" : "移行希望";
                if (_param.isCollege()) {
                    kibou = "1".equals(student._shiftDesireFlg) ? "五併" : "2".equals(student._shiftDesireFlg) ? "和併" : "3".equals(student._shiftDesireFlg) ? "五併/和併" : "";
                }
                String shiftDesire = null != student._shiftDesireFlg ? kibou + " " : "";
                String recomExamno = null != student._recomExamno ? student._recomExamno + " " : "";
                String remark1 = null != student._remark ? student._remark : "";
                
                StringBuffer remark = new StringBuffer();
                remark.append(otherTestDivName);
                remark.append(recomExamno);
                remark.append(shiftDesire);
                remark.append(remark1);

                if (!_param._printScore) {
                    svf.VrsOutn(setformatArea("REMARK", 40, remark.toString()), gyo, remark.toString()); // 備考
                } else {
                    svf.VrsOutn(setformatArea("REMARK", 20, remark.toString()), gyo, remark.toString()); // 備考

                    svf.VrsOutn("TOTAL", gyo, student._total4); // 総合
                    
                    for(int i=1; i<=5; i++) {
                        TestScore ts = student.getTestScore(i);
                        if (null == ts) continue;
                        svf.VrsOutn("SCORE"+i, gyo, ""+ts.getTestScore());
                        //log.debug("SCORE"+i+" : "+ts.getTestScore());
                    }
                }
            }
            svf.VrEndPage();
            nonedata = true;
        }

        return nonedata;
    }
    
    private List getPageList(final List testRecordList, final int max) {
        final List pageList = new ArrayList();
        Map current = null;
        int page = 0;
        Student oldStudent = null;
        for (final Iterator it = testRecordList.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            final boolean pageOver = null != current && getMappedList(current, "LIST").size() >= max;
            if (null == current || isNewPage(student, oldStudent) || pageOver) {
                if (null == current || isNewPage(student, oldStudent)) {
                    page = 0;
                }
                page += 1;
                current = new HashMap();
                current.put("PAGE", String.valueOf(page));
                pageList.add(current);
            }
            getMappedList(current, "LIST").add(student);
            oldStudent = student;
        }
        return pageList;
    }
    
    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /** 科目名を表示する */
    private void svfOutSubclassName(final Vrw32alp svf) {
        TestSubclassName[] testSubclassName = _param._testSubclassName;
        
        for (int i = 0; i < testSubclassName.length; i++) {
            svf.VrsOut("SUBCLASS" + testSubclassName[i]._showOrder, testSubclassName[i]._testSubclassName);
        }
    }
    
    /** 学校コードが違うときtrueを返す */
    private boolean isNewPage(final Student student, final Student oldStudent) {
        if(oldStudent.getSchoolCode(_param.getSchoolDiv()) == null)
            return false;
        String code =student.getSchoolCode(_param.getSchoolDiv()); 
        if (code==null)
            return false;
        return !code.equals(oldStudent.getSchoolCode(_param.getSchoolDiv()));
    }

    

    /** 学校別/塾別 一覧を取得**/
    private String preStat1(final String testDiv)
    {
        final StringBuffer stb = new StringBuffer();
        try {
            final String schoolCd ;
            if(1 == _param.getSchoolDiv()) {
                schoolCd = " t1.FS_CD ";
            } else if(2 == _param.getSchoolDiv()) {
                schoolCd = " t1.PRISCHOOLCD ";
            } else if (3 ==_param.getSchoolDiv()) {
                schoolCd = " t5.GRP_PRISCHOOLCD ";
            } else {
                schoolCd = "";
            }
            stb.append(" select  ");
            stb.append("     t1.EXAMNO, ");
            stb.append("     t1.NAME, ");
            stb.append("     t1.SEX, ");
            stb.append("     t2.ABBV1 AS SEX_NAME, ");
            stb.append("     t1.FS_CD, ");
            stb.append("     t3.FINSCHOOL_NAME, ");
            stb.append("     t4.PREF AS FINSCHOOL_PREF, ");
            stb.append("     t10.NAME1 AS FIN_DISTRICT, ");
            stb.append("     t3.FINSCHOOL_TELNO, ");
            stb.append("     t1.PRISCHOOLCD, ");
            stb.append("     t5.GRP_PRISCHOOLCD, ");
            stb.append("     CASE WHEN t5.GRP_PRISCHOOLCD = t1.PRISCHOOLCD THEN 1 ELSE 2 END AS GRP_SORT, ");
            if (3 == _param.getSchoolDiv()) {
                stb.append("     g5.PRISCHOOL_NAME AS GRP_PRISCHOOL_NAME, ");
                stb.append("     g11.NAME1 AS GRP_PRI_DISTRICT, ");
                stb.append("     g5.PRISCHOOL_TELNO AS GRP_PRISCHOOL_TELNO, ");
            } else {
                stb.append("     '' AS GRP_PRISCHOOL_NAME, ");
                stb.append("     '' AS GRP_PRI_DISTRICT, ");
                stb.append("     '' AS GRP_PRISCHOOL_TELNO, ");
            }
            stb.append("     t5.PRISCHOOL_NAME, ");
            stb.append("     t6.PREF AS PRISCHOOL_PREF, ");
            stb.append("     t11.NAME1 AS PRI_DISTRICT, ");
            stb.append("     t5.PRISCHOOL_TELNO, ");
            stb.append("     t7.NAME1 AS JUDGE_NAME, ");
            if (_param.isCollege() && "1".equals(_param._applicantDiv) && "1".equals(testDiv)) { //カレッジ中学A日程
                stb.append("     case when t1.SUB_ORDER = '2' then t8.TOTAL2 else t8.TOTAL4 end AS TOTAL4, "); //受験型　1:Ⅰ型(国算理)、2:Ⅱ型(国算)
            } else {
                stb.append("     t8.TOTAL4 AS TOTAL4, ");
            }
            stb.append("     t8.AVARAGE4 AS AVERAGE4, ");
            stb.append("     t1.REMARK1, ");
            stb.append("     t1.REMARK2, ");
            stb.append("     t8.TOTAL_RANK4, ");
            stb.append("     t9.TESTSUBCLASSCD,  ");
            stb.append("     t9.SCORE, ");
            stb.append("     VALUE(T_SUBCLASSCD.SHOWORDER, 999) AS SHOWORDER, ");
            stb.append("     t1.RECOM_EXAMNO, ");
            stb.append("     t13.ABBV1 AS OTHER_TESTDIV_NAME, ");
            stb.append("     t1.SHIFT_DESIRE_FLG ");
            stb.append(" from ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT t1 ");
            stb.append("     left join NAME_MST t2 on ");
            stb.append("         t2.NAMECD1 = 'Z002' and ");
            stb.append("         t2.NAMECD2 = T1.SEX ");
            stb.append("     left join FINSCHOOL_MST t3 on ");
            stb.append("         t1.FS_CD = t3.FINSCHOOLCD ");
            stb.append("     left join ZIPCD_MST t4 on ");
            stb.append("         t4.NEW_ZIPCD = t3.FINSCHOOL_ZIPCD ");
            stb.append("     left join PRISCHOOL_MST t5 on ");
            stb.append("         t1.PRISCHOOLCD = t5.PRISCHOOLCD ");
            stb.append("     left join ZIPCD_MST t6 on ");
            stb.append("         t6.NEW_ZIPCD = t5.PRISCHOOL_ZIPCD ");
            stb.append("     left join ENTEXAM_RECEPT_DAT t8 on ");
            stb.append("         t8.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
            stb.append("         t8.EXAMNO = t1.EXAMNO and ");
            stb.append("         t8.APPLICANTDIV = t1.APPLICANTDIV and ");
            stb.append("         t8.TESTDIV = t1.TESTDIV ");
            stb.append("     left join NAME_MST t7 on ");
            stb.append("         t7.NAMECD1 = 'L013' and ");
            stb.append("         t7.NAMECD2 = t8.JUDGEDIV ");
            stb.append("     left join ENTEXAM_SCORE_DAT t9 on ");
            stb.append("         t9.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
            stb.append("         t9.APPLICANTDIV = t1.APPLICANTDIV and ");
            stb.append("         t9.TESTDIV = t1.TESTDIV and ");
            stb.append("         t9.RECEPTNO = t8.RECEPTNO ");
            stb.append("     left join NAME_MST t10 on ");
            stb.append("         t10.NAMECD1 = 'Z003' and ");
            stb.append("         t10.NAMECD2 = t3.DISTRICTCD ");
            stb.append("     left join NAME_MST t11 on ");
            stb.append("         t11.NAMECD1 = 'Z003' and ");
            stb.append("         t11.NAMECD2 = t5.DISTRICTCD ");
            stb.append("     left join ENTEXAM_TESTSUBCLASSCD_DAT T_SUBCLASSCD on ");
            stb.append("         T_SUBCLASSCD.ENTEXAMYEAR = t1.ENTEXAMYEAR and ");
            stb.append("         T_SUBCLASSCD.APPLICANTDIV = t1.APPLICANTDIV and ");
            stb.append("         T_SUBCLASSCD.TESTDIV = t1.TESTDIV and ");
            stb.append("         T_SUBCLASSCD.TESTSUBCLASSCD = t9.TESTSUBCLASSCD ");
            stb.append("     left join ENTEXAM_APPLICANTBASE_DAT t12 ON ");
            stb.append("                 t12.ENTEXAMYEAR=T1.ENTEXAMYEAR AND ");
            stb.append("                 t12.EXAMNO=T1.RECOM_EXAMNO AND ");
            stb.append("                 t12.APPLICANTDIV = '1' AND ");
            stb.append("                 t12.TESTDIV <> T1.TESTDIV ");
            stb.append("     left join NAME_MST t13 ON ");
            stb.append("                 t13.NAMECD1 = 'L004' AND ");
            stb.append("                 t13.NAMECD2 = t12.TESTDIV ");
            if (3 == _param.getSchoolDiv()) {
                stb.append("     left join PRISCHOOL_MST g5 on ");
                stb.append("         g5.PRISCHOOLCD = t5.GRP_PRISCHOOLCD ");
                stb.append("     left join NAME_MST g11 on ");
                stb.append("         g11.NAMECD1 = 'Z003' and ");
                stb.append("         g11.NAMECD2 = g5.DISTRICTCD ");
            }
            stb.append(" where ");
            stb.append("     t1.ENTEXAMYEAR = '"+_param._year+"' and ");
            stb.append("     t1.APPLICANTDIV = '"+_param._applicantDiv+"' and ");
            stb.append("     (t9.RECEPTNO IS NULL or t9.RECEPTNO IS NOT NULL and T_SUBCLASSCD.TESTDIV IS NOT NULL) and ");
            if (TESTDIV_ALL.equals(testDiv)) {
                if (_param.isGojo()) {
                    stb.append("     t1.TESTDIV in ('3','4','5','7','8') AND");
                } else {
                    stb.append("     t1.TESTDIV in ('3','4','5') AND");
                }
            } else if (TESTDIV_HENNYU_PLUS_ALPHA.equals(testDiv)) {
                if (_param.isGojo()) {
                    stb.append("     t1.TESTDIV in ('3','4','7') AND");
                } else {
                    stb.append("     t1.TESTDIV in ('3','4') AND");
                }
            } else if (TESTDIV_ZENKI_KOUKI.equals(testDiv)) { //和歌山中学前期後期
                stb.append("     t1.TESTDIV in ('1','2') AND");
            } else {
                stb.append("     t1.TESTDIV = '"+testDiv+"' and ");
            }
            stb.append("     " + schoolCd + "   in "+SQLUtils.whereIn(true, _param._edboardCd));
            stb.append(" order by ");
            stb.append("     " + schoolCd + " , t1.EXAMNO, T_SUBCLASSCD.SHOWORDER ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    /** 学校コード(学校/塾)ごとの最後のページを取得 */
    private String preStatTotalPage(final String testDiv) {
        String schoolCd = "";
        if(1==_param.getSchoolDiv()) {
            schoolCd = " t1.FS_CD ";
        } else if (2==_param.getSchoolDiv()) {
            schoolCd = " t1.PRISCHOOLCD ";
        } else if (3==_param.getSchoolDiv()) {
            schoolCd = " t5.GRP_PRISCHOOLCD ";
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        stb.append("     "+schoolCd+" AS SCHOOL_CD , ");
        stb.append("     (case MOD(count(*), 25) when 0 then count(*)/25 ");
        stb.append("      else count(*)/25+1 ");
        stb.append("      end) AS TOTAL_PAGE ");
        stb.append(" FROM");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT t1");
        stb.append("     left join PRISCHOOL_MST t5 on ");
        stb.append("         t1.PRISCHOOLCD = t5.PRISCHOOLCD ");
        stb.append(" WHERE");
        stb.append("     t1.ENTEXAMYEAR = '"+_param._year+"' AND");
        stb.append("     t1.APPLICANTDIV = '"+_param._applicantDiv+"' AND");
        if (TESTDIV_ALL.equals(testDiv)) {
            if (_param.isGojo()) {
                stb.append("     t1.TESTDIV in ('3','4','5','7','8') AND");
            } else {
                stb.append("     t1.TESTDIV in ('3','4','5') AND");
            }
        } else if (TESTDIV_HENNYU_PLUS_ALPHA.equals(testDiv)) {
            if (_param.isGojo()) {
                stb.append("     t1.TESTDIV in ('3','4','7') AND");
            } else {
                stb.append("     t1.TESTDIV in ('3','4') AND");
            }
        } else if (TESTDIV_ZENKI_KOUKI.equals(testDiv)) { //和歌山中学前期後期
            stb.append("     t1.TESTDIV in ('1','2') AND");
        } else {
            stb.append("     t1.TESTDIV = '"+testDiv+"' AND");
        }
        stb.append("     "+schoolCd+" in "+SQLUtils.whereIn(true, _param._edboardCd)+" ");
        stb.append(" GROUP BY");
        stb.append("     "+schoolCd+" ");
        stb.append(" ORDER BY");
        stb.append("     "+schoolCd+" ");
        return stb.toString();
    }
    
    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String formatSakuseiDate(String cnvDate) {

        String retDate = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd"); 
            //文字列よりDate型へ変換
            Date date1 = format.parse(cnvDate); 
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }

    /**
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name 帳票出力エリア
     * @param area_len      制限バイト数
     * @param sval          値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

        String retAreaName = "";
        // 値がnullの場合はnullが返される
        if (sval == null) {
            return null;
        }
        // 設定値が制限文字超の場合、帳票設定エリアの変更を行う
        if(area_len >= getMS932count(sval)){
            retAreaName = area_name + "1";
        } else {
            retAreaName = area_name + "2";
        }
        return retAreaName;
    }
    
    private int getMS932count(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
        }
        return count;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }
    
    class Student {
        final String _examno;
        final String _name;
        final String _sexname;
        final String _finCd;
        final String _finSchool;
        final String _finPref;
        final String _finDistrict;
        final String _finTelno;
        final String _priCd;
        final String _grpPriCd;
        final String _grpPriSchool;
        final String _grpPriDistrict;
        final String _grpPriTelno;
        final String _priSchool;
        final String _priPref;
        final String _priDistrict;
        final String _priTelno;
        final String _total4;
        final String _average4;
        final String _total_rank4;
        final String _judgeName;
        final String _remark;
        final String _recomExamno;
        final String _otherTestDivName;
        final String _shiftDesireFlg;
        List _testSubclass = new ArrayList();
        public Student(
                String examno,
                String name,
                String sexname,
                String finCd,
                String finSchool,
                String finPref, 
                String finDistrict,
                String finTelno,
                String priCd,
                String grpPriCd,
                String grpPriSchool,
                String grpPriDistrict,
                String grpPriTelno,
                String priSchool,
                String priPref, 
                String priDistrict,
                String priTelno,
                String total4,
                String average4,
                String total_rank4,
                String judgeName,
                String remark,
                String recomExamno,
                String otherTestDivName,
                String shiftDesireFlg){
            _examno = examno;
            _name = name;
            _sexname = sexname;
            _finCd = finCd;
            _finSchool = finSchool;
            _finPref = finPref;
            _finDistrict = finDistrict;
            _finTelno = finTelno;
            _priCd = priCd;
            _grpPriCd = grpPriCd;
            _grpPriSchool = grpPriSchool;
            _grpPriDistrict = grpPriDistrict;
            _grpPriTelno = grpPriTelno;
            _priSchool = priSchool;
            _priPref = priPref;
            _priDistrict = priDistrict;
            _priTelno = priTelno;
            _total4 = total4;
            _total_rank4 = total_rank4;
            _average4 = average4;
            _judgeName = judgeName;
            _remark = remark;
            _recomExamno = recomExamno;
            _otherTestDivName = otherTestDivName;
            _shiftDesireFlg = shiftDesireFlg;
        }
        
        
        public void addTestSubclass(int testSubclassCd, int testScore, int showOrder) {
            _testSubclass.add(new TestScore(testSubclassCd, testScore, showOrder));
        }
        
        public TestScore getTestScore(int showOrder) {
            for(Iterator it=_testSubclass.iterator(); it.hasNext(); ) {
                TestScore ts = (TestScore) it.next();
                if (ts._showOrder == showOrder) {
                    return ts;
                }
            }
            return null;
        }
        
        public String getSchoolCode(int div) {
            if (1 == div)
                return _finCd;
            else if (2 == div)
                return _priCd;
            else if (3 == div)
                return _grpPriCd;
            return "??";
        }

        public String getSchoolName(int schoolDiv) {
            if (1 == schoolDiv)
                return _finSchool;
            else if (2 == schoolDiv)
                return _priSchool;
            else if (3 == schoolDiv)
                return _priSchool;
            return "??";
        }

        public String getDistrictName(int schoolDiv) {
            if (schoolDiv == 1)
                return _finDistrict;
            else if(schoolDiv==2)
                return _priDistrict;
            else if(schoolDiv==3)
                return _priDistrict;
            return "??";
        }

        public String getSchoolTelno(int schoolDiv) {
            if (schoolDiv == 1)
                return _finTelno;
            else if(schoolDiv==2)
                return _priTelno;
            else if(schoolDiv==3)
                return _priTelno;
            return "??";
        }
        
        public String getPref(int schoolDiv) {
            if (1 == schoolDiv)
                return _finPref;
            else if (2 == schoolDiv)
                return _priPref;
            else if (3 == schoolDiv)
                return _priPref;
            return "??";
        }

        public String getTitleSchoolCode(int div) {
            if (1 == div)
                return _finCd;
            else if (2 == div)
                return _priCd;
            else if (3 == div)
                return _grpPriCd;
            return "??";
        }

        public String getTitleSchoolName(int schoolDiv) {
            if (1 == schoolDiv)
                return _finSchool;
            else if (2 == schoolDiv)
                return _priSchool;
            else if (3 == schoolDiv)
                return _grpPriSchool;
            return "??";
        }

        public String getTitleDistrictName(int schoolDiv) {
            if (schoolDiv == 1)
                return _finDistrict;
            else if(schoolDiv==2)
                return _priDistrict;
            else if(schoolDiv==3)
                return _grpPriDistrict;
            return "??";
        }

        public String getTitleSchoolTelno(int schoolDiv) {
            if (schoolDiv == 1)
                return _finTelno;
            else if(schoolDiv==2)
                return _priTelno;
            else if(schoolDiv==3)
                return _grpPriTelno;
            return "??";
        }
    }

    class TestSubclassName implements Comparable{
        final int _showOrder;
        final String _testSubclassName;
        public TestSubclassName(int showOrder, String testSubclassName) {
            _showOrder = showOrder;
            _testSubclassName = testSubclassName;
        }
        public int compareTo(Object o) {
            if (!(o instanceof TestSubclassName))
                return -1;
            TestSubclassName other = (TestSubclassName) o;
            return new Integer(_showOrder).compareTo(new Integer(other._showOrder));
        }
    }

    class TestScore {
        final int _testSubclassCd;
        final private int _testScore;
        final int _showOrder;
        public TestScore(int testSubclassCd, int testScore, int showOrder) {
            _testSubclassCd = testSubclassCd;
            _showOrder = showOrder;
            _testScore = testScore;
        }
        public String getTestScore() {
            return ""+_testScore;
        }
    }
    
    class Param {
        
        final String _year;
        final String _applicantDiv;
        final String _testDiv;
        final String _loginDate;
        final String _printType;
        final boolean _printScore;
        final String[] _edboardCd; 

        private boolean _seirekiFlg;
        private TestSubclassName[] _testSubclassName;
        private String schoolName = null;
        private HashMap _totalpageMap;
        
        private boolean _initializeSchoolName = false;
        private boolean _initializeTestSubclassName = false;
        private final String _z010SchoolCode;
        
        Param(DB2UDB db2, HttpServletRequest request) {
            _year = request.getParameter("YEAR");                        //年度
            _applicantDiv = request.getParameter("APPLICANTDIV");                //入試制度
            _testDiv = request.getParameter("TESTDIV");                      //入試区分
            _loginDate = request.getParameter("LOGIN_DATE");                  // ログイン日付
            _printType = request.getParameter("PRINT_TYPE");                   // 帳票種類(1.学校別一覧表/2.塾別一覧表)
            String printScore = request.getParameter("PRINT_SCORE");
            if (null != printScore && "1".equals(printScore)) {
                _printScore = true;
            } else {
                _printScore = false;
            }
            _edboardCd = request.getParameterValues("CATEGORY_SELECTED");        // 学校または塾コード
            
            // 西暦使用フラグ
            _seirekiFlg = getSeirekiFlg(db2);
            _z010SchoolCode = getSchoolCode(db2);
        }
        
        public String getTotalpage(String schoolCd) {
            return (String) _totalpageMap.get(schoolCd);
        }
        
        // 最後のページを取得
        public void setTotalpageMap(DB2UDB db2, String testDiv) {
            try{
                _totalpageMap = new HashMap();
                String sql = preStatTotalPage(testDiv);
                log.debug("setTotalpageMap sql="+sql);
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    String code = rs.getString("SCHOOL_CD");
                    String totalPage = rs.getString("TOTAL_PAGE");
                    //log.debug("CODE:"+code+" , TOTAL_PAGE:"+totalPage);
                    _totalpageMap.put(code, totalPage);
                }
                rs.close();
                ps.close();
            } catch(SQLException ex) {
                log.debug("setTotalpageMap",ex);
            }
        }
        
        public int getSchoolDiv() {
            return Integer.valueOf(_printType).intValue();
        }

        public String getSchoolDivName() {
            int schoolDiv = getSchoolDiv();
            if (schoolDiv == 1)
                return "学校";
            else if(schoolDiv == 2)
                return "塾";
            else if(schoolDiv == 3)
                return "塾";
            return "??";
        }
        
        String getNendo() {
            return _seirekiFlg ? _year+"年度":
                KNJ_EditDate.h_format_JP_N(_year+"-01-01")+"度";
        }
        
        String getDateString() {
            if (null != _loginDate) {
                return _seirekiFlg ?
                        _loginDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_loginDate):
                            KNJ_EditDate.h_format_JP(_loginDate ) ;        
            }
            return null;
        }
        
        private String getSchoolName(final DB2UDB db2) {
            if (_initializeSchoolName == true) {
                return schoolName;
            }
            _initializeSchoolName = true;
            
            String certifKindCd = null;
            if ("1".equals(_applicantDiv)) certifKindCd = "105";
            if ("2".equals(_applicantDiv)) certifKindCd = "106";
            if (certifKindCd == null) return null;
            
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME ");
            sql.append(" FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '"+_year+"' AND CERTIF_KINDCD = '"+certifKindCd+"' ");
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch(SQLException ex) {
                log.debug(ex);
            }
            
            return schoolName;
        }
        
        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if ( rs.next() ) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }
        
        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 ");
            sql.append(" FROM NAME_MST ");
            sql.append(" WHERE NAMECD1 = '"+namecd1+"' AND NAMECD2 = '"+namecd2+"' ");
            String name = null;
            try{
                PreparedStatement ps = db2.prepareStatement(sql.toString());
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                   name = rs.getString("NAME1");
                }
            } catch(SQLException ex) {
                log.debug(ex);
            }
            
            return name;
        }
        
        private void setTestSubclassName(final DB2UDB db2, final String testDiv) {
            
            if(_initializeTestSubclassName == true) {
                return;
            }
            _initializeTestSubclassName = true;
            
            String sql = preSubclassName(testDiv);
            log.debug("setTestSubclassName sql="+sql);
            List testSubclassNameList = new ArrayList();
            try{
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    String showOrder = rs.getString("SHOWORDER");
                    String testSubclassName = rs.getString("TESTSUBCLASSNAME");
                    log.debug(showOrder+","+testSubclassName);
                    testSubclassNameList.add(new TestSubclassName(
                            Integer.valueOf(showOrder).intValue(),
                            testSubclassName));
                }
                _testSubclassName = new TestSubclassName[testSubclassNameList.size()];
                for (int i = 0; i < testSubclassNameList.size(); i++) {
                    _testSubclassName[i] = (TestSubclassName) testSubclassNameList.get(i);
                }
            } catch (Exception ex) {
                log.error("setTotalScore exception=", ex);            
            }
        }
        
        /** 表示する科目名を取得 */
        private String preSubclassName(final String testDiv)
        {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.NAME1 AS TESTSUBCLASSNAME, ");
            stb.append("     SHOWORDER ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT ");
            stb.append("       left join NAME_MST T2 on ");
            stb.append("           T2.NAMECD1 = 'L009' AND ");
            stb.append("           T2.NAMECD2 = TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR='"+_year+"' AND ");
            stb.append("     APPLICANTDIV= '"+_applicantDiv+"' AND ");
            if (TESTDIV_HENNYU_PLUS_ALPHA.equals(testDiv) || TESTDIV_ALL.equals(testDiv)) {
                stb.append("     TESTDIV= '3' ");
            } else if (TESTDIV_ZENKI_KOUKI.equals(testDiv)) { //和歌山中学前期後期
                stb.append("     TESTDIV= '1' ");
            } else {
                stb.append("     TESTDIV= '"+testDiv+"' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SHOWORDER ");
            return stb.toString();
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
        
        boolean isGojoOnly() {
            return "30290053001".equals(_z010SchoolCode);
        }
        
        boolean isGojo() {
            return isGojoOnly() || isCollege();
        }
        
        boolean isWakayama() {
            return "30300049001".equals(_z010SchoolCode);
        }

        boolean isCollege() {
            return "30290086001".equals(_z010SchoolCode);
        }
        
    }
}//クラスの括り
