<?php

require_once('for_php7.php');

class knjp905Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;       //コントロールマスタ

    public function knjp905Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "OUTGO_L_CD"           =>  VARS::post("OUTGO_L_CD"),           //支出科目
                             "OUTGO_L_M_CD"         =>  VARS::post("OUTGO_L_M_CD"),         //支出項目
                             "REQUEST_NO"           =>  VARS::post("REQUEST_NO"),           //伝票番号
                             "SEARCH_YMONTH"        =>  VARS::post("SEARCH_YMONTH"),        //伺い対象月
                             "REQUEST_DATE"         =>  VARS::post("REQUEST_DATE"),         //伺い日
                             "OUTGO_DATE"           =>  VARS::post("OUTGO_DATE"),           //支出日
                             "OUTGO_APPROVAL"       =>  VARS::post("OUTGO_APPROVAL"),       //決裁済
                             "NOT_OUTGO_APPROVAL"   =>  VARS::post("NOT_OUTGO_APPROVAL"),   //未決裁
                            );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = VARS::post("SCHOOL_KIND") ? VARS::post("SCHOOL_KIND") : $this->schoolKind;

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
