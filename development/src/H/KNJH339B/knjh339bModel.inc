<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh339bModel extends Model {
    var $cmd;
    var $control;
    var $data_div;

    /**
     * メンバ変数の初期化
     */
    function knjh339bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->data_div = VARS::post("DATA_DIV");
        $this->mockcd = VARS::post("MOCKCD");
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        knjh339bQuery::UpdateMockRankDat($this);
        knjh339bQuery::UpdateMockAverageDat($this);
        $this->setMessage("MSG202");
    }
}
?>
