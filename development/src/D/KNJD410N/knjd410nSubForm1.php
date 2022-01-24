<?php

require_once('for_php7.php');

class knjd410nSubForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjd410nindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //対象学部表示
        $arg["SCHOOL_KIND"] = $db->getOne(knjd410nQuery::getSchoolKind($model->school_kind));

        //対象年組表示
        $arg["GRADE_HR_CLASS"] = $db->getOne(knjd410nQuery::getGradeHrclass($model, $model->hr_class));

        //生徒リストToリスト作成
        if ($model->cmd == "subform1A") {
            $l_query = ($model->selectdata2) ? knjd410nQuery::getRightSchList($model, "list", $model->selectdata2) : "";
        } else {
            $l_query = ($model->schregno) ? knjd410nQuery::getRightSchList($model, "list", $model->schregno) : "";
            $model->selectdata2 = $model->schregno;
        }
        $r_query = knjd410nQuery::getRightSchList($model, "list");
        makeListToList($objForm, $arg, $db, "data2", "CATEGORY_SELECTED2", "CATEGORY_NAME2", $l_query, $r_query, "15", $model);

        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //状態区分コンボ作成
        $query = knjd410nQuery::getCondition($model);
        $extra = "onchange=\"return btn_submit('subform1A');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1);

        //科目リストToリスト作成
        if ($model->cmd == "subform1A") {
            $l_query = ($model->selectdata) ? knjd410nQuery::getSubclassMst($model, $model->selectdata) : "";
        } else {
            $l_query = ($model->selectdata2) ? knjd410nQuery::getSchregCompSubclassDat($model, $model->selectdata2) : "";
        }
        $r_query = knjd410nQuery::getGradeKindCompSubclassDat($model);
        makeListToList($objForm, $arg, $db, "data1", "CATEGORY_SELECTED1", "CATEGORY_NAME1", $l_query, $r_query, "15", $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd410nSubForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array();
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $div, $l_name, $r_name, $l_query, $r_query, $line, $model)
{
    $opt_right = $opt_left = array();
    $selected = array();

    //対象一覧取得
    if ($l_query) {
        $result = $db->query($l_query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $selected[] = $row["VALUE"];
        }
        $result->free();
    }
    //対象一覧リスト作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div][$l_name] = knjCreateCombo($objForm, $l_name, "", $opt_left, $extra, $line);

    //一覧取得
    if ($r_query) {
        $result = $db->query($r_query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $selected)) {
                continue;
            }
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
    }
    //一覧リスト作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div][$r_name] = knjCreateCombo($objForm, $r_name, "", $opt_right, $extra, $line);

    //選択ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_add_all', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //選択ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('left', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //取消ボタン作成（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('right', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //取消ボタン作成（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move1('sel_del_all', '{$l_name}', '{$r_name}', 1);\"";
    $arg[$div]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/D/KNJD410N/knjd410nindex.php?cmd=";
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}
