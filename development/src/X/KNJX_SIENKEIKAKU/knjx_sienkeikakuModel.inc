<?php

require_once('for_php7.php');

class knjx_sienkeikakuModel extends Model
{
    public $cmd;
    public $year;
    public $schregno;
    public $name;

    public function knjx_sieknkeikakuModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->schregno);
        }
        if (VARS::get("YEAR") != "") {
            $this->year = VARS::get("YEAR");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //入学時の障害の状態
        if ($this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_H"]);
            $this->disability_moji = (int)trim($moji);
            $this->disability_gyou = (int)trim($gyou);
        } else {
            $this->disability_moji = 11;
            $this->disability_gyou = 6;
        }
    }
}
