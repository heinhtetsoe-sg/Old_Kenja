<?php

require_once('for_php7.php');

class knjd423Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd423index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('gakki');\"";
        $query = knjd423Query::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning) && strlen($model->field["SEMESTER"]) > 0) {
            $Row  = $db->getRow(knjd423Query::getRow($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row  =& $model->field;
        }

        //重点目標テキストボックス
        for ($i = 1; $i <= 3; $i++) {
            $moji = 20;
            $extra = "";
            $arg["data"]["REMARK".$i] = knjCreateTextBox($objForm, $Row["REMARK".$i], "REMARK".$i, ($moji * 2), ($moji * 2), $extra);
            $arg["data"]["REMARK_COMMENT".$i] = "(全角20文字まで)";
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = KnjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD423");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd423Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $defValue = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
        if ('1' == $row["DEF_VALUE_FLG"]) $defValue = $row["VALUE"];
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : ($defValue ? $defValue : $opt[0]["value"]);
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
