<?php

require_once('for_php7.php');

class knjp915Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm        = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp915index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //事前チェック、前年度の年度締めを全学年していないとエラー
        $gradeCnt = 0;
        $closeCnt = 0;
        $query = knjp915Query::getGdat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knjp915Query::getCloseDat($model, $row["GRADE"]);
            $closeCnt += $db->getOne($query);

            $gradeCnt++;
        }
        if ($gradeCnt != $closeCnt) {
            $arg["closeCheck"] = " closeCheck(); ";
        }

        //年度
        $arg["YEAR"] = $model->year;

        //処理コンボ
        $opt = array();
        $opt[] = array('label' => '更新', 'value' => '1');
        $opt[] = array('label' => 'キャンセル', 'value' => '2');
        $extra = " onchange=\"return btn_submit('main');\"";
        $model->updateDiv = ($model->updateDiv != '') ? $model->updateDiv: '1';
        $arg["data"]["UPDATE_DIV"] = knjCreateCombo($objForm, "UPDATE_DIV", $model->updateDiv, $opt, $extra, 1);

        if ($model->updateDiv == '1') {
            $arg["UP_DIV1"] = '1';
            $arg["UP_DIV2"] = '';
        } else {
            $arg["UP_DIV1"] = '';
            $arg["UP_DIV2"] = '1';
        }

        //校種コンボ
        $query = knjp915Query::getSchkind($model);
        $extra = " onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        if ($model->updateDiv == '1') {
            //繰越項目
            $query = knjp915Query::getCarryLMcd($model);
            $extra = " onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "CARRY_LM_CD", $model->carryLMcd, $extra, 1, "");
            $model->warihuriDiv = substr($model->carryLMcd, 4, 1);
            knjCreateHidden($objForm, "warihuriDiv", $model->warihuriDiv);
            if ($model->warihuriDiv == '1') {
                $arg["LIST_TOLIST"] = '1';
            } else {
                $arg["LIST_TOLIST"] = '';
            }

            //振替先項目
            $query = knjp915Query::getIncomeLMcd($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "INCOME_LM_CD_SAKI", $model->incomeLMcdSaki, $extra, 1, "");

            //収入日
            $value = ($model->incomeDate != '') ? $model->incomeDate: str_replace('-', '/', CTRL_DATE);
            $arg["data"]["INCOME_DATE"] = View::popUpCalendarAlp($objForm, "INCOME_DATE", $value, $disabled, "");
        } else {
            //預り金項目
            $query = knjp915Query::getIncomeLMcdCancel($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "INCOME_LM_CD_CANCEL", $model->incomeLMcdCancel, $extra, 1, "");
        }

        /****************/
        /* List to List */
        /****************/
        //生徒一覧
        $opt_right = array();
        $query = knjp915Query::getTergetSchreg($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }

        //生徒一覧
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjcreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjcreateCombo($objForm, "category_selected", "", array(), $extra, 20);
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

        //名称マスタP016で設定した入金項目が存在するかチェック
        $existsCnt = $db->getOne(knjp915Query::getExistsCollectMMst($model, $model->collectLMcd));

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COLLECT_LM_CD", $model->collectLMcd);
        knjCreateHidden($objForm, "COLLECT_CD_EXISTS_FLG", $existsCnt);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp915Form1.html", $arg);
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
