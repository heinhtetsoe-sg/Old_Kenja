<?php

require_once('for_php7.php');

class knjp916Model extends Model {

    var $cmd;
    var $field = array();
    var $data  = array();

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR;

        $this->schoolKind       = VARS::post("SCHOOL_KIND");
        $this->toLMcd           = VARS::post("TO_LM_CD");
        $this->incomeLMcdSaki   = VARS::post("INCOME_LM_CD_SAKI");
        $this->setMoney         = VARS::post("SET_MONEY");
        $this->taigaku          = VARS::post("TAIGAKU");

        //選択生徒
        $this->selectHasuu = VARS::post("selectHasuu");

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
            $newHassu = knjp916Query::getUpdateQuery($this);
            //端数の値が変わるので再セット
            list($toTmpCd, $toHasuu) = explode(':', $this->toLMcd);
            $this->toLMcd = $toTmpCd.':'.$newHassu;
            $this->setMessage("MSG201");
        }

        return;
    }
}
?>
