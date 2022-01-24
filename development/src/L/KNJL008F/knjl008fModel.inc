<?php

require_once('for_php7.php');


class knjl008fModel extends Model {

    var $field  = array();  //画面データ格納
    var $cmd;               //コマンド
    var $year;              //年度
    var $control;           //コントロールマスタ

    function knjl008fModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->auth = AUTHORITY;

        //画面リサイズ
        $this->windowHeight = VARS::post("windowHeight");

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "S_NAME"           =>  VARS::post("S_NAME"),           //氏名
                             "S_NAME_KANA"      =>  VARS::post("S_NAME_KANA"),      //ふりがな
                             "S_RECEPTNO"       =>  VARS::post("S_RECEPTNO")        //受験番号
                            );
    }
}
?>
