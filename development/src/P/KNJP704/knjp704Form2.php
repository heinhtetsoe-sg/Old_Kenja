<?php

require_once('for_php7.php');

class knjp704Form2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjp704index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学年コンボボックス
        $opt = array();
        $opt[] = array("label" => "新入生", "value" => "00");
        $value_flg = false;
        $query = knjp704Query::get_Grade();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->grade == $row["VALUE"]) $value_flg = true;
        }
        $model->grade2 = $model->grade2 ? $model->grade2 : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('reload')\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE2", $model->grade2, $opt, $extra, 1);

        //警告メッセージが表示される場合、またはリロードした場合
        if ($model->cmd == "grpChange") {
            $query = knjp704Query::getGrpData($model->exp_grpcd);
            $dataAri = $db->getOne($query);
        }
        if (isset($model->warning) || $model->cmd == "reload" || $model->cmd == "change2" ||
            ($model->cmd == "grpChange" && $dataAri == 0)
        ) {
            $Row =& $model->field;
        } else {
            $query = knjp704Query::getRow($model, 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        $arg["COLLECT_GRP_CD"] = ($model->exp_grpcd != "") ? $model->exp_grpcd.":".$model->name : "";

        //入金済みデータの件数
        $paid_money  = $db->getOne(knjp704Query::getPaidMoney($model->taisyouYear, "COLLECT_MONEY_PAID_M_DAT", $Row["COLLECT_GRP_CD"]));
        $paid_money += $db->getOne(knjp704Query::getPaidMoney($model->taisyouYear, "COLLECT_MONEY_PAID_S_DAT", $Row["COLLECT_GRP_CD"]));
        //hidden
        knjCreateHidden($objForm, "paidAri", $paid_money);

        $model->money_change_flg = "";
        if ($paid_money > 0) {
            $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','入金済みデータが存在します。');";
            $model->money_change_flg = "disabled";
        }

        //会計グループコード
        $setBtnDis = $paid_money > 0 ? "1" : "2";
        $extra = "onblur=\"this.value=toInteger(this.value);\" onChange=\"btn_submit('grpChange')\";";
        $textGroupCd = knjCreateTextBox($objForm, $Row["COLLECT_GRP_CD"], "COLLECT_GRP_CD", 4, 4, $extra);

        //会計グループ名称
        $extra = "";
        $textGroupName = knjCreateTextBox($objForm, $Row["COLLECT_GRP_NAME"], "COLLECT_GRP_NAME", 25, 40, $extra);
        $arg["VAL"]["COLLECT_GRP_CD"] = $textGroupCd."：".$textGroupName;

        //割当クラス一覧取得
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $result = $db->query(knjp704Query::ReloadSelectClass($model));
        } else {
            $result = $db->query(knjp704Query::GetSelectClass($model));
        }

        $opt_left = $opt_right = $tempcd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["GRADE"].$row["HR_CLASS"]."：".htmlspecialchars($row["HR_NAME"]);
            $opt_left[]  = array("label" => $label, "value" => $row["HR_CLASS"]);

            $tempcd[] = $row["HR_CLASS"];
        }

        //クラス一覧取得
        $result = $db->query(knjp704Query::GetClass($model, $tempcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["GRADE"].$row["HR_CLASS"]."：".htmlspecialchars($row["HR_NAME"]);
            $opt_right[]  = array("label" => $label, "value" => $row["HR_CLASS"]);
        }

        //ＨＲクラスに関するオブジェクトを作成                
        $arg["class"] = array( "LEFT_LIST"   => "割当クラス一覧",
                               "RIGHT_LIST"  => "クラス一覧",
                               "LEFT_PART"   => $this->CreateCombo($objForm, "left_classcd", "left", $opt_left, "ondblclick=\"move1('right','left_classcd','right_classcd',1,1);\""),
                               "RIGHT_PART"  => $this->CreateCombo($objForm, "right_classcd", "left", $opt_right, "ondblclick=\"move1('left','left_classcd','right_classcd',1,1);\""),
                               "SEL_ADD_ALL" => $this->CreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move1('sel_add_all','left_classcd','right_classcd',1,1);\""),
                               "SEL_ADD"     => $this->CreateBtn($objForm, "sel_add", "＜", "onclick=\"return move1('left','left_classcd','right_classcd',1,1);\""),
                               "SEL_DEL"     => $this->CreateBtn($objForm, "sel_del", "＞", "onclick=\"return move1('right','left_classcd','right_classcd',1,1);\""),
                               "SEL_DEL_ALL" => $this->CreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move1('sel_del_all','left_classcd','right_classcd',1,1);\""));

        //会計項目割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $query = knjp704Query::ReloadSelectMcd($model);
        } else {
            $query = knjp704Query::GetSelectMcd($model);
        }
        $result = $db->query($query);

        $opt_left = $opt_right = $tempcd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]."：".htmlspecialchars($row["COLLECT_M_NAME"]);
            $opt_left[]  = array("label" => $label, "value" => $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]);

            $tempcd[] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"];
        }

        //会計項目一覧
        $result = $db->query(knjp704Query::GetMcd($model, $tempcd));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]."：".htmlspecialchars($row["COLLECT_M_NAME"]);
            $opt_right[]  = array("label" => $label, "value" => $row["COLLECT_L_CD"].$row["COLLECT_M_CD"]);
        }

        //会計項目に関するオブジェクトを作成                
        $arg["expmcd"] = array( "LEFT_LIST"   => "会計項目割当一覧",
                                "RIGHT_LIST"  => "会計項目一覧",
                                "LEFT_PART"   => $this->CreateCombo($objForm, "left_expmcd", "left", $opt_left, "ondblclick=\"move1('right','left_expmcd','right_expmcd',1,2);\""),
                                "RIGHT_PART"  => $this->CreateCombo($objForm, "right_expmcd", "left", $opt_right, "ondblclick=\"move1('left','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_ADD_ALL" => $this->CreateBtn($objForm, "sel_add_all2", "≪", "onclick=\"return move1('sel_add_all2','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_ADD"     => $this->CreateBtn($objForm, "sel_add2", "＜", "onclick=\"return move1('left','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL"     => $this->CreateBtn($objForm, "sel_del2", "＞", "onclick=\"return move1('right','left_expmcd','right_expmcd',1,2);\""),
                                "SEL_DEL_ALL" => $this->CreateBtn($objForm, "sel_del_all2", "≫", "onclick=\"return move1('sel_del_all2','left_expmcd','right_expmcd',1,2);\"")
                              );


        //会計細目割当一覧
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $query = knjp704Query::ReloadSelectScd($model);
        } else {
            $query = knjp704Query::GetSelectScd($model);
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
        $result = $db->query(knjp704Query::GetScd($model, $tempcd, $tempcd2));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"]."：".htmlspecialchars($row["COLLECT_S_NAME"]);
            $val = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$row["COLLECT_S_CD"];
            $opt_right[]  = array("label" => $label, "value" => $val);
        }

        //会計細目に関するオブジェクトを作成
        $arg["expscd"] = array( "LEFT_LIST"   => "会計細目割当一覧",
                                "RIGHT_LIST"  => "会計細目一覧",
                                "LEFT_PART"   => $this->CreateCombo($objForm, "left_expscd", "left", $opt_left, "ondblclick=\"move1('right','left_expscd','right_expscd',1,3);\""),
                                "RIGHT_PART"  => $this->CreateCombo($objForm, "right_expscd", "left", $opt_right, "ondblclick=\"move1('left','left_expscd','right_expscd',1,3);\""),
                                "SEL_ADD_ALL" => $this->CreateBtn($objForm, "sel_add_all3", "≪", "onclick=\"return move1('sel_add_all3','left_expscd','right_expscd',1,3);\""),
                                "SEL_ADD"     => $this->CreateBtn($objForm, "sel_add3", "＜", "onclick=\"return move1('left','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL"     => $this->CreateBtn($objForm, "sel_del3", "＞", "onclick=\"return move1('right','left_expscd','right_expscd',1,3);\""),
                                "SEL_DEL_ALL" => $this->CreateBtn($objForm, "sel_del_all3", "≫", "onclick=\"return move1('sel_del_all3','left_expscd','right_expscd',1,3);\"")
                              );

        //追加
        $extra = " $model->money_change_flg onclick=\"return doSubmit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = " $model->money_change_flg onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = " $model->money_change_flg onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knjp704index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp704Form2.html", $arg);
    }


    //コンボボックス作成
    function CreateCombo(&$objForm, $name, $value, $opt, $extra)
    {
        $objForm->ae( array("type"        => "select",
                            "name"        => $name,
                            "size"        => "15",
                            "extrahtml"   => $extra." multiple style=\"WIDTH:100%; HEIGHT:130px\"",
                            "value"       => $value,
                            "options"     => $opt ) );
        return $objForm->ge($name);
    }
    
    //ボタン作成
    function CreateBtn(&$objForm, $name, $value, $extra)
    {
        $objForm->ae( array("type"        => "button",
                            "name"        => $name,
                            "extrahtml"   => $extra,
                            "value"       => $value ) );
        return $objForm->ge($name);
    }

}
?>
