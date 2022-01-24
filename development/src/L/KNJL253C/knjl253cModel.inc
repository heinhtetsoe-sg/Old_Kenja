<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl253cModel extends Model {
    var $cmd;
    var $ObjYear;

    /**
     * メンバ変数の初期化
     */
    function knjl253cModel()
    {
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        //フィールド
        $this->field = array("PRE_TESTDIV" => VARS::post("PRE_TESTDIV")
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
