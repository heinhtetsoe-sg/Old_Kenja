<?php

require_once('for_php7.php');

class knjl312mModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjl312mModel() {}

    function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTPAPERCD"  =>  VARS::post("TESTPAPERCD")   //入試区分
                             );

        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
