<?php

require_once('for_php7.php');

class knjc152Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;

    function knjc152Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HRCLASS"    =>  VARS::post("GRADE_HRCLASS"),    //学年
                             "SDATE"            =>  VARS::post("SDATE"),            //対象日(開始)
                             "EDATE"            =>  VARS::post("EDATE"),            //対象日(終了)
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "PRINT_DATE"       =>  VARS::post("PRINT_DATE"),       //印刷日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
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