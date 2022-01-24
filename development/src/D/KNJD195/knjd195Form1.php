<?php

require_once('for_php7.php');

class knjd195Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd195Form1", "POST", "knjd195index.php", "", "knjd195Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd195Query::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd195Query::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テストコンボ作成
        $query = knjd195Query::getTest($model->field["SEMESTER"]);
        $opt = array();
        $value = $model->field["SUB_TESTCD"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            /******************************************************/
            /* アンダーバーの後ろの数字は切替コードです。         */
            /* (テスト種別コード + テスト項目コード)_(切替コード) */
            /******************************************************/
            if (preg_match('/(220250|220410)/', $schoolCode)) { //「米子」、「境」
                if ($row["VALUE"] == '9900' && $model->field["SEMESTER"] == '2') {
                    continue;
                }
                if ($schoolCode == '220410' && $row["VALUE"] == '9900' && $model->field["SEMESTER"] == '9') {
                    $opt[] = array('label' => $row["VALUE"] . ':学年評価',
                                   'value' => $row["VALUE"] . "_1");
                }
                if (preg_match('/(0101|0201|0202)/', $row["VALUE"])) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_1");
                } else {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_2");
                }
                if ($row["VALUE"] == '0201') {
                    $opt[] = array('label' => $row["VALUE"] . ':仮評価',
                                   'value' => $row["VALUE"] . "_2");
                }
            } else if (preg_match('/220170/', $schoolCode)) { //「湖陵」
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"] . "_2");
            } else if (preg_match('/224030/', $schoolCode)) { //「中央育英」
                if (preg_match('/(0101|0201|0202)/', $row["VALUE"])) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_1");
                } else {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_2");
                }

            } else { //「倉吉」
                if (preg_match('/(0101|0201)/', $row["VALUE"])) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_1");
                } else {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"] . "_2");
                }
            }
            if (preg_match("/^{$row["VALUE"]}_./", $value)) $value_flg = true;
        }

        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"]["SUB_TESTCD"] = knjCreateCombo($objForm, "SUB_TESTCD", $value, $opt, "", 1);

        //学年コンボ作成
        $query = knjd195Query::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd195Form1.html", $arg); 
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

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD195");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCORE_FLG");
    knjCreateHidden($objForm, "TESTCD");
}

?>
