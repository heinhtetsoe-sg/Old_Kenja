<?php
//定数定義

class knjm438wModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjm438wModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
        $this->testcd = VARS::post("TESTCD");
    }

    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        $data = false;

        $contents  = "";
        $contents .= "学籍番号,氏名,科目コード,科目名\n";

        $query = knjm438wQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($rowDate = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($rowDate, ",") ."\n";
            $data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        if ($data) {
            $filename = sprintf("%s.csv", $this->year . "定期考査未出席者一覧");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

}
?>
