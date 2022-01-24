<?php

require_once('for_php7.php');

class knjb3060Model extends Model {

    function knjb3060Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year     = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->bscSeq = VARS::request("BSCSEQ");
        $this->reflectDiv = VARS::request("REFLECTDIV");
        $this->startDate = VARS::request("START_DATE");
        $this->endDate = VARS::request("END_DATE");
        $this->weekRadio = VARS::request("WEEK_RADIO");
        $this->weekTableDays = VARS::request("WEEK_TABLE_DAYS");
        $this->weekRadioCalc = ($this->weekRadio != 7) ? ((int)$this->weekRadio + 1) : 1;

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function validateInput() {
        if (!isset($this->reflectDiv)) {
            $this->setWarning('MSG301');
            return false;
        }
        if($this->reflectDiv!=3){
            if (!isset($this->startDate)) {
                $this->setWarning('MSG301');
                return false;
            }
            if (!isset($this->endDate)) {
                $this->setWarning('MSG301');
                return false;
            }
            if($this->startDate<$this->rengeStart || $this->startDate>$this->rengeEnd){
                $this->setWarning('範囲エラー1');
                return false;
            }
            if($this->endDate<$this->rengeStart || $this->endDate>$this->rengeEnd){
                $this->setWarning('範囲エラー2');
                return false;
            }
        } else {
            if (!isset($this->weekRadio)) {
                $this->setWarning('MSG301');
                return false;
            }
            if (empty($this->weekTableDays)) {
                $this->setWarning('MSG301');
                return false;
            }
        }
        return true;
    }

    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjb3060Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
