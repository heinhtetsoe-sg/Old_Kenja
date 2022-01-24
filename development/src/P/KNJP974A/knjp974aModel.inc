<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp974aModel extends Model {
    var $field = array();
    var $cmd;

    /**
     * メンバ変数の初期化
     */
    function knjp974aModel() {
    }

    function init() {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "GRADE"        =>  VARS::post("GRADE"),
                             "MONTH"        =>  VARS::post("MONTH"),
                             "FROM_DATE"    =>  VARS::post("FROM_DATE"),
                             "TO_DATE"      =>  VARS::post("TO_DATE")
                     );

        $this->cmd = VARS::request("cmd");

        $this->schoolCd = sprintf("%012d", SCHOOLCD);

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $lf = "\n";
        $comma = ",";
        $contents = ""; //CSVデータ出力用
        $data = false;

        //和暦変換
        $wareki = "";
        $query = knjp974aQuery::getWareki($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gengou = $row["NAME1"];
            $ganNen = $row["NAMESPARE1"];
            $nen = $this->field["YEAR"] - $ganNen + 1;
            if ($nen == 1) {
                $wareki = $gengou."元年度";
            } else {
                $wareki = $gengou.$nen."年度";
            }
        }

        //学校名取得
        $schoolNameArr = array();
        $query = knjp974aQuery::getSchoolName($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schoolNameArr[$row["SCHOOL_KIND"]] = $row["SCHOOLNAME1"];
        }

        //科目マスタ
        $this->levyLmstArr = array();
        $query = knjp974aQuery::getLevyLmst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["SCHOOL_KIND"].$row["LEVY_L_CD"];
            $this->levyLmstArr[$setKey] = $row["LEVY_L_NAME"];
        }

        //項目マスタ
        $this->levyMmstArr = array();
        $query = knjp974aQuery::getLevyMmst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["SCHOOL_KIND"].$row["LEVY_L_CD"].$row["LEVY_M_CD"];
            $this->levyMmstArr[$setKey] = $row["LEVY_M_NAME"];
        }

        //細目マスタ
        $this->levySmstArr = array();
        $query = knjp974aQuery::getLevySmst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setKey = $row["SCHOOL_KIND"].$row["LEVY_L_CD"].$row["LEVY_M_CD"].$row["LEVY_S_CD"];
            $this->levySmstArr[$setKey] = $row["LEVY_S_NAME"];
        }

        //学年リスト取得
        $this->gradeArr = array();
        $query = knjp974aQuery::getGradeList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->gradeArr[$row["GRADE"]] = $row;
        }

        //収入、支出情報等取得
        $lListArr      = array();
        $incomeListArr = array();
        $outgoListArr  = array();
        $sepU  = "_";
        foreach ($this->gradeArr as $grade => $gArr) {
            $schoolKind = $gArr["SCHOOL_KIND"];

            //収入詳細
            $query = knjp974aQuery::getIncomeInfo($this, $schoolKind, $grade);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $lcd = $row["INCOME_L_CD"];

                $incomeListArr[$grade][$lcd][] = $row;

                $lListArr[$grade][$lcd] = $this->levyLmstArr[$schoolKind.$lcd];

                $data = true;
            }

            //支出詳細
            $query = knjp974aQuery::getOutgoInfo($this, $schoolKind, $grade);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $lcd = $row["INCOME_L_CD"];

                $outgoListArr[$grade][$lcd][] = $row;

                $lListArr[$grade][$lcd] = $this->levyLmstArr[$schoolKind.$lcd];

                $data = true;
            }

        }

        //データセット
        //学年毎
        foreach ($this->gradeArr as $grade => $gArr) {
            $schoolKind = $gArr["SCHOOL_KIND"];
            $gradeName1 = $gArr["GRADE_NAME1"];

            //科目毎
            foreach ($lListArr[$grade] as $incomLcd => $lName) {

                $maruIdx = 1;
                $taiouArr = array();

                //タイトル
                $contents .= $wareki.$schoolNameArr[$schoolKind]."（".$gradeName1."）".$lName."会計報告書".$lf;

                /********/
                /* 収入 */
                /********/
                $contents .= "（収入）,,,,（単位：円）".$lf;
                $lineCnt = 1;
                $totalIncomeMoney = 0;
                $totalIncMcdMoney = 0;
                $befIncLMcd = "";
                $incmName = "";
                foreach ((array)$incomeListArr[$grade][$incomLcd] as $incomeArr) {
                    $incomeLMcd  = $incomeArr["INCOME_L_CD"].$incomeArr["INCOME_M_CD"];
                    $koumokuName = $this->levyMmstArr[$schoolKind.$incomeLMcd];

                    //項目の小合計
                    if ($befIncLMcd != "" && $befIncLMcd != $incomeLMcd) {
                        $contents .= $this->mcdTotal("INCOME", $maruIdx, $befIncLMcd, $incmName, $totalIncMcdMoney, $taiouArr);

                        $totalIncMcdMoney = 0;
                        $lineCnt = 1;
                        $maruIdx++;
                    }

                    //連番
                    $contents .= $lineCnt.$comma;
                    //項目名
                    $firstSet = ($befIncLMcd != $incomeLMcd) ? $koumokuName: "";
                    $contents .= $firstSet.$comma;
                    //決済日付
                    list($y, $m, $d) = explode("-", $incomeArr["INCOME_DATE"]);
                    $setDate = $m."月".$d."日";
                    $contents .= $setDate.$comma;
                    //繰越データ(1:繰越、2:繰越以外)
                    if ($incomeArr["SORT"] == "1") {
                        //項目名
                        $contents .= $koumokuName." 前年度繰り越し".$comma;
                        //金額
                        $contents .= $incomeArr["INCOME_MONEY"];
                    } else {
                        //項目名
                        $contents .= $koumokuName." （{$incomeArr["INCOME_MONEY"]}円 × {$incomeArr["SCH_COUNT"]}人）".$comma;
                        //金額
                        $contents .= $incomeArr["INCOME_MONEY"] * $incomeArr["SCH_COUNT"];
                    }
                    $setPrice1 = $incomeArr["INCOME_MONEY"] * $incomeArr["SCH_COUNT"];
                    $contents .= $lf;

                    $befIncLMcd = $incomeLMcd;
                    $incmName   = $koumokuName;
                    $totalIncMcdMoney += $setPrice1;
                    $totalIncomeMoney += $setPrice1;
                    $lineCnt++;
                }

                //項目の小合計
                if ($befIncLMcd != "") {
                    $contents .= $this->mcdTotal("INCOME", $maruIdx, $incomeLMcd, $incmName, $totalIncMcdMoney, $taiouArr);
                    $maruIdx++;
                }

                //収入総合計
                $totalKey = $incomLcd."ALL";
                $contents .= $this->mcdTotal("INCOME", $maruIdx, $totalKey, $lName, $totalIncomeMoney, $taiouArr);
                $maruIdx++;

                /********/
                /* 支出 */
                /********/
                $contents .= "（支出）".$lf;
                $lineCnt = 1;
                $totalOutGoMoney = 0;
                $totalOutMcdMoney = 0;
                $befIncLMcd = "";
                $outmName   = "";
                foreach ((array)$outgoListArr[$grade][$incomLcd] as $outgoArr) {
                    $incomeLMcd  = $outgoArr["INCOME_L_CD"].$outgoArr["INCOME_M_CD"];
                    $outgoLMcd   = $outgoArr["OUTGO_L_CD"].$outgoArr["OUTGO_M_CD"];
                    $koumokuName = $this->levyMmstArr[$schoolKind.$outgoLMcd];
                    $saimokuName = $this->levySmstArr[$schoolKind.$outgoLMcd.$outgoArr["OUTGO_S_CD"]];

                    //項目の小合計
                    if ($befIncLMcd != "" && $befIncLMcd != $incomeLMcd) {
                        $contents .= $this->mcdTotal("OUTGO", $maruIdx, $befIncLMcd, $outmName, $totalOutMcdMoney, $taiouArr);

                        $totalOutMcdMoney = 0;
                        $lineCnt = 1;
                        $maruIdx++;
                    }

                    //連番
                    $contents .= $lineCnt.$comma;
                    //項目名
                    $firstSet = ($befIncLMcd != $incomeLMcd) ? $koumokuName: "";
                    $contents .= $firstSet.$comma;
                    //決済日付
                    list($y, $m, $d) = explode("-", $outgoArr["OUTGO_DATE"]);
                    $setDate = $m."月".$d."日";
                    $contents .= $setDate.$comma;
                    //細目名
                    $contents .= $saimokuName.$comma;
                    //金額
                    $contents .= $outgoArr["TOTAL_PRICE"];
                    $contents .= $lf;

                    $befIncLMcd = $incomeLMcd;
                    $outmName   = $koumokuName;
                    $totalOutMcdMoney += $outgoArr["TOTAL_PRICE"];
                    $totalOutGoMoney  += $outgoArr["TOTAL_PRICE"];
                    $lineCnt++;
                }

                //項目の小合計
                if ($befIncLMcd != "") {
                    $contents .= $this->mcdTotal("OUTGO", $maruIdx, $befIncLMcd, $outmName, $totalOutMcdMoney, $taiouArr);
                    $maruIdx++;
                }

                //支出総合計
                $contents .= $this->mcdTotal("OUTGO", $maruIdx, $totalKey, $lName, $totalOutGoMoney, $taiouArr);
                $maruIdx++;

                /**********/
                /* 最終欄 */
                /**********/
                $alphaCnt = 0;
                foreach ($taiouArr as $valArr) {
                    $contents .= ",,,";

                    //合計名称
                    $contents .= $valArr["INCOME_TITLE"]." ― ".$valArr["OUTGO_TITLE"].$comma;

                    //合計金額
                    $diffMoney = $valArr["INCOME_TOTAL"] - $valArr["OUTGO_TOTAL"];
                    $contents .= $diffMoney;

                    $contents .= $lf;
                    $alphaCnt++;
                }
                $contents .= $lf;
            }
        }

        if ($data) {
            //タイトル
            $couse = CTRL_YEAR."年度"."　会計報告書";
            $filename = sprintf("%s.csv", $couse);

            common::downloadContents($contents, $filename, "SJIS-win");

            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }//getDownloadModelの綴り

    //小計セット
    function mcdTotal($div, $idx, $mCd, $mName, $totalMoney, &$taiouArr) {
        $comma     = ",";
        $contents  = ",,,";
        $lf        = "\n";

        //項目名
        $sou = (substr($mCd, 2) == "ALL") ? "総": "";
        $subTitle = ($div == "INCOME") ? " （収入）": " （支出）";
        $setItem1 = $mName.$subTitle.$sou."合計";
        $contents .= $setItem1.$comma;

        //金額
        $contents .= $totalMoney.$comma;

        //改行
        $contents .= $lf.$lf;

        //最後の欄で使用
        if ($div == "INCOME") {
            $taiouArr[$mCd]["INCOME_TITLE"] = $setItem1;
            $taiouArr[$mCd]["INCOME_TOTAL"] = $totalMoney;
        } else {
            $taiouArr[$mCd]["OUTGO_TITLE"] = $setItem1;
            $taiouArr[$mCd]["OUTGO_TOTAL"] = $totalMoney;
        }

        return $contents;
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
