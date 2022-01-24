<?php

require_once('for_php7.php');

class knjp713Form2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjp713index.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージが表示される場合、またはリロードした場合
        if ($model->cmd == "grpChange") {
            $query = knjp713Query::getGrpData($model->taisyouYear, $model);
            $dataAri = $db->getOne($query);
        }
        if (isset($model->warning) || $model->cmd == "reload" || $model->cmd == "change2" ||
            ($model->cmd == "grpChange" && $dataAri == 0)
        ) {
            $Row =& $model->field;
        } else {
            $query = knjp713Query::getRow($model, 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //課程学科
        $query = knjp713Query::getMajor($model);
        $extra = "onChange=\"btn_submit('grpChange')\";";
        makeCmb($objForm, $arg, $db, $query, $Row["MAJORCD"], "MAJORCD", $extra, 1);

        //コース
        $query = knjp713Query::getCourse($model);
        $extra = "onChange=\"btn_submit('grpChange')\";";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSECODE"], "COURSECODE", $extra, 1);

        //会計科目コードコンボ
        $query = knjp713Query::getCollectLcd();
        $extra = "onChange=\"btn_submit('grpChange')\";";
        $value = substr($Row["COLLECT_GRP_CD"], 0, 2);
        makeCmb($objForm, $arg, $db, $query, $value, "COLLECT_L_CD", $extra, 1);

        //会計グループコード
        $extra = "onblur=\"this.value=toInteger(this.value);\" onChange=\"btn_submit('grpChange')\";";
        $arg["data"]["COLLECT_GRP_CD"] = knjCreateTextBox($objForm, substr($Row["COLLECT_GRP_CD"], 2), "COLLECT_GRP_CD", 2, 2, $extra);

        //会計グループ名称
        $extra = "";
        $arg["data"]["COLLECT_GRP_NAME"] = knjCreateTextBox($objForm, $Row["COLLECT_GRP_NAME"], "COLLECT_GRP_NAME", 25, 40, $extra);

        //会計項目割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $query = knjp713Query::ReloadSelectMcd($model);
        } else {
            $query = knjp713Query::GetSelectMcd($model);
        }
        $result = $db->query($query);

        $opt_left = $opt_right = $tempcd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]."：".htmlspecialchars($row["COLLECT_M_NAME"]);
            $opt_left[]  = array("label" => $label, "value" => $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]);

            $tempcd[] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"];
        }

        //会計項目一覧
        $result = $db->query(knjp713Query::GetMcd($model, $tempcd));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]."：".htmlspecialchars($row["COLLECT_M_NAME"]);
            $opt_right[]  = array("label" => $label, "value" => $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]);
        }

        //会計項目に関するオブジェクトを作成
        $extraLeft = "ondblclick=\"move1('right','left_expmcd','right_expmcd',1,2);\"";
        $extraRight = "ondblclick=\"move1('left','left_expmcd','right_expmcd',1,2);\"";
        $arg["expmcd"] = array( "LEFT_LIST"   => "会計項目割当一覧",
                                "RIGHT_LIST"  => "会計項目一覧",
                                "LEFT_PART"   => knjCreateCombo($objForm, "left_expmcd", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:130px\"", 15),
                                "RIGHT_PART"  => knjCreateCombo($objForm, "right_expmcd", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:130px\"", 15),
                                "SEL_ADD_ALL" => knjCreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return move1('sel_add_all2','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_ADD"     => knjCreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL"     => knjCreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL_ALL" => knjCreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return move1('sel_del_all2','left_expmcd','right_expmcd',1,2);\"")
                              );


        //会計細目割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $query = knjp713Query::ReloadSelectScd($model);
        } else {
            $query = knjp713Query::GetSelectScd($model);
        }
        $result = $db->query($query);

        $opt_left = $opt_right = $tempcd2 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"]."：".htmlspecialchars($row["COLLECT_S_NAME"]);
            $val = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"];

            $opt_left[]  = array("label" => $label, "value" => $val);
            $tempcd2[] = $val;
        }

        //会計細目一覧
        $result = $db->query(knjp713Query::GetScd($model, $tempcd, $tempcd2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"]."：".htmlspecialchars($row["COLLECT_S_NAME"]);
            $val = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"];
            $opt_right[]  = array("label" => $label, "value" => $val);
        }

        //会計細目に関するオブジェクトを作成
        $extraLeft = "ondblclick=\"move1('right','left_expscd','right_expscd',1,3);\"";
        $extraRight = "ondblclick=\"move1('left','left_expscd','right_expscd',1,3);\"";
        $arg["expscd"] = array( "LEFT_LIST"   => "会計細目割当一覧",
                                "RIGHT_LIST"  => "会計細目一覧",
                                "LEFT_PART"   => knjCreateCombo($objForm, "left_expscd", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:130px\"", 15),
                                "RIGHT_PART"  => knjCreateCombo($objForm, "right_expscd", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:130px\"", 15),
                                "SEL_ADD_ALL" => knjCreateBtn($objForm, "sel_add_all3", "≪", "onclick=\"return move1('sel_add_all3','left_expscd','right_expscd',1,3);\""),
                                "SEL_ADD"     => knjCreateBtn($objForm, "sel_add3", "＜", "onclick=\"return move1('left','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL"     => knjCreateBtn($objForm, "sel_del3", "＞", "onclick=\"return move1('right','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL_ALL" => knjCreateBtn($objForm, "sel_del_all3", "≫", "onclick=\"return move1('sel_del_all3','left_expscd','right_expscd',1,3);\"")
                              );

        //追加
        $extra = "onclick=\"return doSubmit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = "onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knjp713index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp713Form2.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

?>
