<?php

require_once('for_php7.php');

class knjp907Form1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp907Form1", "POST", "knjp907index.php", "", "knjp907Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjp907Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOLKIND", $model->field["SCHOOLKIND"], $extra, 1, "");

        //転退学日コンボ
        $query = knjp907Query::getTentaigaku($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TENTAI_DATE", $model->field["TENTAI_DATE"], $extra, 1, "");

        //対象クラス
        $query = knjp907Query::getGradeHrClass($model, $model->field["TENTAI_DATE"]);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "");

        $query = knjp907Query::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //返金確定日
        $model->field["HENKIN_KAKUTEI"] = ($model->field["HENKIN_KAKUTEI"] != '') ? $model->field["HENKIN_KAKUTEI"]: str_replace('-', '/', CTRL_DATE);
        $arg["HENKIN_KAKUTEI"] = View::popUpCalendarAlp($objForm, "HENKIN_KAKUTEI", $model->field["HENKIN_KAKUTEI"], $disabled, "");

        //出力対象radio(1:返金 2:返金済 3:全員)
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_DIV{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //住所checkbox
        $checked = ($model->cmd == '' || $model->field["ADDRESS"] == '1') ? ' checked': '';
        $extra = "id=\"ADDRESS\"".$checked;
        $arg["ADDRESS"] = knjCreateCheckBox($objForm, "ADDRESS", "1", $extra);

        //ラジオ（1:生徒 2:保護者）
        $opt = array(1, 2);
        $model->field["ADDR_DIV"] = ($model->field["ADDR_DIV"] == "") ? "1" : $model->field["ADDR_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"ADDR_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "ADDR_DIV", $model->field["ADDR_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //返金実行ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_henkin_exec"] = knjCreateBtn($objForm, "btn_henkin_exec", "返金実行", $extra);
        //返金キャンセルボタン
        $extra = "onclick=\"return btn_submit('cancel_update')\"";
        $arg["button"]["btn_henkin_cancel"] = knjCreateBtn($objForm, "btn_henkin_cancel", "返金キャンセル", $extra);
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJP907");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
        knjCreateHidden($objForm, "SCHOOLCD", (sprintf("%012d", SCHOOLCD)));
        knjCreateHidden($objForm, "updSelected");
        //給付対象使用するか
        knjCreateHidden($objForm, "useBenefit", $model->Properties["useBenefit"]);
        //予算実績管理を使用するか
        knjCreateHidden($objForm, "LevyBudget", $model->Properties["LevyBudget"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp907Form1.html", $arg); 
    }
}
/*****************************************************************************************************************/
/***************************************** 以下関数 **************************************************************/
/*****************************************************************************************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $rightList = array();
    $query = knjp907Query::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["LABEL"] = $row["HENKIN_INFO"]."　".$row["ATTENDNO"]."番　".$row["NAME"];
        $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TENTAI_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == 'SCHOOLKIND') {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
