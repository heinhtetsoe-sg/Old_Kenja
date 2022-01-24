package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *    学校教育システム 賢者 [SATシステム] SAT発送作業用リスト
 *
 **/

public class KNJL395Q {

    private static final Log log = LogFactory.getLog(KNJL395Q.class);
    
    private boolean _hasData;
    private Param _param;

    private static final String FROM_TO_MARK = "\uFF5E";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }
        
        try {
            log.fatal("$Revision: 69904 $ $Date: 2019-09-27 13:29:28 +0900 (金, 27 9 2019) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            //SVF出力
            for (int i = 0; i < _param._CHECK.length; i++) {
                if (!NumberUtils.isDigits(_param._CHECK[i])) {
                    log.warn(" invalid check:" + _param._CHECK[i]);
                    continue;
                }
                printMain(db2, svf, _param._CHECK[i]); //帳票出力のメソッド
            }

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String check) {
    	
        final String subtitle = (String) _param._titleMap.get(check);
        _param._checkInt = Integer.parseInt((String) _param._titleNoMap.get(check)); 
        log.info(" titleNo = " + _param._checkInt);

        final String form;
        
        final boolean isForm1 = _param._checkInt <= 16 || _param._checkInt == 21 || _param._checkInt == 25 || _param._checkInt == 26 || _param._checkInt == 27 || _param._checkInt == 28;
        final boolean isForm2 = _param._checkInt == 17 || _param._checkInt == 18 || _param._checkInt == 19 || _param._checkInt == 20;
        final boolean isForm3 = _param._checkInt == 22;
        final boolean isForm4 = _param._checkInt == 23;
        final boolean isForm5 = _param._checkInt == 24;
        if (isForm1) {
            form = "KNJL395Q_1.frm";
        } else if (isForm2) {
            form = "KNJL395Q_2.frm";
        } else if (isForm3) {
            form = "KNJL395Q_3.frm";
        } else if (isForm4) {
            form = "KNJL395Q_4.frm";
        } else if (isForm5) {
            form = "KNJL395Q_5.frm";
        } else {
            return;
        }
        log.info(" form = " + form);
        
        final int max = 40;
        
        final String sql = sql(_param);
        log.info(" sql = " + sql);

        final List dataListAll = KnjDbUtils.query(db2, sql);
        final List pageList = getPageList(null, dataListAll, max);
        
        final String title = _param._CTRL_YEAR + "年度実戦模試発送作業用リスト";

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List dataList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 4);

            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("SUBTITLE", subtitle); // サブタイトル
            svf.VrsOut("PAGE1", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("PAGE2", String.valueOf(pageList.size())); // ページ
            svf.VrsOut("APPLY", String.valueOf(dataListAll.size()) + "件"); // 件数
            svf.VrsOut("DATE", null); // 日付
            
            for (int i = 0; i < dataList.size(); i++) {
                final Map row = (Map) dataList.get(i);
                
                svf.VrsOut("NO", KnjDbUtils.getString(row, "NO")); // 連番
                if (isForm1) {
                    svf.VrsOut("EXAM_NO", KnjDbUtils.getString(row, "SAT_NO")); // 受験番号
                    svf.VrsOut("NAME", KnjDbUtils.getString(row, "NAME1")); // 氏名
                    svf.VrsOut("FINSCHOOL_NAME", KnjDbUtils.getString(row, "FINSCHOOL_NAME_ABBV")); // 出身中学名
                    svf.VrsOut("GRADE", KnjDbUtils.getString(row, "GRADE")); // 学年
                    svf.VrsOut("TOTAL_SCORE", KnjDbUtils.getString(row, "SCORE_TOTAL")); // 合計点
                    svf.VrsOut("JUDGE", KnjDbUtils.getString(row, "JUDGE")); // 判定
                    svf.VrsOut("PLACE", KnjDbUtils.getString(row, "PLACEAREA")); // 会場名
                    svf.VrsOut("NOTICE", KnjDbUtils.getString(row, "ABSENCE")); // 欠席
                } else if (isForm2) {
                    svf.VrsOut("EXAM_NO", KnjDbUtils.getString(row, "SAT_NO")); // 受験番号
                    svf.VrsOut("NAME", KnjDbUtils.getString(row, "NAME1")); // 氏名
                    svf.VrsOut("FINSCHOOL_NAME", KnjDbUtils.getString(row, "FINSCHOOL_NAME_ABBV")); // 出身中学名
                    svf.VrsOut("GRADE", KnjDbUtils.getString(row, "GRADE")); // 学年
                    svf.VrsOut("PREF_NAME", KnjDbUtils.getString(row, "PREF_NAME")); // 県名
                    svf.VrsOut("GROUP_NAME", KnjDbUtils.getString(row, "GROUPNAME")); // 団体名
                } else if (isForm3) {
                    svf.VrsOut("EXAM_NO", KnjDbUtils.getString(row, "SAT_NO")); // 受験番号
                    svf.VrsOut("NAME", KnjDbUtils.getString(row, "NAME1")); // 氏名
                    svf.VrsOut("FINSCHOOL_NAME", KnjDbUtils.getString(row, "FINSCHOOL_NAME_ABBV")); // 出身中学名
                    svf.VrsOut("GRADE", KnjDbUtils.getString(row, "GRADE")); // 学年
                    svf.VrsOut("PLACE", KnjDbUtils.getString(row, "PLACEAREA")); // 会場名
                    svf.VrsOut("NOTICE1", KnjDbUtils.getString(row, "ABSENCE_ENGLISH")); // 英語
                    svf.VrsOut("NOTICE2", KnjDbUtils.getString(row, "ABSENCE_MATH")); // 数学
                    svf.VrsOut("NOTICE3", KnjDbUtils.getString(row, "ABSENCE_JAPANESE")); // 国語
                } else if (isForm4) {
                    svf.VrsOut("GROUP_NO", KnjDbUtils.getString(row, "GROUPCD")); // 団体コード
                    svf.VrsOut("GROUP_NAME", KnjDbUtils.getString(row, "GROUPNAME")); // 団体名
                    svf.VrsOut("PREF_NAME", KnjDbUtils.getString(row, "PREF_NAME")); // 都道府県名
                    svf.VrsOut("NUM", KnjDbUtils.getString(row, "CNT")); // 人数
                } else if (isForm5) {
                    svf.VrsOut("GROUP_NO", KnjDbUtils.getString(row, "GROUPCD")); // 団体コード
                    svf.VrsOut("GROUP_NAME", KnjDbUtils.getString(row, "GROUPNAME")); // 団体名
                    svf.VrsOut("PREF_NAME", KnjDbUtils.getString(row, "PREF_NAME")); // 県名
                    svf.VrsOut("HOPE_NUM", KnjDbUtils.getString(row, "ALL_CNT")); // 志願者数
                    svf.VrsOut("EXAM_NUM", KnjDbUtils.getString(row, "TAKE_CNT")); // 受験者数
                    svf.VrsOut("NOTICE_NUM", KnjDbUtils.getString(row, "ABSENCE_CNT")); // 欠席者数
                }

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }
    
    private String sishagonyu(final String numString) {
        if (!NumberUtils.isNumber(numString)) {
            return numString;
        }
        return new BigDecimal(numString).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        /*リストの1から16までのデータ取得*/
        if (param._checkInt <= 16 || param._checkInt == 25 || param._checkInt == 26 || param._checkInt == 27 || param._checkInt == 28) {
            /*データ取る前にデータ件数を別でカウント*/
//        stb.append(" SELECT ");
//        stb.append("     COUNT(*) ");
//        stb.append(" FROM ");
//        stb.append("     ( ");
//        /*************************/
            stb.append("     SELECT ");
            stb.append("         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO, ");                        //通番
            stb.append("         t1.YEAR, ");                                                            //
            stb.append("         t1.SAT_NO, ");                                                          //受験番号
            stb.append("         t1.NAME1, ");                                                           //氏名
            stb.append("         t1.SCHOOLCD, ");                                                        //
            stb.append("         t6.FINSCHOOL_NAME_ABBV, ");                                             //中学校名
            stb.append("         t1.GRADUATION, ");                                                      //
            stb.append("         t3.NAME1 as GRADE, ");                                                  //卒業
            stb.append("         t2.SCORE_TOTAL, ");                                                     //合計
            stb.append("         t2.JUDGE_SAT, ");                                                       //
            stb.append("         t4.NAME1 as JUDGE, ");                                                  //判定
            stb.append("         t1.PLACECD, ");                                                         //
            stb.append("         t5.PLACEAREA, ");                                                       //会場地区名
            stb.append("         case when t1.ABSENCE = '1' then NULL else '欠席' end as ABSENCE ");     //欠席
            stb.append("     FROM ");
            stb.append("         SAT_APP_FORM_MST t1 ");
            stb.append("         left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
            stb.append("         left join FINSCHOOL_MST t6 on t1.SCHOOLCD = t6.FINSCHOOLCD and t6.FINSCHOOL_TYPE = '3' ");
            stb.append("         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ");
            stb.append("         left join NAME_MST t4 on t2.JUDGE_SAT = t4.NAMECD2 and t4.NAMECD1 = 'L200' ");
            stb.append("         left join SAT_EXAM_PLACE_DAT t5 on t1.PLACECD = t5.PLACECD and t1.YEAR = t5.YEAR ");
            if (param._checkInt == 1) {
                /*1.県外会場受験の県内生*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.PREFCD = '19' AND ");
                stb.append("         t1.PLACECD not in ('01','02','03') ");
            } else if (param._checkInt == 2) {
                /*2.県外会場受験の長野県中学校生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t6.FINSCHOOL_PREF_CD = '20' AND ");
                stb.append("     t1.PLACECD not in ('01','02','03') ");
            } else if (param._checkInt == 3) {
                /*3.県内会場受験の長野除く県外生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19','20') AND ");
                stb.append("     t1.PLACECD in ('01','02','03') ");
                
            } else if (param._checkInt == 4) {
                /*4.すべての長野県中学校生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t6.FINSCHOOL_PREF_CD = '20' ");
                
            } else if (param._checkInt == 5) {
                /*5.県内の中2以下*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '19' AND ");
                stb.append("     t1.GRADUATION < '09' ");
                
            } else if (param._checkInt == 6) {
                /*6.県内の特奨生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '19' AND ");
                stb.append("     t1.GRADUATION > '08' AND ");
                stb.append("     t2.JUDGE_SAT = '1' ");
                
            } else if (param._checkInt == 25) {
                /*25.県内の特奨生除くA・準A現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '19' AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT in ('2','3') ");
                
            } else if (param._checkInt == 26) {
                /*26.県内のB〜Dの現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '19' AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT > '3' ");
                
            } else if (param._checkInt == 7) {
                /*7.長野の特奨生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '20' AND ");
                stb.append("     t1.GRADUATION > '08' AND ");
                stb.append("     t2.JUDGE_SAT = '1' ");
                
            } else if (param._checkInt == 8) {
                /*8.長野の特奨生除くA・準A現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '20' AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT in ('2','3') ");
                
            } else if (param._checkInt == 9) {
                /*9.長野のB〜Dの現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '20' AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT > '3' ");
                
            } else if (param._checkInt == 10) {
                /*10.長野除く県外の中2以下*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
                stb.append("     t1.GRADUATION < '09' ");
                
            } else if (param._checkInt == 11) {
                /*11.長野除く県外の特奨生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
                stb.append("     t1.GRADUATION > '08' AND ");
                stb.append("     t2.JUDGE_SAT = '1' ");
                
            } else if (param._checkInt == 12) {
                /*12.長野除く県外の特奨生外A現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT = '2' ");
                
            } else if (param._checkInt == 13) {
                /*13.長野県除く県外のB現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT = '4' ");
                
            } else if (param._checkInt == 14) {
                /*14.長野除く県外のC・Dまたは現役以外*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD not in ('19', '20', '48') AND ");
                stb.append("     (t1.GRADUATION = '99' OR t2.JUDGE_SAT > '4') ");
                
            } else if (param._checkInt == 27) {
                /*27.海外の中2以下*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '48' AND ");
                stb.append("     t1.GRADUATION < '09' ");
                
            } else if (param._checkInt == 15) {
                /*15.海外の特奨生*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '48' AND ");
                stb.append("     t2.JUDGE_SAT = '1' ");
                
            } else if (param._checkInt == 16) {
                /*16.海外の特奨生除くA・B現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '48' AND ");
                stb.append("     t2.JUDGE_SAT in ('2','4') ");

            } else if (param._checkInt == 28) {
                /*28.海外のC・D判定の現役*/
                stb.append(" WHERE ");
                stb.append("     t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("     t1.SCHOOLCD != '2008005' AND ");
                stb.append("     t1.PREFCD = '48' AND ");
                stb.append("     t1.GRADUATION = '09' AND ");
                stb.append("     t2.JUDGE_SAT in ('5','6') ");
            }
            stb.append("     ORDER BY ");
            stb.append("         t1.SAT_NO ");
//            /*************************/
//        /*件数カウント時*/
//        stb.append(" ) ");

        } else if (param._checkInt < 21) {
            /*17から21までのデータ取得**************************************************/
            /*総件数カウント*/
//            stb.append(" SELECT ");
//            stb.append("     COUNT(*) ");
//            stb.append(" FROM ");
//            stb.append("     ( ");
//            /*************/
            stb.append("     SELECT ");
            stb.append("         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO, ");        //通番
            stb.append("         t1.YEAR, ");                                            //
            stb.append("         t1.SAT_NO, ");                                          //受験番号
            stb.append("         t1.NAME1, ");                                           //氏名
            stb.append("         t1.SCHOOLCD, ");                                        //
            stb.append("         t2.FINSCHOOL_NAME_ABBV, ");                             //中学校名
            stb.append("         t1.GRADUATION, ");                                      //
            stb.append("         t3.NAME1 as GRADE, ");                                  //卒業
            stb.append("         t1.PREFCD, ");                                          //
            stb.append("         t4.PREF_NAME, ");                                       //県名
            stb.append("         t1.GROUPCD, ");                                         //
            stb.append("         t5.GROUPNAME, ");                                       //団体名
            stb.append("         t6.SCORE_TOTAL, ");                                     //合計
            stb.append("         t6.JUDGE_SAT, ");                                       //
            stb.append("         t7.NAME1 as JUDGE, ");                                  //判定
            stb.append("         t1.PLACECD, ");                                         //
            stb.append("         t8.PLACEAREA, ");                                       //会場地区名
            stb.append("         case when t1.ABSENCE = '1' then NULL else '欠席' end as ABSENCE ");     //欠席
            stb.append("     FROM ");
            stb.append("         SAT_APP_FORM_MST t1 ");
            stb.append("         left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ");
            stb.append("         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ");
            stb.append("         left join PREF_MST t4 on t1.PREFCD = t4.PREF_CD ");
            stb.append("         left join SAT_GROUP_DAT t5 on t1.YEAR = t5.YEAR and t1.GROUPCD = t5.GROUPCD ");
            stb.append("         left join SAT_EXAM_DAT t6 on t1.YEAR = t6.YEAR and t1.SAT_NO = t6.SAT_NO ");
            stb.append("         left join NAME_MST t7 on t6.JUDGE_SAT = t7.NAMECD2 and t7.NAMECD1 = 'L200' ");
            stb.append("         left join SAT_EXAM_PLACE_DAT t8 on t1.PLACECD = t8.PLACECD and t1.YEAR = t8.YEAR ");
            if (param._checkInt == 17) {
                /*17.県内の欠席者リスト*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.ABSENCE = '0' AND ");
                stb.append("         t1.PREFCD = '19' ");
                
            } else if (param._checkInt == 18) {
                /*18.長野県の欠席者リスト*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.ABSENCE = '0' AND ");
                stb.append("         t1.PREFCD = '20' ");
                
            } else if (param._checkInt == 19) {
                /*19.県外の欠席者リスト*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.ABSENCE = '0' AND ");
                stb.append("         t1.PREFCD not in ('19','20','48') ");
            } else if (param._checkInt == 20) {
                /*20.海外の欠席者リスト*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.ABSENCE = '0' AND ");
                stb.append("         t1.PREFCD = '48' ");
            } else if (param._checkInt == 21) {
                /*21.浪人リスト*/
                stb.append("     WHERE ");
                stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
                stb.append("         t1.SCHOOLCD != '2008005' AND ");
                stb.append("         t1.GRADUATION = '99' ");
            }
            /***********************/
            stb.append("     ORDER BY ");
            stb.append("         t1.SAT_NO ");
//            /*件数カウント時*/
//            stb.append(" ) ");
//            /***********/
        } else if (param._checkInt == 22) {
            /*22.欠科目者リスト*****************************************************/
            /*総件数カウント*/
//            stb.append(" SELECT ");
//            stb.append("     COUNT(*) ");
//            stb.append(" FROM ");
//            stb.append("     ( ");
//            /***********/
            stb.append("     SELECT ");
            stb.append("         ROW_NUMBER() OVER(ORDER BY t1.SAT_NO) as NO, ");                                        //通番
            stb.append("         t1.YEAR, ");                                                                            //
            stb.append("         t1.SAT_NO, ");                                                                          //受験番号
            stb.append("         t1.NAME1, ");                                                                           //氏名
            stb.append("         t1.SCHOOLCD, ");                                                                        //
            stb.append("         t2.FINSCHOOL_NAME_ABBV, ");                                                             //中学校名
            stb.append("         t1.GRADUATION, ");                                                                      //
            stb.append("         t3.NAME1 as GRADE, ");                                                                  //卒業
            stb.append("         t1.PLACECD, ");                                                                         //
            stb.append("         t4.PLACEAREA, ");                                                                       //会場地区名
            stb.append("         case when t5.ABSENCE_ENGLISH = '0' then '欠席' else NULL end as ABSENCE_ENGLISH, ");    //英語
            stb.append("         case when t5.ABSENCE_MATH = '0' then '欠席' else NULL end as ABSENCE_MATH, ");          //数学
            stb.append("         case when t5.ABSENCE_JAPANESE = '0' then '欠席' else NULL end as ABSENCE_JAPANESE ");   //国語
            stb.append("     FROM ");
            stb.append("         SAT_APP_FORM_MST t1 ");
            stb.append("         left join FINSCHOOL_MST t2 on t1.SCHOOLCD = t2.FINSCHOOLCD and t2.FINSCHOOL_TYPE = '3' ");
            stb.append("         left join NAME_MST t3 on t1.GRADUATION = t3.NAMECD2 and t3.NAMECD1 = 'L205' ");
            stb.append("         left join SAT_EXAM_PLACE_DAT t4 on t1.YEAR = t4.YEAR and t1.PLACECD = t4.PLACECD ");
            stb.append("         left join SAT_EXAM_DAT t5 on t1.YEAR = t5.YEAR and t1.SAT_NO = t5.SAT_NO ");
            stb.append("     WHERE ");
            stb.append("         t1.YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("         t1.SCHOOLCD != '2008005' AND ");
            stb.append("         t1.ABSENCE != '0' AND ");
            stb.append("         (t5.ABSENCE_ENGLISH = '0' OR t5.ABSENCE_MATH = '0' OR t5.ABSENCE_JAPANESE = '0') ");
            stb.append("     ORDER BY ");
            stb.append("         t1.SAT_NO ");
//            /*総件数カウント*/
//            stb.append("     ) ");
//            /***********/
            
        } else if (param._checkInt == 23) {
            /*23.県外の特別出願対象生徒を持つ団体**************************************************/
            /*総件数カウント*/
//            stb.append(" SELECT ");
//            stb.append("     COUNT(*) ");
//            stb.append(" FROM ");
//            stb.append("     ( ");
//            /***********/
            stb.append("     SELECT ");
            stb.append("         ROW_NUMBER() OVER(ORDER BY a1.GROUPCD) as NO, ");   //通番
            stb.append("         a1.YEAR, ");                                        //
            stb.append("         a1.GROUPCD, ");                                     //
            stb.append("         a2.GROUPNAME, ");                                   //団体名
            stb.append("         a2.GROUPPREF, ");                                   //
            stb.append("         a3.PREF_NAME, ");                                   //県名
            stb.append("         a1.CNT ");                                          //人数
            stb.append("     FROM ");
            stb.append("         ( ");
            stb.append("         SELECT ");
            stb.append("             YEAR, ");
            stb.append("             GROUPCD, ");
            stb.append("             COUNT(*) as CNT ");
            stb.append("         FROM ");
            stb.append("             ( ");
            stb.append("             SELECT ");
            stb.append("                 t1.YEAR, ");
            stb.append("                 t1.GROUPCD ");
            stb.append("             FROM ");
            stb.append("                 SAT_APP_FORM_MST t1 ");
            stb.append("                 left join SAT_EXAM_DAT t2 on t1.YEAR = t2.YEAR and t1.SAT_NO = t2.SAT_NO ");
            stb.append("             WHERE ");
            stb.append("                 t1.YEAR = '" + _param._CTRL_YEAR + "' ");
            stb.append("             AND ");
            stb.append("                 t2.JUDGE_SAT in ('1','2') ");
            stb.append("             AND ");
            stb.append("                 t1.SCHOOLCD != '2008005' ");
            stb.append("             AND ");
            stb.append("                 t1.PREFCD not in ('19','20', '48') ");
            stb.append("             AND ");
            stb.append("                 t1.GROUPCD IS NOT NULL ");
            stb.append("             AND ");
            stb.append("                 t1.IND_KUBUN != '1' ");
            stb.append("             ) ");
            stb.append("         GROUP BY ");
            stb.append("             YEAR, ");
            stb.append("             GROUPCD ");
            stb.append("         ) a1 ");
            stb.append("         left join SAT_GROUP_DAT a2 on a1.YEAR = a2.YEAR and a1.GROUPCD = a2.GROUPCD ");
            stb.append("         left join PREF_MST a3 on a2.GROUPPREF = a3.PREF_CD ");
            stb.append("     ORDER BY ");
            stb.append("         a1.GROUPCD ");
//            /*総件数カウント*/
//            stb.append("     ) ");
//            /***********/
        } else if (param._checkInt == 24) {
            /*24.出願団体名リスト************************************************************************/
            /*総件数カウント*/
//            stb.append(" SELECT ");
//            stb.append("     COUNT(*) ");
//            stb.append(" FROM ");
//            stb.append("     ( ");
//            /***********/
            stb.append("     SELECT ");
            stb.append("         ROW_NUMBER() OVER(ORDER BY t1.GROUPCD) as NO, ");   //通番
            stb.append("         t1.YEAR, ");                                        //
            stb.append("         t1.GROUPCD, ");                                     //
            stb.append("         t4.GROUPNAME, ");                                   //団体名
            stb.append("         t4.GROUPPREF, ");                                   //
            stb.append("         t5.PREF_NAME, ");                                   //県名
            stb.append("         t1.ALL_CNT, ");                                     //志願者数
            stb.append("         t2.TAKE_CNT, ");                                    //受験者数
            stb.append("         t3.ABSENCE_CNT ");                                  //欠席数
            stb.append("     FROM ");
            stb.append("         (SELECT ");
            stb.append("             YEAR, ");
            stb.append("             GROUPCD, ");
            stb.append("             COUNT(*) as ALL_CNT ");
            stb.append("         FROM ");
            stb.append("             SAT_APP_FORM_MST ");
            stb.append("         WHERE ");
            stb.append("             YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("             GROUPCD IS NOT NULL AND ");
            stb.append("             GROUPCD != '08005' ");
            stb.append("         GROUP BY ");
            stb.append("             YEAR, ");
            stb.append("             GROUPCD ");
            stb.append("         ) t1 ");
            stb.append("         left join (SELECT ");
            stb.append("                         YEAR, ");
            stb.append("                         GROUPCD, ");
            stb.append("                         COUNT(*) as TAKE_CNT ");
            stb.append("                     FROM ");
            stb.append("                         SAT_APP_FORM_MST ");
            stb.append("                     WHERE ");
            stb.append("                         YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("                         GROUPCD IS NOT NULL AND ");
            stb.append("                         GROUPCD != '08005' AND ");
            stb.append("                         ABSENCE != '0' ");
            stb.append("                     GROUP BY ");
            stb.append("                         YEAR, ");
            stb.append("                         GROUPCD ");
            stb.append("                     ) t2 on t1.YEAR = t2.YEAR and t1.GROUPCD = t2.GROUPCD ");
            stb.append("         left join (SELECT ");
            stb.append("                         YEAR, ");
            stb.append("                         GROUPCD, ");
            stb.append("                         COUNT(*) as ABSENCE_CNT ");
            stb.append("                     FROM ");
            stb.append("                         SAT_APP_FORM_MST ");
            stb.append("                     WHERE ");
            stb.append("                         YEAR = '" + _param._CTRL_YEAR + "' AND ");
            stb.append("                         GROUPCD IS NOT NULL AND ");
            stb.append("                         GROUPCD != '08005' AND ");
            stb.append("                         ABSENCE != '1' ");
            stb.append("                     GROUP BY ");
            stb.append("                         YEAR, ");
            stb.append("                         GROUPCD ");
            stb.append("                     ) t3 on t1.YEAR = t3.YEAR and t1.GROUPCD = t3.GROUPCD ");
            stb.append("         left join SAT_GROUP_DAT t4 on t1.YEAR = t4.YEAR and t1.GROUPCD = t4.GROUPCD ");
            stb.append("         left join PREF_MST t5 on t4.GROUPPREF = t5.PREF_CD ");
            stb.append("     ORDER BY ");
            stb.append("         t1.GROUPCD ");
//            /*総件数カウント*/
//            stb.append("     ) ");
//            /***********/
        }
        return stb.toString();
    }
    
    private static String formatDate(final String date) {
        if (null == date) {
            return "";
        }
        final String nen = new SimpleDateFormat("yyyy年").format(Date.valueOf(date));
        return nen + KNJ_EditDate.h_format_JP_MD(date);
    }
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final String groupField, final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String oldGroupVal = null;
        int lineno = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            final boolean isDiffGroup = null != groupField && (null == oldGroupVal && null != KnjDbUtils.getString(row, groupField) || null != oldGroupVal && !oldGroupVal.equals(KnjDbUtils.getString(row, groupField)));
            if (null == current || current.size() >= max || isDiffGroup) {
                current = new ArrayList();
                rtn.add(current);
            }
            if (isDiffGroup) {
                lineno = 0;
            }
            lineno += 1;
            row.put("LINE_NO", String.valueOf(lineno));
            current.add(row);
            if (null != groupField) {
                oldGroupVal = KnjDbUtils.getString(row, groupField);
            }
        }
        return rtn;
    }
    
    private static class Param {
        final String _CTRL_YEAR;
//        final String _CTRL_SEMESTER;
//        final String _CTRL_DATE;

        final Map _titleMap;
        final Map _titleNoMap;
        final String[] _CHECK;
//        final String _CHECK_CNT;
        final String _PRGID;
        final String _cmd;
        int _checkInt;
        private Param(final DB2UDB db2, final HttpServletRequest request) {
            _CTRL_YEAR = request.getParameter("CTRL_YEAR");
//            _CTRL_SEMESTER = request.getParameter("CTRL_SEMESTER");
//            _CTRL_DATE = request.getParameter("CTRL_DATE");
            _CHECK = request.getParameterValues("CHECK[]");
//            _CHECK_CNT = request.getParameter("CHECK_CNT");
            _PRGID = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
            
            _titleMap = new HashMap();
            _titleMap.put("0", "1. 県外会場受験の県内生");
            _titleMap.put("1", "2. 県外会場受験の長野県中学校生");
            _titleMap.put("2", "3. 県内会場受験の長野を除く県外生");
            _titleMap.put("3", "4. すべての長野県中学校生");
            _titleMap.put("4", "5. 県内の中2以下");
            _titleMap.put("5", "6. 県内の特奨生");
            _titleMap.put("6", "7.県内の特奨生除くA・準A現役");
            _titleMap.put("7", "8.県内のB〜Dの現役");
            _titleMap.put("8", "9. 長野の特奨生");
            _titleMap.put("9", "10. 長野の特奨生除くA・準A現役");
            _titleMap.put("10", "11. 長野のB〜Dの現役");
            _titleMap.put("11", "12.長野除く県外の中2以下");
            _titleMap.put("12", "13.長野除く県外の特奨生");
            _titleMap.put("13", "14.長野除く県外の特奨生外A現役");
            _titleMap.put("14", "15.長野除く県外のB現役");
            _titleMap.put("15", "16.長野除く県外のC・Dまたは現役以外");
            _titleMap.put("16", "17.海外の中2以下");
            _titleMap.put("17", "18.海外の特奨生");
            _titleMap.put("18", "19.海外の特奨生除くA・B現役");
            _titleMap.put("19", "20.県内の欠席者リスト");
            _titleMap.put("20", "21.長野県の欠席者リスト");
            _titleMap.put("21", "22.県外の欠席者リスト");
            _titleMap.put("22", "23.海外の欠席者リスト");
            _titleMap.put("23", "24.浪人リスト");
            _titleMap.put("24", "25.欠科目者リスト");
            _titleMap.put("25", "26.県外の特別出願対象生徒を持つ団体名リスト");
            _titleMap.put("26", "27.出願団体名リスト");
            _titleMap.put("27", "28.海外のC・D判定の現役");

            _titleNoMap = new HashMap();
            _titleNoMap.put("0", "1");
            _titleNoMap.put("1", "2");
            _titleNoMap.put("2", "3");
            _titleNoMap.put("3", "4");
            _titleNoMap.put("4", "5");
            _titleNoMap.put("5", "6");
            _titleNoMap.put("6", "25");
            _titleNoMap.put("7", "26");
            _titleNoMap.put("8", "7");
            _titleNoMap.put("9", "8");
            _titleNoMap.put("10", "9");
            _titleNoMap.put("11", "10");
            _titleNoMap.put("12", "11");
            _titleNoMap.put("13", "12");
            _titleNoMap.put("14", "13");
            _titleNoMap.put("15", "14");
            _titleNoMap.put("16", "27");
            _titleNoMap.put("17", "15");
            _titleNoMap.put("18", "16");
            _titleNoMap.put("19", "17");
            _titleNoMap.put("20", "18");
            _titleNoMap.put("21", "19");
            _titleNoMap.put("22", "20");
            _titleNoMap.put("23", "21");
            _titleNoMap.put("24", "22");
            _titleNoMap.put("25", "23");
            _titleNoMap.put("26", "24");
            _titleNoMap.put("27", "28");

        }
    }
}//クラスの括り
