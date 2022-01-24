<?php

require_once('for_php7.php');

class knjm731Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm731Form1", "POST", "knjm731index.php", "", "knjm731Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR.'年度';

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //データをクリア
        if ($model->cmd == "knjm731" || $model->cmd == "clear") unset($model->data);

        //検索種別ラジオボタン 1:新入生 2:在籍者
        $opt_search = array(1, 2);
        $model->field["SEARCH_DIV"] = ($model->field["SEARCH_DIV"] == "") ? "1" : $model->field["SEARCH_DIV"];
        $click = " onclick=\"return btn_submit('knjm731')\"";
        $extra = array("id=\"SEARCH_DIV1\"".$click, "id=\"SEARCH_DIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->field["SEARCH_DIV"], $extra, $opt_search, get_count($opt_search));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //クラス
        $query = knjm731Query::getHrClassList();
        $extra = ($model->field["SEARCH_DIV"] == "2") ? "onchange=\"return btn_submit('knjm731')\"" : "STYLE=\"background-color:darkgray\" disabled";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //データ一覧
        $extra = "STYLE=\"text-align: right\" onblur=\"return Data_check(this);\"";
        $model->data["SCHREGNO"] = "";
        $query = knjm731Query::getMainQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $sep = ($model->data["SCHREGNO"] == "") ? "" : ",";
            $model->data["SCHREGNO"] .= $sep.$row["SCHREGNO"];

            $value = ($model->data["MUSYOU_KAISU".$row["SCHREGNO"]] == "") ? $row["MUSYOU_KAISU"] : $model->data["MUSYOU_KAISU".$row["SCHREGNO"]];
            $row["MUSYOU_KAISU"] = knjCreateTextBox($objForm, $value, "MUSYOU_KAISU".$row["SCHREGNO"], 1, 1, $extra);

            $arg["data"][] = $row;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model, $row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm731Form1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {

    //更新ボタン
    if (($model->field["SEARCH_DIV"] == "1" || ($model->field["SEARCH_DIV"] == "2" && $model->field["GRADE_HR_CLASS"])) && AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = " onclick=\"return btn_submit('update');\"";
    } else {
        $extra = " disabled";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = " onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $row) {
    knjCreateHidden($objForm, "cmd");
}
?>
