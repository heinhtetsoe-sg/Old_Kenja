<?php

require_once('for_php7.php');

class knja110_3aForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja110_3aindex.php", "", "edit");
        $arg["reload"] = "";

        if ($model->TRANSFER_SDATE && !$model->isWarning() && $model->cmd != "change") {
            $Row = knja110_3aQuery::getRow($model);
        } elseif ($model->cmd == "change") {
            $transfercd = $model->field["TRANSFERCD"];
            unset($model->field);
            $model->field["TRANSFERCD"] = $transfercd;
            $Row =& $model->field;
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //異動区分
        $result = $db->query(knja110_3aQuery::getTransfercd());
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }

        $result->free();

        $Row["TRANSFERCD"] = ($Row["TRANSFERCD"] == "") ? $opt[0]["value"] : $Row["TRANSFERCD"];

        $objForm->ae(array("type"        => "select",
                            "name"        => "TRANSFERCD",
                            "size"        => 1,
                            "value"       => $Row["TRANSFERCD"] ,
                            "extrahtml"   => "onChange=\"return btn_submit('change')\"",
                            "options"     => $opt ));

        $arg["data"]["TRANSFERCD"] = $objForm->ge("TRANSFERCD");

        //異動期間
        $arg["data"]["TRANSFER_SDATE"] = View::popUpCalendar(
            $objForm,
            "TRANSFER_SDATE",
            str_replace("-", "/", $Row["TRANSFER_SDATE"]),
            ""
        ) ;
        $arg["data"]["TRANSFER_EDATE"] = View::popUpCalendar(
            $objForm,
            "TRANSFER_EDATE",
            str_replace("-", "/", $Row["TRANSFER_EDATE"]),
            ""
        ) ;

        //項目名
        $arg["label"]["TRANSFERREASON"] = ($model->kyoto > 0 && $Row["TRANSFERCD"] == 1) ? '国名' : '事由';
        $arg["label"]["TRANSFERPLACE"]  = ($model->kyoto > 0 && $Row["TRANSFERCD"] == 1) ? '学校名' : '異動先名称';
        $arg["label"]["TRANSFERADDR"]   = ($model->kyoto > 0 && $Row["TRANSFERCD"] == 1) ? '学年' : '異動先住所';

        //事由
        $objForm->ae(array("type"        => "text",
                            "name"        => "TRANSFERREASON",
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row["TRANSFERREASON"] ));

        $arg["data"]["TRANSFERREASON"] = $objForm->ge("TRANSFERREASON");

        //異動先名称
        $objForm->ae(array("type"        => "text",
                            "name"        => "TRANSFERPLACE",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row["TRANSFERPLACE"] ));

        $arg["data"]["TRANSFERPLACE"] = $objForm->ge("TRANSFERPLACE");

        //異動先住所
        $objForm->ae(array("type"        => "text",
                            "name"        => "TRANSFERADDR",
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row["TRANSFERADDR"] ));

        $arg["data"]["TRANSFERADDR"] = $objForm->ge("TRANSFERADDR");
        $arg["data"]["ADDR_SLEN"] = $model->addrSLen;

        //備考
        if ($model->kyoto > 0 && $Row["TRANSFERCD"] == 1) {
            $arg["kyoto"] = 1;
            $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 60, 90, "");
        }

        //修得単位
        $objForm->ae(array("type"        => "text",
                            "name"        => "ABROAD_CREDITS",
                            "size"        => 25,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"; onblur=\"return selcheck(this)\"",
                            "value"       => $Row["ABROAD_CREDITS"] ));

        $arg["data"]["ABROAD_CREDITS"] = $objForm->ge("ABROAD_CREDITS");

        if ($model->hasABROAD_PRINT_DROP_REGD && $model->Properties["knja110aShowAbroadPrintDropRegd"] == "1") {
            $arg["showABROAD_PRINT_DROP_REGD"] = "1";

            //指導要録に仮在籍を印字する
            $extra = $Row["ABROAD_PRINT_DROP_REGD"] == "1" ? "checked" : "";
            $query = knja110_3aQuery::getValidatePrintDropRegdSql($model);
            if ($model->SCHREGNO == '' || !$model->TRANSFER_SDATE || !$db->getOne($query)) {
                $extra .= " disabled=\"disabled\" ";
            }
            $extra .= " id=\"ABROAD_PRINT_DROP_REGD\" ";
            $arg["data"]["ABROAD_PRINT_DROP_REGD"] = knjCreateCheckBox($objForm, "ABROAD_PRINT_DROP_REGD", "1", $extra, "");
        }

        //追加ボタンを作成
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        /*        //更新ボタンを作成する
                $objForm->ae( array("type"      =>  "button",
                                    "name"      =>  "btn_up_pre",
                                    "value"     =>  "更新後前の生徒へ",
                                    "extrahtml" =>  "style=\"width:130px\"onclick=\"return btn_submit('pre_up');\""));

                $arg["button"]["btn_up_pre"]    = $objForm->ge("btn_up_pre");

                //更新ボタンを作成する
                $objForm->ae( array("type"      =>  "button",
                                    "name"      =>  "btn_up_next",
                                    "value"     =>  "更新後次の生徒へ",
                                    "extrahtml" =>  "style=\"width:130px\"onclick=\"return btn_submit('next_up');\""));

                $arg["button"]["btn_up_next"]    = $objForm->ge("btn_up_next");
        */
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタンを作成
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成
        $objForm->ae(array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.top_frame.location.href='knja110_3aindex.php?cmd=list';";
        }
        $arg["check"]  = "checktest('".$Row["TRANSFERCD"]."');";

        View::toHTML($model, "knja110_3aForm2.html", $arg);
    }
}
