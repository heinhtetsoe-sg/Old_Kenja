package servletpack.KNJA;

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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.StaffInfo;

/**
 * ＨＲ別名票
 *
 * @author nakasone
 *
 */

public class KNJA224 {

    private static final Log log = LogFactory.getLog(KNJA224.class);
    private static final String FORM_FILE_1 = "KNJA224_1.frm";
    private static final String FORM_FILE_2 = "KNJA224_2.frm";
    private static final String FORM_FILE_3 = "KNJA224_3.frm";
    private static final String PRE_FORM_FILE_4 = "KNJA224_4_";
    private static final String FORM_FILE_MUSASHI = "KNJA224_MUSA.frm";
    private static final String FORM_FILE_MUSASHI2 = "KNJA224_2MUSA.frm";
    private static final String FORM_FILE_CHIBEN = "KNJA224_CHI.frm";
    private static final String FORM_FILE_5 = "KNJA224_5.frm";
    private static final String FORM_FILE_6 = "KNJA224_6.frm";
    private static final String FORM_FILE_7 = "KNJA224_4_426_2.frm";
    private static final String FORM_FILE_8 = "KNJA224_8.frm"; //土佐女子
    private static final String FORM_FILE_9 = "KNJA224_9.frm"; //大阪桐蔭
    private int PAGE_MAX_LINE = 50;
    private static final int PAGE_MAX_COL_A4 = 3;
    private static final int PAGE_MAX_COL_A3 = 8;
    private Map _finschoolMstMap;
    private boolean _hasData;
    private Vrw32alp _svf;
    private Param _param;

    private int lenMax = 0; // 列数MAX値
    private int len = 0;    // 列数カウント用

    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);

        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }
        _param = new Param(db2, request);

        _svf = new Vrw32alp();

        if (_svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        try {
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            printMain(db2);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * 印刷処理メイン
     * @param db2   ＤＢ接続オブジェクト
     * @return      対象データ存在フラグ(有り：TRUE、無し：FALSE)
     * @throws Exception
     */
    private void printMain(final DB2UDB db2) throws SQLException {

        final String form = SelectForm();
        _svf.VrSetForm(form, 1);
        log.info(" form = " + form);

        int colMax = 0;

        if (_param._output.equals("musashi")) {
            colMax = 4;
        } else if (_param._output.equals("chiben")) {
            colMax = 1;
        } else if (_param._output.equals("4")) {
            colMax = 8;
        } else if (_param._output.equals("6")) {
            colMax = 1;
        } else {
            colMax = 3;
        }

        for (int i = 0; i < _param._classSelected.length; i++) {

            String serchClass = _param._classSelected[i];
            // 生徒データを取得
            final List studentList = createStudentInfoData(db2, serchClass);
            // 指示画面にて氏名漢字・出身学校を出力時の場合に出身学校マスタ情報を取得する
            if(studentList.size() > 0 && _param._output.equals("3")){
                _finschoolMstMap = createFinschoolMst(db2);
            }

            for (int j = 0; j < Integer.parseInt(_param._kensuu); j++) {
                if (null == _param._kara) {
                    if (outPutPrintMeisai_1(db2, studentList, colMax)) { // 生徒出力のメソッド
                        _hasData = true;
                    }
                } else {
                    if (outPutPrintMeisai_2(db2, studentList, colMax)) { // 生徒出力のメソッド
                        _hasData = true;
                    }
                }
            }
        }
        if (_hasData){
            _svf.VrEndPage(); // SVFフィールド出力
        }
    }

    /**
     * 生徒の出力（空白行なし）
     */
    private boolean outPutPrintMeisai_1(final DB2UDB db2, List studentList, int colMax) {

        boolean refflg = false;
        int gyo = 1; // 行数カウント用
        len++;
        boolean len_flg = false;

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final StudentInfo student = (StudentInfo) it.next();

            gyo = Integer.parseInt(student._attendno);
            if (gyo > PAGE_MAX_LINE) {
                gyo = gyo - PAGE_MAX_LINE;
                if (!len_flg) {
                    len++;
                }
                len_flg = true;
            }
            if (len > colMax) {
                len = 1;
                _svf.VrEndPage(); // SVFフィールド出力
            }
            printStudent(gyo, student, _param._output.equals("chiben") ? "" : String.valueOf(len));

            refflg = true;
        }

        return  refflg;
    }

    /**
     * 生徒の出力（空白行あり）
     */
    private boolean outPutPrintMeisai_2(final DB2UDB db2, List studentList, int colMax) {

        boolean refflg = false;
        int gyo = 1; // 行数カウント用
        len++;

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final StudentInfo student = (StudentInfo) it.next();

            if (gyo > PAGE_MAX_LINE) {
                gyo = 1;
                len++;
            }
            if (len > colMax) {
                len = 1;
                _svf.VrEndPage(); // SVFフィールド出力
            }
            printStudent(gyo, student, _param._output.equals("chiben") ? "" : String.valueOf(len));

            gyo++;
            refflg = true;
        }

        return  refflg;
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

    private void printStudent(final int gyo, final StudentInfo student, final String slen) {
        // 組略称・担任名出力
        _svf.VrsOut("HR_NAME" + slen, _param._output.equals("musashi") ? student._hrClassName2 : _param._output.equals("chiben") ? student._hrName : student._hrNameabbv);
        if (_param._isSapporo && !StringUtils.isBlank(student._staffname) && !StringUtils.isBlank(student._staffname2)) {
            _svf.VrsOut("STAFFNAME" + slen + "_2", student._staffname);
            _svf.VrsOut("STAFFNAME" + slen + "_3", student._staffname2);
        } else {
            String staffNo = _param._output.equals("chiben") ? "" : "_1";
            _svf.VrsOut("STAFFNAME" + slen + staffNo, student._staffname);
        }

        // 出席番号
        _svf.VrsOutn("CLASS" + slen, gyo, student._hrClass);
        _svf.VrsOutn("ATTENDNO" + slen, gyo, student._attendno);

        // 生徒氏名（英語・日本語）
        String showName = null;

        try {
            showName = _param._staffInfo.getStrEngOrJp(student._name_kana, student._name_eng);
        } catch (Throwable e) {
            showName = student._name_kana;
        }
        if (_param._output.equals("musashi")){
            _svf.VrsOut("NENDO" + slen, _param._year); // 年度
        } else if (_param._output.equals("chiben")){
            _svf.VrsOut("NENDO" + slen, _param._nendo); // 年度
            _svf.VrsOut("MAJOR" + slen, student._majorname); // 学科
        } else if (_param._output.equals("3")){
            _svf.VrsOutn("FIN_SCHOOL" + slen, gyo, (String)_finschoolMstMap.get(student._finschoolcd)); // 出身校
        } else if (_param._output.equals("1")){
            _svf.VrsOutn("KANA" + slen + (getMS932ByteLength(showName) > 18 ? "_2" : ""), gyo, showName); // 氏名かな
        }

        if (_param._output.equals("6")) {
            _svf.VrsOut("HR_NAME", student._hrName);
        }
        // 男:空白、女:'*'
        _svf.VrsOutn("MARK" + slen, gyo, student._sex);

        // 生徒漢字・規則に従って出力
        final String strz = null == student._name ? "" : student._name;
        final int spaceIndex = strz.indexOf("　"); // 空白文字の位置
        int spaceIndex2 = -1; // 空白文字2個目
        String strx = "";
        String stry = "";
        if ("6".equals(_param._output)) {
            String field1 = null;
            String field2 = null;
            if (spaceIndex != -1) {
                spaceIndex2 = strz.indexOf("　", spaceIndex + 1);
                strx = strz.substring(0, spaceIndex); // 姓
                stry = strz.substring(spaceIndex + 1); // 名
                if (strx.length() == 1) {
                    field1 = "LNAME" + slen + "_2"; // 姓１文字
                } else {
                    field1 = "LNAME" + slen + "_1"; // 姓２文字以上
                }
                if (stry.length() == 1) {
                    field2 = "FNAME" + slen + "_2"; // 名１文字
                } else {
                    field2 = "FNAME" + slen + "_1"; // 名２文字以上
                }
            }
            final int nameLen = getMS932ByteCount(student._name);
            if (nameLen <= 18 && strx.length() <= 4 && stry.length() <= 4 && null != field1 && null != field2 && spaceIndex2 == -1) {
                _svf.VrsOutn(field1, gyo, strx);
                _svf.VrsOutn(field2, gyo, stry);
            } else if (nameLen <= 20) {
                _svf.VrsOutn("NAME4", gyo, student._name);                   //空白がない
            } else if (nameLen <= 30) {
                _svf.VrsOutn("NAME5", gyo, student._name);                   //空白がない
            } else {
                _svf.VrsOutn("NAME6", gyo, student._name);                   //空白がない
            }
            final int kanaLen = getMS932ByteCount(showName);
            if (kanaLen <= 26) {
                _svf.VrsOutn("KANA1", gyo, showName);                   //空白がない
            } else if (kanaLen <= 30) {
                _svf.VrsOutn("KANA2", gyo, showName);                   //空白がない
            } else {
                _svf.VrsOutn("KANA3", gyo, showName);                   //空白がない
            }
        } else if ("8".equals(_param._output)) {
            // 2:休学 の場合は、同一年度の場合のみ出力する
            if ("2".equals(student._transfercd)) {
                if ((student._transfer_sdate != null) && formatNendo(student._transfer_sdate).equals(formatNendo(_param._loginDate))) {
                    String suffix = student._transfer_name != null ? "（" + student._transfer_name + "）" : "";
                    String name = StringUtils.defaultString(student._name) + suffix;
                    int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                    String nameFieldName = nameByte > 26 ? "4" : nameByte > 22 ? "3" : nameByte > 18 ? "2" : "1";
                    _svf.VrsOutn("NAME" + slen + "_" + nameFieldName, gyo, name);                   //空白がない
                }
            } else {
                String suffix = student._grd_name != null ? "（" + student._grd_name + "）" : student._transfer_name != null ? "（" + student._transfer_name + "）" : "";
                String name = StringUtils.defaultString(student._name) + suffix;
                int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                String nameFieldName = nameByte > 26 ? "4" : nameByte > 22 ? "3" : nameByte > 18 ? "2" : "1";
                _svf.VrsOutn("NAME" + slen + "_" + nameFieldName, gyo, name);                   //空白がない
            }
        } else {

            // 学籍番号表記
            String slenNo = "";
            if ("5".equals(_param._output) && "1".equals(_param._printSchregno) && !_param._isTosajoshi && !_param._isOsakatoin) {
                _svf.VrsOutn("SCHREGNO" + slen, gyo, student._schregno);
                slenNo = "_2"; // 学籍番号表示用の氏名フィールド
            }

            int ketax = 0;
            int ketay = 0;
            if (spaceIndex != -1) {
                spaceIndex2 = strz.indexOf("　", spaceIndex + 1);
                strx = strz.substring(0, spaceIndex); // 姓
                stry = strz.substring(spaceIndex + 1); // 名
                ketax = getMS932ByteLength(strx);
                ketay = getMS932ByteLength(stry);
            }
            if (_param._output.equals("musashi")) {
                _svf.VrsOutn("NAME" + slen + (getMS932ByteLength(strz) > 22 ? "_3" : getMS932ByteLength(strz) > 14 ? "_2" : ""), gyo, strz);
            } else if (spaceIndex < 0 || _param._output.equals("chiben") && (ketax > 8 || ketay > 8) || 0 <= spaceIndex && spaceIndex < spaceIndex2) {
                _svf.VrsOutn("NAME" + slen + slenNo, gyo, strz); // 空白がない
            } else {
                String slenf;
                if (strx.length() == 1) {
                    slenf = "_2"; // 姓１文字
                } else {
                    slenf = "_1"; // 姓２文字以上
                }
                String sfx = "";
                if ("1".equals(_param._output) || "7".equals(_param._output)) {
                    if ((strx.length() <= 3 && stry.length() <= 3)) {
                        sfx = "_3";
                    } else if ((strx.length() > 4 || stry.length() > 4)) {
                        sfx = "_2";
                    }
                }
                final String field1 = "LNAME" + slen + slenf + sfx + slenNo;
                _svf.VrsOutn(field1, gyo, strx);
                if (stry.length() == 1) {
                    slenf = "_2"; // 名１文字
                } else {
                    slenf = "_1"; // 名２文字以上
                }
                final String field2 = "FNAME" + slen + slenf + sfx + slenNo;
                _svf.VrsOutn(field2, gyo, stry);
            }
        }
    }

    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return ret;
    }

    /** 対象フォーム名を返す * */
    private String SelectForm() {
        String formName = "";

        if (_param._output.equals("musashi")) {     // 武蔵用
            formName = "1".equals(_param._wakuNasi) ? FORM_FILE_MUSASHI2 : FORM_FILE_MUSASHI;
            PAGE_MAX_LINE = 49;
        } else if (_param._output.equals("chiben")) {    // 智辯用
            formName = FORM_FILE_CHIBEN;
        } else if (_param._output.equals("1") || _param._output.equals("7")) {    // 氏名漢字・ふりかな
            formName = FORM_FILE_1;
        } else if (_param._output.equals("2")) {    // 氏名漢字
            formName = FORM_FILE_2;
        } else if (_param._output.equals("3")) {    // 氏名漢字・出身校
            formName = FORM_FILE_1;
        } else if (_param._output.equals("4")) {    // Ａ３ヨコ
            formName = FORM_FILE_3;
        } else if (_param._output.equals("6")) {    // Ａ３ヨコ、教務手帳
            formName = "1".equals(_param._kyoumu) ? FORM_FILE_5 : FORM_FILE_6;
            PAGE_MAX_LINE = "1".equals(_param._kyoumu) ? 45 : 48;
        } else if (_param._output.equals("5")) {
            if (_param._isHirokoudatTuusin) {
                formName = FORM_FILE_7;
            } else if (_param._isTosajoshi) {
                formName = FORM_FILE_8;
                PAGE_MAX_LINE = 50;
            } else if (_param._isOsakatoin) {
                formName = FORM_FILE_9;
                PAGE_MAX_LINE = 60;
            } else {
                formName = PRE_FORM_FILE_4 + _param._height + _param._width + ".frm";
            }
        } else if (_param._output.equals("8")) {
            formName = _param._useFormNameA224;
        }
        return formName;
    }

    /**
     * 生徒データ取得処理
     * @param db2           ＤＢ接続オブジェクト
     * @return              帳票出力対象データリスト
     * @throws Exception
     */
    private List createStudentInfoData(final DB2UDB db2, String keyClass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        final String sql = getStudentInfoSql(keyClass);
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final StudentInfo studentInfo = new StudentInfo(
                    rs.getString("schregno"),
                    rs.getString("attendno"),
                    rs.getString("sex"),
                    rs.getString("name"),
                    rs.getString("name_kana"),
                    rs.getString("name_eng"),
                    rs.getString("HR_CLASS"),
                    rs.getString("hr_name"),
                    rs.getString("hr_nameabbv"),
                    _param._output.equals("musashi") ? rs.getString("hr_class_name2") : null,
                    rs.getString("majorname"),
                    rs.getString("staffname"),
                    rs.getString("staffname2"),
                    rs.getString("finschoolcd"),
                    rs.getString("grd_div"),
                    rs.getString("transfercd"),
                    rs.getString("grd_name"),
                    rs.getString("transfer_name"),
                    rs.getString("transfer_sdate")
                );
                rtnList.add(studentInfo);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(String keyClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TRF AS ( ");
        stb.append("     SELECT ");
        stb.append("         TRF1.schregno, ");
        stb.append("         TRF1.transfercd, ");
        stb.append("         TRF1.transfer_sdate ");
        stb.append("     FROM ");
        stb.append("         schreg_transfer_dat TRF1 ");
        stb.append("         inner join ( ");
        stb.append("             SELECT ");
        stb.append("                 schregno, ");
        stb.append("                 transfercd, ");
        stb.append("                 MAX(transfer_sdate) AS transfer_sdate ");
        stb.append("             FROM ");
        stb.append("                 schreg_transfer_dat ");
        stb.append("             WHERE ");
        stb.append("                 (TO_DATE('" + _param._loginDate + "', 'YYYY/MM/DD') BETWEEN transfer_sdate and transfer_edate) ");
        stb.append("             OR  (transfer_sdate <= TO_DATE('" + _param._loginDate + "', 'YYYY/MM/DD') and transfer_edate IS NULL) ");
        stb.append("             GROUP BY ");
        stb.append("                 schregno, ");
        stb.append("                 transfercd ");
        stb.append("         ) TRF2 on TRF1.schregno = TRF2.schregno and ");
        stb.append("             TRF1.transfercd = TRF2.transfercd and ");
        stb.append("             TRF1.transfer_sdate = TRF2.transfer_sdate ");
        stb.append(" ) ");
        stb.append(" select ");
        stb.append("     value(W3.schregno, '') as schregno,");
        stb.append("     value(W3.attendno, '') as attendno,");
        stb.append("     value(W3.HR_CLASS, '') as HR_CLASS,");
        // 文京の場合、性別の＊を表記しない。
        if (_param._isBunkyo) {
            stb.append("     '' AS sex, ");  // 男:空白、女:''
        } else {
            // 青山学院専用フォームを使用している場合、かつ、
            // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）、
            // 性別の＊を表記しない。
            if ("KNJA224_10.xml".equals(_param._useFormNameA224) && "1".equals(_param._nameNasi)) {
                stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN '' WHEN W4.SEX = '2' THEN '*' ELSE '' END AS sex, ");  // 男:空白、女:'*'
            } else {
                stb.append("     CASE WHEN W4.SEX = '2' THEN '*' ELSE '' END AS sex, ");  // 男:空白、女:'*'
            }
        }
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）
        if ("1".equals(_param._nameNasi)) {
            stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN '' ELSE value(W4.name, '') END AS name,");
            stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN '' ELSE value(W4.name_kana, '') END AS name_kana,");
            stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN '' ELSE value(W4.name_eng, '') END AS name_eng,");
        } else {
            stb.append("     value(W4.name, '') as name,");
            stb.append("     value(W4.name_kana, '') as name_kana,");
            stb.append("     value(W4.name_eng, '') as name_eng,");
        }
        stb.append("     value(W1.hr_name, '') as hr_name,");
        stb.append("     value(W1.hr_nameabbv, '') as hr_nameabbv,");
        if (_param._output.equals("musashi")) {
            stb.append("     value(W1.hr_class_name2, '') as hr_class_name2,");
        }
        stb.append("     value(W5.majorname, '') as majorname, ");
        stb.append("     value(W2.staffname, '') as staffname, ");
        stb.append("     value(W6.staffname, '') as staffname2, ");
        stb.append("     value(W4.finschoolcd, '') as finschoolcd, ");
        stb.append("     W4.grd_div, ");
        stb.append("     TRF.transfercd, ");
        // 青山学院専用フォームを使用している場合、かつ、
        // 退学者・転学者・卒業生の除籍区分名や異動区分名は空白（チェック有りの場合）
        if ("KNJA224_10.xml".equals(_param._useFormNameA224) && "1".equals(_param._nameNasi)) {
            stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN NULL ELSE A003.name1 END AS grd_name,");
            stb.append("     CASE WHEN w4.GRD_DIV IN ('1', '2', '3') THEN NULL ELSE A004.name1 END AS transfer_name,");
        } else {
            stb.append("     A003.name1 AS grd_name, ");
            stb.append("     A004.name1 AS transfer_name, ");
        }
        stb.append("     TRF.transfer_sdate ");
        stb.append(" FROM ");
        stb.append(" schreg_base_mst W4 ");
        stb.append(" INNER JOIN schreg_regd_dat W3 ON W3.schregno = W4.schregno ");
        stb.append(" INNER JOIN schreg_regd_hdat W1 ON W1.year = W3.year and ");
        stb.append("     W1.semester = W3.semester and ");
        stb.append("     W1.grade = W3.grade and ");
        stb.append("     W1.hr_class = W3.hr_class ");
        stb.append(" left join major_mst W5 on W3.coursecd = W5.coursecd and W3.majorcd = W5.majorcd ");
        stb.append(" left join staff_mst W2 on W1.tr_cd1 = W2.staffcd ");
        stb.append(" left join staff_mst W6 on W1.tr_cd2 = W6.staffcd ");
        stb.append(" left join v_name_mst A003 on A003.year = W1.year and ");
        stb.append("     A003.namecd1 = 'A003' and ");
        stb.append("     A003.namecd2 = W4.grd_div ");
        stb.append(" left join TRF on TRF.schregno = W3.schregno ");
        stb.append(" left join v_name_mst A004 on A004.year = W1.year and ");
        stb.append("     A004.namecd1 = 'A004' and ");
        stb.append("     A004.namecd2 = TRF.transfercd ");
        stb.append(" WHERE ");
        stb.append("     W1.year = '" + _param._year + "' and ");
        stb.append("     W1.semester = '" + _param._gakki + "' and ");
        stb.append("     W1.grade || W1.hr_class = '" + keyClass + "' ");
        // 青山学院専用フォームを使用している場合、かつ、
        // 空行を詰めて印字する場合、かつ、
        // 退学者・転学者・卒業生の名前は空白（チェック有りの場合）、
        // 印字対象外とする。
        if ("KNJA224_10.xml".equals(_param._useFormNameA224) && "1".equals(_param._kara) && "1".equals(_param._nameNasi)) {
            stb.append("     and VALUE(w4.GRD_DIV, '0') NOT IN ('1', '2', '3') ");
        }
        stb.append(" order by W3.attendno");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class StudentInfo {
        final String _schregno;
        final String _attendno;
        final String _sex;
        final String _name;
        final String _name_kana;
        final String _name_eng;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _hrClassName2;
        final String _majorname;
        final String _staffname;
        final String _staffname2;
        final String _finschoolcd;
        final String _grd_div;
        final String _transfercd;
        final String _grd_name;
        final String _transfer_name;
        final String _transfer_sdate;

        StudentInfo(
                final String schregno,
                final String attendno,
                final String sex,
                final String name,
                final String name_kana,
                final String name_eng,
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String hrClassName2,
                final String majorname,
                final String staffname,
                final String staffname2,
                final String finschoolcd,
                final String grd_div,
                final String transfercd,
                final String grd_name,
                final String transfer_name,
                final String transfer_sdate
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _sex = sex;
            _name = name;
            _name_kana = name_kana;
            _name_eng = name_eng;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _hrClassName2 = hrClassName2;
            _majorname = majorname;
            _staffname = staffname;
            _staffname2 = staffname2;
            _finschoolcd = finschoolcd;
            _grd_div = grd_div;
            _transfercd = transfercd;
            _grd_name = grd_name;
            _transfer_name = transfer_name;
            _transfer_sdate = transfer_sdate;
        }
    }

    /**
     * 出身学校データ。
     */
    public Map createFinschoolMst(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlFinschoolMst());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("FINSCHOOLCD");
            final String name = rs.getString("FINSCHOOL_NAME");

            rtn.put(code, name);
        }
        DbUtils.closeQuietly(null, ps, rs);

        return rtn;
    }

    private String sqlFinschoolMst() {
        return " select"
                + "    FINSCHOOLCD,"
                + "    value(FINSCHOOL_NAME,'') as FINSCHOOL_NAME"
                + " from"
                + "    FINSCHOOL_MST"
                ;
    }


    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 71873 $ $Date: 2020-01-20 20:03:59 +0900 (月, 20 1 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private String formatNendo(String dateStr) {
        String year = dateStr.substring(0, 4);
        String month = dateStr.substring(5, 7);

        // 1月 ～ 3月は －１年した値を年度とする
        if ("1".equals(month) || "2".equals(month) || "3".equals(month)) {
            year = String.valueOf(Integer.parseInt(year) - 1);
        }

        return year;
    }

    /** パラメータクラス */
    private class Param {

        private final String _year;
        private final String _gakki;
        /** 名票種類 */
        private final String _output;
        /** 教務手帳パターン */
        private final String _kyoumu;
        /** 空白行判定 */
        private final String _kara;
        /** 出力件数 */
        private final String _kensuu;
        /** 学年・組 */
        private final String[] _classSelected;
        /** 列の長さ */
        private final String _width;
        /** 行の高さ */
        private final String _height;
        /** 学籍番号を表記する */
        private final String _printSchregno;
        /** 枠無しか */
        private final String _wakuNasi;
        /** 退学者・転学者・卒業生の名前は空白か */
        private final String _nameNasi;

        /** 生徒氏名（英語・日本語）切替処理追加 */
        private final String _staffCd;
        private StaffInfo _staffInfo;

        private String _useFormNameA224;
        private String _loginDate;


        private boolean _seirekiFlg;
        private String _nendo;
        private boolean _isSapporo;
        private boolean _isBunkyo;
        private boolean _isHirokoudatTuusin;
        private boolean _isTosajoshi;
        private boolean _isOsakatoin;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year      = request.getParameter("YEAR");
            _gakki  = request.getParameter("GAKKI");
            _output = request.getParameter("OUTPUT");
            _kyoumu = request.getParameter("KYOUMU");
            _kensuu = request.getParameter("KENSUU");
            _kara = request.getParameter("KARA");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _width = request.getParameter("WIDTH");
            _height = request.getParameter("HEIGHT");
            _printSchregno = request.getParameter("PRINT_SCHREGNO");
            _wakuNasi = request.getParameter("WAKU_NASI");
            _nameNasi = request.getParameter("NAME_NASI");
            _isHirokoudatTuusin = "1".equals(request.getParameter("HIROKOUDAI_TUUSIN"));
            _isTosajoshi = "1".equals(request.getParameter("TOSAJOSHI"));
            _isOsakatoin = "1".equals(request.getParameter("OSAKATOIN"));
            _seirekiFlg = getSeirekiFlg(db2);
            _nendo = getNendo(db2);
            final String name1 = getNameMstZ010(db2);
            _isSapporo = "sapporo".equals(name1);
            _isBunkyo = "bunkyo".equals(name1);
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _useFormNameA224 = request.getParameter("useFormNameA224");
            _loginDate = request.getParameter("LOGIN_DATE");

            try {
                _staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable t) {
                log.error("exception!", t);
            }
        }

        /**
         * @param isJuniorHiSchool 設定する isJuniorHiSchool。
         */
        private String getNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = null;
            try {
                ps = db2.prepareStatement("SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private boolean getSeirekiFlg(DB2UDB db2) {
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
                DbUtils.closeQuietly(null, ps, rs);
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            }
            return seirekiFlg;
        }

        private String getNendo(final DB2UDB db2) {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(db2, _year + "-01-01") + "度";
        }
    }

}// クラスの括り
