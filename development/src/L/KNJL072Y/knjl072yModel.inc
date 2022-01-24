<?php

require_once('for_php7.php');


class knjl072yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $testdiv;
    var $desirediv;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $sort;
    var $score;
    var $inout;
    var $kikoku;
    var $rate_div;

    function knjl072yModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        
        
        $this->applicantdiv = "2";
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->desirediv    = strlen(VARS::post("DESIREDIV")) ? VARS::post("DESIREDIV") : VARS::post("HID_DESIREDIV");
        $this->exam_type    = "1";

        $this->sort         = VARS::post("SORT");
        $this->hid_examno   = VARS::post("HID_EXAMNO");
        $this->inout        = VARS::post("INOUT");
        $this->kikoku       = VARS::post("KIKOKU");
        $this->rate_div     = VARS::post("RATE_DIV");

        $this->field = array("JUDGE_KIND" => VARS::post("JUDGE_KIND"));
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = array();
        //合格種別を再配置
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                $this->score[$examno][$col] = $val[$key];
            }
        }
        //名称マスタの取得
        $db = Query::dbCheckOut();
        $opt_judge = array();
        $namespare2 = array();
        $name1 = array();
        $result = $db->query(knjl072yQuery::getNameMst2("L025", $this->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_judge[] = $row["NAMECD2"];
            $namespare2[$row["NAMECD2"]] = $row["NAMESPARE2"];
            $name1[$row["NAMECD2"]] = $row["NAME1"];
        }
        Query::dbCheckIn($db);
        //合格種別の値チェック
        foreach ($this->score as $examno => $val) {
            $sports_flg = VARS::post("SPORTS_FLG" .$examno);
            $sports_flg2 = VARS::post("SPORTS_FLG2" .$examno);
            foreach ($val as $col => $score) {
                if ($col == "JUDGE_KIND" && $score != "" && !in_array($score, $opt_judge)) {
                    $this->setWarning("MSG901", "( 合格種別 )\\n\\n受験番号：".$examno);
                    return false;
                }
                //他の合格の可能性も考慮して、「FLGはT特奨の合格ではないのに合格選択でT合格となっている物」はエラーにする
                if ($col == "JUDGE_KIND" && $score == "8") {
                    if ($sports_flg2 != "1" && $namespare2[$score] == "1") {
                        $this->setWarning("MSG901", "( 合格種別 )\\n\\n受験番号：".$examno."\\n【{$name1[$score]}】はT特奨希望者のみ入力可能です。");
                        return false;
                    }
                } else if ($col == "JUDGE_KIND" && $score != "" && $sports_flg != "1" && $namespare2[$score] == "1") {
                    $this->setWarning("MSG901", "( 合格種別 )\\n\\n受験番号：".$examno."\\n【{$name1[$score]}】はスポーツ希望者のみ入力可能です。");
                    return false;
                }
            }
        }
        //更新処理
        $result = knjl072yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
