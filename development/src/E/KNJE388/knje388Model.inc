<?php

require_once('for_php7.php');

class knje388Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knje388Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("GRADE"       =>  VARS::post("GRADE"),       //学年
                             "EDATE"       =>  VARS::post("EDATE"),       //対象日
                             "DATE"        =>  VARS::post("DATE"),        //学籍処理日
                             "DBNAME"      =>  VARS::post("DBNAME"),      //DB変更に備えて
                             "SORT_ORDER"  =>  VARS::post("SORT_ORDER")   //出力順ラジオボタン
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean() {}
}
?>
