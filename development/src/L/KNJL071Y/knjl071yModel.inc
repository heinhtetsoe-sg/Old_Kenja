<?php

require_once('for_php7.php');


class knjl071yModel extends model
{
    var $cmd;
    var $applicantdiv;
    var $appHold;
    var $testdiv;
    var $desirediv;
    var $recom_kind;
    var $exam_type;
    var $examno;
    var $ObjYear;
    var $sort;
    var $score;
    var $inout;
    var $kikoku;
    var $rate_div;

    function knjl071yModel()
    {
    }

    function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->cmd = VARS::request("cmd");
        
        
        $this->applicantdiv = strlen(VARS::post("APPLICANTDIV")) ? VARS::post("APPLICANTDIV") : VARS::post("HID_APPLICANTDIV");
        $this->appHold      = VARS::post("APP_HOLD");
        $this->testdiv      = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->exam_type    = "1";
        $this->desirediv    = strlen(VARS::post("DESIREDIV")) ? VARS::post("DESIREDIV") : VARS::post("HID_DESIREDIV");
        $this->recom_kind   = strlen(VARS::post("RECOM_KIND")) ? VARS::post("RECOM_KIND") : VARS::post("HID_RECOM_KIND");

        $this->sort         = VARS::post("SORT");
        $this->hid_examno   = VARS::post("HID_EXAMNO");
        $this->inout        = VARS::post("INOUT");
        $this->kikoku       = VARS::post("KIKOKU");
        $this->rate_div     = VARS::post("RATE_DIV");

        $this->field = array("JUDGEDIV" => VARS::post("JUDGEDIV"));
    }

    function &getMainModel()
    {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->score = array();
        //備考・評価を再配置
        $tmp = explode(",",$this->hid_examno);
        foreach ($tmp as $key => $examno) {
            foreach ($this->field as $col => $val) {
                $this->score[$examno][$col] = $val[$key];
            }
        }
        //名称マスタの取得（判定コード）
        $db = Query::dbCheckOut();
        $opt_judge = array();
        $result = $db->query(knjl071yQuery::getNameMst2("L013", $this->ObjYear));
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
            }
        }
        //更新処理
        $result = knjl071yQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
