<?php

require_once('for_php7.php');

class knjz291_staff_reflectionForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz291_staff_reflectionindex.php", "", "main");
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $query = knjz291_staff_reflectionQuery::getEdboardSchool();
        $model->edboard_schoolcd = $db->getOne($query);

        //教育課程コンボ
        $query = knjz291_staff_reflectionQuery::getYear();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, 'BLANK');

        //検索テキスト
        $extra = "";
        $arg["top"]["STAFFCD"] = knjCreateTextBox($objForm, $model->field["STAFFCD"], "STAFFCD", 10, 10, $extra);
        $arg["top"]["STAFFNAME"] = knjCreateTextBox($objForm, $model->field["STAFFNAME"], "STAFFNAME", 30, 30, $extra);
        $arg["top"]["STAFFNAME_KANA"] = knjCreateTextBox($objForm, $model->field["STAFFNAME_KANA"], "STAFFNAME_KANA", 30, 30, $extra);

        //リスト表示
        if ($model->cmd == "search") {
            $model->data=array();
            makeList($objForm, $arg, $db, $db2, $model);
        }

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz291_staff_reflectionForm1.html", $arg); 
    }
}

function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $flg = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($row["VALUE"] == $value) {
            $flg = 1;
        }
    }
    $result->free();

    $value = $flg == 1 ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeList(&$objForm, &$arg, $db, $db2, &$model) {
    $counter = 0;
    $dataFlg = false;

    $tmpdata = array();
    $TORIKOMIarr = array();
    $STAFFarr = array();

    $query = knjz291_staff_reflectionQuery::getList($model);
    $result = $db2->query($query);
    $model->staffCd = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //登録されている職員か
        $query = knjz291_staff_reflectionQuery::getStaff($row["STAFFCD"]);
        $torikomi = $db->getOne($query);
        //登録済みの場合は登録している職員コードを表示
        $query = knjz291_staff_reflectionQuery::getStaff($row["STAFFCD"], "data");
        $getstaffcd = $db->getOne($query);

        //更新対象教員
        if ($torikomi != "1") {
            $model->staffCd[] = $row["STAFFCD"];
        }

        $row["SDATE"] = str_replace("-", "/", $row["SDATE"]);
        $row["EDATE"] = str_replace("-", "/", $row["EDATE"]);

        //チェックボックス
        $extra = $torikomi == "1" ? " disabled " : " onClick=\"insDefStaffCd(this)\";";
        $row["CHECK_STAFF"] = knjCreateCheckBox($objForm, "CHECK_STAFF".$row["STAFFCD"], $row["STAFFCD"], $extra);

        //UPDATEするSTAFFCD
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        if ($torikomi == "1") {
            $model->fields["UP_STAFFCD"][$row["STAFFCD"]] = $getstaffcd;
            $extra .= " readonly style=\"background-color:lightgray;\"";
        }
        $row["UP_STAFFCD"] = knjCreateTextBox($objForm, $model->fields["UP_STAFFCD"][$row["STAFFCD"]], "UP_STAFFCD".$row["STAFFCD"], 10, 10, $extra);

        //ソート用
        $tmpdata[] = $row;
        $TORIKOMIarr[] = $torikomi;
        $STAFFarr[] = $row["STAFFCD"];
    }
    $result->free();
    array_multisort($TORIKOMIarr, SORT_ASC,
                    $STAFFarr, SORT_ASC,
                    $tmpdata); //配列を並び替える
    foreach ($tmpdata as $key => $row) {
        $arg["data"][] = $row;
    }
}

function makeButton(&$objForm, &$arg, &$model) {
    //検索ボタン
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    
    //更新ボタン
    $extra  = (0 < get_count($model->staffCd)) ? "" : "disabled ";
    $extra .= "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "取 込", $extra);

    //終了ボタン
    if ($model->sendSubmit == "1") {
        $extra = "onclick=\"return closeMethod();\"";
    } else {
        $extra = "onclick=\"return closeWin();\"";
    }

    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
