<?php

require_once('for_php7.php');

class knjd_behavior_lmModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $field = array();
    var $record = array();
    var $control;
    var $call_prg;

    function knjd_behavior_lmModel()
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
            $this->call_prg = VARS::get("CALL_PRG");
        }

        $this->field = array("SEMESTER" =>  VARS::post("SEMESTER"));

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
            $this->getPro = array();
        }
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->schoolKind = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }

        if (VARS::get("send_knjdBehaviorsd_UseText_P") != "") {
            $this->getPro["knjdBehaviorsd_UseText_P"] = VARS::get("send_knjdBehaviorsd_UseText_P");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->getPro["knjdBehaviorsd_UseText_P"] = isset($this->getPro["knjdBehaviorsd_UseText_P"]) ? $this->getPro["knjdBehaviorsd_UseText_P"] : $this->Properties["knjdBehaviorsd_UseText_P"];
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
        if (is_array($this->itemArrayL)) {
            foreach ($this->itemArrayL as $Lkey => $Lval) {
                foreach ($this->itemArrayM[$Lkey] as $Mkey => $Mval) {
                    $lmKey = $Lkey."_".$Mkey;
                    $this->record["RECORD"][$lmKey] = VARS::post("RECORD_{$lmKey}");
                }
            }
            $this->update();
        }
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd_behavior_lmQuery::getInsertQuery($this);

        return true;
    }
}
?>
