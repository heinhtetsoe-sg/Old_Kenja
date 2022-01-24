<?php

require_once('for_php7.php');
class knjx_sogosokenModel extends Model
{
    public $cmd;
    public $schregno;     //学籍番号
    public $name;         //氏名
    public $year;         //学年
    public $annual;       //学年(年次)

    public function knjx_sogosokenModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd     = VARS::request("cmd");

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO"); //学籍番号
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");         //生徒氏名
        }
        if (VARS::get("YEAR")) {
            $this->year = VARS::get("YEAR");         //学年
        }
        if (VARS::get("ANNUAL")) {
            $this->annual = VARS::get("ANNUAL");     //年次
        }

        $this->fieldSize['TOTALREMARK']['moji'] = '44';
        $this->fieldSize['TOTALREMARK']['gyou'] = '13';
        if ($this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"] != "") {
            $temp = explode('*', $this->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
            $this->fieldSize['TOTALREMARK']['moji'] = trim($temp[0]);
            $this->fieldSize['TOTALREMARK']['gyou'] = trim($temp[1]);
        }
    }
}
