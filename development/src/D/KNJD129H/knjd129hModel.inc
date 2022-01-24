<?php

require_once('for_php7.php');


class knjd129hModel extends Model {
    var $field = array();
    var $data  = array();
    var $testcdArray  = array();
    var $cmd;
    var $usePerfect;
    var $chikokuHyoujiFlg;
    var $useSlumpHyouka;
    var $schoolKind;
    var $subclassAll;
    var $fieldsCalc = array();
    var $testcdMotoArray = array();

    function knjd129hModel() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            $this->testcdMotoArray = array();
            common::GetControlMaster_Fnc($this->control);
            $this->dispZumi = false;
        }
        $this->cmd     = VARS::request("cmd");
        if ($this->cmd == "disp") {
            $this->dispZumi = $this->dispZumi ? false : true;
        }
        $this->field = array("SEMESTER"         => VARS::post("SEMESTER"),
                             "GRADE"            => VARS::post("GRADE"),
                             "COURSE"           => VARS::post("COURSE"),
                             "TESTIK"           => VARS::post("TESTIK"),
                             "SUBCLASS_CD"      => VARS::post("SUBCLASS_CD"),
                             );
        $this->field["COUNTER"]  = VARS::post("COUNTER"); //行数

        $this->TYUKAN1 = "010101";
        $this->TYUKAN2 = "010201";
        $this->KIMATU  = "020101";

        //プロパティファイル
        $this->getPropertiesAll();

        //学校種別(H:高校 J:中学 P:小学)
        $this->schoolKind = "H";

        //教育課程対応
        $this->subclassAll = "00-{$this->schoolKind}-00-000000";

        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;
        $this->chikokuHyoujiFlg = (strlen($this->Properties["chikokuHyoujiFlg"])) ? $this->Properties["chikokuHyoujiFlg"] : 0;

        //算出ボタン押し下げ時のみ保持（それ以外は初期化）
        $this->fieldsCalc = array();
    }

    //入力項目チェック
    function validateInput() {
        if ($this->field["COUNTER"] == 0) {
            $this->setWarning("MSG303");
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        //学籍番号を更新時に取得
        $this->data["KEY"] = array();
        for ($i = 0; $i < $this->field["COUNTER"]; $i++) {
            $this->data["KEY"][] = VARS::post("KEY"."_".$i);
        }
        //初期化
        $this->fields = array();
        $this->testInsArray = array();
        foreach ($this->data["KEY"] as $schSub) {
            if (VARS::post("CHK_BOX_".$schSub) == "1") {
                foreach ($this->testcdArray as $key => $codeArray) {
                    $testcd = $codeArray["TESTCD"];
                    if (substr($testcd, 0, 5) == substr($this->field["SEMESTER"].str_replace("-", "", $this->field["TESTIK"]), 0, 5)) {
                        $this->fields[$schSub][$testcd] = VARS::post("SCORE_".$schSub.$testcd);
                    }
                }
            }
        }

        if ($this->validateInput()) {
            knjd129hQuery::update($this);
            $this->setMessage("MSG201");
        }
    }

}
//var_dump($_REQUEST);
?>
