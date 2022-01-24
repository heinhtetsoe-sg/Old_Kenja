<?php

require_once('for_php7.php');

class knjd231dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd231dForm1", "POST", "knjd231dindex.php", "", "knjd231dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd231dQuery::getSemester();
        $extra = "onChange=\"return btn_submit('knjd231d');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ
        $query = knjd231dQuery::getGradeHrClass($model);
        $extra = "onChange=\"return btn_submit('knjd231d');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //種別コンボ
        $query = knjd231dQuery::getTest($model);
        $extra = "onChange=\"return btn_submit('knjd231d');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //欠点
        $ketten = $db->getOne(knjd231dQuery::getAssessHigh());
        if ($model->cmd == "") $model->field["KETTEN"] = $ketten ? $ketten : "39";
        $extra = "style=\"text-align: right\" onblur=\"changeKetten(this, 'chgKetten', '0')\" onkeydown=\"changeKetten(this, 'chgKetten', '1')\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //ALLチェック
        $arg["data"]["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データ取得
        $setval = array();
        $schregno = "";
        $total = array();
        $row_cnt = array();
        $query = knjd231dQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setval[] = $row;

            if ($schregno != $row["SCHREGNO"]) {
                $total[$row["SCHREGNO"]] = 0;
                $row_cnt[$row["SCHREGNO"]] = 0;
            }
            $total[$row["SCHREGNO"]] += $row["CREDITS"];
            $row_cnt[$row["SCHREGNO"]] ++;

            $schregno = $row["SCHREGNO"];
        }

        //データセット
        $schregno = "";
        $counter = 0;
        foreach ($setval as $key => $row) {
            if (is_array($model->checked)) {
                $extra  = (in_array($row["SCHREGNO"], $model->checked)) ? "checked" : "";
            } else {
                $extra  = "";
            }
            $extra .= " onClick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SCHREGNO"], $extra, 1);

            //単位数合計
            $row["TOTAL_CREDITS"] = $total[$row["SCHREGNO"]];

            if ($schregno != $row["SCHREGNO"]) {
                $row["ROWSPAN"] = $row_cnt[$row["SCHREGNO"]];
                $counter++;
            }

            //背景色
            $row["BGCOLOR"] = ($counter % 2 == 0) ? "#cccccc" : "#ffffff";

            $arg["list"][] = $row;
            $schregno = $row["SCHREGNO"];
        }

        //出力日付
        if ($model->cmd == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd231dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //警告書出力ボタン
    $extra  = (get_count($model->checked) > 0) ? "" : "disabled ";
    $extra .= "onclick=\"return newwin('".SERVLET_URL."', 'warning');\"";
    $arg["button"]["btn_print1"] = knjCreateBtn($objForm, "btn_print1", "警告書出力", $extra);
    //リスト出力ボタン
    $extra = "onclick=\"return newwin('".SERVLET_URL."', 'list');\"";
    $arg["button"]["btn_print2"] = knjCreateBtn($objForm, "btn_print2", "リスト出力", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD231D");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "FORM_KIND");
    knjCreateHidden($objForm, "KEEP_KETTEN", $model->field["KETTEN"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
?>
