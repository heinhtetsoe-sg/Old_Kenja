<?php

require_once('for_php7.php');


class knjl015dModel extends model
{
    var $field      = array();
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $ObjYear;

    function knjl015dModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        $this->field = array("MOVE_ENTER"       =>  VARS::post("MOVE_ENTER")
                            );   //受験番号の開始
        
        $this->applicantdiv   = 1;
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->finschool_cd   = strlen(VARS::post("FINSCHOOLCD")) ? VARS::post("FINSCHOOLCD") : VARS::post("HID_FINSCHOOLCD");
        
        $this->exam_type      = "1";
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");//"RECEPTNO"-"COUNT"で取得
        
        //受験番号ごとに配置
        $this->selection_Remark = array();
        $this->selection_Remark2 = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->selection_Remark[$receptno_array[1]]  = VARS::post("SELECTION_REMARK-".$receptno_array[1]);
            $this->selection_Remark2[$receptno_array[1]] = VARS::post("SELECTION_REMARK2-".$receptno_array[1]);
        }
    }

    //受験番号をセット
    function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            if (strlen($this->selection_Remark[$receptno_array[1]]) > 60) {
                $this->setWarning("MSG914","選抜Ⅰは全角20文字までです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if (strlen($this->selection_Remark2[$receptno_array[1]]) > 60) {
                $this->setWarning("MSG914","選抜Ⅱは全角20文字までです。(受験番号:".$receptno_array[0].")");
                return false;
            }
        }

        $tmp = explode(",", $this->hid_receptno);
        $receptno_array = array();
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            
            $searchCnt = knjl015dQuery::chkDataQuery($this, $receptno_array);
            if ($searchCnt === "0"){
                knjl015dQuery::insertQuery($this, $receptno_array);
            }else{
                knjl015dQuery::UpdateQuery($this, $receptno_array);
            }
        }
        $this->setMessage("MSG201");
    }
}
?>
