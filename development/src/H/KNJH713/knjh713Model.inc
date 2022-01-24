<?php

require_once('for_php7.php');

class knjh713Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;       //コントロールマスタ

    //メンバ変数の初期化
    public function knjh713Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"   => VARS::post("YEAR"),   //年度
                             "BUNRI"  => VARS::post("BUNRI"),  //文理別
                             "TIREKI" => VARS::post("TIREKI"), //地歴別
                             "RIKA"   => VARS::post("RIKA"),   //理科別
                             "DBNAME" => VARS::post("DBNAME")  //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
