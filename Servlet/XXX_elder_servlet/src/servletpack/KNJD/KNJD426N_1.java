/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 015cfdb2a20b5edd11579fa7e3026ea46fa12e6c $
 *
 * 作成日: 2020/04/06
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD426N_1 {

    private static final Log log = LogFactory.getLog(KNJD426N_1.class);

    private boolean _hasData;
    private final String HOUTEI = "1";
    private final String JITSU = "2";

    final String FRM_A4 = "1";
    final String FRM_A3 = "2";

    final int OUTPUT_FULL_GYO = 999; //登録された全行出力

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

    /**
     * @param request
     *            リクエスト
     * @param response
     *            レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            //自立活動
            if(_param._printChk01) {
                printStudent(svf, student); //生徒情報
                printPage1(db2, svf, student);
                printPage2(db2, svf, student);
            }
            if(_param._printChk02) printPage3(db2, svf, student);
            if(_param._printChk03) printPage4(db2, svf, student);
            if(_param._printChk04) printPage5(db2, svf, student);
        }
    }

    //生徒情報
    private void printStudent(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD426N_1_1.frm", 1);
        //タイトル
        svf.VrsOut("TITLE", "個別の指導計画");
        //学校名
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);

        //学部
        svf.VrsOut("DEPARTMENT_NAME", student._coursename);
        //年組
        svf.VrsOut("GRADE_NAME", student.getHrName());
        //氏名
        int nameLen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = nameLen > 36 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);
        //かな
        int kanaLen = KNJ_EditEdit.getMS932ByteLength(student._kana);
        final String kanaField = kanaLen > 46 ? "2" : "1";
        svf.VrsOut("KANA" + kanaField, student._kana);
    }

    //1ページ目
    private void printPage1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if(!_param._printSubChk001 && !_param._printSubChk002 && !_param._printSubChk003) return;

        if(_param._printSubChk001) svf.VrsOut("ITEM1_1", _param.getKindName("01", "001")); //項目名称 上段
        if(_param._printSubChk002) svf.VrsOut("ITEM2_1", _param.getKindName("01", "002")); //項目名称 中段
        if(_param._printSubChk003) {
            svf.VrsOut("ITEM3_1", _param.getKindName("01", "003")); //項目名称 下段(左)
            svf.VrsOut("ITEM3_2", _param.getKindName("01", "004")); //項目名称 下段(中)
            svf.VrsOut("ITEM3_3", _param.getKindName("01", "005")); //項目名称 下段(右)
        }

        for (Iterator iterator = student._hreportGuidanceSchregRemark.iterator(); iterator.hasNext();) {
            final HreportGuidanceSchregRemark printData = (HreportGuidanceSchregRemark) iterator.next();
            if(!"01".equals(printData._div)) continue;

            //9学期
            if("9".equals(printData._semester)) {
                if(_param._printSubChk001) {
                    if("1".equals(printData._seq)) VrsOutnRenban(svf, "ITEM1_2", knjobj.retDividString(printData._remark, 90, 4)); //内容 上段
                }

                if(_param._printSubChk002) {
                    if("2".equals(printData._seq)) VrsOutnRenban(svf, "ITEM2_2", knjobj.retDividString(printData._remark, 90, 4)); //内容 中段
                }
            }

            if(_param._printSubChk003 && !"09".equals(printData._semester)) {
                final String semesField = "1".equals(printData._semester) ? "ITEM4" : "2".equals(printData._semester) ? "ITEM5" : "3".equals(printData._semester) ? "ITEM6" : "";
                String divField = "";
                String lenField = "";
                int keta = 0;
                int gyo = 20;

                int length = KNJ_EditEdit.getMS932ByteLength(printData._remark);
                if("3".equals(printData._seq)) {
                    //内容 下段(左)
                    divField = "_1";
                    lenField = length > 400 ? "_2" : "";
                    keta = length > 400 ? 15 : 10;
                } else if("4".equals(printData._seq)) {
                    //内容 下段(中)
                    divField = "_2";
                    lenField = length > 680 ? "_2" : "";
                    keta = length > 680 ? 24 : 17;
                } else if("5".equals(printData._seq)) {
                    //内容 下段(右)
                    divField = "_3";
                    lenField = length > 680 ? "_2" : "";
                    keta = length > 680 ? 24 : 17;
                }
                VrsOutnRenban(svf, semesField + divField + lenField, knjobj.retDividString(printData._remark, keta*2, gyo)); //内容
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //2ページ目
    private void printPage2(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if(!_param._printSubChk006 && !_param._printSubChk007 && !_param._printSubChk008) return;
        svf.VrSetForm("KNJD426N_1_2.frm", 4);

        //上段
        if(_param._printSubChk006) {
            svf.VrsOut("ITEM1_1", _param.getKindName("01", "006")); //項目名称 上段

            String defTarget = "";
            for (Iterator iterator = student._hreportGuidanceSchregSelfreliance.iterator(); iterator.hasNext();) {
                final HreportGuidanceSchregSelfreliance printData = (HreportGuidanceSchregSelfreliance) iterator.next();
                if(!defTarget.equals(printData._self_target)) {
                    final String target = ("01".equals(printData._self_target)) ? "重点目標①" : ("02".equals(printData._self_target)) ? "重点目標②"
                            : ("03".equals(printData._self_target)) ? "重点目標③" : ("04".equals(printData._self_target)) ? "重点目標④"
                            : ("05".equals(printData._self_target)) ? "指導内容①" : ("06".equals(printData._self_target)) ? "指導内容②"
                            : ("07".equals(printData._self_target)) ? "指導内容③" : ("08".equals(printData._self_target)) ? "指導内容④" : "";
                    svf.VrsOut("ITEM2_1", target); //対象
                }
                svf.VrsOut("ITEM2_2", printData._self_div); //区分
                svf.VrsOut("ITEM2_3", printData._self_title); //区分内容
                svf.VrsOut("ITEM2_4", printData._self_item); //項目
                svf.VrsOut("ITEM2_5", printData._self_content); //項目内容
                svf.VrEndRecord();
                defTarget = printData._self_target;
            }
        }

        if(_param._printSubChk007 || _param._printSubChk008) {
            for (Iterator iterator = student._hreportGuidanceSchregRemark.iterator(); iterator.hasNext();) {
                final HreportGuidanceSchregRemark printData = (HreportGuidanceSchregRemark) iterator.next();
                if(!"9".equals(printData._semester)) continue;
                if(!"01".equals(printData._div)) continue;

                //中段
                if("7".equals(printData._seq)) {
                    if(_param._printSubChk007) {
                        svf.VrsOut("ITEM1_1", _param.getKindName("01", "007")); //項目名称
                        VrsOutnRenban2(svf, "ITEM3_1", knjobj.retDividString(printData._remark, 90, 4)); //内容
                    }
                }

                //下段
                if("8".equals(printData._seq)) {
                    if(_param._printSubChk008) {
                        svf.VrsOut("ITEM1_1", _param.getKindName("01", "008")); //項目名称
                        VrsOutnRenban2(svf, "ITEM3_1", knjobj.retDividString(printData._remark, 90, 4)); //内容
                    }
                }
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //3ページ目
    private void printPage3(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if (student._hreportGuidanceSchregSubclassRemark == null || student._hreportGuidanceSchregSubclassRemark.size() == 0) {
            return;
        }
        svf.VrSetForm("KNJD426N_1_3.frm", 4);
        int subclsGrpCnt = 1;
        String subclsGrpStr = "01";
        String baksubclsStr = "";
        for (Iterator iterator = student._hreportGuidanceSchregSubclassRemark.iterator(); iterator.hasNext();) {
            final HreportGuidanceSchregSubclassRemark printData = (HreportGuidanceSchregSubclassRemark) iterator.next();
            if(!"9".equals(printData._semester)) continue;
            if(!"02".equals(printData._div)) continue;

            List subclassName = knjobj.retDividString(printData._subclassname, 2, OUTPUT_FULL_GYO);
            List remark = knjobj.retDividString(printData._remark, 90, OUTPUT_FULL_GYO);
            final int maxLen = subclassName.size() > remark.size() ? subclassName.size() : remark.size();

            if (!baksubclsStr.equals(printData._subclasscd)) {
                if (subclsGrpCnt < 10) {
                    subclsGrpStr = "0" + subclsGrpCnt;
                } else {
                    subclsGrpStr = String.valueOf(subclsGrpCnt);
                }
                subclsGrpCnt++;
            }
            for (int i = 0 ; i < maxLen; i++) {
                svf.VrsOut("GRP1_1", subclsGrpStr); //グループ
                svf.VrsOut("GRP1_2", subclsGrpStr); //グループ
                if(i < subclassName.size()) svf.VrsOut("ITEM1_1", (String) subclassName.get(i)); //科目名
                if(i < remark.size()) svf.VrsOut("ITEM1_2", (String) remark.get(i)); //内容
                svf.VrEndRecord();
            }
            baksubclsStr = printData._subclasscd;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //4ページ目
    private void printPage4(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if ((student._hreportGuidanceSchregSubclass == null || student._hreportGuidanceSchregSubclass.size() == 0)
             && (student._hreportGuidanceSchregRemark == null || student._hreportGuidanceSchregRemark.size() == 0)) {
            return;
        }

        svf.VrSetForm("KNJD426N_1_4.frm", 4);
        //氏名
        svf.VrsOut("NAME", student._name);

        svf.VrsOut("ITEM1_1", "教科等");
        svf.VrsOut("ITEM1_2", "学期");
        svf.VrsOut("ITEM1_3", _param.getKindName("03", "001")); //項目名称 左
        svf.VrsOut("ITEM1_4", _param.getKindName("03", "002")); //項目名称 中
        svf.VrsOut("ITEM1_5", _param.getKindName("03", "003")); //項目名称 右

        //変数を用意
        String beforStr = "";
        String befSubclasscd = ""; //グループで使用
        String beforeSemes = ""; //グループで使用
        String outSubclassCd = ""; //判定で使用
        String outSubSemesCd = ""; //判定で使用
        List subclassName = new ArrayList();
        List semester = new ArrayList();
        List remark1 = new ArrayList();
        List remark2 = new ArrayList(); //上段のみ
        List remark3 = new ArrayList(); //上段のみ
        int subclscdCnt = 1;
        int subsemesCnt = 1;
        String subclsGrpStr = "01";//初期値を設定しておく
        String subsemesGrpStr = "01";

        //上段
        for (Iterator iterator = student._hreportGuidanceSchregSubclass.iterator(); iterator.hasNext();) {
            final HreportGuidanceSchregSubclass printData = (HreportGuidanceSchregSubclass) iterator.next();

            //学期+科目が同一な場合、値を取得。 学期+科目が変わった場合、値を出力。
            if("".equals(beforStr) || beforStr.equals(printData._subclasscd + printData._semester)) {
                //現在科目の取得
                subclassName = knjobj.retDividString(printData._subclassname, 2, OUTPUT_FULL_GYO);
                semester = knjobj.retDividString(printData._semester+"学期", 2, 3);
                if("1".equals(printData._seq)) remark1 = knjobj.retDividString(printData._remark, 24, OUTPUT_FULL_GYO);
                if("2".equals(printData._seq)) remark2 = knjobj.retDividString(printData._remark, 24, OUTPUT_FULL_GYO);
                if("3".equals(printData._seq)) remark3 = knjobj.retDividString(printData._remark, 30, OUTPUT_FULL_GYO);
            } else {
                if (!outSubclassCd.equals(befSubclasscd)) {
                    if (subclscdCnt < 10) {
                        subclsGrpStr = "0" + subclscdCnt;
                    } else {
                        subclsGrpStr = String.valueOf(subclscdCnt);
                    }
                    subclscdCnt++;
                }
                if (!outSubSemesCd.equals(befSubclasscd + beforeSemes)) {
                    if (subsemesCnt < 10) {
                        subsemesGrpStr = "0" + subsemesCnt;
                    } else {
                        subsemesGrpStr = String.valueOf(subsemesCnt);
                    }
                    subsemesCnt++;
                }
                //前科目の出力
                if(outSubclassCd.equals(befSubclasscd)) subclassName = new ArrayList(); //出力済みの科目名称を空とする
                int maxLen = subclassName.size();
                if(maxLen < semester.size()) maxLen = semester.size();
                if(maxLen < remark1.size()) maxLen = remark1.size();
                if(maxLen < remark2.size()) maxLen = remark2.size();
                if(maxLen < remark3.size()) maxLen = remark3.size();
                for (int i = 0 ; i < maxLen; i++) {
                    svf.VrsOut("GRP2_1", subclsGrpStr); //グループ
                    svf.VrsOut("GRP2_2", subsemesGrpStr); //グループ
                    svf.VrsOut("GRP2_3", subsemesGrpStr); //グループ
                    svf.VrsOut("GRP2_4", subsemesGrpStr); //グループ
                    svf.VrsOut("GRP2_5", subsemesGrpStr); //グループ
                    if(i < subclassName.size()) svf.VrsOut("ITEM2_1", (String) subclassName.get(i)); //科目名
                    if(i < semester.size()) svf.VrsOut("ITEM2_2", (String) semester.get(i)); //学期
                    if(i < remark1.size()) svf.VrsOut("ITEM2_3", (String) remark1.get(i)); //内容 左
                    if(i < remark2.size()) svf.VrsOut("ITEM2_4", (String) remark2.get(i)); //内容 中
                    if(i < remark3.size()) svf.VrsOut("ITEM2_5", (String) remark3.get(i)); //内容 右
                    svf.VrEndRecord();
                }
                outSubclassCd = befSubclasscd; //出力した科目コードを保持
                outSubSemesCd = befSubclasscd + beforeSemes;

                //現在科目の取得
                subclassName = knjobj.retDividString(printData._subclassname, 2, OUTPUT_FULL_GYO);
                semester = remark1 = remark2 = remark3 = new ArrayList(); //初期化
                semester = knjobj.retDividString(printData._semester+"学期", 2, 3);
                if("1".equals(printData._seq)) remark1 = knjobj.retDividString(printData._remark, 24, OUTPUT_FULL_GYO);
                if("2".equals(printData._seq)) remark2 = knjobj.retDividString(printData._remark, 24, OUTPUT_FULL_GYO);
                if("3".equals(printData._seq)) remark3 = knjobj.retDividString(printData._remark, 30, OUTPUT_FULL_GYO);
            }
            beforeSemes = printData._semester;
            befSubclasscd = printData._subclasscd;
            beforStr = printData._subclasscd + printData._semester;
        }

        if(!"".equals(beforStr)) {
            //最終科目の出力
            if (!outSubclassCd.equals(befSubclasscd)) {
                if (subclscdCnt < 10) {
                    subclsGrpStr = "0" + subclscdCnt;
                } else {
                    subclsGrpStr = String.valueOf(subclscdCnt);
                }
                subclscdCnt++;
            }
            if (!outSubSemesCd.equals(befSubclasscd + beforeSemes)) {
                if (subsemesCnt < 10) {
                    subsemesGrpStr = "0" + subsemesCnt;
                } else {
                    subsemesGrpStr = String.valueOf(subsemesCnt);
                }
                subsemesCnt++;
            }
            if(outSubclassCd.equals(befSubclasscd)) subclassName = new ArrayList(); //出力済みの科目の名称を空とする
            int maxLen = subclassName.size();
            if(maxLen < semester.size()) maxLen = semester.size();
            if(maxLen < remark1.size()) maxLen = remark1.size();
            if(maxLen < remark2.size()) maxLen = remark2.size();
            if(maxLen < remark3.size()) maxLen = remark3.size();
            for (int i = 0 ; i < maxLen; i++) {
                svf.VrsOut("GRP2_1", subclsGrpStr); //グループ
                svf.VrsOut("GRP2_2", subsemesGrpStr); //グループ
                svf.VrsOut("GRP2_3", subsemesGrpStr); //グループ
                svf.VrsOut("GRP2_4", subsemesGrpStr); //グループ
                svf.VrsOut("GRP2_5", subsemesGrpStr); //グループ
                if(i < subclassName.size()) svf.VrsOut("ITEM2_1", (String) subclassName.get(i)); //科目名
                if(i < semester.size()) svf.VrsOut("ITEM2_2", (String) semester.get(i)); //学期
                if(i < remark1.size()) svf.VrsOut("ITEM2_3", (String) remark1.get(i)); //内容 左
                if(i < remark2.size()) svf.VrsOut("ITEM2_4", (String) remark2.get(i)); //内容 中
                if(i < remark3.size()) svf.VrsOut("ITEM2_5", (String) remark3.get(i)); //内容 右
                svf.VrEndRecord();
            }
        }

        //下段
        boolean firstFlg = true;
        subclassName = semester = remark1 = remark2 = remark3 = new ArrayList(); //初期化
        List kindName = knjobj.retDividString(_param.getKindName("03", "004"), 2, OUTPUT_FULL_GYO); //項目名称
        for (Iterator iterator = student._hreportGuidanceSchregRemark.iterator(); iterator.hasNext();) {
            final HreportGuidanceSchregRemark printData = (HreportGuidanceSchregRemark) iterator.next();
            if("9".equals(printData._semester)) continue;
            if(!"03".equals(printData._div)) continue;
            if(!"4".equals(printData._seq)) continue;

            //値を取得
            if(!firstFlg) kindName = new ArrayList(); //初回以降は空
            semester = knjobj.retDividString(printData._semester+"学期", 2, 3);
            remark1 = knjobj.retDividString(printData._remark, 80, OUTPUT_FULL_GYO);
            int maxLen = kindName.size() > remark1.size() ? kindName.size() : remark1.size();
            if(maxLen < semester.size()) maxLen = semester.size();
            for (int i = 0 ; i < maxLen; i++) {
                //取得した値の出力
                svf.VrsOut("GRP3_1", "1"); //グループ
                svf.VrsOut("GRP3_2", printData._semester); //グループ
                svf.VrsOut("GRP3_3", printData._semester); //グループ
                svf.VrsOut("GRP3_4", printData._semester); //グループ
                svf.VrsOut("GRP3_5", printData._semester); //グループ
                if(i < kindName.size()) svf.VrsOut("ITEM3_1", (String) kindName.get(i)); //項目名称
                if(i < semester.size()) svf.VrsOut("ITEM3_2", (String) semester.get(i)); //学期
                if(i < remark1.size()) svf.VrsOut("ITEM3_3", (String) remark1.get(i)); //内容 下段
                svf.VrEndRecord();
            }
            firstFlg = false;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //5ページ目
    private void printPage5(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        if (student._hreportGuidanceSchregSubclassRemark == null || student._hreportGuidanceSchregSubclassRemark.size() == 0) {
            return;
        }
        svf.VrSetForm("KNJD426N_1_5.frm", 4);
        svf.VrsOut("ITEM1_1", "");
        svf.VrsOut("ITEM1_2", _param.getKindName("04", "001")); //項目名称

        int subclsGrpCnt = 1;
        String subclsGrpStr = "01";
        String baksubclsStr = "";
        for (Iterator iterator = student._hreportGuidanceSchregSubclassRemark.iterator(); iterator.hasNext();) {
            final HreportGuidanceSchregSubclassRemark printData = (HreportGuidanceSchregSubclassRemark) iterator.next();
            if(!"9".equals(printData._semester)) continue;
            if(!"04".equals(printData._div)) continue;

            if (!baksubclsStr.equals(printData._subclasscd)) {
                if (subclsGrpCnt < 10) {
                    subclsGrpStr = "0" + subclsGrpCnt;
                } else {
                    subclsGrpStr = String.valueOf(subclsGrpCnt);
                }
                subclsGrpCnt++;
            }
            //値を取得
            List subclassName = knjobj.retDividString(printData._subclassname, 2, OUTPUT_FULL_GYO);
            List remark = knjobj.retDividString(printData._remark, 90, OUTPUT_FULL_GYO);
            int maxLen = subclassName.size() > remark.size() ? subclassName.size() : remark.size();
            for (int i = 0 ; i < maxLen; i++) {
                //取得した値の出力
                svf.VrsOut("GRP2_1", subclsGrpStr); //グループ
                svf.VrsOut("GRP2_2", subclsGrpStr); //グループ
                if(i < subclassName.size()) svf.VrsOut("ITEM2_1", (String) subclassName.get(i)); //科目名称
                if(i < remark.size()) svf.VrsOut("ITEM2_2", (String) remark.get(i)); //内容
                svf.VrEndRecord();
            }
            baksubclsStr = printData._subclasscd;
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //指定バイト数、指定行数で出力
    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    //指定バイト数、指定行数で出力(EndRecord)
    protected void VrsOutnRenban2(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field, (String) list.get(i));
                svf.VrEndRecord();
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String studentSql = getStudentSql();
            log.debug(" sql =" + studentSql);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String coursename = StringUtils.defaultString(rs.getString("COURSENAME"));
                final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                final String hr_name = StringUtils.defaultString(rs.getString("HR_NAME"));
                final String ghr_name = StringUtils.defaultString(rs.getString("GHR_NAME"));
                final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final String kana = StringUtils.defaultString(rs.getString("KANA"));
                final Student student = new Student(coursename, grade, hr_class, hr_name, ghr_name, schregno, name, kana);

                retList.add(student);
            }
            HreportGuidanceSchregRemark.setHreportGuidanceSchregRemark(db2, _param, retList);
            HreportGuidanceSchregSelfreliance.setHreportGuidanceSchregSelfreliance(db2, _param, retList);
            HreportGuidanceSchregSubclass.setHreportGuidanceSchregSubclass(db2, _param, retList);
            HreportGuidanceSchregSubclassRemark.setHreportGuidanceSchregSubclassRemark(db2, _param, retList);

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   COURSE.COURSENAME, ");
        stb.append("   REGD.GRADE, ");
        stb.append("   REGD.HR_CLASS, ");
        stb.append("   HDAT.HR_NAME, ");
        stb.append("   GHRH.GHR_NAME, ");
        stb.append("   REGD.SCHREGNO, ");
        stb.append("   BASE.NAME, ");
        stb.append("   BASE.NAME_KANA AS KANA ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT REGD ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT HDAT ");
        stb.append("           ON HDAT.YEAR     = REGD.YEAR ");
        stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND HDAT.GRADE    = REGD.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("    INNER JOIN SCHREG_BASE_MST BASE ");
        stb.append("            ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("    LEFT JOIN COURSE_MST COURSE ");
        stb.append("           ON COURSE.COURSECD = REGD.COURSECD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_DAT GHR ");
        stb.append("            ON GHR.YEAR     = REGD.YEAR ");
        stb.append("           AND GHR.SEMESTER = REGD.SEMESTER ");
        stb.append("           AND GHR.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_GHR_HDAT GHRH ");
        stb.append("            ON GHRH.YEAR     = GHR.YEAR ");
        stb.append("           AND GHRH.SEMESTER = GHR.SEMESTER ");
        stb.append("           AND GHRH.GHR_CD   = GHR.GHR_CD ");
        stb.append(" WHERE ");
        stb.append("       REGD.YEAR     = '"+ _param._ctrlYear +"' ");
        stb.append("   AND REGD.SEMESTER = '"+ _param._semester +"' ");
        stb.append("   AND REGD.SCHREGNO IN "+ SQLUtils.whereIn(true, _param._categorySelected) +" ");
        stb.append(" ORDER BY ");
        stb.append("     GHR.GHR_CD, ");
        stb.append("     GHR.GHR_ATTENDNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _coursename;
        final String _grade;
        final String _hr_class;
        final String _hr_name;
        final String _ghr_name;
        final String _schregno;
        final String _name;
        final String _kana;
        List _hreportGuidanceSchregRemark = new ArrayList();
        List _hreportGuidanceSchregSelfreliance = new ArrayList();
        List _hreportGuidanceSchregSubclass = new ArrayList();
        List _hreportGuidanceSchregSubclassRemark = new ArrayList();

        public Student(
                final String coursename,
                final String grade,
                final String hr_class,
                final String hr_name,
                final String ghr_name,
                final String schregno,
                final String name,
                final String kana
        ) {
            _coursename = coursename;
            _grade = grade;
            _hr_class = hr_class;
            _hr_name = hr_name;
            _ghr_name = ghr_name;
            _schregno = schregno;
            _name = name;
            _kana = kana;
        }

        private String getHrName() {
            if (HOUTEI.equals(_param._hukusikiRadio)) {
                return _hr_name;
            } else {
                return _ghr_name;
            }
        }
    }

    private static class HreportGuidanceSchregRemark {
        final String _semester;
        final String _div;
        final String _seq;
        final String _remark;

        public HreportGuidanceSchregRemark(
                final String semester,
                final String div,
                final String seq,
                final String remark
        ) {
            _semester = semester;
            _div = div;
            _seq = seq;
            _remark = remark;
        }

        public static void setHreportGuidanceSchregRemark(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportGuidanceSchregRemarkSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hreportGuidanceSchregRemark = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String div = StringUtils.defaultString(rs.getString("DIV"));
                        final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                        final String remark = StringUtils.defaultString(rs.getString("REMARK"));
                        final HreportGuidanceSchregRemark hreportGuidanceSchregRemark = new HreportGuidanceSchregRemark(semester, div, seq, remark);
                        student._hreportGuidanceSchregRemark.add(hreportGuidanceSchregRemark);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHreportGuidanceSchregRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_REMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR        = '"+ param._ctrlYear +"' ");
            stb.append("   AND T1.SCHREGNO    = ? ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            stb.append("                               FROM HREPORT_GUIDANCE_SCHREG_REMARK_DAT ");
            stb.append("                              WHERE YEAR     = T1.YEAR ");
            stb.append("                                AND SEMESTER = T1.SEMESTER ");
            stb.append("                                AND SCHREGNO = T1.SCHREGNO ");
            stb.append("                                AND DIV      = T1.DIV ");
            stb.append("                                AND SEQ      = T1.SEQ ");
            stb.append("                        ) ");
            stb.append(" ORDER BY T1.SEMESTER, T1.DIV, T1.SEQ ");
            return stb.toString();
        }
    }

    private static class HreportGuidanceSchregSelfreliance {
        final String _self_target;
        final String _self_div;
        final String _self_title;
        final String _self_item;
        final String _self_content;


        public HreportGuidanceSchregSelfreliance(
                final String self_target,
                final String self_div,
                final String self_title,
                final String self_item,
                final String self_content
        ) {
            _self_target = self_target;
            _self_div = self_div;
            _self_title = self_title;
            _self_item = self_item;
            _self_content = self_content;
        }

        public static void setHreportGuidanceSchregSelfreliance(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportGuidanceSchregSelfrelianceSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hreportGuidanceSchregSelfreliance = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String self_target = StringUtils.defaultString(rs.getString("SELF_TARGET"));
                        final String self_div = StringUtils.defaultString(rs.getString("SELF_DIV"));
                        final String self_title = StringUtils.defaultString(rs.getString("SELF_TITLE"));
                        final String self_item = StringUtils.defaultString(rs.getString("SELF_ITEM"));
                        final String self_content = StringUtils.defaultString(rs.getString("SELF_CONTENT"));

                        final HreportGuidanceSchregSelfreliance hreportGuidanceSchregSelfreliance = new HreportGuidanceSchregSelfreliance(self_target, self_div, self_title, self_item, self_content);
                        student._hreportGuidanceSchregSelfreliance.add(hreportGuidanceSchregSelfreliance);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHreportGuidanceSchregSelfrelianceSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SELF_TARGET, ");
            stb.append("   T2.SELF_DIV, ");
            stb.append("   T2.SELF_TITLE, ");
            stb.append("   T2.SELF_ITEM, ");
            stb.append("   T2.SELF_CONTENT ");
            stb.append(" FROM  ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_SELFRELIANCE_DAT T1 ");
            stb.append("   INNER JOIN HREPORT_GUIDANCE_SELFRELIANCE_MST T2 ");
            stb.append("           ON T2.YEAR     = T1.YEAR ");
            stb.append("          AND T2.SELF_DIV = T1.SELF_DIV ");
            stb.append("          AND T2.SELF_SEQ = T1.SELF_SEQ ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR     = '"+ param._ctrlYear +"' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   T1.SELF_TARGET, ");
            stb.append("   T2.SELF_DIV, ");
            stb.append("   T2.SELF_SEQ ");
            return stb.toString();
        }
    }

    private static class HreportGuidanceSchregSubclass {
        final String _semester;
        final String _subclasscd;
        final String _subclassname;
        final String _guidance_pattern;
        final String _unitcd;
        final String _seq;
        final String _remark;

        public HreportGuidanceSchregSubclass(
                final String semester,
                final String subclasscd,
                final String subclassname,
                final String guidance_pattern,
                final String unitcd,
                final String seq,
                final String remark
        ) {
            _semester = semester;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _guidance_pattern = guidance_pattern;
            _unitcd = unitcd;
            _seq = seq;
            _remark = remark;
        }

        public static void setHreportGuidanceSchregSubclass(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportGuidanceSchregSubclassSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hreportGuidanceSchregSubclass = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                        final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                        final String guidance_pattern = StringUtils.defaultString(rs.getString("GUIDANCE_PATTERN"));
                        final String unitcd = StringUtils.defaultString(rs.getString("UNITCD"));
                        final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                        final String remark = StringUtils.defaultString(rs.getString("REMARK"));

                        final HreportGuidanceSchregSubclass hreportGuidanceSchregSubclass = new HreportGuidanceSchregSubclass(semester, subclasscd, subclassname, guidance_pattern, unitcd, seq, remark);
                        student._hreportGuidanceSchregSubclass.add(hreportGuidanceSchregSubclass);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHreportGuidanceSchregSubclassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD,  ");
            stb.append("   T2.SUBCLASSNAME, ");
            stb.append("   T1.GUIDANCE_PATTERN, ");
            stb.append("   T1.UNITCD, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.REMARK ");
            stb.append(" FROM  ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT T1 ");
            stb.append("   INNER JOIN SUBCLASS_MST T2 ");
            stb.append("           ON T2.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("   INNER JOIN V_GRADE_KIND_SCHREG_UNIT_GROUP_DAT T3 ");
            stb.append("           ON T3.YEAR          = T1.YEAR ");
            stb.append("          AND T3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("          AND T3.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");

            stb.append(" WHERE  ");
            stb.append("       T1.YEAR     = '"+ param._ctrlYear +"'  ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY  ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.GUIDANCE_PATTERN, ");
            stb.append("   T1.UNITCD, ");
            stb.append("   T1.SEQ ");
            return stb.toString();
        }
    }

    private static class HreportGuidanceSchregSubclassRemark {
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _semester;
        final String _div;
        final String _remark;

        public HreportGuidanceSchregSubclassRemark(
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String semester,
                final String div,
                final String remark
        ) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _semester = semester;
            _div = div;
            _remark = remark;
        }

        public static void setHreportGuidanceSchregSubclassRemark(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHreportGuidanceSchregSubclassRemarkSql(param);
                log.debug("hreport sql = "+sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._hreportGuidanceSchregSubclassRemark = new ArrayList();

                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                        final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                        final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                        final String semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                        final String div = StringUtils.defaultString(rs.getString("DIV"));
                        final String remark = StringUtils.defaultString(rs.getString("REMARK"));

                        final HreportGuidanceSchregSubclassRemark hreportGuidanceSchregSubclassRemark = new HreportGuidanceSchregSubclassRemark(classcd, subclasscd, subclassname, semester, div, remark);
                        student._hreportGuidanceSchregSubclassRemark.add(hreportGuidanceSchregSubclassRemark);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getHreportGuidanceSchregSubclassRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD,  ");
            stb.append("   T2.SUBCLASSNAME, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.DIV, ");
            stb.append("   T1.REMARK ");
            stb.append(" FROM  ");
            stb.append("   HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT T1 ");
            stb.append("   INNER JOIN SUBCLASS_MST T2 ");
            stb.append("           ON T2.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" WHERE  ");
            stb.append("       T1.YEAR     = '"+ param._ctrlYear +"'  ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append("   AND T1.RECORD_DATE = (SELECT MAX(RECORD_DATE) ");
            stb.append("                               FROM HREPORT_GUIDANCE_SCHREG_SUBCLASS_REMARK_DAT L1 ");
            stb.append("                              WHERE L1.YEAR          = T1.YEAR ");
            stb.append("                                AND L1.SEMESTER      = T1.SEMESTER ");
            stb.append("                                AND L1.SCHREGNO      = T1.SCHREGNO ");
            stb.append("                                AND L1.DIV           = T1.DIV ");
            stb.append("                                AND L1.CLASSCD       = T1.CLASSCD ");
            stb.append("                                AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("                                AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                                AND L1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("                        ) ");
            stb.append(" ORDER BY  ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.DIV ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75386 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _hukusikiRadio;        //クラス種別(1:法定クラス 2:実クラス)
        final String _schoolKind;
        final String _ghrCd;
        final String _outputPtrn;           //帳票パターン(1:知的用)
        final String[] _categorySelected;
        final String _moveDate;
        final String _printDate;
        final boolean _printChk01; //1・2ページ
        final boolean _printSubChk001; //1ページ 上段
        final boolean _printSubChk002; //1ページ 中段
        final boolean _printSubChk003; //1ページ 下段(3枠)
        final boolean _printSubChk006; //2ページ 上段
        final boolean _printSubChk007; //2ページ 中段
        final boolean _printSubChk008; //2ページ 下段
        final boolean _printChk02; //3ページ
        final boolean _printChk03; //4ページ
        final boolean _printChk04; //5ページ
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _schoolName;
        final String _semesterName;
        final Map _kindNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _hukusikiRadio = request.getParameter("HUKUSIKI_RADIO");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ghrCd = request.getParameter("GHR_CD");
            _outputPtrn = request.getParameter("OUTPUT_PTRN");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _moveDate = request.getParameter("MOVE_DATE");
            _printDate = request.getParameter("PRINT_DATE");
            _printChk01 = "01".equals(request.getParameter("PRINT_CHK01"));
            _printSubChk001 = "001".equals(request.getParameter("PRINT_SUB_CHK001"));
            _printSubChk002 = "002".equals(request.getParameter("PRINT_SUB_CHK002"));
            _printSubChk003 = "003".equals(request.getParameter("PRINT_SUB_CHK003"));
            _printSubChk006 = "006".equals(request.getParameter("PRINT_SUB_CHK006"));
            _printSubChk007 = "007".equals(request.getParameter("PRINT_SUB_CHK007"));
            _printSubChk008 = "008".equals(request.getParameter("PRINT_SUB_CHK008"));
            _printChk02 = "02".equals(request.getParameter("PRINT_CHK02"));
            _printChk03 = "03".equals(request.getParameter("PRINT_CHK03"));
            _printChk04 = "04".equals(request.getParameter("PRINT_CHK04"));
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2);
            _semesterName = getSemesterName(db2);
            _kindNameMap = getKindNameMap(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            final String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' AND SCHOOLCD = '000000000000' AND SCHOOL_KIND = '" + _schoolKind + "' ";
            String retName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.error("school_mst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private String getSemesterName(final DB2UDB db2) {
            final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ";
            String retName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.error("semester_mst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private Map getKindNameMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();

            final String sql = getKindNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Map map = new HashMap();
                    final String kindNo = StringUtils.defaultString(rs.getString("KIND_NO"));
                    final String kindSeq = StringUtils.defaultString(rs.getString("KIND_SEQ"));
                    final String kindRemark = StringUtils.defaultString(rs.getString("KIND_REMARK"));
                    if (rtnMap.containsKey(kindNo)) {
                        map = (Map) rtnMap.get(kindNo);
                    }
                    map.put(kindSeq, kindRemark);
                    rtnMap.put(kindNo, map);
                }
            } catch (SQLException ex) {
                log.error("setSubclassList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getKindNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     KIND_NO, ");
            stb.append("     KIND_SEQ, ");
            stb.append("     KIND_REMARK ");
            stb.append(" FROM ");
            stb.append("     HREPORT_GUIDANCE_KIND_NAME_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     KIND_NO, ");
            stb.append("     KIND_SEQ ");

            return stb.toString();
        }

        private String getKindName(final String kindNo, final String kindSeq) {
            String kindName = "";
            if(_kindNameMap.containsKey(kindNo)) {
                final Map map = (Map) _kindNameMap.get(kindNo);
                if(map.containsKey(kindSeq)) {
                    kindName = (String) map.get(kindSeq);
                }
            }
            return kindName;
        }
    }
}

// eof
