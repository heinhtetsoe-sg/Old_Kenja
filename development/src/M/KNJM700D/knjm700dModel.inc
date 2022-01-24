<?php

require_once('for_php7.php');

class knjm700dModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $expYear = "";
    var $expSemester = "";
    var $field = array();
    var $control_data = array();    //コントロールマスタデータを保持

    function knjm700dModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "" || VARS::get("init")) {
            unset($this->expYear);
            unset($this->expSemester);
            unset($this->schregno);
            unset($this->name);
        }

        $this->checked = array();
        if (VARS::request("CHECKED")) {
            $this->checked  = VARS::request("CHECKED");     //チェック
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->expYear = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->expSemester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        $this->field = array(
            "SEMESTER" => VARS::post("SEMESTER"),
        );

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        knjm700dQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
