<?php

require_once('for_php7.php');
class knjp972Model extends Model {
    var $field = array();
    var $cmd;
    var $control;
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    function knjp972Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "INCOME_L_CD"      =>  VARS::post("INCOME_L_CD"),
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectleft = VARS::post("selectleft");

        //収入細目数 - 4　が入る
        $this->addDummyNum = 0;

        //初期値
        if ($cmd == "") {
            $this->field["LIMIT_DATE"] = ($this->field["LIMIT_DATE"]) ? $this->field["LIMIT_DATE"] : str_replace("-","/",CTRL_DATE);
        }
        if ($this->cmd != 'read') {
            $this->select_opt = array();
        }
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    /**
     * CSV出力
     */
    function getCsvOutputModel() {

        //DB接続
        $db = Query::dbCheckOut();

        $contents  = "";
        $header_data  = false;

        //端数データ取得
        $hasuuQuery = knjp972Query::getHasuu($this);
        $hasuuList = $db->query($hasuuQuery);
        while ($hasuuRow = $hasuuList->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hasuu[$hasuuRow["INCOME_L_CD"].$hasuuRow["OUTGO_APPROVAL"].$hasuuRow["SORT_DATE"].$hasuuRow["REQUEST_NO"].$hasuuRow["LINE_NO"]] = $hasuuRow["HASUU"];
        }

        $lListQuery = knjp972Query::getLList($this);
        $lList = $db->query($lListQuery);
        while ($lListRow = $lList->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lListRow = $this->commaConv($lListRow);

            $beforeHr = "";

            //生徒情報取得
            $schregSqlQuery = knjp972Query::getSchregSql($this, $lListRow);

            //クラス毎の収入をあらかじめ取得
            $income = $this->getSyunyumeisai($db, $lListRow, $schregSqlQuery);

            //クラス毎の支出、繰越、返金をあらかじめ取得
            $outgo = $this->getSisyutumeisai($db, $lListRow, $schregSqlQuery);

            $schreg = $db->query($schregSqlQuery);
            while ($schregRow = $schreg->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schregRow = $this->commaConv($schregRow);

                $header_data = true;   //出力フラグ

                //クラス毎の明細を作成するため、クラス単位でタイトル、ヘッダを設定
                $hr = $schregRow["GRADE"].$schregRow["HR_CLASS"];
                if ($beforeHr == "" || $beforeHr != $hr) {
                    if ($beforeHr != "") {
                        $contents .= $this->setGedan($incomeCnt, $outgoCnt, $totalIncomeCnt, $totalOutgoCnt, $sagakuCnt, $incomeGedan, $totalIncomeGedan, $outgoGedan, $totalOutgoGedan, $sagakuGedan, $hasuuGedan, $totalHasuuGedan);
                    }
                    $incomeCnt = array();
                    $totalIncomeCnt = 0;
                    $outgoCnt = array();
                    $totalOutgoCnt = 0;
                    $sagakuCnt = 0;
                    $incomeGedan = array();
                    $totalIncomeGedan = 0;
                    $outgoGedan  = array();
                    $totalOutgoGedan = 0;
                    $sagakuGedan = 0;
                    $hasuuGedan = array();
                    $totalHasuuGedan = 0;
                    $setHasuu = array();

                    $title = array();
                    $titleContents = "";

                    //年度（和暦）
                    $baseDate = (CTRL_YEAR == "2019") ? "05/01" : "04/01";  //令和の初年度への対応
                    $year = common::DateConv1(CTRL_YEAR."/".$baseDate, 10);
                    $title["TITLE"] = $year."年度　収入-支出細目一覧（".$lListRow["LEVY_L_NAME"]."）";
                    $titleContents .= implode(",", $title)."\n";

                    $title = array();
                    $title["HR_NAME"] = $schregRow["HR_NAME"];
                    $title["TEACHER_NAME"] = $schregRow["STAFFNAME"];

                    //現在時刻 西暦年月日
                    $date = new DateTime('now');
                    $dateStr = common::DateConv1($date->format('Y/m/d'), 0);
                    $timeStr = $date->format('H時 i分');
                    $title["DATE"] =  $dateStr." ".$timeStr;

                    $titleContents .= implode(",", $title)."\n";
                    $contents .= $titleContents;

                    $headerContents = "";

                    //細目
                    $incomeSaimoku  = "";
                    $outgoSaimoku   = "";
                    $this->commas   = "";

                    if ($income[$hr]) {
                        ksort($income[$hr]);
                        $tmp = $this->addDummy($income[$hr]);
                        $incomeSaimoku .= implode(',', $tmp);
                    }
                    if ($outgo[$hr]) {
                        ksort($outgo[$hr]);
                        $outgoSaimoku   .= implode(',', $outgo[$hr]);
                    }

                    if (get_count($income[$hr]) > 4) {
                        $this->addDummyNum = get_count($income[$hr]) - 4;
                    }

                    $commas = implode(",", $this->addDummy(array(), $this->addDummyNum));
                    $headerPart1 = ($this->Properties["useDispSchregno_KNJP972"] == "1") ? "項目,,,収入明細,%s,支出明細" : "項目,,収入明細,%s,支出明細";
                    $headerContents1 = sprintf($headerPart1,$commas);
                    $headerPart2       = ($this->Properties["useDispSchregno_KNJP972"] == "1") ? "番号,学籍番号,氏名,%s,収入計,%s,支出計,差額" : "番号,氏名,%s,収入計,%s,支出計,差額";
                    $headerContents2 = sprintf($headerPart2, $incomeSaimoku, $outgoSaimoku);
                    $contents .= $headerContents1."\n".$headerContents2."\n";
                }

                $data = array();
                $data["ATTENDNO"] = $schregRow["ATTENDNO"];
                if ($this->Properties["useDispSchregno_KNJP972"] == "1") {
                    $data["SCHREGNO"] = $schregRow["SCHREGNO"];
                }
                $data["SCH_NAME"] = $schregRow["SCH_NAME"];

                //収入
                $totalIncome = 0;
                foreach ($income[$hr] as $incomeKey => $incomeValue) {
                    $query = knjp972Query::getIncomeSql($this, $lListRow, $schregRow);
                    $result = $db->query($query);
                    $hasIncome = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $row = $this->commaConv($row);

                        if ($row["COLLECT_DIV"] . $row["INCOME_L_CD"] . $row["INCOME_M_CD"] == $incomeKey) {
                            $data[$incomeKey] = $row["INCOME_MONEY"];
                            $totalIncome += $row["INCOME_MONEY"];

                            $incomeCnt[$incomeKey]++;
                            $incomeGedan[$incomeKey] += $row["INCOME_MONEY"];

                            $hasIncome = true;
                        }
                    }
                    if (!$hasIncome) {
                        $data[$incomeKey] = "";
                    }
                }

                //レイアウト調整
                for ($i = 0; $i < 4 -get_count($income[$hr]); $i++) {
                    $data[] = "";
                }

                //収入計
                $data["TOTAL_INCOME"] = $totalIncome;
                if ($totalIncome != 0) {
                    $totalIncomeCnt++;
                    $totalIncomeGedan += $schregRow["INCOME_MONEY"];
                }


                //支出
                $totalOutgo = 0;
                foreach ($outgo[$hr] as $outgoKey => $outgoValue) {
                    $query = knjp972Query::getOutgoSql($this, $lListRow, $schregRow);
                    $result = $db->query($query);
                    $hasOutgo = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $row = $this->commaConv($row);

                        //繰越
                        if ($row["REQUEST_NO"] == "CARRY_OVER" && $row["REQUEST_NO"] == $outgoKey) {
                            $data[$outgoKey] = $row["OUTGO_MONEY"];
                            $totalOutgo += $row["OUTGO_MONEY"];

                            $outgoCnt[$outgoKey]++;
                            $outgoGedan[$outgoKey] += $row["OUTGO_MONEY"];

                            $hasOutgo = true;
                        //返金
                        } else if ($row["REQUEST_NO"].$row["LINE_NO"] == "HENKIN1" && $row["REQUEST_NO"].$row["LINE_NO"] == $outgoKey) {
                            $data[$outgoKey] = $row["OUTGO_MONEY"];
                            $totalOutgo += $row["OUTGO_MONEY"];

                            $outgoCnt[$outgoKey]++;
                            $outgoGedan[$outgoKey] += $row["OUTGO_MONEY"];

                            $hasOutgo = true;
                        } else if ($row["OUTGO_APPROVAL"].$row["SORT_DATE"].$row["REQUEST_NO"].$row["LINE_NO"] == $outgoKey) {
                            $data[$outgoKey] = $row["OUTGO_MONEY"];
                            $totalOutgo += $row["OUTGO_MONEY"];

                            $outgoCnt[$outgoKey]++;
                            $outgoGedan[$outgoKey] += $row["OUTGO_MONEY"];

                            $hasOutgo = true;
                        }
                    }
                    if (!$hasOutgo) {
                        $data[$outgoKey] = "";
                    }

                    //端数を設定
                    $hasuuKey = $lListRow["INCOME_L_CD"].$outgoKey;
                    if (!$setHasuu[$hasuuKey]) {
                        if ($outgoKey == "CARRY_OVER" || $outgoKey == "HENKIN1") {
                            $hasuuGedan[$hasuuKey] = "";
                        } else if ($hasuu[$hasuuKey]) {
                            $hasuuGedan[$hasuuKey] = $hasuu[$hasuuKey];
                            $totalHasuuGedan += $hasuu[$hasuuKey];
                            $setHasuu[$hasuuKey] = true;
                        } else {
                            $hasuuGedan[$hasuuKey] = 0;
                        }
                    }
                }

                //支出計
                $data["TOTAL_OUTGO"] = $totalOutgo;
                if ($totalOutgo != 0) {
                    $totalOutgoCnt++;
                    $totalOutgoGedan += $totalOutgo;
                }

                //差額
                $data["SAGAKU"] = $schregRow["INCOME_MONEY"] - $totalOutgo;
                if ($data["SAGAKU"] != 0) {
                    $sagakuCnt++;
                    $sagakuGedan += $data["SAGAKU"];
                }

                $dataContents = implode(",", $data)."\n";
                $contents .= $dataContents;

                $beforeHr = $hr;
            }
            $contents .= $this->setGedan($incomeCnt, $outgoCnt, $totalIncomeCnt, $totalOutgoCnt, $sagakuCnt, $incomeGedan, $totalIncomeGedan, $outgoGedan, $totalOutgoGedan, $sagakuGedan, $hasuuGedan, $totalHasuuGedan);
        }

        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $couse = "収入-支出細目一覧データ";

            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
     */
    function commaConv($row) {
        $row = str_replace(","   , "." , $row);
        $row = str_replace("\r\n", "↓", $row);
        $row = str_replace("\r"  , "↓", $row);
        $row = str_replace("\n"  , "↓", $row);

        return $row;
    }

    function getSyunyumeisai($db, $lListRow, $schregSqlQuery) {

        $beforeHr = "";
        $schreg = $db->query($schregSqlQuery);
        while ($schregRow = $schreg->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hr = $schregRow["GRADE"].$schregRow["HR_CLASS"];

            //クラス単位の支出を配列として初期化
            if ($beforeHr != $hr) {
                $income[$hr] = array();
            }

            $query = knjp972Query::getIncomeSql($this, $lListRow, $schregRow);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $this->commaConv($row);

                $mName   = $row["LEVY_M_NAME"];
                $setName = $row["COLLECT_DIV"] == "2" ? $mName . "(繰越)" : $mName;

                $keySet = $row["COLLECT_DIV"] . $row["INCOME_L_CD"] . $row["INCOME_M_CD"];

                $income[$hr][$keySet] = $setName;
            }
            $beforeHr = $hr;
        }

        return $income;
    }

    function getSisyutumeisai($db, $lListRow, $schregSqlQuery) {

        $beforeHr = "";
        $schreg = $db->query($schregSqlQuery);
        while ($schregRow = $schreg->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hr = $schregRow["GRADE"].$schregRow["HR_CLASS"];

            //クラス単位の支出を配列として初期化
            if ($beforeHr != $hr) {
                $outgo[$hr] = array();
            }

            $query = knjp972Query::getOutgoSql($this, $lListRow, $schregRow);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = $this->commaConv($row);

                if ($row["REQUEST_NO"] == "CARRY_OVER") {
                    $keySet = $row["REQUEST_NO"];
                } else if ($row["REQUEST_NO"] == "HENKIN") {
                    $keySet = $row["REQUEST_NO"].$row["LINE_NO"];
                } else {
                    $keySet = $row["OUTGO_APPROVAL"].$row["SORT_DATE"].$row["REQUEST_NO"].$row["LINE_NO"];
                }
                $headerStr = ($row["OUTGO_APPROVAL"] == "2") ? "*" : "";
                $outgo[$hr][$keySet] = $headerStr.$row["LEVY_S_NAME"];
            }
            $beforeHr = $hr;
        }

        return $outgo;
    }

    /**
     * 下段を設定
     */
    function setGedan($incomeCnt, $outgoCnt, $totalIncomeCnt, $totalOutgoCnt, $sagakuCnt, $incomeGedan, $totalIncomeGedan, $outgoGedan, $totalOutgoGedan, $sagakuGedan, $hasuuGedan, $totalHasuuGedan) {
        $contents = "";

        $addCommas = ($this->Properties["useDispSchregno_KNJP972"] == "1") ? "," : ""; //学籍番号項目を追加した分右にずらす

        //人数
        $gedanCnt = array();
        $gedanCntIn = array();
        $gedanCntOut = array();
        ////INCOME
        ksort($incomeCnt);
        $incomeCnt = $this->addDummy($incomeCnt);
        $gedanCntIn["TOTAL_INCOME_CNT"] = $totalIncomeCnt;
        $gedanCntIn = array_merge($incomeCnt, $gedanCntIn);
        ////OUTGO
        ksort($outgoCnt);
        $gedanCntOut["TOTAL_OUTGO_CNT"] = $totalOutgoCnt;
        $gedanCntOut = array_merge($outgoCnt, $gedanCntOut);

        $gedanCnt = array_merge($gedanCntIn, $gedanCntOut);
        $gedanCnt["SAGAKU_CNT"] = $sagakuCnt;
        $contents .= $addCommas.",人数,".implode(",", $gedanCnt)."\n";


        //合計
        $gedanMoney = array();
        $gedanMoneyIn = array();
        $gedanMoneyOut = array();
        ////INCOME
        ksort($incomeGedan);
        $incomeGedan = $this->addDummy($incomeGedan);
        $gedanMoneyIn["TOTAL_INCOME_GEDAN"] = $totalIncomeGedan;
        $gedanMoneyIn = array_merge($incomeGedan, $gedanMoneyIn);
        ////OUTGO
        ksort($outgoGedan);
        $gedanMoneyOut["TOTAL_OUTGO_GEDAN"] = $totalOutgoGedan;
        $gedanMoneyOut = array_merge($outgoGedan, $gedanMoneyOut);

        $gedanMoney = array_merge($gedanMoneyIn, $gedanMoneyOut);
        $gedanMoney["SAGAKU_GEDAN"] = $sagakuGedan;
        $contents .= $addCommas.",合計,".implode(",", $gedanMoney)."\n";

        //端数
        $gedanHasuu = array();
        ksort($hasuuGedan);
        $gedanHasuu = array_merge($gedanHasuu, $hasuuGedan);
        $gedanHasuu["TOTAL_HASUU_GEDAN"] = $totalHasuuGedan;
        $commas = implode(",", $this->addDummy(array(), $this->addDummyNum));
        $contents .= $addCommas.",端数,,".$commas.",".implode(",", $gedanHasuu)."\n";

        $contents .= "\n\n";

        return $contents;
    }

    //配列に対してダミーの要素を追加する関数(CSVレイアウト用)
    function addDummy($inputArr, $plusNum = 0) {
        $addNum = get_count($inputArr) > 4 ? 0 : 4 -get_count($inputArr) ;
        for($i = 0; $i < $addNum + $plusNum; $i++) {
            $inputArr["DUMMY".$i]  = "";
        }
        return $inputArr;
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

}
?>
