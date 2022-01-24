<?php
class knjl870hModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl870hModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //学校種別
                             "TESTDIV"      =>  VARS::post("TESTDIV"),      //入試区分
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //帳票種類
                             "DATADIV"      =>  VARS::post("DATADIV")       //入学者タックシールラジオボタン
                            );

    }
}
?>
