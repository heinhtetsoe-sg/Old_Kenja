<?php

class knjl351kForm1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl351kForm1", "POST", "knjl351kindex.php", "", "knjl351kForm1");
    $db = Query::dbCheckOut();

    //年度
    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl351kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    if ($testcnt == 0){
        $opt_testdiv[$testcnt] = array("label" => "　　",
                                       "value" => "99");
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "",
//                      "extrahtml" => "onchange=\" return btn_submit('knjl351k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl351kQuery::getSpecialReasonDiv($model);
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

    //切替ラジオ（1:出身学校,2:出身塾）
    $opt[0]=1;
    $opt[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "extrahtml"  => "onclick =\" return btn_submit('knjl351k');\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2",2);

    if ($model->output2 == 1) $arg["schoolno"] = $model->output2;
    if ($model->output2 == 2) $arg["prino"] = $model->output2;

    //一覧リスト作成する
    $opt_data = array();

    if ($model->output2 == 1){
        $query = knjl351kQuery::GetFinschool($model);
    }else {
        $query = knjl351kQuery::GetPrischool($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_data[] = array("label" => $row["OUTCD"]."　".$row["OUTNAME"],
                            "value" => $row["OUTCD"]);
    }

    $result->free();

    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_NAME",
                        "extrahtml"  => "multiple style=\"width=270px\" width=\"270px\" ondblclick=\"move1('left')\"",
                        "size"       => "20",
                        "options"    => $opt_data));

    $arg["data"]["DATA_NAME"] = $objForm->ge("DATA_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_SELECTED",
                        "extrahtml"  => "multiple style=\"width=270px\" width=\"270px\" ondblclick=\"move1('right')\"",
                        "size"       => "20",
                        "options"    => array()));

    $arg["data"]["DATA_SELECTED"] = $objForm->ge("DATA_SELECTED");

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

    //通知承諾可のみチェックボックスを作成する
    //NO001
/*
    $chk = ($model->output == "on" || $model->cmd == "") ? "checked" : "" ;

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT",
                        "value"     => "on",
                        "extrahtml" => $chk ) );

    $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");
*/
    //印刷日付
    if ($model->date == "") $model->date = str_replace("-","/",CTRL_DATE);
    $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->date);

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl351kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //印刷ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

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
                        "value"     => "KNJL351K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //csv
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata") );  

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl351kForm1.html", $arg); 
    }
}
?>
