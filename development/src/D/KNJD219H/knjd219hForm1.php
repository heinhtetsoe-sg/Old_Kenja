<?php

require_once('for_php7.php');

class knjd219hform1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd219hindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["sepa"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //対象
        //学期コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");
        //学期(在籍データ用)
        $model->field["SEMESTER_SCH"] = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        //校種コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getSchoolKind($model, $model->year, "A023");
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //ALL
        $model->field["GRADE_ALL"]      = "00";
        $model->field["COURSECODE_ALL"] = "0000";
        $model->field["SUBCLASSCD_ALL"] = "00-".$model->field["SCHOOL_KIND"]."-00-000000";

        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "ALL", $model->field["GRADE_ALL"]);
        //コースコンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getCoursecode($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSECODE"], "COURSECODE", $extra, 1, "ALL", $model->field["COURSECODE_ALL"]);
        //科目コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getSubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "ALL", $model->field["SUBCLASSCD_ALL"]);
        //算出先になる成績
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjd219hQuery::getTestcd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTCD"], "TESTCD", $extra, 1, "");

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd219hForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, $model) {
    //履歴一覧
    $query = knjd219hQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHAIRDATE"] = str_replace("-", "/", $row["CHAIRDATE"]);

        $arg['data'][] = $row;
    }
    $result->free();
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $valAll = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL" && strlen($valAll)) {
        $opt[] = array("label" => "全て", "value" => $valAll);
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($blank == "ALL" && strlen($valAll)) {
        if ($value == $valAll) $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
