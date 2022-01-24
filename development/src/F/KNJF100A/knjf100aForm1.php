<?php

require_once('for_php7.php');

class knjf100aForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf100aForm1", "POST", "knjf100aindex.php", "", "knjf100aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期コンボボックスを作成する
        $opt_seme = array();
        $query = knjf100aQuery::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        if($model->field["SEMESTER"]=="") $model->field["SEMESTER"] = CTRL_SEMESTER;
        $result->free();

        $extra = "onchange=\"return btn_submit('knjf100a');\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt_seme, $extra, 1);

        //カレンダーコントロール１
        $value = isset($model->field["DATE1"])?$model->field["DATE1"]:$model->control["学籍処理日"];

        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);


        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"])?$model->field["DATE2"]:$model->control["学籍処理日"];

        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);


        /******************/
        /* リストtoリスト */
        /******************/
        /**************************** クラス一覧リスト ****************************/
        $query = knjf100aQuery::getAuth($model->control["年度"],$model->field["SEMESTER"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //クラス一覧
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px;height:180px;\" width=\"180px\" ondblclick=\"move1('left', 'class')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));
        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px;height:180px;\" width=\"180px\" ondblclick=\"move1('right', 'class')\"",
                            "size"       => "15",
                            "options"    => array()));
        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right', 'class');\"" ) );
        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left', 'class');\"" ) );
        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right', 'class');\"" ) );
        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left', 'class');\"" ) );
        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

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
        $objForm->ae( array("type"       => "select",
                            "name"       => "SORT_NAME",
                            "extrahtml"  => "multiple style=\"width:180px;height:75px;\" width=\"180px\" ondblclick=\"move1('left', 'sort')\"",
                            "size"       => "15",
                            "options"    => isset($opt_right) ? $opt_right:array()));
        $arg["data"]["SORT_NAME"] = $objForm->ge("SORT_NAME");

        //選択ソート一覧
        $objForm->ae( array("type"       => "select",
                            "name"       => "SORT_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px;height:75px;\" width=\"180px\" ondblclick=\"move1('right', 'sort')\"",
                            "size"       => "15",
                            "options"    => isset($opt_left) ? $opt_left:array()));
        $arg["data"]["SORT_SELECTED"] = $objForm->ge("SORT_SELECTED");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1_sort",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right', 'sort');\"" ) );
        $arg["button"]["btn_right1_sort"] = $objForm->ge("btn_right1_sort");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1_sort",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left', 'sort');\"" ) );
        $arg["button"]["btn_left1_sort"] = $objForm->ge("btn_left1_sort");


        /********************/
        /* チェックボックス */
        /********************/
        //内科
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "NAIKA",
                            "checked"    => ($model->field["NAIKA"]=="on")?true:false,
                            "value"      => "on"));
        $arg["data"]["NAIKA"] = $objForm->ge("NAIKA");

        //外科
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "GEKA",
                            "checked"    => ($model->field["GEKA"]=="on")?true:false,
                            "value"      => "on"));
        $arg["data"]["GEKA"] = $objForm->ge("GEKA");

        //外科
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "KENKO_SODAN",
                            "checked"    => ($model->field["KENKO_SODAN"]=="on")?true:false,
                            "value"      => "on"));
        $arg["data"]["KENKO_SODAN"] = $objForm->ge("KENKO_SODAN");

        //その他
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SONOTA",
                            "checked"    => ($model->field["SONOTA"]=="on")?true:false,
                            "value"      => "on"));
        $arg["data"]["SONOTA"] = $objForm->ge("SONOTA");

        //生徒以外
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SEITO_IGAI",
                            "checked"    => ($model->field["SEITO_IGAI"]=="on")?true:false,
                            "value"      => "on"));
        $arg["data"]["SEITO_IGAI"] = $objForm->ge("SEITO_IGAI");

        //来室種別ごとの改ページあり
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "CHECK1",
                            "checked"    => ($model->field["CHECK1"]=="on" || $model->cmd == '') ? true : false,
                            "value"      => "on"));
        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //クラスごとの改ページあり
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "CHECK2",
                            "checked"    => ($model->field["CHECK2"]=="on" || $model->cmd == '') ? true : false,
                            "value"      => "on"));
        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");


        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        /**********/
        /* hidden */
        /**********/
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJF100A"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        //ログイン年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_YEAR",
                            "value"     => CTRL_YEAR
                            ) );
        //ログイン学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_SEMESTER",
                            "value"     => CTRL_SEMESTER
                            ) );
        //ログイン日付
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_DATE",
                            "value"     => CTRL_DATE
                            ) );
        //学期開始日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHK_SDATE",
                            "value"     => CTRL_YEAR . "/04/01"
                            ) );
        //学期終了日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHK_EDATE",
                            "value"     => (CTRL_YEAR + 1) . "/03/31"
                            ) );
        //ソート順
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SORT_SELECTED_HIDDEN"
                            ) );

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf100aForm1.html", $arg);
    }
}
?>
