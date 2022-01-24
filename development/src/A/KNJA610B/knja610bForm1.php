<?php

require_once('for_php7.php');

class knja610bForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja610bForm1", "POST", "knja610bindex.php", "", "knja610bForm1");

        $opt=array();

        //年度コンボボックスを作成する/////////////////////////////////////
        $db = Query::dbCheckOut();

        $opt_year=array();
        $query = knja610bQuery::getSelectYear();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[]= $row["YEAR"];
        }
        if($model->field["YEAR"]=="") $model->field["YEAR"] = $model->control["年度"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "YEAR",
                            "size"       => "1",
                            "value"      => $model->field["YEAR"],
                            "extrahtml"  => "onChange=\"btn_submit('init');\"",
                            "options"    => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //学期コンボボックスを作成する/////////////////////////////////////
        $opt_sem = array();
        $db = Query::dbCheckOut();
        $query = knja610bQuery::getSelectSeme($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sem[]= array('label' => $row["SEMESTERNAME"],
                              'value' => $row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if($model->field["GAKKI"]=="") $model->field["GAKKI"] = $model->control["学期"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"btn_submit('init');\"",
                            "options"    => $opt_sem));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //テスト種別リスト
        if(($model->tableName == 'TESTITEM_MST_COUNTFLG' && $model->field["GAKKI"] != "9") || $model->tableName == 'TESTITEM_MST_COUNTFLG_NEW'){
            $query = knja610bQuery::getTestItem($model);
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
            Query::dbCheckIn($db);
        }

        if($model->tableName == 'TESTITEM_MST_COUNTFLG'){
            $opt_kind[] = array('label' => '評価成績', 'value' => '9900');
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
        $db = Query::dbCheckOut();
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja610bQuery::getAuth($model);
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
        Query::dbCheckIn($db);

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


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja610bForm1.html", $arg); 
    }
}
?>
