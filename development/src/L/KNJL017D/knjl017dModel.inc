<?php

require_once('for_php7.php');


class knjl017dModel extends model
{
    public $field      = array();
    public $cmd;
    public $applicantdiv;
    public $appHold;
    public $testdiv;
    public $exam_type;
    public $examno;
    public $ObjYear;

    public function knjl017dModel()
    {
    }

    public function init()
    {
        $this->ObjYear     = CTRL_YEAR+1;

        $this->cmd    = VARS::request("cmd");
        $this->field = array("MOVE_ENTER"       =>  VARS::post("MOVE_ENTER")
                            );   //受験番号の開始
        $this->applicantdiv   = 1;
        $this->appHold        = VARS::post("APP_HOLD");
        $this->testdiv        = strlen(VARS::post("TESTDIV")) ? VARS::post("TESTDIV") : VARS::post("HID_TESTDIV");
        $this->schoolcd       = SCHOOLCD;
        $this->schoolkind     = SCHOOLKIND;
        $this->exam_type      = "1";
        $this->hid_receptno   = VARS::post("HID_RECEPTNO");//"RECEPTNO"-"COUNT"で取得
        $this->sorttype       = VARS::post("SORT_TYPE");

        //受験番号ごとに配置
        $this->clubcd = array();
        $this->judgekind = array();
        $this->suborder = array();
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $this->judgekind[$receptno_array[1]]  = VARS::post("JUDGE_KIND-".$receptno_array[1]);
            $this->clubcd[$receptno_array[1]]  = VARS::post("CLUBCD-".$receptno_array[1]);
            $this->suborder[$receptno_array[1]]  = VARS::post("SUB_ORDER-".$receptno_array[1]);
        }
    }

    //受験番号をセット
    public function &getMainModel()
    {

        if ($this->cmd == "reset") {
            $this->cmd = "read";
        }
    }

    // 編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        $receptno_array = array();
        $tmp = explode(",", $this->hid_receptno);

        //DB接続
        $db = Query::dbCheckOut();

        $codeinfo_jdgkind = $this->getNameMstL025Cd($db);
        $codeinfo_suborder = $this->getDecisionMstCd($db);

        //DB切断
        Query::dbCheckIn($db);

        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            if (strlen($this->judgekind[$receptno_array[1]]) > 1) {
                $this->setWarning("MSG914", "特待区分は半角1文字までです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if ($this->judgekind[$receptno_array[1]] !== "") {
                $findcd_flg = $this->chkMstList($codeinfo_jdgkind['codelist'], $this->judgekind[$receptno_array[1]]);
                if ($findcd_flg === false) {
                    $this->setWarning("MSG914", "特待区分は半角の". $codeinfo_jdgkind['codeliststr'] ."のみ有効です。(受験番号:".$receptno_array[0].")");
                    return false;
                }
            }
            if (strlen($this->suborder[$receptno_array[1]]) > 1) {
                $this->setWarning("MSG914", "内部判定は半角1文字までです。(受験番号:".$receptno_array[0].")");
                return false;
            }
            if ($this->suborder[$receptno_array[1]] !== "") {
                $findcd_flg = $this->chkMstList($codeinfo_suborder['codelist'], $this->suborder[$receptno_array[1]]);
                if ($findcd_flg === false) {
                    $this->setWarning("MSG914", "内部判定は半角の" . $codeinfo_suborder['codeliststr'] . "のみ有効です。(受験番号:".$receptno_array[0].")");
                    return false;
                }
            }
        }

        $tmp = explode(",", $this->hid_receptno);
        $receptno_array = array();
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);

            $searchCnt = knjl017dQuery::chkDataQuery_BaseDet($this, $receptno_array);
            if ($searchCnt === "0") {
                knjl017dQuery::insertQuery_BaseDet($this, $receptno_array);
            } else {
                knjl017dQuery::UpdateQuery_BaseDet($this, $receptno_array);
            }
            knjl017dQuery::UpdateQuery_Base($this, $receptno_array);
        }
        $this->setMessage("MSG201");
    }

    public function getNameMstL025Cd($db)
    {
        $query = knjl017dQuery::getNameMstCdQuery('L025');
        $result = $db->query($query);

        $codelist = array();
        $codeliststr = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($codeliststr !== "") {
                $codeliststr .= ",";
            }
            $codeliststr .= $row['VALUE'];
            $codelist[] = $row['VALUE'];
        }
        return array('codelist'=>$codelist, 'codeliststr'=>$codeliststr);
    }

    public function getDecisionMstCd($db)
    {
        $query = knjl017dQuery::getDecisionMstCdQuery();
        $result = $db->query($query);

        $codelist = array();
        $codeliststr = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($codeliststr !== "") {
                $codeliststr .= ",";
            }
            $codeliststr .= $row['VALUE'];
            $codelist[] = $row['VALUE'];
        }
        return array('codelist'=>$codelist, 'codeliststr'=>$codeliststr);
    }

    public function chkMstList($list, $chkval)
    {
        $findcd_flg = false;
        foreach ($list as $rowcd) {
            if ($rowcd == $chkval) {
                $findcd_flg = true;
            }
        }
        return $findcd_flg;
    }

    public function sortSetting($setsortcol)
    {
        if ($setsortcol == "sort_j_lang") {
            $this->sortRule("J_LANG_DESC", "J_LANG_ASC");
        } elseif ($setsortcol == "sort_math") {
            $this->sortRule("MATH_DESC", "MATH_ASC");
        } elseif ($setsortcol == "sort_e_lang") {
            $this->sortRule("E_LANG_DESC", "E_LANG_ASC");
        } elseif ($setsortcol == "sort_total") {
            $this->sortRule("TOTAL_DESC", "TOTAL_ASC");
        } else {
            $this->sorttype = "";
        }
    }

    public function sortRule($sort1, $sort2)
    {
        $now_sorttype = $this->sorttype;
        if ($now_sorttype === $sort2) {
            $this->sorttype = "";
        } elseif ($now_sorttype === $sort1) {
            $this->sorttype = $sort2;
        } else {
            $this->sorttype = $sort1;
        }
    }

    public function getDecisionInfoStr($db, $spflg = true)
    {
        $result = $this->getDecisionMstCd($db);
        $spstr = " ";
        if ($spflg) {
            $spstr = "&nbsp";
        }
        $retstr = "合計人数：".$db->getOne(knjl017dQuery::getJudgeAllCntQuery($this, "")) . "人" . $spstr . $spstr ."内部判定";
        foreach ($result["codelist"] as $row) {
            $retstr .= $spstr . $row ."：" . $db->getOne(knjl017dQuery::getJudgeAllCntQuery($this, $row)) . "人";
        }
        return $retstr;
    }

    public function &downloadCsvFile()
    {
        if ($this->testdiv === "" || $this->testdiv === 0) {
            $this->setWarning("MSG914", "受験種別");
            return false;
        }

        //DB接続
        $db = Query::dbCheckOut();

        $chkcnt = $this->chkCsvData($db);
        if ($chkcnt > 0) {
            $contents = $this->makeCsvData($db, $this);
            if ($contents !== "") {
                $fsubname = preg_split("/,/", $contents, 2);
                $filename = $fsubname[0] .".csv";
                common::downloadContents($contents, $filename);
            } else {
                $this->setMessage("MSG310");
            }
        } else {
            $this->setMessage("MSG303");
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    public function chkCsvData($db)
    {
        $query = knjl017dQuery::SelectQuery($this, "COUNT");
        $result = $db->getOne($query);

        return $result;
    }


    public function makeCsvData($db)
    {
        $retval = "";

        //受験種別名称取得
        $query = knjl017dQuery::getNameMst("L004", $this->ObjYear, false, $this->testdiv);
        $result = $db->query($query);

        $testdivstr = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivstr = $row['LABEL'];
        }
        //取得チェック
        if ($testdivstr === "") {
            return $retval;
        }

        //タイトル,日付
        $changeYear = $this->ObjYear;
        $eyear = common::Calc_Wareki($changeYear, "4", "1");
        $retval .= $eyear . "度" . $testdivstr . "内部選考資料,日付:" . CTRL_DATE . "\r\n";
        //項目名称
        $retval .= "受験番号,氏名,専併区分,志望累計,内申合計(5科目),内申合計(9科目),欠席日数(1年),欠席日数(2年),欠席日数(3年),入試得点(国語),入試得点(数学),入試得点(英語),入試得点(合計),特待区分,クラブ名,内部判定" . "\r\n";

        //DB接続
        $db = Query::dbCheckOut();

        $decisioninfo = $this->getDecisionInfoStr($db, false);

        $query = knjl017dQuery::SelectQuery($this, $this->sorttype);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = "";
            $data  = $row['EXAMNO'] . ",";
            $data .= $row['NAME'] . ",";
            $data .= $row['SHDIV'] . ",";
            $data .= $row['DESIREDIV'] . ",";
            $data .= $row['SUBTOTAL5'] . ",";
            $data .= $row['SUBTOTAL_ALL'] . ",";
            $data .= $row['ABSENCE_DAYS'] . ",";
            $data .= $row['ABSENCE_DAYS2'] . ",";
            $data .= $row['ABSENCE_DAYS3'] . ",";
            $data .= $row['SCORE1'] . ",";
            $data .= $row['SCORE2'] . ",";
            $data .= $row['SCORE3'] . ",";
            $data .= $row['SCORE_TOTAL'] . ",";
            $data .= $row['JUDGE_KIND'] . ",";
            $data .= $row['CLUBNAME'] . ",";
            $data .= $row['SUB_ORDER'];

            $retval .= $data . "\r\n";
        }
        //DB切断
        Query::dbCheckIn($db);

        $retval .= $decisioninfo . "\r\n";
        return $retval;
    }
}
