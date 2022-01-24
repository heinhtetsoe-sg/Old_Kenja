<?php

require_once('for_php7.php');

class knjl329qModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;

    function knjl329qModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "NOTICEDATE"       =>  VARS::post("NOTICEDATE")        //通知日付
                             );

        $this->cmd = VARS::request("cmd");
    }
}
?>
