<?php

require_once('for_php7.php');


class knjl072bModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $ObjYear;
    var $sort;
    var $judgment_div_search;
    var $avg_from;
    var $avg_to;
    var $special_reason_div;

    var $judgment_div;
    var $field = array();

    function knjl072bModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->sort         = VARS::post("SORT");
        $this->judgment_div_search = VARS::post("JUDGMENT_DIV_SEARCH");
        $this->avg_from     = VARS::post("AVG_FROM");
        $this->avg_to       = VARS::post("AVG_TO");
        $this->special_reason_div = VARS::post("SPECIAL_REASON_DIV");

        $this->cmd = VARS::request("cmd");

        $this->judgment_div = VARS::post("JUDGMENT_DIV");
        $this->field["CHK_DATA"] = VARS::post("CHK_DATA");
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        if (isset($this->field["CHK_DATA"])) {
            $rtnMsg = knjl072bQuery::decision($this);
            $this->setMessage($rtnMsg);
        } else {
            $this->setMessage("MSG310");
        }
    }
}
?>
