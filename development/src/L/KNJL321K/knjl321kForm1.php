<?php

class knjl321kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl321kForm1", "POST", "knjl321kindex.php", "", "knjl321kForm1");

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;
    $db = Query::dbCheckOut();

    $row = $db->getOne(knjl321kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }

    $result = $db->query(knjl321kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    if ($testcnt == 0){
        $opt_testdiv[$testcnt] = array("label" => "　　",
                                       "value" => "99");
    }else {
        $opt_testdiv[$testcnt] = array("label" => "全て",
                                       "value" => "99");
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();

    $objForm->ae( array("type"         => "select",
                        "name"        => "TESTDIV",
                        "size"        => 1,
                        "value"        => $model->testdiv,
                        "extrahtml"    => "onchange=\" return btn_submit('knjl321k');\"",
                        "options"    => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl321kQuery::getSpecialReasonDiv($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($model->special_reason_div == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE1"] == '1') {
            $special_reason_div = $row["VALUE"];
        }
    }
    $model->special_reason_div = (strlen($model->special_reason_div) && $value_flg) ? $model->special_reason_div : $special_reason_div;
    $extra = "";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    //会場を作成する NO001
    $opt_hallL = array();
    $opt_hallR = array();

    $result = $db->query(knjl321kQuery::GetExamHallcd($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if (in_array($row["EXAMHALLCD"], $model->selected)) {
            $opt_hallL[] = array("label" => $row["EXAMHALL_NAME"],
                                 "value" => $row["EXAMHALLCD"]);
        } else {
            $opt_hallR[] = array("label" => $row["EXAMHALL_NAME"],
                                 "value" => $row["EXAMHALLCD"]);
        }
    }

    $result->free();

    $objForm->ae( array("type"       => "select",
                        "name"       => "HALL_NAME",
                        "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('left')\"",
                        "size"       => "20",
                        "options"    => $opt_hallR));

    $arg["data"]["HALL_NAME"] = $objForm->ge("HALL_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "HALL_SELECTED",
                        "extrahtml"  => "multiple style=\"width=180px\" width=\"180px\" ondblclick=\"move1('right')\"",
                        "size"       => "20",
                        "options"    => $opt_hallL));

    $arg["data"]["HALL_SELECTED"] = $objForm->ge("HALL_SELECTED");

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

    //タイトル
    $model->title = $jhflg == 1 ? $db->getOne(knjl321kQuery::GetTestdiv($model, $model->testdiv))."入学試験" : "入学試験";

    $objForm->ae( array("type"        => "text",
                        "name"        => "TITLE",
                        "size"        => 20,
                        "maxlength"   => 30,
                        "value"       => $model->title));
    $arg["data"]["TITLE"] = $objForm->ge("TITLE");

    //印刷ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => $model->ObjYear
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJL321K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SELECTED"
                        ) );

    //印刷処理
    $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
    $model->print = "off";

    Query::dbCheckIn($db);

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl321kForm1.html", $arg); 
    }
}
?>
