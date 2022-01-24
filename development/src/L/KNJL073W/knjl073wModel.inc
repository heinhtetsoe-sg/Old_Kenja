<?php

require_once('for_php7.php');

class knjl073wModel extends Model
{
    public $cmd;

    //メンバ変数の初期化
    public function knjl073wModel()
    {
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->output       = VARS::post("OUTPUT");          //帳票ラジオボタン
        $this->kisai_date   = VARS::post("KISAI_DATE");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
}
