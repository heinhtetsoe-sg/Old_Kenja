<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp961Model extends Model
{
    public $field = array();
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjp961Model()
    {
    }

    public function init()
    {
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "PRINT_DIV"        =>  VARS::post("PRINT_DIV"),        //出力指定
                             "REQUEST_NO"       =>  VARS::post("REQUEST_NO"),       //伝票番号
                             "INCOME_DIV"       =>  VARS::post("INCOME_DIV"),       //収入伺From
                             "REQUEST_MONTH_F"  =>  VARS::post("REQUEST_MONTH_F"),  //収入伺To
                             "REQUEST_MONTH_T"  =>  VARS::post("REQUEST_MONTH_T"),  //決定From
                             "INCOME_MONTH_T"   =>  VARS::post("INCOME_MONTH_T"),   //決定To
                             "INCOME_MONTH_F"   =>  VARS::post("INCOME_MONTH_F"),   //期間指定
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
