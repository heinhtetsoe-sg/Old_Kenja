<?php

require_once('for_php7.php');

class knjp972aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp972aForm1", "POST", "knjp972aindex.php", "", "knjp972aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjp972aQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('knjp972a');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjp972aQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('knjp972a');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //校種
        $query = knjp972aQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //収入科目コンボ
        $query = knjp972aQuery::getIncomeLMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INCOME_L_CD", $model->field["INCOME_L_CD"], $extra, 1, "ALL");

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model, $arg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp972aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "-- 全て --",
                       'value' => "99");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $opt = array();
    $query = knjp972aQuery::getHrClass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象一覧リスト
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onClick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSV出力ボタン
    $extra = "onClick=\"return btn_submit('csvOutput');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタン
    $extra = "onClick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, &$arg)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJP972A");
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
    knjCreateHidden($objForm, "useDispSchregno_KNJP972", $model->Properties["useDispSchregno_KNJP972"]);
}
