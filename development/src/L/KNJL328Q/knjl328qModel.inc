<?php

require_once('for_php7.php');

class knjl328qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;

    function knjl328qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV")      //入試制度
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
