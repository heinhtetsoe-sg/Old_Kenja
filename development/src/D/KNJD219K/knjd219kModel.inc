<?php

require_once('for_php7.php');

class knjd219kModel extends Model {
    var $field       = array();
    var $cmd;
    var $sepa;
    var $level;
    var $selectdata;

    function knjd219kModel() {}

    function init() {
        $this->cmd  = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;//年度
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "COURSECODE"       =>  VARS::post("COURSECODE"),       //コース
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "TESTCD"           =>  VARS::post("TESTCD"),           //算出先になる学期成績
                             "TESTCD9"          =>  "9-99-00-08"                    //算出先になる学年末成績
                             );
        $this->field["SEMESTER_SCH"] = ($this->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $this->field["SEMESTER"];//学期(在籍データ用)

        $this->field["GRADE_ALL"]       = "00";
        $this->field["COURSECODE_ALL"]  = "0000";
        $this->field["SUBCLASSCD_ALL"]  = "00-".$this->field["SCHOOL_KIND"]."-00-000000";
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
            knjd219kQuery::executeRireki($this);
            knjd219kQuery::insRecordScore($this);
            $this->setMessage("MSG201");
        }
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
