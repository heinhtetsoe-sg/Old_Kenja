<?php
class knjl401iModel extends Model {
    var $cmd;
    var $examyear;
    var $applicantdiv;
    var $testdiv;
    var $schoolKind;

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->schoolKind = VARS::get("URL_SCHOOLKIND");
        }
    
        $this->examyear = CTRL_YEAR + 1;
        $this->cmd              = VARS::request("cmd");
        $this->testdiv          = VARS::post("TESTDIV");
        $this->outputdiv        = VARS::post("OUTPUT_DIV");
        
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    function &getMainModel() {}
}
?>
