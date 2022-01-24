<?php

require_once('for_php7.php');

class knjl320gModel extends Model {
    var $field = array();
    var $ObjYear;
    var $cmd;

    function knjl320gModel() {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"      =>  VARS::post("TESTDIV"),          //入試区分
                             "PASS_SCORE"   =>  VARS::post("PASS_SCORE"),       //点以上
                             "PASS_SCORE_TO" => VARS::post("PASS_SCORE_TO"),    //点以下
                             "SORT"         =>  VARS::post("SORT"),             //ソート
                             "FORM_TYPE"    =>  VARS::post("FORM_TYPE"),        //帳票区分
                             "SPECIAL_REASON_DIV" => VARS::post("SPECIAL_REASON_DIV"), //特別措置者のみ
                             "DBNAME"       =>  VARS::post("DBNAME")            //DB変更に備えて
                            );
        $this->cmd = VARS::request("cmd");
    }
}
?>
