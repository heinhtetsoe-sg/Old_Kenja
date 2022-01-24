<?php

require_once('for_php7.php');
class knjd_behavior_sdModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $field = array();
    var $record = array();
    var $control;
    var $call_prg;

    function knjd_behavior_sdModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("CALL_PRG") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            $this->call_prg = VARS::get("CALL_PRG");
        }

        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"));

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->grade        = VARS::get("GRADE");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            $this->getPro = array();
        }

        if (VARS::get("send_knjdBehaviorsd_UseText") != "") {
            $this->getPro["knjdBehaviorsd_UseText"] = VARS::get("send_knjdBehaviorsd_UseText");
        }
        if (VARS::get("send_knjdBehaviorsd_DispViewName") != "") {
            $this->getPro["knjdBehaviorsd_DispViewName"] = VARS::get("send_knjdBehaviorsd_DispViewName");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->getPro["knjdBehaviorsd_UseText"]      = isset($this->getPro["knjdBehaviorsd_UseText"])      ? $this->getPro["knjdBehaviorsd_UseText"]      : $this->Properties["knjdBehaviorsd_UseText"];
        $this->getPro["knjdBehaviorsd_DispViewName"] = isset($this->getPro["knjdBehaviorsd_DispViewName"]) ? $this->getPro["knjdBehaviorsd_DispViewName"] : $this->Properties["knjdBehaviorsd_DispViewName"];
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

        //行動の記録
        if (is_array($this->itemArray)) {
            foreach ($this->itemArray as $key => $val) {
                $this->record["RECORD"][$key] = VARS::post("RECORD".$key);
            }
            $this->update();
        }
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd_behavior_sdQuery::getInsertQuery($this);

        return ;
    }
}
?>
