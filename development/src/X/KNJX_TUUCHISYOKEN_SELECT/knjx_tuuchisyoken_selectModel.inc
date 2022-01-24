<?php

require_once('for_php7.php');

class knjx_tuuchisyoken_selectModel extends Model {
    var $cmd;
    var $exp_year = "";
    var $exp_semester = "";
    var $schregno = "";
    var $schoolKind = "";
    var $name = "";
    var $target = "";
    var $send_prgid = "";

    function knjx_tuuchisyoken_selectModel() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->schoolKind);
            unset($this->name);
            unset($this->target);
            unset($this->send_prgid);
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno     = VARS::get("SCHREGNO");
        }
        $this->schoolKind = '';
        if (VARS::get("SCHOOL_KIND") != "") {
            $this->schoolKind   = VARS::get("SCHOOL_KIND");
        }
        if (VARS::get("NAME") != "") {
            $this->name         = VARS::get("NAME");
        }
        if (VARS::get("TARGET") != "") {
            $this->target       = VARS::get("TARGET");
        }
        if (VARS::get("SEND_PRGID") != "") {
            $this->send_prgid   = VARS::get("SEND_PRGID");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
