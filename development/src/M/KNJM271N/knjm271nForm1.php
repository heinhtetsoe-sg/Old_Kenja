<?php

require_once('for_php7.php');


class knjm271nForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm271nindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["GrYEAR"] = CTRL_YEAR;

        //日付データ
        if ($model->Date == "") $model->Date = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATE"] = View::popUpCalendar($objForm  ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");

        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->Date);

        //科目
        $query = knjm271nQuery::getSubClass($model);
        $extra = "onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //添削者
        $opt_staf = array();
        $opt_staf[] = array("label" => "", "value" => "");
        $loginStaffExist = false;
        $result = $db->query(knjm271nQuery::selectStaff($model));
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

        //回数
        $query = knjm271nQuery::getStandardSeq($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seqAll = $row["REP_SEQ_ALL"];
            $startSeq = $row["REP_START_SEQ"];
        }
        $result->free();
        $seqArray = array();
        $seqArray[] = array("label" => "", "value" => "");
        for ($i = $startSeq; $i < $startSeq + $seqAll; $i++) {
            $seqArray[] = array('label' => $i."回目",
                                'value' => $i);
            if ($model->field["STANDARD_SEQ"] == $i) $value_flg = true;
        }
        $model->field["STANDARD_SEQ"] = ($model->field["STANDARD_SEQ"] && $value_flg) ? $model->field["STANDARD_SEQ"] : $seqArray[0]["value"];
        $extra = "onChange=\"btn_submit('main')\"";
        $arg["sel"]["STANDARD_SEQ"] = knjCreateCombo($objForm, "STANDARD_SEQ", $model->field["STANDARD_SEQ"], $seqArray, $extra, 1);

        /****************************/
        /* 生徒データ出力(画面下部) */
        /****************************/

        //ソート表示文字作成
        $order[1] = "▲";
        $order[2] = "▼";
        $model->getSort = $model->getSort ? $model->getSort : "SRT_CLASS";

        //リストヘッダーソート作成
        $model->sort["SRT_CLASS"] = $model->sort["SRT_CLASS"] ? $model->sort["SRT_CLASS"] : 1;
        $setOrder = $model->getSort == "SRT_CLASS" ? $order[$model->sort["SRT_CLASS"]] : "";
        $CLASS_SORT = "<a href=\"knjm271nindex.php?cmd=sort&sort=SRT_CLASS\" target=\"_self\" STYLE=\"color:white\">クラス{$setOrder}</a>";
        $arg["CLASS_SORT"] = $CLASS_SORT;

        //リストヘッダーソート作成
        $model->sort["SRT_SCHREGNO"] = $model->sort["SRT_SCHREGNO"] ? $model->sort["SRT_SCHREGNO"] : 1;
        $setOrder = $model->getSort == "SRT_SCHREGNO" ? $order[$model->sort["SRT_SCHREGNO"]] : "";
        $SCHREGNO_SORT = "<a href=\"knjm271nindex.php?cmd=sort&sort=SRT_SCHREGNO\" target=\"_self\" STYLE=\"color:white\">学籍番号{$setOrder}</a>";
        $arg["SCHREGNO_SORT"] = $SCHREGNO_SORT;

        //リストヘッダーソート作成
        $model->sort["SRT_NAME"] = $model->sort["SRT_NAME"] ? $model->sort["SRT_NAME"] : 1;
        $setOrder = $model->getSort == "SRT_NAME" ? $order[$model->sort["SRT_NAME"]] : "";
        $NAME_SORT = "<a href=\"knjm271nindex.php?cmd=sort&sort=SRT_NAME\" target=\"_self\" STYLE=\"color:white\">氏名{$setOrder}</a>";
        $arg["NAME_SORT"] = $NAME_SORT;

        //評価コンボ
        $opt_grval = array();
        $result = $db->query(knjm271nQuery::getM003());
        $opt_grval[] = array("label" => "", "value" => "9999");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grval[] = array ("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
        $result->free();

        //抽出データを格納
        $model->stdDbData = array();
        $query = knjm271nQuery::getStdData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->stdDbData[$row["SCHREGNO"]]["SCHREGNO"] = $row["SCHREGNO"];
            $model->stdDbData[$row["SCHREGNO"]]["NAME"]     = $row["NAME"];
            $model->stdDbData[$row["SCHREGNO"]]["GRADE"]    = $row["GRADE"];
            $model->stdDbData[$row["SCHREGNO"]]["HR_CLASS"] = $row["HR_CLASS"];
            $model->stdDbData[$row["SCHREGNO"]]["ATTENDNO"] = $row["ATTENDNO"];
            $model->stdDbData[$row["SCHREGNO"]]["HR_NAME"]  = $row["HR_NAME"];

            //評価
            for ($seqI = 1; $seqI <= $model->maxSeq; $seqI++) {
                if ($seqI == $row["STANDARD_SEQ"]) {
                    $model->stdDbData[$row["SCHREGNO"]]["GRAD_INPUT_DATE{$seqI}"]   = $row["GRAD_INPUT_DATE"];
                    $model->stdDbData[$row["SCHREGNO"]]["GRAD_VALUE{$seqI}"]        = $row["GRAD_VALUE"];
                    $model->stdDbData[$row["SCHREGNO"]]["GRADMARK{$seqI}"]          = $row["GRADMARK"];
                    $model->stdDbData[$row["SCHREGNO"]]["STANDARD_SEQ{$seqI}"]      = $row["STANDARD_SEQ"];
                    $model->stdDbData[$row["SCHREGNO"]]["REPRESENT_SEQ{$seqI}"]     = $row["REPRESENT_SEQ"];
                    $model->stdDbData[$row["SCHREGNO"]]["GRAD_TIME{$seqI}"]         = $row["GRAD_TIME"];
                    $model->stdDbData[$row["SCHREGNO"]]["GRAD_VALUE{$seqI}"]        = $row["GRAD_VALUE"];
                }
            }
        }
        $result->free();

        //抽出データを元に表示データ作成
        $schcnt = 0;
        $setColor1 = "#ffffff";
        $setColor2 = "#00aa77";
        foreach ($model->stdDbData as $schregNo => $schVal) {
            array_walk($schVal, "htmlspecialchars_array");

            $dispData = $schVal;

            $dispData["BG_COLOR"] = ($schcnt % 2) == 0 ? $setColor2 : $setColor1;

            //評価
            for ($seqI = 1; $seqI <= $model->maxSeq; $seqI++) {
                if ($seqI == $model->field["STANDARD_SEQ"]) {
                    $extra = "";
                    $dispData["GRAD_VALUESUB{$seqI}"] = makeCmbArray($objForm, $arg, $opt_grval, $schVal["GRAD_VALUE{$seqI}"], "GRAD_VALUE_{$schregNo}", $extra, 1, "");
                } else {
                    $dispData["GRAD_VALUESUB{$seqI}"] = $schVal["GRADMARK{$seqI}"] ? $schVal["GRADMARK{$seqI}"] : "　";
                }
                if ($schVal["GRAD_INPUT_DATE{$seqI}"]) {
                    list($grdYear, $grdMonth, $grdDay) = explode("-", $schVal["GRAD_INPUT_DATE{$seqI}"]);
                    $dispData["GRAD_INPUT_DATE{$seqI}"] = $grdMonth."/".$grdDay;
                } else {
                    $dispData["GRAD_INPUT_DATE{$seqI}"] = "　";
                }
            }

            $arg["data2"][] = $dispData;

            $schcnt++;
        }
        $model->schcntall = $schcnt;

        $arg["TOTALCNT"] = $model->schcntall."件";

        //ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "登　録", $extra);

        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取　消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終　了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm271nForm1.html", $arg); 
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
