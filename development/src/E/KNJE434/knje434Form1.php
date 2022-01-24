<?php

require_once('for_php7.php');

class knje434Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje434Form1", "POST", "knje434index.php", "", "knje434Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //更新する内容があった場合に日付を入力させるポップアップ
        if ($model->cmd == "fixed") {
            $arg["reload"] = " fixed('".REQUESTROOT."')";
        }

        //年度表示
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //教育委員会判定
        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            $arg["Z010ABBV1"] = "1";
        }

        //報告データ
        $query = knje434Query::getFixed($model);
        $extra = "onChange=\"return btn_submit('knje434');\"";
        makeCmb($objForm, $arg, $db, $query, "FIXED_DATA", $model->field["FIXED_DATA"], $extra, 1, 1);

        $disable1 = ($model->field["FIXED_DATA"] == "") ? " disabled" : "";
        $disable2 = ($model->field["FIXED_DATA"] == "") ? "" : " disabled";

        //報告日
        $model->field["EXECUTE_DATE"] = str_replace("-", "/", $model->field["EXECUTE_DATE"]);
        $arg["data"]["EXECUTE_DATE"] = View::popUpCalendarAlp($objForm, "EXECUTE_DATE", $model->field["EXECUTE_DATE"], $disable1);

        //県への報告ボタン
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["button"]["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra.$disable1);

        //報告済み日付表示
        $setExeDate = $sep = "";
        $counter = 1;
        $query = knje434Query::getReport($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setExeDate .= $sep.str_replace("-", "/", $row["VALUE"]);
            $sep = ($counter % 5 == 0) ? "<br>" : ",";
            $counter++;
        }
        $result->free();
        $arg["data"]["EXE_DATES"] = $setExeDate;

        //異動対象日付取得
        $table = ($model->field["FIXED_DATA"] == "") ? "AFT_DISEASE_ADDITION434_HDAT" : "AFT_DISEASE_ADDITION434_FIXED_HDAT";
        $getIdouDate = $db->getOne(knje434Query::getIdouDate($model, $table));

        //異動対象日付
        $model->field["IDOU_DATE"] = ($model->field["FIXED_DATA"]) ? $getIdouDate : (($getIdouDate) ? $getIdouDate : CTRL_DATE);
        $model->field["IDOU_DATE"] = str_replace("-", "/", $model->field["IDOU_DATE"]);
        $arg["data"]["IDOU_DATE"] = View::popUpCalendarAlp($objForm, "IDOU_DATE", $model->field["IDOU_DATE"], $disable2);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable2);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //ＣＳＶ出力ボタン
        $extra  = ($model->field["FIXED_DATA"] == "" && $getIdouDate == "") ? "disabled" : "";
        $extra .= " onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "FIXED_DATE");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje434Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "FIXED_DATA") $row["LABEL"] = str_replace("-", "/", $row["LABEL"]);
        if ($name == "FIXED_DATA") $row["VALUE"] = str_replace("-", "/", $row["VALUE"]);
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
