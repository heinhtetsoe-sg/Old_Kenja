<?php

require_once('for_php7.php');

class knje382Form1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knje382index.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //学期表示
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //学校種別
        $query = knje382Query::getSchoolKind($model);
        if (get_count($db->getCol($query)) > 1) {
            $extra = "onchange=\"return btn_submit('');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");
            $arg["show_school_kind"] = 1;
            knjCreateHidden($objForm, "SHOW_SCHOOL_KIND", "1");
        } else {
            $model->field["SCHOOL_KIND"] = $db->getOne($query);
            knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
            $arg["show_school_kind"] = "";
            knjCreateHidden($objForm, "SHOW_SCHOOL_KIND", "0");
        }

        //進路種別
        $opt = array();
        $coursekind = array('', '1：進学', '2：就職', '3：その他', '4：未定');
        foreach ($coursekind as $key => $val) {
            $key = ($key == "0") ? "" : $key;
            if ($key == "3") $val .= '（家事手伝い等）';
            $opt[] = array('label' => $val, 'value' => $key);
        }
        $extra = "";
        $arg["COURSE_KIND"] = knjCreateCombo($objForm, "COURSE_KIND", $model->field["COURSE_KIND"], $opt, $extra, 1);

        //調査名
        $query = knje382Query::getQuestionnaireList();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["FROM_QUESTIONNAIRECD"], "FROM_QUESTIONNAIRECD", $extra, 1, "blank");
        makeCmb($objForm, $arg, $db, $query, $model->field["TO_QUESTIONNAIRECD"], "TO_QUESTIONNAIRECD", $extra, 1, "blank");

        //登録日
        if ($model->field["TO_ENTRYDATE"] == "") $model->field["TO_ENTRYDATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["TO_ENTRYDATE"] = View::popUpCalendar($objForm, "TO_ENTRYDATE", $model->field["TO_ENTRYDATE"]);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model, $coursekind);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);

        //年度開始終了日付（登録日チェック用）
        $semes = $db->getRow(knje382Query::getSemesterMst(), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));

        knjCreateHidden($objForm, "HIDDEN_SCHOOL_KIND");
        knjCreateHidden($objForm, "HIDDEN_COURSE_KIND");
        knjCreateHidden($objForm, "HIDDEN_FROM_QUESTIONNAIRECD");
        knjCreateHidden($objForm, "HIDDEN_TO_QUESTIONNAIRECD");
        knjCreateHidden($objForm, "HIDDEN_TO_ENTRYDATE");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knje382Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND") {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model, $coursekind) {
    $query = knje382Query::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["TO_ENTRYDATE"] = str_replace("-", "/", $row["TO_ENTRYDATE"]);
        $row["COURSE_KIND_LABEL"] = $coursekind[$row["COURSE_KIND"]];

        $arg["rireki"][] = $row;
    }
    $result->free();
}
?>
