<?php

require_once('for_php7.php');


class knja228Model extends model
{
    var $cmd;
    var $year;
    var $preischoolcd;
    var $preischoolClassCd;
    var $semester;
    var $grade;
    var $hrClass;
    var $selected;
    var $disppattern;

    function knja228Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->year = VARS::post("YEAR");
        $this->preischoolcd = VARS::post("PRISCHOOLCD");
        $this->preischoolClassCd = VARS::post("PRISCHOOL_CLASS_CD");
        $this->semester = "1";
        $this->selected = explode(",", VARS::post("CATEGORY_SELECTED_DATA"));
        $this->disppattern = VARS::post("DISP_PATTERN");

        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->nenkumi);
            unset($this->preischoolcd);
            unset($this->preischoolClassCd);
            unset($this->grade);
            unset($this->hrClass);
            unset($this->selected);
            unset($this->disppattern);
        }

    }

    //受験番号をセット
    function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        knja228Query::deleteQuery($this);
        if ($this->selected[0]) {
            foreach ($this->selected as $key => $value){
                list($grade, $hrClass, $attendNo, $schregNo) = explode("-", $value);
                knja228Query::insertQuery($this, $schregNo);
            }
        }
        $this->setMessage("MSG201");
    }
}
?>
