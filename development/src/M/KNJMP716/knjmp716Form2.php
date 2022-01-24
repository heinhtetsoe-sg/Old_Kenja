<?php

require_once('for_php7.php');

class knjmp716Form2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjmp716index.php", "", "edit");

        $db = Query::dbCheckOut();

        $arg["COLLECT_GRP_CD"] = ($model->exp_grpcd != "") ? $model->exp_grpcd.":".$model->name : "";

        //学年コンボボックス
        $opt = array();
        $value_flg = false;
        $query = knjmp716Query::get_Grade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->grade == $row["VALUE"]) $value_flg = true;
        }
        $model->grade2 = $model->grade2 ? $model->grade2 : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('reload')\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE2", $model->grade2, $opt, $extra, 1);

        if (isset($model->warning) || $model->cmd == "reload" || $model->cmd == "change2") {
            $Row =& $model->field;
        } else {
            $query = knjmp716Query::getRow($model, 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //入金済みデータの件数
        $paid_money  = $db->getOne(knjmp716Query::getPaidMoney($model->taisyouYear, "COLLECT_MONEY_PAID_M_DAT", $model->exp_grpcd));
        $paid_money += $db->getOne(knjmp716Query::getPaidMoney($model->taisyouYear, "COLLECT_MONEY_PAID_S_DAT", $model->exp_grpcd));

        //hidden
        knjCreateHidden($objForm, "paidAri", $paid_money);

        $model->money_change_flg = "";
        if ($paid_money > 0) {
            $arg["jmsg"] = " jmsg('追加・更新・削除ボタンによる更新は行えません。','入金済みデータが存在します。');";
            $model->money_change_flg = "disabled";
        }

        //割当クラス一覧取得
        if (isset($model->warning) || $model->cmd == "reload") {
            //会計項目割当一覧の値が変更してリロードした時
            $result = $db->query(knjmp716Query::ReloadSelectClass($model));
        } else {
            $result = $db->query(knjmp716Query::GetSelectClass($model));
        }

        $opt_left = $opt_right = $tempcd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $label = $row["GRADE"].$row["HR_CLASS"]."：".htmlspecialchars($row["HR_NAME"]);
            $opt_left[]  = array("label" => $label, "value" => $row["HR_CLASS"]);

            $tempcd[] = $row["HR_CLASS"];
        }

        //クラス一覧取得
        $result = $db->query(knjmp716Query::GetClass($model, $tempcd));
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

        //更新
        $extra = " $model->money_change_flg onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = " $model->money_change_flg onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit") {
            $arg["jscript"] = "window.open('knjmp716index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjmp716Form2.html", $arg);
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
