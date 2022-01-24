<?php

require_once('for_php7.php');

class knjp906Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;
    var $schregno = "";

    /**
     * メンバ変数の初期化
     */
    function knjp906Model() {
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->schregno);
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");

        //年組より校種を取得
        $db = Query::dbCheckOut();
        $grade = !strlen($this->field["GRADE_HR_CLASS"]) ? "" : substr($this->field["GRADE_HR_CLASS"],0,2);
        $query = knjp906Query::getSchoolKind($grade);
        $school_kind = $db->getOne($query);
        $school_kind = !strlen($school_kind) ? "H" : $school_kind;
        $this->schoolKind = $school_kind;
        Query::dbCheckIn($db);

    }

    //入力項目チェック
    function validateInput() {

        if (get_count($this->data["SCHREGNO"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->updFields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $schregno = $this->data["SCHREGNO"][$i];
            $term_name = "BENEFIT_MONEY"."-".$schregno;
            $this->updFields[$schregno] = VARS::post($term_name);

            $this->preMoney[$schregno] = VARS::post("PRE_MONEY-".$schregno);
        }

        if ($this->validateInput()) {
            knjp906Query::update($this);
            $this->setMessage("MSG201");
        }
    }
}
//var_dump($_REQUEST);
?>