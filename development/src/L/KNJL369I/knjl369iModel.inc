<?php
class knjl369iModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $schoolKind;

    function knjl369iModel()
    {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "SEX"              =>  VARS::post("SEX"),              //抽出区分
                             "ORDER"            =>  VARS::post("ORDER")             //並び順
                        );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function clean()
    {
    }
}
?>
