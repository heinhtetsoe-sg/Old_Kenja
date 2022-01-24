<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp967Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjmp967Model()
    {
    }

    function init()
    {
        $this->field = array("OUTGO_DATE"   =>  VARS::post("OUTGO_DATE"), //支払日
                             "REQUEST_NO"   =>  VARS::post("REQUEST_NO"), //伝票番号
                             "DBNAME"       =>  VARS::post("DBNAME")      //DB変更に備えて
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
