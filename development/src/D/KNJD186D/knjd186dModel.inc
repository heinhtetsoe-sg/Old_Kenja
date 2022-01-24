<?php

require_once('for_php7.php');

class knjd186dModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;
    var $selectdata;

    function knjd186dModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("SDATE"            =>  VARS::post("SDATE"),            //出欠集計開始日付
                             "EDATE"            =>  VARS::post("EDATE"),            //出欠集計終了日付
                             "DESC_DATE"        =>  VARS::post("DESC_DATE"),        //記載日付
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );
        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
