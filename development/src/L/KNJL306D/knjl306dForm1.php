<?php

require_once('for_php7.php');

class knjl306dForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl306dindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = $model->ObjYear;
        knjCreateHidden($objForm, "year", $model->ObjYear);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //観点評価段階値
        $opt = array();
        $opt[] = array("label" => "1：レンジ", "value" =>"1");
        if ($model->field["ASSESSCD"] == "") {
            $model->field["ASSESSCD"] = $opt[0]["value"];
        }
        $extra = "onChange=\"return btn_submit('change');\"";
        $arg["ASSESSCD"] = knjCreateCombo($objForm, "ASSESSCD", $model->field["ASSESSCD"], $opt, $extra, 1);

        //評定説明
        $arg["ASSESSMEMO"] = $db->getOne(knjl306dQuery::getAssessHdat($model));

        //段階値数取得
        $countAssess = $db->getOne(knjl306dQuery::selectCountQuery($model));

        //条件が変更されたとき、初期値を取得
        if ($model->cmd == "main" || $model->cmd == "change" || $model->cmd == "reset") {
            $model->field["MAX_ASSESSLEVEL"] = ($countAssess > 0) ? $countAssess : "";
        }
        $extra = "style=\"text-align: center\" onblur=\"this.value=NumCheck(this.value)\";";
        $arg["MAX_ASSESSLEVEL"] = knjCreateTextBox($objForm, $model->field["MAX_ASSESSLEVEL"], "MAX_ASSESSLEVEL", 2, 2, $extra);

        $model->data = array();
        if ($model->field["MAX_ASSESSLEVEL"]) {
            $counter = 0;
            //一覧表示
            $result = $db->query(knjl306dQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //段階値
                $row["ASSESSLEVEL"] = $counter + 1;

                //下限値テキストボックス
                if ($counter != 0) {
                    $extra = "style=\"text-align: right\" onblur=\"isNumb(this, ".($row["ROW_NUM"] -1).");\"";
                    $value = (!isset($model->warning)) ? $row["ASSESSLOW"] : $model->fields["ASSESSLOW"][$counter];
                    $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 3, $extra);
                } else {
                    $extra = "style=\"text-align: right\"";
                    $value = ($row["ASSESSLOW"]) ? $row["ASSESSLOW"] : "0";
                    $row["ASSESSLOW"] = knjCreateTextBox($objForm, $value, "ASSESSLOW-".$counter, 4, 3, $extra);
                }

                //記号テキストボックス
                $extra = "style=\"text-align: center\"";
                $value = (!isset($model->warning)) ? $row["ASSESSMARK"] : $model->fields["ASSESSMARK"][$counter];
                $row["ASSESSMARK"] = knjCreateTextBox($objForm, $value, "ASSESSMARK-".$counter, 4, 4, $extra);

                //上限値の表示
                if (($counter +1) == $model->field["MAX_ASSESSLEVEL"]) {
                    $extra = "style=\"text-align: right\"";
                    $value = ($row["ASSESSHIGH"]) ? $row["ASSESSHIGH"] : "435";
                    $row["ASSESSHIGHTEXT"] = knjCreateTextBox($objForm, $value, "ASSESSHIGH_MAX", 4, 4, $extra);
                } else {
                    if (isset($model->warning)) {
                        $row["ASSESSHIGH"] = $model->fields["ASSESSHIGH"][$counter];
                    }
                    $row["ASSESSHIGHTEXT"]  = "<span id=\"ASSESSHIGH_ID";
                    $row["ASSESSHIGHTEXT"] .= $row["ROW_NUM"];
                    $row["ASSESSHIGHTEXT"] .= "\">";
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    $row["ASSESSHIGHTEXT"] .= "</span>";
                }
                //段階値の上限値をhiddenで保持
                knjCreateHidden($objForm, "Assesshighvalue".$row["ROW_NUM"], $row["ASSESSHIGH"]);

                //上限値を配列で取得
                $model->data["ASSESSHIGH"][] = $row["ASSESSHIGH"];
                $model->data["ROW_NUM"][] = $row["ROW_NUM"];
                $counter++;
                $arg["data"][] = $row;
            }
            $result->free();
        }

        //ボタン作成
        //確定ボタン
        $extra = "onclick=\"return btn_submit('kakutei');\"";
        $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
        //更新ボタンを作成する
        $disable = ($model->field["MAX_ASSESSLEVEL"] > 0) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"".$disable;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL306D");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl306dForm1.html", $arg);
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
