<?php

require_once('for_php7.php');

class knjl316Model extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
        
    function knjl316Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init() {
        $this->field = array("APDIV"  =>  VARS::post("APDIV"),  //入試制度
                            );

        $this->cmd = VARS::request("cmd");
    }
}
?>
