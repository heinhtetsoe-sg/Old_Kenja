<?php

require_once('for_php7.php');

class knjx_reason_collection_selectModel extends Model
{
    public $cmd;
    public $exp_year = "";
    public $exp_semester = "";
    public $schregno = "";
    public $name = "";
    public $target = "";
    public $send_prgid = "";

    public function knjx_reason_collection_selectModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
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
