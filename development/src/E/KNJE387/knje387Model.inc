<?php

require_once('for_php7.php');


class knje387Model extends model
{
    var $field      = array();
    var $cmd;
    var $grade;
    var $schregno;
    var $ObjYear;

    function knje387Model()
    {
    }

    function init()
    {
        $this->ObjYear      = CTRL_YEAR;
        $this->cmd          = VARS::request("cmd");
        $this->grade        = VARS::get("GRADE") != "" ? VARS::get("GRADE") : VARS::post("GRADE");
        $this->schoolcd     = SCHOOLCD;
        $this->schoolkind   = SCHOOLKIND;
        $this->hid_schregno = VARS::post("HID_SCHREGNO");//"SCHREGNO"-"COUNT"で取得
        $this->sorttype     = VARS::post("SORT_TYPE");
        
        //受験番号ごとに配置
        $this->inputadjust = array();
        $schregno_array = array();
        $tmp = explode(",", $this->hid_schregno);
        foreach ($tmp as $key => $schregno_count) {
            $schregno_array = explode("-", $schregno_count);
            $this->inputadjust[$schregno_array[1]]  = VARS::post("INPUT_ADJUST-".$schregno_array[1]);
        }
    }

    //受験番号をセット
    function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $schregno_array = array();
        $tmp = explode(",", $this->hid_schregno);

        //DB接続
        $db = Query::dbCheckOut();

        //DB切断
        Query::dbCheckIn($db);

        foreach ($tmp as $key => $schregno_count) {
            $schregno_array = explode("-", $schregno_count);
            if (strlen($this->inputadjust[$schregno_array[1]]) > 0) {
                if (strlen($this->inputadjust[$schregno_array[1]]) > 4) {
                    $this->setWarning("MSG914","加減点計は数字4文字までです。(受験番号:".$schregno_array[0].")");
                    return false;
                }
                if (!is_numeric($this->inputadjust[$schregno_array[1]])) {
                    $this->setWarning("MSG914","加減点計は数字4文字までです。(受験番号:".$schregno_array[0].")");
                    return false;
                }
            }
        }

        $tmp = explode(",", $this->hid_schregno);
        $schregno_array = array();
        foreach ($tmp as $key => $schregno_count) {
            $schregno_array = explode("-", $schregno_count);

            $searchCnt = knje387Query::cntUpdTblQuery($this, "AFT_TOTAL_STUDY_BATCH_DAT", $schregno_array[0]);
            knje387Query::UpdQuery_TSBatch($this, $schregno_array, $searchCnt);
            $searchCnt = knje387Query::cntUpdTblQuery($this, "AFT_TOTAL_STUDY_ADJUSTMENT_DAT", $schregno_array[0]);
            knje387Query::UpdQuery_TSAdjustment($this, $schregno_array, $searchCnt);
        }

        $this->setMessage("MSG201");
    }

    function sortSetting($setsortcol) {
        if ($setsortcol == "score1_sort") {
            $this->sortRule("SCORE1SORT_DESC", "SCORE1SORT_ASC");
        } else if ($setsortcol == "score2_sort") {
            $this->sortRule("SCORE2SORT_DESC", "SCORE2SORT_ASC");
        } else if ($setsortcol == "score3_sort") {
            $this->sortRule("SCORE3SORT_DESC", "SCORE3SORT_ASC");
        } else if ($setsortcol == "score4_sort") {
            $this->sortRule("SCORE4SORT_DESC", "SCORE4SORT_ASC");
        } else if ($setsortcol == "score5_sort") {
            $this->sortRule("SCORE5SORT_DESC", "SCORE5SORT_ASC");
        } else if ($setsortcol == "sort_total") {
            $this->sortRule("TOTAL_DESC", "TOTAL_ASC");
        } else if ($setsortcol == "sort_class") {
            $this->sortRule("CLASS_DESC", "CLASS_ASC");
        } else {
            $this->sorttype = "";
        }
    }

    function sortRule($sort1, $sort2) {
        $now_sorttype = $this->sorttype;
        if ($now_sorttype === $sort2) {
            $this->sorttype = "";
        } else if ($now_sorttype === $sort1) {
            $this->sorttype = $sort2;
        } else {
            $this->sorttype = $sort1;
        }
    }

}
?>
