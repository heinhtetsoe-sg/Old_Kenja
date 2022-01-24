<?php

require_once('for_php7.php');


class knjj130Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj130Form1", "POST", "knjj130index.php", "", "knjj130Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1" && $model->Properties["useClubMultiSchoolKind"] != "1") {
            $arg["schkind"] = "1";
            $query = knjj130Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj130');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        $query = knjj130Query::getClubNameList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label'      => $row["VALUE"]."　".$row["LABEL"],
                            'value'     => $row["VALUE"]);
        }
        $result->free();

        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left', 'club')\"";
        $arg["data"]["CLUB_NAME"] = knjCreateCombo($objForm, "CLUB_NAME", $value, isset($row1) ? $row1 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right', 'club')\"";
        $arg["data"]["CLUB_SELECTED"] = knjCreateCombo($objForm, "CLUB_SELECTED", $value, array(), $extra, 15);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', 'club');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', 'club');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'club');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'club');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //ソート一覧
        $query = knjj130Query::getSortList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row2[]= array('label'     => $row["LABEL"],
                           'value'     => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"height:90px;width:180px\" width:\"180px\" ondblclick=\"move1('left', 'sort')\"";
        $arg["data"]["SORT_NAME"] = knjCreateCombo($objForm, "SORT_NAME", $value, isset($row2) ? $row2 : array(), $extra, 15);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"height:90px;width:180px\" width:\"180px\" ondblclick=\"move1('right', 'sort')\"";
        $arg["data"]["SORT_SELECTED"] = knjCreateCombo($objForm, "SORT_SELECTED", $value, array(), $extra, 15);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'sort');\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'sort');\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "＜", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJJ130");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj130Form1.html", $arg);
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

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
