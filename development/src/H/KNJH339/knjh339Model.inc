<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh339Model extends Model
{
    public $cmd;
    public $control;
    public $data_div;

    /**
     * メンバ変数の初期化
     */
    public function knjh339Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->data_div = VARS::post("DATA_DIV");
        $this->mockcd = VARS::post("MOCKCD");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        knjh339Query::updateMockDat($this);
        knjh339Query::updateMockRankDat($this);
        knjh339Query::updateMockAverageDat($this);
        $this->setMessage("MSG202");
    }
}
