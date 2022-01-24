<?php
//メニュー画面
class knjl022yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $testdiv;
    var $appHold;
    var $examno;
    var $OjbYear;               //入試年度
    var $score;
    var $editable_rpt;          //編集可能教科フラグ
    var $colname = array();     //項目名称

    function knjl022yModel()
    {
    }

    function init()
    {
        $this->ObjYear  = CTRL_YEAR+1;
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->examno = sprintf("%05d",VARS::post("EXAMNO"));
        }
        
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->field = array("ACTIVITY" => VARS::post("ACTIVITY"),
                             "SECTION"  => VARS::post("SECTION"),
                             "RESULTS"  => VARS::post("RESULTS"));
    }

    function &getMainModel()
    {
        /* 最大9999まで */
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
        if ($this->cmd == "reset") $this->cmd = "read";
    }

    function &getUpdateModel()
    {
        $this->score = array();
        //点数を再配置(エラー時再表示するために一度すべてセットする)
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                $this->score[$examno][$col] = $val[$key];
            }
        }
        //入力値チェック
        foreach ($this->score as $examno => $val) {
            foreach ($val as $col => $score) {
                if ($col == "ACTIVITY" && strlen($score) > 60) {
                    $this->setWarning("MSG901", "( 活動：60バイトまで )\\n\\n受験番号：".$examno);
                    return false;
                } else if ($col == "SECTION" && strlen($score) > 60) {
                    $this->setWarning("MSG901", "( 所属：60バイトまで )\\n\\n受験番号：".$examno);
                    return false;
                } else if ($col == "RESULTS" && strlen($score) > 240) {
                    $this->setWarning("MSG901", "( 主な実績：240バイトまで )\\n\\n受験番号：".$examno);
                    return false;
                }
            }
        }
        //更新
        $result = knjl022yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
