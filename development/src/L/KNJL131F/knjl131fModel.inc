<?php

require_once('for_php7.php');


class knjl131fModel extends model
{
    var $cmd;
    var $year;
    var $applicantdiv;
    var $finschoolcd;
    var $printDate;
    var $printZumi;
    var $hid_examno;
    var $field = array();

    function knjl131fModel() {
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->finschoolcd  = strlen(VARS::post("FINSCHOOLCD")) ? VARS::post("FINSCHOOLCD") : VARS::post("HID_FINSCHOOLCD");
        $this->printDate    = strlen(VARS::post("PRINT_DATE")) ? VARS::post("PRINT_DATE") : CTRL_DATE;
        $this->printZumi    = strlen(VARS::post("PRINT_ZUMI")) ? VARS::post("PRINT_ZUMI") : VARS::post("HID_PRINT_ZUMI");;
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
            $this->field["YOUROKU_DATA"][$receptno] = VARS::post("YOUROKU_DATA"."-".$receptno);
            $this->field["MEDEXAM_DATA"][$receptno] = VARS::post("MEDEXAM_DATA"."-".$receptno);
        }
        if (isset($this->field["YOUROKU_DATA"]) || isset($this->field["MEDEXAM_DATA"])) {
            $result = knjl131fQuery::getUpdateQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }

    // 編集完了用のモデルを返します
    function &getUpdatePrintModel() {
        $this->field = array();
        $keyList = array();
        $keyList = explode(",", $this->hid_examno);
        $this->printExamNo = "";
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $receptno = $tmp[0];
            $examno = $tmp[1];
            $this->field["PRINT_DATA"][$receptno] = VARS::post("PRINT_DATA"."-".$receptno);
        }
        if (isset($this->field["PRINT_DATA"])) {
            $this->printExamNo = knjl131fQuery::getUpdatePrintQuery($this);
            if ($result) {
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
