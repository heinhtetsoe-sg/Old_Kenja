<?php

require_once('for_php7.php');


class knjl110bModel extends model
{
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $tangan_kirikae;
    var $examno;
    var $ObjYear;

    function knjl110bModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;
        $this->cmd    = VARS::request("cmd");
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->tangan_kirikae = VARS::post("TANGAN_KIRIKAE");

        $this->hid_examno   = VARS::post("HID_EXAMNO");//"EXAMNO"-"COUNT"で取得
        
        //各希望を受験番号ごとに配置
        $this->remark1 = array();
        $this->remark2 = array();
        $this->remark3 = array();
        $this->remark4 = array();
        $this->remark5 = array();
        $this->remark6 = array();
        $this->remark7 = array();
        $this->remark8 = array();
        $examno_array = array();
        $tmp = explode(",", $this->hid_examno);
        foreach ($tmp as $key => $examno_count) {
            $examno_array = explode("-", $examno_count);
            $this->remark1[$examno_array[1]]  = VARS::post("REMARK1-".$examno_array[1]);
            $this->remark2[$examno_array[1]]  = VARS::post("REMARK2-".$examno_array[1]);
            $this->remark3[$examno_array[1]]  = VARS::post("REMARK3-".$examno_array[1]);
            $this->remark4[$examno_array[1]]  = VARS::post("REMARK4-".$examno_array[1]);
            $this->remark5[$examno_array[1]]  = VARS::post("REMARK5-".$examno_array[1]);
            $this->remark6[$examno_array[1]]  = VARS::post("REMARK6-".$examno_array[1]);
            $this->remark7[$examno_array[1]]  = VARS::post("REMARK7-".$examno_array[1]);
            $this->remark8[$examno_array[1]]  = VARS::post("REMARK8-".$examno_array[1]);
        }
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->interViewCheck = array();
        knjl110bQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
