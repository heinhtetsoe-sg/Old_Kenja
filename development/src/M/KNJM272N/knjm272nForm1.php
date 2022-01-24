<?php

require_once('for_php7.php');


class knjm272nForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm272nindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $query = knjm272nQuery::useCombinedDat();
        $model->useCombinedDat = $db->getOne($query);

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //生徒
        $student = array();
        $student = $db->getRow(knjm272nQuery::getStudent($model), DB_FETCHMODE_ASSOC);
        $arg["sel"]["SCHREGNO"] = $student["SCHREGNO"];
        $arg["sel"]["NAME"] = $student["NAME"];

        //添削者
        $opt_staf = array();
        $opt_staf[] = array("label" => "", "value" => "");
        $loginStaffExist = false;
        $result = $db->query(knjm272nQuery::selectStaff($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_staf[] = array("label" => $row["STAFFNAME"],
                                "value" => $row["STAFFCD"]);
            if (!$loginStaffExist && $row["STAFFCD"] == STAFFCD) {
                $loginStaffExist = true;
            }
        }
        $result->free();

        if (!$model->field["STAFF"]) {
            $model->field["STAFF"] = $loginStaffExist ? STAFFCD : $opt_staf[0]["value"];
        }
        $extra = "";
        $arg["sel"]["STAFF"] = knjCreateCombo($objForm, "STAFF", $model->field["STAFF"], $opt_staf, $extra, 1);

        //科目
        $subArray = array();
        if (strlen($model->schregno)) {
            $result = $db->query(knjm272nQuery::getSubClass($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subArray[$row["SUBCD"]]["SUBCLASSNAME"] = $row["SUBNAME"];
            }
            $result->free();
        }

        //回数
        $subseqArray = array();
        $model->subArray = array();
        foreach ($subArray as $subclassCd => $subVal) {
            $result = $db->query(knjm272nQuery::getStandardSeq($model, $subclassCd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $seqAll = $row["REP_SEQ_ALL"];
                $startSeq = $row["REP_START_SEQ"];
            }
            $result->free();
            $model->subArray[$subclassCd]["SUBCLASSNAME"] = $subVal["SUBCLASSNAME"];

            if (strlen($seqAll) && strlen($startSeq)) {
                for ($kai = $startSeq; $kai < $startSeq + $seqAll; $kai++) {
                    $model->subArray[$subclassCd]["SEQ_{$kai}"]["SEQ"] = $kai;
                }
            }
        }

        /*******************/
        /* 生徒&評価データ */
        /*******************/

        //評価コンボ
        $opt_grval = array();
        $result = $db->query(knjm272nQuery::getM003($model));
        $opt_grval[] = array("label" => "", "value" => "9999");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grval[] = array ("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
        $result->free();

        foreach ($model->subArray as $subclassCd => $subVal) {
            $result = $db->query(knjm272nQuery::getSch($model, $subclassCd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->subArray[$subclassCd]["SEQ_{$row["STANDARD_SEQ"]}"]["GRAD_VALUE"] = $row["GRAD_VALUE"];
                if ($row["GRAD_INPUT_DATE"]) {
                    list($year, $month, $day) = explode("-", $row["GRAD_INPUT_DATE"]);
                    $model->subArray[$subclassCd]["SEQ_{$row["STANDARD_SEQ"]}"]["GRAD_INPUT_DATE"] = $month."/".$day;
                } else {
                    $model->subArray[$subclassCd]["SEQ_{$row["STANDARD_SEQ"]}"]["GRAD_INPUT_DATE"] = "　";
                }
            }
            $result->free();
        }

        foreach ($model->subArray as $subclassCd => $subVal) {

            $Row = array();
            //科目名
            $Row["SUBCLASSNAME"] = $subVal["SUBCLASSNAME"];
            for ($seqCnt = 1; $seqCnt <= 12; $seqCnt++) {

                $seqVal = $subVal["SEQ_{$seqCnt}"];
                $soeji = $subclassCd."_".$seqVal["SEQ"];
                if (strlen($seqVal["SEQ"]) > 0) {
                    $extra = "";
                    $Row["GRAD_VALUESUB{$seqCnt}"] = makeCmbArray($objForm, $arg, $opt_grval, $seqVal["GRAD_VALUE"], "GRAD_VALUE_{$soeji}", $extra, 1, "");
                    $Row["GRAD_INPUT_DATE{$seqCnt}"] = $seqVal["GRAD_INPUT_DATE"];

                    //textbox
                    $extra = "style=\"text-align:right\" onblur=\"checkDate(this)\"";
                    $Row["GRAD_INPUT_DATE{$seqCnt}"] = knjCreateTextBox($objForm, $Row["GRAD_INPUT_DATE{$seqCnt}"], "GRAD_INPUT_DATE_{$soeji}", 5, 5, $extra);
                } else {
                    $Row["GRAD_VALUESUB{$seqCnt}"] = "　";
                    $Row["GRAD_INPUT_DATE{$seqCnt}"] = "　";
                }

            }
            $arg["data2"][] = $Row;
        }

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
        knjCreateHidden($objForm, "thisYear", CTRL_YEAR);
        knjCreateHidden($objForm, "nextYear", (CTRL_YEAR + 1));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm272nForm1.html", $arg); 
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
//makeCmb
function makeCmbArray(&$objForm, &$arg, $opt, &$value, $name, $extra, $size, $blank = "")
{
    $value_flg = false;
    foreach ($opt as $key => $val) {
        if ($value == $val["value"]) $value_flg = true;
    }
    $value = (strlen($value) > 0 && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
