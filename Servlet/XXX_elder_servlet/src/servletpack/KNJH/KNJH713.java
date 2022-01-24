// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/05/07
 * 作成者: Nutec
 *
 */
package servletpack.KNJH;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＨ７１３ ＞  学力テスト人数集計表
 */
public class KNJH713 {

    private static final Log log = LogFactory.getLog(KNJH713.class);
    private boolean nonedata = false; //該当データなしフラグ
    private KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
    private static final String ATTR_UNDERLINE = "UnderLine=(0,1,1)";
    private Param  _param;
    private Bunri  _bunri;
    private Kyoukabetu _kyoukabetu;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null; //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter(response.getOutputStream());

            //svf設定
            svf.VrInit(); //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

            sd.setSvfInit(request, response, svf);
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);
            //文理別にチェックを入れたとき
            if (_param._bunri != null) {
                 nonedata = false;
                 ArrayList<Bunri> _bunri = new ArrayList<Bunri>();
                 _bunri = setBunriData(db2);
                 printSvfMainStaffBunri(db2, svf, _bunri);
            }
            //地歴別にチェックを入れたとき
            if (_param._tireki != null) {
                nonedata = false;
                ArrayList<Kyoukabetu> _kyoukabetu = new ArrayList<Kyoukabetu>();
                _kyoukabetu = setKyoukabetuData(db2, "tireki");
                printSvfMainStaffSubClass(db2, svf,_kyoukabetu, "tireki");
            }
            //理科別にチェックを入れたとき
            if (_param._rika != null) {
                nonedata = false;
                ArrayList<Kyoukabetu> _kyoukabetu = new ArrayList<Kyoukabetu>();
                _kyoukabetu = setKyoukabetuData(db2, "rika");
                printSvfMainStaffSubClass(db2, svf,_kyoukabetu, "rika");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close(); //DBを閉じる
            outstrm.close(); //ストリームを閉じる
        }

    }//doGetの括り

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$"); // CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }
    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    /** 帳票出力(文理別) **/
    private void printSvfMainStaffBunri(
            final DB2UDB db2,
            final Vrw32alp svf,
            final ArrayList<Bunri> _bunri
    ) {
        try {
            setForm(svf, "KNJH713.xml", 4);

            //出力日
            svf.VrsOut("DATE", _param._date);
            //表題
            svf.VrsOut("TITLE", _param._year + "年度　3年学力テスト人数集計表");
            //見出し
            svf.VrsOut("BUNRIDIV", "【 文理型 】");
            //型・辞退
            svf.VrsOut("ITEM1", "文科型");
            svf.VrsOut("ITEM2", "理科型");
            svf.VrsOut("ITEM3", "辞退");

            int bunkaTotal = 0;
            int rikaTotal  = 0;
            int zitaiTotal = 0;
            int minyuTotal = 0;
            int total      = 0;

            //集計情報
            for (Bunri bunri : _bunri) {
                //クラス内の合計を計算
                int hrTotal = toInt(bunri._bunkaCnt, 0) +
                                toInt(bunri._rikaCnt , 0) +
                                toInt(bunri._minyuCnt, 0) +
                                toInt(bunri._zitaiCnt, 0);

                svf.VrsOut("HR_NAME", bunri._hrClass);
                //文科型
                if (bunri._bunkaCnt == null) {
                    svf.VrsOut("NUM1", "0");
                } else {
                    svf.VrsOut("NUM1", bunri._bunkaCnt);
                }
                //理科型
                if (bunri._rikaCnt == null) {
                    svf.VrsOut("NUM2", "0");
                } else {
                    svf.VrsOut("NUM2", bunri._rikaCnt);
                }
                //辞退
                if (bunri._zitaiCnt == null) {
                    svf.VrsOut("NUM3", "0");
                } else {
                    svf.VrsOut("NUM3", bunri._zitaiCnt);
                }
                //未入力
                if (bunri._minyuCnt == null) {
                    svf.VrsOut("NUM4", "0");
                } else {
                    svf.VrsOut("NUM4", bunri._minyuCnt);
                }
                //クラス内合計
                svf.VrsOut("NUM5", Integer.toString(hrTotal));

                //各値それぞれの合計を計算
                bunkaTotal += toInt(bunri._bunkaCnt, 0);
                rikaTotal  += toInt(bunri._rikaCnt , 0);
                zitaiTotal += toInt(bunri._zitaiCnt, 0);
                minyuTotal += toInt(bunri._minyuCnt, 0);
                total      += hrTotal;
                svf.VrEndRecord();
            }
            //各値それぞれの合計
            svf.VrsOut("TOTAL_NUM1", Integer.toString(bunkaTotal));
            svf.VrsOut("TOTAL_NUM2", Integer.toString(rikaTotal));
            svf.VrsOut("TOTAL_NUM3", Integer.toString(zitaiTotal));
            svf.VrsOut("TOTAL_NUM4", Integer.toString(minyuTotal));
            svf.VrsOut("TOTAL_NUM5", Integer.toString(total));
            svf.VrEndRecord();

            nonedata = true;
            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setBunriSvfout set error!", ex);
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }


    /** 帳票出力(地歴別、理科別) **/
    private void printSvfMainStaffSubClass(
            final DB2UDB db2,
            final Vrw32alp svf,
            final ArrayList<Kyoukabetu> _kyoukabetu,
            final String mode
    ) {
        try {
            setForm(svf, "KNJH713.xml", 4);

            //出力日
            svf.VrsOut("DATE", _param._date);
            //表題
            svf.VrsOut("TITLE", _param._year + "年度　3年学力テスト人数集計表");
            //見出し
            if ("tireki".equals(mode) == true) {
                svf.VrsOut("BUNRIDIV", "【 地歴型 】");
            } else if ("rika".equals(mode) == true) {
                svf.VrsOut("BUNRIDIV", "【 理科型 】");
            }
            //科目名1
            if (_kyoukabetu.get(0)._hrClassName.size() > 0) {
                svf.VrsOut("ITEM1", _kyoukabetu.get(0)._hrClassName.get(0));
            } else {
                svf.VrsOut("ITEM1", "-");
            }
            //科目名2
            if (_kyoukabetu.get(0)._hrClassName.size() > 1) {
                svf.VrsOut("ITEM2", _kyoukabetu.get(0)._hrClassName.get(1));
            } else {
                svf.VrsOut("ITEM2", "-");
            }
            //科目名3
            if (_kyoukabetu.get(0)._hrClassName.size() > 2) {
                svf.VrsOut("ITEM3", _kyoukabetu.get(0)._hrClassName.get(2));
            } else {
                svf.VrsOut("ITEM3", "-");
            }
            int subClassTotal1 = 0;  //科目1の合計
            int subClassTotal2 = 0;  //科目2の合計
            int subClassTotal3 = 0;  //科目3の合計
            int minyuTotal = 0;       //未入力の合計
            int total = 0;             //合計

            //集計情報
            for (Kyoukabetu kyoukabetu : _kyoukabetu) {
                //クラス内の合計を計算
                int hrTotal = toInt(kyoukabetu._kamokuCnt1, 0) +
                                toInt(kyoukabetu._kamokuCnt2, 0) +
                                toInt(kyoukabetu._kamokuCnt3, 0) +
                                toInt(kyoukabetu._minyuCnt, 0);

                svf.VrsOut("HR_NAME", kyoukabetu._hrClass);
                //科目1
                if (kyoukabetu._kamokuCnt1 == null) {
                    svf.VrsOut("NUM1", "0");
                } else {
                    svf.VrsOut("NUM1", kyoukabetu._kamokuCnt1);
                }
                //科目2
                if (kyoukabetu._kamokuCnt2 == null) {
                    svf.VrsOut("NUM2", "0");
                } else {
                    svf.VrsOut("NUM2", kyoukabetu._kamokuCnt2);
                }
                //科目3
                if (kyoukabetu._kamokuCnt3 == null) {
                    svf.VrsOut("NUM3", "0");
                } else {
                    svf.VrsOut("NUM3", kyoukabetu._kamokuCnt3);
                }
                //未入力
                if (kyoukabetu._minyuCnt == null) {
                    svf.VrsOut("NUM4", "0");
                } else {
                    svf.VrsOut("NUM4", kyoukabetu._minyuCnt);
                }
                //クラス内合計
                svf.VrsOut("NUM5", Integer.toString(hrTotal));

                //各値それぞれの合計を計算
                subClassTotal1 += toInt(kyoukabetu._kamokuCnt1, 0);
                subClassTotal2 += toInt(kyoukabetu._kamokuCnt2, 0);
                subClassTotal3 += toInt(kyoukabetu._kamokuCnt3, 0);
                minyuTotal     += toInt(kyoukabetu._minyuCnt, 0);
                total          += hrTotal;
                svf.VrEndRecord();
            }
            //各値それぞれの合計
            svf.VrsOut("TOTAL_NUM1", Integer.toString(subClassTotal1));
            svf.VrsOut("TOTAL_NUM2", Integer.toString(subClassTotal2));
            svf.VrsOut("TOTAL_NUM3", Integer.toString(subClassTotal3));
            svf.VrsOut("TOTAL_NUM4", Integer.toString(minyuTotal));
            svf.VrsOut("TOTAL_NUM5", Integer.toString(total));
            svf.VrEndRecord();

            nonedata = true;
            if (nonedata) {
                svf.VrEndPage();
                svf.VrsOut("DUMMY", "　");
                svf.VrEndPage();
            }

        } catch (Exception ex) {
            log.error("setSubClassSvfout set error!", ex);
        }
        if (nonedata) {
            svf.VrEndPage();
        }
    }

    /**生徒の文理選択情報を取得**/
    private static class Bunri {
        String _bunkaCnt;
        String _rikaCnt;
        String _minyuCnt;
        String _zitaiCnt;
        String _hrClass;

        public Bunri(
                final String bunkaCnt,
                final String rikaCnt,
                final String minyuCnt,
                final String zitaiCnt,
                final String hrClass
                ) {
            _bunkaCnt = bunkaCnt;
            _rikaCnt  = rikaCnt;
            _minyuCnt = minyuCnt;
            _zitaiCnt = zitaiCnt;
            _hrClass  = hrClass;
        }
    }

    public ArrayList<Bunri> setBunriData(final DB2UDB db2) {

        ArrayList<Bunri> bunriData = new ArrayList<Bunri>();
        Bunri bunri;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getBunriDateSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String hrClass = "";
            String hrNameAbbv = "";
            int bunkaCnt = 0;
            int rikaCnt  = 0;
            int minyuCnt = 0;
            int zitaiCnt = 0;


            //文理選択情報を格納
            while (rs.next()) {
                //HRクラスが変わったとき
                if (hrClass.equals(rs.getString("HR_CLASS")) == false && "".equals(hrClass) == false) {
                    bunri = new Bunri(
                            Integer.toString(bunkaCnt),
                            Integer.toString(rikaCnt),
                            Integer.toString(minyuCnt),
                            Integer.toString(zitaiCnt),
                            hrNameAbbv);
                    //情報を格納
                    bunriData.add(bunri);
                    //カウントの初期化
                    bunkaCnt = 0;
                    rikaCnt = 0;
                    minyuCnt = 0;
                    zitaiCnt = 0;
                }
                if ("1".equals(rs.getString("BUNRIDIV")) == true && "1".equals(rs.getString("DECLINE_FLG")) == false) {
                    //文科型
                    bunkaCnt += rs.getInt("BUNRI");
                } else if ("2".equals(rs.getString("BUNRIDIV")) == true && "1".equals(rs.getString("DECLINE_FLG")) == false) {
                    //理科型
                    rikaCnt += rs.getInt("BUNRI");
                } else if (rs.getString("BUNRIDIV") == null && rs.getString("DECLINE_FLG") == null) {
                    //未入力
                    minyuCnt += rs.getInt("MINYU");
                }
                if ("1".equals(rs.getString("DECLINE_FLG")) == true) {
                    //辞退
                    zitaiCnt += rs.getInt("DECLINE");
                }
                //一つ前のhrClassを保持
                hrClass    = rs.getString("HR_CLASS");
                hrNameAbbv = rs.getString("HR_NAMEABBV");
            }
            bunri = new Bunri(
                    Integer.toString(bunkaCnt),
                    Integer.toString(rikaCnt),
                    Integer.toString(minyuCnt),
                    Integer.toString(zitaiCnt),
                    hrNameAbbv);
            //情報を格納
            bunriData.add(bunri);

        } catch (Exception e) {
            log.error("getBunriDateSql exception!", e);
        }
        return bunriData;

    }


    /**生徒の地歴または理科の選択情報を取得**/
    private static class Kyoukabetu {
        String _kamokuCnt1;
        String _kamokuCnt2;
        String _kamokuCnt3;
        String _minyuCnt;
        String _hrClass;
        ArrayList<String> _hrClassName;

        public Kyoukabetu(
                final String kamokuCnt1,
                final String kamokuCnt2,
                final String kamokuCnt3,
                final String minyuCnt,
                final String hrClass,
                final ArrayList<String> hrClassName
                ) {
            _kamokuCnt1  = kamokuCnt1;
            _kamokuCnt2  = kamokuCnt2;
            _kamokuCnt3  = kamokuCnt3;
            _minyuCnt    = minyuCnt;
            _hrClass     = hrClass;
            _hrClassName = hrClassName;
        }
    }

    public ArrayList<Kyoukabetu> setKyoukabetuData(final DB2UDB db2, String mode) {

        ArrayList<Kyoukabetu> kyoukabetuData = new ArrayList<Kyoukabetu>();
        Kyoukabetu kyoukabetu;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> subClassArray = new ArrayList();
        ArrayList<String> subClassNameArray = new ArrayList();
        int subCnt = 1;
        String sql = "";
        //kyoukabetu = new Kyoukabetu();
        try {
            if ("tireki".equals(mode) == true) {
                //地歴別のSUBCLASSCDを取得
                sql = getSubClassCdSql(_param, "tireki");
            } else if ("rika".equals(mode) == true) {
                //理科別のSUBCLASSCDを取得
                sql = getSubClassCdSql(_param, "rika");
            }
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (subCnt > 3) {
                    //3科目より多いとき
                    break;
                }
                //SUBCLASSCD,SUBCLASSNAMEを格納
                subClassArray.add(rs.getString("SUBCLASSCD"));
                subClassNameArray.add(rs.getString("SUBCLASSNAME"));
                subCnt++;
            }

            if ("tireki".equals(mode) == true) {
                //地歴別のデータを取得
                sql = getSubClassDateSql(_param, "tireki");
            } else if ("rika".equals(mode) == true) {
                //理科別のデータを取得
                sql = getSubClassDateSql(_param, "rika");
            }

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            String hrClass = "";
            String hrNameAbbv = "";
            int kamokuCnt1 = 0;
            int kamokuCnt2 = 0;
            int kamokuCnt3 = 0;
            int minyuCnt  = 0;

            //地歴または理科の選択情報を格納
            while (rs.next()) {
                //HRクラスが変わったとき
                if (hrClass.equals(rs.getString("HR_CLASS")) == false && "".equals(hrClass) == false) {
                    kyoukabetu = new Kyoukabetu(
                            Integer.toString(kamokuCnt1),
                            Integer.toString(kamokuCnt2),
                            Integer.toString(kamokuCnt3),
                            Integer.toString(minyuCnt),
                            hrNameAbbv,
                            subClassNameArray);
                    //情報を格納
                    kyoukabetuData.add(kyoukabetu);
                    //カウントの初期化
                    kamokuCnt1 = 0;
                    kamokuCnt2 = 0;
                    kamokuCnt3 = 0;
                    minyuCnt  = 0;
                }
                //科目1、科目2、科目3、未入力の人数をカウント
                if (subClassArray.size() > 0) {
                    if (subClassArray.get(0).equals(rs.getString("SUBCLASSCD")) == true &&"1".equals(rs.getString("DECLINE_FLG")) == false) {
                        //科目1
                        kamokuCnt1 += rs.getInt("KAMOKU");
                    }
                }
                if (subClassArray.size() > 1) {
                    if (subClassArray.get(1).equals(rs.getString("SUBCLASSCD")) == true && "1".equals(rs.getString("DECLINE_FLG")) == false) {
                        //科目2
                        kamokuCnt2 += rs.getInt("KAMOKU");
                    }
                }
                if (subClassArray.size() > 2) {
                    if (subClassArray.get(2).equals(rs.getString("SUBCLASSCD")) == true && "1".equals(rs.getString("DECLINE_FLG")) == false) {
                        //科目3
                        kamokuCnt3 += rs.getInt("KAMOKU");
                    }
                }
                if (rs.getString("BUNRIDIV") == null && rs.getString("DECLINE_FLG") == null) {
                    //未入力
                    minyuCnt += rs.getInt("MINYU");
                }

                //一つ前のhrClassを保持
                hrClass = rs.getString("HR_CLASS");
                hrNameAbbv  = rs.getString("HR_NAMEABBV");
            }
            kyoukabetu = new Kyoukabetu(
                        Integer.toString(kamokuCnt1),
                        Integer.toString(kamokuCnt2),
                        Integer.toString(kamokuCnt3),
                        Integer.toString(minyuCnt),
                        hrNameAbbv,
                        subClassNameArray);
            //情報を格納
            kyoukabetuData.add(kyoukabetu);

        } catch (Exception e) {
            log.error("getSubClassDateSql exception!", e);
        }
        return kyoukabetuData;

    }

    /**文理選択情報取得**/
    private static String getBunriDateSql(final Param _param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( ");
        stb.append("     SELECT YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , SEMESTER ");
        stb.append("          , HR_CLASS ");
        stb.append("          , ATTENDNO ");
        stb.append("       FROM SCHREG_REGD_DAT SRD ");
        stb.append("      WHERE YEAR     = '" + _param._year + "' ");
        stb.append("        AND SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND GRADE    = '03' ");
        stb.append("   GROUP BY YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , SEMESTER ");
        stb.append("          , HR_CLASS ");
        stb.append("          , ATTENDNO ");
        stb.append(" ), SAD AS (  ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , YEAR ");
        stb.append("       FROM SCHREG_ACADEMICTEST_DAT ");
        stb.append("   GROUP BY SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , YEAR ");
        stb.append(" ) ");
        stb.append("     SELECT SRH.HR_NAMEABBV ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SAD.BUNRIDIV ");
        stb.append("          , SAD.DECLINE_FLG ");
        stb.append("          , COUNT(SAD.BUNRIDIV) AS BUNRI");
        stb.append("          , COUNT(SAD.DECLINE_FLG) AS DECLINE");
        stb.append("          , COUNT(SRH.HR_NAMEABBV) AS MINYU");
        stb.append("       FROM SRD ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("         ON SRH.YEAR     = SRD.YEAR ");
        stb.append("        AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("        AND SRH.GRADE    = '03' ");
        stb.append("        AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("         ON SBM.SCHREGNO = SRD.SCHREGNO ");
        stb.append("  LEFT JOIN SAD ");
        stb.append("         ON SAD.YEAR = SRD.YEAR ");
        stb.append("        AND SAD.SCHREGNO = SBM.SCHREGNO ");
        stb.append("      WHERE SBM.GRD_DIV  IS NULL ");
        stb.append("   GROUP BY SRH.HR_NAMEABBV ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SAD.BUNRIDIV ");
        stb.append("          , SAD.DECLINE_FLG ");
        stb.append("   ORDER BY SRD.HR_CLASS ");

        return stb.toString();
    }

    /**地歴または理科の選択情報取得**/
    private static String getSubClassDateSql(final Param _param, final String mode) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SRD AS ( ");
        stb.append("     SELECT YEAR ");
        stb.append("          , SCHREGNO ");
        stb.append("          , SEMESTER ");
        stb.append("          , HR_CLASS ");
        stb.append("          , ATTENDNO ");
        stb.append("       FROM SCHREG_REGD_DAT SRD ");
        stb.append("      WHERE YEAR     = '" + _param._year + "' ");
        stb.append("        AND SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND GRADE    = '03' ");
        stb.append("   GROUP BY YEAR ");
        stb.append("           , SCHREGNO ");
        stb.append("           , SEMESTER ");
        stb.append("           , HR_CLASS ");
        stb.append("           , ATTENDNO ");
        stb.append(" ), SAD AS ( ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , CLASSCD ");
        stb.append("          , SUBCLASSCD ");
        stb.append("          , YEAR ");
        stb.append("       FROM SCHREG_ACADEMICTEST_DAT ");
        if ("tireki".equals(mode) == true) {
            //地歴別のとき
            stb.append("      WHERE CLASSCD = '12' ");
        } else if ("rika".equals(mode) == true) {
            //理科別のとき
            stb.append("      WHERE CLASSCD = '15' ");
        }
        stb.append("   GROUP BY SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , CLASSCD ");
        stb.append("          , SUBCLASSCD ");
        stb.append("          , YEAR ");
        stb.append(" ), SAD2 AS ( ");
        stb.append("     SELECT SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , YEAR ");
        stb.append("       FROM SCHREG_ACADEMICTEST_DAT ");
        stb.append("   GROUP BY SCHREGNO ");
        stb.append("          , DECLINE_FLG ");
        stb.append("          , BUNRIDIV ");
        stb.append("          , YEAR ");
        stb.append(" ) ");
        stb.append("     SELECT SRH.HR_NAMEABBV ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SAD.CLASSCD ");
        stb.append("          , SAD.SUBCLASSCD ");
        stb.append("          , SAD2.BUNRIDIV ");
        stb.append("          , SAD2.DECLINE_FLG ");
        stb.append("          , COUNT(SAD.SUBCLASSCD) AS KAMOKU");
        stb.append("          , COUNT(SRD.HR_CLASS) AS MINYU");
        stb.append("       FROM SRD ");
        stb.append("  LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("         ON SRH.YEAR     = SRD.YEAR ");
        stb.append("        AND SRH.SEMESTER = SRD.SEMESTER ");
        stb.append("        AND SRH.GRADE    = '03' ");
        stb.append("        AND SRH.HR_CLASS = SRD.HR_CLASS ");
        stb.append("  LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("         ON SBM.SCHREGNO = SRD.SCHREGNO ");
        stb.append("  LEFT JOIN SAD ");
        stb.append("         ON SAD.YEAR     = SRD.YEAR ");
        stb.append("        AND SAD.SCHREGNO = SBM.SCHREGNO ");
        stb.append("  LEFT JOIN SAD2 ");
        stb.append("         ON SAD2.YEAR     = SRD.YEAR ");
        stb.append("        AND SAD2.SCHREGNO = SBM.SCHREGNO ");
        stb.append("      WHERE SBM.GRD_DIV  IS NULL ");
        stb.append("   GROUP BY SRH.HR_NAMEABBV ");
        stb.append("          , SRD.HR_CLASS ");
        stb.append("          , SAD.CLASSCD ");
        stb.append("          , SAD.SUBCLASSCD ");
        stb.append("          , SAD2.BUNRIDIV ");
        stb.append("          , SAD2.DECLINE_FLG ");
        stb.append("   ORDER BY SRD.HR_CLASS ");

        return stb.toString();
    }

    /**科目コード取得**/
    private static String getSubClassCdSql(final Param _param, String mode) {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT SAD.SUBCLASSCD ");
        stb.append("          , ASD.SUBCLASSNAME ");
        stb.append("       FROM SCHREG_ACADEMICTEST_DAT SAD ");
        stb.append("  LEFT JOIN ACADEMICTEST_SUBCLASS_DAT ASD ");
        stb.append("         ON ASD.YEAR       = SAD.YEAR ");
        stb.append("        AND ASD.BUNRIDIV   = SAD.BUNRIDIV ");
        stb.append("        AND ASD.CLASSCD    = SAD.CLASSCD ");
        stb.append("        AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ");
        if ("tireki".equals(mode) == true) {
            //地歴別
            stb.append("      WHERE SAD.CLASSCD  = '12' ");
            stb.append("        AND ASD.ELECTDIV = '2' ");  //選択科目
        } else if ("rika".equals(mode) == true) {
            //理科別
            stb.append("      WHERE SAD.CLASSCD = '15' ");
        }
        stb.append("   GROUP BY SAD.SUBCLASSCD ");
        stb.append("          , ASD.SUBCLASSNAME ");
        stb.append("   ORDER BY SAD.SUBCLASSCD ");

        return stb.toString();
    }

    private static class Param {
        private final String _year;
        private final String _semester;
        private final String _date;
        private final String _bunri;
        private final String _tireki;
        private final String _rika;
        private final String _schoolKind;
        final boolean _isOutputDebug;

        public Param(final DB2UDB db2, final HttpServletRequest request) {
            _year          = request.getParameter("YEAR");                         //年度
            _semester      = request.getParameter("SEMESTER");                     //学期
            _date          = request.getParameter("LOGIN_DATE").replace("-", "/"); //ログイン日付
            _bunri         = request.getParameter("BUNRI");                        //文理別チェックボックス値
            _tireki        = request.getParameter("TIREKI");                       //地歴別チェックボックス値
            _rika          = request.getParameter("RIKA");                         //理科別チェックボックス値
            _schoolKind    = request.getParameter("SCHOOLKIND");                   //SCHOOLKIND
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2,
                    " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH713' AND NAME = '" + propName + "' "));
        }

    }
}//クラスの括り
