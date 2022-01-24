<?php

require_once('for_php7.php');


class knjl071cModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $testdiv;
    var $examno;
    var $OjbYear;               //入試年度
    var $score;
    var $sort;
    var $shdiv;
    var $isGojou;
    var $isCollege;

    function knjl071cModel()
    {
    }

    function init()
    {
        $this->ObjYear  = CTRL_YEAR+1;
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == "read" || $this->cmd == "back" || $this->cmd == "next") {
            $this->examno = sprintf("%04d",VARS::post("EXAMNO"));
        }
        
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->hid_examno   = VARS::post("HID_EXAMNO");
        $this->sort = VARS::post("SORT");
        $this->shdiv = VARS::post("SHDIV");

        $this->field = array("JUDGEDIV" => VARS::post("JUDGEDIV"));
        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl071cQuery::getNameMstZ010();
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
    }

    function &getMainModel()
    {
        /* 最大9999まで */
        if ($this->cmd == "back") {
            $this->examno = sprintf("%04d",((int)$this->examno - 50));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno > 9999) {
                $this->examno = "9999";
            }
        }
        if ($this->cmd == "next") {
            $this->examno = sprintf("%04d",((int)$this->examno + 50));
            if ((int)$this->examno < 1) {
                $this->examno = "0001";
            } elseif ((int)$this->examno > 9999) {
                $this->examno = "9999";
            }
        }

        $this->e_examno = sprintf("%04d", $this->examno + 49);
        if ($this->sort == "2") {
            $row = knjl071cQuery::getMaxExamno($this);
            if (strlen($row["MIN_EXAMNO"])) $this->examno   = $row["MIN_EXAMNO"];
            if (strlen($row["MAX_EXAMNO"])) $this->e_examno = $row["MAX_EXAMNO"];
        }
        if ((int)$this->e_examno > 9999) $this->e_examno = "9999";
        if ($this->cmd == "reset")
            $this->cmd = "read";

    }

    function &getUpdateModel()
    {
        $this->score = array();
        //点数を再配置(エラー時再表示するために一度すべてセットする)
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                //編集可能項目の値のみ上書き
                if ($col == "JUDGEDIV") {
                    $this->score[$examno][$col] = $val[$key];
                }
            }
        }

        //名称マスタの取得（判定コード）
        $db = Query::dbCheckOut();
        $opt_judge = array();
        $result = $db->query(knjl071cQuery::GetName("L013",$this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_judge[] = $row["NAMECD2"];
        }
        Query::dbCheckIn($db);
        //判定コード値チェック
        foreach ($this->score as $examno => $val) {
            foreach ($val as $col => $score) {
                if ($col == "JUDGEDIV" && $score != "" && !in_array($score, $opt_judge)) {
                    $this->setWarning("MSG901", "( 判定 )\\n\\n受験番号：".$examno);
                    return false;
                }
                //移行合格（移行希望）チェック
                if ($col == "JUDGEDIV" && $score == "4") {
                    $row = knjl071cQuery::shiftCheck($this->ObjYear, $examno);
                    if (!isset($row["EXAMNO"])) {
                        $this->setWarning("MSG901", "( 判定 )\\n\\n受験番号：".$examno."\\n移行合格は移行希望有りの場合のみです。");
                        return false;
                    }
                }
                //特併合格チェック
                if ($col == "JUDGEDIV" && $score == "7") {
                    $row = knjl071cQuery::slideCheck($this->ObjYear, $examno);
                    if (!isset($row["EXAMNO"])) {
                        $this->setWarning("MSG901", "( 判定 )\\n\\n受験番号：".$examno."\\n特併合格は特進併願希望有りの場合のみです。");
                        return false;
                    }
                }
            }
        }

        //更新
        $result = knjl071cQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
