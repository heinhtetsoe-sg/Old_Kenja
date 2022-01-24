<?php

require_once('for_php7.php');

class knjl054dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //科目コンボ
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl054dQuery::getNameMst($model, "L009");
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS_CD", $model->field["SUBCLASS_CD"], $extra, 1, "");

        //データ取得
        $model->setDataArr = array();
        $chgFlg = "";
        $result = $db->query(knjl054dQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if ($chgFlg != $row["ERROR_NO"] && $chgFlg != "") {
                $arg["data"][] = "";
            }

            //訂正受験番号
            if ($row["ERROR_NO"] != "４") {
                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                $value = (isset($model->warning)) ? $model->field["CORRECT_EXAMNO:".$row["ERROR_NO"]."-".$row["SEQ"]]: "";
                $row["CORRECT_EXAMNO"] = knjCreateTextBox($objForm, $value, "CORRECT_EXAMNO:".$row["ERROR_NO"]."-".$row["SEQ"], 6, 5, $extra);
            }

            //更新時に使用
            $model->setDataArr[$row["ERROR_NO"]."-".$row["SEQ"]] = $row["SEQ"];

            $chgFlg = $row["ERROR_NO"];

            $arg["data"][] = $row;
        }

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl054dindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl054dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TEST_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
