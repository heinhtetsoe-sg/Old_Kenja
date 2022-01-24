<?php

require_once('for_php7.php');

class knjz069Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz069index.php", "", "sel");
        $db = Query::dbCheckOut();
        
        //年度設定
        $arg["YEAR"] = CTRL_YEAR;

        $extra = "onchange=\"btn_submit('changeCombo')\"";
        $query = knjz069Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        $extra = "onchange=\"btn_submit('changeCombo')\"";
        $query = knjz069Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        $extra = "onchange=\"btn_submit('changeCombo')\"";
        $query = knjz069Query::getSubclass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1);

        $opt = array(
            array("value" => "1",    "label" => "1：Outputs"),
            array("value" => "2",    "label" => "2：Skills"),
        );
        $extra = "onchange=\"btn_submit('changeCombo')\"";
        $model->field["ELEMENT_DIV"] = $model->field["ELEMENT_DIV"] ? $model->field["ELEMENT_DIV"] : "1";
        $arg["ELEMENT_DIV"] = knjCreateCombo($objForm, "ELEMENT_DIV", $model->field["ELEMENT_DIV"], $opt, $extra, 1);

        //対象一覧取得
        $query = knjz069Query::selectQuery($model);
        $result = $db->query($query);
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        
        //評価の要素一覧取得
        $query = knjz069Query::selectSubclassQuery($opt_left_id, $model);
        $result = $db->query($query);
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //対象一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_list",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_left));

        //評価の要素一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_list",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"moves('left');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"move1('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"move1('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"moves('right');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_list"),
                                   "RIGHT_PART"  => $objForm->ge("right_list"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
                                      
        //評価の要素マスタボタン
        $link = REQUESTROOT."/Z/KNJZ069_2/knjz069_2index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => " 評価の要素マスタ ",
                            "extrahtml"   => "onclick=\"document.location.href='$link'\"") ); 

        //保存ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_MASTER"  =>$objForm->ge("btn_master"),
                               "BTN_OK"      =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"   =>$objForm->ge("btn_clear"),
                               "BTN_END"     =>$objForm->ge("btn_end"),
                              );
        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_Z069/knjx_z069index.php','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["BTN_CSV"] = KnjCreateBtn($objForm, "btn_csv", "CSV入出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "firstData");
        knjCreateHidden($objForm, "rightMoveData");
        $arg["info"] = array("LEFT_LIST"  => "対象一覧",
                             "RIGHT_LIST" => "評価の要素一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["jscript"] = " setFirstData(); ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "sel.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
            "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
            "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
