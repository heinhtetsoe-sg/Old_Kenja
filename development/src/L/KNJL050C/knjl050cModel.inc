<?php

require_once('for_php7.php');


class knjl050cModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $exam_type;
    var $examno;
    var $OjbYear;
    var $examhallcd;
    var $isCollege;

    function knjl050cModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        
        
        $this->applicantdiv   = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type      = "1";
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        $this->examhallcd     = strlen(VARS::post("EXAMHALLCD")) ? VARS::post("EXAMHALLCD") : VARS::post("HID_EXAMHALLCD");
        
        $this->tmp_score      = VARS::post("SCORE"); //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl050cQuery::getNameMstZ010();
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
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
       $result = knjl050cQuery::getUpdateQuery($this);
       if ($result) {
           $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
       }
    }
}
?>
