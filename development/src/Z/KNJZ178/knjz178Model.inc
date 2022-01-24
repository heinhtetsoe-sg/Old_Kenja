<?php

require_once('for_php7.php');

class knjz178Model extends Model {
        var $field       = array();
        var $low_stock   = array();
        var $high_stock  = array();
        var $cmd;
        var $sepa;
        var $level;

    function knjz178Model() {}

    function init() {
        $this->cmd  = VARS::request("cmd");

        $this->field = array("GRADE"          =>  VARS::post("GRADE"),          //学年
                             "SEMESTER"       =>  VARS::post("SEMESTER"),       //学期
                             "ASSESSLEVELCNT" =>  VARS::post("ASSESSLEVELCNT")  //評定段階数
                             );

        if ($this->cmd == 'update') {
            for ($i = 1; $i <= $this->field["ASSESSLEVELCNT"]; $i++) {
                $assessMark = "ASSESSMARK_" . $this->field["SEMESTER"] . "_" . $i;
                $assessLow  = "ASSESSLOW_"  . $this->field["SEMESTER"] . "_" . $i;
                $assessHigh = "ASSESSHIGH_" . $this->field["SEMESTER"] . "_" . $i;
                $this->field[$assessMark] = VARS::post($assessMark);
                $this->field[$assessLow]  = VARS::post($assessLow);
                $this->field[$assessHigh] = VARS::post($assessHigh);
            }
        }
    }

    /********************/
    /* 入力項目チェック */
    /********************/
    function validateInput() {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $this->setWarning("MSG300");
            return false;
        }
        return true;
    }

    /****************/
    /* アップデート */
    /****************/
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz178Query::getInsertQuery($this);
        }
        $this->setMessage("MSG201");
        return false;
    }

    //変数をクリアします
    function clean() {}
}
?>
