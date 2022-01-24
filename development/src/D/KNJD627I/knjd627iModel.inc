<?php

require_once('for_php7.php');

class knjd627iModel extends Model
{
    public $programID;
    public $cmd;
    public $eventFrom;      //ポスト発行元コントロール名
    public $year;
    public $targetClass;
    public $seperator = "-";    //年組選択項目値において年次コードとクラスコードを仕切る文字
    public $grade;              //選択されたクラスの年次
    public $hrclass;            //選択されたクラスのクラスコード
    public $selectedStudents;
    public $includeRankAndIQ;

    public function init()
    {
        $this->year              = CTRL_YEAR;
        $this->cmd               = VARS::request("cmd");
        $this->eventFrom         = VARS::post("HID_EVENT_FROM");
        $this->targetClass       = VARS::post("TARGET_CLASS");
        $this->selectedStudents  = VARS::post("STUDENTS_SELECTED");
        $this->includeRankAndIQ  = VARS::post("INCLUDE_RANK_AND_IQ");

        if (isset($this->targetClass)) {
            $splited = explode($this->seperator, $this->targetClass);
            if (is_array($splited) && 1 < get_count($splited)) {
                $this->grade       = $splited[0];
                $this->hrclass     = $splited[1];
            }
        }

        //プロパティファイル読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //学校種別の指定有無を取得
    public function isIndicateSchoolKind()
    {
        return ($this->Properties["useSchool_KindField"] == "1" && $this->selectSchoolKind != "");
    }
}