<?php

require_once('for_php7.php');

class knjd219bModel extends Model {
    var $field       = array();
    var $cmd;
    var $sepa;
    var $level;
    var $selectdata;

    function knjd219bModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("SEMESTER"         =>  VARS::request("SEMESTER"),  //学期(対象)
                             "GRADE"            =>  VARS::request("GRADE"),     //学年
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),     //コースグループ
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),   //科目
                             "PROFICIENCY_SUBCLASS_CD"  =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),   //実力科目
                             "TEST_COUNT"       =>  VARS::post("TEST_COUNT")    //テスト種別数
                             );
        //処理対象テスト種別(左のリスト）
        $this->selectdata = VARS::post("selectdata");

        if ($this->cmd == 'update') {
            for ($i = 1; $i <= $this->field["TEST_COUNT"]; $i++) {
                $this->field["TESTCD".$i] = VARS::post("TESTCD".$i);
                $this->field["PERCENT".$i] = VARS::post("PERCENT".$i);
                $this->field["PERFECT".$i] = VARS::post("PERFECT".$i);
            }
            $this->field["TOTAL"]  = VARS::post("TOTAL");  //合計点
            $this->field["ADJUST"] = VARS::post("ADJUST"); //調整点
        }
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    function validateInput() {
        return true;
    }

    /****************/
    /* アップデート */
    /****************/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd219bQuery::insAssessLevel($this);
            knjd219bQuery::insRecordScore($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
