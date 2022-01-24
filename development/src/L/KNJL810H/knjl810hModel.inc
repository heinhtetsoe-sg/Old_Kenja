<?php
class knjl810hModel extends Model {
    var $field = array();
    var $cmd;
    var $examyear;

    function knjl810hModel(){}

    function init() {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");

        $this->field = array("APPLICANTDIV"          =>  VARS::post("APPLICANTDIV"),          //学校種別
                             "TESTDIV"               =>  VARS::post("TESTDIV"),               //入試区分
                             "CHECK"                 =>  VARS::post("CHECK"),                 //氏名・種別チェックボックス
                             "OUTPUT"                =>  VARS::post("OUTPUT"),                //帳票種類
                             "PASS_SCHOOL_CD"        =>  VARS::post("PASS_SCHOOL_CD"),        //志願者一覧表ラジオボタン
                             "KINDDIV"               =>  VARS::post("KINDDIV"),               //入試種別
                             "SYUKESSEKI_HANTEI_HOU" =>  VARS::post("SYUKESSEKI_HANTEI_HOU"), //出欠席記入表ラジオボタン
                             "SCORE"                 =>  VARS::post("SCORE"),                 //成績記入表ラジオボタン
                             "HALLCD1"               =>  VARS::post("HALLCD1"),               //出欠席記入表 会場コンボ
                             "HALLCD2"               =>  VARS::post("HALLCD2"),               //成績記入表 会場コンボ
                             "HALLCD3"               =>  VARS::post("HALLCD3")                //机上タックシール 会場コンボ
                            );

    }
}
?>
