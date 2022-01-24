<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjl322bModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl322bModel()
    {
    }
    function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "OUTPUT"       =>  VARS::post("OUTPUT")        //出力対象
                     );
        $this->cmd         = VARS::request("cmd");
    }

    //ＣＳＶ出力
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダデータ1
        $header1 = array("1"    => date("Y/m/d H:i:s"),
                         "2"    => "",
                         "3"    => "",
                         "4"    => "",
                         "5"    => "",
                         "6"    => "",
                         "7"    => "",
                         "8"    => "",
                         "9"    => "",
                         "10"   => "",
                         "11"   => "",
                         "12"   => "",
                         "13"   => "",
                         "14"   => "",
                         "15"   => "",
                         "16"   => "",
                         "17"   => "",
                         "18"   => "",
                         "19"   => "",
                         "20"   => "",
                         "21"   => "");
        //ヘッダデータ2
        $header2 = array("1"    => "No.",
                         "2"    => "入試区分",
                         "3"    => "合否区分略称",
                         "4"    => "入学コース",
                         "5"    => "入学区分",
                         "6"    => "希望コース",
                         "7"    => "受験番号",
                         "8"    => "受験者氏名",
                         "9"    => "かな氏名",
                         "10"   => "性別",
                         "11"   => "中学CD",
                         "12"   => "中学校名",
                         "13"   => "受付日付",
                         "14"   => "進路希望",
                         "15"   => "芸術科目の希望",
                         "16"   => "入辞区分",
                         "17"   => "入学手続日",
                         "18"   => "手続受付番号",
                         "19"   => "入学金",
                         "20"   => "施設拡充費",
                         "21"   => "計");

        $contents  = "";    //クリア

        $header_data = true;   //出力フラグ
        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjl322bQuery::selectMainQuery($this);

        $dataTemp = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row["RECEPTDATE"] = str_replace("-", "/", $row["RECEPTDATE"]);
            if ($row["REMARK1"] == "1") {
                $row["REMARK1"] = "4年制大学";
            } else if ($row["REMARK1"] == "2") {
                $row["REMARK1"] = "短期大学";
            } else if ($row["REMARK1"] == "3") {
                $row["REMARK1"] = "専門学校";
            } else if ($row["REMARK1"] == "4") {
                $row["REMARK1"] = "就職";
            } else if ($row["REMARK1"] == "5") {
                $row["REMARK1"] = "その他";
            }
            
            if ($row["REMARK678"]) {
                $remark_array = array();
                $remark_array = explode("-", $row["REMARK678"]);
                $row["REMARK678"] = "";
                if ($remark_array[0] != "") $row["REMARK678"] .= '音楽Ⅰ ';
                if ($remark_array[1] != "") $row["REMARK678"] .= '美術Ⅰ ';
                if ($remark_array[2] != "") $row["REMARK678"] .= '書道Ⅰ';
            }
            
            if ($row["ENT_MONEY"] != "" || $row["FAC_MONEY"] != "") {
                $row["GOUKEI"] = $row["ENT_MONEY"] + $row["FAC_MONEY"];
            }
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "1") {
                $subCourse = "(全て)";
            } else if ($this->field["OUTPUT"] == "2") {
                $subCourse = "(合格者のみ)";
            } else {
                $subCourse = "(不合格者のみ)";
            }
            $couse = "合否台帳CSV".$subCourse;
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
