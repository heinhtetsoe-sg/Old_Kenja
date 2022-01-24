<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm560Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjm560Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("CHAIRCD"  => VARS::post("CHAIRCD"),   //講座
                             "SORT"     => VARS::post("SORT")       //並び順
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
