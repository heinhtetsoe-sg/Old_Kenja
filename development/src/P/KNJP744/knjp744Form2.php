<?php

require_once('for_php7.php');

class knjp744Form2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp744index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change") {
            $Row =& $model->field;
        } elseif (isset($model->warning)) {
            $Row =& $model->field;
        } elseif ($model->auto_no) {
            $query = knjp744Query::getAutoData($model, $model->auto_no);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($model->Properties["not_select_schregno_auto_income"] != "1") {
                $schno = $db->getCol(knjp744Query::getAutoSchregData($model, $model->auto_no));
                $model->selectdata = (strlen($schno[0])) ? implode(',', $schno) : "";
            }
        }

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjp744Query::getSchkind($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //ID
        $extra = "onblur=\"this.value=toInteger(this.value);\";";
        $arg["data"]["AUTO_NO"] = knjCreateTextBox($objForm, $Row["AUTO_NO"], "AUTO_NO", 3, 3, $extra);

        //名称
        $extra = "";
        $arg["data"]["AUTO_NAME"] = knjCreateTextBox($objForm, $Row["AUTO_NAME"], "AUTO_NAME", 60, 60, $extra);

        //入金項目コンボ
        $query = knjp744Query::getCollectLMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["COLLECT_LM_CD"], "COLLECT_LM_CD", $extra, 1, "blank");

        //預り金項目(収入)コンボ
        $query = knjp744Query::getLevyMMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["INCOME_LM_CD"], "INCOME_LM_CD", $extra, 1, "blank");

        //決議理由
        $extra = "";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $Row["REQUEST_REASON"], "REQUEST_REASON", 60, 80, $extra);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 21, 30, $extra);

        //起案者コンボ
        $query = knjp744Query::getRequestStaff($model, $Row["REQUEST_STAFF"]);
        $setStaff = $db->getOne($query);
        $arg["data"]["REQUEST_STAFF"] = $setStaff;
        //伝票作成者
        knjCreateHidden($objForm, "REQUEST_STAFF", $Row["REQUEST_STAFF"]);

        if ($model->Properties["not_select_schregno_auto_income"] != "1") {
            //単価
            $arg["dispCommodity"] = "1";
            $extra = "style=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\";";
            $arg["data"]["COMMODITY_PRICE"] = knjCreateTextBox($objForm, $Row["COMMODITY_PRICE"], "COMMODITY_PRICE", 6, 6, $extra);

            //学年コンボ
            $query = knjp744Query::getGrade($model);
            $extra = "onChange=\"return btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "blank");

            $opt_left = $opt_right = $tempcd = array();

            //対象生徒一覧
            $query = knjp744Query::getSchList($model, "1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_left[]  = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
                $tempcd[] = $row["VALUE"];
            }

            //生徒一覧
            $result = $db->query(knjp744Query::getSchList($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!in_array($row["VALUE"], $tempcd)) {
                    $opt_right[]  = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
                }
            }

            //オブジェクト作成
            $extraLeft = "ondblclick=\"move1('right','left_expmcd','right_expmcd');\"";
            $extraRight = "ondblclick=\"move1('left','left_expmcd','right_expmcd');\"";
            $arg["expmcd"] = array( "LEFT_LIST"   => "対象生徒一覧",
                                "RIGHT_LIST"  => "生徒一覧",
                                "LEFT_PART"   => knjCreateCombo($objForm, "left_expmcd", "left", $opt_left, $extraLeft." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                "RIGHT_PART"  => knjCreateCombo($objForm, "right_expmcd", "left", $opt_right, $extraRight." multiple style=\"WIDTH:100%; HEIGHT:330px\"", 15),
                                "SEL_ADD_ALL" => knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move1('sel_add_all','left_expmcd','right_expmcd');\""),
                                "SEL_ADD"     => knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move1('left','left_expmcd','right_expmcd');\""),
                                "SEL_DEL"     => knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move1('right','left_expmcd','right_expmcd');\""),
                                "SEL_DEL_ALL" => knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move1('sel_del_all','left_expmcd','right_expmcd');\"")
                              );
        }

        if ($model->Properties["disp_approval_chk"] == "1") {
            //決済状況
            $arg["dispApproval"] = "1";
            $extra = " id=\"AUTO_INCOME_APPROVAL\" ";
            $checked = $Row["AUTO_INCOME_APPROVAL"] == '1' ? " checked " : "";
            $arg["data"]["AUTO_INCOME_APPROVAL"] = knjCreateCheckBox($objForm, "AUTO_INCOME_APPROVAL", "1", $checked.$extra);
        }

        //追加
        $extra = "onclick=\"return doSubmit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = "onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "not_select_schregno_auto_income", $model->Properties["not_select_schregno_auto_income"]);

        //DB切断
        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit2" && $model->cmd != "change") {
            $arg["jscript"] = "window.open('knjp744index.php?cmd=list','left_frame');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp744Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
