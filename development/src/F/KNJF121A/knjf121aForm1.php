<?php

require_once('for_php7.php');

class knjf121aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf121aForm1", "POST", "knjf121aindex.php", "", "knjf121aForm1");

        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjf121aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjf121a');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $schoolname2 = $db->getOne(knjf121aQuery::getSchoolName2($model));
            $arg["SCH_NAME2"] = (strlen($schoolname2) > 0) ? "<<".$schoolname2.">>" : "";
        }

        //リストToリスト
        $opt_right = array();
        $opt_left = array();
        foreach ($model->kubunList as $val) {
            if (in_array($val['value'], $model->field["selectdata"])) {
                $opt_left[] = array('label' => $val['label'],
                                    'value' => $val['value']);
            } else {
                $opt_right[] = array('label' => $val['label'],
                                     'value' => $val['value']);
            }
        }

        //利用区分一覧
        $extra = "multiple style=\"width:150px;height:130px;\" width=\"150px\" ondblclick=\"move1('left', 'sort')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 7);
        //選択利用区分一覧
        $extra = "multiple style=\"width:150px;height:130px;\" width=\"150px\" ondblclick=\"move1('right', 'sort')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 7);

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
        $extra = "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //csvボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJF121A");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        knjCreateHidden($objForm, "SDATE", CTRL_YEAR.'/04/01');
        knjCreateHidden($objForm, "EDATE", (CTRL_YEAR+1).'/03/31');

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf121aForm1.html", $arg);
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

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
