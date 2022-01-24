<?php

require_once('for_php7.php');


class knjl040cModel extends model
{
    var $cmd;
    var $receptno;
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $ObjYear;
    var $isCollege;
    var $isGojou;

    function knjl040cModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->cmd    = VARS::request("cmd");

        if ($this->cmd == "read" || $this->cmd == "next" || $this->cmd == "back") {
            if (strlen(VARS::post("RECEPTNO"))) {
                $this->receptno = sprintf("%04d",VARS::post("RECEPTNO"));
            }
        }

        if (strlen(VARS::post("APPLICANTDIV"))) {
            $this->applicantdiv = VARS::post("APPLICANTDIV");
        } elseif (strlen(VARS::post("HID_APPLICANTDIV"))) {
            $this->applicantdiv = VARS::post("HID_APPLICANTDIV");
        }
        if (strlen(VARS::post("TESTDIV"))) {
            $this->testdiv = VARS::post("TESTDIV");
        } elseif (strlen(VARS::post("HID_TESTDIV"))) {
            $this->testdiv = VARS::post("HID_TESTDIV");
        }
        
        if (VARS::post("EXAMNO")) {
            $this->examno       = VARS::post("EXAMNO"); //配列
        }

        //学校コードを取得：カレッジを判断するため
        $rtnRow = knjl040cQuery::getNameMstZ010();
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isGojou   = $rtnRow["NAME2"] == "30290053001" ? true : false;
    }

    function &getMainModel()
    {
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
        
        $this->e_receptno = sprintf("%04d", (int)$this->receptno + 39);
        if ((int)$this->e_receptno > 9999) $this->e_receptno = "9999";  //最大9999まで
         
        if ($this->cmd == "reset")
            $this->cmd = "read";
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力値の重複チェック
        $temp = array_count_values($this->examno);
        foreach ($temp as $key => $val)
        {
            if (strlen($key) && $val > 1) {
                $this->setWarning("MSG302", "受験番号：".$key);
                return;
            }
        }

        $result = knjl040cQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
