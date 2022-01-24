<?php

require_once('for_php7.php');

class knjh430Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh430index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            unset($model->selectdata);
        }

        //校種
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjh430Query::getSchoolKind($model->selectSchoolKind);
            $SchKindCnt = $db->getCol($query);
            if (get_count($SchKindCnt) > 1) {
                $extra = "onChange=\"return btn_submit('edit');\"";
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                $arg["SCHOOL_KIND_SHOW"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $db->getOne($query);
                knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        }

        //課程コンボ
        $query = knjh430Query::getPortfolioHeadMst($model, "course");
        $CourseCnt = $db->getCol($query);
        if (get_count($CourseCnt) > 1) {
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, "COURSECD", $model->field["COURSECD"], $extra, 1);
            $arg["COURSE_SHOW"] = 1;
        } else {
            $model->field["COURSECD"] = $db->getOne($query);
            knjCreateHidden($objForm, "COURSECD", $model->field["COURSECD"]);

        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh430Form1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //項目一覧取得
    $opt_right = $opt_left = array();
    $query = knjh430Query::getPortfolioHeadMst($model, "list");
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->selectdata) {
            list ($sort, $seq) = explode(':', $row["VALUE"]);
            if (in_array($seq, $selectdata)) {
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        } else {
            list ($sort, $seq) = explode(':', $row["VALUE"]);

            if ($sort != "00") {
                $opt_left[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_SELECT"] = knjCreateCombo($objForm, "RIGHT_SELECT", "", $opt_right, $extra, 15);

    //対象リスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_SELECT"] = knjCreateCombo($objForm, "LEFT_SELECT", "", $opt_left, $extra, 15);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>