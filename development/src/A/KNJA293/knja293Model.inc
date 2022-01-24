<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knja293Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja293Model()
    {
    }
    function init()
    {
        $this->ObjYear = CTRL_YEAR;
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),      //入試区分
                             "OUTPUT"       =>  VARS::post("OUTPUT")        //出力対象
                     );
        $this->cmd         = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        //学納金CSVヘッダー取得
        $getHeaderRow = array();
        $getHeaderRow = $db->getRow(knja293Query::getHeaderQuery($this), DB_FETCHMODE_ASSOC);
        
        //科目取得
        $testsubclassList = array();
        $result = $db->query(knja293Query::getNameMst($this, "L009"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubclassList[$row["NAMECD2"]] = $row["NAME1"];
        }

        //ヘッダデータ
        $header["ROWNO"]                = "No.";
        $header["GRADE"]                = "学年";
        $header["HR_CLASS"]             = "組";
        $header["ATTENDNO"]             = "出席番号";
        $header["TESTDIV"]              = "入試区分コード";
        $header["TESTDIV_NAME"]         = "入試区分名";
        $header["EXAMCOURSECD"]         = "志望コースコード";
        $header["EXAMCOURSE_NAME"]      = "志望コース名";
        $header["ENTER_COURSECODE"]     = "入学コースコード";
        $header["ENTER_COURSECODENAME"] = "入学コース名";
        $header["SUC_COURSECODE"]       = "合格コースコード";
        $header["SUC_COURSECODENAME"]   = "合格コース名";
        $header["JUDGEMENT"]            = "合否コード";
        $header["JUDGEMENT_NAME"]       = "合否";
        $header["EXAMNO"]               = "受験番号";
        $header["NAME"]                 = "受験者氏名";
        $header["NAME_KANA"]            = "かな氏名";
        $header["SEX"]                  = "性別コード";
        $header["SEX_NAME"]             = "性別";
        $header["FS_CD"]                = "出身中学校コード";
        $header["FINSCHOOL_NAME_ABBV"]  = "出身中学校名";
        $header["RECEPTDATE"]           = "受付日付";
        $header["ONGAKU1_KIBOU"]        = "選択科目(音楽Ⅰ)";
        $header["BIJYUTU1_KIBOU"]       = "選択科目(美術Ⅰ)";
        $header["SHODOU1_KIBOU"]        = "選択科目(書道Ⅰ)";
        $header["ENTDIV"]               = "入辞区分コード";
        $header["ENTDIV_NAME"]          = "入辞区分";
        $header["PROCEDUREDATE"]        = "入学手続日";
        $header["ENT_MONEY"]            = $getHeaderRow["ENT_MONEY_NAME"];
        $header["FAC_MONEY"]            = $getHeaderRow["FAC_MONEY_NAME"];
        $header["REMARK1_MONEY"]        = $getHeaderRow["REMARK1_MONEY_NAME"];
        $header["REMARK2_MONEY"]        = $getHeaderRow["REMARK2_MONEY_NAME"];
        $header["REMARK3_MONEY"]        = $getHeaderRow["REMARK3_MONEY_NAME"];
        $header["TOTAL_MONEY"]          = "計";
        //
        $header["SHDIV"]                = "専併コード";
        $header["SHDIV_NAME"]           = "専併";
        $header["REMARK9"]              = "内諾コード";
        $header["REMARK9_NAME"]         = "内諾";
        $header["BIRTHDAY"]             = "生年月日";
        $header["ZIPCD"]                = "郵便番号";
        $header["ADDRESS1"]             = "現住所1";
        $header["ADDRESS2"]             = "現住所2";
        $header["TELNO"]                = "電話番号";
        $header["FS_DAY"]               = "卒業年月日";
        $header["FS_GRDDIV"]            = "既卒コード";
        $header["FS_GRDDIV_NAME"]       = "既卒";
        $header["GNAME"]                = "保護者氏名";
        $header["GKANA"]                = "保護者かな";
        $header["GZIPCD"]               = "保護者郵便番号";
        $header["GADDRESS1"]            = "保護者現住所1";
        $header["GADDRESS2"]            = "保護者現住所2";
        $header["GTELNO"]               = "保護者電話番号";
        $header["REMARK7"]              = "備考1-奨学生コード";
        $header["REMARK7_NAME"]         = "備考1-奨学生";
        $header["REMARK1"]              = "備考1";
        $header["REMARK2"]              = "備考2";
        $header["REMARK3"]              = "備考3";
        $header["REMARK4"]              = "備考4";
        $header["REMARK8"]              = "備考5-奨学生コード";
        $header["REMARK8_NAME"]         = "備考5-奨学生";
        $header["REMARK5"]              = "備考5";
        $header["REMARK6"]              = "備考6";
        $header["CONFIDENTIAL_RPT01"]   = "国語";
        $header["CONFIDENTIAL_RPT02"]   = "社会";
        $header["CONFIDENTIAL_RPT03"]   = "数学";
        $header["CONFIDENTIAL_RPT04"]   = "理科";
        $header["CONFIDENTIAL_RPT05"]   = "音楽";
        $header["CONFIDENTIAL_RPT06"]   = "美術";
        $header["CONFIDENTIAL_RPT07"]   = "保体";
        $header["CONFIDENTIAL_RPT08"]   = "技家";
        $header["CONFIDENTIAL_RPT09"]   = "外国";
        $header["TOTAL_ALL"]            = "評定合計";
        $header["TOTAL5"]               = "5科目評定合計";
        $header["KASANTEN_ALL"]         = "段階";
        $header["ABSENCE_DAYS"]         = "欠席日数（1年）";
        $header["ABSENCE_DAYS2"]        = "欠席日数（2年）";
        $header["ABSENCE_DAYS3"]        = "欠席日数（3年）";
        $header["ABSENCE_REMARK"]       = "主な欠席理由（1年）";
        $header["ABSENCE_REMARK2"]      = "主な欠席理由（2年）";
        $header["ABSENCE_REMARK3"]      = "主な欠席理由（3年）";
        $header["REMARK1_004"]          = "クラブ";
        $header["TOKU_REMARK1"]         = "特活・部活・特記事項要約";
        $header["E_TOTAL"]              = "合計";
        foreach ($testsubclassList as $val => $label) {
            $header["EXAM_SCORE".$val]      = $label;
        }

        $contents  = "";    //クリア

        $header_data = true;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knja293Query::selectMainQuery($this, $testsubclassList);

        $dataNo = 0;
        $dataTemp = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $dataNo++;
            $row["ROWNO"] = $dataNo;
            $row["RECEPTDATE"] = str_replace("-", "/", $row["RECEPTDATE"]);
            $row["PROCEDUREDATE"] = str_replace("-", "/", $row["PROCEDUREDATE"]);
            $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
            $row["FS_DAY"] = str_replace("-", "/", $row["FS_DAY"]);

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";
//            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "受験者情報CSV";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
