<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm433wModel extends Model {
    var $field      = array();
    var $cmd;
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjm433wModel() {
    }

    function init() {
        $this->field = array("YEAR"    =>  VARS::post("YEAR"));

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput() {
        return true;
    }

    function getUpdateModel() {
        if ($this->validateInput()) {
            knjm433wQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
