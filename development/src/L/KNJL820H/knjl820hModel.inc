<?php
class knjl820hModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl820hModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"          =>  VARS::post("APPLICANTDIV"),          //学校種別
                             "TESTDIV"               =>  VARS::post("TESTDIV"),               //入試区分
                             "OUTPUT"                =>  VARS::post("OUTPUT"),                //帳票種類
                             "SYUKESSEKI_HANTEI_HOU" =>  VARS::post("SYUKESSEKI_HANTEI_HOU"), //志願者一覧表ラジオボタン
                             "HALLCD1"               =>  VARS::post("HALLCD1"),               //出欠席記入表 会場コンボ
                            );

    }
}
?>
