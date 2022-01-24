<?php
class knjl672hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $schoolKind;

    public function knjl672hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array(
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
