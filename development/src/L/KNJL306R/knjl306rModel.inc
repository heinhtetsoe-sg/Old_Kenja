<?php

require_once('for_php7.php');

class knjl306rModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $fixApplicantDiv;  //入試制度(ログイン校種により固定)
        
    function knjl306rModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->ObjYear = CTRL_YEAR+1;
    }

    function init() {
        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "OUTPUT"       =>  VARS::post("OUTPUT")            //出力順
                            );

        $this->cmd = VARS::request("cmd");
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
                $this->fixApplicantDiv = '2';
        }
    }
}
?>
