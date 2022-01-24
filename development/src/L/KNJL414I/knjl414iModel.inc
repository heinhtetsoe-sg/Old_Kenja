<?php
class knjl414iModel extends Model {
    var $field = array();
    var $cmd;
    var $ObjYear;
    var $schoolKind;

    function knjl414iModel()
    {
    }

    function init() {
        $this->ObjYear = CTRL_YEAR + 1;

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "PREAMBLE"         =>  VARS::post("PREAMBLE"),         //前文
                             "LIMIT_DATE"         =>  VARS::post("LIMIT_DATE")      //送付期日
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
