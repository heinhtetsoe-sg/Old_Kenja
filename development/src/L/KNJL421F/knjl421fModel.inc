<?php

require_once('for_php7.php');

class knjl421fModel extends Model {
    var $field      = array();
    var $ObjYear;
    var $cmd;
    var $control;   //コントロールマスタ
        
    function knjl421fModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("SCHOOL_KIND"  =>  VARS::post("SCHOOL_KIND"),      //学種
                             "OUTPUT_KAKUTEI" =>  VARS::post("OUTPUT_KAKUTEI"), //確定データを出力
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
