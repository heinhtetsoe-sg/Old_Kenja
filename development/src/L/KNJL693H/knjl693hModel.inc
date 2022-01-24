<?php
class knjl693hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $schoolKind;

    public function knjl693hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("TESTDIV"          =>  VARS::post("TESTDIV"),          //入試日程
                             "TESTDIV1"         =>  VARS::post("TESTDIV1"),         //類別
                             "RECEPTNO_START"   =>  VARS::post("RECEPTNO_START"),   //受験番号（開始）
                             "RECEPTNO_END"     =>  VARS::post("RECEPTNO_END"),     //受験番号（終了）
                             "SEX"              =>  VARS::post("SEX"),              //男女別
                             "ORDER"            =>  VARS::post("ORDER"),            //出力順
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //帳票種類
                        );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }
}
