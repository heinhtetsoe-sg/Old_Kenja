// kanji=漢字
/*
 * $Id: db047e4a4c34b11f404dcc5f043f837d35f01d08 $
 *
 * 作成日: 2005/06/24
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJF.detail.MedexamDetDat;
import servletpack.KNJF.detail.MedexamToothDat;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 *
 *  学校教育システム 賢者 [保健管理]
 *
 *                  ＜ＫＮＪＦ０３４Ａ＞  駿台甲府
 */
public class KNJF034A {

    private static final Log log = LogFactory.getLog(KNJF034A.class);

    private Param _param;
    private static String printKenkouSindanIppan = "1";
    private boolean _hasdata;

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        //  print設定
        final PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());           //PDFファイル名の設定

        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        //  ＳＶＦ作成処理
        try {
            _param = new Param(db2, request);

            printMain(request, svf, db2);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {

            //  該当データ無し
            if (!_hasdata) {
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

    private void printMain(final HttpServletRequest request, final Vrw32alp svf, DB2UDB db2) throws SQLException {
        final List studentList = Student.getStudentList(db2, _param);
        
        _param._physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, _param);

        log.info(" studentList size = " + studentList.size());
        
        for (int i = 0; i < studentList.size(); i++) {
            final Student student = (Student) studentList.get(i);

            if ("P".equals(student._schoolKind)) {
                printP(svf, db2, student);
            }
            if ("J".equals(student._schoolKind)) {
                printJ(svf, db2, student);
            }
        }
    }
    

    private void printP(final Vrw32alp svf, final DB2UDB db2, final Student student) {
        final String form = "KNJF034A_1.frm";
        svf.VrSetForm(form, 1);

        final int ketaName = KNJ_EditKinsoku.getMS932ByteCount(student._name);
        svf.VrsOut("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名
        
        final String gakunen = (NumberUtils.isDigits(student._gradeCd) ? _param.getZenkakuDigit(String.valueOf(Integer.parseInt(student._gradeCd))) : StringUtils.defaultString(student._gradeCd)) + "年";
        svf.VrsOut("GRADE", gakunen); // 学年

        final MedexamDetDat mdd = student._medexamDetDat;
        if (null != mdd) {
            svf.VrsOut("HIGHT1", append(Util.sishagonyu(mdd._height), "cm")); // 身長
            svf.VrsOut("WEIGHT1", append(Util.sishagonyu(mdd._weight), "kg")); // 体重
            svf.VrsOut("SITTING", append(Util.sishagonyu(mdd._sitheight), "cm")); // 座高
            svf.VrsOut("EYESIGHT1_1", mdd._rBarevisionMark); // 右 視力
            svf.VrsOut("EYESIGHT1_2", mdd._rVisionMark); // 右 視力 矯正
            svf.VrsOut("EYESIGHT2_1", mdd._lBarevisionMark); // 左 視力
            svf.VrsOut("EYESIGHT2_2", mdd._lVisionMark); // 左 視力 矯正
            svf.VrsOut("OPGTHA", getEyeDisease(mdd)); // 眼科
            svf.VrsOut("EAR_L", _param.getNameMstName1("F010", mdd._lEar)); // 聴力
            svf.VrsOut("EAR_R", _param.getNameMstName1("F010", mdd._rEar)); // 聴力
            svf.VrsOut("OTOLA", getNose(mdd)); // 耳鼻科
            svf.VrsOut("INTERNAL", getNaika(mdd)); // 内科
            svf.VrsOut("TUBER", getKekkaku(mdd)); // 結核
            svf.VrsOut("HEART", getHeart(mdd)); // 心臓検診
            svf.VrsOut("REMARK" + (KNJ_EditKinsoku.getMS932ByteCount(mdd._remark) <= 68 ? "1" : "2"), mdd._remark); // 備考
            svf.VrsOut("URINE", getNyo(mdd)); // 尿
            svf.VrsOut("PARASITE", _param.getNameMstName1("F023", mdd._parasite)); // 蟯虫検査
        }

        final MedexamToothDat mtd = student._medexamToothDat;
        if (null != mtd) {
            svf.VrsOut("CAVITY1", mtd._remainbabytooth); // むし歯 乳歯
            svf.VrsOut("CAVITY2", mtd._remainadulttooth); // むし歯 永久歯
            svf.VrsOut("CAUTION", mtd._brackBabytooth); // 要注意乳歯
            svf.VrsOut("OBSERV", mtd._brackAdulttooth); // 要観察歯
            svf.VrsOut("TEETHLINE", _param.getNameMstName1("F510", mtd._jawsJointcd)); // 歯列咬合
            svf.VrsOut("JAWS", _param.getNameMstName1("F511", mtd._jawsJointcd2)); // 顎関節
            svf.VrsOut("PLAQUE", _param.getNameMstName1("F520", mtd._plaquecd)); // 歯垢の状態
            svf.VrsOut("GUM", _param.getNameMstName1("F513", mtd._gumcd)); // 歯肉の状態
            svf.VrsOut("OTHERS", getToothOther(mtd)); // その他の歯周疾患
        }
        
        final String[] weightMonths = {"04", "06", "09", "11", "01", "03"};
        for (int i = 0; i < weightMonths.length; i++) {
            final String si = String.valueOf(i + 1);
            svf.VrsOut("MONTH1_" + si, String.valueOf(Integer.parseInt(weightMonths[i])) + "月"); // 月
            final String weight = getString(Util.getMappedMap(student._medexamDetMonthDatMap, weightMonths[i]), "WEIGHT");
            svf.VrsOut("YEAR_WEIGHT" + si, append(Util.sishagonyu(weight), "kg")); // 年間体重
        }
        final String[] heightMonths = {"04", "09", "01"};
        for (int i = 0; i < heightMonths.length; i++) {
            final String si = String.valueOf(i + 1);
            svf.VrsOut("MONTH2_" + si, String.valueOf(Integer.parseInt(heightMonths[i])) + "月"); // 月
            final String height = getString(Util.getMappedMap(student._medexamDetMonthDatMap, heightMonths[i]), "HEIGHT");
            svf.VrsOut("YEAR_HIGHT" + si, append(Util.sishagonyu(height), "cm")); // 年間身長
        }
        final String[] himandoMonths = {"04", "09", "01"};
        for (int i = 0; i < himandoMonths.length; i++) {
            final String si = String.valueOf(i + 1);
            svf.VrsOut("MONTH3_" + si, String.valueOf(Integer.parseInt(himandoMonths[i])) + "月"); // 月

            final String weight = getString(Util.getMappedMap(student._medexamDetMonthDatMap, himandoMonths[i]), "WEIGHT");
            final String height = getString(Util.getMappedMap(student._medexamDetMonthDatMap, himandoMonths[i]), "HEIGHT");
            final BigDecimal himando = Util.calcHimando(student, weight, height, _param);
            if (null != himando) {
                svf.VrsOut("YEAR_OBESITY" + si, himando.toString() + "％"); // 年間肥満度
            }
        }

        final String picFilePath = _param.getPath("KNJF034A_1_1.png");
        if (null != picFilePath) {
            svf.VrsOut("PIC", picFilePath); // 
        }
        
        svf.VrEndPage();
        _hasdata = true;
    }

    private String getToothOther(final MedexamToothDat mtd) {
        String toothOther = Util.mkString(new String[] {_param.getNameMstName1("F530", mtd._otherdiseasecd, true), mtd._otherdisease}, " ");
        if (StringUtils.isBlank(toothOther)) {
            toothOther = _param.getNameMstName1("F530", mtd._otherdiseasecd, false);
        }
        return toothOther;
    }

    private String getEyeDisease(final MedexamDetDat mdd) {
        String eyeDisease = Util.mkString(new String[] {_param.getNameMstName1("F050", mdd._eyediseasecd, true), mdd._eyeTestResult}, " ");
        if (StringUtils.isBlank(eyeDisease)) {
            eyeDisease = _param.getNameMstName1("F050", mdd._eyediseasecd, false);
        }
        return eyeDisease;
    }

    private String getKekkaku(final MedexamDetDat mdd) {
        String kekkaku = Util.mkString(new String[] {
                _param.getNameMstName1("F100", mdd._tbOthertestcd, true),
                _param.getNameMstName1("F120", mdd._tbNamecd, true),
                _param.getNameMstName1("F130", mdd._tbOthertestRemark1, true)}, " ");
        if (StringUtils.isBlank(kekkaku)) {
            kekkaku = Util.getNotBlank(new String[] {
                    _param.getNameMstName1("F100", mdd._tbOthertestcd, false),
                    _param.getNameMstName1("F120", mdd._tbNamecd, false),
                    _param.getNameMstName1("F130", mdd._tbOthertestRemark1, false)});
        }
        return kekkaku;
    }

    private String getHeart(final MedexamDetDat mdd) {
        String heart = Util.mkString(new String[] {_param.getNameMstName1("F090", mdd._heartdiseasecd, true), mdd._heartdiseasecdRemark}, " ");
        if (StringUtils.isBlank(heart)) {
            return _param.getNameMstName1("F090", mdd._heartdiseasecd, false);
        }
        return heart;
    }

    private String getNaika(final MedexamDetDat mdd) {
        final StringBuffer naika = new StringBuffer();
        final String eiyo = Util.mkString(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd, true), mdd._nutritioncdRemark}, " "); // 栄養状態
        final String sekichuKyokaku = Util.mkString(new String[] {_param.getNameMstName1("F040", mdd._spineribcd, true), mdd._spineribcdRemark}, " "); // 脊柱・胸郭
        final String skinDisease = Util.mkString(new String[] {_param.getNameMstName1("F070", mdd._skindiseasecd, true), mdd._skindiseasecdRemark}, " "); // 皮膚疾患
        log.info(" naika = " + ArrayUtils.toString(new String[] {eiyo, sekichuKyokaku, skinDisease}));
        if (null != eiyo) {
            if (naika.length() > 0) { naika.append(" "); }
            naika.append(eiyo);
        }
        if (null != sekichuKyokaku) {
            if (naika.length() > 0) { naika.append(" "); }
            naika.append(sekichuKyokaku);
        }
        if (null != skinDisease) {
            if (naika.length() > 0) { naika.append(" "); }
            naika.append(skinDisease);
        }
        if (StringUtils.isBlank(naika.toString())) {
            naika.append(StringUtils.defaultString(Util.getNotBlank(new String[] {_param.getNameMstName1("F030", mdd._nutritioncd, false), _param.getNameMstName1("F040", mdd._spineribcd, false), _param.getNameMstName1("F070", mdd._skindiseasecd, false)})));
        }
        return naika.toString();
    }

    private String getNyo(final MedexamDetDat mdd) {
        boolean cdIjounasi = false;
        String nyoCd = null;
        if (null != mdd._albuminuria2cd || null != mdd._uricbleed2cd || null != mdd._uricsugar2cd) {
            nyoCd = getNyoKensaKekka(mdd._albuminuria2cd, mdd._uricsugar2cd, mdd._uricbleed2cd);
            cdIjounasi = "01".equals(mdd._albuminuria2cd) && "01".equals(mdd._uricsugar2cd) && "01".equals(mdd._uricbleed2cd);
        } else if (null != mdd._albuminuria1cd || null != mdd._uricbleed1cd || null != mdd._uricsugar1cd) {
            nyoCd = getNyoKensaKekka(mdd._albuminuria1cd, mdd._uricsugar1cd, mdd._uricbleed1cd);
            cdIjounasi = "01".equals(mdd._albuminuria1cd) && "01".equals(mdd._uricsugar1cd) && "01".equals(mdd._uricbleed1cd);
        }
        String nyo2 = "";
        if (!StringUtils.isEmpty(mdd._uricothertest)) {
            if (StringUtils.isBlank(nyo2)) { nyo2 += " "; }
            nyo2 += StringUtils.defaultString(mdd._uricothertest);
        }
        final String uriAdvicecdname = _param.getNameMstName1("F021", mdd._uriAdvisecd);
        if (!StringUtils.isEmpty(uriAdvicecdname)) {
            if (StringUtils.isBlank(nyo2)) { nyo2 += " "; }
            nyo2 += StringUtils.defaultString(uriAdvicecdname);
        }
        final String nyo;
        if (cdIjounasi && StringUtils.isEmpty(nyo2)) {
            nyo = StringUtils.defaultString(_param.getNameMstName1("F020", mdd._albuminuria1cd), StringUtils.defaultString(_param.getNameMstName1("F019", mdd._uricsugar2cd), _param.getNameMstName1("F018", mdd._uricbleed2cd)));
        } else {
            nyo = StringUtils.defaultString(nyoCd) + nyo2;
        }
        return nyo;
    }
    
    private String getNyoKensaKekka(final String albuminuriacd, final String uricsugarcd, final String uricbleedcd) {
        String nyo = null;
        if (null != albuminuriacd && !"01".equals(albuminuriacd)) {
            if (null != nyo) { nyo += " "; } else { nyo = "";}
            nyo += "尿蛋白 " + StringUtils.defaultString(_param.getNameMstName1("F020", albuminuriacd));
        }
        if (null != uricsugarcd && !"01".equals(uricsugarcd)) {
            if (null != nyo) { nyo += " "; } else { nyo = "";}
            nyo += "尿糖 " + StringUtils.defaultString(_param.getNameMstName1("F019", uricsugarcd));
        }
        if (null != uricbleedcd && !"01".equals(uricbleedcd)) {
            if (null != nyo) { nyo += " "; } else { nyo = "";}
            nyo += "尿潜血 " + StringUtils.defaultString(_param.getNameMstName1("F018", uricbleedcd));
        }
        return nyo;
    }

    private void printJ(final Vrw32alp svf, final DB2UDB db2, final Student student) {
        final String form = "KNJF034A_2.frm";
        svf.VrSetForm(form, 1);
        
        final int ketaName = KNJ_EditKinsoku.getMS932ByteCount(student._name);
        svf.VrsOut("NAME" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._name); // 氏名

        final String[] month = {"04", "08", "01"}; // 印字する測定月

        for (int si = 0; si < 3; si++) {
            final String ssi = String.valueOf(si + 1);
            for (int mi = 0; mi < month.length; mi++) {
                svf.VrsOut("MONTH" + ssi + "_" + String.valueOf(mi + 1), String.valueOf(Integer.parseInt(month[mi])) + "月"); // 月
            }
        }
        
        final String title = _param._nendoZenkaku + "年度　定期健康診断・発育測定の記録";

        svf.VrsOut("TITLE", title); // タイトル
        svf.VrsOut("HR_NAME", student.getHrattendno()); // 年組番

        final MedexamDetDat mdd = student._medexamDetDat;
        final MedexamToothDat mtd = student._medexamToothDat;
        if (null != mtd) {
            svf.VrsOut("DENT", getDental(mtd)); // 歯科
        }
        if (null != mdd) {
            svf.VrsOut("INTERNAL", getNaika(mdd)); // 内科
            svf.VrsOut("OPGTHA", getEyeDisease(mdd)); // 眼科
            svf.VrsOut("OTOLA", getNose(mdd)); // 耳鼻科
            
            svf.VrsOut("ELECTRO", getHeartMedexam(mdd)); // 心電図

            svf.VrsOut("URINE1_1", _param.getNameMstName1("F020", mdd._albuminuria1cd)); // 尿　蛋白
            svf.VrsOut("URINE1_2", _param.getNameMstName1("F019", mdd._uricsugar1cd));   // 尿　糖
            svf.VrsOut("URINE1_3", _param.getNameMstName1("F018", mdd._uricbleed1cd));   // 尿　潜血
            svf.VrsOut("URINE2_1", _param.getNameMstName1("F020", mdd._albuminuria2cd)); // 尿　蛋白 ２次 
            svf.VrsOut("URINE2_2", _param.getNameMstName1("F019", mdd._uricsugar2cd));   // 尿　糖   ２次
            svf.VrsOut("URINE2_3", _param.getNameMstName1("F018", mdd._uricbleed2cd));   // 尿　潜血 ２次
            
            svf.VrsOut("EYESIGHT1", "　　　　" + StringUtils.defaultString(mdd._rBarevisionMark, " ") + "　　　( " + StringUtils.defaultString(mdd._rVisionMark, " ") + " )"); // 視力
            svf.VrsOut("EYESIGHT2", "　　　　" + StringUtils.defaultString(mdd._lBarevisionMark, " ") + "　　　( " + StringUtils.defaultString(mdd._lVisionMark, " ") + " )"); // 視力
            final String lEar = "左" + prepend(" 平均Db:", mdd._lEarDb) + " " + StringUtils.defaultString(_param.getNameMstName1("F010", mdd._lEar));
            final String rEar = "右" + prepend(" 平均Db:", mdd._rEarDb) + " " + StringUtils.defaultString(_param.getNameMstName1("F010", mdd._rEar));
            svf.VrsOut("HEARING", lEar + "、" + rEar); // 聴力
        }

        for (int mi = 0; mi < month.length; mi++) {
            final String smi = String.valueOf(mi + 1);

            final String weight = getString(Util.getMappedMap(student._medexamDetMonthDatMap, month[mi]), "WEIGHT");
            final String height = getString(Util.getMappedMap(student._medexamDetMonthDatMap, month[mi]), "HEIGHT");

            svf.VrsOut("HIGHT" + smi, Util.sishagonyu(height)); // 身長
            svf.VrsOut("WEIGHT" + smi, Util.sishagonyu(weight)); // 体重
            
            final BigDecimal himando = Util.calcHimando(student, weight, height, _param);
            String hantei = null;
            if (null != himando) {
                if (50 <= himando.doubleValue()) {
                    hantei = "高度の肥満傾向";
                } else if (30 <= himando.doubleValue()) {
                    hantei = "中程度の肥満傾向";
                } else if (20 <= himando.doubleValue()) {
                    hantei = "軽度の肥満傾向";
                } else if (himando.doubleValue() <= -30) {
                    hantei = "高度の痩身傾向";
                } else if (himando.doubleValue() <= -20) {
                    hantei = "軽度の痩身傾向";
                }
                
                svf.VrsOut("OBESITY" + smi, himando.toString()); // 肥満度
                svf.VrsOut("JUDGE" + smi, hantei); // 判定
            }
        }

        final String zenkakuAsterisk = "\uFF0A";
        final String gakunen = (NumberUtils.isDigits(student._gradeCd) ? _param.getZenkakuDigit(String.valueOf(Integer.parseInt(student._gradeCd))) : StringUtils.defaultString(student._gradeCd)) + "学年";
        
        for (int i = 0; i < _param._seibetuEntryList.size(); i++) {
            final Map.Entry e = (Map.Entry) _param._seibetuEntryList.get(i);
            final String sex = (String) e.getKey();
            String name2 = (String) e.getValue();
            if (null != name2 && !name2.endsWith("子")) {
                name2 += "子";
            }
            svf.VrsOut("REFERENCE" + String.valueOf(i + 1), zenkakuAsterisk + "参考" + zenkakuAsterisk + "　本校平均値（" + gakunen + name2 + "）"); // 参考
            for (int mi = 0; mi < month.length; mi++) {
                final String smi = String.valueOf(mi + 1);

                final Map heightWeightMap = getHeightWeightAvgMap(db2, student._grade, month[mi], sex);

                if (i == 0) {
                    svf.VrsOut("AVE_M_HIGHT" + smi, getString(heightWeightMap, "HEIGHT")); // 平均身長
                    svf.VrsOut("AVE_M_WEIGHT" + smi, getString(heightWeightMap, "WEIGHT")); // 平均体重
                } else if (i == 1) {
                    svf.VrsOut("AVE_F_HIGHT" + smi, getString(heightWeightMap, "HEIGHT")); // 平均身長
                    svf.VrsOut("AVE_F_WEIGHT" + smi, getString(heightWeightMap, "WEIGHT")); // 平均体重
                }
            }
        }
        
        final String picFilePath = _param.getPath("KNJF034A_2_1.png");
        if (null != picFilePath) {
            svf.VrsOut("PIC", picFilePath); // 
        }
        
        svf.VrEndPage();
        _hasdata = true;
    }

    private String getDental(final MedexamToothDat mtd) {
        String dental = Util.mkString(new String[] {
                prepend("乳歯未処置数", zeroToNull(mtd._remainbabytooth))
              , prepend("要注意乳歯数", zeroToNull(mtd._brackBabytooth))
              , prepend("永久歯未処置数", zeroToNull(mtd._remainadulttooth))
              , prepend("要観察歯数", zeroToNull(mtd._brackAdulttooth))
              , _param.getNameMstName1("F510", mtd._jawsJointcd, true)
              , _param.getNameMstName1("F511", mtd._jawsJointcd2, true)
              , _param.getNameMstName1("F520", mtd._plaquecd, true)
          }, " ");
        if (StringUtils.isBlank(dental)) {
            dental = Util.getNotBlank(new String[] {
                    _param.getNameMstName1("F510", mtd._jawsJointcd, false)
                  , _param.getNameMstName1("F511", mtd._jawsJointcd2, false)
                  , _param.getNameMstName1("F520", mtd._plaquecd, false)
              });
        }
        return dental;
    }
    
    private String zeroToNull(final String num) {
        if (!NumberUtils.isNumber(num)) {
            return null;
        }
        if (Double.parseDouble(num) == 0) {
            return null;
        }
        return num;
    }

    private String getHeartMedexam(final MedexamDetDat mdd) {
        String rtn = Util.mkString(new String[] {_param.getNameMstName1("F080", mdd._heartMedexam, true), mdd._heartMedexamRemark}, " ");
        if (StringUtils.isEmpty(rtn)) {
            rtn = _param.getNameMstName1("F080", mdd._heartMedexam, false);
        }
        return rtn;
    }

    private String getNose(final MedexamDetDat mdd) {
        String noseDisease = Util.mkString(new String[] {
                _param.getNameMstName1("F060", mdd._nosediseasecd)
                , mdd._nosediseasecdRemark
                , _param.getNameMstName1("F061", mdd._nosediseasecd5, true)
                , mdd._nosediseasecdRemark1
                , _param.getNameMstName1("F062", mdd._nosediseasecd6, true)
                , mdd._nosediseasecdRemark2
                , _param.getNameMstName1("F063", mdd._nosediseasecd7, true)
                , mdd._nosediseasecdRemark3
          }, " ");
        if (!StringUtils.isEmpty(noseDisease)) {
            noseDisease = Util.getNotBlank(new String[] {
                    _param.getNameMstName1("F060", mdd._nosediseasecd, false)
                    , _param.getNameMstName1("F061", mdd._nosediseasecd5, false)
                    , _param.getNameMstName1("F062", mdd._nosediseasecd6, false)
                    , _param.getNameMstName1("F063", mdd._nosediseasecd7, false)
              });
        }
        return noseDisease;
    }
    
    private Map getHeightWeightAvgMap(final DB2UDB db2, final String grade, final String month, final String sex) {
        if (null == _param._cacheMap.get(grade)) {
            
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     REGD.GRADE, T1.SEMESTER, T1.MONTH, BASE.SEX, AVG(T1.HEIGHT) AS HEIGHT, AVG(T1.WEIGHT) AS WEIGHT ");
            sql.append(" FROM MEDEXAM_DET_MONTH_DAT T1 ");
            sql.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            sql.append("     AND REGD.YEAR = '" + _param._year + "' ");
            sql.append("     AND REGD.SEMESTER = '" + _param._gakki + "' ");
            sql.append("     AND REGD.GRADE = '" + grade + "' ");
            sql.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + _param._year + "' ");
            sql.append(" GROUP BY ");
            sql.append("     REGD.GRADE, T1.SEMESTER, T1.MONTH, BASE.SEX ");
            sql.append(" ORDER BY ");
            sql.append("     REGD.GRADE, T1.SEMESTER, T1.MONTH, BASE.SEX ");
            
            final List rowList = KnjDbUtils.query(db2, sql.toString());
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final Map heightWeightMap = Util.getMappedMap(Util.getMappedMap(Util.getMappedMap(_param._cacheMap, "GRADE" + getString(row, "GRADE")), "MONTH" + getString(row, "MONTH")), "SEX" + getString(row, "SEX"));
                heightWeightMap.put("HEIGHT", Util.sishagonyu(getString(row, "HEIGHT")));
                heightWeightMap.put("WEIGHT", Util.sishagonyu(getString(row, "WEIGHT")));
            }
        }
        
        return Util.getMappedMap(Util.getMappedMap(Util.getMappedMap(_param._cacheMap, "GRADE" + grade), "MONTH" + month), "SEX" + sex);
    }

    private static String append(final String a, final String b) {
        if (StringUtils.isBlank(a)) {
            return "";
        }
        return a + b;
    }

    private static String prepend(final String a, final String b) {
        if (StringUtils.isBlank(b)) {
            return "";
        }
        return a + b;
    }

    private static String getString(final Map map, final String field) {
        if (null == field || null == map || map.isEmpty()) {
            return null;
        }
        try {
            if (!map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        final String rtn = (String) map.get(field);
//        log.info(" field = " + field + " / rtn = " + rtn);
        return rtn;
    }

    private static class Util {

        private static String sishagonyu(final String s) {
            if (NumberUtils.isNumber(s)) {
                return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            return null;
        }

        public static String[] splitByLength(final String data, int count) {
            if (null == data || data.length() == 0) {
                return new String[] {};
            }
            if (data.length() > count) {
                return new String[] {data.substring(0, count), data.substring(count)};
            }
            return new String[] {data};
        }

        private static void svfVrsOutArray(final Vrw32alp svf, final String[] field, final String[] tokens) {
            if (null != tokens) {
                svfVrsOutArrayList(svf, field, Arrays.asList(tokens));
            }
        }

        private static void svfVrsOutArrayList(final Vrw32alp svf, final String[] field, final List tokens) {
            for (int i = 0; i < Math.min(field.length,  tokens.size()); i++) {
                svf.VrsOut(field[i], (String) tokens.get(i));
            }
        }

        private static int getMS932ByteLength(final String str) {
            int len = 0;
            if (null != str) {
                try {
                    len = str.getBytes("MS932").length;
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return len;
        }

        private static int getMaxMS932ByteLength(final String[] strs) {
            int max = 0;
            for (int i = 0; i < strs.length; i++) {
                max = Math.max(max, getMS932ByteLength(strs[i]));
            }
            return max;
        }

        /**
         * 文字列をMS932にエンコードして指定分割バイト数で分割する
         * @param str 文字列
         * @param splitByte MS932にエンコードした場合の分割バイト数
         * @return 分割された文字列
         */
        private static String[] getMS932ByteToken(final String str, final int[] splitByte) {
            if (null == str) {
                return new String[] {};
            }
            final List tokenList = new ArrayList();
            StringBuffer token = new StringBuffer();
            int splitByteIdx = 0;
            int currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
            for (int i = 0; i < str.length(); i++) {
                final String ch = String.valueOf(str.charAt(i));
                if (getMS932ByteLength(token.toString() + ch) > currentSplitByte) {
                    tokenList.add(token.toString());
                    token = new StringBuffer();
                    splitByteIdx += 1;
                    currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
                }
                token.append(String.valueOf(ch));
            }
            if (token.length() != 0) {
                tokenList.add(token.toString());
            }
            final String[] array = new String[tokenList.size()];
            for (int i = 0; i < tokenList.size(); i++) {
                array[i] = (String) tokenList.get(i);
            }
            return array;
        }

        private static int toInt(final String s, final int def) {
            if (!NumberUtils.isDigits(s)) {
                return def;
            }
            return Integer.parseInt(s);
        }

        private static double toDouble(final String s, final double def) {
            if (!NumberUtils.isNumber(s)) {
                return def;
            }
            return Double.parseDouble(s);
        }

        private static Map getMappedMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap());
            }
            return (Map) map.get(key1);
        }

        private static List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
        }

        private static String mkString(final String[] array, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String c = "";
            for (int i = 0; i < array.length; i++) {
                if (!StringUtils.isEmpty(array[i])) {
                    stb.append(c).append(array[i]);
                    c = comma;
                }
            }
            return stb.toString();
        }

        private static String getNotBlank(final String[] array) {
            for (int i = 0; i < array.length; i++) {
                if (!StringUtils.isBlank(array[i])) {
                    return array[i];
                }
            }
            return null;
        }

        // 肥満度計算
        //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
        public static BigDecimal calcHimando(final Student student, final String weight, final String height, final Param param) {
            if (null == weight) {
                log.info(" " + student._schregno + ", " + param._year + " 体重がnull");
                return null;
            }
            BigDecimal weightAvg = null;
            final boolean isUseMethod2 = true;
            if (isUseMethod2) {
                // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
                final BigDecimal weightAvg2 = getWeightAvgMethod2(student, param._physAvgMap, height, param);
                // log.fatal(" (schregno, attendno, weight1, weight2) = (" + rs.getString("SCHREGNO") + ", " + rs.getString("ATTENDNO") + ", " + weightAvg1 + ", " + weightAvg2 + ")");
                log.fatal(" (schregno, attendno, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg2 + ")");
                weightAvg = weightAvg2;
            } else {
                // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
            }
            if (null == weightAvg) {
                return null;
            }
            final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(weight).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
            log.fatal(" himando = 100 * (" + weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
            return himando;
        }

        private static BigDecimal getWeightAvgMethod2(final Student student, final Map physAvgMap, final String height, final Param param) {
            final String schregno = student._schregno;
            if (null == height) {
                log.debug(" " + schregno + ", " + param._year + " 身長がnull");
                return null;
            }
//            if (null == student._birthDay) {
//                log.debug(" " + schregno + ", " + param._year + " 生年月日がnull");
//                return null;
//            }
            // 日本小児内分泌学会 (http://jspe.umin.jp/)
            // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
            // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
            // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
            // 標準体重＝ａ×身長（cm）- ｂ 　 　
            final BigDecimal heightBd = new BigDecimal(height);
            final String kihonDate = param._year + "-04-01";
            final int iNenrei = (int) getNenrei(student, kihonDate, param._year, param._year);
//            final int iNenrei = (int) getNenrei2(rs, param._year, param._year);
            final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, getMappedList(physAvgMap, student._sex));
            if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
                return null;
            }
            final BigDecimal a = hpad._stdWeightKeisuA;
            final BigDecimal b = hpad._stdWeightKeisuB;
            final BigDecimal avgWeight = a.multiply(heightBd).subtract(b);
            log.fatal(" method2 avgWeight = " + a + " * " + heightBd + " - " + b + " = " + avgWeight);
            return avgWeight;
        }

        // 学年から年齢を計算する
        private static double getNenrei2(final Student student, final String year1, final String year2) throws NumberFormatException {
            final int startAge;
            if ("P".equals(student._schoolKind)) {
                startAge = 6;
            } else if ("J".equals(student._schoolKind)) {
                startAge = 12;
            } else { // "H".equals(student._schoolKind) {
                startAge = 15;
            }
            return startAge + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0);
        }

        // 生年月日と対象日付から年齢を計算する
        private static double getNenrei(final Student student, final String date, final String year1, final String year2) throws NumberFormatException {
            if (null == student._birthDay) {
                return getNenrei2(student, year1, year2);
            }
            final Calendar calBirthDate = Calendar.getInstance();
            calBirthDate.setTime(Date.valueOf(student._birthDay));
            final int birthYear = calBirthDate.get(Calendar.YEAR);
            final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

            final Calendar calTestDate = Calendar.getInstance();
            calTestDate.setTime(Date.valueOf(date));
            final int testYear = calTestDate.get(Calendar.YEAR);
            final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);

            int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
            final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
            final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
            return nenrei;
        }

        // 年齢の平均データを得る
        private static HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
            HexamPhysicalAvgDat tgt = null;
            for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
                final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
                if (hpad._nenrei <= nenrei) {
                    tgt = hpad;
                    if (hpad._nenreiYear == nenrei) {
                        break;
                    }
                }
            }
            return tgt;
        }
    }

    public static class Student {
        final String _schregno;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendno;
        final String _annual;
        final String _name;
        final String _sex;
        final String _birthDay;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursename;
        final String _majorname;
        final String _coursecodename;
        final String _schoolKind;
        final Map _medexamDetMonthDatMap = new HashMap();
        MedexamDetDat _medexamDetDat = null;
        MedexamToothDat _medexamToothDat = null;
        String _nenkuminamae;

        Student(final String schregno,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendno,
                final String annual,
                final String name,
                final String sex,
                final String birthDay,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String coursename,
                final String majorname,
                final String coursecodename,
                final String schoolKind
        ) {
            _schregno = schregno;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendno = attendno;
            _annual = annual;
            _name = name;
            _sex = sex;
            _birthDay = birthDay;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _schoolKind = schoolKind;
        }

        public String getHrattendno() {
            final StringBuffer stb = new StringBuffer();
            if (NumberUtils.isDigits(_gradeCd)) {
                stb.append(Integer.parseInt(_gradeCd)).append("年");
            }
            if (null != _hrAbbv && _hrAbbv.length() > 0) {
                stb.append(_hrAbbv.substring(_hrAbbv.length() - 1)).append("組");
            }
            if (NumberUtils.isDigits(_attendno)) {
                stb.append(Integer.parseInt(_attendno)).append("番");
            } else {
                stb.append(StringUtils.defaultString(_attendno));
            }
            return stb.toString();
        }

        static List getStudentList(final DB2UDB db2, final Param param) throws SQLException {
            final List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String studentSql = Student.getStudentSql(param);
            log.debug(" student sql = " + studentSql);
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrAbbv = rs.getString("HR_NAMEABBV");
                    final String attendno = rs.getString("ATTENDNO");
                    final String annual = rs.getString("ANNUAL");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    final String birthDay = rs.getString("BIRTHDAY");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String coursecode = rs.getString("COURSECODE");
                    final String coursename = rs.getString("COURSENAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final String coursecodename = rs.getString("COURSECODENAME");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final Student student = new Student(schregno,
                                                        grade,
                                                        gradeCd,
                                                        hrClass,
                                                        hrName,
                                                        hrAbbv,
                                                        attendno,
                                                        annual,
                                                        name,
                                                        sex,
                                                        birthDay,
                                                        coursecd,
                                                        majorcd,
                                                        coursecode,
                                                        coursename,
                                                        majorname,
                                                        coursecodename,
                                                        schoolKind);
                    final String seki = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno);
                    student._nenkuminamae = StringUtils.defaultString(student._hrName) + " " + seki + "席　名前　" + StringUtils.defaultString(student._name);
                    rtnList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            setHeightWeightAvgMap(db2, param, rtnList);

            for (final Iterator it = rtnList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                student._medexamDetDat = new MedexamDetDat(db2, param._year, student._schregno, printKenkouSindanIppan);
                student._medexamToothDat = new MedexamToothDat(db2, param._year, student._schregno);
            }
            return rtnList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     HR.HR_NAME, ");
            stb.append("     HR.HR_NAMEABBV, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.ANNUAL, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX AS SEX, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     COURSE.COURSENAME, ");
            stb.append("     MAJOR.MAJORNAME, ");
            stb.append("     COURSEC.COURSECODENAME, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
            stb.append("          AND REGD.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HR ON REGD.YEAR = HR.YEAR ");
            stb.append("          AND REGD.SEMESTER = HR.SEMESTER ");
            stb.append("          AND REGD.GRADE = HR.GRADE ");
            stb.append("          AND REGD.HR_CLASS = HR.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST AS BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST AS N1 ON N1.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = N1.NAMECD2 ");
            stb.append("     LEFT JOIN COURSECODE_MST AS COURSEC ON REGD.COURSECODE = COURSEC.COURSECODE ");
            stb.append("     LEFT JOIN MAJOR_MST AS MAJOR ON REGD.COURSECD = MAJOR.COURSECD ");
            stb.append("          AND REGD.MAJORCD = MAJOR.MAJORCD ");
            stb.append("     LEFT JOIN COURSE_MST AS COURSE ON REGD.COURSECD = COURSE.COURSECD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._gakki + "' ");
            if ("1".equals(param._output)) { //1:個人
                stb.append("       AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else if ("2".equals(param._output)) { //2:クラス
                stb.append("       AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
            return stb.toString();
        }
        
        
        private static void setHeightWeightAvgMap(final DB2UDB db2, final Param param, final List studentList) {

            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("     T1.MONTH, T1.HEIGHT, T1.WEIGHT ");
            sql.append(" FROM MEDEXAM_DET_MONTH_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.YEAR = '" + param._year + "' ");
            sql.append("     AND T1.SCHREGNO = ? ");

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                for (final Iterator rit = KnjDbUtils.query(db2, sql.toString(), new String[] { student._schregno }).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final Map heightWeightAvgMap = Util.getMappedMap(student._medexamDetMonthDatMap, getString(row, "MONTH"));
                    heightWeightAvgMap.put("HEIGHT", Util.sishagonyu(getString(row, "HEIGHT")));
                    heightWeightAvgMap.put("WEIGHT", Util.sishagonyu(getString(row, "WEIGHT")));
                }
            }
        }
    }

    private static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _year;
        final String _nendoZenkaku;
        final String _gakki;
        final String _output;
        final String[] _categorySelected;
//        final String _schoolJudge;
        final String _date;
        private String _textDoctorStamp;

//        final String _ctrlDate;
        private String _namemstZ010Name1;
        private String _hiduke;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final Map _formFieldInfoMapMap = new HashMap();
        private String _imagePath;
        final Map _cacheMap = new HashMap();

        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;

        private final List _seibetuEntryList;
        private final Map _nameMstFXX;
        private Map _physAvgMap = null;

//        final String _useKnjf030AHeartBiko;
//        final String _useForm5_H_Ippan;
//        /** 名称マスタのコンボで名称予備2=1は表示しない */
//        final String _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            //  パラメータの取得
            _year = request.getParameter("YEAR");                        //年度
            _gakki = request.getParameter("GAKKI");                       //学期 1,2,3
            _output = request.getParameter("OUTPUT");                       //1:個人 2:クラス
//            _schoolJudge = request.getParameter("SCHOOL_JUDGE");                //H:高校、J:中学
            _categorySelected = request.getParameterValues("category_selected");

            _date = request.getParameter("DATE");        // 学校への提出日

//            _useKnjf030AHeartBiko = request.getParameter("useKnjf030AHeartBiko");
//            _useForm5_H_Ippan = request.getParameter("useForm5_H_Ippan");
//            _kenkouSindanIppanNotPrintNameMstComboNamespare2Is1 = request.getParameter("kenkouSindanIppanNotPrintNameMstComboNamespare2Is1");

            if (null != _date) {
                _hiduke = KNJ_EditDate.h_format_JP(_date);
            }

            //  学校名・学校住所・校長名の取得
//            _returnval2 = new KNJ_Schoolinfo(_year).get_info(db2);

//            _printStamp = request.getParameter("PRINT_STAMP");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _namemstZ010Name1 = getNameMstZ010(db2);
            log.info(" _namemstZ010Name1 = " + _namemstZ010Name1);

            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
//            setCertifSchoolDat(db2);

            _nameMstFXX = getNameMstFXX(db2);
//            for (final Iterator it = _nameMstFXX.keySet().iterator(); it.hasNext();) {
//                final String namecd1 = (String) it.next();
//                final Map map2 = Util.getMappedMap(_nameMstFXX, namecd1);
//                log.info(" ----:namecd1 = " + namecd1);
//                for (final Iterator it2 = map2.keySet().iterator(); it2.hasNext();) {
//                    final String namecd2 = (String) it2.next();
//                    log.info("   :namecd2 = " + namecd2 + ", map = " + map2.get(namecd2));
//                }
//            }
            loadControlMst(db2);
            
            _seibetuEntryList = new ArrayList(new TreeMap(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME2 FROM NAME_MST WHERE NAMECD1 = 'Z002' "), "NAMECD2", "NAME2")).entrySet());
            _nendoZenkaku = getZenkakuDigit(_year);
        }
        
        public String getZenkakuDigit(final String num) {
            final StringBuffer zenkaku = new StringBuffer();
            for (int i = 0; i < num.length(); i++) {
                final char ch = num.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    zenkaku.append((char) (ch - '0' + '０'));
                } else {
                    zenkaku.append(ch);
                }
            }
            return zenkaku.toString();
        }
        
        public String getPath(final String filename) {
            final File file = new File(_documentRoot + "/" + _imagePath + "/" + filename);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return null;
        }

        private Map getFieldInfoMap(final Vrw32alp svf, final String form) {
            if (null == _formFieldInfoMapMap.get(form)) {
                _formFieldInfoMapMap.put(form, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
            }
            return Util.getMappedMap(_formFieldInfoMapMap, form);
        }

        private static int getFieldKeta(final Map fieldnameMap, final String fieldname) {
            int rtn = 0;
            final SvfField fieldHrName = (SvfField) fieldnameMap.get(fieldname);
            if (null == fieldHrName) {
                log.info("not found svf field: " + fieldname);
            } else {
                 rtn = fieldHrName._fieldLength;
            }
            return rtn;
        }

        private String getNameMstName1(final String namecd1, final String namecd2) {
            return getNameMstName1(namecd1, namecd2, false);
        }

        private String getNameMstName1(final String namecd1, final String namecd2, final boolean notNamecd2Is01) {
            if (notNamecd2Is01 && "01".equals(namecd2)) {
                return null;
            }
            return getNameMst(namecd1, namecd2, "NAME1");
        }

        private String getNameMst(final String namecd1, final String namecd2, final String field) {
            return getString(KnjDbUtils.firstRow(Util.getMappedList(Util.getMappedMap(_nameMstFXX, namecd1), namecd2)), field);
        }

        private Map getNameMstFXX(final DB2UDB db2) {
            final String sql = "SELECT NAMECD1, NAMECD2, NAME1, NAME2, NAME3, NAMESPARE1, NAMESPARE2, NAMESPARE3 FROM NAME_MST WHERE NAMECD1 LIKE 'F%' ";
            return getColumnGroupByMap(new String[] {"NAMECD1", "NAMECD2"}, KnjDbUtils.query(db2, sql));
        }

        private String getNameMstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /**
         * 指定カラムのデータのリストを得る
         * @param idx インデクス
         * @param columnList レコードのリスト
         * @param rowList 行リスト
         * @return　指定カラムのデータのリスト
         */
        public static Map getColumnGroupByMap(final String[] columns, final List rowList) {
            final Map rtn = new TreeMap();
            for (final Iterator it = rowList.iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                Map parent = rtn;
                for (int i = 0; i < columns.length; i++) {
                    final String column = columns[i];

                    if (i == columns.length - 1) {
                        Util.getMappedList(parent, getString(row, column)).add(row);
                    } else {
                        parent = Util.getMappedMap(parent, getString(row, column));
                    }
                }
            }
            return rtn;
        }
    }

}//クラスの括り
