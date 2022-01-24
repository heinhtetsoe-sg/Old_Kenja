<?php

require_once('for_php7.php');

class knjp914aForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm        = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp914aindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //校種コンボ
        $query = knjp914aQuery::getSchkind($model);
        $extra = " onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        //振替元項目
        $query = knjp914aQuery::getIncomeLMcd($model);
        $extra = " onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "INCOME_LM_CD_MOTO", $model->incomeLMcdMoto, $extra, 1, "");

        //振替先項目
        $query = knjp914aQuery::getIncomeLMcd($model, $model->incomeLMcdMoto);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "INCOME_LM_CD_SAKI", $model->incomeLMcdSaki, $extra, 1, "");

        //支出項目
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjp914aQuery::getLevyMDiv($model);
        makeCmb($objForm, $arg, $db, $query, "OUTGO_LM_CD_MOTO", $model->outgoLMcdMoto, $extra, 1, "");

        //支出細目
        $extra = "";
        $query = knjp914aQuery::getLevySDiv($model);
        makeCmb($objForm, $arg, $db, $query, "OUTGO_LMS_CD_MOTO", $model->outgoLMScdMoto, $extra, 1, "");

        //件名
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REQUEST_REASON"] = knjCreateTextBox($objForm, $model->requestReason, "REQUEST_REASON", 60, 60, $extra);

        //振替金額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); return btn_submit('main');\"";
        $arg["data"]["SET_MONEY"] = knjCreateTextBox($objForm, $model->setMoney, "SET_MONEY", 6, 6, $extra);

        //転退学checkBox
        $checked = ($model->taigaku == '1' || $model->cmd == '') ? " checked": "";
        $extra = "id=\"TAIGAKU\" onClick=\"return btn_submit('main');\"";
        $arg["data"]["TAIGAKU"] = knjCreateCheckBox($objForm, "TAIGAKU", "1", $extra.$checked);

        /****************/
        /* List to List */
        /****************/
        //生徒一覧
        $opt_left = $opt_right = array();
        if ($model->setMoney != '') {
            $query = knjp914aQuery::getTergetSchreg($model, 'LIST');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (isset($model->warning) && in_array($row["SCHREGNO"], explode(",", $model->selectStudent))) {
                    $opt_left[]  = array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
                } else {
                    $opt_right[] = array('label' => $row["LABEL"],
                                         'value' => $row["VALUE"]);
                }
            }
        }

        //生徒一覧
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjcreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);
        knjCreateHidden($objForm, "selectStudent");

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjcreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjcreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjcreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjcreateBtn($objForm, "btn_left1", "＜", $extra);

        //ボタン作成
        //実行
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COLLECT_LM_CD", $model->collectLMcd);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp914aForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
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
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value != "" && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
