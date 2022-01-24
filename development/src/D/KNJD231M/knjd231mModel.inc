<?php

require_once('for_php7.php');

class knjd231mModel extends Model {
    var $field      = array();
    var $cmd;
    var $qualified_cd;        //primary

    function knjd231mModel() {}
    function init() {
        $this->field = array("SPECIAL_REASON_DIV" =>  VARS::post("SPECIAL_REASON_DIV"),
                             "HR_CLASS"           =>  VARS::post("HR_CLASS")
                             );

        $this->cmd       = VARS::request("cmd");

        if ($this->cmd == 'update') {
            $this->field = $_POST;
        }

        if (VARS::get("mode") == "1") {
            unset($this->qualified_cd);
        }
        $temp = VARS::get("QUALIFIED_CD");
        if (isset($temp)) {
            $this->qualified_cd  = VARS::get("QUALIFIED_CD");
        }
    }

   //入力項目チェック
    function validateInput() {
        if (strlen($this->field["REMARK"]) > 120) {
            $this->setWarning('MSG901', "(備考) 120バイトまで");
            return false;
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd231mQuery::getUpdateQuery($this);
            $this->setMessage('MSG201');
        }
    }

    function clean() {}
}
?>
