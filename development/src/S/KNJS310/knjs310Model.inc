<?php

require_once('for_php7.php');

//定数定義
class knjs310Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    //メンバ変数の初期化
    function knjs310Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        
        $this->field = array("SELECT_DIV"       =>  VARS::post("SELECT_DIV"),       //選択区分
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "STAFF"            =>  VARS::post("STAFF"),            //先生
                             "TARGET_MONTH"     =>  VARS::post("TARGET_MONTH"),     //対象月
                             "UNIT"             =>  VARS::post("UNIT"),             //単元名
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
