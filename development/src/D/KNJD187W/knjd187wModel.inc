<?php

require_once('for_php7.php');

class knjd187wModel extends Model
{
    public $programID;
    public $cmd;
    public $year;
    public $targetClass;
    public $grade;           //選択されたクラスの年次
    public $limitedScore;    //科目点数
    public $avarageScore;    //平均点
    public $selectedClasses;

    public function init()
    {
        $this->year             = CTRL_YEAR;
        $this->cmd              = VARS::request("cmd");
        $this->grade            = VARS::post("GRADE");
        $this->selectedClasses  = VARS::post("CLASS_SELECTED");
        $this->lowerScore       = VARS::post("LOWER_SCORE");
        $this->avarageScore     = VARS::post("AVARAGE_SCORE");

        if ($this->lowerScore == "") {
            $this->lowerScore = "55";
        }
        if ($this->avarageScore == "") {
            $this->avarageScore = "65";
        }

        //プロパティファイル読込み
        $this->selectSchoolKind = VARS::get("URL_SCHOOLKIND");
        $this->getPropertiesAll(); //プロパティファイル読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //学校種別の指定有無を取得
    public function isIndicateSchoolKind()
    {
        return ($this->selectSchoolKind != "");
    }
}
