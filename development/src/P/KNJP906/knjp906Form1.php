<?php

require_once('for_php7.php');

class knjp906Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp906index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjp906Query::getSemesterList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $query = knjp906Query::getGradeHrclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $schregNos = "";
        $schSep = "";
        $colorFlg = false;
        $result = $db->query(knjp906Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];
            $model->data["ATTENDNO"][$row["SCHREGNO"]] = $row["ATTENDNO"];

            //更新前の給付額を保持
            knjCreateHidden($objForm, "PRE_MONEY-".$row["SCHREGNO"], $row["BENEFIT_MONEY"]);

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //給付金額
            $value = (!isset($model->warning)) ? $row["BENEFIT_MONEY"] : $model->fields["BENEFIT_MONEY"][$row["SCHREGNO"]];
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); checkChgValue('{$row["SCHREGNO"]}');\" onkeypress=\"btn_keypress();\" onPaste=\"return showPaste(this, '{$counter}');\"";
            $setName  = "BENEFIT_MONEY-".$row["SCHREGNO"];
            $row["BENEFIT_MONEY"] = knjCreateTextBox($objForm, $value, $setName, 6, 6, $extra);

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $schregNos .= $schSep.$row["SCHREGNO"];
            $schSep = ",";

            $counter++;
            $arg["data"][] = $row;
        }

        knjCreateHidden($objForm, "CHANGE_VAL_FLG");

        //テキストの名前を取得
        knjCreateHidden($objForm, "TEXT_FIELD_NAME", "BENEFIT_MONEY");
        knjCreateHidden($objForm, "SCHREGNOS", $schregNos);

        //ボタン作成
        //更新ボタンを作成する
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp906Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
