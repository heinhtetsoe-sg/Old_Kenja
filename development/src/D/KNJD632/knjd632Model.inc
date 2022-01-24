<?php

require_once('for_php7.php');

class knjd632Model extends Model {

    var $cmd;
    var $control;
    var $grade;
    var $electdiv;

    function knjd632Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::post("GRADE");

        if (VARS::get("PROGRAMID")) {
            $this->electdiv = "1"; //初期値
        } else {
            $this->electdiv = VARS::post("ELECTDIV");
        }

    }

    function &getMainModel()
    {
    }

    //実行モデルを返します
    function &getExecModel()
    {
        //事前処理チェック
        if (knjd632Query::ExecuteQuery($this)) {
            $this->setMessage("MSG202");
        }
    }

}
?>
