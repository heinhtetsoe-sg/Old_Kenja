<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp951Model extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjmp951Model()
    {
    }

    function init()
    {
        $this->field = array("INCOME_L_M_CD"    =>  VARS::post("INCOME_L_M_CD"),    //収入科目
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
