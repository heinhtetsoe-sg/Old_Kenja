<?php

require_once('for_php7.php');

class knjd619aForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd619aForm1", "POST", "knjd619aindex.php", "", "knjd619aForm1");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度コンボボックスを作成する/////////////////////////////////////
        $query = knjd619aQuery::getSelectYear();
        $extra = "onchange=\"return btn_submit('init');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjd619aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('init');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);
        }

        //学期コンボボックスを作成する/////////////////////////////////////
        $opt_sem = array();
        $query = knjd619aQuery::getSelectSeme($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sem[]= array('label' => $row["SEMESTERNAME"],
                              'value' => $row["SEMESTER"]);
        }
        $result->free();

        if($model->field["GAKKI"]=="") $model->field["GAKKI"] = $model->control["学期"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"btn_submit('init');\"",
                            "options"    => $opt_sem));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knjd619aQuery::getCourseMajor($model);
            $extra = "onChange=\"btn_submit('init')\";";
            makeCmb($objForm, $arg, $db, $query, $model->field["COURSE_MAJOR"], "COURSE_MAJOR", $extra, 1);
        }

        //テスト種別リスト
        if ($model->field["GAKKI"] != "9" || in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            $query = knjd619aQuery::getTestItem($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
        }

        if (!in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            if($model->tableName == 'TESTITEM_MST_COUNTFLG'){
                $opt_kind[] = array('label' => '評価成績', 'value' => '9900');
            } else if ($model->field["GAKKI"] == "9") {
                $opt_kind[] = array('label' => '学年評定', 'value' => '9900');
                $opt_kind[] = array('label' => '学年評価', 'value' => '9901');
            }
        }

        //出欠集計日付
        $model->field["ATTENDDATE"] = ($model->field["ATTENDDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["ATTENDDATE"];
        $arg["data"]["ATTENDDATE"] = View::popUpCalendar2($objForm, "ATTENDDATE", $model->field["ATTENDDATE"], "", "", "");

        //初期化処理
        $test_flg = false;
        for ($i=0; $i<get_count($opt_kind); $i++) 
            if ($model->field["TESTKINDCD"] == $opt_kind[$i]["value"]) $test_flg = true;
        if (!$test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "options"    => $opt_kind));

        if ($opt_kind != null) {
            $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");
        } else {
            $arg["data"]["TESTKINDCD"] = "該当データなし";
        }

        //クラス一覧リスト作成する///////////////////////////////////////////////////////////////////////////////
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knjd619aQuery::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["VALUE"], $model->select_data["selectdata"])){
                $opt_class_right[]= array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
            } else {
                $opt_class_left[]= array('label' => $row["LABEL"],
                                        'value' => $row["VALUE"]);
            }
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => $opt_class_right));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する///////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        if (in_array($model->tableName, array('TESTITEM_MST_COUNTFLG_NEW_SDIV', 'TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV'))) {
            //欠席者は「*」を印字する
            $extra  = ($model->field["KESSEKI_FLG"] == '1') ? "checked " : "";
            $extra .= " id=\"KESSEKI_FLG\"";
            $arg["data"]["KESSEKI_FLG"] = knjCreateCheckBox($objForm, "KESSEKI_FLG", "1", $extra);
        }

        //csvボタンを作成する
        $extra = ($model->field["TESTKINDCD"]) ? "onclick=\"return btn_submit('csv');\"" : "disabled";
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => $extra ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

            $objForm->ae( array("type"      => "hidden",
                                "name"      => "selectdata") );  

        //データベース接続切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd619aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
