<?php

require_once('for_php7.php');

class knje370lForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje370lForm1", "POST", "knje370lindex.php", "", "knje370lForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $query = knje370lQuery::getSemester();
        $result = $db->query($query);
        $semester = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $arg["data"]["SEMESTER"] = $semester["LABEL"];

        //進学チェックボックス
        $radioValue = array(1, 2);
        $click = "onclick =\" return btn_submit('knje370l');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
        $model->field["OUTPUT"] = $model->field["OUTPUT"] ? $model->field["OUTPUT"] : "1";
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["OUTPUT"] == "2") {
            $arg["LISTTITLE"] = "就職先";
        } else {
            $arg["LISTTITLE"] = "進学先";
        }

        //抽出条件チェックボックス
        $radioValue = array(1, 2);
        $click = "onclick =\" return btn_submit('knje370l');\"";
        $extra = array("id=\"FILTER1\"".$click, "id=\"FILTER2\"".$click);
        $model->field["FILTER"] = $model->field["FILTER"] ? $model->field["FILTER"] : "2";
        $radioArray = knjCreateRadio($objForm, "FILTER", $model->field["FILTER"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //リストToリスト作成
        makeGradeList($objForm, $arg, $db, $model);

        /********/
        /*ボタン*/
        /********/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        // //ＣＳＶ出力ボタン
        // $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        // $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE370L");
        knjCreateHidden($objForm, "cmd");
        // knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        // knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        // knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje370lForm1.html", $arg); 
    }
}

function makeGradeList(&$objForm, &$arg, $db, $model) {
    //対象クラスリストを作成する
    $query = knje370lQuery::getListInfo($model);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"height:230px;width:230px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //出力対象一覧リストを作成する//
    $extra = "multiple style=\"height:230px;width:230px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    //対象選択">>"ボタン
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消"<<"ボタン
    $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択"＞"ボタン
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消"＜"ボタン
    $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}
?>
