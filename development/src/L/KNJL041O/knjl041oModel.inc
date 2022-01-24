<?php

require_once('for_php7.php');

//メニュー画面
class knjl041oModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $hallcd;
    var $OjbYear;               //入試年度

    function knjl041oModel()
    {
        $this->ObjYear     = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd    = VARS::request("cmd");

        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->hallcd       = strlen(VARS::post("HALLCD")) ? VARS::post("HALLCD") : VARS::post("HID_HALLCD");
        $this->sort         = VARS::post("SORT");
        
        $this->hid_receptno = VARS::post("HID_RECEPTNO");
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->setReceptNo = array();
        $this->arrTargetFlg = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_receptno);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrTargetFlg[$receptno] = VARS::post("TARGET_FLG-".$receptno);
        }

        //更新
        $result = knjl041oQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
