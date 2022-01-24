<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp968Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjmp968Model()
    {
    }

    function init()
    {
        $this->field = array("PRINTDIV"     => VARS::post("PRINTDIV"),   //出力区分
                             "DBNAME"       => VARS::post("DBNAME")      //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
