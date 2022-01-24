<?php

require_once('for_php7.php');


class knjl015pModel extends model {
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $field = array();

    function knjl015pModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->whereDiv     = VARS::post("WHERE_DIV");
        $this->kakuteiDisp  = VARS::post("KAKUTEI_DISP");

        $this->hid_examno   = VARS::post("HID_EXAMNO");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno  = $tmp[0];
            $receptno2 = $tmp[1];
            $this->field["CHK_DATA"][$val] = VARS::post("CHK_DATA"."-".$val);
        }
        if (isset($this->field["CHK_DATA"])) {
            $result = knjl015pQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
