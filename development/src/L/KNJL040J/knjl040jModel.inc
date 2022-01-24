<?php

require_once('for_php7.php');

//メニュー画面
class knjl040jModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $testdiv;
    var $exam_type;
    var $examno;
    var $OjbYear;               //入試年度
    var $receptno;
    var $data  = array();

    function knjl040jModel()
    {
        $this->ObjYear     = CTRL_YEAR+1;
    }

    function init()
    {
        $this->cmd    = VARS::request("cmd");
 
        if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->examno = sprintf("%05d",VARS::post("EXAMNO"));
        }
        
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = strlen(VARS::post("EXAM_TYPE")) ? VARS::post("EXAM_TYPE") : VARS::post("HID_EXAM_TYPE");
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->field = array("RECEPTNO" => VARS::post("RECEPTNO"));
        if (VARS::post("RECEPTNO")) {
            $this->receptno = VARS::post("RECEPTNO"); //配列
        }
    }

    function &getMainModel()
    {
        /* 最大99999まで */
        if ($this->cmd == "back") {
            $this->examno = sprintf("%05d",((int)$this->examno - 50));
            if ((int)$this->examno < 1) {
                $this->examno = "00001";
            } elseif ((int)$this->examno > 99999) {
                $this->examno = "99999";
            }
        }
        if ($this->cmd == "next") {
            $this->examno = sprintf("%05d",((int)$this->examno + 50));
            if ((int)$this->examno < 1) {
                $this->examno = "00001";
            } elseif ((int)$this->examno > 99999) {
                $this->examno = "99999";
            }
        }

        $this->e_examno = sprintf("%05d", $this->examno + 49);
        if ((int)$this->e_examno > 99999) $this->e_examno = "99999";
        if ($this->cmd == "reset")
            $this->cmd = "read";

    }

    function &getUpdateModel()
    {
        $temp = array_count_values($this->receptno);
        foreach ($temp as $key => $val)
        {
            if (strlen($key) && $val > 1) {
                $this->setWarning("MSG302", "座席番号：".$key);
                return;
            }
        }
        $result = knjl040jQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
