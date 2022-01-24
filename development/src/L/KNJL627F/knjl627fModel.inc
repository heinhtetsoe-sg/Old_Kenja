<?php

require_once('for_php7.php');

class knjl627fModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl627fModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"              =>  VARS::post("APPLICANTDIV"),              //入試制度
                             "TESTDIV"                   =>  VARS::post("TESTDIV"),                   //入試区分
                             "PRINT_DIV"                 =>  VARS::post("PRINT_DIV"),                 //(合格/不合格)通知書印刷区分
                             "PASS_DIV"                  =>  VARS::post("PASS_DIV"),                  //合格通知書(対象者指定)
                             "PRINT_DATE"                =>  VARS::post("PRINT_DATE"),                //通知日付
                             "UNPASS_DIV"                =>  VARS::post("UNPASS_DIV"),                //不合格通知書(対象者指定)
                             "PASS_EXAMNO"               =>  VARS::post("PASS_EXAMNO"),               //受験番号指定FROM
                             "PASS_EXAMNO_TO"            =>  VARS::post("PASS_EXAMNO_TO"),            //受験番号指定TO
                             "UNPASS_EXAMNO"             =>  VARS::post("UNPASS_EXAMNO"),             //受験番号指定FROM
                             "UNPASS_EXAMNO_TO"          =>  VARS::post("UNPASS_EXAMNO_TO"),          //受験番号指定TO
                             "SLIDEPASS_EXAMNO"          =>  VARS::post("SLIDEPASS_EXAMNO"),          //受験番号指定FROM
                             "SLIDEPASS_EXAMNO_TO"       =>  VARS::post("SLIDEPASS_EXAMNO_TO"),       //受験番号指定TO
                             "SCHOLARSHIPPASS_EXAMNO"    =>  VARS::post("SCHOLARSHIPPASS_EXAMNO"),    //受験番号指定FROM
                             "SCHOLARSHIPPASS_EXAMNO_TO" =>  VARS::post("SCHOLARSHIPPASS_EXAMNO_TO"), //受験番号指定TO
                             "DBNAME"                    =>  VARS::post("DBNAME"),                    //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
