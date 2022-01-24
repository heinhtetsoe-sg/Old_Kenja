<?php

require_once('for_php7.php');

class knjl082aModel extends model
{
    public $cmd;
    public $applicantdiv;
    public $testdiv;
    public $examType;
    public $ObjYear;

    public function knjl082aModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType = 1;    //1固定

        $this->applicantdiv = VARS::post("APPLICANTDIV");

        $this->cmd = VARS::request("cmd");

        $this->testdiv = VARS::post("TESTDIV");

        $this->hid_ReceptNo = VARS::post("HID_RECEPTNO");
    }

    public function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //初期化
        $this->setReceptNo =
        $this->arrExamNo = $this->arrJitaiFlg = array();

        //再配置
        $this->setReceptNo = explode(",", $this->hid_ReceptNo);
        foreach ($this->setReceptNo as $key => $receptno) {
            $this->arrExamNo[$receptno] = VARS::post("EXAMNO-".$receptno);
            $this->arrJitaiFlg[$receptno] = VARS::post("JITAI_FLG-".$receptno);
        }

        //更新
        knjl082aQuery::getUpdateQuery($this);
        $this->setMessage("MSG201");
    }
}
