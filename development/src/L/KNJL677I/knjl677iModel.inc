<?php
require_once('for_php7.php');

//定数定義
class knjl677iModel extends Model
{
    public $cmd;
    public $year;
    public $applicantdiv;

    //メンバ変数の初期化
    public function knjl677iModel()
    {
    }

    public function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = "2";

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //変数をクリアします
    public function clean()
    {
    }
}
