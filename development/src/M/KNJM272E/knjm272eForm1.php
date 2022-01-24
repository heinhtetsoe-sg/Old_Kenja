<?php

require_once('for_php7.php');


class knjm272eForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm272eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjm272eQuery::useCombinedDat();
        $model->useCombinedDat = $db->getOne($query);

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //生徒
        $student = array();
        $student = $db->getRow(knjm272eQuery::getStudent($model), DB_FETCHMODE_ASSOC);
        $arg["sel"]["SCHREGNO"] = $student["SCHREGNO"];
        $arg["sel"]["NAME"] = $student["NAME"];

        //添削者
        $opt_staf = array();
        $result = $db->query(knjm272eQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
        }
        $result->free();

        if (!$model->field["STAFF"] && $model->User == 0) {
            $model->field["STAFF"] = $opt_staf[0]["value"];
        } else if(!$model->field["STAFF"] && $model->User == 1) {
            $model->field["STAFF"] = STAFFCD;
        }

        if (!$opt_staf[0]) {
            $arg["Closing"] = " closing_window('MSG300');";
        }
        $extra = "onChange=\"btn_submit('main');\" ";
        $arg["sel"]["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->field["STAFF"], $opt_staf, $extra, 1);

        //科目
        $subArray = array();
        if (strlen($model->schregno)) {
            $result = $db->query(knjm272eQuery::getSubClass($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subArray[] = array('SUBCLASSCD'     => $row["SUBCD"],
                                    'SUBCLASSNAME'   => $row["SUBNAME"]);
            }
            $result->free();
        }

        //回数
        $subseqArray = array();
        foreach ($subArray as $key => $sub) {
            $result = $db->query(knjm272eQuery::getStandardSeq($model, $sub["SUBCLASSCD"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $seqAll = $row["REP_SEQ_ALL"];
                $startSeq = $row["REP_START_SEQ"];
            }
            $result->free();

            if (strlen($seqAll) && strlen($startSeq)) {
                for ($kai = $startSeq; $kai < $startSeq + $seqAll; $kai++) {
                    $subseqArray[] = array('KEY'            => $sub["SUBCLASSCD"]."-".$kai,
                                           'SUBCLASSCD'     => $sub["SUBCLASSCD"],
                                           'SUBCLASSNAME'   => $sub["SUBCLASSNAME"],
                                           'STANDARD_SEQ'   => $kai);
                }
            }
        }

        //入力値変換(NAMECD2 → ABBV2)
        $hyoukaArray = array();
        $result = $db->query(knjm272eQuery::getHyouka());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $hyoukaArray[$row["NAMECD2"]] = $row["ABBV2"];
        }
        $result->free();

        //入力値チェック用
        $checkHyouka = "";
        $sep = "";
        foreach ($hyoukaArray as $key => $val) {
            $checkHyouka .= $sep.$val;
            $sep = ",";
        }
        knjCreateHidden($objForm, "CHECK_HYOUKA", $checkHyouka);

        /*******************/
        /* 生徒&評価データ */
        /*******************/
        //評価、再1～9
        $dataArray = array();
        foreach ($subseqArray as $key => $subseq) {
            $result = $db->query(knjm272eQuery::getSch($model, $subseq["SUBCLASSCD"], $subseq["STANDARD_SEQ"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //入力値変換(NAMECD2 → ABBV2)
                $row["GRAD_VALUE"] = strlen($row["GRAD_VALUE"]) ? $hyoukaArray[$row["GRAD_VALUE"]] : $row["GRAD_VALUE"];

                //MAX受付日のデータを保管する
                $dataArray[$subseq["KEY"]][$row["REPRESENT_SEQ"]] = array('REPRESENT_SEQ'   => $row["REPRESENT_SEQ"],
                                                                          'RECEIPT_DATE'    => $row["RECEIPT_DATE"],
                                                                          'GRAD_VALUE'      => $row["GRAD_VALUE"]);
            }
            $result->free();
        }

        //(配列)評価、再1～9
        $repSeqArray = array();
        for ($i = 0; $i <= 9; $i++) {
            $repSeqArray[$i] = ($i == 0) ? "評価" : "再".$i;
        }

        $subcdKeep = "";
        $schcnt = 0;
        foreach ($subseqArray as $key => $subseq) {

            //科目名(最初の1行のみ)
            $Row["SUBCLASSNAME"] = ($subcdKeep != $subseq["SUBCLASSCD"]) ? $subseq["SUBCLASSNAME"] : "";
            //科目コード
            knjCreateHidden($objForm, "SUBCLASSCD".$schcnt, $subseq["SUBCLASSCD"]);
            $subcdKeep = $subseq["SUBCLASSCD"];

            //回数
            $Row["STANDARD_SEQ"] = $subseq["STANDARD_SEQ"];
            knjCreateHidden($objForm, "STANDARD_SEQ".$schcnt, $subseq["STANDARD_SEQ"]);

            //初期化
            for ($i = 0; $i <= 9; $i++) {
                $Row["GRAD_VALUE".$i] = "";
                $Row["RECEIPT_DATE".$i] = "";
            }

            //評価、再1～9データ
            //受付日の表示があり、かつ一番右側「MAX(REPRESENT_SEQ)」の評価の登録・更新が可能
            if (get_count($dataArray[$subseq["KEY"]])) {
                $repSeqCnt = 1;
                foreach ($dataArray[$subseq["KEY"]] as $repSeq => $repRow) {
//echo "KEY = ".$subseq["KEY"].", REPRESENT_SEQ = ".$repSeq.", RECEIPT_DATE = ".$repRow["RECEIPT_DATE"].", GRAD_VALUE = ".$repRow["GRAD_VALUE"]."<BR>";
                    //一番右側のREPRESENT_SEQのみ入力
                    if (get_count($dataArray[$subseq["KEY"]]) == $repSeqCnt) {
                        //評価(入力)
                        $extra = "STYLE=\"text-align: center\" onblur=\"check(this)\"";
                        $value = (!isset($model->warning)) ? $repRow["GRAD_VALUE"] : $model->setdata["GRAD_VALUE"][$schcnt];
                        $Row["GRAD_VALUE".$repSeq] = knjCreateTextBox($objForm, $value, "GRAD_VALUE".$schcnt, 2, 2, $extra);
                        //データ保持。入力値との比較用
                        knjCreateHidden($objForm, "PRE_GRAD_VALUE".$schcnt, $repRow["GRAD_VALUE"]);
                        //更新キー
                        knjCreateHidden($objForm, "REPRESENT_SEQ".$schcnt, $repRow["REPRESENT_SEQ"]);
                        knjCreateHidden($objForm, "RECEIPT_DATE".$schcnt, $repRow["RECEIPT_DATE"]);

                        //受付日(表示)
                        $Row["RECEIPT_DATE".$repSeq] = str_replace("-","/",$repRow["RECEIPT_DATE"]);
                    } else {
                        //評価(表示)
                        $Row["GRAD_VALUE".$repSeq] = $repRow["GRAD_VALUE"];

                        //受付日(表示)
                        $Row["RECEIPT_DATE".$repSeq] = str_replace("-","/",$repRow["RECEIPT_DATE"]);
                    }

                    $repSeqCnt++;
                }
            }

            $arg["data2"][] = $Row;

            $schcnt++;
        }
        $model->schcntall = $schcnt;

        $arg["TOTALCNT"] = $model->schcntall."件";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "更 新", $extra);

        //取消ボタン
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //HIDDEN
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm272eForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sel"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//getCmb
function getCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $result->free();

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
