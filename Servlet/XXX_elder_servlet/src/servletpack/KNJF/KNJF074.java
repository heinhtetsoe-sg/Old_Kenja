/*
 * $Id: 8238a7a48a0fe4380387ab7d83352e92b5556871 $
 *
 * 作成日: 2019/02/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *   学校教育システム 賢者 [保健管理]
 *
 *                   ＜ＫＮＪＦ０７４＞  学校保健統計(疾病異常)集計表
 */
public class KNJF074 {

    private static final Log log = LogFactory.getLog(KNJF074.class);

    private boolean _hasData;

    Param _param;

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

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void repletionMap(Map studentMap) {
    	//存在しない箇所があれば、nullデータで埋めたデータを用意する。

    	//前提として:
    	// 指示画面では選択する年度を健康診断のテーブルから取得しているので、
    	// データとして0件はありえない(仕様変更の可能性あり)。
    	// 但し、出力タイミングにより「列が欠ける」パターンは存在する。
    	// そのため、この処理が必要となっている。
    	// 「埋めた状態で」SQLデータを取得して更新するパターンもあるが、
    	// 文章の先頭で記載した指示画面の仕様が変わった場合に0件の判定がしづらくなるため、
    	// SQL取得後にチェックをするようにしている

    	if ("1".equals(_param._useSpecial_Support_School)) {
    		//キーは、性別コード+校種
        	//幼稚園
    		jdgNulData(studentMap, "1", "K");
    		jdgNulData(studentMap, "2", "K");

    		//小学校
    		jdgNulData(studentMap, "1", "P");
    		jdgNulData(studentMap, "2", "P");

    		//中学校
    		jdgNulData(studentMap, "1", "J");
    		jdgNulData(studentMap, "2", "J");

    		//高校
    		jdgNulData(studentMap, "1", "H");
    		jdgNulData(studentMap, "2", "H");
        } else {
        	//キーは、性別コード+年齢
        	//15歳
    		jdgNulData(studentMap, "1", "15");
    		jdgNulData(studentMap, "2", "15");

    		//16歳
    		jdgNulData(studentMap, "1", "16");
    		jdgNulData(studentMap, "2", "16");

    		//17歳
    		jdgNulData(studentMap, "1", "17");
    		jdgNulData(studentMap, "2", "17");
        }
    }

    private void jdgNulData(Map studentMap, final String setSexCd, final String keyStr) {
    	final String ksetcd = setSexCd + "-" + keyStr;
        //SQL取得で、必要なキー項目が取得できたか、チェックする。
    	//取得できていない場合は、「1列を"／"で埋めるため」nullデータを用意する。
    	if (!studentMap.keySet().contains(ksetcd)) {
    		studentMap.put(ksetcd, getaddWkNulData(setSexCd, keyStr));
    	}
    }

    private PrintData getaddWkNulData(final String setSexCd, final String keyStr) {
    	final String setSchoolStr;
    	final String setAge;
        //データをnullで埋めたクラスを用意する。
    	if ("1".equals(_param._useSpecial_Support_School)) {
    		setSchoolStr = keyStr;
    		setAge = "";
    	} else {
    		setSchoolStr = "";
    		setAge = keyStr;
    	}
    	PrintData retDat = new PrintData("0", _param._year, setSchoolStr, setAge, setSexCd,
    			                          null, null, null, null, null, null, null, null, null,
    			                          null, null, null, null, null, null, null, null, null,
    			                          null, null, null, null, null, null, null, null, null,
    			                          null, null, null, null, null, null, null, null, null,
    			                          null, null, null, null, null, null, null, null, null,
    			                          null, null, null, null, null, null, null, null, null, null, null);
    	return retDat;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
    	final String fname = "1".equals(_param._useSpecial_Support_School) ? "KNJF074_2.frm" : "KNJF074_1.frm";
        svf.VrSetForm(fname, 1);

        final String ttlstr = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度 学校保健統計（疾病異常）集計表";
        svf.VrsOut("TITLE", ttlstr);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);

        Map studentMap = getStudentMap(db2);

        //存在しないレコードがあれば、"／"を埋めるために各要素がNULLのデータを追加する。
        repletionMap(studentMap);

        //出力
        for (final Iterator ite = studentMap.keySet().iterator();ite.hasNext();) {
        	final String kstr = (String) ite.next();
        	final PrintData pdat = (PrintData)studentMap.get(kstr);

        	final String divColStr; //男女分離フラグ
    		final String sexCdStr = pdat._sexCd;
    		//フォームのフィールド指定するための文字列を確定する。
    		//男女分け
    		if ("1".equals(sexCdStr)) {
    			divColStr = "MALE";
    		} else {
    			divColStr = "FEMALE";
    		}
    		int divColDetailVal = -1;  //見つからなかった場合の値
        	if ("1".equals(_param._useSpecial_Support_School)) {
                //出力列を判定(特別支援学校)
        		final String schKind = pdat._schoolKind;
        		//校種分け
        		if ("H".equals(schKind)) {//高校
        			divColDetailVal = 4;
        		} else if ("J".equals(schKind)) {//中学
        			divColDetailVal = 3;
        		} else if ("P".equals(schKind)) {//小学
        			divColDetailVal = 2;
        		} else if ("K".equals(schKind)) {//幼稚園
        			divColDetailVal = 1;
        		} else if ("合計".equals(schKind)) { //合計
        			divColDetailVal = 5;
        		}
        	} else {
                //出力列を判定(全日制:高校のみ)
        		final String ageStr = pdat._age;
        		//年齢分け
        		if ("15".equals(ageStr)) { //15歳
        			divColDetailVal = 1;
        		} else if ("16".equals(ageStr)) { //16歳
        			divColDetailVal = 2;
        		} else if ("17".equals(ageStr)) { //17歳
        			divColDetailVal = 3;
        		} else if ("合計".equals(ageStr)) { //合計
        			divColDetailVal = 4;
        		}
        	}
        	//divColDetailValが見つからなかったら出力位置がずれるので、次データへ
        	if (divColDetailVal < 0) {
        		continue;
        	}

        	int tblId = 1;  // 1:ページ内上部の表 2:ページ下部の表
        	int line = 1;   //出力位置(特別支援学校と全日制で出力行が違うので、相対的な出力制御を行う)

        	//生徒数
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._stuCnt);
        	line++;
        	//やせ傾向
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._yase);
        	line++;
        	//肥満傾向　（＋２０％以上）
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._himan);
        	line++;
        	//脊柱側わん症・脊柱側わんの者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._sekicyuSokuwan);
        	line++;
        	//その他の脊柱異常・胸郭異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherSekicyuSokuwan);
        	line++;
        	//四肢の状態
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._sisi);
        	line++;
        	//アトピー性皮膚炎
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._atopySkin);
        	line++;
        	//その他の皮膚疾患・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherSkin);
        	line++;
        	//心臓の疾病・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._heartDis);
        	line++;
        	//心電図の異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._heartBeatDis);
        	line++;
        	//ぜん息
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._zensoku);
        	line++;
        	//腎臓疾患
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._jinzou);
        	line++;
        	//委員会での検討を必要とする者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._kentou);
        	line++;
        	//結核の精密検査の対象者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._kekkakuChkDetail);
        	line++;
        	//結核
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._kekkaku);
        	line++;
        	//その他のアレルギー疾患
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherArelgy);
        	line++;
        	//その他の疾病・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherDisease);
        	line++;
        	//難聴
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._nancyou);
        	line++;
        	//耳疾患
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._earSippei);
        	line++;
        	//鼻・副鼻腔疾患
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._noseSubSippei);
        	line++;
        	//咽喉頭疾患
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._throatHeadSippei);
        	line++;
        	//眼疾患・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeSippei);
        	line++;
        	//視力検査受検者数
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyechkInspect);
        	line++;
            if (!"1".equals(_param._useSpecial_Support_School)) {
                //裸眼視力１．０以上の者
            	if(_param._isFukuiken) { //福井県では斜線
            		outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
            	}else {
            		outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._nakedEye1over);
            	}
                line++;
            } else {
            	//特別支援学校では斜線
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
            	line++;
            }
        	//視力Ｂ(０．９～０．７)
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeBLv);
        	line++;
        	//視力Ｃ(０．６～０．３)
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeCLv);
        	line++;
        	//視力Ｄ(０．３未満)
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeDLv);
        	line++;
        	//合　計
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeBCDSum);
        	line++;
            //裸眼視力検査を省略した者
            if ("1".equals(_param._useSpecial_Support_School)) {
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeChkOmit);
            } else {
            	//高校は斜線
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
            }
    	    line++;
        	//歯科検診受検者数
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._toothChkInspect);
        	line++;
        	//う歯なしの者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._noRemainUbaTooth);
        	line++;
        	//う歯の保有者/処置完了者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._ubaToothHolder);
        	line++;
        	//う歯の保有者/未処置者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._remainUbaTooth);
        	line++;
        	//う歯の保有者/合計
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._totalUbaTooth);
        	line++;
            if ("1".equals(_param._useSpecial_Support_School)) {
                //永久歯のう歯数等/う歯等数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._ubaTooth);
                line++;
                //永久歯のう歯数等/喪失歯数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._lostAdultTooth);
                line++;
                //永久歯のう歯数等/ＤＭＦＴ指数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._toothDmftIndex);
                line++;
            } else {
            	//高校では不要なので、斜線
                //永久歯のう歯数等/う歯等数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
                line++;
                //永久歯のう歯数等/喪失歯数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
                line++;
                //永久歯のう歯数等/ＤＭＦＴ指数
                outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, null);
                line++;
            }
        	//その他の歯疾・異常/顎関節
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherChinJoint);
        	line++;
        	//その他の歯疾・異常/歯列・咬合
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherToothAlignment);
        	line++;
        	//その他の歯疾・異常/歯垢の状態
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._plaque);
        	line++;
        	//その他の歯疾・異常/歯肉の状態
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._gum);
        	line++;
        	//その他の歯疾・異常/その他の疾病・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherToothSippei);
        	line++;
        	//口腔の疾病・異常
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._toothDisease);
        	line++;
        	//尿検査受検者数
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._urineChkInspect);
        	line++;
        	//尿二次検査結果/蛋白検出者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._urineChkSndProtein);
        	line++;
        	//尿二次検査結果/糖検出者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._urineChkSndSugar);
        	line++;
        	//尿二次検査結果/潜血検出者
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._urineChkSndHideBlood);

        	tblId = 2;  // 2:ページ下部の表
        	line = 1;   //表が変わるので、クリア
        	//アトピー性皮膚炎
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._atopySkin2);
        	line++;
        	//ぜん息
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._zensoku2);
        	line++;
        	//アレルギー性鼻炎
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._noseArelgy);
        	line++;
        	//アレルギー性結膜炎
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._eyeArelgy);
        	line++;
        	//食物アレルギー
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._foodAllergy);
        	line++;
        	//花粉症
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._hayFever);
        	line++;
        	//薬物アレルギー
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._medicineAllergy);
        	line++;
        	//じんましん
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._jinmasin);
        	line++;
        	//その他のアレルギー
        	outputWithChkSlash(svf, tblId, divColStr, divColDetailVal, line, pdat._otherAllergy);
        	line++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }
    private Map getStudentMap(final DB2UDB db2) {
    	Map retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String dispDatType = rs.getString("DISP_DATTYPE");
            	final String year = rs.getString("YEAR");
            	final String schoolKind;
            	final String age;
            	if ("1".equals(_param._useSpecial_Support_School)) {
            		age = "";
            		schoolKind = rs.getString("SCHOOL_KIND");
            	} else {
            		schoolKind = "";
            		age = rs.getString("AGE");
            	}
            	final String sexCd = rs.getString("SEX");
            	final String stuCnt = rs.getString("STU_CNT");
            	final String yase = rs.getString("YASE_SUM");
            	final String himan = rs.getString("HIMAN_SUM");
            	final String sekicyuSokuwan = rs.getString("SEKICYU_SOKUWAN_SUM");
            	final String otherSekicyuSokuwan = rs.getString("OTHER_SEKICYU_SOKUWAN_SUM");
            	final String sisi = rs.getString("SISI_SUM");
            	final String atopySkin = rs.getString("ATOPY_SKIN_SUM");
            	final String otherSkin = rs.getString("OTHER_SKIN_SUM");
            	final String heartDis = rs.getString("HEART_DIS_SUM");
            	final String heartBeatDis = rs.getString("HEARTBEAT_DIS_SUM");
            	final String zensoku = rs.getString("ZENSOKU_SUM");
            	final String jinzou = rs.getString("JINZOU_SUM");
            	final String kentou = rs.getString("KENTOU_SUM");
            	final String kekkakuChkDetail = rs.getString("KEKKAKU_CHK_DETAIL_SUM");
            	final String kekkaku = rs.getString("KEKKAKU_SUM");
            	final String otherArelgy = rs.getString("OTHER_ARELGY_SUM");
            	final String otherDisease = rs.getString("OTHER_DISEASE_SUM");
            	final String nancyou = rs.getString("NANCYOU_SUM");
            	final String earSippei = rs.getString("EAR_SIPPEI_SUM");
            	final String noseSubSippei = rs.getString("NOSE_SUB_SIPPEI_SUM");
            	final String throatHeadSippei = rs.getString("THROAT_HEAD_SIPPEI_SUM");
            	final String eyeSippei = rs.getString("EYE_SIPPEI_SUM");
            	final String eyechkInspect = rs.getString("EYECHK_INSPECT_SUM");
            	final String eyeBLv = rs.getString("B_SUM");
            	final String eyeCLv = rs.getString("C_SUM");
            	final String eyeDLv = rs.getString("D_SUM");
            	final String eyeBCDSum = rs.getString("BCD_SUM");
            	final String eyeChkOmit;
            	final String ubaTooth;
            	final String lostAdultTooth;
            	final String toothDmftIndex;
            	final String nakedEye1over;
            	if ("1".equals(_param._useSpecial_Support_School)) {
            		eyeChkOmit = rs.getString("EYECHK_OMIT_SUM");
            		ubaTooth = rs.getString("UBA_TOOTH_SUM");
            		lostAdultTooth = rs.getString("LOSTADULTTOOTH_SUM");
            		toothDmftIndex = rs.getString("TOOTH_DMFT_INDEX");

            		nakedEye1over = null;
            	} else {
            		eyeChkOmit = null;
            		ubaTooth = null;
            		lostAdultTooth = null;
            		toothDmftIndex = null;

            		nakedEye1over = rs.getString("NAKEDEYE_1_OVER_SUM");
            	}

            	final String toothChkInspect = rs.getString("TOOTHCHK_INSPECT_SUM");
            	final String noRemainUbaTooth = rs.getString("NO_REMAIN_UBA_TOOTH_SUM");
            	final String ubaToothHolder = rs.getString("UBA_TOOTH_HOLDER_SUM");
            	final String remainUbaTooth = rs.getString("REMAIN_UBA_TOOTH_SUM");
            	final String totalUbaTooth = rs.getString("TOTAL_UBA_TOOTH_SUM");
            	final String otherChinJoint = rs.getString("OTHER_CHIN_JOINT_SUM");
            	final String otherToothAlignment = rs.getString("OTHER_TOOTH_ALIGNMENT_SUM");
            	final String plaque = rs.getString("PLAQUE_SUM");
            	final String gum = rs.getString("GUM_SUM");
            	final String otherToothSippei = rs.getString("OTHER_TOOTH_SIPPEI_SUM");
            	final String toothDisease = rs.getString("TOOTH_DISEASE_SUM");
            	final String urineChkInspect = rs.getString("URINE_CHK_INSPECT_SUM");
            	final String urineChkSndProtein = rs.getString("URINE_CHKSND_PROTEIN_SUM");
            	final String urineChkSndSugar = rs.getString("URINE_CHKSND_SUGAR_SUM");
            	final String urineChkSndHideBlood = rs.getString("URINE_CHKSND_HIDEBLOOD_SUM");
            	final String atopySkin2 = rs.getString("ATOPY_SKIN_SUM2");
            	final String zensoku2 = rs.getString("ZENSOKU_SUM2");
            	final String noseArelgy = rs.getString("NOSE_ARELGY_SUM");
            	final String eyeArelgy = rs.getString("EYE_ARELGY_SUM");
            	final String foodAllergy = rs.getString("FOOD_ALLERGY_SUM");
            	final String hayFever = rs.getString("HAY_FEVER_SUM");
            	final String medicineAllergy = rs.getString("MEDICINE_ALLERGY_SUM");
            	final String jinmasin = rs.getString("JINMASIN_SUM");
            	final String otherAllergy = rs.getString("OTHER_ALLERGY_SUM");
                PrintData addwk = new PrintData(dispDatType, year, schoolKind, age, sexCd, stuCnt, yase, himan, sekicyuSokuwan, otherSekicyuSokuwan, sisi, atopySkin, otherSkin, heartDis, heartBeatDis, zensoku, jinzou, kentou, kekkakuChkDetail, kekkaku, otherArelgy, otherDisease, nancyou, earSippei, noseSubSippei, throatHeadSippei, eyeSippei, eyechkInspect, nakedEye1over, eyeBLv, eyeCLv, eyeDLv, eyeBCDSum, eyeChkOmit, toothChkInspect, noRemainUbaTooth, ubaToothHolder, remainUbaTooth, totalUbaTooth, ubaTooth, lostAdultTooth, toothDmftIndex, otherChinJoint, otherToothAlignment, plaque, gum, otherToothSippei, toothDisease, urineChkInspect, urineChkSndProtein, urineChkSndSugar, urineChkSndHideBlood, atopySkin2, zensoku2, noseArelgy, eyeArelgy, foodAllergy, hayFever, medicineAllergy, jinmasin, otherAllergy);
                //性別コード+年齢、または性別コード+校種となるよう、キーを設定する。
                retMap.put(sexCd + "-" + age + schoolKind, addwk);
                _hasData = true;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private void outputWithChkSlash(final Vrw32alp svf, final int tblId, final String divColStr, final int divColDetailVal, final int gyo, final String data) {
    	if (data == null) {
        	svf.VrsOutn(divColStr + "_SLASH" + divColDetailVal, gyo, "／");
    	} else {
        	svf.VrsOutn(divColStr + String.valueOf(tblId) + "_" + divColDetailVal, gyo, data);
    	}
		log.debug("COL:"+ divColStr + String.valueOf(tblId) + "_" + divColDetailVal + " _ GYO:" + gyo + " _ VAL:" + (data == null ? "NULL" : data));
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        ///キーとなる在校生情報を整理
        stb.append(" WITH SCHREG_B_DIST_MST AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.SCHOOL_KIND, ");
        stb.append("     T2.SEX, ");
        //年齢は4/2時点の年齢
        stb.append("  YEAR('" + _param._year + "-04-01' - DATE(T2.BIRTHDAY)) AS AGE, ");
        stb.append("     1 AS REC_CNT");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("       ON T3.YEAR = T1.YEAR ");
        stb.append("      AND T3.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            // 高校のみ対象
            stb.append("  AND T3.SCHOOL_KIND = 'H' ");
            stb.append("  AND YEAR('" + _param._year + "-04-01' - DATE(T2.BIRTHDAY)) BETWEEN 15 AND 17 ");
        }
        stb.append(" ), STD_CNT1 AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("     SCHOOL_KIND, ");
        } else {
            stb.append("     AGE, ");
        }
        stb.append("     SEX, ");
        stb.append("     SUM(REC_CNT) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_B_DIST_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEX, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("     SCHOOL_KIND ");
        } else {
            stb.append("     AGE ");
        }
        stb.append(" ), STD_CNT2 AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEX, ");
        stb.append("     SUM(REC_CNT) AS CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_B_DIST_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEX ");
        //目の視力レベル判別用のデータを整理(矯正データを優先し、NULLなら裸眼データを取得。)
        stb.append(" ), CUT_EYE_ELM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     VALUE(R_VISION_MARK, '') AS VISION_MARK, ");
        stb.append("     VALUE(R_VISION, '99') AS VISION ");
        stb.append(" FROM  ");
        stb.append("     MEDEXAM_DET_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND (R_VISION_MARK IS NOT NULL OR R_VISION IS NOT NULL) ");
        stb.append(" UNION ");
        stb.append(" SELECT  ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     VALUE(L_VISION_MARK, '') AS VISION_MARK, ");
        stb.append("     VALUE(L_VISION, '99') AS VISION ");
        stb.append(" FROM  ");
        stb.append("     MEDEXAM_DET_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND (L_VISION_MARK IS NOT NULL OR L_VISION IS NOT NULL) ");
        stb.append(" UNION ");
        stb.append(" SELECT  ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     VALUE(R_BAREVISION_MARK, '') AS VISION_MARK, ");
        stb.append("     VALUE(R_BAREVISION, '99') AS VISION ");
        stb.append(" FROM  ");
        stb.append("     MEDEXAM_DET_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND R_VISION_MARK IS NULL AND R_VISION IS NULL ");
        stb.append("     AND L_VISION_MARK IS NULL AND L_VISION IS NULL ");
        stb.append("     AND (R_BAREVISION_MARK IS NOT NULL OR R_BAREVISION IS NOT NULL) ");
        stb.append(" UNION ");
        stb.append(" SELECT  ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     VALUE(L_BAREVISION_MARK, '') AS VISION_MARK, ");
        stb.append("     VALUE(L_BAREVISION, '99') AS VISION ");
        stb.append(" FROM  ");
        stb.append("     MEDEXAM_DET_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND R_VISION_MARK IS NULL AND R_VISION IS NULL ");
        stb.append("     AND L_VISION_MARK IS NULL AND L_VISION IS NULL ");
        stb.append("     AND (L_BAREVISION_MARK IS NOT NULL OR L_BAREVISION IS NOT NULL) ");
        //目の視力レベル判別用のデータを整理(左右の資料で評価の低いものを選定)
        stb.append(" ), MIN_EYE_SEARCH AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO, ");
        stb.append("     MAX(VALUE(VISION_MARK, '')) AS VISION_MARK, ");
        stb.append("     CASE WHEN MIN(VALUE(VISION, '')) = '99' ");
        stb.append("          THEN '' ");
        stb.append("          ELSE MIN(VALUE(VISION, '')) ");
        stb.append("     END AS VISION ");
        stb.append(" FROM  ");
        stb.append("     CUT_EYE_ELM ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SCHREGNO ");
        //肥満度で判定
        //1.やせ傾向（-20%以下）
        //2.肥満傾向（+20%以上）
        stb.append(" ), MAX_YEAR AS ( ");
        stb.append("     SELECT ");
        stb.append("         MAX(YEAR) AS YEAR ");
        stb.append("     FROM ");
        stb.append("         HEXAM_PHYSICAL_AVG_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR <= '" + _param._year + "' ");
        stb.append(" ), MAX_NENREI_MONTH AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEX, ");
        stb.append("         T1.NENREI_YEAR, ");
        stb.append("         MAX(T1.NENREI_MONTH) AS NENREI_MONTH ");
        stb.append("     FROM ");
        stb.append("         HEXAM_PHYSICAL_AVG_DAT T1 ");
        stb.append("         INNER JOIN MAX_YEAR T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEX, ");
        stb.append("         T1.NENREI_YEAR ");
        stb.append(" ), HEXAM_PHYSICAL AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SEX, ");
        stb.append("         T1.NENREI_YEAR, ");
        stb.append("         T1.STD_WEIGHT_KEISU_A, ");
        stb.append("         T1.STD_WEIGHT_KEISU_B ");
        stb.append("     FROM ");
        stb.append("         HEXAM_PHYSICAL_AVG_DAT T1 ");
        stb.append("         INNER JOIN MAX_NENREI_MONTH T2 ON T2.YEAR = T1.YEAR AND T2.SEX = T1.SEX AND T2.NENREI_YEAR = T1.NENREI_YEAR AND T2.NENREI_MONTH = T1.NENREI_MONTH ");
        stb.append(" ), HIMANDO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.YEAR, ");
        stb.append("         T2.SCHREGNO, ");
        stb.append("         DECIMAL(ROUND((T1.WEIGHT - (HP.STD_WEIGHT_KEISU_A * T1.HEIGHT - HP.STD_WEIGHT_KEISU_B)) / (HP.STD_WEIGHT_KEISU_A * T1.HEIGHT - HP.STD_WEIGHT_KEISU_B) * 100, 1), 5, 1) AS HIMANDO ");
        stb.append("     FROM ");
        stb.append("         MEDEXAM_DET_DAT T1 ");
        stb.append("         LEFT JOIN SCHREG_B_DIST_MST T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN HEXAM_PHYSICAL HP ON HP.SEX = T2.SEX AND HP.NENREI_YEAR = T2.AGE ");
        stb.append("     WHERE ");
        stb.append("         T2.YEAR = '" + _param._year + "' ");
        //各項目のデータ件数カウント判定
        stb.append(" ), DAT_CNT_LST AS ( ");
        stb.append(" SELECT ");
        stb.append("  T2.YEAR AS YEAR, ");
        stb.append("  T2.SCHOOL_KIND AS SCHOOL_KIND, ");
        stb.append("  T2.SCHREGNO AS SCHREGNO, ");
        stb.append("  T2.AGE, ");
        stb.append("  T2.SEX AS SEX, ");
        stb.append("  CASE WHEN T7.HIMANDO IS NULL THEN NULL ");
        stb.append("       WHEN T7.HIMANDO <= -20  THEN 1 ELSE 0 END AS YASE_VAL, ");
        stb.append("  CASE WHEN T7.HIMANDO IS NULL THEN NULL ");
        stb.append("       WHEN T7.HIMANDO >=  20  THEN 1 ELSE 0 END AS HIMAN_VAL, ");
        stb.append("  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SPINERIBCD = '02' THEN 1 ELSE 0 END AS SEKICYU_SOKUWAN_VAL, ");
        stb.append("  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SPINERIBCD = '03' THEN 1 ELSE 0 END AS OTHER_SEKICYU_SOKUWAN_VAL, ");
        stb.append("  CASE WHEN T1.SPINERIBCD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SPINERIBCD = '04' THEN 1 ELSE 0 END AS SISI_VAL, ");
        stb.append("  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SKINDISEASECD = '02' THEN 1 ELSE 0 END AS ATOPY_SKIN_VAL, ");
        stb.append("  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SKINDISEASECD NOT IN ('01', '02') THEN 1 ELSE 0 END AS OTHER_SKIN_VAL, ");
        stb.append("  CASE WHEN T1.HEARTDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.HEARTDISEASECD <> '01' THEN 1 ELSE 0 END AS HEART_DIS_VAL, ");
        stb.append("  CASE WHEN T1.HEART_MEDEXAM IS NULL THEN NULL ");
        stb.append("       WHEN T1.HEART_MEDEXAM <> '01' THEN 1 ELSE 0 END AS HEARTBEAT_DIS_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '01' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ");
        stb.append("       ELSE 0 END AS ZENSOKU_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '02' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ");
        stb.append("       ELSE 0 END AS JINZOU_VAL, ");
        stb.append("  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.TB_NAMECD = '02' THEN 1 ELSE 0 END AS KENTOU_VAL, ");
        stb.append("  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.TB_NAMECD = '03' THEN 1 ELSE 0 END AS KEKKAKU_CHK_DETAIL_VAL, ");
        stb.append("  CASE WHEN T1.TB_NAMECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.TB_NAMECD = '04' THEN 1 ELSE 0 END AS KEKKAKU_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '06' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ");
        stb.append("       ELSE 0 END AS OTHER_ARELGY_VAL, ");
        stb.append("  CASE WHEN T1.OTHERDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.OTHERDISEASECD <> '01' THEN 1 ELSE 0 END AS OTHER_DISEASE_VAL, ");
        stb.append("  CASE WHEN T1.NOSEDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.NOSEDISEASECD = '02' THEN 1 ELSE 0 END AS NANCYOU_VAL, ");
        stb.append("  CASE WHEN T3_003.DET_REMARK7 IS NULL THEN NULL ");
        stb.append("       WHEN T3_003.DET_REMARK7 <> '01' THEN 1 ELSE 0 END AS EAR_SIPPEI_VAL, ");
        stb.append("  CASE WHEN T3_003.DET_REMARK8 IS NULL THEN NULL ");
        stb.append("       WHEN T3_003.DET_REMARK8 <> '01' THEN 1 ELSE 0 END AS NOSE_SUB_SIPPEI_VAL, ");
        stb.append("  CASE WHEN T3_003.DET_REMARK9 IS NULL THEN NULL ");
        stb.append("       WHEN T3_003.DET_REMARK9 <> '01' THEN 1 ");
        stb.append("       ELSE 0 END AS THROAT_HEAD_SIPPEI_VAL, ");
        stb.append("  CASE WHEN T1.EYEDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.EYEDISEASECD <> '01' THEN 1 ");
        stb.append("       ELSE 0 END AS EYE_SIPPEI_VAL, ");
        stb.append("  CASE WHEN T1.R_BAREVISION IS NULL AND T1.R_BAREVISION_MARK IS NULL ");
        stb.append("            AND T1.L_BAREVISION IS NULL AND T1.L_BAREVISION_MARK IS NULL ");
        stb.append("            AND T1.R_VISION IS NULL AND T1.R_VISION_MARK IS NULL ");
        stb.append("            AND T1.L_VISION IS NULL AND T1.L_VISION_MARK IS NULL ");
        stb.append("            THEN NULL ");
        stb.append("       WHEN T1.R_BAREVISION IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.R_BAREVISION_MARK IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.L_BAREVISION IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.L_BAREVISION_MARK IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.R_VISION IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.R_VISION_MARK IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.L_VISION IS NOT NULL THEN 1 ");
        stb.append("       WHEN T1.L_VISION_MARK IS NOT NULL THEN 1 ");
        stb.append("       ELSE 0 END AS EYECHK_INSPECT_VAL, ");
        if (!"1".equals(_param._useSpecial_Support_School)) {//☆
            // 高校のみ対象
            stb.append("  CASE WHEN T1.R_BAREVISION IS NULL AND T1.L_BAREVISION IS NULL THEN NULL ");
            stb.append("       WHEN T1.R_BAREVISION >= '1.0' AND T1.L_BAREVISION >= '1.0' THEN 1 ELSE 0 END AS NAKEDEYE_1_OVER_VAL, ");
        }
        stb.append("  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ");
        stb.append("       WHEN T6.VISION_MARK = 'B' THEN 1 ");
        stb.append("       WHEN T6.VISION_MARK = '' AND ('0.7' <= T6.VISION AND T6.VISION < '1.0') THEN 1 ");
        stb.append("       ELSE 0 END AS B_VAL, ");
        stb.append("  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ");
        stb.append("       WHEN T6.VISION_MARK = 'C' THEN 1 ");
        stb.append("       WHEN T6.VISION_MARK = '' AND ('0.3' <= T6.VISION AND T6.VISION < '0.7') THEN 1 ");
        stb.append("       ELSE 0 END AS C_VAL, ");
        stb.append("  CASE WHEN T6.VISION_MARK = '' AND T6.VISION = '' THEN NULL ");
        stb.append("       WHEN T6.VISION_MARK = 'D' THEN 1 ");
        stb.append("       WHEN T6.VISION_MARK = '' AND T6.VISION < '0.3' THEN 1 ");
        stb.append("       ELSE 0 END AS D_VAL, ");
        if ("1".equals(_param._useSpecial_Support_School)) {//☆
            stb.append("  CASE WHEN T1.R_BAREVISION IS NULL AND T1.R_BAREVISION_MARK IS NULL AND T1.L_BAREVISION IS NULL AND T1.L_BAREVISION_MARK IS NULL THEN 1 ");
            stb.append("       ELSE 0 END AS EYECHK_OMIT_VAL, ");
        }
        stb.append("  CASE WHEN T4.SCHREGNO IS NULL THEN NULL ");
        stb.append("       WHEN T4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS TOOTHCHK_INSPECT_VAL, ");
        stb.append("  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDBABYTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ");
        stb.append("       WHEN T4.REMAINBABYTOOTH = 0 AND T4.REMAINADULTTOOTH = 0 AND T4.TREATEDBABYTOOTH = 0 AND T4.TREATEDADULTTOOTH = 0 THEN 1 ELSE 0 END AS NO_REMAIN_UBA_TOOTH_VAL, ");
        stb.append("  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDBABYTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ");
        stb.append("       WHEN T4.REMAINBABYTOOTH = 0 AND T4.REMAINADULTTOOTH = 0 AND T4.TREATEDBABYTOOTH + T4.TREATEDADULTTOOTH > 0 THEN 1 ELSE 0 END AS UBA_TOOTH_HOLDER_VAL, ");
        stb.append("  CASE WHEN T4.REMAINBABYTOOTH IS NULL AND T4.REMAINADULTTOOTH IS NULL THEN NULL ");
        stb.append("       WHEN T4.REMAINBABYTOOTH + T4.REMAINADULTTOOTH > 0 THEN 1 ELSE 0 END AS REMAIN_UBA_TOOTH_VAL, ");
        if ("1".equals(_param._useSpecial_Support_School)) {//☆
            stb.append("  CASE WHEN T4.REMAINADULTTOOTH IS NULL AND T4.TREATEDADULTTOOTH IS NULL THEN NULL ELSE T4.REMAINADULTTOOTH + T4.TREATEDADULTTOOTH END AS UBA_TOOTH_CNT, ");
            stb.append("  T4.LOSTADULTTOOTH AS LOSTADULTTOOTH_CNT, ");
        }
        stb.append("  CASE WHEN T4.JAWS_JOINTCD2 IS NULL THEN NULL ");
        stb.append("       WHEN T4.JAWS_JOINTCD2 = '03' THEN 1 ELSE 0 END AS OTHER_CHIN_JOINT_VAL, ");
        stb.append("  CASE WHEN T4.JAWS_JOINTCD IS NULL THEN NULL ");
        stb.append("       WHEN T4.JAWS_JOINTCD = '03' THEN 1 ELSE 0 END AS OTHER_TOOTH_ALIGNMENT_VAL, ");
        stb.append("  CASE WHEN T4.PLAQUECD IS NULL THEN NULL ");
        stb.append("       WHEN T4.PLAQUECD = '03' THEN 1 ELSE 0 END AS PLAQUE_VAL, ");
        stb.append("  CASE WHEN T4.GUMCD IS NULL THEN NULL  ");
        stb.append("       WHEN T4.GUMCD = '03' THEN 1 ELSE 0 END AS GUM_VAL, ");
        stb.append("  CASE WHEN T4.OTHERDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T4.OTHERDISEASECD <> '01' THEN 1 ELSE 0 END AS OTHER_TOOTH_SIPPEI_VAL, ");
        stb.append("  CASE WHEN T5_002.TOOTH_REMARK1 IS NULL THEN NULL ");
        stb.append("       WHEN T5_002.TOOTH_REMARK1 <> '01' THEN 1 ELSE 0 END AS TOOTH_DISEASE_VAL, ");
        stb.append("  CASE WHEN T1.ALBUMINURIA1CD IS NULL AND T1.URICSUGAR1CD IS NULL AND T1.URICBLEED1CD IS NULL ");
        stb.append("            AND T1.ALBUMINURIA2CD IS NULL AND T1.URICSUGAR2CD IS NULL AND T1.URICBLEED2CD IS NULL THEN NULL ");
        stb.append("       WHEN T1.ALBUMINURIA1CD IS NOT NULL OR T1.URICSUGAR1CD IS NOT NULL OR T1.URICBLEED1CD IS NOT NULL ");
        stb.append("            OR T1.ALBUMINURIA2CD IS NOT NULL OR T1.URICSUGAR2CD IS NOT NULL OR T1.URICBLEED2CD IS NOT NULL ");
        stb.append("            THEN 1 ELSE 0 END AS URINE_CHK_INSPECT_VAL, ");
        stb.append("  CASE WHEN T1.ALBUMINURIA2CD IS NULL THEN NULL  ");
        stb.append("       WHEN T1.ALBUMINURIA2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_PROTEIN_VAL, ");
        stb.append("  CASE WHEN T1.URICSUGAR2CD IS NULL THEN NULL ");
        stb.append("       WHEN T1.URICSUGAR2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_SUGAR_VAL, ");
        stb.append("  CASE WHEN T1.URICBLEED2CD IS NULL THEN NULL ");
        stb.append("       WHEN T1.URICBLEED2CD >= '03' THEN 1 ELSE 0 END AS URINE_CHKSND_HIDEBLOOD_VAL, ");
        stb.append("  CASE WHEN T1.SKINDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.SKINDISEASECD = '02' THEN 1 ELSE 0 END AS ATOPY_SKIN_VAL2, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '01' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS ZENSOKU_VAL2, ");
        stb.append("  CASE WHEN T3_003.DET_REMARK8 IS NULL THEN NULL ");
        stb.append("       WHEN T3_003.DET_REMARK8 = '02' THEN 1 ELSE 0 END AS NOSE_ARELGY_VAL, ");
        stb.append("  CASE WHEN T1.EYEDISEASECD IS NULL THEN NULL ");
        stb.append("       WHEN T1.EYEDISEASECD = '02' THEN 1 ELSE 0 END AS EYE_ARELGY_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '03' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS FOOD_ALLERGY_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '07' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS HAY_FEVER_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '04' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS MEDICINE_ALLERGY_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '08' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS JINMASIN_VAL, ");
        stb.append("  CASE WHEN T1.MEDICAL_HISTORY1 IS NULL AND T1.MEDICAL_HISTORY2 IS NULL AND T1.MEDICAL_HISTORY3 IS NULL THEN NULL ");
        stb.append("       WHEN '06' IN (T1.MEDICAL_HISTORY1, T1.MEDICAL_HISTORY2, T1.MEDICAL_HISTORY3) THEN 1 ELSE 0 END AS OTHER_ALLERGY_VAL ");
        stb.append(" FROM ");
        stb.append("  MEDEXAM_DET_DAT T1 ");
        stb.append("  LEFT JOIN SCHREG_B_DIST_MST T2 ");
        stb.append("    ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN MEDEXAM_DET_DETAIL_DAT T3_003 ");
        stb.append("    ON T3_003.YEAR = T1.YEAR ");
        stb.append("   AND T3_003.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T3_003.DET_SEQ = '003' ");
        stb.append("  LEFT JOIN MEDEXAM_TOOTH_DAT T4 ");
        stb.append("    ON T4.YEAR = T1.YEAR ");
        stb.append("   AND T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND (T4.ADULTTOOTH IS NOT NULL ");
        stb.append("        OR T4.REMAINADULTTOOTH IS NOT NULL ");
        stb.append("        OR T4.REMAINBABYTOOTH IS NOT NULL ");
        stb.append("        OR T4.TREATEDADULTTOOTH IS NOT NULL ");
        stb.append("        OR T4.TREATEDBABYTOOTH IS NOT NULL ");
        stb.append("        OR T4.BRACK_ADULTTOOTH IS NOT NULL ");
        stb.append("        OR T4.BRACK_BABYTOOTH IS NOT NULL ");
        stb.append("        OR T4.LOSTADULTTOOTH IS NOT NULL) ");
        stb.append("  LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT T5_002 ");
        stb.append("    ON T5_002.YEAR = T1.YEAR ");
        stb.append("   AND T5_002.SCHREGNO = T1.SCHREGNO ");
        stb.append("   AND T5_002.TOOTH_SEQ = '002' ");
        stb.append("  LEFT JOIN MIN_EYE_SEARCH T6 ");
        stb.append("    ON T6.YEAR = T1.YEAR ");
        stb.append("   AND T6.SCHREGNO = T1.SCHREGNO ");
        stb.append("  LEFT JOIN HIMANDO T7 ON T7.YEAR = T1.YEAR AND T7.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("  T2.YEAR = '" + _param._year + "' ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            // 高校のみ対象
            stb.append("  AND T2.SCHOOL_KIND = 'H' ");
        }
        //①集計(男女/年齢(or校種)別)
        stb.append(" ), SUMMARY_TBL_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("  T1.YEAR, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.SCHOOL_KIND, ");
        } else {
            stb.append("  CAST(T1.AGE AS CHAR(2)) AS AGE, ");
        }
        stb.append("  T1.SEX, ");
        stb.append("  SUM(T1.YASE_VAL) AS YASE_SUM, ");
        stb.append("  SUM(T1.HIMAN_VAL) AS HIMAN_SUM, ");
        stb.append("  SUM(T1.SEKICYU_SOKUWAN_VAL) AS SEKICYU_SOKUWAN_SUM, ");
        stb.append("  SUM(T1.OTHER_SEKICYU_SOKUWAN_VAL) AS OTHER_SEKICYU_SOKUWAN_SUM, ");
        stb.append("  SUM(T1.SISI_VAL) AS SISI_SUM, ");
        stb.append("  SUM(T1.ATOPY_SKIN_VAL) AS ATOPY_SKIN_SUM, ");
        stb.append("  SUM(T1.OTHER_SKIN_VAL) AS OTHER_SKIN_SUM, ");
        stb.append("  SUM(T1.HEART_DIS_VAL) AS HEART_DIS_SUM, ");
        stb.append("  SUM(T1.HEARTBEAT_DIS_VAL) AS HEARTBEAT_DIS_SUM, ");
        stb.append("  SUM(T1.ZENSOKU_VAL) AS ZENSOKU_SUM, ");
        stb.append("  SUM(T1.JINZOU_VAL) AS JINZOU_SUM, ");
        stb.append("  SUM(T1.KENTOU_VAL) AS KENTOU_SUM, ");
        stb.append("  SUM(T1.KEKKAKU_CHK_DETAIL_VAL) AS KEKKAKU_CHK_DETAIL_SUM, ");
        stb.append("  SUM(T1.KEKKAKU_VAL) AS KEKKAKU_SUM, ");
        stb.append("  SUM(T1.OTHER_ARELGY_VAL) AS OTHER_ARELGY_SUM, ");
        stb.append("  SUM(T1.OTHER_DISEASE_VAL) AS OTHER_DISEASE_SUM, ");
        stb.append("  SUM(T1.NANCYOU_VAL) AS NANCYOU_SUM, ");
        stb.append("  SUM(T1.EAR_SIPPEI_VAL) AS EAR_SIPPEI_SUM, ");
        stb.append("  SUM(T1.NOSE_SUB_SIPPEI_VAL) AS NOSE_SUB_SIPPEI_SUM, ");
        stb.append("  SUM(T1.THROAT_HEAD_SIPPEI_VAL) AS THROAT_HEAD_SIPPEI_SUM, ");
        stb.append("  SUM(T1.EYE_SIPPEI_VAL) AS EYE_SIPPEI_SUM, ");
        stb.append("  SUM(T1.EYECHK_INSPECT_VAL) AS EYECHK_INSPECT_SUM, ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.NAKEDEYE_1_OVER_VAL) AS NAKEDEYE_1_OVER_SUM, ");
        }
        stb.append("  SUM(T1.B_VAL) AS B_SUM, ");
        stb.append("  SUM(T1.C_VAL) AS C_SUM, ");
        stb.append("  SUM(T1.D_VAL) AS D_SUM, ");
        stb.append("  SUM(T1.B_VAL) + SUM(T1.C_VAL) + SUM(T1.D_VAL) AS BCD_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.EYECHK_OMIT_VAL) AS EYECHK_OMIT_SUM, ");
        }
        stb.append("  SUM(T1.TOOTHCHK_INSPECT_VAL) AS TOOTHCHK_INSPECT_SUM, ");
        stb.append("  SUM(T1.NO_REMAIN_UBA_TOOTH_VAL) AS NO_REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  SUM(T1.UBA_TOOTH_HOLDER_VAL) AS UBA_TOOTH_HOLDER_SUM, ");
        stb.append("  SUM(T1.REMAIN_UBA_TOOTH_VAL) AS REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  SUM(T1.UBA_TOOTH_HOLDER_VAL) + SUM(T1.REMAIN_UBA_TOOTH_VAL) AS TOTAL_UBA_TOOTH_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.UBA_TOOTH_CNT) AS UBA_TOOTH_SUM, ");
            stb.append("  SUM(T1.LOSTADULTTOOTH_CNT) AS LOSTADULTTOOTH_SUM, ");
            stb.append("  SUM(T1.UBA_TOOTH_CNT) + SUM(T1.LOSTADULTTOOTH_CNT) AS TOOTH_DMFT_INDEX, ");
        }
        stb.append("  SUM(T1.OTHER_CHIN_JOINT_VAL) AS OTHER_CHIN_JOINT_SUM, ");
        stb.append("  SUM(T1.OTHER_TOOTH_ALIGNMENT_VAL) AS OTHER_TOOTH_ALIGNMENT_SUM, ");
        stb.append("  SUM(T1.PLAQUE_VAL) AS PLAQUE_SUM, ");
        stb.append("  SUM(T1.GUM_VAL) AS GUM_SUM, ");
        stb.append("  SUM(T1.OTHER_TOOTH_SIPPEI_VAL) AS OTHER_TOOTH_SIPPEI_SUM, ");
        stb.append("  SUM(T1.TOOTH_DISEASE_VAL) AS TOOTH_DISEASE_SUM, ");
        stb.append("  SUM(T1.URINE_CHK_INSPECT_VAL) AS URINE_CHK_INSPECT_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_PROTEIN_VAL) AS URINE_CHKSND_PROTEIN_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_SUGAR_VAL) AS URINE_CHKSND_SUGAR_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_HIDEBLOOD_VAL) AS URINE_CHKSND_HIDEBLOOD_SUM, ");
        //下部の表に出力する項目については、斜線不要なのでNULL=0になるようにする。
        stb.append("  VALUE(SUM(T1.ATOPY_SKIN_VAL2), 0) AS ATOPY_SKIN_SUM2, ");
        stb.append("  VALUE(SUM(T1.ZENSOKU_VAL2), 0) AS ZENSOKU_SUM2, ");
        stb.append("  VALUE(SUM(T1.NOSE_ARELGY_VAL), 0) AS NOSE_ARELGY_SUM, ");
        stb.append("  VALUE(SUM(T1.EYE_ARELGY_VAL), 0) AS EYE_ARELGY_SUM, ");
        stb.append("  VALUE(SUM(T1.FOOD_ALLERGY_VAL), 0) AS FOOD_ALLERGY_SUM, ");
        stb.append("  VALUE(SUM(T1.HAY_FEVER_VAL), 0) AS HAY_FEVER_SUM, ");
        stb.append("  VALUE(SUM(T1.MEDICINE_ALLERGY_VAL), 0) AS MEDICINE_ALLERGY_SUM, ");
        stb.append("  VALUE(SUM(T1.JINMASIN_VAL), 0) AS JINMASIN_SUM, ");
        stb.append("  VALUE(SUM(T1.OTHER_ALLERGY_VAL), 0) AS OTHER_ALLERGY_SUM ");
        stb.append(" FROM ");
        stb.append("  DAT_CNT_LST T1 ");
        stb.append(" GROUP BY ");
        stb.append("  T1.YEAR, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.SCHOOL_KIND, ");
        } else {
            stb.append("  T1.AGE, ");
        }
        stb.append("  T1.SEX ");
        stb.append("), SUMMARY_TBL_ADDTOTALSTU_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     SC1.CNT AS STU_CNT, ");
        stb.append("     TT1.* ");
        stb.append(" FROM ");
        stb.append("     SUMMARY_TBL_DAT TT1 ");
        stb.append("     LEFT JOIN STD_CNT1 SC1 ON TT1.YEAR = SC1.YEAR ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("          AND TT1.SCHOOL_KIND = SC1.SCHOOL_KIND ");
        } else {
            stb.append("          AND TT1.AGE = CAST(SC1.AGE AS CHAR(2)) ");
        }
        stb.append("          AND TT1.SEX = SC1.SEX ");

        //②集計(男女別)
        stb.append(" ), TOTAL_SUMMARY_TBL_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.SEX, ");
        stb.append("  SUM(T1.YASE_VAL) AS YASE_SUM, ");
        stb.append("  SUM(T1.HIMAN_VAL) AS HIMAN_SUM, ");
        stb.append("  SUM(T1.SEKICYU_SOKUWAN_VAL) AS SEKICYU_SOKUWAN_SUM, ");
        stb.append("  SUM(T1.OTHER_SEKICYU_SOKUWAN_VAL) AS OTHER_SEKICYU_SOKUWAN_SUM, ");
        stb.append("  SUM(T1.SISI_VAL) AS SISI_SUM, ");
        stb.append("  SUM(T1.ATOPY_SKIN_VAL) AS ATOPY_SKIN_SUM, ");
        stb.append("  SUM(T1.OTHER_SKIN_VAL) AS OTHER_SKIN_SUM, ");
        stb.append("  SUM(T1.HEART_DIS_VAL) AS HEART_DIS_SUM, ");
        stb.append("  SUM(T1.HEARTBEAT_DIS_VAL) AS HEARTBEAT_DIS_SUM, ");
        stb.append("  SUM(T1.ZENSOKU_VAL) AS ZENSOKU_SUM, ");
        stb.append("  SUM(T1.JINZOU_VAL) AS JINZOU_SUM, ");
        stb.append("  SUM(T1.KENTOU_VAL) AS KENTOU_SUM, ");
        stb.append("  SUM(T1.KEKKAKU_CHK_DETAIL_VAL) AS KEKKAKU_CHK_DETAIL_SUM, ");
        stb.append("  SUM(T1.KEKKAKU_VAL) AS KEKKAKU_SUM, ");
        stb.append("  SUM(T1.OTHER_ARELGY_VAL) AS OTHER_ARELGY_SUM, ");
        stb.append("  SUM(T1.OTHER_DISEASE_VAL) AS OTHER_DISEASE_SUM, ");
        stb.append("  SUM(T1.NANCYOU_VAL) AS NANCYOU_SUM, ");
        stb.append("  SUM(T1.EAR_SIPPEI_VAL) AS EAR_SIPPEI_SUM, ");
        stb.append("  SUM(T1.NOSE_SUB_SIPPEI_VAL) AS NOSE_SUB_SIPPEI_SUM, ");
        stb.append("  SUM(T1.THROAT_HEAD_SIPPEI_VAL) AS THROAT_HEAD_SIPPEI_SUM, ");
        stb.append("  SUM(T1.EYE_SIPPEI_VAL) AS EYE_SIPPEI_SUM, ");
        stb.append("  SUM(T1.EYECHK_INSPECT_VAL) AS EYECHK_INSPECT_SUM, ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.NAKEDEYE_1_OVER_VAL) AS NAKEDEYE_1_OVER_SUM, ");
        }
        stb.append("  SUM(T1.B_VAL) AS B_SUM, ");
        stb.append("  SUM(T1.C_VAL) AS C_SUM, ");
        stb.append("  SUM(T1.D_VAL) AS D_SUM, ");
        stb.append("  SUM(T1.B_VAL) + SUM(T1.C_VAL) + SUM(T1.D_VAL) AS BCD_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.EYECHK_OMIT_VAL) AS EYECHK_OMIT_SUM, ");
        }
        stb.append("  SUM(T1.TOOTHCHK_INSPECT_VAL) AS TOOTHCHK_INSPECT_SUM, ");
        stb.append("  SUM(T1.NO_REMAIN_UBA_TOOTH_VAL) AS NO_REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  SUM(T1.UBA_TOOTH_HOLDER_VAL) AS UBA_TOOTH_HOLDER_SUM, ");
        stb.append("  SUM(T1.REMAIN_UBA_TOOTH_VAL) AS REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  SUM(T1.UBA_TOOTH_HOLDER_VAL) + SUM(T1.REMAIN_UBA_TOOTH_VAL) AS TOTAL_UBA_TOOTH_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SUM(T1.UBA_TOOTH_CNT) AS UBA_TOOTH_SUM, ");
            stb.append("  SUM(T1.LOSTADULTTOOTH_CNT) AS LOSTADULTTOOTH_SUM, ");
            stb.append("  SUM(T1.UBA_TOOTH_CNT) + SUM(T1.LOSTADULTTOOTH_CNT) AS TOOTH_DMFT_INDEX, ");
        }
        stb.append("  SUM(T1.OTHER_CHIN_JOINT_VAL) AS OTHER_CHIN_JOINT_SUM, ");
        stb.append("  SUM(T1.OTHER_TOOTH_ALIGNMENT_VAL) AS OTHER_TOOTH_ALIGNMENT_SUM, ");
        stb.append("  SUM(T1.PLAQUE_VAL) AS PLAQUE_SUM, ");
        stb.append("  SUM(T1.GUM_VAL) AS GUM_SUM, ");
        stb.append("  SUM(T1.OTHER_TOOTH_SIPPEI_VAL) AS OTHER_TOOTH_SIPPEI_SUM, ");
        stb.append("  SUM(T1.TOOTH_DISEASE_VAL) AS TOOTH_DISEASE_SUM, ");
        stb.append("  SUM(T1.URINE_CHK_INSPECT_VAL) AS URINE_CHK_INSPECT_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_PROTEIN_VAL) AS URINE_CHKSND_PROTEIN_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_SUGAR_VAL) AS URINE_CHKSND_SUGAR_SUM, ");
        stb.append("  SUM(T1.URINE_CHKSND_HIDEBLOOD_VAL) AS URINE_CHKSND_HIDEBLOOD_SUM, ");
        //下部の表に出力する項目については、斜線不要なのでNULL=0になるようにする。
        stb.append("  VALUE(SUM(T1.ATOPY_SKIN_VAL2), 0) AS ATOPY_SKIN_SUM2, ");
        stb.append("  VALUE(SUM(T1.ZENSOKU_VAL2), 0) AS ZENSOKU_SUM2, ");
        stb.append("  VALUE(SUM(T1.NOSE_ARELGY_VAL), 0) AS NOSE_ARELGY_SUM, ");
        stb.append("  VALUE(SUM(T1.EYE_ARELGY_VAL), 0) AS EYE_ARELGY_SUM, ");
        stb.append("  VALUE(SUM(T1.FOOD_ALLERGY_VAL), 0) AS FOOD_ALLERGY_SUM, ");
        stb.append("  VALUE(SUM(T1.HAY_FEVER_VAL), 0) AS HAY_FEVER_SUM, ");
        stb.append("  VALUE(SUM(T1.MEDICINE_ALLERGY_VAL), 0) AS MEDICINE_ALLERGY_SUM, ");
        stb.append("  VALUE(SUM(T1.JINMASIN_VAL), 0) AS JINMASIN_SUM, ");
        stb.append("  VALUE(SUM(T1.OTHER_ALLERGY_VAL), 0) AS OTHER_ALLERGY_SUM ");
        stb.append(" FROM ");
        stb.append("  DAT_CNT_LST T1 ");
        stb.append(" GROUP BY ");
        stb.append("  T1.YEAR, ");
        stb.append("  T1.SEX ");
        stb.append("), TOTAL_SUMMARY_TBL_DAT_ADDTOTALSTU_DAT AS ( ");
        stb.append(" SELECT ");
        stb.append("     SC2.CNT AS STU_CNT, ");
        stb.append("     TT2.* ");
        stb.append(" FROM ");
        stb.append("     TOTAL_SUMMARY_TBL_DAT TT2 ");
        stb.append("     LEFT JOIN STD_CNT2 SC2 ON TT2.YEAR = SC2.YEAR ");
        stb.append("          AND TT2.SEX = SC2.SEX ");
        stb.append(" ) ");

        //①、②を並列に並べる(DISP_DATTYPEで、①、②を判別)
        stb.append(" SELECT ");
        stb.append("  '0' AS DISP_DATTYPE, ");
        stb.append("  T1.YEAR, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.SCHOOL_KIND, ");
        } else {
            stb.append("  T1.AGE, ");
        }
        stb.append("  T1.SEX, ");
        stb.append("  T1.STU_CNT, ");
        stb.append("  T1.YASE_SUM, ");
        stb.append("  T1.HIMAN_SUM, ");
        stb.append("  T1.SEKICYU_SOKUWAN_SUM, ");
        stb.append("  T1.OTHER_SEKICYU_SOKUWAN_SUM, ");
        stb.append("  T1.SISI_SUM, ");
        stb.append("  T1.ATOPY_SKIN_SUM, ");
        stb.append("  T1.OTHER_SKIN_SUM, ");
        stb.append("  T1.HEART_DIS_SUM, ");
        stb.append("  T1.HEARTBEAT_DIS_SUM, ");
        stb.append("  T1.ZENSOKU_SUM, ");
        stb.append("  T1.JINZOU_SUM, ");
        stb.append("  T1.KENTOU_SUM, ");
        stb.append("  T1.KEKKAKU_CHK_DETAIL_SUM, ");
        stb.append("  T1.KEKKAKU_SUM, ");
        stb.append("  T1.OTHER_ARELGY_SUM, ");
        stb.append("  T1.OTHER_DISEASE_SUM, ");
        stb.append("  T1.NANCYOU_SUM, ");
        stb.append("  T1.EAR_SIPPEI_SUM, ");
        stb.append("  T1.NOSE_SUB_SIPPEI_SUM, ");
        stb.append("  T1.THROAT_HEAD_SIPPEI_SUM, ");
        stb.append("  T1.EYE_SIPPEI_SUM, ");
        stb.append("  T1.EYECHK_INSPECT_SUM, ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.NAKEDEYE_1_OVER_SUM, ");
        }
        stb.append("  T1.B_SUM, ");
        stb.append("  T1.C_SUM, ");
        stb.append("  T1.D_SUM, ");
        stb.append("  T1.BCD_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.EYECHK_OMIT_SUM, ");
        }
        stb.append("  T1.TOOTHCHK_INSPECT_SUM, ");
        stb.append("  T1.NO_REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  T1.UBA_TOOTH_HOLDER_SUM, ");
        stb.append("  T1.REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  T1.TOTAL_UBA_TOOTH_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T1.UBA_TOOTH_SUM, ");
            stb.append("  T1.LOSTADULTTOOTH_SUM, ");
            stb.append("  T1.TOOTH_DMFT_INDEX, ");
        }
        stb.append("  T1.OTHER_CHIN_JOINT_SUM, ");
        stb.append("  T1.OTHER_TOOTH_ALIGNMENT_SUM, ");
        stb.append("  T1.PLAQUE_SUM, ");
        stb.append("  T1.GUM_SUM, ");
        stb.append("  T1.OTHER_TOOTH_SIPPEI_SUM, ");
        stb.append("  T1.TOOTH_DISEASE_SUM, ");
        stb.append("  T1.URINE_CHK_INSPECT_SUM, ");
        stb.append("  T1.URINE_CHKSND_PROTEIN_SUM, ");
        stb.append("  T1.URINE_CHKSND_SUGAR_SUM, ");
        stb.append("  T1.URINE_CHKSND_HIDEBLOOD_SUM, ");
        stb.append("  T1.ATOPY_SKIN_SUM2, ");
        stb.append("  T1.ZENSOKU_SUM2, ");
        stb.append("  T1.NOSE_ARELGY_SUM, ");
        stb.append("  T1.EYE_ARELGY_SUM, ");
        stb.append("  T1.FOOD_ALLERGY_SUM, ");
        stb.append("  T1.HAY_FEVER_SUM, ");
        stb.append("  T1.MEDICINE_ALLERGY_SUM, ");
        stb.append("  T1.JINMASIN_SUM, ");
        stb.append("  T1.OTHER_ALLERGY_SUM ");
        stb.append(" FROM ");
        stb.append("  SUMMARY_TBL_ADDTOTALSTU_DAT T1 ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("  '1' AS DISP_DATTYPE, ");
        stb.append("  T2.YEAR, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  '合計' AS SCHOOL_KIND, ");
        } else {
            stb.append("  '合計' AS AGE, ");
        }
        stb.append("  T2.SEX, ");
        stb.append("  T2.STU_CNT, ");
        stb.append("  T2.YASE_SUM, ");
        stb.append("  T2.HIMAN_SUM, ");
        stb.append("  T2.SEKICYU_SOKUWAN_SUM, ");
        stb.append("  T2.OTHER_SEKICYU_SOKUWAN_SUM, ");
        stb.append("  T2.SISI_SUM, ");
        stb.append("  T2.ATOPY_SKIN_SUM, ");
        stb.append("  T2.OTHER_SKIN_SUM, ");
        stb.append("  T2.HEART_DIS_SUM, ");
        stb.append("  T2.HEARTBEAT_DIS_SUM, ");
        stb.append("  T2.ZENSOKU_SUM, ");
        stb.append("  T2.JINZOU_SUM, ");
        stb.append("  T2.KENTOU_SUM, ");
        stb.append("  T2.KEKKAKU_CHK_DETAIL_SUM, ");
        stb.append("  T2.KEKKAKU_SUM, ");
        stb.append("  T2.OTHER_ARELGY_SUM, ");
        stb.append("  T2.OTHER_DISEASE_SUM, ");
        stb.append("  T2.NANCYOU_SUM, ");
        stb.append("  T2.EAR_SIPPEI_SUM, ");
        stb.append("  T2.NOSE_SUB_SIPPEI_SUM, ");
        stb.append("  T2.THROAT_HEAD_SIPPEI_SUM, ");
        stb.append("  T2.EYE_SIPPEI_SUM, ");
        stb.append("  T2.EYECHK_INSPECT_SUM, ");
        if (!"1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T2.NAKEDEYE_1_OVER_SUM, ");
        }
        stb.append("  T2.B_SUM, ");
        stb.append("  T2.C_SUM, ");
        stb.append("  T2.D_SUM, ");
        stb.append("  T2.BCD_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T2.EYECHK_OMIT_SUM, ");
        }
        stb.append("  T2.TOOTHCHK_INSPECT_SUM, ");
        stb.append("  T2.NO_REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  T2.UBA_TOOTH_HOLDER_SUM, ");
        stb.append("  T2.REMAIN_UBA_TOOTH_SUM, ");
        stb.append("  T2.TOTAL_UBA_TOOTH_SUM, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  T2.UBA_TOOTH_SUM, ");
            stb.append("  T2.LOSTADULTTOOTH_SUM, ");
            stb.append("  T2.TOOTH_DMFT_INDEX, ");
        }
        stb.append("  T2.OTHER_CHIN_JOINT_SUM, ");
        stb.append("  T2.OTHER_TOOTH_ALIGNMENT_SUM, ");
        stb.append("  T2.PLAQUE_SUM, ");
        stb.append("  T2.GUM_SUM, ");
        stb.append("  T2.OTHER_TOOTH_SIPPEI_SUM, ");
        stb.append("  T2.TOOTH_DISEASE_SUM, ");
        stb.append("  T2.URINE_CHK_INSPECT_SUM, ");
        stb.append("  T2.URINE_CHKSND_PROTEIN_SUM, ");
        stb.append("  T2.URINE_CHKSND_SUGAR_SUM, ");
        stb.append("  T2.URINE_CHKSND_HIDEBLOOD_SUM, ");
        stb.append("  T2.ATOPY_SKIN_SUM2, ");
        stb.append("  T2.ZENSOKU_SUM2, ");
        stb.append("  T2.NOSE_ARELGY_SUM, ");
        stb.append("  T2.EYE_ARELGY_SUM, ");
        stb.append("  T2.FOOD_ALLERGY_SUM, ");
        stb.append("  T2.HAY_FEVER_SUM, ");
        stb.append("  T2.MEDICINE_ALLERGY_SUM, ");
        stb.append("  T2.JINMASIN_SUM, ");
        stb.append("  T2.OTHER_ALLERGY_SUM ");
        stb.append(" FROM ");
        stb.append("  TOTAL_SUMMARY_TBL_DAT_ADDTOTALSTU_DAT T2 ");
        stb.append(" ORDER BY ");
        stb.append("  DISP_DATTYPE, ");
        stb.append("  YEAR, ");
        stb.append("  SEX, ");
        if ("1".equals(_param._useSpecial_Support_School)) {
            stb.append("  SCHOOL_KIND ");
        } else {
            stb.append("  AGE ");
        }

        return stb.toString();
    }

    private class PrintData {
        final String _dispDatType;
        final String _year;
        final String _schoolKind;
        final String _age;
        final String _sexCd;
        final String _stuCnt;
        final String _yase;
        final String _himan;
        final String _sekicyuSokuwan;
        final String _otherSekicyuSokuwan;
        final String _sisi;
        final String _atopySkin;
        final String _otherSkin;
        final String _heartDis;
        final String _heartBeatDis;
        final String _zensoku;
        final String _jinzou;
        final String _kentou;
        final String _kekkakuChkDetail;
        final String _kekkaku;
        final String _otherArelgy;
        final String _otherDisease;
        final String _nancyou;
        final String _earSippei;
        final String _noseSubSippei;
        final String _throatHeadSippei;
        final String _eyeSippei;
        final String _eyechkInspect;
        final String _nakedEye1over;
        final String _eyeBLv;
        final String _eyeCLv;
        final String _eyeDLv;
        final String _eyeBCDSum;
        final String _eyeChkOmit;
        final String _toothChkInspect;
        final String _noRemainUbaTooth;
        final String _ubaToothHolder;
        final String _remainUbaTooth;
        final String _totalUbaTooth;
        final String _ubaTooth;
        final String _lostAdultTooth;
        final String _toothDmftIndex;
        final String _otherChinJoint;
        final String _otherToothAlignment;
        final String _plaque;
        final String _gum;
        final String _otherToothSippei;
        final String _toothDisease;
        final String _urineChkInspect;
        final String _urineChkSndProtein;
        final String _urineChkSndSugar;
        final String _urineChkSndHideBlood;
        final String _atopySkin2;
        final String _zensoku2;
        final String _noseArelgy;
        final String _eyeArelgy;
        final String _foodAllergy;
        final String _hayFever;
        final String _medicineAllergy;
        final String _jinmasin;
        final String _otherAllergy;

        public PrintData (final String dispDatType, final String year, final String schoolKind, final String age,
        		final String sexCd, final String stuCnt, final String yase, final String himan, final String sekicyuSokuwan,
        		final String otherSekicyuSokuwan, final String sisi, final String atopySkin, final String otherSkin,
        		final String heartDis, final String heartBeatDis, final String zensoku, final String jinzou,
        		final String kentou, final String kekkakuChkDetail, final String kekkaku, final String otherArelgy,
        		final String otherDisease, final String nancyou, final String earSippei, final String noseSubSippei,
        		final String throatHeadSippei, final String eyeSippei, final String eyechkInspect, final String nakedEye1over,
        		final String eyeBLv, final String eyeCLv, final String eyeDLv, final String eyeBCDSum, final String eyeChkOmit,
        		final String toothChkInspect, final String noRemainUbaTooth, final String ubaToothHolder, final String remainUbaTooth,
        		final String totalUbaTooth, final String ubaTooth, final String lostAdultTooth, final String toothDmftIndex,
        		final String otherChinJoint, final String otherToothAlignment, final String plaque, final String gum,
        		final String otherToothSippei, final String toothDisease, final String urineChkInspect, final String urineChkSndProtein,
        		final String urineChkSndSugar, final String urineChkSndHideBlood, final String atopySkin2, final String zensoku2,
        		final String noseArelgy, final String eyeArelgy, final String foodAllergy, final String hayFever,
        		final String medicineAllergy, final String jinmasin, final String otherAllergy)
        {
            _dispDatType = dispDatType;
            _year = year;
            _schoolKind = schoolKind;
            _age = age;
            _sexCd = sexCd;
            _stuCnt = stuCnt;
            _yase = yase;
            _himan = himan;
            _sekicyuSokuwan = sekicyuSokuwan;
            _otherSekicyuSokuwan = otherSekicyuSokuwan;
            _sisi = sisi;
            _atopySkin = atopySkin;
            _otherSkin = otherSkin;
            _heartDis = heartDis;
            _heartBeatDis = heartBeatDis;
            _zensoku = zensoku;
            _jinzou = jinzou;
            _kentou = kentou;
            _kekkakuChkDetail = kekkakuChkDetail;
            _kekkaku = kekkaku;
            _otherArelgy = otherArelgy;
            _otherDisease = otherDisease;
            _nancyou = nancyou;
            _earSippei = earSippei;
            _noseSubSippei = noseSubSippei;
            _throatHeadSippei = throatHeadSippei;
            _eyeSippei = eyeSippei;
            _eyechkInspect = eyechkInspect;
            _nakedEye1over = nakedEye1over;
            _eyeBLv = eyeBLv;
            _eyeCLv = eyeCLv;
            _eyeDLv = eyeDLv;
            _eyeBCDSum = eyeBCDSum;
            _eyeChkOmit = eyeChkOmit;
            _toothChkInspect = toothChkInspect;
            _noRemainUbaTooth = noRemainUbaTooth;
            _ubaToothHolder = ubaToothHolder;
            _remainUbaTooth = remainUbaTooth;
            _totalUbaTooth = totalUbaTooth;
            _ubaTooth = ubaTooth;
            _lostAdultTooth = lostAdultTooth;
            _toothDmftIndex = toothDmftIndex;
            _otherChinJoint = otherChinJoint;
            _otherToothAlignment = otherToothAlignment;
            _plaque = plaque;
            _gum = gum;
            _otherToothSippei = otherToothSippei;
            _toothDisease = toothDisease;
            _urineChkInspect = urineChkInspect;
            _urineChkSndProtein = urineChkSndProtein;
            _urineChkSndSugar = urineChkSndSugar;
            _urineChkSndHideBlood = urineChkSndHideBlood;
            _atopySkin2 = atopySkin2;
            _zensoku2 = zensoku2;
            _noseArelgy = noseArelgy;
            _eyeArelgy = eyeArelgy;
            _foodAllergy = foodAllergy;
            _hayFever = hayFever;
            _medicineAllergy = medicineAllergy;
            _jinmasin = jinmasin;
            _otherAllergy = otherAllergy;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75572 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _loginDate;
        final String _useSpecial_Support_School;
        final String _loginSchKind;
        final String _schoolCd;
        final String _schoolName;
        final String _namemstZ010Name1;
        final boolean _isFukuiken;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _loginDate = request.getParameter("CTRL_DATE");
            _useSpecial_Support_School = request.getParameter("useSpecial_Support_School");
            _schoolCd = request.getParameter("SCHOOLCD");
            _loginSchKind = request.getParameter("SCHOOLKIND");

            _schoolName = getSchoolName(db2);

            _namemstZ010Name1 = getNameMstZ010(db2);
            log.debug(" _namemstZ010Name1 = " + _namemstZ010Name1);
            _isFukuiken = "fukuiken".equals(_namemstZ010Name1);
        }
        private String getSchoolName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SCHOOLNAME1 FROM SCHOOL_MST ");
            stb.append(" WHERE YEAR = '" + _year + "' AND SCHOOLCD = '" + _schoolCd + "' AND SCHOOL_KIND = '" + _loginSchKind + "' ");
            final String sql = stb.toString();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	retStr = rs.getString("SCHOOLNAME1");
                }
            } catch (final Exception e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
            }
            return retStr;
        }
        private String getNameMstZ010(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' AND NAME1 IS NOT NULL "));
        }
    }
}

// eof

