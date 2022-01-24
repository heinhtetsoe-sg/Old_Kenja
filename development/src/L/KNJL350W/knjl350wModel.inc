<?php

require_once('for_php7.php');

    //定数定義
//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}

class knjl350wModel extends Model {
    var $cmd;
    var $gc_select;                     //セレクトメニュー
    var $control_data = array();        //コントロールのデータを保持
    var $staffcd;                       //セキュリティーチェック
    var $sec_competence;                //権限保持
    var $updated = array();             //UPDATED値保持
    var $field = array();
    var $fields = array();
    var $schoolcd;  //学校コード

    function knjl350wModel() {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->gc_select    = VARS::post("gc_select");        //プルダウンメニュー

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd,"KNJL350W");

        $this->field = array("YEAR"             => VARS::post("YEAR"),
                             "DOC_NUMBER"       => VARS::post("DOC_NUMBER"),
                             "APPLICANTDIV"     => VARS::post("APPLICANTDIV"),
                             "TESTDIV"          => VARS::post("TESTDIV"),
                             "RUIKEI_DIV"       => VARS::post("RUIKEI_DIV"),
                             "TESTDIV2"         => VARS::post("TESTDIV2"),
                             "REPORT"           => VARS::post("REPORT"),
                            );

        $this->field["TESTDIV2"] = $this->field["TESTDIV2"] ? $this->field["TESTDIV2"] : "0";
        //CSVファイル
        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->fields = array();
        $totalData = array();
        foreach ($this->cmc as $cmcKey => $cmcVal) {
            $subTotalData = array();
            $setCmc = $cmcVal["COURSECD"]."_".$cmcVal["MAJORCD"]."_".$cmcVal["COURSECODE"];
            if ($setCmc == "9_999_9999") {
                foreach ($totalData as $tname => $totalVal) {
                    $this->fields[$setCmc][$tname] = $totalVal;
                }
            } else {
                foreach ($this->nameArray as $name) {
                    $this->fields[$setCmc][$name] = VARS::post($name.$setCmc);
                    if (!preg_match("/_T$/", $name)) {
                        $totalData[$name] += $this->fields[$setCmc][$name];
                    }
                    if (!preg_match("/_TEIIN$/", $name)) {
                        if (preg_match("/_T$/", $name)) {
                            $totalData[$name] += $this->fields[$setCmc][$name];
                        }
                    }
                }
            }
        }

        $this->update();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &update() {
        knjl350wQuery::getUpdateQuery($this);
        return;
    }

    //確定
    function &getUpdateFixedModel() {
        //県側・報告テーブル
        knjl350wQuery::getUpdateReportFixed($this);
        $this->setMessage("MSG201");
    }

    //県への報告
    function &getUpdateEdboardModel() {
        //県側・一覧テーブル
        //県側・報告テーブル
        knjl350wQuery::getUpdateReport($this);
        $this->print = "on";
        $this->setMessage("MSG201");
    }

}
?>
