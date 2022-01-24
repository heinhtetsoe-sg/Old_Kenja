<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl221cModel extends Model {
    var $cmd;
    var $ObjYear;
    var $field = array();

    /**
     * メンバ変数の初期化
     */
    function knjl221cModel()
    {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR+1;
        $this->cmd = VARS::request("cmd");
        $this->field = array("PRE_TESTDIV"    => VARS::post("PRE_TESTDIV"),
                             "TESTSUBCLASSCD" => VARS::post("TESTSUBCLASSCD")
                             );
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
