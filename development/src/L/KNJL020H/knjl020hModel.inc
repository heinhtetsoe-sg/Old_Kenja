<?php

require_once('for_php7.php');

//メニュー画面
class knjl020hModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $applicantdiv;
    var $examno;
    var $OjbYear;               //入試年度
    var $score;
    var $rpt5;                  //5教科
    var $editable_rpt;          //編集可能教科フラグ
    var $colname = array();     //項目名称
    var $suisenKasanTen;        //高校一般受験で推薦も受けていた生徒の加算点

    function knjl020hModel()
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
        $this->hid_examno   = VARS::post("HID_EXAMNO");
        $this->suisenKasanTen = VARS::post("suisenKasanTen");

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
                             "RPT11" => VARS::post("RPT11"),
                             "RPT12" => VARS::post("RPT12"),
                             "absence_days" => VARS::post("ABSENCE_DAYS"));
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
        if ((int)$this->e_examno > 9999) $this->e_examno = "9999";
        if ($this->cmd == "reset")
            $this->cmd = "read";

    }

    function &getUpdateModel()
    {
        //加算点とその範囲を配列にセット
        $db           = Query::dbCheckOut();
        $kasan_ten = $kasan_min = $kasan_max = array();
        $query  = knjl020hQuery::GetName("L014",$this->ObjYear);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $kasan_ten[] = $row["NAME1"];
            $kasan_min[] = $row["NAMESPARE1"];
            $kasan_max[] = $row["NAMESPARE2"];
        }

        //点数を再配置(エラー時再表示するために一度すべてセットする)
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno)
        {
            $sum5 = $cnt5 = $sumA = $cntA = 0;
            $addA_flg = false;

            foreach ($this->field as $col => $val)
            {
                //編集可能項目の値のみ上書き
                if ($col == "absence_days" || in_array($col, $this->editable_rpt)) {
                    $this->score[$examno][$col] = $val[$key];
                }
                //５教科平均
                if (in_array($col, $this->rpt5) && strlen($val[$key])) {
                    $sum5 += (int)$val[$key];
                    $cnt5++;
                }
                //全体平均
                if ($col != "absence_days" && strlen($val[$key])) {
                    $sumA += (int)$val[$key];
                    $cntA++;
                    if ((int)$val[$key] <= 2) $addA_flg = true;
                }
            }
            $this->score[$examno]["avg5"] = ($cnt5 > 0) ? round($sum5 / $cnt5, 1) : "NULL";
            $this->score[$examno]["avgA"] = ($cntA > 0) ? round($sumA / $cntA, 1) : "NULL";
            $this->score[$examno]["sumA"] = ($cntA > 0) ? $sumA : "NULL";//全体合計
            if ($cntA > 0 && $this->applicantdiv == "2") {      //入試制度=2 ⇒ 高校一般
                $addA = -1;
                for ($i = 0; $i < get_count($kasan_ten); $i++) {
                    if ((int)$kasan_min[$i] <= $sumA && $sumA <= (int)$kasan_max[$i]) {
                        $addA = (int)$kasan_ten[$i];
                    }
                }
                if ($addA_flg) $addA = 0;//1科目でも内申点に「2以下」があれば加算0点
                $this->score[$examno]["addA"] = ($addA > -1) ? $addA : "NULL";//加算点
                //推薦も受けた生徒は10点加算
                $query     = knjl020hQuery::suisenCheck($this->ObjYear, $examno);
                $suisen    = $db->getOne($query);
                if ($suisen != null) {
                    $this->score[$examno]["addA"] += $this->suisenKasanTen;
                }
            } else {
                $this->score[$examno]["addA"] = "NULL";//加算点
            }
        }
        Query::dbCheckIn($db);

        //最大値チェック(MAX999まで)
        foreach ($this->score as $examno => $val) {

            foreach ($val as $col => $score) {
                if ($col == "avg5" || $col == "avgA" || $col == "sumA" || $col == "addA") continue;

                if ($col == "absence_days" && (int)$score > 999) {	//2005.12.28 alp m-yama 99→999

                    $this->setWarning("MSG901", "( 最大 999まで )\\n\\n受験番号：".$examno."\\n"."欠席日数");
                    return false;

                } elseif ($col != "absence_days" && (int)$score > 999){

                    $this->setWarning("MSG901", "( 最大 999まで )\\n\\n受験番号：".$examno."\\n".$this->colname[$col]);
                    return false;
                }
            }
        }

        $result = knjl020hQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
