<?php

require_once('for_php7.php');

class knjs330Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs330Form1", "POST", "knjs330index.php", "", "knjs330Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        /**********/
        /* コンボ */
        /**********/
        //年組
        $query = knjs330Query::getGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", "", 1);
        //対象月
        $query = knjs330Query::getMonthAll($model);
        $extra = "onChange=\"btn_submit('knjs330')\";";
        makeCmb($objForm, $arg, $db, $query, $model->field["MONTH"], "MONTH", $extra, 1);

        /**********/
        /* ラジオ */
        /**********/
        //週 1:1週 ～ 6:6週
        $opt = array();
        for ($weekCnt = 1; $weekCnt <= get_count($model->weekArray[0]["DAY"]); $weekCnt++) {
            $opt[] = $weekCnt;
            $arg["WEEK_SHOW".$weekCnt] = "1";
            knjCreateHidden($objForm, "FIRSTDATE".$weekCnt, $model->weekArray[0]["DAY"][$weekCnt]);
        }
        $extra = array("id=\"WEEK1\"",
                       "id=\"WEEK2\"",
                       "id=\"WEEK3\"",
                       "id=\"WEEK4\"",
                       "id=\"WEEK5\"",
                       "id=\"WEEK6\"");
        $model->field["WEEK"] = $model->field["WEEK"] ? $model->field["WEEK"] : getWeekShowVal($model);
        $model->field["WEEK"] = $model->field["WEEK"] > get_count($opt) ? "1" : $model->field["WEEK"];
        $radioArray = knjCreateRadio($objForm, "WEEK", $model->field["WEEK"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //教科名 1:時間割上教科名 2:時数集計教科名
        $opt = array(1, 2);
        $extra = array("id=\"JISU1\"", "id=\"JISU2\"");
        $model->field["JISU"] = strlen($model->field["JISU"]) ? $model->field["JISU"] : "1";
        $radioArray = knjCreateRadio($objForm, "JISU", $model->field["JISU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //単元名 1:最下位 2:大単元
        $opt = array(1, 2);
        $extra = array("id=\"UNIT1\"", "id=\"UNIT2\"");
        $model->field["UNIT"] = strlen($model->field["UNIT"]) ? $model->field["UNIT"] : "2";
        $radioArray = knjCreateRadio($objForm, "UNIT", $model->field["UNIT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //備考欄 1:メモ 2:授業内容 3:目標
        $opt = array(1, 2, 3);
        $extra = array("id=\"REMARK1\"", "id=\"REMARK2\"", "id=\"REMARK3\"");
        $model->field["REMARK"] = strlen($model->field["REMARK"]) ? $model->field["REMARK"] : "1";
        $radioArray = knjCreateRadio($objForm, "REMARK", $model->field["REMARK"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //フォーム 1:月～金 2:月～日
        $opt = array(1, 2);
        $extra = array("id=\"FORM1\"", "id=\"FORM2\"");
        $model->field["FORM"] = strlen($model->field["FORM"]) ? $model->field["FORM"] : "1";
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs330Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "PRGID", "KNJS330");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "MONTH") {
        $ctrl_date = preg_split("/-/", CTRL_DATE);
        $value = ($value && $value_flg) ? $value : $ctrl_date[1];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function getWeekShowVal($model) {
    $setWeek = 1;
    foreach ($model->weekArray as $key => $val) {
        foreach ($val["DAY"] as $Dkey => $Dval) {
            if ($Dval == strtr(CTRL_DATE, "/", "-")) {
                $setWeek = $Dkey;
                break;
            }
        }
    }
    return $setWeek;
}
?>
