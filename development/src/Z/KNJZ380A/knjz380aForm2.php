<?php

require_once('for_php7.php');
class knjz380aForm2
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz380aindex.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "knjz380aForm2")
    {
        $Row = knjz380aQuery::getRow($model);
    } else {
        $Row =& $model->field;
    }

    //学期コンボボックス作成
    $db     = Query::dbCheckOut();
    $opt_semester = array();
    $opt_semester[] = array("label" => "","value" => "");
    $result = $db->query(knjz380aQuery::getSemester($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_semester[] = array("label" => $row["SEMESTERNAME"],
                                "value" => $row["SEMESTER"]);
    }
    $result->free();

    //学期
    $objForm->ae( array("type"        => "select",
                        "name"        => "SEMESTER",
                        "value"       => $Row["SEMESTER"],
                        "extrahtml"   => "onChange=\"btn_submit('knjz380aForm2')\"",
                        "options"     => $opt_semester
                        ));

    $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

    //テスト項目種別名コンボボックスの中身を作成------------------------------
    $query  = knjz380aQuery::getTestKindName($model);
    $result = $db->query($query);
    $opt_testKind = array();
    $opt_testKind[] = array("label" => "","value" => "");
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testKind[] = array("label" => htmlspecialchars(($row["TESTKINDCD"]." ".$row["TESTKINDNAME"])),
                                "value" => $row["TESTKINDCD"]);
    }

    //テスト項目種別
    $objForm->ae( array("type"        => "select",
                        "name"        => "TESTKINDCD",
                        "value"       => $Row["TESTKINDCD"],
                        "extrahtml"   => "",
                        "options"     => $opt_testKind
                        ));
    $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");
    $result->free();

    //テスト項目コード
    $objForm->ae( array("type"        => "text",
                        "name"        => "TESTITEMCD",
                        "size"        => 3,
                        "maxlength"   => 2,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => $Row["TESTITEMCD"] ));

    $arg["data"]["TESTITEMCD"] = $objForm->ge("TESTITEMCD");

    //テスト期間
    if ($model->Properties["Test_Period_Hyouji"] == "1") {
        $arg["Test_Period_Hyouji"] = "1";
        $arg["data"]["TEST_START_DATE"] = View::popUpCalendar($objForm, "TEST_START_DATE", str_replace("-", "/",  $Row["TEST_START_DATE"]));
        $arg["data"]["TEST_END_DATE"] = View::popUpCalendar($objForm, "TEST_END_DATE", str_replace("-", "/",  $Row["TEST_END_DATE"]));
    }

    //テスト項目名
    $objForm->ae( array("type"        => "text",
                        "name"        => "TESTITEMNAME",
                        "size"        => 20,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["TESTITEMNAME"] ));

    $arg["data"]["TESTITEMNAME"] = $objForm->ge("TESTITEMNAME");
    
    //集計フラグ 1:集計する 0:集計しない
	$checked_flg = ($Row["COUNTFLG"] == "1") ? "checked" : "";
    $objForm->ae( array("type"        => "checkbox",
                        "name"        => "COUNTFLG",
                        "extrahtml"   => $checked_flg,
                        "value"       => "1"));
    $arg["data"]["COUNTFLG"] = $objForm->ge("COUNTFLG");

    //出欠集計範囲表示切替
    if($model->Properties["Semester_Detail_Hyouji"] == "1") {
        $arg["sem_detail"] = 1;
    }

    //出欠集計範囲コンボボックス作成
    $opt = array();
    $opt[] = array("label" => "","value" => "");
    $result = $db->query(knjz380aQuery::getSemesterDetail($Row));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $sdate = str_replace("-","/",$row["SDATE"]);
        $edate = str_replace("-","/",$row["EDATE"]);
        $label = $row["SEMESTERNAME"];
        $value = "{$row["SEMESTER_DETAIL"]},{$sdate}～{$edate}";
        $opt[] = array("label" => "{$row["SEMESTER_DETAIL"]}：{$row["SEMESTERNAME"]}",
                       "value" => $row["SEMESTER_DETAIL"]);
    }
    $result->free();

    //出欠集計範囲
    $objForm->ae( array("type"        => "select",
                        "name"        => "SEMESTER_DETAIL",
                        "value"       => $Row["SEMESTER_DETAIL"],
                        "extrahtml"   => "onChange=\"btn_submit('knjz380aForm2')\"",
                        "options"     => $opt
                        ));
    $arg["data"]["SEMESTER_DETAIL"] = $objForm->ge("SEMESTER_DETAIL");

    $query = knjz380aQuery::getSemesterDetail_sdate_edate($Row);
    $s_e_date = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if (get_count($s_e_date)) {
        $arg["data"]["S_E_DATE"] = str_replace("-", "/", $s_e_date["SDATE"]) . ' ～ ' . str_replace("-", "/", $s_e_date["EDATE"]);
    }

    //追加ボタンを作成する
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_add",
                        "value"       => "追 加",
                        "extrahtml"   => "onclick=\"return doSubmit('add');\"" ) );

    $arg["button"]["btn_add"] = $objForm->ge("btn_add");

    //修正ボタンを作成する
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_udpate",
                        "value"       => "更 新",
                        "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );

    $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

    //削除ボタンを作成する
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_del",
                        "value"       => "削 除",
                        "extrahtml"   => "onclick=\"return doSubmit('delete');\"" ) );

    $arg["button"]["btn_del"] = $objForm->ge("btn_del");

    //クリアボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return doSubmit('reset')\"" ) );

    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "UPDATED",
                        "value"     => $Row["UPDATED"]
                        ) );

    $arg["finish"]  = $objForm->get_finish();

    if (VARS::get("cmd") != "edit" && $model->cmd != "knjz380aForm2"){
    }

    Query::dbCheckIn($db);
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz380aForm2.html", $arg);
    }
}
?>
