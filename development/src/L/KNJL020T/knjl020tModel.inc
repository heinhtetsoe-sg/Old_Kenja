<?php

require_once('for_php7.php');

//メニュー画面
class knjl020tModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $examno;
    var $OjbYear;               //入試年度
    var $score;
    var $rpt3;                  //3教科
    var $rpt5;                  //5教科
    var $editable_rpt;          //編集可能教科フラグ
    var $colname = array();     //項目名称

    function knjl020tModel()
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
        //点数を再配置(エラー時再表示するために一度すべてセットする)
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno)
        {
            $sum5 = $cnt5 = $sum3 = $cnt3 = $sumA = $cntA = 0;
            foreach ($this->field as $col => $val)
            {
                //編集可能項目の値のみ上書き
                if ($col == "ABSENCE_DAYS" || $col == "ABSENCE_DAYS2" || $col == "ABSENCE_DAYS3" || in_array($col, $this->editable_rpt)) {
                    $this->score[$examno][$col] = $val[$key];
                }
                //５教科平均
                if (in_array($col, $this->rpt5) && strlen($val[$key])) {
                    $sum5 += (int)$val[$key];
                    $cnt5++;
                }
                //３教科平均
                if (in_array($col, $this->rpt3) && strlen($val[$key])) {
                    $sum3 += (int)$val[$key];
                    $cnt3++;
                }
                //全体平均
                if ($col != "ABSENCE_DAYS" && $col != "ABSENCE_DAYS2" && $col != "ABSENCE_DAYS3" && strlen($val[$key])) {
                    $sumA += (int)$val[$key];
                    $cntA++;
                }
            }
            $this->score[$examno]["avg5"] = ($cnt5 > 0) ? round($sum5 / $cnt5, 1) : "NULL";
            $this->score[$examno]["avg3"] = ($cnt3 > 0) ? round($sum3 / $cnt3, 1) : "NULL";
            $this->score[$examno]["avgA"] = ($cntA > 0) ? round($sumA / $cntA, 1) : "NULL";
            $this->score[$examno]["sum5"] = ($cnt5 > 0) ? $sum5 : "NULL";   //３教科合計
            $this->score[$examno]["sum3"] = ($cnt3 > 0) ? $sum3 : "NULL";   //５教科合計
            $this->score[$examno]["sumA"] = ($cntA > 0) ? $sumA : "NULL";   //全体合計
        }

        //最大値チェック(MAX999まで)
        foreach ($this->score as $examno => $val) {

            foreach ($val as $col => $score) {
                if ($col == "avg5" || $col == "sum5" || $col == "avg3" || $col == "sum3" || $col == "avgA" || $col == "sumA" || $col == "addA") continue;

                if (($col == "ABSENCE_DAYS" || $col == "ABSENCE_DAYS2" || $col == "ABSENCE_DAYS3") && (int)$score > 999) {

                    $this->setWarning("MSG901", "( 最大 999まで )\\n\\n受検番号：".$examno."\\n"."欠席日数");
                    return false;

                } elseif ($col != "ABSENCE_DAYS" && $col != "ABSENCE_DAYS2" && $col != "ABSENCE_DAYS3" && (int)$score > 999){

                    $this->setWarning("MSG901", "( 最大 999まで )\\n\\n受検番号：".$examno."\\n".$this->colname[$col]);
                    return false;
                }
            }
        }

        $result = knjl020tQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
