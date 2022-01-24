<?php

require_once('for_php7.php');

class knjd616fModel extends Model {
    var $field = array();
    var $control;
    var $cmd;

    function knjd616fModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),      //校種
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SDATE"            =>  VARS::post("SDATE"),            //集計開始日付
                             "DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
    }
}
?>
