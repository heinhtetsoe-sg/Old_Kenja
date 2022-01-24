<?php

require_once('for_php7.php');

class knjz210tForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz210tindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "edit2" && $model->subclasscd){
            $query = knjz210tQuery::getChkChrList($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        //科目コンボ作成
        $query = knjz210tQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('edit2');\"";
        if (VARS::request("cmd") != 'update') {
//            $model->subclasscd = $Row["SUBCLASSCD"];
        }
        //教育課程対応
        if (($model->Properties["useCurriculumcd"] == '1') && (strlen($model->subclasscd) == 6)) {
            $model->subclasscd = $model->classcd.'-'.$model->school_kind.'-'.$model->curriculum_cd.'-'.$model->subclasscd;
        }
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->subclasscd, $extra, 1, $model);

        //区分表示
        $query = knjz210tQuery::getSemesterName($model);
        $semester_name = $db->getOne($query);   //学期名

        $query = knjz210tQuery::getTestName($model);
        $test_name = $db->getOne($query);       //テスト名

        $arg["RECORD_DIV"] = ($model->record_div == "2") ? "{$semester_name} - {$test_name}(評価・評定)" : "{$semester_name} - {$test_name}(素点)";

        //対象講座一覧（左）
        $chaircd = $option = array();
        $result = $db->query(knjz210tQuery::getChkChrList($model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["CLASSCD"] ."_". $row["SCHOOL_KIND"] ."_". $row["CURRICULUM_CD"] ."_". $row["SUBCLASSCD"] ."_". $row["CHAIRCD"] ."_". $row["EXECUTED"]);

                $chaircd[] = $row["CHAIRCD"];
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["SUBCLASSCD"] ."_". $row["CHAIRCD"] ."_". $row["EXECUTED"]);

                $chaircd[] = $row["CHAIRCD"];
            }
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "left_chaircd",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right','left_chaircd','right_chaircd',1);\"",
                            "options"     => $option));

        //講座一覧（右）
        $option = array();
        $result = $db->query(knjz210tQuery::getChairList($model, $chaircd));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["CLASSCD"] ."_". $row["SCHOOL_KIND"] ."_". $row["CURRICULUM_CD"] ."_". $row["SUBCLASSCD"] ."_". $row["CHAIRCD"]."_0");
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                   "value" => $row["SUBCLASSCD"] ."_". $row["CHAIRCD"]."_0");
            }
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_chaircd",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left','left_chaircd','right_chaircd',1);\"",
                            "options"     => $option));

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "style=\"height:20px;width:28px\" onclick=\"return move1('sel_add_all','left_chaircd','right_chaircd',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:28px\" onclick=\"return move1('left','left_chaircd','right_chaircd',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:28px\" onclick=\"return move1('right','left_chaircd','right_chaircd',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "style=\"height:20px;width:28px\" onclick=\"return move1('sel_del_all','left_chaircd','right_chaircd',1);\"" ) );

        $arg["chair"] = array( "LEFT_LIST"   => "入力完了講座一覧",
                               "RIGHT_LIST"  => "入力完了講座候補一覧",
                               "LEFT_PART"   => $objForm->ge("left_chaircd"),
                               "RIGHT_PART"  => $objForm->ge("right_chaircd"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update") {
            $arg["jscript"] = "window.open('knjz210tindex.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz210tForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = '') {
    $opt = array();
    $value_flg = false;
    if ($name == 'SUBCLASSCD') {
        $opt[] = array('label' => '　全て', 'value' => 'XXXXXX'); //999999の科目コードがあると困るのでXXXXXXにる。
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //合併科目を区別して表示
        if ($name == 'SUBCLASSCD') {
            if ($row["VALUE"] !== $row["COMBINED_VALUE"]) {
                $opt[] = array('label' => "　".$row["LABEL"],
                               'value' => $row["VALUE"]);
            } else {
                $opt[] = array('label' => "●".$row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
