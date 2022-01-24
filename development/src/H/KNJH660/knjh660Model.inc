<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh660Model extends Model
{
    public $cmd;
    public $control;
    public $data_div;

    /**
     * メンバ変数の初期化
     */
    public function knjh660Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        knjh660Query::UpdateKansanRankDat($this);
        $this->setMessage("MSG202");
    }
}
