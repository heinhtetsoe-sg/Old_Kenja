<?php

require_once('for_php7.php');

class knjd418Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd418index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //状態区分コンボ
        $query = knjd418Query::getCondition($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "CONDITION", $model->condition, $extra, 1, $model);

        //状態区分コンボ
        $query = knjd418Query::getGuidancePattern($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, "GUIDANCE_PATTERN", $model->guidance_pattern, $extra, 1, $model);

        //項目一覧作成
        if ($model->guidance_pattern) {
            //データ取得
            $query = knjd418Query::getHreportGuidanceItemNameDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $cnt = 1;
            $tmp = array();
            foreach ($model->pattern[$model->guidance_pattern] as $key => $val) {
                //連番
                $tmp["ITEM"] = '項目'.$cnt;
                //項目名テキスト
                if ($model->cmd == "check") $Row["ITEM_REMARK".$key] = $model->field["ITEM_REMARK".$key];
                if ($model->cmd == "set") $Row["ITEM_REMARK".$key] = $val;
                $tmp["ITEM_REMARK"] = knjCreateTextBox($objForm, $Row["ITEM_REMARK".$key], "ITEM_REMARK".$key, 20, 30, "");
                //初期値
                $tmp["DEFAULT_REMARK"] = $val;

                $arg["data"][] = $tmp;
                $cnt++;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjd418index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd418Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //取込ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "style=\"font-size:8pt;\" onclick=\"return btn_submit('set');\"" : "style=\"font-size:8pt;\" disabled";
    $arg["button"]["btn_set"] = knjCreateBtn($objForm, "btn_set", "取込", $extra);

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
    //削除ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD418");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "SEMESTER", $model->semester);
    knjCreateHidden($objForm, "GAKUBU_SCHOOL_KIND", $model->gakubu_school_kind);
}
?>
