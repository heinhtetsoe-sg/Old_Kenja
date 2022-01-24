<?php

require_once('for_php7.php');

class knjl081aModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examType;
    public $ObjYear;

    public function knjl081aModel()
    {
    }

    public function init()
    {
        $this->ObjYear  = CTRL_YEAR+1;
        $this->examType = 1;    //固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->fs_cd        = VARS::post("FS_CD");
        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");

        $this->cmd = VARS::request("cmd");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //初期化
        $this->setReceptNo =
        $this->arrExamNo = $this->arrProcedureDiv = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrExamNo[$receptno] = VARS::post("EXAMNO-".$receptno);
            $this->arrProcedureDiv[$receptno] = VARS::post("PROCEDUREDIV-".$receptno);
        }

        //更新
        knjl081aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
