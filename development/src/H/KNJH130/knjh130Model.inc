<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh130Model extends Model {
    var $field      = array();
    var $cmd;
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh130Model()
    {
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                            "GAKKI"         =>  VARS::post("GAKKI"),
                            "OUTPUTADDR"    =>  VARS::post("OUTPUTADDR"),
                            "DBNAME"        =>  VARS::post("DBNAME")
                            );


        $this->cmd       = VARS::request("cmd");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
