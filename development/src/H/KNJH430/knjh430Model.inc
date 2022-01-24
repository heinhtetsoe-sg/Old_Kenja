<?php

require_once('for_php7.php');

class knjh430Model extends Model {
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
        if (VARS::get("SEND_selectSchoolKind")) {
            $this->selectSchoolKind = VARS::get("SEND_selectSchoolKind");
        }

        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),  //校種
                             "COURSECD"     =>  VARS::post("COURSECD")        //課程
                             );

        $this->cmd = VARS::request("cmd");

        //対象項目
        $this->selectdata   = VARS::post("selectdata");

    }

    //入力項目チェック
    function validateInput() {
        if ($this->selectdata) {
            $selectdata = explode(',', $this->selectdata);
            if (get_count($selectdata) > 12) {
                $this->setWarning("MSG915", "出力対象は12項目までです。");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjh430Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            unset($this->selectdata);
        }
    }
}
?>