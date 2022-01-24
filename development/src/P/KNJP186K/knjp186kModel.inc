<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjp186kModel extends Model {
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp186kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjp186kQuery::update($this);
        $this->setMessage("MSG202");
    }
}
?>
