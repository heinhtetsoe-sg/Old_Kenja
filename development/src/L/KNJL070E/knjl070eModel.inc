<?php

require_once('for_php7.php');

class knjl070eModel extends model {
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $s_examno;
    var $e_examno;
    var $ObjYear;

    function knjl070eModel() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->applicantdiv);
            unset($this->testdiv);
        }

        $this->ObjYear      = CTRL_YEAR+1;
        $this->applicantdiv = "1";  //固定値
        $this->exam_type    = "1";  //固定値

        $this->cmd = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv          = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");

        $this->interview = array();
        if (isset($this->examnoArray)) {
            foreach ($this->examnoArray as $key => $examno) {
                $this->interview[$examno]["JUDGEDIV"]   = VARS::post("JUDGEDIV_".$examno);
                $this->interview[$examno]["REMARK4"]    = VARS::post("REMARK4_".$examno);
                $this->interview[$examno]["REMARK5"]    = VARS::post("REMARK5_".$examno);
                $this->interview[$examno]["HOPE"]       = VARS::post("HOPE_".$examno);
                if ($this->nameCdL004Row["NAMESPARE1"] == '1') {
                    $this->interview[$examno]["JUDGEMENT2"] = VARS::post("JUDGEDIV_".$examno);
                } else {
                    $this->interview[$examno]["JUDGEMENT2"] = strlen(VARS::post("JUDGEMENT2_".$examno)) ? VARS::post("JUDGEMENT2_".$examno) : VARS::post("HID_JUDGEMENT2_".$examno);
                }
            }
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        knjl070eQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }

}
?>
