<?php

require_once('for_php7.php');

class knjb1309Model extends Model {
    var $field      = array();
    var $selectdata = array();
    var $cmd;
    var $control;   //コントロールマスタ

    function knjb1309Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->year = CTRL_YEAR;
        $this->field = array("DISP"         =>  VARS::post("DISP"),         //対象一覧区分
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "SUBCLASS"     =>  VARS::post("SUBCLASS"),     //科目
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //出力ラジオ 1:予定講座 2:講座
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                     );
        $this->cmd = VARS::request("cmd");
        $this->selectdata = explode(",", VARS::post("selectdata"));
    }

    function clean() {}
}
?>