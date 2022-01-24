<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp400Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $month;     //月
        
    /**
     * メンバ変数の初期化
     */
    function knjp400Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {
        $this->cmd      = VARS::request("cmd");
        if (VARS::post("MONTH")) $this->month  = VARS::post("MONTH");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
