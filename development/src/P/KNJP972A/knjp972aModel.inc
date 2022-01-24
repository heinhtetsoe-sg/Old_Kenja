<?php

require_once('for_php7.php');
class knjp972aModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;
    public $select_data = array();
    public $select_opt  = array();    //セレクトデータ
    public $selectleft;

    public function knjp972aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->year = CTRL_YEAR;
        $this->schoolcd = SCHOOLCD;

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "GRADE"            =>  VARS::post("GRADE"),
                             "INCOME_L_CD"      =>  VARS::post("INCOME_L_CD"),
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND"),
                             "DBNAME"           =>  VARS::post("DBNAME")
                            );
        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectleft = VARS::post("selectleft");

        //収入細目の最小表示列数(この列数に満たなければパディング)
        $this->minInComeSaimokuNum = 4;
        //収入計内訳の最小表示列数(この列数に満たなければパディング)
        $this->minInComeUtiwakeNum = 2;

        //収入細目数 - 4　が入る
        $this->addDummyNum = 0;
        $this->addDummyNum2 = 0;

        //初期値
        if ($cmd == "") {
            $this->field["LIMIT_DATE"] = ($this->field["LIMIT_DATE"]) ? $this->field["LIMIT_DATE"] : str_replace("-", "/", CTRL_DATE);
        }
        if ($this->cmd != 'read') {
            $this->select_opt = array();
        }
        $this->getPropertiesAll();  //プロパティファイルの読込み
    }

    /**
     * CSV出力
     */
    public function getCsvOutputModel()
    {

        //DB接続
        $db = Query::dbCheckOut();

        $contents  = "";
        $header_data  = false;

        //端数データ取得
        $hasuuQuery = knjp972aQuery::getHasuu($this);
        $hasuuList = $db->query($hasuuQuery);
        while ($hasuuRow = $hasuuList->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hasuu[$hasuuRow["INCOME_L_CD"].$hasuuRow["OUTGO_APPROVAL"].$hasuuRow["SORT_DATE"].$hasuuRow["REQUEST_NO"].$hasuuRow["LINE_NO"]] = $hasuuRow["HASUU"];
        }

        $lListQuery = knjp972aQuery::getLList($this);
        $lList = $db->query($lListQuery);
        while ($lListRow = $lList->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lListRow = $this->commaConv($lListRow);

            $incomeLcd   = $lListRow["INCOME_L_CD"];
            $incomeLName = $lListRow["LEVY_L_NAME"];

            //項目名取得
            $levyMmst = array();
            $levyMQuery = knjp972aQuery::getLevyMMst($this);
            $result = $db->query($levyMQuery);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $levyMmst[$row["LEVY_L_CD"].$row["LEVY_M_CD"]] = $row["LEVY_M_NAME"];
            }
            //細目名取得
            $levySmst = array();
            $levySQuery = knjp972aQuery::getLevySMst($this);
            $result = $db->query($levySQuery);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $levySmst[$row["LEVY_L_CD"].$row["LEVY_M_CD"].$row["LEVY_S_CD"]] = $row["LEVY_S_NAME"];
            }
            $p009Mst = array();
            $query = knjp972aQuery::getVNameP009Mst($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $p009Mst[$row["LMCD"]] = $row["SYMBOL"];
            }

            //クラス取得
            $regdHQuery = knjp972aQuery::getSchregHdat($this);
            $hdatRow = $this->getRowArray($db, $regdHQuery, array("GRADE", "HR_CLASS"), "%s-%s");

            foreach ($this->select_data["selectdata"] as $gradeHrClass) {

                //クラス毎収入データ
                $schIncomeSql = knjp972aQuery::getIncomeSql($this, $incomeLcd, $gradeHrClass);
                $hrIncomeRow  = $this->getRowArray($db, $schIncomeSql, array("CSV_HEADER_KEY"));

                //クラス毎支出データ
                $hrOutgoSql = knjp972aQuery::getOutgoSql($this, $incomeLcd, $gradeHrClass);
                $hrOutgoRow = $this->getRowArray($db, $hrOutgoSql, array("CSV_HEADER_KEY"), "");


                //**********ヘッダ作成***************//
                $title = array();
                $titleContents = "";

                //ヘッダ1,2行目
                //年度（和暦）
                $baseDate = (CTRL_YEAR == "2019") ? "05/01" : "04/01";  //令和の初年度への対応
                $year = common::DateConv1(CTRL_YEAR."/".$baseDate, 10);
                $titleContents .= $year."　収入-支出細目一覧（{$incomeLName}）\n";

                $title["HR_NAME"]       = $hdatRow[$gradeHrClass]["HR_NAME"];
                $title["TEACHER_NAME"]  = $hdatRow[$gradeHrClass]["STAFFNAME"];

                //現在時刻 西暦年月日
                $date = new DateTime('now');
                $dateStr = common::DateConv1($date->format('Y/m/d'), 0);
                $timeStr = $date->format('H時 i分');
                $title["DATE"] =  $dateStr." ".$timeStr;

                $titleContents .= implode(",", $title)."\n";
                $contents .= $titleContents;

                $header = array();

                $schHeader = array();
                $schHeader["ATTENDNO"] = "番号";
                if ($this->Properties["useDispSchregno_KNJP972"] == "1") {
                    $schHeader["SCHREGNO"] = "学籍番号";
                }
                $schHeader["NAME"]     = "氏名";

                //**収入ヘッダ作成**/
                $incomeHeader = array();
                //SQL取得結果における収入明細・内訳・収入計それぞれの項目数
                $incomeDivRecordCnt = array_count_values(array_column($hrIncomeRow, "INCOME_DIV"));
                //CSVレイアウト上における最低限必要な収入明細(4列)・内訳(2列)・収入計(1列)の項目数を設定
                $incomeDivMinCnt    = array();
                $incomeDivMinCnt["INCOME_MEISAI"]   = 4;
                $incomeDivMinCnt["INCOME_UTIWAKE"]  = 2;
                $incomeDivMinCnt["INCOME_TOTAL"]    = 1;

                //各項目が最小表示数以下の場合に挿入するダミー列数を設定
                $incomeDivDummyCnt = array();
                foreach ($incomeDivMinCnt as $incomeDiv => $minCnt) {
                    $currentCnt = $incomeDivRecordCnt[$incomeDiv];
                    $incomeDivDummyCnt[$incomeDiv] = max($minCnt - $currentCnt, 0);
                }

                $incomeUtiwakeArray = array();
                foreach ($hrIncomeRow as $headerKey => $hrIncomeData) {
                    $incomeLMCd      = $hrIncomeData["INCOME_L_CD"].$hrIncomeData["INCOME_M_CD"];
                    $incomeMName     = $levyMmst[$incomeLMCd];
                    $incomeDiv       = $hrIncomeData["INCOME_DIV"];
                    $incomeDivNo     = $hrIncomeData["INCOME_DIV_NO"];
                    $inComeDivLastNo = $incomeDivRecordCnt[$incomeDiv];

                    if ($incomeDiv == "INCOME_MEISAI") {
                        $collectDiv         = $hrIncomeData["COLLECT_DIV"];

                        $incomeHeader[$headerKey] = ($collectDiv == "2") ? $incomeMName."(繰越)" : $incomeMName;
                    } elseif ($incomeDiv == "INCOME_UTIWAKE") {
                        $incomeUtiwakeArray[$incomeLMCd]["M_NAME"]     = $incomeMName; //差額ヘッダ作成用に収入内訳の項目を取得しておく
                        $incomeUtiwakeArray[$incomeLMCd]["HEAD_KEY"]   = $headerKey;   //差額データに収入内訳の項目のCSVヘッダのキーを取得しておく

                        $incomeHeader[$headerKey] = "収入計({$incomeMName})";
                    } elseif ($incomeDiv == "INCOME_TOTAL") {
                        $incomeHeader[$headerKey] = "収入計";
                    }

                    //最低列数以上になるよう空列を挿入(明細は4列、内訳は2列)
                    if ($incomeDivNo == $inComeDivLastNo) {
                        for ($i = 1; $i <= $incomeDivDummyCnt[$incomeDiv]; $i++) {
                            $dummyIncomeDivNo = $inComeDivLastNo + $i;
                            $dummyHeaderKey   = $incomeDiv.$dummyIncomeDivNo;
                            $incomeHeader[$dummyHeaderKey] = "";
                        }
                    }
                }

                //**支出ヘッダ作成**/
                $outgoHeader = array();
                //SQL取得結果における収入明細・内訳・収入計それぞれの項目数
                $outgoDivRecordCnt = array_count_values(array_column($hrOutgoRow, "OUTGO_DIV"));
                //CSVレイアウト上における最低限必要な収入明細(4列)・内訳(2列)・収入計(1列)の項目数を設定
                $outgoDivMinCnt = array();
                $outgoDivMinCnt["OUTGO_MEISAI"]   = max($outgoDivRecordCnt["OUTGO_MEISAI"], 1); //明細は取得した項目数でそのまま表示(明細が0なら空列を出す)
                $outgoDivMinCnt["OUTGO_UTIWAKE"]  = 2;
                $outgoDivMinCnt["OUTGO_TOTAL"]    = 1;

                //各項目が最小表示数以下の場合に挿入するダミー列数を設定
                $outgoDivDummyCnt = array();
                foreach ($outgoDivMinCnt as $outgoDiv => $minCnt) {
                    $currentCnt = $outgoDivRecordCnt[$outgoDiv];
                    $outgoDivDummyCnt[$outgoDiv] = max($minCnt - $currentCnt, 0);
                }

                $outgoUtiwakeArray = array();
                foreach ($hrOutgoRow as $headerKey => $hrOutgoData) {
                    $outgoDiv       = $hrOutgoData["OUTGO_DIV"];
                    $outgoDivNo     = $hrOutgoData["OUTGO_DIV_NO"];
                    $outgoDivLastNo = $outgoDivRecordCnt[$outgoDiv];
                    $incomeLMCd     = $hrOutgoData["INCOME_L_CD"].$hrOutgoData["INCOME_M_CD"];
                    $incomeMName = $levyMmst[$incomeLMCd];

                    if ($outgoDiv == "OUTGO_MEISAI") {
                        $outgoLMSCd    = $hrOutgoData["OUTGO_L_CD"].$hrOutgoData["OUTGO_M_CD"].$hrOutgoData["OUTGO_S_CD"];

                        $outgoApproval = $hrOutgoData["OUTGO_APPROVAL"];
                        $requestNo     = $hrOutgoData["REQUEST_NO"];
                        $headerStr     = ($outgoApproval == "2") ? "*" : "";
                        $headerSymbol  = $p009Mst[$incomeLMCd] ?: "";
                        if ($requestNo == "CARRY_OVER") {
                            $outgoSName    = "繰越({$incomeMName})";
                        } else {
                            $outgoSName    = $levySmst[$outgoLMSCd];
                        }

                        $outgoHeader[$headerKey] = $headerSymbol.$headerStr.$outgoSName;
                    } elseif ($outgoDiv == "OUTGO_UTIWAKE") {
                        $outgoUtiwakeArray[$incomeLMCd]["M_NAME"]     = $incomeMName; //差額ヘッダ作成用に支出内訳の項目を取得しておく
                        $outgoUtiwakeArray[$incomeLMCd]["HEAD_KEY"]   = $headerKey;   //差額データに支出内訳の項目のCSVヘッダのキーを取得しておく

                        $outgoHeader[$headerKey] = "支出計({$incomeMName})";
                    } elseif ($outgoDiv == "OUTGO_TOTAL") {
                        $outgoHeader[$headerKey] = "支出計";
                    }

                    //最低列数以上になるよう空列を挿入(明細は4列、内訳は2列)
                    if ($outgoDivNo == $outgoDivLastNo) {
                        for ($i = 1; $i <= $outgoDivDummyCnt[$outgoDiv]; $i++) {
                            $dummyoutgoDivNo = $outgoDivLastNo + $i;
                            $dummyHeaderKey  = $outgoDiv.$dummyoutgoDivNo;
                            $outgoHeader[$dummyHeaderKey] = "";
                        }
                    }
                }
                //**差額ヘッダ作成**/
                $sagakuHeader = array();
                $sagakuHeader["SAGAKU_TOTAL1"] = "差額";
                $sagakuDivNo = 1;
                foreach ($incomeUtiwakeArray as $incomeLMCd => $incomeLMVal) {
                    $sagakuDiv = "SAGAKU_UTIWAKE".$sagakuDivNo;
                    $incomeMName = $incomeLMVal["M_NAME"];
                    $sagakuHeader[$sagakuDiv] = "差額({$incomeMName})";
                    $sagakuDivNo++;
                }

                //ヘッダ3行目
                $contents .= sprintf("項目%s", str_repeat(",", count($schHeader)));
                $contents .= sprintf("収入明細%s", str_repeat(",", count($incomeHeader)));
                $contents .= sprintf("支出明細%s", str_repeat(",", count($outgoHeader)));
                $contents .= "\n";

                //ヘッダ4行目
                $header    = array_merge($header, $schHeader, $incomeHeader, $outgoHeader, $sagakuHeader);
                $contents .= implode(",", $header);
                $contents .= "\n";

                //ヘッダ5行目
                $contents .= str_repeat(",", count($schHeader));
                $contents .= str_repeat(",", count($incomeHeader));
                $sep = "";
                foreach ($outgoHeader as $headerKey => $headerLabel) {
                    if ($hrOutgoRow[$headerKey]["REQUEST_NO"] == "CARRY_OVER") {
                        //繰越の場合は日付を表示しない ※帳票の方では明示的に除外していないが、仕様は繰越日付は非表示にするのが正しい
                        continue;
                    }
                    $sortDate  = ($hrOutgoRow[$headerKey]["OUTGO_APPROVAL"] == "1") ? $hrOutgoRow[$headerKey]["SORT_DATE"] : "";
                    $contents .= $sep.$sortDate;
                    $sep = ",";
                }
                $contents .= "\n";

                //クラス生徒リスト
                $schregSqlQuery = knjp972aQuery::getSchregSql($this, $incomeLcd, $gradeHrClass);
                $schregRow = $this->getRowArray($db, $schregSqlQuery, array("SCHREGNO"));

                //学籍番号毎収入データ
                $schIncomeSql = knjp972aQuery::getIncomeSql($this, $incomeLcd, $gradeHrClass, "SCHREGNO");
                $schIncomeRow = $this->getRowArray($db, $schIncomeSql, array("SCHREGNO", "CSV_HEADER_KEY"), "%s-%s");

                //学籍番号毎支出データ
                $schOutgoSql = knjp972aQuery::getOutgoSql($this, $incomeLcd, $gradeHrClass, "SCHREGNO");
                $schOutgoRow = $this->getRowArray($db, $schOutgoSql, array("SCHREGNO", "CSV_HEADER_KEY"), "%s-%s");

                $sagakuMoneyArray = array();
                foreach ($schregRow as $schregno => $schregData) {
                    //学籍情報
                    $sep = "";
                    foreach ($schHeader as $headerKey => $headerLabel) {
                        $contents .= $sep.$schregData[$headerKey];
                        $sep = ",";
                    }

                    //収入
                    foreach ($incomeHeader as $headerKey => $headerLabel) {
                        $key = $schregno."-".$headerKey;
                        $contents .= $sep.$schIncomeRow[$key]["INCOME_MONEY"];
                        $sep = ",";
                    }
                    //支出
                    foreach ($outgoHeader as $headerKey => $headerLabel) {
                        $key = $schregno."-".$headerKey;
                        $contents .= $sep.$schOutgoRow[$key]["OUTGO_MONEY"];
                        $sep = ",";
                    }
                    //差額
                    $keyIn  = $schregno."-INCOME_TOTAL1";
                    $keyOut = $schregno."-OUTGO_TOTAL1";
                    $sagakuTotal = $schIncomeRow[$keyIn]["INCOME_MONEY"] - $schOutgoRow[$keyOut]["OUTGO_MONEY"];

                    $sagakuMoneyArray["SAGAKU_TOTAL1"][$schregno] = $sagakuTotal;

                    $sagakuDivNo = 1;
                    foreach ($incomeUtiwakeArray as $incomeLMCd => $incomeLMVal) {
                        $headerKey = "SAGAKU_UTIWAKE".$sagakuDivNo;
                        $sagakuHeaderKeyIn = $incomeLMVal["HEAD_KEY"];
                        $sagakuHeaderKeyOut = $outgoUtiwakeArray[$incomeLMCd]["HEAD_KEY"];

                        $keyIn  = $schregno."-".$sagakuHeaderKeyIn;
                        $keyOut = $schregno."-".$sagakuHeaderKeyOut;
                        $sagakuMoney = $schIncomeRow[$keyIn]["INCOME_MONEY"] - $schOutgoRow[$keyOut]["OUTGO_MONEY"];

                        $sagakuMoneyArray[$headerKey][$schregno] = $sagakuMoney;
                        $sagakuDivNo++;
                    }

                    foreach ($sagakuHeader as $headerKey => $headerLabel) {
                        $contents .= $sep.$sagakuMoneyArray[$headerKey][$schregno];
                        $sep = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;   //出力フラグ
                }

                //下段クラス計(人数・合計・端数)
                $schCntContents = "";
                $totalContents = "";
                $hasuuContents = "";

                //収入
                $sep = "";
                foreach ($incomeHeader as $headerKey => $headerLabel) {
                    $schCntContents .= $sep.$hrIncomeRow[$headerKey]["SCH_CNT"];
                    $totalContents .= $sep.$hrIncomeRow[$headerKey]["INCOME_MONEY"];
                    $sep = ",";
                }
                //支出
                foreach ($outgoHeader as $headerKey => $headerLabel) {
                    $schCntContents .= $sep.$hrOutgoRow[$headerKey]["SCH_CNT"];
                    $totalContents .= $sep.$hrOutgoRow[$headerKey]["OUTGO_MONEY"];
                    $sep = ",";
                }
                //差額
                foreach ($sagakuHeader as $headerKey => $headerLabel) {
                    $cnt = count(array_filter($sagakuMoneyArray[$headerKey], function ($val) {
                        return $val != 0;
                    }));
                    $total  = array_sum($sagakuMoneyArray[$headerKey]);
                    $schCntContents .= $sep.$cnt;
                    $totalContents  .= $sep.$total;
                    $sep = ",";
                }
                //端数
                $hasuuTotal = 0;
                $hasuuUtiwake = array();
                $sep = "";
                foreach ($outgoHeader as $headerKey => $headerLabel) {
                    if (!isset($hrOutgoRow[$headerKey])) {
                        continue;
                    }
                    $requestNo = $hrOutgoRow[$headerKey]["REQUEST_NO"];
                    $outgoKey  = $hrOutgoRow[$headerKey]["OUTGO_KEY"];
                    if ($requestNo == "CARRY_OVER" || $requestNo == "HENKIN") {
                        $hasuuMoney = 0;
                    } else {
                        $hasuuMoney = intval($hasuu[$outgoKey]);
                    }
                    $hasuuContents .= $sep.$hasuuMoney;
                    $sep = ",";
                }

                $contents .= sprintf("%s人数,", str_repeat(",", count($schHeader) - 1));
                $contents .= $schCntContents."\n";
                $contents .= sprintf("%s合計,", str_repeat(",", count($schHeader) - 1));
                $contents .= $totalContents."\n";
                $contents .= sprintf("%s端数", str_repeat(",", count($schHeader) - 1));
                $contents .= sprintf("%s,", str_repeat(",", count($incomeHeader)));
                $contents .= $hasuuContents."\n";

                $contents .= "\n\n";
            }
        }
        Query::dbCheckIn($db);

        //CSV出力
        if ($header_data) {
            $couse = "収入-支出細目一覧データ";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //クエリ結果のレコードを連想配列にセットする
    public function getRowArray($db, $query, $fieldLabelArray, $keyFormat = "")
    {
        $rtnArray = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fieldValArray = array_map(function ($label) use ($row) {
                return $row[$label];
            }, $fieldLabelArray);

            $key = "";
            if ($keyFormat != "") {
                $key = vsprintf($keyFormat, $fieldValArray);
            } else {
                $key = implode($fieldValArray);
            }
            $rtnArray[$key] = $row;
        }
        return $rtnArray;
    }

    /**
     * データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
     */
    public function commaConv($row)
    {
        $row = str_replace(",", ".", $row);
        $row = str_replace("\r\n", "↓", $row);
        $row = str_replace("\r", "↓", $row);
        $row = str_replace("\n", "↓", $row);

        return $row;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
