<?php

require_once('for_php7.php');

//定数定義

class knjm275mModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjm275mModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");

        $this->getPropertiesAll();
    }

    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        $header = array("学籍番号",
                        "氏名",
                        "氏名かな",
                        "性別",
                        "卒業予定",
                        "教育課程コード",
                        "科目コード",
                        "科目名",
                        "講座コード",
                        "講座名",
                        "レポート回数",
                        "提出基準日",
                        "初回受付日",
                        "最終返送日",
                        "最終レポート評価",
                        "再提出回数"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;
        $query = knjm275mQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataContents .= $row["SCHREGNO"] . ",";
            $dataContents .= $row["NAME"] . ",";
            $dataContents .= $row["NAME_KANA"] . ",";
            $dataContents .= $row["SEX_NAME"] . ",";
            $dataContents .= $row["BASE_REMARK1"] . ",";
            $dataContents .= $row["CURRICULUM_CD"] . ",";
            $dataContents .= $row["SUBCLASSCD"] . ",";
            $dataContents .= $row["SUBCLASSNAME"] . ",";
            $dataContents .= $row["CHAIRCD"] . ",";
            $dataContents .= $row["CHAIRNAME"] . ",";
            $dataContents .= $row["STANDARD_SEQ"] . ",";
            $dataContents .= $row["STANDARD_DATE"] . ",";
            $dataContents .= $row["RECEIPT_DATE"] . ",";
            $dataContents .= $row["GRAD_DATE"] . ",";
            $dataContents .= $row["GRAD_VALUE"] . ",";
            $dataContents .= $row["REPRESENT_SEQ"];
            $dataContents .= $lf;

            $data = true;
        }
        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", $this->year . "レポート成績一覧");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
