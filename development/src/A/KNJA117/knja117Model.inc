<?php

require_once('for_php7.php');

class knja117Model extends Model {
    var $cmd;
    var $field = array();
    var $selectdata;

    function knja150Model() {}

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->selectdata);
            $this->field = array();
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS")    //年組
                             );

        $this->cmd = VARS::request("cmd");

        //対象生徒
        $this->selectdata   = VARS::post("selectdata");
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if (!strlen($this->field["GRADE"])) {
            $this->setWarning("MSG310", "( 学年 )");
            return false;
        }
        if (!strlen($this->field["TESTDIV"])) {
            $this->setWarning("MSG310", "( 入試区分 )");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knja117Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            unset($this->selectdata);
        }
    }
}
?>
