<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp964Model extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjmp964Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),     //学期コード
                             "GRADE"            =>  VARS::post("GRADE"),        //学年
                             "REQUEST_MONTH"    =>  VARS::post("REQUEST_MONTH"),//伺日
                             "OUTGO_MONTH"      =>  VARS::post("OUTGO_MONTH"),  //決定日
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
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
