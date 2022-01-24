<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp963Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjmp963Model()
    {
    }

    function init()
    {
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "PRINT_DIV"        =>  VARS::post("PRINT_DIV"),        //出力指定
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "OUTGO_DIV"        =>  VARS::post("OUTGO_DIV"),        //支出伺From
                             "REQUEST_MONTH_F"  =>  VARS::post("REQUEST_MONTH_F"),  //支出伺To
                             "REQUEST_MONTH_T"  =>  VARS::post("REQUEST_MONTH_T"),  //決定From
                             "OUTGO_MONTH_T"    =>  VARS::post("OUTGO_MONTH_T"),    //決定To
                             "OUTGO_MONTH_F"    =>  VARS::post("OUTGO_MONTH_F"),    //期間指定
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
