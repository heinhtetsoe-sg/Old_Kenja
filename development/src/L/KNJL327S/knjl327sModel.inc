<?php

require_once('for_php7.php');

class knjl327sModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード

    function knjc171aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;  //職員コード
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //対象日
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                             );

        //最終学期を取得
        $rtnSem = knjl327sQuery::getSemesterLast();
        $this->test_year = ($rtnSem == CTRL_SEMESTER) ? CTRL_YEAR + 1 : CTRL_YEAR;
        $this->cmd = VARS::request("cmd");
    }

    function clean() {}
}
?>
