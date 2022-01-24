<?php

require_once('for_php7.php');

class knjd130kForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130kindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //画面サイズ
        if ($model->allwidth == "") {
            $arg["widthcheck"] = "getTextAreaWidth();";
        }
        $arg["ALLWIDTH"] = ($model->allwidth) ? $model->allwidth : 600;

        //生徒情報
        $arg["SCH_INFO"] = $model->schregno."　".$model->name;

        //入力不可
        $disable = ($model->schregno) ? "" : " disabled";

        //学期コンボ
        $query = knjd130kQuery::getSemesterQuery();
        $extra = "onChange=\"return btn_submit('gakki');\"";
        makeCmb($objForm, $arg, $db, $query, "GAKKI", $model->gakki, $extra.$disable, 1);

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $Row    = $db->getRow(knjd130kQuery::getHreportremarkDat($model->schregno, $model->gakki), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row    =& $model->field;
        }

        //テキスト
        foreach ($model->textList as $field => $val) {
            $value = $Row[$field];
            $name  = $field;
            if ($model->getPro[$field]["gyou"] == 1) {
                $extra = "";
                $arg[$name] = knjCreateTextBox($objForm, $value, $name, ($model->getPro[$field]["moji"] * 2), ($model->getPro[$field]["moji"] * 2), $extra.$disable);
                $arg[$name."_COMMENT"] = "(全角{$model->getPro[$field]["moji"]}文字まで)";
            } else {
                $height = $model->getPro[$field]["gyou"] * 13.5 + ($model->getPro[$field]["gyou"] - 1) * 3 + 5;
                $extra = "style=\"height:{$height}px;white-space: pre;\"";
                $arg[$name] = KnjCreateTextArea($objForm, $name, $model->getPro[$field]["gyou"], ($model->getPro[$field]["moji"] * 2 + 1), "soft", $extra.$disable, $value);
                $arg[$name."_COMMENT"] = "(全角{$model->getPro[$field]["moji"]}文字X{$model->getPro[$field]["gyou"]}行まで)";
            }
            $arg[$name."_LABEL"] = $val["label"];
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);

        //更新後前の生徒へボタン
        if ($disable) {
            $pre = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", "style=\"width:130px\" ".$disable);
            $next = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", "style=\"width:130px\" ".$disable);
            $arg["btn_up_next"] = $pre.$next;
        } else {
            $arg["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disable);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "allwidth");

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd130kForm1.html", $arg);
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

    if ($name == "GAKKI") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
