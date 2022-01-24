<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp964Model extends Model
{
    public $field      = array();
    public $cmd;

    /**
     * メンバ変数の初期化
     */
    public function knjp964Model()
    {
    }

    public function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),           //学年
                             "REQUEST_YMONTH"   =>  VARS::post("REQUEST_YMONTH"),  //伺日
                             "OUTGO_YMONTH"     =>  VARS::post("OUTGO_YMONTH"),    //決定日
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
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
