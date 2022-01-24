<?php

require_once('for_php7.php');

//メニュー画面
class knjl020yModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $examno;
    var $OjbYear;               //入試年度
    var $score;
    var $editable_rpt;          //編集可能教科フラグ
    var $colname = array();     //項目名称

    function knjl020yModel()
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
        $this->hid_examno   = VARS::post("HID_EXAMNO");

        $this->field = array("RPT01" => VARS::post("RPT01"),
                             "RPT02" => VARS::post("RPT02"),
                             "RPT03" => VARS::post("RPT03"),
                             "RPT04" => VARS::post("RPT04"),
                             "RPT05" => VARS::post("RPT05"),
                             "RPT06" => VARS::post("RPT06"),
                             "RPT07" => VARS::post("RPT07"),
                             "RPT08" => VARS::post("RPT08"),
                             "RPT09" => VARS::post("RPT09"),
                             "RPT10" => VARS::post("RPT10"),
                             "AVERAGE_ALL"  => VARS::post("AVERAGE_ALL"),
                             "AVERAGE5"  => VARS::post("AVERAGE5"),
                             "ABSENCE_DAYS"  => VARS::post("ABSENCE_DAYS"),
                             "ABSENCE_DAYS2" => VARS::post("ABSENCE_DAYS2"),
                             "ABSENCE_DAYS3" => VARS::post("ABSENCE_DAYS3"));
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
        foreach ($tmp as $key => $examno)
        {
            $sumA = $cntA = 0;
            foreach ($this->field as $col => $val)
            {
                //編集可能項目の値のみ上書き
                if ($col == "AVERAGE_ALL" || $col == "AVERAGE5" || $col == "ABSENCE_DAYS" || $col == "ABSENCE_DAYS2" || $col == "ABSENCE_DAYS3" || in_array($col, $this->editable_rpt)) {
                    $this->score[$examno][$col] = $val[$key];
                }
            }
        }

        $result = knjl020yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
