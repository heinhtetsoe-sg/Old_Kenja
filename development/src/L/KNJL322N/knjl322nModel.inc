<?php

require_once('for_php7.php');


/**
 * 定数定義
*/

class knjl322nModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl322nModel()
    {
    }
    function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;
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
        $getHeaderRow = $db->getRow(knjl322nQuery::getHeaderQuery($this), DB_FETCHMODE_ASSOC);
        
        //ヘッダデータ
        $header =  array("1"    => "No.",
                         "2"    => "入試区分",
                         "3"    => "志望区分",
                         "4"    => "入学コース",
                         "5"    => "合格コース",
                         "6"    => "受験番号",
                         "7"    => "受験者氏名",
                         "8"    => "かな氏名",
                         "9"    => "性別",
                         "10"   => "中学CD",
                         "11"   => "出身中学校名",
                         "12"   => "受付日付",
                         "13"   => "選択科目(音楽Ⅰ)",
                         "14"   => "選択科目(美術Ⅰ)",
                         "15"   => "選択科目(書道Ⅰ)",
                         "16"   => "入辞区分",
                         "17"   => "入学手続日",
                         "18"   => $getHeaderRow["ENT_MONEY_NAME"],
                         "19"   => $getHeaderRow["FAC_MONEY_NAME"],
                         "20"   => $getHeaderRow["REMARK1_MONEY_NAME"],
                         "21"   => $getHeaderRow["REMARK2_MONEY_NAME"],
                         "22"   => $getHeaderRow["REMARK3_MONEY_NAME"],
                         "23"   => "計");

        $contents  = "";    //クリア

        $header_data = true;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $query = knjl322nQuery::selectMainQuery($this);

        $dataTemp = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $row["RECEPTDATE"] = str_replace("-", "/", $row["RECEPTDATE"]);
            $row["PROCEDUREDATE"] = str_replace("-", "/", $row["PROCEDUREDATE"]);

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
