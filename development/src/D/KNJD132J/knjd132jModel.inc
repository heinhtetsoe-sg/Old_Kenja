<?php

require_once('for_php7.php');

class knjd132jModel extends Model {
    var $cmd;
    var $field = array();
    var $arrSchregNo = array();

    function knjd132jModel() {}

    function init() {

        $this->field = array("IBPRG_COURSE"         =>  VARS::post("IBPRG_COURSE"),     //IBコース
                             "SEMESTER"             =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"                =>  VARS::post("GRADE"),            //学年
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        foreach ($this->arrSchregNo as $key => $schregNo) {
            $this->field["SCORE_".$schregNo] = VARS::post("SCORE_".$schregNo);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (strlen($this->field["SEMESTER"]) == 0) {
            $this->setWarning("MSG304","（学期）");
            return false;
        }
        if (strlen($this->field["GRADE"]) == 0) {
            $this->setWarning("MSG304","（学年）");
            return false;
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        knjd132jQuery::getInsertQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
