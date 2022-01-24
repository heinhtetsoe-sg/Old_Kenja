<?php

require_once('for_php7.php');


class knjp714Model extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $hr_class;
    var $data_cnt;
    var $control;
    var $z010name1;

    function knjp714Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("SEND_SCHREGNO")) {
            $this->sendSchregNo = VARS::get("SEND_SCHREGNO");
            $this->sendSlipNo   = VARS::get("SEND_SLIP_NO");
        } else {
            $this->year = VARS::post("YEAR");
            $this->semester = VARS::post("SEMESTER");
            $this->dataDiv = VARS::post("DATADIV");
            $this->seikyuuDisp = VARS::post("SEIKYUU_DISP");
            $this->hr_class = VARS::post("HR_CLASS");
            $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

            if (VARS::request("PROGRAMID") != "") {
                $this->year = CTRL_YEAR;
                $this->semester = CTRL_SEMESTER;
            }
            unset($this->sendSchregNo);
            unset($this->sendSlipNo);
        }

        $this->cmd   = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

}
?>
