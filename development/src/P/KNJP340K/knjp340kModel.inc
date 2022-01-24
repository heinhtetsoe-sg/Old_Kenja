<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp340kModel extends Model {
    var $cmd;
    var $control;
    var $field = array();

    /**
     * メンバ変数の初期化
     */
    function knjp340kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field = array("PRINT_DIV"    => VARS::post("PRINT_DIV"));
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjp340kQuery::updateAdjust($this);
        knjp340kQuery::updateBurden($this);
        $this->setMessage("MSG202");
    }
}
?>
