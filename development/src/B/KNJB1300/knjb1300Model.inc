<?php

require_once('for_php7.php');

class knjb1300Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ

    function knjb1300Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //対象日付
                             "YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "OUTPUT"       =>  VARS::post("OUTPUT"),       //帳票出力
                             "RIREKI_CODE"  =>  VARS::post("RIREKI_CODE"),  //履修登録日付
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
