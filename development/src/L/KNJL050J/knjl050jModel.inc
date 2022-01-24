<?php

require_once('for_php7.php');

//メニュー画面
class knjl050jModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $receptno;
    var $testdiv;
    var $examno;
    var $ObjYear;               //入試年度
        
    function knjl050jModel()
    {
        $this->ObjYear     = CTRL_YEAR+1;
    }
        
    function init()
    {
        $this->cmd    = VARS::request("cmd");
        
        if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->receptno = sprintf("%04d",VARS::post("RECEPTNO"));
        }
        
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->testsubclasscd = strlen(VARS::post("TESTSUBCLASSCD")) ? VARS::post("TESTSUBCLASSCD") : VARS::post("HID_TESTSUBCLASSCD");
        
        $this->tmp_score      = VARS::post("SCORE"); //配列
        $this->tmp_remark     = VARS::post("REMARK");//配列
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");
    }

    function &getMainModel()
    {
    /* 最大9999まで */
    
        if ($this->cmd == "back") {
            $this->receptno = sprintf("%04d",((int)$this->receptno - 40));
            if ((int)$this->receptno < 1) {
                $this->receptno = "0001";
            } elseif ((int)$this->receptno > 9999) {
                $this->receptno = "9999";
            }
        }
        if ($this->cmd == "next") {
            $this->receptno = sprintf("%04d",((int)$this->receptno + 40));
            if ((int)$this->receptno < 1) {
                $this->receptno = "0001";
            } elseif ((int)$this->receptno > 9999) {
                $this->receptno = "9999";
            }
        }

        $this->e_receptno = sprintf("%04d", $this->receptno + 39);
        if ((int)$this->e_receptno > 9999) $this->e_receptno = "9999";
        if ($this->cmd == "reset")
            $this->cmd = "read";
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = $this->remark = array();

        //得点を再配置
        $tmp = explode(",",$this->hid_receptno);
        foreach ($tmp as $key => $receptno)
        {
            $this->score[$receptno] = $this->tmp_score[$key];
            $this->remark[$receptno] = $this->tmp_remark[$key];
        }
        $result = knjl050jQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201","\\n全受験型の得点入力が完了したら\\n偏差値算出処理を実行して下さい。");
        }
    }
}
?>
