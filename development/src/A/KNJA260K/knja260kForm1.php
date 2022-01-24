<?php

require_once('for_php7.php');

class knja260kForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja260kForm1", "POST", "knja260kindex.php", "", "knja260kForm1");

        $opt=array();

        //年度コンボボックスを作成する
        $db = Query::dbCheckOut();

        $opt_year=array();
        $query = knja260kQuery::getSelectYear();
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

        //学期コンボボックスを作成する
        $opt_sem = array();
        $db = Query::dbCheckOut();
        $query = knja260kQuery::getSelectSeme($model->field["YEAR"]);
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
        $hyoutei_flg = "";//2005.09.30
        if (knja260kQuery::GetJorH($model->field["YEAR"])) $hyoutei_flg = "on";//2005.09.30
        $model->jhflg = $hyoutei_flg;    //NO001

        if($model->field["GAKKI"] < 3 ){
            $opt_kind[]= array('label' => '中間試験',
                               'value' => '01');
            $opt_kind[]= array('label' => '期末試験',
                               'value' => '02');
            $opt_kind[]= array('label' => '学期成績',
                               'value' => '0');
            if ($hyoutei_flg == "on") {
                $opt_kind[]= array('label' => '学期評定', 'value' => '88');
                $opt_kind[]= array('label' => '学年評定', 'value' => '99');//2005.09.30
            }
        }else if ($model->field["GAKKI"] == 3 ){
            $opt_kind[]= array('label' => '学年末試験',
                               'value' => '02');
            /*
            $opt_kind[]= array('label' => '学期成績',
                               'value' => '0');
            */
            
            if ($hyoutei_flg == "on") {
                $opt_kind[]= array('label' => '学期評定', 'value' => '88');
            /*
                $opt_kind[]= array('label' => '学年評定', 'value' => '99');//2005.09.30
            */
            }
        }else{
            $opt_kind[]= array('label' => '学年成績',
                               'value' => '9');
            if ($hyoutei_flg == "on") {
                $opt_kind[]= array('label' => '学年評定', 'value' => '99');//2005.09.30
            }
        }

        //初期化処理
        $test_flg = false;
        for ($i=0; $i<get_count($opt_kind); $i++) 
            if ($model->field["TESTKINDCD"] == $opt_kind[$i]["value"]) $test_flg = true;
        if (!$test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "extrahtml"  => "",
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //クラス一覧リスト作成する
        $db = Query::dbCheckOut();
        $opt_class_left = $opt_class_right = array();
        $opt_class=array();
        $query = knja260kQuery::getAuth($model);
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
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => $opt_class_right));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
                            "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => $opt_class_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //echo $model->field["TESTKINDCD"];

        //csvボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "ＣＳＶ出力",
                            "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );
        //                    "extrahtml"   => "onclick=\"return newwin();\"" ) );

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
                            "value"     => DB_DATABASE
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );  


        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja260kForm1.html", $arg); 
    }
}
?>
