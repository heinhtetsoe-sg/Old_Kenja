<?php

require_once('for_php7.php');


class knjh080dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjh080dForm1", "POST", "knjh080dindex.php", "", "knjh080dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //クラス選択コンボ
        $query = knjh080dQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('knjh080d');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "");

        //対象者リスト
        $query = knjh080dQuery::getStudentSql($model);
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[] = array('label' => $row["NAME"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //生徒一覧リストを作成する
        $extra = "multiple style=\"height:230px; width:230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, $opt1, $extra, 20);

        //対象リストを作成する
        $extra = "multiple style=\"height:230px; width:230px\" ondblclick=\"move1('right')\"";
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

        //(PDF)基本情報
        $extra = "id=\"PRINTPAGE1\" checked ";
        $arg["data"]["PRINTPAGE1"] = knjCreateCheckBox($objForm, "PRINTPAGE1", "1", $extra);

//コンボ1
        $query = knjh080dQuery::getNameMstSql("H508");
        $extra = "onchange=\"return btn_submit('knjh080d')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCY_TYPE"], "PROFICIENCY_TYPE", $extra, 1);

//コンボ2
        $query = knjh080dQuery::getProfSubclassMst($model);
        $extra = "onchange=\"return btn_submit('knjh080d')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCY_SUBJECT"], "PROFICIENCY_SUBJECT", $extra, 1);

        //(PDF)指導情報
        $extra = "id=\"PRINTPAGE2\" checked";
        $arg["data"]["PRINTPAGE2"] = knjCreateCheckBox($objForm, "PRINTPAGE2", "1", $extra);

        //(PDF)活動情報
        $extra = "id=\"PRINTPAGE3\" checked";
        $arg["data"]["PRINTPAGE3"] = knjCreateCheckBox($objForm, "PRINTPAGE3", "1", $extra);

        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJH080D");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh080dForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
