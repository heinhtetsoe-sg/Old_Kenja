<?php

require_once('for_php7.php');


class knjl322kForm1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl322kForm1", "POST", "knjl322kindex.php", "", "knjl322kForm1");
    $db = Query::dbCheckOut();

    //年度
    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl322kQuery::GetTestdiv($model));
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
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onchange=\" return btn_submit('knjl322k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl322kQuery::getSpecialReasonDiv($model);
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

    //試験科目を作成する
    $opt_subclass = array();
    $subclasscnt = 0;

    $result = $db->query(knjl322kQuery::GetSubclass($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_subclass[] = array("label" => $row["NAME1"],
                                "value" => $row["NAMECD2"]);
        $subclasscnt++;
    }
    $opt_subclass[$subclasscnt] = array("label" => "全て",
                                    "value" => "99");

    if (!$model->subclass) $model->subclass = $opt_subclass[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "SUBCLASS",
                        "size"      => 1,
                        "value"     => $model->subclass,
                        "extrahtml" => "",
//                      "extrahtml" => "onchange=\" return btn_submit('knjl322k');\"",
                        "options"   => $opt_subclass ) );

    $arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");

    //切替ラジオ（1:会場,2:受験番号）
    $opt[0]=1;
    $opt[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => "onclick =\" return btn_submit('knjl322k');\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

    if ($model->output == 1) $arg["clsno"] = $model->output;
    if ($model->output == 2) $arg["schno"] = $model->output;

    //一覧リスト作成する
    $opt_data = array();

    if ($model->output == 1) $query = knjl322kQuery::GetExamHallcd($model);
    if ($model->output == 2) $query = knjl322kQuery::GetExamno($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_data[] = array("label" => $row["CD"]."　".$row["NAME"],
                            "value" => $row["CD"]);
    }

    $result->free();

    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_NAME",
                        "extrahtml"  => "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left')\"",
                        "size"       => "20",
                        "options"    => $opt_data));

    $arg["data"]["DATA_NAME"] = $objForm->ge("DATA_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_SELECTED",
                        "extrahtml"  => "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right')\"",
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

    //改ページチェックボックス
    $checked = "";
    if ($model->pchange == "on"){
        $checked = "checked";
    }
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "PCHANGE",
                        "extrahtml" => $checked,
                        "value"     => "on"));

    $arg["data"]["PCHANGE"] = $objForm->ge("PCHANGE");

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl322kQuery::GetJorH());
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
                        "value"     => "KNJL322K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl322kForm1.html", $arg); 
    }
}
?>
