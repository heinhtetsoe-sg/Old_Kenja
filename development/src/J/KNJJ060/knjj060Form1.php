<?php

require_once('for_php7.php');

class knjj060Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjj060Form1", "POST", "knjj060index.php", "", "knjj060Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if (!$model->Properties["useClubMultiSchoolKind"] && $model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj060Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjj060');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        /* 部・クラブ一覧リスト作成する */
        $query = knjj060Query::getClubNameList($model);
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

        /* ソート順リスト作成する */
        $opt = array();
        $opt[]= array('label' => '性別',             'value' => 'SEX');
        $opt[]= array('label' => '役職コード(降順)', 'value' => 'EXECUTIVECD');
        $opt[]= array('label' => '役職コード(昇順)', 'value' => 'EXECUTIVECD2');
        $opt[]= array('label' => '年組番',           'value' => 'NEN_KUMI_BAN');

        $opt_mst = array('SEX'          => '性別',
                         'EXECUTIVECD'  => '役職コード(降順)',
                         'EXECUTIVECD2' => '役職コード(昇順)',
                         'NEN_KUMI_BAN' => '年組番');

        //ソート一覧の項目を作成
        $opt_right = array();
        foreach ($opt as $val) {
            if (in_array($val['value'], $model->field["SORT_SELECTED_HIDDEN"])) {
                continue;
            }
            $opt_right[] = array('label' => $val['label'],
                                 'value' => $val['value']);
        }

        //選択ソート一覧の項目を作成
        $opt_left = array();
        foreach ($model->field["SORT_SELECTED_HIDDEN"] as $val) {
            $opt_left[] = array('label' => $opt_mst[$val],
                                'value' => $val);
        }
        //ソート一覧
        $extra = "multiple style=\"width:180px;height:75px;\" width:\"180px\" ondblclick=\"move1('left', 'sort')\"";
        $arg["data"]["SORT_NAME"] = knjCreateCombo($objForm, "SORT_NAME", $value, isset($opt_right) ? $opt_right : array(), $extra, 15);

        //選択ソート一覧
        $extra = "multiple style=\"width:180px;height:75px;\" width:\"180px\" ondblclick=\"move1('right', 'sort')\"";
        $arg["data"]["SORT_SELECTED"] = knjCreateCombo($objForm, "SORT_SELECTED", $value, isset($opt_left) ? $opt_left : array(), $extra, 15);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'sort');\"";
        $arg["button"]["btn_right1_sort"] = knjCreateBtn($objForm, "btn_right1_sort", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'sort');\"";
        $arg["button"]["btn_left1_sort"] = knjCreateBtn($objForm, "btn_left1_sort", "＜", $extra);

        //保護者、住所、電話番号のチェックボックス
        $extra = ($model->field["hogosya"] == "on") ? "checked" : "";
        $extra .= " id=\"hogosya\"";
        $arg["hogosya"] = knjCreateCheckBox($objForm, "hogosya", "on", $extra, "");

        //退部者除くのチェックボックス
        $extra = ($model->field["taibusya_nozoku"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"taibusya_nozoku\"";
        $arg["taibusya_nozoku"] = knjCreateCheckBox($objForm, "taibusya_nozoku", "on", $extra, "");

        //対象期間
        $arg["FROM_DATE"] = View::popUpCalendar($objForm, "FROM_DATE", str_replace("-", "/", CTRL_DATE), "");
        $arg["TO_DATE"] = View::popUpCalendar($objForm, "TO_DATE", str_replace("-", "/", CTRL_DATE), "");

        //帳票パターン
        if ($model->schoolName != "sundaikoufu") {
            $arg["show_pattern"] = "1";
        }
        $opt = array(1, 2);
        $model->field["PATTERN"] = ($model->field["PATTERN"] == "") ? "1" : $model->field["PATTERN"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"PATTERN{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "PATTERN", $model->field["PATTERN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //ＣＳＶ出力
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJJ060");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectsort");
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);

        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj060Form1.html", $arg);
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
