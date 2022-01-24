<?php

require_once('for_php7.php');

class knjh539aForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = CTRL_YEAR ."年度";

        /* 学期コンボ */
        $query = knjh539aQuery::getSemester();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        /* 学年コンボ */
        $query = knjh539aQuery::getGrade($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $hrName = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "BLANK");

        //区分コンボボックス
        $query = knjh539aQuery::getProficiencyDiv();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1);

        //テストコンボボックス
        $query = knjh539aQuery::getProficiencyMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYCD"], "PROFICIENCYCD", $extra, 1, "BLANK");

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFF_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "PASS_AUTH", DEF_UPDATABLE);

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjh539aindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh539aForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }
    if ($name == "SEMESTER") {
        $value = ($value && in_array($value, $serch)) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $query = knjh539aQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHAIRDATE"] = str_replace("-", "/", $row["CHAIRDATE"]);
        $arg['dataRireki'][] = $row;
    }
    $result->free();
}
?>
