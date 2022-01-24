<?php

require_once('for_php7.php');

//メニュー画面
class knjl050hModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $OjbYear;               //入試年度
    var $examhallcd;

    function knjl050hModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        
        $this->tmp_score      = VARS::post("SCORE"); //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = array();

        //得点を再配置
       $tmp = explode(",",$this->hid_receptno);
       foreach ($tmp as $key => $receptno)
       {
           $this->score[$receptno] = $this->tmp_score[$key];
       }
       $result = knjl050hQuery::getUpdateQuery($this);
       if ($result) {
           $this->setMessage("MSG201","\\n全受験型の得点入力が完了したら\\n偏差値算出処理を実行して下さい。");
       }
    }
}
?>
