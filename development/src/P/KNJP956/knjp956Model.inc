<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp956Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp956Model()
    {
    }

    function init()
    {
        $this->field = array("SEISAN_L_M_CD"    =>  VARS::post("SEISAN_L_M_CD"),    //精算科目
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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
