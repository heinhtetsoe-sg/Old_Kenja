<?php

require_once('for_php7.php');


class knjz403Form2{

    function main(&$model){
    
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz403index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "edit2") {
            $Row = knjz403Query::getRow($model->grade, $model->code);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        //学年コンボ
        $query = knjz403Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $Row["GRADE"], $extra, 1);

        //行動の記録コード
        $extra = "";
        $arg["data"]["CODE"] = knjCreateTextBox($objForm, $Row["CODE"], "CODE", 2, 2, $extra);

        //行動の記録名称
        $extra = "";
        $arg["data"]["CODENAME"] = knjCreateTextBox($objForm, $Row["CODENAME"], "CODENAME", 20, 30, $extra);

        //観点名称
        $extra = "";
        $arg["data"]["VIEWNAME"] = knjCreateTextBox($objForm, $Row["VIEWNAME"], "VIEWNAME", ($model->lenval * 2), ($model->lenval * 3), $extra);

        //指導要録用行動の記録コードコンボ
        $query = knjz403Query::getStudyrecCode($Row["GRADE"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "STUDYREC_CODE", $Row["STUDYREC_CODE"], $extra, 1, "blank");

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"] ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2" && !isset($model->warning)) {
            $arg["reload"]  = "window.open('knjz403index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz403Form2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
