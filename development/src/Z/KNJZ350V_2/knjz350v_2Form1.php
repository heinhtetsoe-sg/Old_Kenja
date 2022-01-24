<?php

require_once('for_php7.php');

class knjz350v_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz350v_2index.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $opt[0] = array("label" => $model->year, "value" => $model->year);

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjz350v_2Query::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "");

        //算出先コンボ
        $extra = "onChange=\"btn_submit('')\";";
        $query = knjz350v_2Query::selectTestQuery($db, $model);
        makeCmb($objForm, $arg, $db, $query, "SAKI_TESTCD", $model->field["SAKI_TESTCD"], $extra, 1, "");

        //対象データ表示
        $query = knjz350v_2Query::selectListQuery($db, $model);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //自動換算しない
        $extra = "id=\"NOT_AUTO_KANSAN_FLG\"";
        if ($Row["NOT_AUTO_KANSAN_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["NOT_AUTO_KANSAN_FLG"] = knjCreateCheckBox($objForm, "NOT_AUTO_KANSAN_FLG", "1", $extra);
        
        //換算後上書きする
        $extra = "id=\"KANSAN_AFTER_UPDATE_FLG\"";
        if ($Row["KANSAN_AFTER_UPDATE_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["KANSAN_AFTER_UPDATE_FLG"] = knjCreateCheckBox($objForm, "KANSAN_AFTER_UPDATE_FLG", "1", $extra);

        //DB切断
        Query::dbCheckIn($db);

        $arg["year"] = array( "VAL"       => $model->year );
        
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        
        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
                
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350v_2Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SAKI_TESTCD") {
        $value = ($value && $value_flg) ? $value : 9990009;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
