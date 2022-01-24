<?php

require_once('for_php7.php');

class knjl015mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $test_year;

    function knjl015mModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }
    function init() {
        $this->field = array("JUDGEMENT1" => VARS::post("JUDGEMENT1"),
                             "JUDGEMENT2" => VARS::post("JUDGEMENT2"),
                             "JUDGEMENT3" => VARS::post("JUDGEMENT3")
                            );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $data = false;

        $contents = "";
        $contents .= "{$this->test_year}年度入学者　";

        if ($this->field["JUDGEMENT1"]) {
            $contents .= knjl015mQuery::getJudgementName('1', $db);
        }

        if ($this->field["JUDGEMENT1"] && $this->field["JUDGEMENT2"]) {
            $contents .= '・';
        }

        if ($this->field["JUDGEMENT2"]) {
            $contents .= knjl015mQuery::getJudgementName('4', $db);
        }

        if ($this->field["JUDGEMENT3"]) {
            $contents .= '志願者全員';
        }

        $contents .= "\n";

        $headArray = array("受験番号", "受験者名", "カナ", "保護者名", "カナ", "合否判定");

        $contents .= implode(",", $headArray)."\n";

        $query = knjl015mQuery::getStudentSql($this);
        $result = $db->query($query);
        $studentArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);

            $contents .= implode(",", $row)."\n";

            $data = true;
        }
        $result->free();

        //ＣＳＶ
        if ($data) {
            $filename = sprintf("%s.csv", $this->test_year."年度入学者経理用CSV出力");
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
