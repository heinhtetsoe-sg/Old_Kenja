<?php

require_once('for_php7.php');


class knje065iModel extends Model {
    var $cmd;
    var $school_kind;
    var $grade;

    function knje065iModel() {
        common::GetControlMaster_Fnc($this->control);
    }
    
    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");//校種
        $this->field["GRADE"]       = VARS::post("GRADE");      //学年
        $this->rankDivArray = array("1" => "GRADE", "2" => "CLASS", "3" => "COURSE", "4" => "MAJOR");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function validateInput() {
        return true;
    }

    function &getExecModel() {
        knje065iQuery::updSchregStudyrecRankClassDat($this);
        knje065iQuery::updSchregStudyrecAvgClassDat($this);
        $this->setMessage("MSG202");
    }
}
?>
