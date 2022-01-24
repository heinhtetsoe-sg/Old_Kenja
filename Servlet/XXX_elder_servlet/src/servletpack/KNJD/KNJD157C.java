// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2020/03/22
 * 作成者: Nutec
 *
 */
package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪⅮ１５７Ｃ＞  平均点一覧表
 */
public class KNJD157C {

    private static final Log log = LogFactory.getLog(KNJD157C.class);

    private boolean nonedata = false;  //該当データなしフラグ

    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private Param _param = null;
    private String _keikokutenKubun = "";
    private String _printClass = "";
    private final String TOTAL_CLASSCD = "99999";
    private double _totalAvg        = 0;   //全体平均に使用
    private double _maxAvg          = 0;   //最大平均
    private double _minAvg          = 100; //最低平均
    private int _totalCnt           = 0;   //全体人数
    private int _totalYusyuCnt      = 0;   //全体優秀者数
    private int _totalKettenNobeCnt = 0;   //全体欠点科目延べ人数
    private int _totalNobeCnt       = 0;   //全体欠点数延べ人数
    private int _totalTasuCnt       = 0;   //全体欠点多数保有者数

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                       //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter (response.getOutputStream());

            //  svf設定
            svf.VrInit();                                          //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());  //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();                //DBを閉じる
            outstrm.close();            //ストリームを閉じる
        }

    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        log.info(" form = " + form);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(
        final DB2UDB db2,
        final Vrw32alp svf
    ) {

        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        try {
            nonedata = true;

            //日付の和暦変換に使用
            Calendar calendar = Calendar.getInstance();
            calendar.set(toInt(_param._year, 0), 4, 1);
            String dateStr = KNJ_EditDate.gengou(db2, calendar.get(Calendar.YEAR)) + "年度";

            //テスト種別
            sql = sqlTestType();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            String titleName = rs.getString("TESTITEMNAME");
            String score_div = rs.getString("SCORE_DIV");
            String div = "";
            if ("01".equals(score_div)) {
                div = "(素点)";
            } else if ("08".equals(score_div)) {
                div = "(評価)";
            } else if ("09".equals(score_div)) {
                div = "(評定)";
            }
            String title = dateStr + "　" +titleName + "　平均点一覧表" + div;

            //クラス情報
            final List classList = getList(db2);

            //科目
            sql = sqlSubclass();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<subclass> subclass = new ArrayList<subclass>();
            while (rs.next()) {
                subclass.add(new subclass(
                        rs.getString("SUBCLASSCD")
                      , rs.getString("SUBCLASSABBV")
                ));
            }

            //科目毎の点数区分一覧
            sql = sqlsublassScoreCat(_printClass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ArrayList<scoreCat> scoreCat = new ArrayList<scoreCat>();
            while (rs.next()) {
                scoreCat.add(new scoreCat(
                        rs.getString("SUBCLASSCD")
                      , rs.getString("CAT9")
                      , rs.getString("CAT8")
                      , rs.getString("CAT7")
                      , rs.getString("CAT6")
                      , rs.getString("CAT5")
                      , rs.getString("CAT4")
                      , rs.getString("CAT3")
                      , rs.getString("CAT2")
                      , rs.getString("CAT1")
                      , rs.getString("CAT0")
                ));
            }

            //類型（講座グループ）の平均点の一覧
            //学校マスタの警告点区分が「類型平均」の場合のみ印字する
            ArrayList<groupData> groupData = new ArrayList<groupData>();
            if ("2".equals(_keikokutenKubun)) {
                sql = sqlGroupData();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    groupData.add(new groupData(
                            rs.getString("CHAIR_GROUP_CD")
                          , rs.getString("SUBCLASSNAME")
                          , rs.getString("CHAIR_GROUP_NAME")
                          , rs.getString("GROUP_AVG")
                          , rs.getString("GROUP_HIGHSCORE")
                          , rs.getString("GROUP_LOWSCORE")
                          , rs.getString("GROUP_CNT")
                          , rs.getString("HR_CLASS")
                          , rs.getString("AVG")
                          , rs.getString("KETTEN_CNT")
                          , rs.getString("CLASS_CNT")
                    ));
                }
            }

            //帳票に印字

            int subclassSize = 0;
            while (subclassSize <= subclass.size()) {
                svf.VrEndPage();
                setForm(svf, "KNJD157C_1.xml", 1);
                //ヘッダー
                setHeader(svf, subclass, subclassSize, title);

                //クラス
                int classRow = 0;
                final int MAX_CLASS_ROW = 10;

                for (Iterator iterator = classList.iterator(); iterator.hasNext();) {
                    final ClassData classData = (ClassData) iterator.next();
                    classRow++;
                    if (MAX_CLASS_ROW < classRow) {
                        //全体(最後の行)
                        setTotalClassData(svf, classList, subclass, subclassSize);
                        //各段階の人数
                        setSubclassScoreCat(svf, score_div, scoreCat, subclass, subclassSize);
                        svf.VrEndPage();
                        //ヘッダー
                        setHeader(svf, subclass, subclassSize, title);
                        classRow -= MAX_CLASS_ROW;
                    }
                    if (TOTAL_CLASSCD.equals(classData._classcd) == false) {
                        //クラス名
                        svf.VrsOut("HR_NAMEABBV" + classRow, classData._className);

                        //クラス毎の学級平均、最高点、最低点、人数
                        //学級平均
                        if (classData._avgScore != null) {
                            svf.VrsOut("HR_AVE" + classRow, RoundHalfUp(classData._avgScore));
                        }

                        //最高点
                        if (classData._maxAvgScore != null) {
                            svf.VrsOut("HR_MAX_AVE" + classRow, RoundHalfUp(classData._maxAvgScore));
                        }

                        //最低点
                        if (classData._minAvgScore != null) {
                            svf.VrsOut("HR_MIN_AVE" + classRow, RoundHalfUp(classData._minAvgScore));
                        }

                        //人数
                        if (classData._count != null) {
                            svf.VrsOut("HR_NUM" + classRow, classData._count);
                        }

                        //優秀者数
                        if (classData._yusyu != null) {
                            svf.VrsOut("HR_EXELLENT" + classRow, classData._yusyu);
                        }

                        //クラス毎、科目毎の平均点数、人数
                        for (String subclasscd : classData._subclassScoreMap.keySet()) {
                            final subclassData sub = classData._subclassScoreMap.get(subclasscd);
                            for (int subclassRow = subclassSize; subclassRow < subclass.size(); subclassRow++) {
                                String sc = (subclass.get(subclassRow))._subclassCD;
                                if (sc.equals(subclasscd)) {
                                    //ヘッダーの科目と同じ列に印字
                                    //平均
                                    if (sub._avg != null) {
                                        svf.VrsOutn("AVE" + classRow, (subclassRow - subclassSize + 1), RoundHalfUp(sub._avg));
                                    }

                                    //人数
                                    svf.VrsOutn("NUM" + classRow, (subclassRow - subclassSize + 1), sub._count);

                                    //欠点者数
                                    svf.VrsOutn("DEFECT" + classRow, (subclassRow - subclassSize + 1), sub._kettenCount);

                                    break;
                                }
                            }
                        }

                        //欠点数延べ
                        final String nobe = classData._nobe != null ? classData._nobe : "0";
                        final String tasu = (_param._unskilfulFlg == false) ? "-" : classData._tasu != null ? classData._tasu : "0";
                        String note = "延べ" + classData._nobeKosu + "個：" + nobe + "人";
                        note += "(多数保有者" + tasu + "人)";
                        svf.VrsOut("HR_DEFECT_NOTE" + classRow, note);

                    } else {
                        //全体(最後の行)
                        setTotalClassData(svf, classList, subclass, subclassSize);
                    }
                }
                //各段階の人数
                setSubclassScoreCat(svf, score_div, scoreCat, subclass, subclassSize);
                subclassSize += _param._maxSubclass;
            }

            //2ページ目
            //類型グループ
            //学校マスタの警告点区分が「類型平均」の場合のみ印字する
            if ("2".equals(_keikokutenKubun)) {
                svf.VrEndPage();
                setForm(svf, "KNJD157C_2.xml", 1);
                final int MAX_SUBCLASS_ROW = 15;  //1ページ15科目
                String group = "";
                String ch    = "";
                int groupCnt  = 0;
                int kettenCnt = 0;
                int classRow = 0;

                //ヘッダー
                svf.VrsOut("TITLE", title + "類型");
                svf.VrsOutn("HR_NAME3", 1, "類型平均");
                svf.VrsOutn("HR_NAME3", 2, "最高点");
                svf.VrsOutn("HR_NAME3", 3, "最低点");

                //クラス
                for (Iterator iterator = classList.iterator(); iterator.hasNext();) {
                    final ClassData classData = (ClassData) iterator.next();
                    if (TOTAL_CLASSCD.equals(classData._classcd) == false) {
                        classRow++;
                        int classLen = KNJ_EditEdit.getMS932ByteLength(classData._className);
                        final String classField = (classLen <= 4)? "1": (classLen <= 6)? "2": (classLen <= 8)? "3": "4";

                        //クラス名
                        //クラス名の前3列は類型平均、最高点、最低点を印字するため+3している
                        svf.VrsOutn("HR_NAME" + classField, (classRow + 3), classData._className);
                    }
                }
                //類型グループ
                for (int groupRow = 0; groupRow < groupData.size(); groupRow++) {
                    //講座グループが変わる場合
                    if (group.equals((groupData.get(groupRow))._chairGroupCd) == false) {
                        if (groupRow != 0) {
                            //欠点数合計
                            svf.VrsOutn("DEFECT" + groupCnt, 1, String.valueOf(kettenCnt));
                            kettenCnt = 0;

                        }
                        groupCnt++;
                        //1ページ最大科目数
                        if (MAX_SUBCLASS_ROW < groupCnt) {
                            svf.VrEndPage();
                            setForm(svf, "KNJD157C_2.xml", 1);
                            groupCnt = 1;
                        }

                        //科目名
                        svf.VrsOut("SUBCLASS_NAME" + groupCnt, (groupData.get(groupRow))._subClassName);

                        //類型名
                        svf.VrsOut("CHAIR_GROUP_NAME" + groupCnt, (groupData.get(groupRow))._chairGroupName);

                        //類型平均
                        if ((groupData.get(groupRow))._groupAvg != null) {
                            svf.VrsOutn("AVE" + groupCnt, 1, RoundHalfUp((groupData.get(groupRow))._groupAvg));
                        }

                        //グループ最高点
                        if ((groupData.get(groupRow))._groupHighScore != null) {
                            svf.VrsOutn("AVE" + groupCnt, 2, (groupData.get(groupRow))._groupHighScore);
                        }

                        //グループ最低点
                        if ((groupData.get(groupRow))._groupLowScore != null) {
                            svf.VrsOutn("AVE" + groupCnt, 3, (groupData.get(groupRow))._groupLowScore);
                        }

                        //類型人数
                        svf.VrsOutn("NUM" + groupCnt, 1, (groupData.get(groupRow))._groupCnt);

                        //グループを保持
                        group = (groupData.get(groupRow))._chairGroupCd;
                    }
                    //クラスを保持
                    classRow = 0;
                    ch = (groupData.get(groupRow))._hrClass;
                    for (Iterator iterator = classList.iterator(); iterator.hasNext();) {
                        final ClassData classData = (ClassData) iterator.next();
                        if (TOTAL_CLASSCD.equals(classData._classcd) == false) {
                             classRow++;
                            if (ch.equals(classData._classcd)) {
                                //ヘッダーのクラスと同じ列に印字
                                //クラス平均
                                if ((groupData.get(groupRow))._avg != null) {
                                    svf.VrsOutn("AVE" + groupCnt, (classRow + 3), RoundHalfUp((groupData.get(groupRow))._avg));
                                }

                                //クラス欠点者数
                                svf.VrsOutn("DEFECT" + groupCnt, (classRow + 3), (groupData.get(groupRow))._kettenCnt);

                                kettenCnt += toInt((groupData.get(groupRow))._kettenCnt, 0);

                                //クラス人数
                                if ((groupData.get(groupRow))._classCnt != null) {
                                    svf.VrsOutn("NUM" + groupCnt, (classRow + 3), (groupData.get(groupRow))._classCnt);
                                }

                                break;
                            }
                        }
                    }
                }  //groupRowの括り
                //欠点数合計
                svf.VrsOutn("DEFECT" + groupCnt, 1, String.valueOf(kettenCnt));

                kettenCnt = 0;
            }

            nonedata = true;

            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("printSvfMain set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    //ヘッダ
    private void setHeader(final Vrw32alp svf, final ArrayList<subclass> subclass, final int subclassSize, String title) {
        //ヘッダー
        svf.VrsOut("TITLE", title);

        //科目
        for (int subclassRow = subclassSize; subclassRow < subclass.size(); subclassRow++) {
            String sc = (subclass.get(subclassRow))._subclassAbbv;
            int subclassLen = KNJ_EditEdit.getMS932ByteLength(sc);
            final String subclassField = (subclassLen <= 4)? "1": (subclassLen <= 6)? "2": (subclassLen <= 8)? "3": "4";
            svf.VrsOutn("SUBCLASS_NAME" + subclassField, (subclassRow - subclassSize + 1), sc);
        }

        //注釈
        String note = "※優秀者は平均が";
        if(_param._excellentFlg) {
            note += _param._excellentPerson;
        } else {
            note += "-";
        }
        note += "以上の者。欠点は「警告点」又は「評定1となる評価」。多数保有者とは";
        if (_param._unskilfulFlg) {
            note += _param._unskilfulPerson;
        } else {
            note += "-";
        }
        note += "個以上保有する者。";
        svf.VrsOut("NOTICE", note);
    }

    //全体
    private void setTotalClassData(final Vrw32alp svf, final List classList, final ArrayList<subclass> subclass, final int subclassSize) {
        //全体(最後の行)
        final ClassData totalClassData = (ClassData) classList.get(classList.size() - 1);
        svf.VrsOut("HR_NAMEABBV11", totalClassData._className);

        //全体平均
        if (0 < classList.size()) {
            svf.VrsOut("HR_AVE11", RoundHalfUp(String.valueOf(_totalAvg / classList.size())));
        }

        //最高平均
        svf.VrsOut("HR_MAX_AVE11", RoundHalfUp(String.valueOf(_maxAvg)));

        //最低平均
        if (_maxAvg < _minAvg) {
            _minAvg = 0;
        }
        svf.VrsOut("HR_MIN_AVE11", RoundHalfUp(String.valueOf(_minAvg)));

        //全体人数
        svf.VrsOut("HR_NUM11", String.valueOf(_totalCnt));

        //全体優秀者数
        if (_param._excellentFlg == true) {
            svf.VrsOut("HR_EXELLENT11", String.valueOf(_totalYusyuCnt));
        } else {
            svf.VrsOut("HR_EXELLENT11", "-");
        }

        //延べ欠点数、多数保有者
        String totalTasuCnt = "";
        if (_param._unskilfulFlg == true) {
            totalTasuCnt = String.valueOf(_totalTasuCnt);
        } else {
            totalTasuCnt = "-";
        }
        String note = "延べ" + _totalKettenNobeCnt + "個：" + _totalNobeCnt + "人(多数保有者" + totalTasuCnt + "人)";
        svf.VrsOut("HR_DEFECT_NOTE11", note);

        for (String subclasscd : totalClassData._subclassScoreMap.keySet()) {
            final subclassData sub = totalClassData._subclassScoreMap.get(subclasscd);
            for (int subclassRow = subclassSize; subclassRow < subclass.size(); subclassRow++) {
                String sc = (subclass.get(subclassRow))._subclassCD;
                if (sc.equals(subclasscd)) {
                    //ヘッダーの科目と同じ列に印字
                    //平均
                    if (sub._avg != null) {
                        svf.VrsOutn("AVE11",    (subclassRow - subclassSize + 1), RoundHalfUp(sub._avg));
                    }

                    //人数
                    svf.VrsOutn("NUM11",    (subclassRow - subclassSize + 1), sub._count);

                    //欠点者数
                    svf.VrsOutn("DEFECT11", (subclassRow - subclassSize + 1), sub._kettenCount);

                    break;
                }
            }
        }
    }

    //各段階の人数
    private void setSubclassScoreCat(final Vrw32alp svf, final String score_div, final ArrayList<scoreCat> scoreCat, final ArrayList<subclass> subclass, final int subclassSize) {
        //評価、評定以外の場合
        if (!("08".equals(score_div)) && !("09".equals(score_div))) {
            //各段階の人数(科目毎の点数区分一覧)
            for (int scoreCatRow = 0; scoreCatRow < scoreCat.size(); scoreCatRow++) {
                //科目を保持
                String sc = (scoreCat.get(scoreCatRow))._subclassCD;
                for (int subclassRow = subclassSize; subclassRow < subclass.size(); subclassRow++) {
                    if (sc.equals((subclass.get(subclassRow))._subclassCD)) {
                        //ヘッダーの科目と同じ列に印字(点数区分10個)
                        svf.VrsOutn("DIV_NUM" +  1, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat9);
                        svf.VrsOutn("DIV_NUM" +  2, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat8);
                        svf.VrsOutn("DIV_NUM" +  3, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat7);
                        svf.VrsOutn("DIV_NUM" +  4, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat6);
                        svf.VrsOutn("DIV_NUM" +  5, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat5);
                        svf.VrsOutn("DIV_NUM" +  6, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat4);
                        svf.VrsOutn("DIV_NUM" +  7, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat3);
                        svf.VrsOutn("DIV_NUM" +  8, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat2);
                        svf.VrsOutn("DIV_NUM" +  9, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat1);
                        svf.VrsOutn("DIV_NUM" + 10, (subclassRow - subclassSize + 1), (scoreCat.get(scoreCatRow))._cat0);
                    }
                }
            }
        }
    }

    //小数第二位を四捨五入
    private String RoundHalfUp(String str) {
        BigDecimal bg = new BigDecimal(str);
        bg = bg.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.format("%.1f", bg);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sqlClass();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final ClassData classData = new ClassData();

                classData._classcd   = rs.getString("HR_CLASS");     //クラス
                classData._className = rs.getString("HR_NAMEABBV");  //クラス名

                classData.setData(db2);
                retList.add(classData);

                _printClass += "'" + classData._classcd + "', ";
                if (classData._keikokutenKubun != null) {
                    _keikokutenKubun = classData._keikokutenKubun;
                }

                if (classData._avgScore != null) {
                    _totalAvg += Double.parseDouble(classData._avgScore);
                }
                if (   classData._maxAvgScore != null
                    && _maxAvg < Double.parseDouble(classData._maxAvgScore)) {
                    _maxAvg = Double.parseDouble(classData._maxAvgScore);
                }
                if (   classData._minAvgScore != null
                    && Double.parseDouble(classData._minAvgScore) < _minAvg) {
                    _minAvg = Double.parseDouble(classData._minAvgScore);
                }
                _totalCnt      += toInt(classData._count, 0);
                _totalYusyuCnt += toInt(classData._yusyu, 0);
                _totalNobeCnt  += toInt(classData._nobe,  0);
                _totalTasuCnt  += toInt(classData._tasu,  0);
            }

            final ClassData classData = new ClassData();

            classData._classcd   = TOTAL_CLASSCD;  //クラス
            classData._className = "全体";         //クラス名
            _printClass = _printClass.substring(0, _printClass.length() - 2);
            classData.setSubclassScoreData(db2, 1, _printClass);
            retList.add(classData);

        } catch (SQLException ex) {
            log.debug("getListErrorException:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /**年度、クラス**/
    private String sqlClass() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT YEAR ");
        stb.append("       , GRADE ");
        stb.append("       , HR_CLASS ");
        stb.append("       , HR_NAMEABBV ");      //クラス名
        stb.append("    FROM SCHREG_REGD_HDAT ");
        stb.append("   WHERE YEAR  = '" + _param._year + "' ");
        stb.append("     AND GRADE = '" + _param._grade + "' ");
        stb.append("GROUP BY YEAR ");
        stb.append("       , GRADE ");
        stb.append("       , HR_CLASS ");
        stb.append("       , HR_NAMEABBV ");
        stb.append("ORDER BY HR_CLASS ");

        return stb.toString();
    }

    /**テスト種別**/
    private String sqlTestType() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT T1.TESTITEMNAME ");
        stb.append("      , T1.SCORE_DIV ");
        stb.append("   FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1");
        stb.append("  WHERE T1.YEAR         = '" + _param._year + "' ");
        stb.append("    AND T1.SEMESTER     = '" + _param._semester + "' ");
        stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");

        return stb.toString();
    }

    /**科目**/
    private String sqlSubclass() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH RASD AS ( ");
        stb.append("     SELECT YEAR ");
        stb.append("          , SUBCLASSCD ");
        stb.append("          , CURRICULUM_CD ");
        stb.append("       FROM RECORD_AVERAGE_SDIV_DAT ");
        stb.append("      WHERE YEAR     = '" + _param._year + "' ");
        stb.append("        AND SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("        AND GRADE    = '" + _param._grade + "' ");
        stb.append("        AND AVG_DIV  = '2' ");
        stb.append("   GROUP BY YEAR ");
        stb.append("          , SUBCLASSCD ");
        stb.append("          , CURRICULUM_CD ");
        stb.append(") ");
        stb.append("     SELECT RASD.YEAR ");
        stb.append("          , RASD.SUBCLASSCD ");
        stb.append("          , SM.SUBCLASSABBV ");  //科目名
        stb.append("     FROM RASD ");
        stb.append("LEFT JOIN SUBCLASS_MST SM ");
        stb.append("       ON SM.SUBCLASSCD    = RASD.SUBCLASSCD ");
        stb.append("      AND SM.CURRICULUM_CD = RASD.CURRICULUM_CD ");
        stb.append("    WHERE SM.SUBCLASSCD IS NOT NULL ");

        return stb.toString();
    }

    /**各段階の人数(科目毎の点数区分一覧)**/
    private String sqlsublassScoreCat(String printClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SRD AS ( ");
        stb.append("     SELECT YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , GRADE ");
        stb.append("          , HR_CLASS ");
        stb.append("       FROM SCHREG_REGD_DAT ");
        stb.append("   GROUP BY YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , GRADE ");
        stb.append("          , HR_CLASS ");
        stb.append("), BASE AS ( ");
        stb.append("     SELECT RSD.YEAR ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , RSD.SCHREGNO ");
        stb.append("          , RSD.SUBCLASSCD ");
        stb.append("          , RSD.SCORE ");
        stb.append("       FROM RECORD_SCORE_DAT RSD ");
        stb.append("  LEFT JOIN SRD ");
        stb.append("         ON SRD.YEAR        = RSD.YEAR  ");
        stb.append("        AND SRD.SCHREGNO    = RSD.SCHREGNO ");
        stb.append("      WHERE RSD.YEAR        = '" + _param._year + "' ");
        stb.append("        AND RSD.SEMESTER    = '" + _param._semester + "' ");
        stb.append("        AND RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("        AND SRD.GRADE       = '" + _param._grade + "' ");
        stb.append("        AND (   RSD.VALUE_DI IS NULL ");
        stb.append("             OR RSD.VALUE_DI <> '*') ");
        stb.append("        AND SRD.HR_CLASS IN (" + printClass + ")");
        stb.append("   ORDER BY RSD.YEAR ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , RSD.SCHREGNO ");
        stb.append("          , RSD.SUBCLASSCD ");
        stb.append("), CAT AS ( ");
        stb.append("SELECT BASE.YEAR ");
        stb.append("     , BASE.SCHREGNO ");
        stb.append("     , BASE.SUBCLASSCD ");
        stb.append("     , CASE WHEN 90 <= BASE.SCORE ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT9 ");
        stb.append("     , CASE WHEN 80 <= BASE.SCORE AND BASE.SCORE < 90 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT8 ");
        stb.append("     , CASE WHEN 70 <= BASE.SCORE AND BASE.SCORE < 80 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT7 ");
        stb.append("     , CASE WHEN 60 <= BASE.SCORE AND BASE.SCORE < 70 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT6 ");
        stb.append("     , CASE WHEN 50 <= BASE.SCORE AND BASE.SCORE < 60 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT5 ");
        stb.append("     , CASE WHEN 40 <= BASE.SCORE AND BASE.SCORE < 50 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT4 ");
        stb.append("     , CASE WHEN 30 <= BASE.SCORE AND BASE.SCORE < 40 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT3 ");
        stb.append("     , CASE WHEN 20 <= BASE.SCORE AND BASE.SCORE < 30 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT2 ");
        stb.append("     , CASE WHEN 10 <= BASE.SCORE AND BASE.SCORE < 20 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT1 ");
        stb.append("     , CASE WHEN BASE.SCORE < 10 ");
        stb.append("            THEN 1 ");
        stb.append("            ELSE 0 ");
        stb.append("       END CAT0 ");
        stb.append("  FROM BASE ");
        stb.append(") ");
        stb.append("  SELECT CAT.YEAR ");
        stb.append("       , CAT.SUBCLASSCD ");
        stb.append("       , SUM(CAT.CAT9) AS CAT9 ");
        stb.append("       , SUM(CAT.CAT8) AS CAT8 ");
        stb.append("       , SUM(CAT.CAT7) AS CAT7 ");
        stb.append("       , SUM(CAT.CAT6) AS CAT6 ");
        stb.append("       , SUM(CAT.CAT5) AS CAT5 ");
        stb.append("       , SUM(CAT.CAT4) AS CAT4 ");
        stb.append("       , SUM(CAT.CAT3) AS CAT3 ");
        stb.append("       , SUM(CAT.CAT2) AS CAT2 ");
        stb.append("       , SUM(CAT.CAT1) AS CAT1 ");
        stb.append("       , SUM(CAT.CAT0) AS CAT0 ");
        stb.append("    FROM CAT ");
        stb.append("GROUP BY CAT.YEAR ");
        stb.append("       , CAT.SUBCLASSCD ");
        stb.append("ORDER BY CAT.YEAR ");
        stb.append("       , CAT.SUBCLASSCD ");

        return stb.toString();
    }

    /**類型（講座グループ）の平均点の一覧**/
    private String sqlGroupData() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH CHAIR_GROUP1 AS ( ");
        stb.append("     SELECT * ");
        stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
        stb.append("      WHERE TESTKINDCD = '00' ");
        stb.append("        AND TESTITEMCD = '00' ");
        stb.append("        AND SCORE_DIV  = '00' ");
        stb.append("), CHAIR_GROUP2 AS ( ");
        stb.append("     SELECT * ");
        stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
        stb.append("      WHERE TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("), CHAIR_GROUP AS ( ");
        //優先するCHAIR_GROUP_CDを取得
        stb.append("   SELECT CD.YEAR ");
        stb.append("        , CD.SEMESTER ");
        stb.append("        , CD.CHAIRCD ");
        stb.append("        , CASE WHEN CHAIR_GROUP1.CHAIR_GROUP_CD IS NOT NULL ");
        stb.append("               THEN CHAIR_GROUP1.CHAIR_GROUP_CD ");
        stb.append("               ELSE CHAIR_GROUP2.CHAIR_GROUP_CD ");
        stb.append("          END CHAIR_GROUP_CD ");
        stb.append("     FROM CHAIR_DAT CD ");
        stb.append("LEFT JOIN CHAIR_GROUP1 ");
        stb.append("       ON CD.YEAR     = CHAIR_GROUP1.YEAR ");
        stb.append("      AND CD.SEMESTER = CHAIR_GROUP1.SEMESTER ");
        stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP1.CHAIRCD ");
        stb.append("LEFT JOIN CHAIR_GROUP2 ");
        stb.append("       ON CD.YEAR     = CHAIR_GROUP2.YEAR ");
        stb.append("      AND CD.SEMESTER = CHAIR_GROUP2.SEMESTER ");
        stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP2.CHAIRCD ");
        stb.append("    WHERE CD.YEAR     = '" + _param._year + "' ");
        stb.append("      AND CD.SEMESTER = '" + _param._semester + "' ");
        stb.append("), BASE1 AS ( ");
        stb.append("   SELECT SRD.HR_CLASS ");
        stb.append("        , RSD.SCHREGNO ");
        stb.append("        , CGD.CHAIR_GROUP_CD ");
        stb.append("        , SM.SUBCLASSCD ");
        stb.append("        , SM.SUBCLASSNAME ");
        stb.append("        , CGM.CHAIR_GROUP_NAME  ");
        stb.append("        , COUNT(RSD.SCORE) AS COUNT ");
        stb.append("        , RSD.SCORE ");
        stb.append("        , RASD.AVG * SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 AS KEIKOKUTEN ");
        stb.append("        , RASD.AVG AS GROUP_AVG ");
        stb.append("        , RASD.HIGHSCORE AS GROUP_HIGHSCORE ");
        stb.append("        , RASD.LOWSCORE  AS GROUP_LOWSCORE ");
        stb.append("        , RASD.COUNT AS GROUP_CNT ");
        stb.append("        , RASD2.COUNT AS CLASS_CNT ");
        stb.append("     FROM CHAIR_GROUP_MST CGM ");
        stb.append("LEFT JOIN CHAIR_GROUP CGD ");
        stb.append("       ON CGM.YEAR            = CGD.YEAR ");
        stb.append("      AND CGM.SEMESTER        = CGD.SEMESTER ");
        stb.append("      AND CGM.CHAIR_GROUP_CD  = CGD.CHAIR_GROUP_CD ");
        stb.append("LEFT JOIN SUBCLASS_MST SM ");
        stb.append("       ON SM.CLASSCD          = CGM.CLASSCD ");
        stb.append("      AND SM.SCHOOL_KIND      = CGM.SCHOOL_KIND ");
        stb.append("      AND SM.SUBCLASSCD       = CGM.SUBCLASSCD ");
        stb.append("      AND SM.CURRICULUM_CD    = CGM.CURRICULUM_CD ");
        stb.append("LEFT JOIN CHAIR_CLS_DAT CCD ");
        stb.append("       ON CCD.YEAR            = CGD.YEAR ");
        stb.append("      AND CCD.SEMESTER        = CGD.SEMESTER ");
        stb.append("      AND CCD.CHAIRCD         = CGD.CHAIRCD ");
        stb.append("LEFT JOIN RECORD_SCORE_DAT RSD ");
        stb.append("       ON RSD.YEAR            = CGD.YEAR ");
        stb.append("      AND RSD.SEMESTER        = CGD.SEMESTER ");
        stb.append("      AND RSD.SUBCLASSCD      = CGM.SUBCLASSCD ");
        stb.append("LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("       ON SRD.YEAR            = CGD.YEAR ");
        stb.append("      AND SRD.SEMESTER        = CGD.SEMESTER ");
        stb.append("      AND SRD.SCHREGNO        = RSD.SCHREGNO ");
        stb.append("      AND SRD.GRADE           = CCD.TRGTGRADE ");
        stb.append("      AND SRD.HR_CLASS        = CCD.TRGTCLASS ");
        stb.append("LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
        stb.append("       ON SDD.YEAR            = CGD.YEAR ");
        stb.append("      AND SDD.SCHOOLCD        = '000000000000' ");
        stb.append("      AND SDD.SCHOOL_SEQ      = '009' ");
        stb.append("      AND SDD.SCHOOL_REMARK1  = '2' ");
        stb.append("      AND SDD.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
        stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
        stb.append("       ON RASD.YEAR           = CGD.YEAR ");
        stb.append("      AND RASD.SEMESTER       = CGD.SEMESTER ");
        stb.append("      AND RASD.TESTKINDCD     = RSD.TESTKINDCD ");
        stb.append("      AND RASD.TESTITEMCD     = RSD.TESTITEMCD ");
        stb.append("      AND RASD.SCORE_DIV      = RSD.SCORE_DIV ");
        stb.append("      AND RASD.SCHOOL_KIND    = RSD.SCHOOL_KIND ");
        stb.append("      AND RASD.MAJORCD        = CGD.CHAIR_GROUP_CD ");
        stb.append("      AND RASD.AVG_DIV        = '6' ");
        stb.append("      AND RASD.HR_CLASS       = '000' ");
        stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD2 ");
        stb.append("       ON RASD2.YEAR          = CGD.YEAR ");
        stb.append("      AND RASD2.SEMESTER      = CGD.SEMESTER ");
        stb.append("      AND RASD2.TESTKINDCD    = RSD.TESTKINDCD ");
        stb.append("      AND RASD2.TESTITEMCD    = RSD.TESTITEMCD ");
        stb.append("      AND RASD2.SCORE_DIV     = RSD.SCORE_DIV ");
        stb.append("      AND RASD2.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
        stb.append("      AND RASD2.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("      AND RASD2.SUBCLASSCD    = CGM.SUBCLASSCD ");
        stb.append("      AND RASD2.GRADE         = SRD.GRADE ");
        stb.append("      AND RASD2.HR_CLASS      = SRD.HR_CLASS ");
        stb.append("      AND RASD2.AVG_DIV       = '2' ");
        stb.append("    WHERE RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("      AND SRD.GRADE           = '" + _param._grade + "' ");
        stb.append("      AND RSD.SCHOOL_KIND     = '" + _param._schoolkind + "' ");
        stb.append(" GROUP BY SRD.HR_CLASS ");
        stb.append("        , RSD.SCHREGNO ");
        stb.append("        , CGD.CHAIR_GROUP_CD ");
        stb.append("        , SM.SUBCLASSCD ");
        stb.append("        , SM.SUBCLASSNAME ");
        stb.append("        , CGM.CHAIR_GROUP_NAME  ");
        stb.append("        , RASD.AVG ");
        stb.append("        , RASD.HIGHSCORE ");
        stb.append("        , RASD.LOWSCORE ");
        stb.append("        , SDD.SCHOOL_REMARK2/SDD.SCHOOL_REMARK3 ");
        stb.append("        , RSD.SCORE ");
        stb.append("        , RASD.AVG * SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 ");
        stb.append("        , RASD.COUNT ");
        stb.append("        , RASD2.COUNT ");
        stb.append(" ORDER BY SRD.HR_CLASS ");
        stb.append("        , RSD.SCHREGNO ");
        stb.append("        , CGD.CHAIR_GROUP_CD ");
        stb.append("), BASE2 AS ( ");
        stb.append("   SELECT BASE1.* ");
        stb.append("        , CASE WHEN BASE1.SCORE <= BASE1.KEIKOKUTEN ");
        stb.append("               THEN 1 ");
        stb.append("               ELSE 0 ");
        stb.append("          END KETTEN_CNT ");
        stb.append("     FROM BASE1 ");
        stb.append("    WHERE 0 < BASE1.COUNT ");
        stb.append("), MAIN1 AS ( ");
        stb.append("   SELECT BASE2.HR_CLASS ");
        stb.append("        , BASE2.CHAIR_GROUP_CD ");
        stb.append("        , SUM(BASE2.KETTEN_CNT) AS KETTEN_CNT ");
        stb.append("        , CLASS_CNT ");
        stb.append("     FROM BASE2 ");
        stb.append(" GROUP BY BASE2.HR_CLASS ");
        stb.append("        , BASE2.CHAIR_GROUP_CD ");
        stb.append("        , BASE2.CLASS_CNT ");
        stb.append("), MAIN2 AS ( ");
        stb.append("   SELECT BASE2.CHAIR_GROUP_CD ");
        stb.append("        , BASE2.SUBCLASSCD ");
        stb.append("        , BASE2.SUBCLASSNAME ");
        stb.append("        , BASE2.CHAIR_GROUP_NAME ");
        stb.append("        , BASE2.GROUP_CNT ");
        stb.append("        , BASE2.GROUP_AVG ");
        stb.append("        , BASE2.GROUP_HIGHSCORE ");
        stb.append("        , BASE2.GROUP_LOWSCORE ");
        stb.append("     FROM BASE2 ");
        stb.append(" GROUP BY BASE2.CHAIR_GROUP_CD ");
        stb.append("        , BASE2.SUBCLASSCD ");
        stb.append("        , BASE2.SUBCLASSNAME ");
        stb.append("        , BASE2.CHAIR_GROUP_NAME ");
        stb.append("        , BASE2.GROUP_CNT ");
        stb.append("        , BASE2.GROUP_AVG ");
        stb.append("        , BASE2.GROUP_HIGHSCORE ");
        stb.append("        , BASE2.GROUP_LOWSCORE ");
        stb.append("), MAIN3 AS ( ");
        stb.append("   SELECT BASE2.CHAIR_GROUP_CD ");
        stb.append("        , BASE2.HR_CLASS ");
        stb.append("        , to_char(SUM(NVL(BASE2.SCORE, 0)), '99999D9') / BASE2.CLASS_CNT AS AVG ");
        stb.append("     FROM BASE2 ");
        stb.append(" GROUP BY BASE2.CHAIR_GROUP_CD ");
        stb.append("        , BASE2.HR_CLASS ");
        stb.append("        , BASE2.CLASS_CNT ");
        stb.append(") ");
        stb.append("   SELECT MAIN1.CHAIR_GROUP_CD ");    //講座グループCD
        stb.append("        , MAIN2.SUBCLASSNAME ");      //講座科目名
        stb.append("        , MAIN2.CHAIR_GROUP_NAME ");  //講座グループ名
        stb.append("        , MAIN2.GROUP_AVG ");         //講座グループ平均
        stb.append("        , MAIN2.GROUP_HIGHSCORE ");   //講座グループ最高点
        stb.append("        , MAIN2.GROUP_LOWSCORE ");    //講座グループ最低点
        stb.append("        , MAIN2.GROUP_CNT ");         //講座グループ人数
        stb.append("        , MAIN1.HR_CLASS ");          //クラス
        stb.append("        , MAIN3.AVG ");               //クラス平均
        stb.append("        , MAIN1.KETTEN_CNT ");        //クラス欠点者人数
        stb.append("        , MAIN1.CLASS_CNT ");         //クラス人数
        stb.append("     FROM MAIN1 ");
        stb.append("LEFT JOIN MAIN2 ");
        stb.append("       ON MAIN1.CHAIR_GROUP_CD = MAIN2.CHAIR_GROUP_CD ");
        stb.append("LEFT JOIN MAIN3 ");
        stb.append("       ON MAIN1.CHAIR_GROUP_CD = MAIN3.CHAIR_GROUP_CD ");
        stb.append("      AND MAIN1.HR_CLASS       = MAIN3.HR_CLASS ");
        stb.append(" ORDER BY MAIN2.SUBCLASSCD ");
        stb.append("        , MAIN1.CHAIR_GROUP_CD ");
        stb.append("        , MAIN1.HR_CLASS ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private class ClassData {
        String _classcd;          //クラスCD
        String _className;        //クラス名
        String _avgScore;         //学級平均
        String _maxAvgScore;      //最高平均点
        String _minAvgScore;      //最低平均点
        String _count;            //人数
        String _yusyu;            //優秀者数
        String _keikokutenKubun;  //警告点区分
        String _nobe;             //欠点数延べ人数
        String _tasu;             //欠点多数保有者数
        final Map<String, subclassData> _subclassScoreMap = new TreeMap();
        int _nobeKosu;           //欠点数延べ個数

        private void setData(final DB2UDB db2) {
            setClassData(db2);
            setSubclassScoreData(db2, 0, "'" + _classcd + "'");
            setKettensuNobe(db2);
        }

        public void setClassData(final DB2UDB db2) {
            String classSql = sqlClassData();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(classSql);
                rs = ps.executeQuery();

                //クラス毎の学級平均、最高点、最低点、人数
                while (rs.next()) {
                    _avgScore    = (rs.getString("AVG"));       //学級平均
                    _maxAvgScore = (rs.getString("MAX"));       //最高平均点
                    _minAvgScore = (rs.getString("MIN"));       //最低平均点
                    _count       = (rs.getString("COUNT"));     //人数
                    _yusyu       = (rs.getString("YUSYU"));     //優秀者数
                }
            } catch (SQLException e) {
                log.error("setClassData error!", e);
            }
        }

        public void setSubclassScoreData(final DB2UDB db2, int flg, String printClass) {
            String subclassSql = sqlSubclassScore(flg, printClass);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();

                //クラス毎、科目毎の平均点数、人数
                while (rs.next()) {
                    final String subclasscd  = (rs.getString("SUBCLASSCD"));    //科目CD
                    final String avg          = (rs.getString("AVG"));           //平均
                    final String count        = (rs.getString("COUNT"));         //人数
                    final String kettenCount = (rs.getString("KETTENCOUNT"));   //欠点者数

                    if (subclasscd != null) {
                        _subclassScoreMap.put(subclasscd, new subclassData(avg, count, kettenCount));
                    }

                    if ((TOTAL_CLASSCD.equals(_classcd))) {
                        _totalKettenNobeCnt += toInt(kettenCount, 0);
                    } else {
                        _nobeKosu += toInt(kettenCount, 0);
                    }

                    if (flg == 0) {
                        _keikokutenKubun = rs.getString("SCHOOL_REMARK1");
                    }
                }
            } catch (SQLException e) {
                log.error("setSubclassScoreData error!", e);
            }
        }

        public void setKettensuNobe(final DB2UDB db2) {
            String kettenSql = sqlKetten();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(kettenSql);
                rs = ps.executeQuery();

                //欠点数延べ人数、欠点多数保有者数
                while (rs.next()) {
                    _nobe = (rs.getString("NOBE"));      //欠点数延べ人数
                    _tasu = (rs.getString("TASU"));      //欠点多数保有者数
                }
            } catch (SQLException e) {
                log.error("setKettensuNobe error!", e);
            }
        }

        /**クラス毎の学級平均、最高点、最低点、人数、優秀者数**/
        private String sqlClassData() {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SRD AS ( ");
            stb.append("       SELECT YEAR ");
            stb.append("            , GRADE ");
            stb.append("            , HR_CLASS ");
            stb.append("            , SCHREGNO ");
            stb.append("         FROM SCHREG_REGD_DAT ");
            stb.append("        WHERE YEAR     = '" + _param._year + "' ");
            stb.append("          AND GRADE    = '" + _param._grade + "' ");
            stb.append("     GROUP BY YEAR ");
            stb.append("            , GRADE ");
            stb.append("            , HR_CLASS ");
            stb.append("            , SCHREGNO ");
            stb.append("),  SCHREG_SCORE_AVG AS ( ");
            stb.append("       SELECT SRD.YEAR ");
            stb.append("            , RSD.SEMESTER ");
            stb.append("            , SRD.GRADE ");
            stb.append("            , SRD.HR_CLASS ");
            stb.append("            , SRD.SCHREGNO ");
            stb.append("            , RSD.TESTKINDCD ");
            stb.append("            , RSD.TESTITEMCD ");
            stb.append("            , RSD.SCORE_DIV ");
            stb.append("            , SUM(NVL(RSD.SCORE, 0)) AS SUM ");
            stb.append("            , COUNT(RSD.SCORE) AS COUNT ");
            stb.append("            , CASE COUNT(SCORE) WHEN 0 THEN 0 ");
            stb.append("                   ELSE to_char(SUM(NVL(RSD.SCORE, 0)), '99999D9') / COUNT(RSD.SCORE)  ");
            stb.append("              END AS AVG ");
            stb.append("         FROM SRD ");
            stb.append("    LEFT JOIN RECORD_SCORE_DAT RSD ");
            stb.append("           ON SRD.SCHREGNO = RSD.SCHREGNO ");
            stb.append("          AND SRD.YEAR     = RSD.YEAR ");
            stb.append("        WHERE RSD.SEMESTER = '" + _param._semester + "' ");
            stb.append("          AND RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("          AND (   RSD.VALUE_DI IS NULL ");
            stb.append("               OR RSD.VALUE_DI <> '*') ");
            stb.append("    GROUP BY SRD.YEAR ");
            stb.append("           , RSD.SEMESTER ");
            stb.append("           , SRD.GRADE ");
            stb.append("           , SRD.HR_CLASS ");
            stb.append("           , RSD.TESTKINDCD ");
            stb.append("           , RSD.TESTITEMCD ");
            stb.append("           , RSD.SCORE_DIV ");
            stb.append("           , SRD.SCHREGNO ");
            stb.append("), CR_CLASS_COUNT AS ( ");
            stb.append("     SELECT YEAR ");
            stb.append("          , SEMESTER ");
            stb.append("          , GRADE ");
            stb.append("          , HR_CLASS ");
            stb.append("          , COUNT(*) AS COUNT ");
            stb.append("       FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR ");
            stb.append("          , SEMESTER ");
            stb.append("          , GRADE ");
            stb.append("          , HR_CLASS ");
            if (_param._excellentFlg == true) {
                stb.append("), YUSYU AS ( ");
                stb.append("     SELECT SSA.YEAR ");
                stb.append("          , SSA.HR_CLASS ");
                stb.append("          , COUNT(*) AS COUNT ");
                stb.append("       FROM SCHREG_SCORE_AVG SSA ");
                stb.append("      WHERE " + _param._excellentPerson + " <= SSA.AVG ");
                stb.append("   GROUP BY SSA.YEAR ");
                stb.append("          , SSA.HR_CLASS ");
            }
            stb.append(") ");
            stb.append("   SELECT RASD.YEAR ");
            stb.append("        , RASD.HR_CLASS ");
            stb.append("        , RACSD.AVG    AS AVG ");  //学級平均
            stb.append("        , MAX(SSA.AVG) AS MAX ");  //最高平均点
            stb.append("        , MIN(SSA.AVG) AS MIN ");  //最低平均点
            stb.append("        , CCC.COUNT    AS COUNT"); //人数
            if (_param._excellentFlg == true) {
                stb.append("        , NVL(YUSYU.COUNT, 0) AS YUSYU ");  //優秀者数
            } else {
                stb.append("        , '-' AS YUSYU ");          //優秀者数
            }
            stb.append("     FROM RECORD_AVERAGE_SDIV_DAT RASD ");
            stb.append("LEFT JOIN SCHREG_SCORE_AVG SSA ");
            stb.append("       ON SSA.YEAR         = RASD.YEAR ");
            stb.append("      AND SSA.SEMESTER     = RASD.SEMESTER ");
            stb.append("      AND SSA.GRADE        = RASD.GRADE ");
            stb.append("      AND SSA.HR_CLASS     = RASD.HR_CLASS ");
            stb.append("      AND SSA.TESTKINDCD   = RASD.TESTKINDCD ");
            stb.append("      AND SSA.TESTITEMCD   = RASD.TESTITEMCD ");
            stb.append("      AND SSA.SCORE_DIV    = RASD.SCORE_DIV ");
            stb.append("LEFT JOIN CR_CLASS_COUNT CCC ");
            stb.append("       ON CCC.YEAR         = RASD.YEAR ");
            stb.append("      AND CCC.SEMESTER     = RASD.SEMESTER ");
            stb.append("      AND CCC.GRADE        = RASD.GRADE ");
            stb.append("      AND CCC.HR_CLASS     = RASD.HR_CLASS ");
            if (_param._excellentFlg == true) {
                stb.append("LEFT JOIN YUSYU ");
                stb.append("       ON YUSYU.YEAR       = RASD.YEAR ");
                stb.append("      AND YUSYU.HR_CLASS   = RASD.HR_CLASS ");
            }
            stb.append("LEFT JOIN RECORD_AVERAGE_CLASS_SDIV_DAT RACSD ");
            stb.append("       ON RACSD.YEAR         = RASD.YEAR ");
            stb.append("      AND RACSD.SEMESTER     = RASD.SEMESTER ");
            stb.append("      AND RACSD.TESTKINDCD   = RASD.TESTKINDCD ");
            stb.append("      AND RACSD.TESTITEMCD   = RASD.TESTITEMCD ");
            stb.append("      AND RACSD.SCORE_DIV    = RASD.SCORE_DIV ");
            stb.append("      AND RACSD.GRADE        = RASD.GRADE ");
            stb.append("      AND RACSD.HR_CLASS     = RASD.HR_CLASS ");
            stb.append("      AND RACSD.SCHOOL_KIND  = '" + _param._schoolkind + "' ");
            stb.append("      AND RACSD.CLASSCD      = '00' ");
            stb.append("      AND RACSD.COURSECD     = '0' ");
            stb.append("      AND RACSD.MAJORCD      = '000' ");
            stb.append("      AND RACSD.COURSECODE   = '0000' ");
            stb.append("      AND RACSD.AVG_DIV      = '2' ");
            stb.append("      AND RACSD.CLASS_DIV    = '9' ");
            stb.append("    WHERE RASD.SCHOOL_KIND   = '" + _param._schoolkind + "' ");
            stb.append("      AND RASD.SUBCLASSCD    = '999999' ");
            stb.append("      AND RASD.AVG_DIV       = '2' ");  //平均区分 1：学年、2：クラス、3：コース、4：学科、5：コースグループ
            stb.append("      AND 0                  < SSA.COUNT ");
            stb.append("      AND RASD.HR_CLASS      = '" + _classcd  + "' ");
            stb.append(" GROUP BY RASD.YEAR ");
            stb.append("        , RASD.HR_CLASS ");
            stb.append("        , RACSD.AVG ");
            stb.append("        , CCC.COUNT ");
            if (_param._excellentFlg == true) {
                stb.append("        , YUSYU.COUNT ");
            }
            stb.append(" ORDER BY RASD.YEAR ");
            stb.append("        , RASD.HR_CLASS ");

            return stb.toString();
        }

        /**クラス毎、科目毎の平均点数、人数、欠点者数**/
        //f = 0 : クラスごと
        //f = 1 : 学年全体
        private String sqlSubclassScore(int f, String printClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH CHAIR_GROUP1 AS ( ");
            stb.append("     SELECT * ");
            stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
            stb.append("      WHERE TESTKINDCD = '00' ");
            stb.append("        AND TESTITEMCD = '00' ");
            stb.append("        AND SCORE_DIV  = '00' ");
            stb.append("), CHAIR_GROUP2 AS ( ");
            stb.append("     SELECT * ");
            stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
            stb.append("      WHERE TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("), CHAIR_GROUP AS ( ");
            //優先するCHAIR_GROUP_CDを取得
            stb.append("   SELECT CD.YEAR ");
            stb.append("        , CD.SEMESTER ");
            stb.append("        , CD.CHAIRCD ");
            stb.append("        , CASE WHEN CHAIR_GROUP1.CHAIR_GROUP_CD IS NOT NULL ");
            stb.append("               THEN CHAIR_GROUP1.CHAIR_GROUP_CD ");
            stb.append("               ELSE CHAIR_GROUP2.CHAIR_GROUP_CD ");
            stb.append("          END CHAIR_GROUP_CD ");
            stb.append("     FROM CHAIR_DAT CD ");
            stb.append("LEFT JOIN CHAIR_GROUP1 ");
            stb.append("       ON CD.YEAR     = CHAIR_GROUP1.YEAR ");
            stb.append("      AND CD.SEMESTER = CHAIR_GROUP1.SEMESTER ");
            stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP1.CHAIRCD ");
            stb.append("LEFT JOIN CHAIR_GROUP2 ");
            stb.append("       ON CD.YEAR     = CHAIR_GROUP2.YEAR ");
            stb.append("      AND CD.SEMESTER = CHAIR_GROUP2.SEMESTER ");
            stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP2.CHAIRCD ");
            stb.append("    WHERE CD.YEAR     = '" + _param._year + "' ");
            stb.append("      AND CD.SEMESTER = '" + _param._semester + "' ");
            stb.append("), SRD AS ( ");
            stb.append("     SELECT YEAR ");
            stb.append("          , SCHREGNO ");
            stb.append("          , GRADE ");
            stb.append("          , HR_CLASS ");
            stb.append("       FROM SCHREG_REGD_DAT ");
            stb.append("   GROUP BY YEAR ");
            stb.append("          , SCHREGNO ");
            stb.append("          , GRADE ");
            stb.append("          , HR_CLASS ");
            stb.append("), BASE AS ( ");
            stb.append("     SELECT RSD.YEAR ");
            stb.append("          , SRD.HR_CLASS ");
            stb.append("          , RSD.SCHREGNO  ");
            stb.append("          , RSD.SUBCLASSCD ");
            stb.append("          , SDD.SCHOOL_REMARK1 ");
            stb.append("          , RSD.SCORE ");
            stb.append("          , CASE WHEN SDD.SCHOOL_REMARK1 = '1' THEN CDD.REMARK1 ");
            stb.append("                 WHEN SDD.SCHOOL_REMARK1 = '2' THEN RASD.AVG * SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 ");  //類型グループの平均点を使用
            stb.append("                 ELSE( CASE WHEN RSD.SCORE_DIV = '09' THEN 1 ");  //評定の場合、警告点は1
            stb.append("                            ELSE (SELECT ASSESSHIGH FROM ASSESS_MST WHERE ASSESSCD = '2' AND ASSESSLEVEL = '1') ");
            stb.append("                       END ) ");
            stb.append("            END AS KEIKOKUTEN ");
            stb.append("       FROM RECORD_SCORE_DAT RSD ");
            stb.append("  LEFT JOIN SUBCLASS_MST SM ");
            stb.append("         ON SM.CLASSCD         = RSD.CLASSCD ");
            stb.append("        AND SM.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
            stb.append("        AND SM.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
            stb.append("        AND SM.SUBCLASSCD      = RSD.SUBCLASSCD ");
            stb.append("  LEFT JOIN SRD ");
            stb.append("         ON SRD.YEAR           = RSD.YEAR  ");
            stb.append("        AND SRD.SCHREGNO       = RSD.SCHREGNO ");
            stb.append("  LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
            stb.append("         ON SDD.YEAR           = RSD.YEAR ");
            stb.append("        AND SDD.SCHOOLCD       = '000000000000' ");
            stb.append("        AND SDD.SCHOOL_KIND    = '" + _param._schoolkind + "' ");
            stb.append("        AND SDD.SCHOOL_SEQ     = '009' ");
            stb.append("  LEFT JOIN CHAIR_DETAIL_DAT CDD ");
            stb.append("         ON CDD.YEAR           = SDD.YEAR ");
            stb.append("        AND CDD.SEMESTER       = RSD.SEMESTER ");
            stb.append("        AND CDD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("        AND CDD.SEQ            = '003' ");
            stb.append("  LEFT JOIN CHAIR_GROUP_MST CGM ");
            stb.append("         ON CGM.YEAR           = RSD.YEAR ");
            stb.append("        AND CGM.SEMESTER       = RSD.SEMESTER ");
            stb.append("        AND CGM.CLASSCD        = RSD.CLASSCD ");
            stb.append("        AND CGM.SCHOOL_KIND    = RSD.SCHOOL_KIND ");
            stb.append("        AND CGM.CURRICULUM_CD  = RSD.CURRICULUM_CD ");
            stb.append("        AND CGM.SUBCLASSCD     = RSD.SUBCLASSCD ");
            stb.append("  LEFT JOIN CHAIR_GROUP CGD ");
            stb.append("         ON CGD.YEAR           = RSD.YEAR ");
            stb.append("        AND CGD.SEMESTER       = RSD.SEMESTER ");
            stb.append("        AND CGD.CHAIR_GROUP_CD = CGM.CHAIR_GROUP_CD ");
            stb.append("        AND CGD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("  LEFT JOIN CHAIR_CLS_DAT CCD ");
            stb.append("         ON CCD.YEAR           = RSD.YEAR ");
            stb.append("        AND CCD.SEMESTER       = RSD.SEMESTER ");
            stb.append("        AND CCD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("        AND CCD.TRGTGRADE      = SRD.GRADE ");
            stb.append("        AND CCD.TRGTCLASS      = SRD.HR_CLASS ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
            stb.append("         ON RASD.YEAR          = SDD.YEAR ");
            stb.append("        AND RASD.SEMESTER      = RSD.SEMESTER ");
            stb.append("        AND RASD.TESTKINDCD    = RSD.TESTKINDCD ");
            stb.append("        AND RASD.TESTITEMCD    = RSD.TESTITEMCD ");
            stb.append("        AND RASD.SCORE_DIV     = RSD.SCORE_DIV ");
            stb.append("        AND RASD.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("        AND RASD.MAJORCD       = CGD.CHAIR_GROUP_CD ");
            stb.append("        AND RASD.AVG_DIV       = '6' ");
            stb.append("        AND RASD.HR_CLASS      = '000' ");
            stb.append("      WHERE RSD.YEAR           = '" + _param._year + "' ");
            stb.append("        AND RSD.SEMESTER       = '" + _param._semester + "' ");
            stb.append("        AND RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("        AND SRD.GRADE          = '" + _param._grade + "' ");
            stb.append("        AND CASE WHEN SDD.SCHOOL_REMARK1 = '2' THEN CCD.YEAR IS NOT NULL END ");
            stb.append("   ORDER BY RSD.YEAR ");
            stb.append("          , SRD.HR_CLASS ");
            stb.append("          , RSD.SCHREGNO  ");
            stb.append("          , RSD.SUBCLASSCD ");
            stb.append("), KETTEN AS ( ");
            stb.append("     SELECT BASE.YEAR ");
            stb.append("          , BASE.HR_CLASS ");
            stb.append("          , BASE.SUBCLASSCD ");
            stb.append("          , BASE.SCHOOL_REMARK1 ");
            stb.append("          , SUM(CASE  WHEN BASE.SCORE <= BASE.KEIKOKUTEN ");
            stb.append("                      THEN 1 ");
            stb.append("                      ELSE 0 ");
            stb.append("                END  ");
            stb.append("            ) AS COUNT ");
            stb.append("       FROM BASE ");
            stb.append("   GROUP BY BASE.YEAR ");
            stb.append("          , BASE.HR_CLASS ");
            stb.append("          , BASE.SUBCLASSCD ");
            stb.append("          , BASE.SCHOOL_REMARK1 ");
            if (f == 1) {
                stb.append("), BASE2 AS ( ");
            } else {
                stb.append(") ");
            }
            stb.append("   SELECT RASD.YEAR ");
            stb.append("        , RASD.HR_CLASS ");
            stb.append("        , RASD.SUBCLASSCD ");
            stb.append("        , SM.SUBCLASSABBV ");
            stb.append("        , RASD.AVG ");         //平均点
            stb.append("        , RASD.COUNT ");       //人数
            stb.append("        , NVL(KETTEN.COUNT, 0) AS KETTENCOUNT ");  //欠点者数
            stb.append("        , KETTEN.SCHOOL_REMARK1 ");             //警告点区分
            stb.append("     FROM RECORD_AVERAGE_SDIV_DAT RASD ");
            stb.append("LEFT JOIN SUBCLASS_MST SM ");
            stb.append("       ON SM.CLASSCD        = RASD.CLASSCD ");
            stb.append("      AND SM.SCHOOL_KIND    = RASD.SCHOOL_KIND ");
            stb.append("      AND SM.CURRICULUM_CD  = RASD.CURRICULUM_CD ");
            stb.append("      AND SM.SUBCLASSCD     = RASD.SUBCLASSCD ");
            stb.append("LEFT JOIN KETTEN ");
            stb.append("       ON KETTEN.YEAR       = RASD.YEAR ");
            stb.append("      AND KETTEN.HR_CLASS   = RASD.HR_CLASS ");
            stb.append("      AND KETTEN.SUBCLASSCD = RASD.SUBCLASSCD ");
            stb.append("    WHERE RASD.YEAR         = '" + _param._year + "' ");
            stb.append("      AND RASD.SEMESTER     = '" + _param._semester + "' ");
            stb.append("      AND RASD.TESTKINDCD || RASD.TESTITEMCD || RASD.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("      AND RASD.SCHOOL_KIND  = '" + _param._schoolkind + "' ");
            stb.append("      AND RASD.AVG_DIV      = '2' ");  //平均区分2：クラス
            stb.append("      AND RASD.GRADE        = '" + _param._grade + "' ");
            stb.append("      AND RASD.HR_CLASS     IN (" + printClass + ") ");
            stb.append("      AND SM.SUBCLASSCD IS NOT NULL ");
            stb.append(" ORDER BY RASD.YEAR ");
            stb.append("        , RASD.HR_CLASS ");
            stb.append("        , SM.SUBCLASSCD ");
            if (f == 1) {
                stb.append(") ");
                stb.append("  SELECT BASE2.YEAR ");
                stb.append("       , BASE2.SUBCLASSCD ");
                stb.append("       , SUM(BASE2.AVG) / COUNT(*) AS AVG ");
                stb.append("       , SUM(BASE2.COUNT)          AS COUNT ");
                stb.append("       , SUM(BASE2.KETTENCOUNT)    AS KETTENCOUNT ");
                stb.append("    FROM BASE2 ");
                stb.append("GROUP BY BASE2.YEAR ");
                stb.append("       , BASE2.SUBCLASSCD ");
            }

            return stb.toString();
        }

        /**欠点数延べ人数、欠点多数保有者数**/
        private String sqlKetten() {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH CHAIR_GROUP1 AS ( ");
            stb.append("     SELECT * ");
            stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
            stb.append("      WHERE TESTKINDCD = '00' ");
            stb.append("        AND TESTITEMCD = '00' ");
            stb.append("        AND SCORE_DIV  = '00' ");
            stb.append("), CHAIR_GROUP2 AS ( ");
            stb.append("     SELECT * ");
            stb.append("       FROM CHAIR_GROUP_SDIV_DAT ");
            stb.append("      WHERE TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("), CHAIR_GROUP AS ( ");
            //優先するCHAIR_GROUP_CDを取得
            stb.append("   SELECT CD.YEAR ");
            stb.append("        , CD.SEMESTER ");
            stb.append("        , CD.CHAIRCD ");
            stb.append("        , CASE WHEN CHAIR_GROUP1.CHAIR_GROUP_CD IS NOT NULL ");
            stb.append("               THEN CHAIR_GROUP1.CHAIR_GROUP_CD ");
            stb.append("               ELSE CHAIR_GROUP2.CHAIR_GROUP_CD ");
            stb.append("          END CHAIR_GROUP_CD ");
            stb.append("     FROM CHAIR_DAT CD ");
            stb.append("LEFT JOIN CHAIR_GROUP1 ");
            stb.append("       ON CD.YEAR     = CHAIR_GROUP1.YEAR ");
            stb.append("      AND CD.SEMESTER = CHAIR_GROUP1.SEMESTER ");
            stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP1.CHAIRCD ");
            stb.append("LEFT JOIN CHAIR_GROUP2 ");
            stb.append("       ON CD.YEAR     = CHAIR_GROUP2.YEAR ");
            stb.append("      AND CD.SEMESTER = CHAIR_GROUP2.SEMESTER ");
            stb.append("      AND CD.CHAIRCD  = CHAIR_GROUP2.CHAIRCD ");
            stb.append("    WHERE CD.YEAR     = '" + _param._year + "' ");
            stb.append("      AND CD.SEMESTER = '" + _param._semester + "' ");
            stb.append("), SRD AS ( ");
            stb.append("   SELECT YEAR ");
            stb.append("        , SCHREGNO ");
            stb.append("        , GRADE ");
            stb.append("        , HR_CLASS  ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append(" GROUP BY YEAR ");
            stb.append("        , SCHREGNO ");
            stb.append("        , GRADE ");
            stb.append("        , HR_CLASS ");
            stb.append("), BASE AS ( ");
            stb.append("   SELECT RSD.YEAR ");
            stb.append("        , SRD.HR_CLASS ");
            stb.append("        , RSD.SCHREGNO ");
            stb.append("        , RSD.SUBCLASSCD ");
            stb.append("        , SDD.SCHOOL_REMARK1 ");
            stb.append("        , RSD.SCORE ");
            stb.append("        , CASE WHEN SDD.SCHOOL_REMARK1 = '1' THEN CDD.REMARK1 ");
            stb.append("               WHEN SDD.SCHOOL_REMARK1 = '2' THEN RASD.AVG * SDD.SCHOOL_REMARK2 / SDD.SCHOOL_REMARK3 ");  //類型グループの平均点を使用
            stb.append("               ELSE ( CASE WHEN RSD.SCORE_DIV = '09' THEN 1 ");  //評定の場合、警告点は1
            stb.append("                           ELSE ( SELECT ASSESSHIGH  FROM ASSESS_MST  WHERE ASSESSCD = '2'  AND ASSESSLEVEL = '1') ");
            stb.append("                      END ) ");
            stb.append("          END AS KEIKOKUTEN ");
            stb.append("     FROM RECORD_SCORE_DAT RSD ");
            stb.append("LEFT JOIN SUBCLASS_MST SM ");
            stb.append("       ON SM.CLASSCD         = RSD.CLASSCD ");
            stb.append("      AND SM.SCHOOL_KIND     = RSD.SCHOOL_KIND ");
            stb.append("      AND SM.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
            stb.append("      AND SM.SUBCLASSCD      = RSD.SUBCLASSCD ");
            stb.append("LEFT JOIN SRD ");
            stb.append("       ON SRD.YEAR           = RSD.YEAR ");
            stb.append("      AND SRD.SCHREGNO       = RSD.SCHREGNO ");
            stb.append("LEFT JOIN SCHOOL_DETAIL_DAT SDD ");
            stb.append("       ON SDD.YEAR           = RSD.YEAR ");
            stb.append("      AND SDD.SCHOOLCD       = '000000000000' ");
            stb.append("      AND SDD.SCHOOL_KIND    = '" + _param._schoolkind + "' ");
            stb.append("      AND SDD.SCHOOL_SEQ     = '009' ");
            stb.append("LEFT JOIN CHAIR_DETAIL_DAT CDD ");
            stb.append("       ON CDD.YEAR           = SDD.YEAR ");
            stb.append("      AND CDD.SEMESTER       = RSD.SEMESTER ");
            stb.append("      AND CDD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("      AND CDD.SEQ            = '003' ");
            stb.append("  LEFT JOIN CHAIR_GROUP_MST CGM ");
            stb.append("         ON CGM.YEAR         = RSD.YEAR ");
            stb.append("        AND CGM.SEMESTER     = RSD.SEMESTER ");
            stb.append("        AND CGM.CLASSCD      = RSD.CLASSCD ");
            stb.append("        AND CGM.SCHOOL_KIND  = RSD.SCHOOL_KIND ");
            stb.append("        AND CGM.CURRICULUM_CD = RSD.CURRICULUM_CD ");
            stb.append("        AND CGM.SUBCLASSCD   = RSD.SUBCLASSCD ");
            stb.append("LEFT JOIN CHAIR_GROUP CGD ");
            stb.append("       ON CGD.YEAR           = RSD.YEAR ");
            stb.append("      AND CGD.SEMESTER       = RSD.SEMESTER ");
            stb.append("      AND CGD.CHAIR_GROUP_CD = CGM.CHAIR_GROUP_CD ");
            stb.append("      AND CGD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("LEFT JOIN CHAIR_CLS_DAT CCD ");
            stb.append("       ON CCD.YEAR           = RSD.YEAR ");
            stb.append("      AND CCD.SEMESTER       = RSD.SEMESTER ");
            stb.append("      AND CCD.CHAIRCD        = RSD.CHAIRCD ");
            stb.append("      AND CCD.TRGTGRADE      = SRD.GRADE ");
            stb.append("      AND CCD.TRGTCLASS      = SRD.HR_CLASS ");
            stb.append("LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD ");
            stb.append("       ON RASD.YEAR          = SDD.YEAR ");
            stb.append("      AND RASD.SEMESTER      = RSD.SEMESTER ");
            stb.append("      AND RASD.TESTKINDCD    = RSD.TESTKINDCD ");
            stb.append("      AND RASD.TESTITEMCD    = RSD.TESTITEMCD ");
            stb.append("      AND RASD.SCORE_DIV     = RSD.SCORE_DIV ");
            stb.append("      AND RASD.SCHOOL_KIND   = RSD.SCHOOL_KIND ");
            stb.append("      AND RASD.MAJORCD       = CGD.CHAIR_GROUP_CD ");
            stb.append("      AND RASD.AVG_DIV       = '6' ");
            stb.append("      AND RASD.HR_CLASS      = '000' ");
            stb.append("    WHERE RSD.YEAR           = '" + _param._year + "' ");
            stb.append("      AND RSD.SEMESTER       = '" + _param._semester + "' ");
            stb.append("      AND RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("      AND SRD.GRADE          = '" + _param._grade + "' ");
            stb.append("      AND CASE WHEN SDD.SCHOOL_REMARK1 = '2' THEN CCD.YEAR IS NOT NULL END ");
            stb.append("), KETTEN AS ( ");
            stb.append("  SELECT  BASE.YEAR ");
            stb.append("        , BASE.HR_CLASS ");
            stb.append("        , COUNT(*) AS COUNT ");
            stb.append("        , BASE.SCHREGNO ");
            stb.append("     FROM BASE ");
            stb.append("    WHERE BASE.SCORE <= BASE.KEIKOKUTEN ");
            stb.append(" GROUP BY BASE.YEAR ");
            stb.append("        , BASE.HR_CLASS ");
            stb.append("        , BASE.SCHREGNO ");
            stb.append("), MAIN1 AS ( ");
            stb.append("  SELECT KETTEN.YEAR ");
            stb.append("       , KETTEN.HR_CLASS ");
            stb.append("       , COUNT(*) AS COUNT ");
            stb.append("    FROM KETTEN ");
            stb.append("GROUP BY KETTEN.YEAR ");
            stb.append("       , KETTEN.HR_CLASS ");
            if (_param._unskilfulFlg == true) {
                stb.append("), MAIN2 AS ( ");
                stb.append("     SELECT KETTEN.YEAR ");
                stb.append("          , KETTEN.HR_CLASS ");
                stb.append("          , COUNT(*) AS COUNT ");
                stb.append("       FROM KETTEN ");
                stb.append("      WHERE " + _param._unskilfulPerson + " <= KETTEN.COUNT ");
                stb.append("   GROUP BY KETTEN.YEAR ");
                stb.append("          , KETTEN.HR_CLASS ");
            }
            stb.append(") ");
            stb.append("   SELECT MAIN1.YEAR ");
            stb.append("        , MAIN1.HR_CLASS ");
            stb.append("        , MAIN1.COUNT AS NOBE ");
            if (_param._unskilfulFlg == true) {
                stb.append("        , MAIN2.COUNT AS TASU ");
            } else {
                stb.append("        , '-' AS TASU ");
            }
            stb.append("     FROM MAIN1 ");
            if (_param._unskilfulFlg == true) {
                stb.append("LEFT JOIN MAIN2 ");
                stb.append("       ON MAIN1.YEAR     = MAIN2.YEAR ");
                stb.append("      AND MAIN1.HR_CLASS = MAIN2.HR_CLASS ");
            }
            stb.append("   WHERE MAIN1.HR_CLASS = '" + _classcd + "' ");
            stb.append("ORDER BY MAIN1.YEAR ");
            stb.append("       , MAIN1.HR_CLASS ");

            return stb.toString();
        }
    }

    private static class subclassData {
        final String _avg;
        final String _count;
        final String _kettenCount;

        public subclassData(final String avg, final String count, final String kettenCount) {
            _avg         = avg;
            _count       = count;
            _kettenCount = kettenCount;
        }
    }

    private static class subclass {
        final String _subclassCD;    //科目CD
        final String _subclassAbbv;  //科目名

        public subclass(final String subclassCD, final String subclassAbbv) {

            _subclassCD         = subclassCD;
            _subclassAbbv       = subclassAbbv;
        }
    }

    private static class scoreCat {
        final String _subclassCD;
        final String _cat9;
        final String _cat8;
        final String _cat7;
        final String _cat6;
        final String _cat5;
        final String _cat4;
        final String _cat3;
        final String _cat2;
        final String _cat1;
        final String _cat0;

        public scoreCat(
             final String subclassCD
            ,final String cat9
            ,final String cat8
            ,final String cat7
            ,final String cat6
            ,final String cat5
            ,final String cat4
            ,final String cat3
            ,final String cat2
            ,final String cat1
            ,final String cat0
        ) {

            _subclassCD = subclassCD;
            _cat9       = cat9;
            _cat8       = cat8;
            _cat7       = cat7;
            _cat6       = cat6;
            _cat5       = cat5;
            _cat4       = cat4;
            _cat3       = cat3;
            _cat2       = cat2;
            _cat1       = cat1;
            _cat0       = cat0;
        }
    }

    private static class groupData {
          final String _chairGroupCd;
          final String _subClassName;
          final String _chairGroupName;
          final String _groupAvg;
          final String _groupHighScore;
          final String _groupLowScore;
          final String _groupCnt;
          final String _hrClass;
          final String _avg;
          final String _kettenCnt;
          final String _classCnt;

        public groupData(
             final String chairGroupCd
            ,final String subClassName
            ,final String chairGroupName
            ,final String groupAvg
            ,final String groupHighScore
            ,final String groupLowScore
            ,final String groupCnt
            ,final String hrClass
            ,final String avg
            ,final String kettenCnt
            ,final String classCnt
        ) {

            _chairGroupCd   = chairGroupCd;
            _subClassName   = subClassName;
            _chairGroupName = chairGroupName;
            _groupAvg       = groupAvg;
            _groupHighScore = groupHighScore;
            _groupLowScore  = groupLowScore;
            _groupCnt       = groupCnt;
            _hrClass        = hrClass;
            _avg            = avg;
            _kettenCnt      = kettenCnt;
            _classCnt       = classCnt;
        }
    }

    private class Param {
        private final String _year;
        private final String _loginDate;
        private final String _documentRoot;
        private final boolean _isOutputDebug;
        private final String _semester;
        private final String _schoolkind;
        private final String _grade;
        private final String _testcd;
        private final String _excellentPerson;
        private final boolean _excellentFlg;
        private final String _unskilfulPerson;
        private final boolean _unskilfulFlg;
        private final int _maxSubclass;

        public Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year            = request.getParameter("YEAR");            //年度
            _semester        = request.getParameter("SEMESTER");        //学期
            _schoolkind      = request.getParameter("SCHOOLKIND");      //学校種別
            _grade           = request.getParameter("GRADE");           //学年
            _testcd          = request.getParameter("TESTCD");          //テスト
            _loginDate       = request.getParameter("LOGIN_DATE");      //ログイン日付
            _excellentPerson = request.getParameter("EXCELLENT_PERSON");//優秀者の平均点
            _unskilfulPerson = request.getParameter("UNSKILFUL_PERSON");//欠点個数


            _maxSubclass   = 21;  //1ページの最大科目数

            _documentRoot = request.getParameter("DOCUMENTROOT");

            if ("".equals(_excellentPerson)) {
                //優秀者設定なし
                _excellentFlg = false;
            } else {
                _excellentFlg = true;
            }

            if ("".equals(_unskilfulPerson)) {
                //欠点多数保持者設定なし
                _unskilfulFlg = false;
            } else {
                _unskilfulFlg = true;
            }

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJA157C' AND NAME = '" + propName + "' "));
        }
    }

}//クラスの括り
