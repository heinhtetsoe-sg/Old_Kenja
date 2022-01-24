<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh181Model extends Model {
    var $field      = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjh181Model()
    {
    }

    function init()
    {
        $this->field = array("FLG"      =>  VARS::post("FLG"),
                             "UP_DOWN"  =>  VARS::post("UP_DOWN"),
                             "SORTFLG"  =>  VARS::post("SORTFLG"),
                             "ROSEN"    =>  VARS::post("ROSEN")
                            );

        $this->cmd = VARS::request("cmd");
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
