<?php

require_once('for_php7.php');

class knjb105Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb105Form1", "POST", "knjb105index.php", "", "knjb105Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjb105Query::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('knjb105')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->setSchoolKind, $extra, 1, "");
        } else {
            $model->setSchoolKind = SCHOOLKIND;
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb105Form1.html", $arg); 
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "") {
    $opt = array();
    $result = $db->query($query);
    $chk = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $chk = $value;
        }
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value != "" && $chk) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //コースコンボ作成
    $opt = array();
    $value_flg = false;
    $result = $db->query(knjb105Query::getCouseName($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $value = $row["GRADE"].":".$row["COURSECD"].":".$row["MAJORCD"].":".$row["COURSECODE"];

        $opt[] = array("label" => $row["GRADE_NAME1"]."&nbsp;&nbsp;".
                                  "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                  $row["COURSENAME"].$row["MAJORNAME"]."&nbsp;&nbsp;".
                                  "(".$row["COURSECODE"].")&nbsp;".
                                  $row["COURSECODENAME"], 
                       "value" => $value);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:380px\" width:\"380px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:380px\" width:\"380px\" ondblclick=\"move1('right')\"";
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

function makeBtn(&$objForm, &$arg, $model) {
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
    knjCreateHidden($objForm, "PRGID",         "KNJB105");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
