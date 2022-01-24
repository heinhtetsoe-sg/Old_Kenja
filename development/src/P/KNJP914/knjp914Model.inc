<?php

require_once('for_php7.php');

class knjp914Model extends Model {

    var $cmd;
    var $field = array();
    var $data  = array();

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->incomeLMcdMoto   = VARS::post("INCOME_LM_CD_MOTO");
        $this->incomeLMcdSaki   = VARS::post("INCOME_LM_CD_SAKI");
        $this->setMoney         = VARS::post("SET_MONEY");
        $this->taigaku          = VARS::post("TAIGAKU");

        //選択生徒
        $this->selectStudent = VARS::post("selectStudent");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput() {

        return true;
    }

    //実行
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjp914Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
