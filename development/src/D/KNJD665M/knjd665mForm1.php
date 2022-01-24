<?php

require_once('for_php7.php');

class knjd665mForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("knjd665mForm1", "POST", "knjd665mindex.php", "", "knjd665mForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->field["YEAR"];

        //学期コンボ
        $query = knjd665mQuery::getSchKind($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //学年コンボ
        $query = knjd665mQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //考査種別一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //教科
        $query = knjd665mQuery::getClassMst($model);
        $extra = "onchange=\"return btn_submit('chgClsCd')\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1);

        //試験種別一覧リストToリスト
        makeListToList2($objForm, $arg, $db, $model);

        //
        // ボタン
        //
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //
        // hidden
        //
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", KNJD665M);

        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd665mForm1.html", $arg);
    }
}

//
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $opt_l = array();
    $opt_r = array();
    $query = knjd665mQuery::getTest($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->cmd != 'chgClsCd' || !in_array($row["VALUE"], $model->selectdata, true)) {
            $opt_l[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $opt_r[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_l, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    if ($model->cmd == 'chgClsCd') {
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_r, $extra, 20);
    } else {
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);
    }

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//クラス一覧リストToリスト作成
function makeListToList2(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $opt = array();
    $query = knjd665mQuery::getSubclassMst($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1_2('left')\"";
    $arg["data"]["CATEGORY_NAME2"] = knjCreateCombo($objForm, "CATEGORY_NAME2", "", $opt, $extra, 10);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1_2('right')\"";
    $arg["data"]["CATEGORY_SELECTED2"] = knjCreateCombo($objForm, "CATEGORY_SELECTED2", "", array(), $extra, 10);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves_2('left');\"";
    $arg["button"]["btn_lefts2"] = knjCreateBtn($objForm, "btn_lefts2", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1_2('left');\"";
    $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1_2('right');\"";
    $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves_2('right');\"";
    $arg["button"]["btn_rights2"] = knjCreateBtn($objForm, "btn_rights2", ">>", $extra);
}
