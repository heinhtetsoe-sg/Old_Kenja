// kanji=漢字
/*
 * $Id: abaa45f49e657d4dcbcc0fd2d1914f573b92950d $
 *
 * 作成日: 2020/01/27 19:55:30 - JST
 * 作成者: yogi
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 定期試験度数分布表
 * @author m-yama
 * @version $Id: abaa45f49e657d4dcbcc0fd2d1914f573b92950d $
 */
public class KNJD614B {

    /**
     * 得点データ区切り
     */
    private static final String SCOREDIV1 = "01";  //中間(TESTIK=0101)/期末(TESTIK=9900)試験成績データ
    private static final String SCOREDIV2 = "02";  //平常点
    private static final String SCOREDIV8 = "08";  //学期末試験成績
    private static final String SCOREDIV9 = "09";  //評定

    /**
     * 降順ソート用
     */

    private static final Log log = LogFactory.getLog(KNJD614B.class);

    private static final String FORM_FILE = "KNJD614B.frm";

    private static final String TESTIK_FST = "0101";
    private static final String TESTIK_SCND = "0201";
    private static final String TESTIK_FINAL = "9900";

    Param _param;

    private boolean _hasData = false;
    private static final String TOTAL_ID_SCHREG = "ZZZZZZ";   //合計データの取得時にMapキーとして利用

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);  // パラメータ作成

            // 印字メイン
            _hasData = false;   // 該当データ無しフラグ
            printMain(db2, svf);
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        // 出力データ取得
        final Map subclassesData = getSubclsNameMst(db2);  //科目名称
        final Map schregData = getSchregData(db2);         //生徒データ
        final Map detailData = getDetailData(db2);         //学期別成績データ
        final Map totalData = getTotalData(db2);           //学年末成績データ
        final Map lineTotalMap = getTotalLineData(db2);    //表最下部の合計データ

        for (int clsidx = 0;clsidx < _param._hrClasses.length;clsidx++) {  //クラス
            int pageNo = 1;
            for (Iterator ite = _param._classCdNameMap.keySet().iterator();ite.hasNext();) {
                final String chair_useClassCd = (String)ite.next();
                // データ出力
                if (printOut(db2, svf, chair_useClassCd, _param._hrClasses[clsidx], subclassesData, detailData, totalData, schregData, lineTotalMap, pageNo)) {
                    pageNo++;
                }
            }

        }
        return rtnflg;
    }

    /** 出力処理 */
    private boolean printOut(final DB2UDB db2, final Vrw32alp svf, final String chair_useClassCd, final String hrClass, final Map subclassesData, final Map detailData, final Map totalData, final Map schregData, final Map lineTotalMap, final int pageNo) {

        boolean bret = false;
        svf.VrSetForm(FORM_FILE, 1);
        if (chkLineDataExists(db2, svf, chair_useClassCd, hrClass, subclassesData, detailData, totalData, schregData)) {
            bret = true;
            //ヘッダ出力
            setHead(db2, svf, chair_useClassCd, hrClass, subclassesData, pageNo);

            //表部分の設定
            printLineData(db2, svf, chair_useClassCd, hrClass, subclassesData, detailData, totalData, schregData);
            printTotalLineData(db2, svf, chair_useClassCd, hrClass, subclassesData, lineTotalMap);
            if (_hasData) {
                svf.VrEndPage();
            }
        }
        return bret;
    }

    /** ヘッダデータセット */
    private void setHead(final DB2UDB db2, final Vrw32alp svf, final String chair_useClassCd, final String hrClass, final Map subclassesData, final int pageNo) {
        //この処理の出力は、表の外側のみ
        //ヘッダー部分
        final String MapFstKey = hrClass + "-" + _param._semester + "-" + chair_useClassCd;
        final String MapFstCutKey = (!"".equals(chair_useClassCd)) ? hrClass + "-" + _param._semester + "-" + chair_useClassCd : "";
        svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度 " + _param._semesterName + " 教科原票");
        svf.VrsOut("SCHOOL_KIND", _param._schoolKindName);
        final Map hrInfo = (Map)_param._hrNameMap.get(hrClass);
        final Map subclsInfo = (Map)subclassesData.get(MapFstKey);

        svf.VrsOut("HR_NAME", (String)hrInfo.get("HR_NAME"));  //_hrNameMap
        final Map classdat_row = (Map)_param._classCdNameMap.get(chair_useClassCd);
        svf.VrsOut("CLASS_NAME", (String)classdat_row.get("CLASSNAME"));
        svf.VrsOut("HR_TR_NAME", (String)hrInfo.get("STAFFNAME"));
        if (subclsInfo != null) {
            final String ctName = getCtName(subclsInfo);
            svf.VrsOut("CLASS_TR_NAME", ctName);
        }
        final String dStr = KNJ_EditDate.getAutoFormatDate(db2, _param._date.replace('/', '-'));
        svf.VrsOut("DATE", dStr);
        svf.VrsOut("PAGE", String.valueOf(pageNo));

        //フッター部分
        Map subclsfootInfo = null;
        for (Iterator ite = _param._semesMap.keySet().iterator();ite.hasNext();) {
            final String semester = (String)ite.next();
            final String convsemes = "9".equals(semester) ? "4" : semester;
            subclsfootInfo = null;
            if (_param._assessLevelMap.containsKey(MapFstCutKey)) {
                subclsfootInfo = (Map)_param._assessLevelMap.get(MapFstCutKey);
            } else {
                if (!"".equals(chair_useClassCd)) {
                    final String Map0Key = "000" + "-" + _param._semester + "-" + chair_useClassCd;
                    if (_param._assessLevelMap.containsKey(Map0Key)) {
                        subclsfootInfo = (Map)_param._assessLevelMap.get(Map0Key);
                    } else {
                        final String MapF0Key = "000" + "-" + _param._semester + "-" + "00";
                        if (_param._assessLevelMap.containsKey(MapF0Key)) {
                            subclsfootInfo = (Map)_param._assessLevelMap.get(MapF0Key);
                        }
                    }
                }
            }
            if (subclsfootInfo != null) {
                int cnt = 1;
                for (Iterator itk = subclsfootInfo.keySet().iterator();itk.hasNext();) {
                    final String kStr = (String)itk.next();
                    if (cnt > 5) continue;
                    final Map row = (Map)subclsfootInfo.get(kStr);
                    final String idx = String.valueOf(cnt);
                    if (cnt == 1) {
                        svf.VrsOut("ASSESS"+convsemes+"_"+idx, (String)row.get("ASSESSLOW"));
                    } else {
                        svf.VrsOut("ASSESS"+convsemes+"_"+idx+"_1", (String)row.get("ASSESSHIGH"));
                        svf.VrsOut("ASSESS"+convsemes+"_"+idx+"_2", (String)row.get("ASSESSLOW"));
                    }
                    cnt++;
                }
            }
        }
    }

    private String getCtName(final Map subclsInfo) {
        String retStr = "";
        final Map sortMap = new TreeMap();

        for (Iterator ite = subclsInfo.keySet().iterator();ite.hasNext();) {
            final String kStr = (String)ite.next();
            final SubclsNameMst row = (SubclsNameMst)subclsInfo.get(kStr);
//    		final String s1 = row._st_Num1;
//    		final String s2 = row._st_Num2;
            if (row._staffcd != null && !"".equals(row._staffcd)) {
                sortMap.put(row._staffcd, row);
            }
        }
        if (sortMap.size() > 0) {
            String sep = "";
            final List sroreSorted = new ArrayList(sortMap.keySet());
            Collections.sort(sroreSorted);
            for (Iterator its = sroreSorted.iterator();its.hasNext();) {
                final String kStr = (String)its.next();
                SubclsNameMst row = (SubclsNameMst)sortMap.get(kStr);
                if (row._staffname != null && !"".equals(row._staffname)) {
                    retStr += sep + (String)row._staffname + " 印";
                    sep = " ";
                }
            }
        }

        return retStr;
    }

    private boolean chkLineDataExists(final DB2UDB db2, final Vrw32alp svf, final String chair_useClassCd, final String hrClass, final Map subclassesData, final Map detailData, final Map totalData, final Map schregData) {
        boolean bret = false;
        final Map schregSubMap = (Map)schregData.get(hrClass);
        if (schregSubMap == null) return bret;
        for (Iterator schr = schregSubMap.keySet().iterator();schr.hasNext();) {
            final String schregno = (String)schr.next();
            for (Iterator ite = _param._semesMap.keySet().iterator();ite.hasNext();) {
                final String semester = (String)ite.next();
                if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {
                  continue;
                }
                final String mapFstKey = hrClass + "-" + semester + "-" + chair_useClassCd;
                if (subclassesData.containsKey(mapFstKey)) {
                    final Map subclassInfoMap = (Map)subclassesData.get(mapFstKey);
                    if (subclassInfoMap != null) {
                        bret = true;
                        break;
                    }
                }
            }
            if (bret) {
                break;
            }
        }
        return bret;
    }

    private void printLineData(final DB2UDB db2, final Vrw32alp svf, final String chair_useClassCd, final String hrClass, final Map subclassesData, final Map detailData, final Map totalData, final Map schregData) {
        int prtrow = 1;
        final Map schregSubMap = (Map)schregData.get(hrClass);
        if (schregSubMap == null) return;
        final List prtSubclsCdList = new ArrayList();
        for (Iterator schr = schregSubMap.keySet().iterator();schr.hasNext();) {
            final String schregno = (String)schr.next();
            final SchregData prtwk = (SchregData)schregSubMap.get(schregno);
            svf.VrsOutn("NO", prtrow, String.valueOf(Integer.parseInt(prtwk._attendno)));
            svf.VrsOutn("NAME1", prtrow, prtwk._name);
            for (Iterator ite = _param._semesMap.keySet().iterator();ite.hasNext();) {
                  final String semester = (String)ite.next();
                  if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {  //指定学期以降の出力処理は不要。※指示画面で"学期末"を指定した時に右端の学年末を出力
                    continue;
                  }
                  final String mapFstKey = hrClass + "-" + semester + "-" + chair_useClassCd;
                log.debug("mapFstKey:" + mapFstKey);

                final Map subclassInfoMap = (Map)subclassesData.get(mapFstKey);
                //タイトル部 (学年末は出力なし)
                if (subclassInfoMap != null || "9".equals(semester)) {
                    if (!"9".equals(semester)) {
                        int cnt=1;
                        for (Iterator its = subclassInfoMap.keySet().iterator();its.hasNext();) {
                            final String kStr = (String)its.next();
                            final SubclsNameMst snwk = (SubclsNameMst)subclassInfoMap.get(kStr);
                            if (cnt<=4) {
                                svf.VrsOut("SUBCLASS_NAME" + semester + "_1_" + cnt, snwk._subclassname);      //中間(科目名) ※3学期は期末
                                svf.VrsOut("CREDIT" + semester + "_1_" + cnt, snwk._credits);                  //中間(単位)   ※3学期は期末
                                svf.VrsOut("SUBCLASS_NAME" + semester + "_2_" + cnt, snwk._subclassname);      //期末(科目名) ※3学期は平常点
                                if (Integer.parseInt(semester) < 3) {                                      //3学期には設定フォームが用意されてない箇所
                                    svf.VrsOut("CREDIT" + semester + "_2_" + cnt, snwk._credits);              //期末(単位)
                                    svf.VrsOut("SUBCLASS_NAME" + semester + "_3_" + cnt, snwk._subclassname);  //平常点
                                }
                            }
                            cnt++;
                        }
                    }
                    //データ部
                    final Map detailSubMap = (Map)detailData.get(mapFstKey);
                    final Map totalSubMap =  (Map)totalData.get(mapFstKey);
                    //ここで生徒毎にまとまったMAPを取得
                    final String mapSndKey_1 = schregno + "-" + SCOREDIV1; //中間,期末データ
                    final String mapSndKey_2 = schregno + "-" + SCOREDIV2; //平常点
                    final String mapSndKey_3 = schregno + "-" + SCOREDIV8; //学期末データ
                    final String mapSndKey_4 = schregno + "-" + SCOREDIV9; //到達度(評定)
                    log.debug("mapSndKey_1:" + mapSndKey_1);
                    log.debug("mapSndKey_2:" + mapSndKey_2);
                    log.debug("mapSndKey_3:" + mapSndKey_3);
                    log.debug("mapSndKey_4:" + mapSndKey_4);
                    final Map schregDetailMap1 = getDetailSubMap(detailSubMap, mapSndKey_1);
                    final Map schregDetailMap2 = getDetailSubMap(detailSubMap, mapSndKey_2);
                    final Map schregDetailMap3 = getDetailSubMap(detailSubMap, mapSndKey_3);
                    final Map schregDetailMap4 = getDetailSubMap(detailSubMap, mapSndKey_4);

                    final Map schregTotalMap = getDetailSubMap(totalSubMap, mapSndKey_4);

                    if (subclassInfoMap != null) {
                        int cnt=1;
                        for (Iterator its = subclassInfoMap.keySet().iterator();its.hasNext();) {
                            final String kStr = (String)its.next();
                            final SubclsNameMst subclsInfo = (SubclsNameMst)subclassInfoMap.get(kStr);

                            if (cnt<=4) {
                                if (!prtSubclsCdList.contains(subclsInfo._subclasscd)) {
                                    prtSubclsCdList.add(subclsInfo._subclasscd);
                                }

                                DetailData prtDetail1 = schregDetailMap1 != null ? (DetailData)schregDetailMap1.get(subclsInfo._subclasscd + "-" + TESTIK_FST) : null; //中間
                                DetailData prtDetail2 = schregDetailMap1 != null ? (DetailData)schregDetailMap1.get(subclsInfo._subclasscd + "-" + TESTIK_SCND ) : null; //期末
                                DetailData prtDetail3 = schregDetailMap2 != null ? (DetailData)schregDetailMap2.get(subclsInfo._subclasscd + "-" + TESTIK_FINAL ) : null; //平常点
                                if (!"9".equals(semester)) {
                                    if (!"3".equals(semester)) {
                                        if (prtDetail1 != null) {
                                            VrsOutnScore(svf, "SCORE"+semester+"_1_"+cnt, prtrow, getScoreWithDI(prtDetail1));  //中間
                                        }
                                        if (prtDetail2 != null) {
                                            VrsOutnScore(svf, "SCORE"+semester+"_2_"+cnt, prtrow, getScoreWithDI(prtDetail2));  //期末
                                        }
                                        if (prtDetail3 != null) {
                                            VrsOutnScore(svf, "SCORE"+semester+"_3_"+cnt, prtrow, getScoreWithDI(prtDetail3));  //平常点
                                        }
                                    } else {
                                        if (prtDetail2 != null) {
                                            VrsOutnScore(svf, "SCORE"+semester+"_1_"+cnt, prtrow, getScoreWithDI(prtDetail2));  //期末
                                        }
                                        if (prtDetail3 != null) {
                                            VrsOutnScore(svf, "SCORE"+semester+"_2_"+cnt, prtrow, getScoreWithDI(prtDetail3));  //平常点
                                        }
                                    }
                                }
                                _hasData = true;
                            }
                            cnt++;
                        }
                    }
                    final String sakiSubClsCd = getSakiSubClsCd(prtSubclsCdList);
                    if (!"".equals(sakiSubClsCd)) { //先科目だったら総合結果も出力。教科でしぼった状態なので、先科目は1つのはず。
                        if (!"9".equals(semester)) {
                              DetailData prtDetail3 = schregDetailMap3 != null ? (DetailData)schregDetailMap3.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null; //期末成績,到達度
                               DetailData prtDetail4 = schregDetailMap4 != null ? (DetailData)schregDetailMap4.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null; //期末成績,到達度
                               if (prtDetail3 != null) {
                                   svf.VrsOutn("SCORE"+semester+"_9", prtrow, StringUtils.defaultString(prtDetail3._score));
                               }
                               final String attKey = semester + ":" + schregno + ":" +  sakiSubClsCd;
                               if (_param._attendInf.containsKey(attKey)) {
                                   svf.VrsOutn("KEKKA"+semester, prtrow, (String)_param._attendInf.get(attKey));
                               } else {
                                   svf.VrsOutn("KEKKA"+semester, prtrow, "0");
                               }
                               if (prtDetail4 != null) {
                                   svf.VrsOutn("HOPE"+semester, prtrow, StringUtils.defaultString(prtDetail4._score));
                                   svf.VrsOutn("MANNER"+semester, prtrow, "");
                            }
                        } else {
                            TotalData prtTotal = schregTotalMap != null ? (TotalData)schregTotalMap.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null;
                              if (prtTotal != null) {
                                 svf.VrsOutn("TOTAL_SCORE4", prtrow, StringUtils.defaultString(prtTotal._total_3Gakki));
                                   svf.VrsOutn("SCORE4_9", prtrow, StringUtils.defaultString(prtTotal._kimatu_Score));
                                   svf.VrsOutn("HOPE4", prtrow, StringUtils.defaultString(prtTotal._last_Toutatudo));
                                   svf.VrsOutn("MANNER4", prtrow, StringUtils.defaultString(prtTotal._last_Gakusyutaido));
                                   svf.VrsOutn("REMARK", prtrow, "");
                            }
                               final String attKey = semester + ":" + schregno + ":" +  sakiSubClsCd;
                               if (_param._attendInf.containsKey(attKey)) {
                                   svf.VrsOutn("KEKKA4", prtrow, (String)_param._attendInf.get(attKey));
                               } else {
                                   svf.VrsOutn("KEKKA4", prtrow, "0");
                               }
                        }
                    }
                }
            }
            prtrow++;
        }
    }

    private Map getDetailSubMap(final Map BaseMap, final String keyStr) {
        final Map retMap;
        if (BaseMap != null && BaseMap.containsKey(keyStr)) {
            retMap = (Map)BaseMap.get(keyStr);
        } else {
            retMap = null;
        }
        return retMap;
    }

    private String getSakiSubClsCd(final List prtSubclsCdList) {
        String retStr = "";
        for (Iterator ite = prtSubclsCdList.iterator();ite.hasNext();) {
            final String lStr = (String)ite.next();
            if (!_param._ignoreSubclsList.contains(lStr)) {
                retStr = lStr;
                break;//取得できるのは1科目だけのはずだが、念のため、見つかった先頭を取得しておく。
            }
        }
        return retStr;
    }

    private String getScoreWithDI(final DetailData chkObj) {
        return getValWithDI(chkObj._score, chkObj._value_Di, chkObj._score_Di);
    }

    private String getValWithDI(final String val, final String val_di, final String score_di) {
        String retStr = "";
        if ("*".equals(val_di)) {
            final String paddStr = score_di != null ? (score_di.length() > 1 ? "" : " ") : "";
            retStr = "".equals(StringUtils.defaultString(score_di, "")) ? "（欠）" : "(" + paddStr + score_di + ")";
        } else if ("**".equals(val_di)) {
            final String paddStr = score_di != null ? (score_di.length() > 1 ? "" : " ") : "";
            retStr = "".equals(StringUtils.defaultString(score_di, "")) ? "（公欠）" : "[" + paddStr + score_di + "]";
        } else {
            retStr = StringUtils.defaultString(val, "");
        }
        return retStr;
    }

    private void printTotalLineData(final DB2UDB db2, final Vrw32alp svf, final String chair_useClassCd, final String hrClass, final Map subclassesData, final Map lineTotalMap) {
        int prtrow = 51;
        final List prtSubclsCdList = new ArrayList();
        for (Iterator ite = _param._semesMap.keySet().iterator();ite.hasNext();) {
              final String semester = (String)ite.next();
              if (Integer.parseInt(semester) > Integer.parseInt(_param._semester)) {  //指定学期以降の出力処理は不要。※指示画面で"学期末"を指定した時に右端の学年末を出力
                continue;
              }
              final String mapFstKey1 = hrClass + "-" + semester + "-" + chair_useClassCd;
              //final String mapFstKey2 = hrClass + "-" + semester + "-" + ccSplit[1];
            log.debug("mapFstKey:" + mapFstKey1);
            final Map subclassInfoMap = (Map)subclassesData.get(mapFstKey1);
            final Map lineTotalSubMap = (Map)lineTotalMap.get(mapFstKey1);
            //タイトル部 (学年末は出力なし)
            if (subclassInfoMap != null || "9".equals(semester)) {
                if (!"9".equals(semester)) {
                    int cnt=1;
                    for (Iterator its = subclassInfoMap.keySet().iterator();its.hasNext();) {
                        final String kStr = (String)its.next();
                        final SubclsNameMst snwk = (SubclsNameMst)subclassInfoMap.get(kStr);
                        if (lineTotalSubMap != null) {
                            final Map schregTotalSubMap1 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV1);
                            final Map schregTotalSubMap2 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV2);
                            final TotalLineData prtDetail1 = schregTotalSubMap1 != null ? (TotalLineData)schregTotalSubMap1.get(snwk._subclasscd + "-" + TESTIK_FST) : null;   //中間
                            final TotalLineData prtDetail2 = schregTotalSubMap1 != null ? (TotalLineData)schregTotalSubMap1.get(snwk._subclasscd + "-" + TESTIK_SCND) : null; //期末,平常点
                            final TotalLineData prtDetail3 = schregTotalSubMap2 != null ? (TotalLineData)schregTotalSubMap2.get(snwk._subclasscd + "-" + TESTIK_FINAL) : null; //期末,平常点
                            if (!"".equals(snwk._subclasscd) && !prtSubclsCdList.contains(snwk._subclasscd)) {
                                prtSubclsCdList.add(snwk._subclasscd);
                            }
                            if (cnt<=4) {
                                if (!"9".equals(semester)) {
                                    if (!"3".equals(semester)) {
                                        if (prtDetail1 != null) {
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow, prtDetail1._score);  //合計(中間)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+1, prtDetail1._cnt);  //受験人数(中間)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+2, prtDetail1._class_Avg);  //クラス平均(中間)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+3, prtDetail1._grp_Avg);  //グループ平均(中間)
                                        }
                                        if (prtDetail2 != null) {
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow, (prtDetail2._score));  //合計(期末)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+1, (prtDetail2._cnt));  //受験人数(期末)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+2, (prtDetail2._class_Avg));  //クラス平均(期末)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+3, (prtDetail2._grp_Avg));  //グループ平均(期末)
                                        }
                                        if (prtDetail3 != null) {
                                            svf.VrsOutn("SCORE"+semester+"_3_"+cnt, prtrow, prtDetail3._score);  //合計(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_3_"+cnt, prtrow+1, prtDetail3._cnt);  //受験人数(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_3_"+cnt, prtrow+2, prtDetail3._class_Avg);  //クラス平均(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_3_"+cnt, prtrow+3, prtDetail3._grp_Avg);  //グループ平均(平常点)
                                        }
                                    } else {
                                        if (prtDetail2 != null) {
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow, (prtDetail2._score));  //合計(期末)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+1, (prtDetail2._cnt));  //受験人数(期末)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+2, (prtDetail2._class_Avg));  //クラス平均(期末)
                                            svf.VrsOutn("SCORE"+semester+"_1_"+cnt, prtrow+3, (prtDetail2._grp_Avg));  //グループ平均(期末)
                                        }
                                        if (prtDetail3 != null) {
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow, prtDetail3._score);  //合計(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+1, prtDetail3._cnt);  //受験人数(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+2, prtDetail3._class_Avg);  //クラス平均(平常点)
                                            svf.VrsOutn("SCORE"+semester+"_2_"+cnt, prtrow+3, prtDetail3._grp_Avg);  //グループ平均(平常点)
                                        }
                                    }
                                    _hasData = true;
                                }
                            }
                            cnt++;
                        }
                    }
                    if (lineTotalSubMap != null) {
                        Map schregTotalSubMap3 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV8); //期末成績,到達度
                        Map schregTotalSubMap4 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV9); //期末成績,到達度
                        final String sakiSubClsCd = getSakiSubClsCd(prtSubclsCdList);
                        if (!"".equals(sakiSubClsCd)) {
                            final TotalLineData prtDetail3 = schregTotalSubMap3 != null ? (TotalLineData)schregTotalSubMap3.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null;
                            final TotalLineData prtDetail4 = schregTotalSubMap4 != null ? (TotalLineData)schregTotalSubMap4.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null;
                            if (prtDetail3 != null) {
                                svf.VrsOutn("SCORE"+semester+"_9", prtrow, prtDetail3._score);              //合計
                                svf.VrsOutn("SCORE"+semester+"_9", prtrow+1, prtDetail3._cnt);       //人数
                                svf.VrsOutn("SCORE"+semester+"_9", prtrow+2, prtDetail3._class_Avg);        //クラス平均
                                svf.VrsOutn("SCORE"+semester+"_9", prtrow+3, prtDetail3._grp_Avg);          //グループ平均
                                //svf.VrsOutn("KEKKA"+semester, prtrow, prtDetail3._absent);                //サンプルが空いているので、踏襲。そもそも、合計、受験人数見てどうするの？
                            }
                            if (prtDetail4 != null) {
                                svf.VrsOutn("HOPE"+semester, prtrow, prtDetail4._score);              //合計
                                svf.VrsOutn("HOPE"+semester, prtrow+1, prtDetail4._cnt);             //人数
                                svf.VrsOutn("HOPE"+semester, prtrow+2, prtDetail4._class_Avg);  //クラス平均
                                svf.VrsOutn("HOPE"+semester, prtrow+3, prtDetail4._grp_Avg);    //グループ平均
                                //svf.VrsOutn("MANNER"+semester, prtrow, "");
                            }
                        }
                    }
                } else {
                    if (lineTotalSubMap != null) {
                        Map schregTotalSubMap3 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV8); //期末成績,到達度
                        Map schregTotalSubMap4 = (Map)lineTotalSubMap.get(TOTAL_ID_SCHREG + "-" + SCOREDIV9); //期末成績,到達度
                        final String sakiSubClsCd = getSakiSubClsCd(prtSubclsCdList);
                        if (!"".equals(sakiSubClsCd)) {
                            final TotalLineData prtDetail3 = schregTotalSubMap3 != null ? (TotalLineData)schregTotalSubMap3.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null;
                            final TotalLineData prtDetail4 = schregTotalSubMap4 != null ? (TotalLineData)schregTotalSubMap4.get(sakiSubClsCd + "-" + TESTIK_FINAL) : null;
                            if (prtDetail3 != null) {
                                svf.VrsOutn("SCORE4_9", prtrow, prtDetail3._score);        //合計
                                svf.VrsOutn("SCORE4_9", prtrow+1, prtDetail3._cnt); //人数
                                svf.VrsOutn("SCORE4_9", prtrow+2, prtDetail3._class_Avg);  //クラス平均
                                svf.VrsOutn("SCORE4_9", prtrow+3, prtDetail3._grp_Avg);    //グループ平均
                            }
                            if (prtDetail4 != null) {
                                //svf.VrsOutn("KEKKA"+semester, prtrow, prtDetail3._absent); //サンプルが空いているので、踏襲。そもそも、合計、受験人数見てどうするの？
                                svf.VrsOutn("HOPE4", prtrow, prtDetail4._score);
                                svf.VrsOutn("HOPE4", prtrow+1, prtDetail4._cnt);
                                svf.VrsOutn("HOPE4", prtrow+2, prtDetail4._class_Avg);
                                svf.VrsOutn("HOPE4", prtrow+3, prtDetail4._grp_Avg);
                                //svf.VrsOutn("MANNER"+semester, prtrow, "");
                            }
                        }
                    }
                }
            }
        }
    }

    private void VrsOutnScore(final Vrw32alp svf, final String field, final int gyo, final String data) {
        if (5 < KNJ_EditEdit.getMS932ByteLength(data)) {
            svf.VrsOutn(field + "_8KETA", gyo, data);
            return;
        }
        svf.VrsOutn(field, gyo, data);
    }

    //科目名称
    private Map getSubclsNameMst(final DB2UDB db2) throws Exception {
        final Map retMap = new LinkedMap();
        final String sql = getSubclsNameMstSql();
        log.debug("getSubclsNameMst sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwkMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String hr_Class = rs.getString("HR_CLASS");
                final String semester = rs.getString("SEMESTER");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String classcd = rs.getString("CLASSCD");
                final String classname = rs.getString("CLASSNAME");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String credits = rs.getString("CREDITS");
                final String staffcd = rs.getString("STAFFCD");
                final String staffname = rs.getString("STAFFNAME");
                final String st_Num1 = rs.getString("ST_NUM1");
                final String st_Num2 = rs.getString("ST_NUM2");
                SubclsNameMst addwk = new SubclsNameMst(hr_Class, semester, subclasscd, classcd, classname, subclassname, subclassabbv, credits, staffcd, staffname, st_Num1, st_Num2);
                final String mapFstKey = hr_Class + "-" + semester + "-" + classcd;
                if (retMap.containsKey(mapFstKey)) {
                    addwkMap = (Map)retMap.get(mapFstKey);
                } else {
                    addwkMap = new LinkedMap();
                    retMap.put(mapFstKey, addwkMap);
                }
                addwkMap.put(subclasscd, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSubclsNameMstSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SUBCLS_DAT_BASE AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("   T4.CLASSNAME, ");
        stb.append("   T6.SUBCLASSNAME, ");
        stb.append("   T6.SUBCLASSABBV, ");
        stb.append("   CASE WHEN T9.ATTEND_CLASSCD || T9.ATTEND_SCHOOL_KIND || T9.ATTEND_CURRICULUM_CD || T9.ATTEND_SUBCLASSCD IS NULL THEN 0 ELSE 1 END AS COMB_FLG, ");
        stb.append("   T8.CREDITS, ");
        stb.append("   T5.STAFFCD, ");
        stb.append("   T7.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   LEFT JOIN CHAIR_DAT T3 ");
        stb.append("     ON T3.YEAR = T2.YEAR ");
        stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("   LEFT JOIN CLASS_MST T4 ");
        stb.append("     ON T4.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("   LEFT JOIN CHAIR_STF_DAT T5 ");
        stb.append("     ON T5.YEAR = T2.YEAR ");
        stb.append("    AND T5.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T5.CHAIRCD = T2.CHAIRCD ");
        stb.append("   LEFT JOIN SUBCLASS_MST T6 ");
        stb.append("     ON T6.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T6.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T6.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T6.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("   LEFT JOIN STAFF_MST T7 ");
        stb.append("     ON T7.STAFFCD = T5.STAFFCD ");
        stb.append("   LEFT JOIN CREDIT_MST T8 ");
        stb.append("     ON T8.YEAR = T1.YEAR ");
        stb.append("    AND T8.COURSECD = T1.COURSECD ");
        stb.append("    AND T8.MAJORCD = T1.MAJORCD ");
        stb.append("    AND T8.COURSECODE = T1.COURSECODE ");
        stb.append("    AND T8.GRADE = T1.GRADE ");
        stb.append("    AND T8.CLASSCD = T3.CLASSCD ");
        stb.append("    AND T8.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T8.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T8.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T9 ");
        stb.append("     ON T9.YEAR = T1.YEAR ");
        stb.append("    AND T9.COMBINED_CLASSCD = T3.CLASSCD ");
        stb.append("    AND T9.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("    AND T9.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("    AND T9.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("    AND REPLACECD = '1' ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.SEMESTER, T1.HR_CLASS, T1.SUBCLASSCD ORDER BY T1.STAFFCD) AS ST_NUM1, ");
        stb.append("   ROW_NUMBER() OVER(PARTITION BY T1.SEMESTER, T1.HR_CLASS ORDER BY T1.STAFFCD) AS ST_NUM2, ");
        stb.append("   T1.* ");
        stb.append(" FROM ");
        stb.append("   SUBCLS_DAT_BASE T1 ");
        stb.append(" ORDER BY ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.SEMESTER, ");
        stb.append("   T1.CLASSCD,");
        stb.append("   T1.COMB_FLG DESC, ");
        stb.append("   T1.SUBCLASSCD ");
        return stb.toString();
    }

    //生徒データ
    private Map getSchregData(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getSchregDataSql();
        log.debug("getSchregData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwkMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                SchregData addwk = new SchregData(schregno, hr_Class, attendno, name);
                final String mapFstKey = hr_Class;
                if (retMap.containsKey(mapFstKey)) {
                    addwkMap = (Map)retMap.get(mapFstKey);
                } else {
                    addwkMap = new LinkedMap();
                    retMap.put(mapFstKey, addwkMap);
                }
                addwkMap.put(schregno, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSchregDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T2.NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_BASE_MST T2 ");
        stb.append("     ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses));
        stb.append(" ORDER BY ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO ");
        return stb.toString();
    }

    //学年末成績データ
    private Map getTotalData(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getTotalDataSql();
        log.debug("getTotalData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwkMap = new LinkedMap();
        Map addwkSubMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String score_Div = rs.getString("SCORE_DIV");
                final String testik = rs.getString("TESTIK");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String total_3Gakki = rs.getString("TOTAL_3GAKKI");
                final String kimatu_Score = rs.getString("KIMATU_SCORE");
                final String last_Toutatudo = rs.getString("LAST_TOUTATUDO");
                final String last_Gakusyutaido = rs.getString("LAST_GAKUSYUTAIDO");
                TotalData addwk = new TotalData(semester, score_Div, testik, chaircd, classcd, subclasscd, grade, hr_Class, attendno, schregno, total_3Gakki, kimatu_Score, last_Toutatudo, last_Gakusyutaido);
                final String mapFstKey = hr_Class + "-" + semester + "-" + classcd;
                if (retMap.containsKey(mapFstKey)) {
                    addwkMap = (Map)retMap.get(mapFstKey);
                } else {
                    addwkMap = new LinkedMap();
                    retMap.put(mapFstKey, addwkMap);
                }
                final String mapSubKey = schregno + "-" + score_Div;
                if (addwkMap.containsKey(mapSubKey)) {
                    addwkSubMap = (Map)addwkMap.get(mapSubKey);
                } else {
                    addwkSubMap = new LinkedMap();
                    addwkMap.put(mapSubKey, addwkSubMap);
                }
                addwkSubMap.put(subclasscd + "-" + testik, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getTotalDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH KEKKA_DATA_BASE AS ( ");
        stb.append(" select ");
        stb.append("   SCHREGNO, ");
        stb.append("   '9' AS SEMESTER, ");
        stb.append("   CLASSCD, ");
        stb.append("   CASE WHEN SUM(VALUE(SICK, 0)+VALUE(NOTICE, 0)+VALUE(NONOTICE, 0)) = 0 THEN NULL ELSE SUM(VALUE(SICK, 0)+VALUE(NOTICE, 0)+VALUE(NONOTICE, 0)) END AS ABSENT ");
        stb.append(" from ");
        stb.append("   ATTEND_SUBCLASS_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '"+_param._year+"' ");
        stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append("   AND CASE WHEN '04' <= '" + _param._attendMonth + "' THEN ('04' <= MONTH AND MONTH <= '" + _param._attendMonth + "') ELSE ('04' <= MONTH OR MONTH <= '" + _param._attendMonth + "') END");
        stb.append(" GROUP BY ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     '" + SCOREDIV8 + "' AS SCORE_DIV, ");
        stb.append("     '" + TESTIK_FINAL + "' AS TESTIK, ");
        stb.append("     T3.CHAIRCD, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     NULL AS TOTAL_3GAKKI, ");
        stb.append("     T9.SCORE AS KIMATU_SCORE, ");
        stb.append("     T11.ABSENT, ");
        stb.append("     T10.SCORE AS LAST_TOUTATUDO, ");
        stb.append("     NULL AS LAST_GAKUSYUTAIDO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("      AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T8 ");
        stb.append("       ON T8.YEAR = T1.YEAR ");
        stb.append("      AND T8.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T8.TESTKINDCD || T8.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T8.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T8.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T9 ");
        stb.append("       ON T9.YEAR = T1.YEAR ");
        stb.append("      AND T9.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T9.TESTKINDCD || T9.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T9.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T9.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T9.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T9.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T9.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T9.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T10 ");
        stb.append("       ON T10.YEAR = T1.YEAR ");
        stb.append("      AND T10.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T10.TESTKINDCD || T10.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T10.SCORE_DIV = '" + SCOREDIV9 + "' ");
        stb.append("      AND T10.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T10.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T10.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T10.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("      AND T10.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN KEKKA_DATA_BASE T11 ");
        stb.append("       ON T11.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T11.CLASSCD = T3.CLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER <= '" + _param.getSemester() + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     '9' AS SEMESTER, ");
        stb.append("     '" + SCOREDIV9 + "' AS SCORE_DIV, ");
        stb.append("     '" + TESTIK_FINAL + "' AS TESTIK, ");
        stb.append("     T3.CHAIRCD, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CASE WHEN (T8_1.SCORE = NULL AND T8_2.SCORE = NULL AND T8_3.SCORE = NULL) THEN NULL ELSE (VALUE(T8_1.SCORE, 0) + VALUE(T8_2.SCORE, 0) + VALUE(T8_3.SCORE, 0)) END AS TOTAL_3GAKKI, ");
        stb.append("     T9.SCORE AS KIMATU_SCORE, ");
        stb.append("     T11.ABSENT, ");
        stb.append("     T10.SCORE AS LAST_TOUTATUDO, ");
        stb.append("     NULL AS LAST_GAKUSYUTAIDO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("      AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T8_1 ");  //1学期の成績;(年度)
        stb.append("       ON T8_1.YEAR = T1.YEAR ");
        stb.append("      AND T8_1.SEMESTER = '1' ");
        stb.append("      AND T8_1.TESTKINDCD || T8_1.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T8_1.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T8_1.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T8_1.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8_1.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8_1.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8_1.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T8_2 ");  //2学期の成績;(年度)
        stb.append("       ON T8_2.YEAR = T1.YEAR ");
        stb.append("      AND T8_2.SEMESTER = '2' ");
        stb.append("      AND T8_2.TESTKINDCD || T8_2.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T8_2.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T8_2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T8_2.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8_2.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8_2.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8_2.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T8_3 ");  //3学期の成績;(年度)
        stb.append("       ON T8_3.YEAR = T1.YEAR ");
        stb.append("      AND T8_3.SEMESTER = '3' ");
        stb.append("      AND T8_3.TESTKINDCD || T8_3.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T8_3.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T8_3.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T8_3.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8_3.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8_3.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8_3.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T9 ");  //年度末の成績(年度)
        stb.append("       ON T9.YEAR = T1.YEAR ");
        stb.append("      AND T9.SEMESTER = '9' ");
        stb.append("      AND T9.TESTKINDCD || T9.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T9.SCORE_DIV = '" + SCOREDIV8 + "' ");
        stb.append("      AND T9.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T9.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T9.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T9.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T9.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T10 ");  //年度末の評定(年度)
        stb.append("       ON T10.YEAR = T1.YEAR ");
        stb.append("      AND T10.SEMESTER = '9' ");
        stb.append("      AND T10.TESTKINDCD || T10.TESTITEMCD = '" + TESTIK_FINAL + "' ");
        stb.append("      AND T10.SCORE_DIV = '" + SCOREDIV9 + "'");
        stb.append("      AND T10.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T10.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T10.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T10.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("      AND T10.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN KEKKA_DATA_BASE T11 ");  //欠課時数(年度)
        stb.append("       ON T11.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T11.CLASSCD = T3.CLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");

        return stb.toString();
    }

    //学期別成績データ
    private Map getDetailData(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getDetailDataSql();
        log.debug("getDetailData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwkMap = new LinkedMap();
        Map addwkSubMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String semester = rs.getString("SEMESTER");
                final String score_Div = rs.getString("SCORE_DIV");
                final String testik = rs.getString("TESTIK");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String score = rs.getString("SCORE");
                final String value_Di = rs.getString("VALUE_DI");
                final String score_Di = rs.getString("SCORE_DI");
                DetailData addwk = new DetailData(semester, score_Div, testik, chaircd, classcd, subclasscd, grade, hr_Class, attendno, schregno, score, value_Di, score_Di);
                final String mapFstKey = hr_Class + "-" + semester + "-" + classcd;
                if (retMap.containsKey(mapFstKey)) {
                    addwkMap = (Map)retMap.get(mapFstKey);
                } else {
                    addwkMap = new LinkedMap();
                    retMap.put(mapFstKey, addwkMap);
                }
                final String mapSubKey = schregno + "-" + score_Div;
                if (addwkMap.containsKey(mapSubKey)) {
                    addwkSubMap = (Map)addwkMap.get(mapSubKey);
                } else {
                    addwkSubMap = new LinkedMap();
                    addwkMap.put(mapSubKey, addwkSubMap);
                }
                addwkSubMap.put(subclasscd + "-" + testik, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getDetailDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCORE_WITH_VDI AS ( ");
        stb.append(" (SELECT ");
        stb.append("   T8.YEAR, ");
        stb.append("   T8.SEMESTER, ");
        stb.append("   T8.TESTKINDCD, ");
        stb.append("   T8.TESTITEMCD, ");
        stb.append("   T8.SCORE_DIV, ");
        stb.append("   T8.CLASSCD, ");
        stb.append("   T8.SCHOOL_KIND, ");
        stb.append("   T8.CURRICULUM_CD, ");
        stb.append("   T8.SUBCLASSCD, ");
        stb.append("   T8.SCHREGNO, ");
        stb.append("   T8.SCORE, ");
        stb.append("   '' AS VALUE_DI, ");
        stb.append("   NULL AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T8) ");
        stb.append(" UNION ALL ");
        stb.append(" (SELECT ");
        stb.append("   T10.YEAR, ");
        stb.append("   T10.SEMESTER, ");
        stb.append("   T10.TESTKINDCD, ");
        stb.append("   T10.TESTITEMCD, ");
        stb.append("   T10.SCORE_DIV, ");
        stb.append("   T10.CLASSCD, ");
        stb.append("   T10.SCHOOL_KIND, ");
        stb.append("   T10.CURRICULUM_CD, ");
        stb.append("   T10.SUBCLASSCD, ");
        stb.append("   T10.SCHREGNO, ");
        stb.append("   0 AS SCORE, ");
        stb.append("   T10.VALUE_DI, ");
        stb.append("   T8_DI.SCORE AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT T10 ");
        stb.append("   LEFT JOIN SUPP_EXA_SDIV_DAT T8_DI ");
        stb.append("     ON T8_DI.YEAR = T10.YEAR ");
        stb.append("    AND T8_DI.SEMESTER = T10.SEMESTER ");
        stb.append("    AND T8_DI.TESTKINDCD = T10.TESTKINDCD ");
        stb.append("    AND T8_DI.TESTITEMCD = T10.TESTITEMCD ");
        stb.append("    AND T8_DI.SCORE_DIV = T10.SCORE_DIV ");
        stb.append("    AND T8_DI.CLASSCD = T10.CLASSCD ");
        stb.append("    AND T8_DI.SCHOOL_KIND = T10.SCHOOL_KIND ");
        stb.append("    AND T8_DI.CURRICULUM_CD = T10.CURRICULUM_CD ");
        stb.append("    AND T8_DI.SUBCLASSCD = T10.SUBCLASSCD ");
        stb.append("    AND T8_DI.SCHREGNO = T10.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   VALUE_DI IS NOT NULL AND VALUE_DI <> '') ");
        stb.append(" ), SCORE_WITH_VDI_GRP AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCHREGNO, ");
        stb.append("   SUM(SCORE) AS SCORE, ");
        stb.append("   LISTAGG(VALUE_DI, '') AS VALUE_DI, ");
        stb.append("   SUM(SCORE_DI) AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   SCORE_WITH_VDI ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCHREGNO ");
        stb.append(" ), GET_DAT_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T8.SEMESTER, ");
        stb.append("     T8.SCORE_DIV, ");
        stb.append("     T8.TESTKINDCD || T8.TESTITEMCD AS TESTIK, ");
        stb.append("     T3.CHAIRCD, ");
        stb.append("     T3.CLASSCD, ");
        stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T8.SCORE AS SCORE, ");
        stb.append("     T8.VALUE_DI, ");
        stb.append("     T8.SCORE_DI AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("      AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     LEFT JOIN SCORE_WITH_VDI_GRP T8 ");
        stb.append("       ON T8.YEAR = T1.YEAR ");
        stb.append("      AND T8.SEMESTER <= T1.SEMESTER ");
        stb.append("      AND T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND CASE WHEN T8.SEMESTER < T1.SEMESTER THEN '" + TESTIK_FINAL + "' ELSE T8.TESTKINDCD || T8.TESTITEMCD END <= '" + TESTIK_FINAL + "' ");
        stb.append("      AND T8.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append(" ), KEKKA_DATA_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SEMESTER, ");
        stb.append("     CLASSCD, ");
        stb.append("     CASE WHEN SUM(VALUE(SICK, 0)+VALUE(NOTICE, 0)+VALUE(NONOTICE, 0)) = 0 THEN NULL ELSE SUM(VALUE(SICK, 0)+VALUE(NOTICE, 0)+VALUE(NONOTICE, 0)) END AS ABSENT ");
        stb.append(" FROM ");
        stb.append("     ATTEND_SUBCLASS_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        stb.append("   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append("   AND CASE WHEN '04' <= '" + _param._attendMonth + "' THEN ('04' <= MONTH AND MONTH <= '" + _param._attendMonth + "') ELSE ('04' <= MONTH OR MONTH <= '" + _param._attendMonth + "') END ");
        stb.append(" GROUP BY ");
        stb.append("     SEMESTER, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHREGNO ");
        stb.append(" )  SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T11.ABSENT ");
        stb.append(" FROM ");
        stb.append("    GET_DAT_BASE T1 ");
        stb.append("    LEFT JOIN KEKKA_DATA_BASE T11 ");
        stb.append("      ON T11.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T11.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T11.CLASSCD = T1.CLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.SCORE_DIV, ");
        stb.append("     T1.TESTIK, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    //表最下部の合計データ
    private Map getTotalLineData(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        final String sql = getTotalLineData();
        log.debug("getTotalLineData sql:"+sql);
        ResultSet rs = null;
        PreparedStatement ps = null;
        Map addwkMap = new LinkedMap();
        Map addwkSubMap = new LinkedMap();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String testik = rs.getString("TESTIK");
                final String score_Div = rs.getString("SCORE_DIV");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String combo_Subclasscd = rs.getString("COMBO_SUBCLASSCD");
                final String grade = rs.getString("GRADE");
                final String hr_Class = rs.getString("HR_CLASS");
                final String score = rs.getString("SCORE");
                final String class_Avg = rs.getString("CLASS_AVG");
                final String cnt = rs.getString("CNT");
                final String grp_Avg = rs.getString("GRP_AVG");
                TotalLineData addwk = new TotalLineData(year, semester, testik, score_Div, chaircd, classcd, subclasscd, combo_Subclasscd, grade, hr_Class, score, class_Avg, cnt, grp_Avg);
                final String mapFstKey = hr_Class + "-" + semester + "-" + classcd;
                if (retMap.containsKey(mapFstKey)) {
                    addwkMap = (Map)retMap.get(mapFstKey);
                } else {
                    addwkMap = new LinkedMap();
                    retMap.put(mapFstKey, addwkMap);
                }
                final String mapSubKey = TOTAL_ID_SCHREG + "-" + score_Div;
                if (addwkMap.containsKey(mapSubKey)) {
                    addwkSubMap = (Map)addwkMap.get(mapSubKey);
                } else {
                    addwkSubMap = new LinkedMap();
                    addwkMap.put(mapSubKey, addwkSubMap);
                }
                addwkSubMap.put(combo_Subclasscd + "-" + testik, addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getTotalLineData() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCORE_WITH_VDI AS ( ");
        stb.append(" (SELECT ");
        stb.append("   T8.YEAR, ");
        stb.append("   T8.SEMESTER, ");
        stb.append("   T8.TESTKINDCD, ");
        stb.append("   T8.TESTITEMCD, ");
        stb.append("   T8.SCORE_DIV, ");
        stb.append("   T8.CLASSCD, ");
        stb.append("   T8.SCHOOL_KIND, ");
        stb.append("   T8.CURRICULUM_CD, ");
        stb.append("   T8.SUBCLASSCD, ");
        stb.append("   T8.SCHREGNO, ");
        stb.append("   T8.SCORE, ");
        stb.append("   '' AS VALUE_DI, ");
        stb.append("   NULL AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   RECORD_RANK_SDIV_DAT T8) ");
        stb.append(" UNION ALL ");
        stb.append(" (SELECT ");
        stb.append("   T10.YEAR, ");
        stb.append("   T10.SEMESTER, ");
        stb.append("   T10.TESTKINDCD, ");
        stb.append("   T10.TESTITEMCD, ");
        stb.append("   T10.SCORE_DIV, ");
        stb.append("   T10.CLASSCD, ");
        stb.append("   T10.SCHOOL_KIND, ");
        stb.append("   T10.CURRICULUM_CD, ");
        stb.append("   T10.SUBCLASSCD, ");
        stb.append("   T10.SCHREGNO, ");
        stb.append("   0 AS SCORE, ");
        stb.append("   T10.VALUE_DI, ");
        stb.append("   T8_DI.SCORE AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   RECORD_SCORE_DAT T10 ");
        stb.append("   LEFT JOIN SUPP_EXA_SDIV_DAT T8_DI ");
        stb.append("     ON T8_DI.YEAR = T10.YEAR ");
        stb.append("    AND T8_DI.SEMESTER = T10.SEMESTER ");
        stb.append("    AND T8_DI.TESTKINDCD = T10.TESTKINDCD ");
        stb.append("    AND T8_DI.TESTITEMCD = T10.TESTITEMCD ");
        stb.append("    AND T8_DI.SCORE_DIV = T10.SCORE_DIV ");
        stb.append("    AND T8_DI.CLASSCD = T10.CLASSCD ");
        stb.append("    AND T8_DI.SCHOOL_KIND = T10.SCHOOL_KIND ");
        stb.append("    AND T8_DI.CURRICULUM_CD = T10.CURRICULUM_CD ");
        stb.append("    AND T8_DI.SUBCLASSCD = T10.SUBCLASSCD ");
        stb.append("    AND T8_DI.SCHREGNO = T10.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   VALUE_DI IS NOT NULL AND VALUE_DI <> '') ");
        stb.append(" ), SCORE_WITH_VDI_GRP AS ( ");
        stb.append(" SELECT ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCHREGNO, ");
        stb.append("   SUM(SCORE) AS SCORE, ");
        stb.append("   LISTAGG(VALUE_DI, '') AS VALUE_DI, ");
        stb.append("   SUM(SCORE_DI) AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("   SCORE_WITH_VDI ");
        stb.append(" GROUP BY ");
        stb.append("   YEAR, ");
        stb.append("   SEMESTER, ");
        stb.append("   TESTKINDCD, ");
        stb.append("   TESTITEMCD, ");
        stb.append("   SCORE_DIV, ");
        stb.append("   CLASSCD, ");
        stb.append("   SCHOOL_KIND, ");
        stb.append("   CURRICULUM_CD, ");
        stb.append("   SUBCLASSCD, ");
        stb.append("   SCHREGNO ");
        stb.append(" ), GET_DAT_BASE AS ( "); //  -- 各項目(SCORE_DIVで判別)毎のデータ。後のGRP_DAT_BASEで集計
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T8.SEMESTER, ");
        stb.append("     T8.SCORE_DIV, ");  //  --ここでは01,02,08,09を持ってるはず。それぞれで合計する。
        stb.append("     T8.TESTKINDCD || T8.TESTITEMCD AS TESTIK, ");
        stb.append("     T3.CHAIRCD, ");
        stb.append("     T8.CLASSCD, ");
        stb.append("     T8.SUBCLASSCD, ");
        stb.append("     T8.CLASSCD || '-' || T8.SCHOOL_KIND || '-' || T8.CURRICULUM_CD || '-' || T8.SUBCLASSCD AS COMBO_SUBCLASSCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T8.SCORE AS SCORE, ");
        stb.append("     T8.VALUE_DI, ");
        stb.append("     T8.SCORE AS SCORE_DI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_STD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN CHAIR_DAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("      AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("      AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     LEFT JOIN SCORE_WITH_VDI_GRP T8 ");
        stb.append("       ON T8.YEAR = T1.YEAR ");
        stb.append("      AND T8.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND (T8.TESTKINDCD || T8.TESTITEMCD = '9900' ");
        stb.append("           OR ");
        stb.append("           T8.TESTKINDCD || T8.TESTITEMCD <= '" + _param._testik + "' ");  //    -- 画面指定の試験まで取得。
        stb.append("          ) ");
        stb.append("      AND T8.CLASSCD = T3.CLASSCD ");
        stb.append("      AND T8.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("      AND T8.CURRICULUM_CD = T3.CURRICULUM_CD ");
        stb.append("      AND T8.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER <= '" + _param.getSemester() + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("     AND T8.CLASSCD || '-' || T8.SCHOOL_KIND || '-' || T8.CURRICULUM_CD || '-' || T8.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append(" ), GRP_DAT_BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     B1.YEAR, ");
        stb.append("     B1.SEMESTER, ");
        stb.append("     B1.TESTIK, ");
        stb.append("     B1.SCORE_DIV, ");
        stb.append("     B1.CHAIRCD, ");
        stb.append("     B1.CLASSCD, ");
        stb.append("     B1.SUBCLASSCD, ");
        stb.append("     B1.COMBO_SUBCLASSCD, ");
        stb.append("     B1.GRADE, ");
        stb.append("     B1.HR_CLASS, ");
        stb.append("     SUM(CASE WHEN VALUE(B1.VALUE_DI, '') <> '' THEN B1.SCORE_DI ELSE B1.SCORE END) AS SCORE ");
        stb.append(" FROM ");
        stb.append("     GET_DAT_BASE B1 ");
        stb.append(" GROUP BY ");
        stb.append("     B1.YEAR, ");
        stb.append("     B1.SEMESTER, ");
        stb.append("     B1.GRADE, ");
        stb.append("     B1.HR_CLASS, ");
        stb.append("     B1.SCORE_DIV, ");
        stb.append("     B1.TESTIK, ");
        stb.append("     B1.CHAIRCD, ");
        stb.append("     B1.CLASSCD, ");
        stb.append("     B1.SUBCLASSCD, ");
        stb.append("     B1.COMBO_SUBCLASSCD ");
        stb.append(" ), AVG_DAT_BASE AS ( ");
        stb.append(" select ");
        stb.append("     AVGD1.YEAR, ");
        stb.append("     AVGD1.SEMESTER, ");
        stb.append("     AVGD1.AVG_DIV, ");
        stb.append("     AVGD1.SCORE_DIV, ");
        stb.append("     AVGD1.TESTKINDCD || AVGD1.TESTITEMCD AS TESTIK, ");
        stb.append("     AVGD1.CLASSCD, ");
        stb.append("     AVGD1.SUBCLASSCD, ");
        stb.append("     AVGD1.CLASSCD || '-' || AVGD1.SCHOOL_KIND || '-' || AVGD1.CURRICULUM_CD || '-' || AVGD1.SUBCLASSCD AS COMBO_SUBCLASSCD, ");
        stb.append("     AVGD1.GRADE, ");
        stb.append("     AVGD1.HR_CLASS, ");
        stb.append("     AVGD1.AVG AS AVERAGE, ");
        stb.append("     AVGD1.COUNT AS CNT ");
        stb.append(" from ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT AVGD1 ");
        stb.append("       ON AVGD1.YEAR = T1.YEAR ");
        stb.append("      AND AVGD1.SEMESTER <= CASE WHEN T1.SEMESTER = '3' THEN '9' ELSE T1.SEMESTER END ");  // --大小、3学期なら9学期として取得
        stb.append("      AND CASE WHEN AVGD1.SEMESTER < T1.SEMESTER THEN '" + TESTIK_FINAL + "' ELSE AVGD1.TESTKINDCD || AVGD1.TESTITEMCD END <= '" + TESTIK_FINAL + "' ");  //     -- 画面指定の試験まで取得。
        stb.append("      AND AVGD1.GRADE = T1.GRADE ");
        stb.append("      AND AVGD1.COURSECD = '0' ");
        stb.append("      AND AVGD1.MAJORCD = '000' ");
        stb.append("      AND AVGD1.COURSECODE = '0000' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _param._hrClasses) + " ");
        stb.append("     AND AVGD1.CLASSCD || '-' || AVGD1.SCHOOL_KIND || '-' || AVGD1.CURRICULUM_CD || '-' || AVGD1.SUBCLASSCD IN " + SQLUtils.whereIn(false, _param._searchedSubclass) + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     G1.*, ");
        stb.append("     DECIMAL(INT(AVGD1.AVERAGE*10.0+0.5)/10.0, 4,1) AS CLASS_AVG, ");
        stb.append("     AVGD1.CNT, ");
        stb.append("     DECIMAL(INT(AVGD2.AVERAGE*10.0+0.5)/10.0, 4,1) AS GRP_AVG ");
        stb.append(" FROM ");
        stb.append("     GRP_DAT_BASE G1 ");
        stb.append("     LEFT JOIN AVG_DAT_BASE AVGD1 ");
        stb.append("       ON AVGD1.YEAR = G1.YEAR ");
        stb.append("      AND AVGD1.SEMESTER = G1.SEMESTER ");
        stb.append("      AND AVGD1.SCORE_DIV = G1.SCORE_DIV ");
        stb.append("      AND AVGD1.TESTIK = G1.TESTIK ");
        stb.append("      AND AVGD1.CLASSCD = G1.CLASSCD ");
        stb.append("      AND AVGD1.SUBCLASSCD = G1.SUBCLASSCD ");
        stb.append("      AND AVGD1.COMBO_SUBCLASSCD = G1.COMBO_SUBCLASSCD ");
        stb.append("      AND AVGD1.GRADE = G1.GRADE ");
        stb.append("      AND AVGD1.HR_CLASS = G1.HR_CLASS ");
        stb.append("      AND AVGD1.AVG_DIV = '2' ");  //クラス指定
        stb.append("     LEFT JOIN AVG_DAT_BASE AVGD2 ");
        stb.append("       ON AVGD2.YEAR = G1.YEAR ");
        stb.append("      AND AVGD2.SEMESTER = G1.SEMESTER ");
        stb.append("      AND AVGD2.SCORE_DIV = G1.SCORE_DIV ");
        stb.append("      AND AVGD2.TESTIK = G1.TESTIK ");
        stb.append("      AND AVGD2.CLASSCD = G1.CLASSCD ");
        stb.append("      AND AVGD2.SUBCLASSCD = G1.SUBCLASSCD ");
        stb.append("      AND AVGD2.COMBO_SUBCLASSCD = G1.COMBO_SUBCLASSCD ");
        stb.append("      AND AVGD2.GRADE = G1.GRADE ");
        stb.append("      AND AVGD2.HR_CLASS = '000' ");
        stb.append("      AND AVGD2.AVG_DIV = '1' ");    //学年指定
        stb.append(" ORDER BY ");
        stb.append("     G1.SEMESTER, ");
        stb.append("     G1.GRADE, ");
        stb.append("     G1.HR_CLASS, ");
        stb.append("     G1.SCORE_DIV, ");
        stb.append("     G1.TESTIK, ");
        stb.append("     G1.CLASSCD, ");
        stb.append("     G1.CHAIRCD, ");
        stb.append("     G1.COMBO_SUBCLASSCD ");
        return stb.toString();
    }

    private class SubclsNameMst {
        final String _hr_Class;
        final String _semester;
        final String _subclasscd;
        final String _classcd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
        final String _credits;
        final String _staffcd;
        final String _staffname;
        final String _st_Num1;
        final String _st_Num2;
        public SubclsNameMst  (final String hr_Class, final String semester, final String subclasscd, final String classcd, final String classname, final String subclassname, final String subclassabbv, final String credits, final String staffcd, final String staffname, final String st_Num1, final String st_Num2)
        {
            _hr_Class = hr_Class;
            _semester = semester;
            _subclasscd = subclasscd;
            _classcd = classcd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _credits = credits;
            _staffcd = staffcd;
            _staffname = staffname;
            _st_Num1 = st_Num1;
            _st_Num2 = st_Num2;
        }
    }

    private class DetailData {
        final String _semester;
        final String _score_Div;
        final String _testik;
        final String _chaircd;
        final String _classcd;
        final String _subclasscd;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _score;
        final String _value_Di;
        final String _score_Di;
        public DetailData (final String semester, final String score_Div, final String testik, final String chaircd, final String classcd, final String subclasscd, final String grade, final String hr_Class, final String attendno, final String schregno, final String score, final String value_Di, final String score_Di)
        {
            _semester = semester;
            _score_Div = score_Div;
            _testik = testik;
            _chaircd = chaircd;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _score = score;
            _value_Di = value_Di;
            _score_Di = score_Di;
        }

    }

    private class TotalData {
        final String _semester;
        final String _score_Div;
        final String _testik;
        final String _chaircd;
        final String _classcd;
        final String _subclasscd;
        final String _grade;
        final String _hr_Class;
        final String _attendno;
        final String _schregno;
        final String _total_3Gakki;
        final String _kimatu_Score;
        final String _last_Toutatudo;
        final String _last_Gakusyutaido;
        public TotalData (final String semester, final String score_Div, final String testik, final String chaircd, final String classcd, final String subclasscd, final String grade, final String hr_Class, final String attendno, final String schregno, final String total_3Gakki, final String kimatu_Score, final String last_Toutatudo, final String last_Gakusyutaido)
        {
            _semester = semester;
            _score_Div = score_Div;
            _testik = testik;
            _chaircd = chaircd;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _schregno = schregno;
            _total_3Gakki = total_3Gakki;
            _kimatu_Score = kimatu_Score;
            _last_Toutatudo = last_Toutatudo;
            _last_Gakusyutaido = last_Gakusyutaido;
        }
    }

    private class SchregData {
        final String _schregno;
        final String _hr_Class;
        final String _attendno;
        final String _name;
        public SchregData (final String schregno, final String hr_Class, final String attendno, final String name)
        {
            _schregno = schregno;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _name = name;
        }
    }

    private class TotalLineData {
        final String _year;
        final String _semester;
        final String _testik;
        final String _score_Div;
        final String _chaircd;
        final String _classcd;
        final String _subclasscd;
        final String _combo_Subclasscd;
        final String _grade;
        final String _hr_Class;
        final String _score;
        final String _class_Avg;
        final String _cnt;
        final String _grp_Avg;
        public TotalLineData (final String year, final String semester, final String testik, final String score_Div, final String chaircd, final String classcd, final String subclasscd, final String combo_Subclasscd, final String grade, final String hr_Class, final String score, final String class_Avg, final String cnt, final String grp_Avg)
        {
            _year = year;
            _semester = semester;
            _testik = testik;
            _score_Div = score_Div;
            _chaircd = chaircd;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _combo_Subclasscd = combo_Subclasscd;
            _grade = grade;
            _hr_Class = hr_Class;
            _score = score;
            _class_Avg = class_Avg;
            _cnt = cnt;
            _grp_Avg = grp_Avg;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 75888 $ $Date: 2020-08-06 15:41:03 +0900 (木, 06 8 2020) $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _schoolKindName;
        private final String _testcd;
        private final String _testik;
        private final String[] _hrClasses;
        private final String _date;
        private final String _attendDate;
        private final String _attendMonth;
        private final String[] _selectedSubclass;
        private final String[] _searchedSubclass;

        private Map _semesMap;
        private Map _hrNameMap;
        private final List _classCdList;
        private final Map _classCdNameMap;
        private Map _assessLevelMap;
        private List _ignoreSubclsList;

        private String _lastSemester;
        final Map _attendParamMap;
        final Map _attendInf;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _testik = _testcd.substring(0, 4);
            _hrClasses = request.getParameterValues("CATEGORY_SELECTED");
            final String[] subclassWk = request.getParameterValues("SUBCLASS_SELECTED");
            List subclsList =new ArrayList();
            for (int cnt = 0;cnt < subclassWk.length;cnt++) {
                if (subclassWk[cnt] != null && !"".equals(subclassWk[cnt]) && subclassWk[cnt].indexOf(":") >= 0) {
                    final String[] cutwk = StringUtils.split(subclassWk[cnt], ':');
                    if (!"".equals(cutwk[0])) {
                        subclsList.add(cutwk[0]);
                    }
                } else {
                    subclsList.add(subclassWk[cnt]);
                }
            }
            _selectedSubclass = (String[])subclsList.toArray(new String[subclsList.size()]);
            _searchedSubclass = getSearchedSubclass(db2);

            // DBより取得
            _semesterName = getSemesterName(db2, _year, _semester);

            _date = request.getParameter("DATE") != null ? request.getParameter("DATE").replace('/', '-') : "";//KNJ_EditDate.h_format_US_Y(returnval.val3) + "年" + KNJ_EditDate.h_format_JP_MD(returnval.val3);
            _attendDate = request.getParameter("ATTEND_DATE") != null ? request.getParameter("ATTEND_DATE").replace('/', '-') : "";
            _attendMonth = "".equals(StringUtils.defaultString(_attendDate, "")) ? "" : StringUtils.split(_attendDate, '-')[1];

            _semesMap = getSemesterMap(db2);
            _schoolKindName = getSchoolKindName(db2);
            _hrNameMap = getHrName(db2);
            _classCdList = getChairClassCdList(db2);
            _classCdNameMap = getClassCdNameMap(db2);
            _assessLevelMap = getAssessLevelMap(db2);
            _ignoreSubclsList = getIgnoreSubclsList(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendInf = getSubclassAttendance(db2);
        }

        public String[] getSearchedSubclass(final DB2UDB db2) {
            List retWkList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            ////元科目を求めた上で、さらに元に紐づいている科目を抽出する。そのために、一度元科目を探る
            ////元科目が無いパターン、元科目だけの選択パターンも想定されるので、選択した科目、元科目、元科目から辿った先科目(探った科目と選択時点でで元科目)を並べた後でDISTINCTする。
            stb.append(" WITH MOTO_SUBCLS AS ( ");
            stb.append(" SELECT DISTINCT");
            stb.append("   REP_MOTO.COMBINED_CLASSCD AS CLASSCD, ");
            stb.append("   REP_MOTO.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("   REP_MOTO.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("   REP_MOTO.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   SUBCLASS_MST T1 ");
            stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT REP_MOTO ");
            stb.append("     ON REP_MOTO.YEAR = '" + _year + "' ");
            stb.append("    AND REP_MOTO.ATTEND_CLASSCD || '-' || REP_MOTO.ATTEND_SCHOOL_KIND || '-' || REP_MOTO.ATTEND_CURRICULUM_CD || '-' || REP_MOTO.ATTEND_SUBCLASSCD  ");
            stb.append("        = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   REP_MOTO.ATTEND_CLASSCD || '-' || REP_MOTO.ATTEND_SCHOOL_KIND || '-' || REP_MOTO.ATTEND_CURRICULUM_CD || '-' || REP_MOTO.ATTEND_SUBCLASSCD IN " + SQLUtils.whereIn(false, _selectedSubclass));
            stb.append("   AND REP_MOTO.ATTEND_CLASSCD IS NOT NULL ");
            stb.append(" ), MARGE_MOTO_SUBCLS AS ( ");
            ////指示画面で選択した科目。
            stb.append(" SELECT ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   SUBCLASS_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN  " + SQLUtils.whereIn(false, _selectedSubclass));
            ////上記"MOTO_SUBCLS"の元科目。
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   MOTO_SUBCLS T1 ");
            ////上記"MOTO_SUBCLS"の元科目から、先科目を割り出す。
            stb.append(" UNION ALL ");
            stb.append(" SELECT DISTINCT");
            stb.append("   REP_SAKI.ATTEND_CLASSCD AS CLASSCD, ");
            stb.append("   REP_SAKI.ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("   REP_SAKI.ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("   REP_SAKI.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   MOTO_SUBCLS T1 ");
            stb.append("   INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT REP_SAKI ");
            stb.append("     ON REP_SAKI.YEAR = '" + _year + "' ");
            stb.append("    AND REP_SAKI.COMBINED_CLASSCD || '-' || REP_SAKI.COMBINED_SCHOOL_KIND || '-' || REP_SAKI.COMBINED_CURRICULUM_CD || '-' || REP_SAKI.COMBINED_SUBCLASSCD  ");
            stb.append("        = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   REP_SAKI.ATTEND_CLASSCD IS NOT NULL ");
            ////指示画面で選択した科目が元科目として、先科目を割り出す。
            stb.append(" UNION ALL ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     REP_SSAKI.ATTEND_CLASSCD AS CLASSCD, ");
            stb.append("     REP_SSAKI.ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ");
            stb.append("     REP_SSAKI.ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("     REP_SSAKI.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT REP_SSAKI ");
            stb.append(" WHERE ");
            stb.append("     REP_SSAKI.YEAR = '" + _year + "' ");
            stb.append("     AND REP_SSAKI.COMBINED_CLASSCD || '-' || REP_SSAKI.COMBINED_SCHOOL_KIND || '-' || REP_SSAKI.COMBINED_CURRICULUM_CD || '-' || REP_SSAKI.COMBINED_SUBCLASSCD IN  " + SQLUtils.whereIn(false, _selectedSubclass));
            stb.append("     AND REP_SSAKI.ATTEND_CLASSCD IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' ||  T1.SUBCLASSCD AS SUBCLASS ");
            stb.append(" FROM ");
            stb.append("   MARGE_MOTO_SUBCLS T1 ");
            log.debug("getSearchedSubclass sql:"+ stb.toString());
            final List wkList = KnjDbUtils.query(db2, stb.toString());
            for (Iterator ite = wkList.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                retWkList.add((String)row.get("SUBCLASS"));
            }

            return (String[])retWkList.toArray(new String[retWkList.size()]);
        }

        public String[] getClassCdListToAry(final int cutidx, final boolean add00Flg) {  //引数は、0(CHAIRCD)/1(CLASSCD)のみ。
            List retAry = new ArrayList();
            if (add00Flg) {
                retAry.add("00"); //共通利用のコード
            }
            int addCnt = 0;
            for (Iterator ite  = _classCdList.iterator();ite.hasNext();) {
                final String chairClassStr = (String)ite.next();
                if (!"".equals(chairClassStr)) {
                    final String[] cutStr = StringUtils.split(chairClassStr, '-');
                    if (cutStr.length > 0 && !"".equals(cutStr[cutidx])) {
                        if (!retAry.contains(cutStr[cutidx])) {
                            retAry.add(cutStr[cutidx]);
                        }
                        addCnt++;
                    }
                }
            }
            return (String[])retAry.toArray(new String[retAry.size()]);
        }

        public String getSemester() {
            return _semester.equals("9") ? _lastSemester : _semester;
        }

        private List getIgnoreSubclsList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("   SUBCLASS_RANK_REPLACE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            log.debug("getIgnoreSubclsList sql:"+ stb.toString());
            final List wkList = KnjDbUtils.query(db2, stb.toString());

            for (Iterator ite = wkList.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                retList.add((String)row.get("SUBCLASSCD"));
            }

            return retList;
        }

        private List getChairClassCdList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" select DISTINCT ");
            stb.append("   T2.CHAIRCD, ");
            stb.append("   T3.CLASSCD ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN CHAIR_DAT T3 ");
            stb.append("     ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER <= '" + _semester + "' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");
            stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _hrClasses) + " ");
            stb.append("   AND T3.CLASSCD < '90' ");
            stb.append("   AND T3.CLASSCD NOT IN ('00', '33', '55', '99') ");
            stb.append("   AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN " + SQLUtils.whereIn(false, _searchedSubclass));
            log.debug("getClassCdList sql:"+ stb.toString());
            final List wkList = KnjDbUtils.query(db2, stb.toString());

            for (Iterator ite = wkList.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                retList.add(StringUtils.defaultString((String)row.get("CHAIRCD"), "") + "-" + StringUtils.defaultString((String)row.get("CLASSCD"),""));
            }
            return retList;
        }

        private String getSemesterName(
                final DB2UDB db2,
                final String year,
                final String semester
        ) throws Exception {
            String rtnVal = "";

            if ("9".equals(semester)) {
                return rtnVal;
            }

            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
            log.debug("getSemesterName sql:"+ sql);
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                rs.next();
                rtnVal = rs.getString("SEMESTERNAME");
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnVal;
        }

        private Map getClassCdNameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" select DISTINCT ");
            stb.append("   T2.CLASSCD, ");
            stb.append("   T2.CLASSNAME, ");
            stb.append("   T2.CLASSABBV ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT TG ");
            stb.append("     ON TG.YEAR = T1.YEAR ");
            stb.append("    AND TG.GRADE = T1.GRADE ");
            stb.append("   LEFT JOIN CHAIR_STD_DAT T3 ");
            stb.append("     ON T3.YEAR = T1.YEAR");
            stb.append("    AND T3.SEMESTER = T1.SEMESTER");
            stb.append("    AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append("   LEFT JOIN CHAIR_DAT T4 ");
            stb.append("     ON T4.YEAR = T3.YEAR");
            stb.append("    AND T4.SEMESTER = T3.SEMESTER");
            stb.append("    AND T4.CHAIRCD = T4.CHAIRCD");
            stb.append("   LEFT JOIN CLASS_MST T2 ");
            stb.append("     ON T2.SCHOOL_KIND = TG.SCHOOL_KIND ");
              stb.append("    AND T2.CLASSCD IN "+SQLUtils.whereIn(false, getClassCdListToAry(1, false))+" ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER <= '" + _semester + "'");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");
            stb.append("   AND T1.HR_CLASS IN " + SQLUtils.whereIn(false, _hrClasses) + " ");
            stb.append("   AND T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD IN " + SQLUtils.whereIn(false, _searchedSubclass));
            stb.append(" ORDER BY ");
            stb.append("   T2.CLASSCD ");
            log.debug("getClassCdNameMap sql:"+ stb.toString());

            final List Lst = KnjDbUtils.query(db2, stb.toString());
            final Map retMap = new LinkedMap();
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String addKey = (String)row.get("CLASSCD");
                if (retMap.containsKey(addKey)) {
                    Map mergeStaff = (Map)retMap.get(addKey);
                    final String getStaffName = (String)mergeStaff.get("STAFFNAME");
                    mergeStaff.put("STAFFNAME", getStaffName + "," + (String)row.get("STAFFNAME"));
                } else {
                    retMap.put(addKey, row);
                }
            }
            return retMap;
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final String sql = " SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
            log.debug("getSemesterMap sql:"+ sql);
            final List Lst = KnjDbUtils.query(db2, sql);
            final Map rtn = new LinkedMap();
            String maxStr = "";
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String wkSems = (String)row.get("SEMESTER");
                if ("".equals(maxStr)) {
                    maxStr = wkSems;
                } else if (!"9".equals(wkSems) && Integer.parseInt(wkSems) > Integer.parseInt(maxStr)) {
                    maxStr = wkSems;
                }
                rtn.put(row.get("SEMESTER"), row);
            }
            _lastSemester = maxStr;
            return rtn;
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final String sql = " SELECT T2.ABBV1 from SCHREG_REGD_GDAT T1 LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T2.NAME1 = T1.SCHOOL_KIND WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' ";
            log.debug("getSchoolKindName sql:"+ sql);
            final List Lst = KnjDbUtils.query(db2, sql);
            return Lst.size() > 0 ? (String)((Map)Lst.get(0)).get("ABBV1") : "";
        }

        private Map getHrName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAMEABBV, ");
            stb.append("     T2.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     LEFT JOIN STAFF_MST T2 ");
            stb.append("       ON T2.STAFFCD = T1.TR_CD1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' AND ");
            stb.append("     T1.SEMESTER = '" + getSemester() + "' AND ");
            stb.append("     T1.GRADE = '" + _grade + "' AND ");
            stb.append("     T1.HR_CLASS IN " + SQLUtils.whereIn(false, _hrClasses) + " ");
            log.debug("getHrName sql:"+ stb.toString());

            final List Lst = KnjDbUtils.query(db2, stb.toString());
            final Map rtn = new LinkedMap();
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                rtn.put(row.get("HR_CLASS"), row);
            }
            return rtn;
        }

        private Map getAssessLevelMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("   TA.SEMESTER, ");
            stb.append("   TA.CLASSCD, ");
            stb.append("   TA.CLASSCD || '-' || TA.SCHOOL_KIND || '-' || TA.CURRICULUM_CD || '-' || TA.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   TA.HR_CLASS, ");
            stb.append("   TA.ASSESSLEVEL, ");
            stb.append("   TA.ASSESSHIGH, ");
            stb.append("   TA.ASSESSLOW, ");
            stb.append("   CASE WHEN TB.COMBINED_CLASSCD || TB.COMBINED_SCHOOL_KIND || TB.COMBINED_CURRICULUM_CD || TB.COMBINED_SUBCLASSCD IS NULL THEN 1 ELSE 0 END AS SORTFLG ");
            stb.append(" FROM ");
            stb.append("   ASSESS_LEVEL_SDIV_MST TA ");
            stb.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT TB ");
            stb.append("     ON TB.REPLACECD = '1' ");
            stb.append("    AND TB.YEAR = TA.YEAR ");
            stb.append("    AND TB.ATTEND_CLASSCD = TA.CLASSCD ");
            stb.append("    AND TB.ATTEND_SCHOOL_KIND = TA.SCHOOL_KIND ");
            stb.append("    AND TB.ATTEND_CURRICULUM_CD = TA.CURRICULUM_CD ");
            stb.append("    AND TB.ATTEND_SUBCLASSCD = TA.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   TA.YEAR = '" + _year + "' ");
            stb.append("   AND TA.GRADE = '" + _grade + "' ");
            stb.append("   AND TA.CLASSCD IN " + SQLUtils.whereIn(false, getClassCdListToAry(1, true)) + " ");
            stb.append("   AND (TA.HR_CLASS IN " + SQLUtils.whereIn(false, _hrClasses) + " ");
            stb.append("        OR TA.HR_CLASS = '000' ) ");  //HR_CLASS指定が"000"か指示画面選択のデータを取得。指示画面選択のデータを優先するが、無ければ000を利用。
            stb.append("   AND TA.RUISEKI_DIV = 'A' ");
            stb.append("   AND TA.DIV = '1' ");
            stb.append("   AND TA.TESTKINDCD || TA.TESTITEMCD || TA.SCORE_DIV = '" + TESTIK_FINAL + SCOREDIV9 + "' ");
            stb.append("   AND NOT EXISTS ( ");
            stb.append("                   SELECT ");
            stb.append("                     'X' ");
            stb.append("                   FROM ");
            stb.append("                     SUBCLASS_RANK_REPLACE_DAT TW ");
            stb.append("                   WHERE ");
            stb.append("                     TW.YEAR = TA.YEAR ");
            stb.append("                     AND TW.ATTEND_CLASSCD || '-' || TW.ATTEND_SCHOOL_KIND || '-' || TW.ATTEND_CURRICULUM_CD || '-' || TW.ATTEND_SUBCLASSCD ");
            stb.append("                         = TA.CLASSCD || '-' || TA.SCHOOL_KIND || '-' || TA.CURRICULUM_CD || '-' || TA.SUBCLASSCD ");
            stb.append("                 ) ");
            stb.append(" ORDER BY ");
            stb.append("   HR_CLASS, ");
            stb.append("   SEMESTER, ");
            stb.append("   CLASSCD, ");
            stb.append("   SORTFLG DESC, ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   ASSESSLEVEL DESC ");
            log.debug("getAssessLevelMap sql:"+ stb.toString());
            final List Lst = KnjDbUtils.query(db2, stb.toString());
            final Map rtn = new LinkedMap();
            Map addwkMap = new LinkedMap();
            for (Iterator ite = Lst.iterator();ite.hasNext();) {
                final Map row = (Map)ite.next();
                final String kStr = (String)row.get("HR_CLASS") + "-" + (String)row.get("SEMESTER") + "-" + (String)row.get("CLASSCD");
                if (rtn.containsKey(kStr)) {
                    addwkMap = (Map)rtn.get(kStr);
                } else {
                    addwkMap = new LinkedMap();
                    rtn.put(kStr, addwkMap);
                }
                addwkMap.put((String)row.get("SUBCLASSCD") + (String)row.get("ASSESSLEVEL"), row);
            }
            return rtn;
        }

        public Map getSubclassAttendance(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String SSEMESTER = "1";
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("hrClass", "?");
            _attendParamMap.put("sSemester", SSEMESTER);
            String edate = _attendDate;
            Map semester = (Map)_semesMap.get(SSEMESTER);
            String sdate = (String)semester.get("SDATE");
            if (sdate.compareTo(edate) < 0) {
                final String sqlAttendSubclass = AttendAccumulate.getAttendSubclassSql(
                        _year,
                        "9",
                        (String)sdate,
                        (String)edate,
                        _attendParamMap
                        );
                final List chkLst = Arrays.asList(_searchedSubclass);
                try {
                    ps = db2.prepareStatement(sqlAttendSubclass);
                    for (int clsCnt = 0;clsCnt < _hrClasses.length;clsCnt++) {
                        ps.setString(1, _hrClasses[clsCnt]);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final String schregno = rs.getString("SCHREGNO");
                            final String semesStr = rs.getString("SEMESTER");
                            final String subclasscd = rs.getString("SUBCLASSCD");
                            final String sick = rs.getString("SICK2");
                            if (chkLst.contains(subclasscd)) {
                                retMap.put(semesStr+ ":" + schregno + ":" + subclasscd, sick);
                            }
                        }
                        if (clsCnt+1 < _hrClasses.length) {  //最終じゃなければ、psを再利用するので、rsのみ閉じる。最後はfinallyで。
                            DbUtils.closeQuietly(rs);
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return retMap;
        }
    }

}
