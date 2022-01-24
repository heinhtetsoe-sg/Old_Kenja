<?php

require_once('for_php7.php');

class knjc039bform1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjc039bindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //校種
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjc039bQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");
        //ALL
        $model->field["GRADE_ALL"]      = "00";
        $model->field["HR_CLASS_ALL"]   = "000";
        //学年
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjc039bQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "ALL", $model->field["GRADE_ALL"]);
        //組
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjc039bQuery::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1, "ALL", $model->field["HR_CLASS_ALL"]);
        //集計単位
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjc039bQuery::getCollectionCd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COLLECTION_CD"], "COLLECTION_CD", $extra, 1, "");

        //備考生成エラー処理　集計データが既に存在するか？
        $data_cnt = $db->getOne(knjc039bQuery::getCollectionDataCnt($model));
        knjCreateHidden($objForm, "DATA_CNT", $data_cnt);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc039bForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $valAll = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL" && strlen($valAll)) {
        $opt[] = array("label" => "全て", "value" => $valAll);
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($blank == "ALL" && strlen($valAll)) {
        if ($value == $valAll) $value_flg = true;
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //備考生成ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "備考生成", $extra);
    //確認画面へボタン
    $url = REQUESTROOT."/C/KNJC039C/knjc039cindex.php?SEND_PRGID=KNJC039B&SEND_AUTH=".AUTHORITY."&SEND_SCHOOL_KIND={$model->field["SCHOOL_KIND"]}&SEND_GRADE={$model->field["GRADE"]}&SEND_HR_CLASS={$model->field["HR_CLASS"]}&SEND_COLLECTION_CD={$model->field["COLLECTION_CD"]}";
    $extra = "onClick=\"callCollection('{$url}');\"";
    $arg["button"]["btn_collection"] = knjCreateBtn($objForm, "btn_collection", "確認画面へ", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
}
?>
