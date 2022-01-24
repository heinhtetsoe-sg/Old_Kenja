<?php
class knjb103aForm2
{
    public function main(&$model)
    {

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb103aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //取消時に時限、開始時間、終了時間のコンボを初期値に戻す
        if ($model->cmd == "reset") {
            unset($model->field["PERIODNAME2"]);
            unset($model->field["STARTTIME_HOUR"]);
            unset($model->field["STARTTIME_MINUTE"]);
            unset($model->field["ENDTIME_HOUR"]);
            unset($model->field["ENDTIME_MINUTE"]);
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->year && $model->lastPeriodCd != "") {
            $query = knjb103aQuery::selectQuery($model, $model->lastPeriodCd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Row["PERIODCD"] = $model->field["PERIODNAME2"];
        }

        //時限セレクト作成
        $extra = "";
        $query = knjb103aQuery::getSettingJigen($model);
        makeCombo($objForm, $arg, $db, $query, "PERIODNAME2", $Row["PERIODCD"], $extra, 1);

        //開始時間
        $extra = "";
        makeSelectHour($objForm, $arg, $db, "STARTTIME_HOUR", $Row["STARTTIME_HOUR"], $extra, 1);
        makeSelectMinute($objForm, $arg, $db, "STARTTIME_MINUTE", $Row["STARTTIME_MINUTE"], $extra, 1);

        //終了時間
        $extra = "";
        makeSelectHour($objForm, $arg, $db, "ENDTIME_HOUR", $Row["ENDTIME_HOUR"], $extra, 1);
        makeSelectMinute($objForm, $arg, $db, "ENDTIME_MINUTE", $Row["ENDTIME_MINUTE"], $extra, 1);


        /****************/
        /*  ボタン作成  */
        /****************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRADE", $model->field["GRADE"]);
        knjCreateHidden($objForm, "SEMESTER", $model->field["SEMESTER"]);
        knjCreateHidden($objForm, "SUB_TESTCD", $model->field["SUB_TESTCD"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjb103aindex.php?cmd=list2"
                            . "&year=".$model->year."&GRADE=".$model->field["GRADE"]."&SEMESTER=".$model->field["SEMESTER"]."&SUB_TESTCD=".$model->field["SUB_TESTCD"]."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb103aForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $opt[] = array('label' => "礼拝",'value' => "Z");
    if ($value == 'Z') {
        $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//時間セレクト作成
function makeSelectHour(&$objForm, &$arg, $db, $name, &$value, $extra, $size)
{
    $opt = array();
    for ($idx = 8; $idx <= 20; $idx++) {
        $opt[] = array('label' => sprintf("%02d", $idx), 'value' => sprintf("%02d", $idx));
    }
    $value = $value != "" ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//分セレクト作成
function makeSelectMinute(&$objForm, &$arg, $db, $name, &$value, $extra, $size)
{
    $opt = array();
    for ($idx = 0; $idx <= 55; $idx +=5) {
        $opt[] = array('label' => sprintf("%02d", $idx), 'value' => sprintf("%02d", $idx));
    }
    $value = $value != "" ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
