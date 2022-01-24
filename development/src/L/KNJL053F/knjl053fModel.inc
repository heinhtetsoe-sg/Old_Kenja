<?php

require_once('for_php7.php');


class knjl053fModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $chkUnShow;
    var $field = array();
    var $special_reason_div;
    var $s_receptno;
    var $e_receptno;

    function knjl053fModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->chkUnShow    = VARS::post("CHK_UN_SHOW");

        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");
        $this->s_receptno = VARS::post("S_RECEPTNO");
        $this->e_receptno = VARS::post("E_RECEPTNO");

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
            $receptno = $tmp[0];
            $examno = $tmp[1];
            $this->field["SH_FLG"][$receptno] = VARS::post("SH_FLG"."-".$receptno);
            for ($i = 1; $i <= 6; $i++) {
                $this->field["SH_SCHOOLNAME{$i}"][$receptno] = VARS::post("SH_SCHOOLNAME{$i}"."-".$receptno);
                $this->field["SH_JUDGEMENT{$i}"][$receptno] = VARS::post("SH_JUDGEMENT{$i}"."-".$receptno);
            }
        }
        if (isset($this->field["SH_FLG"])) {
            $result = knjl053fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
