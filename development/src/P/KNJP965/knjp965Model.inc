<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp965Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp965Model()
    {
    }

    function init()
    {
        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),//校種
                             "MONTH_F"      =>  VARS::post("MONTH_F"),    //日付From
                             "MONTH_T"      =>  VARS::post("MONTH_T"),    //日付To
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
