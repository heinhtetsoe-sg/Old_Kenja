<?php

require_once('for_php7.php');

class knjm373eForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm373eForm1", "POST", "knjm373eindex.php", "", "knjm373eForm1");

        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する

        $arg["data"]["YEAR"] = $model->control["年度"];

        //hidden
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期コンボボックス設定
        $semester_flg = false;
        $ctlsemes_flg = false;

        $query = knjm373eQuery::getSemesterMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row0[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GAKKI"] == $row["VALUE"]) {
                $semester_flg = true;
            }
            if (CTRL_SEMESTER == $row["VALUE"]) {
                $ctlsemes_flg = true;
            }
        }
        $result->free();

        if (!isset($model->field["GAKKI"]) || !$semester_flg) {
            if ($ctlsemes_flg) {
                $model->field["GAKKI"] = CTRL_SEMESTER;
            } else {
                $model->field["GAKKI"] = $row0[0]["value"];
            }
        }
        $extra = "onchange=\"return btn_submit('gakki'),AllClearList();\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], isset($row0) ? $row0 : array(), $extra, 1);
        knjCreateHidden($objForm, "GAKKI", $model->field["GAKKI"]);

        //クラス選択コンボボックスを作成する
        $query = knjm373eQuery::getGrade($model);

        $result = $db->query($query);
        $grade_flg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => sprintf("%1d",$row["GRADE"])."学年",
                           'value' => $row["GRADE"]);
            if ($model->field["GRADE"] == $row["GRADE"]) {
                $grade_flg = true;
            }
        }
        $result->free();

        if (!isset($model->field["GRADE"]) || !$grade_flg) {
            $model->field["GRADE"] = $row1[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('gakki'),AllClearList();\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], isset($row1) ? $row1 : array(), $extra, 1);

        //対象者リストを作成する
        $query = knjm373eQuery::getAuth($model->control["年度"],$model->control["学期"],$model->field["GRADE"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $arg["data"][""] = $objForm->ge("category_name");
        $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, isset($opt1) ? $opt1 : array(), $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $value, array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM373E");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useRepStandarddateCourseDat", $model->Properties["useRepStandarddateCourseDat"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm373eForm1.html", $arg); 
    }
}
?>
