<?php

require_once('for_php7.php');
class knjd680jModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $name;
    var $field = array();
    var $Field = array();
    var $record = array();
    var $control;

    function knjd680jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            $this->record = array();
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->field["SEMESTER"] = "9";

        $this->Field = array("TOTALSTUDYTIME"       =>  VARS::post("TOTALSTUDYTIME"),
                             "UPDATED"              =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        if ($this->cmd == "update") $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        //行動の記録
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->record["RECORD"][$key] = VARS::post("RECORD".$key);
            }
            knjd680jQuery::getInsertBehaviorQuery($this);

            $this->record = array();
        }

        return ;
    }

}
?>
