<?php

require_once('for_php7.php');

class knjf101aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf101aForm1", "POST", "knjf101aindex.php", "", "knjf101aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjf101aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjf101a');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        //キャンパス
        if ($model->Properties["use_select_nurseoffice"] == "1") {
            $arg["sel_nurseoffice"] = 1;
            $query = knjf101aQuery::getNameMst($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "CAMPUS_DIV", $model->field["CAMPUS_DIV"], $extra, 1);
        }

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];

        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);


        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];

        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $value2);

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $info = $db->getRow(knjf101aQuery::getSchoolMst($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        /******************/
        /* リストtoリスト */
        /******************/
        /**************************** ソート一覧リスト ****************************/
        $opt = array();
        $opt[]= array('label' => '来室種別' , 'value' => 'TYPE');
        $opt[]= array('label' => '学籍番号' , 'value' => 'SCHREGNO');
        $opt[]= array('label' => '来室日'   , 'value' => 'VISIT_DATE');
        $opt[]= array('label' => '年組番'   , 'value' => 'NEN_KUMI_BAN');

        $opt_mst = array('TYPE'         => '来室種別',
                         'SCHREGNO'     => '学籍番号',
                         'VISIT_DATE'   => '来室日',
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
        $extra = "multiple style=\"width:180px;height:75px;\" ondblclick=\"move1('left', 'sort')\"";
        $arg["data"]["SORT_NAME"] = knjCreateCombo($objForm, "SORT_NAME", "", isset($opt_right) ? $opt_right:array(), $extra, 15);

        //選択ソート一覧
        $extra = "multiple style=\"width:180px;height:75px;\" ondblclick=\"move1('right', 'sort')\"";
        $arg["data"]["SORT_SELECTED"] = knjCreateCombo($objForm, "SORT_SELECTED", "", isset($opt_left) ? $opt_left:array(), $extra, 15);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', 'sort');\"";
        $arg["button"]["btn_right1_sort"] = knjCreateBtn($objForm, "btn_right1_sort", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', 'sort');\"";
        $arg["button"]["btn_left1_sort"] = knjCreateBtn($objForm, "btn_left1_sort", "＜", $extra);

        /********************/
        /* チェックボックス */
        /********************/
        //内科
        $extra  = " id=\"NAIKA\"";
        $extra .= $model->field["NAIKA"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["NAIKA"] = knjCreateCheckBox($objForm, "NAIKA", "on", $extra);

        //外科
        $extra  = " id=\"GEKA\"";
        $extra .= $model->field["GEKA"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["GEKA"] = knjCreateCheckBox($objForm, "GEKA", "on", $extra);

        //健康相談
        $extra  = " id=\"KENKO_SODAN\"";
        $extra .= $model->field["KENKO_SODAN"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["KENKO_SODAN"] = knjCreateCheckBox($objForm, "KENKO_SODAN", "on", $extra);

        //その他
        $extra  = " id=\"SONOTA\"";
        $extra .= $model->field["SONOTA"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["SONOTA"] = knjCreateCheckBox($objForm, "SONOTA", "on", $extra);

        //生徒以外
        $extra  = " id=\"SEITO_IGAI\"";
        $extra .= $model->field["SEITO_IGAI"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["SEITO_IGAI"] = knjCreateCheckBox($objForm, "SEITO_IGAI", "on", $extra);

        //来室種別ごとの改ページあり
        $extra  = " id=\"CHECK1\"";
        $extra .= $model->field["CHECK1"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "on", $extra);

        //クラスごとの改ページあり
        $extra  = " id=\"CHECK2\"";
        $extra .= $model->field["CHECK2"] == "on" || $model->cmd == "" ? " checked" : "";
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "on", $extra);


        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");


        //終了ボタンを作成する
        $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");


        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF101A");
        knjCreateHidden($objForm, "cmd");
        //ログイン年度
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        //ログイン学期
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        //ログイン日付
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        //学期開始日
        knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR . "/04/01");
        //学期終了日
        knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR + 1) . "/03/31");
        //ソート順
        knjCreateHidden($objForm, "SORT_SELECTED_HIDDEN");

        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "use_select_nurseoffice", $model->Properties["use_select_nurseoffice"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf101aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
