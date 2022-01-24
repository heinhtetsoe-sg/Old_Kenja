<?php

require_once('for_php7.php');

class knjd219jModel extends Model {
    var $field       = array();
    var $cmd;
    var $sepa;
    var $level;
    var $selectdata;

    function knjd219jModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->field = array("SEMESTER"         =>  VARS::request("SEMESTER"),     //学期(対象)
                             "GRADE"            =>  VARS::request("GRADE"),        //学年
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),     //校種
                             "GROUP_CD"         =>  VARS::post("GROUP_CD"),        //コースグループ
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),      //科目
                             "PROFICIENCY_SUBCLASS_CD"  =>  VARS::post("PROFICIENCY_SUBCLASS_CD"),   //実力科目
                             "TEST_COUNT"       =>  VARS::post("TEST_COUNT"),       //テスト種別数
                             "COMBINED"         =>  VARS::post("COMBINED")        //算出先になる学校成績
                             );
        //処理対象テスト種別(左のリスト）
        $this->selectdata = VARS::post("selectdata");

        if ($this->cmd == 'update') {
            for ($i = 1; $i <= $this->field["TEST_COUNT"]; $i++) {
                $this->field["TESTCD".$i] = VARS::post("TESTCD".$i);
                $this->field["PERCENT".$i] = VARS::post("PERCENT".$i);
                $this->field["PERFECT".$i] = VARS::post("PERFECT".$i);
            }
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
            knjd219jQuery::insAssessLevel($this);
//            knjd219jQuery::insRecordScore($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    /****************/
    /*   デリート   */
    /****************/
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd219jQuery::delAssessLevel($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
