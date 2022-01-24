<?php

require_once('for_php7.php');


class knjd328Form1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjd328Form1", "POST", "knjd328index.php", "", "knjd328Form1");

    //年度
    $arg["data"]["YEAR"] = CTRL_YEAR;

    //切替ラジオ（1:出身学校,2:出身塾）
    $opt[0]=1;
    $opt[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "extrahtml"  => "onclick =\" return btn_submit('knjd328');\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2",2);

    if ($model->output2 == 1) $arg["prino"] = $model->output2;
    if ($model->output2 == 2) $arg["schoolno"] = $model->output2;

    //中高判別フラグを作成する
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjd328Query::GetJorH());
    if ($row == 1) {
        $model->jhflg = 1;
    } else {
        $model->jhflg = 2;
    }
    Query::dbCheckIn($db);
    $objForm->ae( array("type"  => "hidden",
                        "name"  => "JHFLG",
                        "value" => $model->jhflg ) );

    //一覧リスト作成する
    $opt_data = array();
    $db = Query::dbCheckOut();

    if ($model->output2 == 2) {
        $query = knjd328Query::GetFinschool($model);
    } else {
        $query = knjd328Query::GetPrischool($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_data[] = array("label" => $row["OUTCD"]."　".$row["OUTNAME"],
                            "value" => $row["OUTCD"]);
    }

    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_NAME",
                        "extrahtml"  => "multiple style=\"width:270px\" width=\"270px\" ondblclick=\"move1('left')\"",
                        "size"       => "20",
                        "options"    => $opt_data));

    $arg["data"]["DATA_NAME"] = $objForm->ge("DATA_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_SELECTED",
                        "extrahtml"  => "multiple style=\"width:270px\" width=\"270px\" ondblclick=\"move1('right')\"",
                        "size"       => "20",
                        "options"    => array()));

    $arg["data"]["DATA_SELECTED"] = $objForm->ge("DATA_SELECTED");

    //対象選択ボタンを作成する（全部）
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_rights",
                        "value"     => ">>",
                        "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

    $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


    //対象取消ボタンを作成する（全部）
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_lefts",
                        "value"     => "<<",
                        "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

    $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

    //対象選択ボタンを作成する（一部）
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_right1",
                        "value"     => "＞",
                        "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

    $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

    //対象取消ボタンを作成する（一部）
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_left1",
                        "value"     => "＜",
                        "extrahtml" => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

    $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

    //印刷ボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_print",
                        "value"     => "プレビュー／印刷",
                        "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //CSVボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_csv",
                        "value"     => "ＣＳＶ出力",
                        "extrahtml" => "onclick=\"return btn_submit('csv');\"" ) );

    $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

    //終了ボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_end",
                        "value"     => "終 了",
                        "extrahtml" => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => CTRL_YEAR
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "GAKKI",
                        "value"     => CTRL_SEMESTER
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "GAKKISU",
                        "value"     => $model->control["学期数"]
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJD328"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //csv
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata") );  

    //教育課程対応（帳票）
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjd328Form1.html", $arg); 
    }
}
?>
