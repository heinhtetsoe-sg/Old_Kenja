<?php

require_once('for_php7.php');
class knjl327gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl327gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),      //入試区分
                             "PRINT_DATE"   =>  VARS::post("PRINT_DATE"),   //通知日付
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                             "SPECIAL_REASON_DIV" => VARS::post("SPECIAL_REASON_DIV"),       //特別措置者
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
