<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjx240Model extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjx240Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
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
