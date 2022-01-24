<?php

require_once('for_php7.php');


class knjl220cModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $exam_type;
    var $examno;
    var $OjbYear;
    var $score = array();

    function knjl220cModel()
    {
    }

    function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");

        $this->applicantdiv     = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->preTestdiv       = strlen(VARS::post("PRE_TESTDIV")) ? VARS::post("PRE_TESTDIV") : VARS::post("HID_PRE_TESTDIV");
        $this->testsubclasscd   = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");

        $this->tmp_score = array();
        $this->tmp_score      = VARS::post("SCORE"); //配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //初期化
        $this->score = array();
        //得点を再配置
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno) {
            $this->score[$receptno] = $this->tmp_score[$key];
        }
        //追加or更新
        $result = knjl220cQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全ての得点入力が完了したら\\n成績確定処理を実行して下さい。");
        }
    }
}
?>
