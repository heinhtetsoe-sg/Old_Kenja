<?php

require_once('for_php7.php');

class knjb1212Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjb1212index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change") unset($model->checked);

        //生徒データ表示
        makeStudentInfo($objForm, $arg, $db, $model);

        //対象年度コンボ
        $opt_year[] = array("label" => (CTRL_YEAR+1).'年度', "value" => (CTRL_YEAR+1));
        if ($model->search_div == "2") $opt_year[] = array("label" => CTRL_YEAR.'年度', "value" => CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('change'), AllClearList();\"";
        $model->year = ($model->year) ? $model->year : CTRL_YEAR + 1;
        $arg["YEAR_CMB"] = knjCreateCombo($objForm, "YEAR_CMB", $model->year, $opt_year, $extra, 1);

        //明細
        $meisai_cnt = makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $meisai_cnt);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1212Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model) {
    $info = $db->getRow(knjb1212Query::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) $setRow[$key] = $val;
        $setRow["ANNUAL"] = ($setRow["ANNUAL"]) ? trim(sprintf("%2d", $setRow["ANNUAL"]))."年次" : "";
    }

    $setRow["SCHREGNO"] = $model->schregno;
    $setRow["NAME"]     = $model->name;

    $arg["info"] = $setRow;
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//明細
function makeMeisai(&$objForm, &$arg, $db, &$model) {
    $model->allVal = array();
    $model->div1_num_cnt = $model->div2_num_cnt = $model->div3_num_cnt = $model->total_num_cnt = 0;
    $model->div1_price_cnt = $model->div2_price_cnt = $model->div3_price_cnt = $model->total_price_cnt = 0;
    $meisai_cnt = 0;
    $query = knjb1212Query::getMeisaiQuery($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列$arg[data]に追加していく。 
        array_walk($row, "htmlspecialchars_array");

        //今年度のSCHREG_TEXTBOOK_SUBCLASS_DATの総件数
        $sonzai_cnt = $db->getOne(knjb1212Query::getSchregTextbookSubclassDatCnt($model, $row, "", 1));
        //今年度のSCHREG_TEXTBOOK_CHKFIN_DAT
        $executed = $db->getOne(knjb1212Query::getSchregTextbookFinDat($model, $row, "", 1));
        //過去の購入済み件数（教科書ごと）
        $past_cnt = $db->getOne(knjb1212Query::getSchregTextbookSubclassDatCnt($model, $row, "past"));
        //今年度の購入済み件数（教科書ごと）
        $this_cnt = $db->getOne(knjb1212Query::getSchregTextbookSubclassDatCnt($model, $row, "this"));
        //同じ教科書で異なる科目の件数
        $same_txt_cnt = $db->getOne(knjb1212Query::getSubclassTextbookDatCnt($model, $row, "1"));

        $setval  =  $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].'-'.$row["TEXTBOOKCD"].'-'.$row["DIV"].'-'.$row["TEXTBOOKUNITPRICE"];

        $extra = ($sonzai_cnt > 0 || $executed > 0) ? "" : (($row["TEXTBOOKCD"] && $row["NOT_DEFAULT"] != "1" && !$past_cnt) ? "checked" : "");
        if ($model->checked) $extra = (in_array($setval, $model->checked)) ? "checked" : "";
        if ($this_cnt > 0) $extra = "checked";
        $extra2  = " onclick=\"CalculateSum(this);\"";
        $extra2 .= ($row["TEXTBOOKCD"] && $same_txt_cnt == "0") ? "" : " disabled";
        $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $setval, $extra.$extra2, "1");

        $model->div1_num_cnt += ($row["DIV"] == "1" && $extra == "checked") ? 1 : 0;
        $model->div2_num_cnt += ($row["DIV"] == "2" && $extra == "checked") ? 1 : 0;
        $model->div3_num_cnt += ($row["DIV"] == "3" && $extra == "checked") ? 1 : 0;
        $model->total_num_cnt += ($extra == "checked") ? 1 : 0;

        $model->div1_price_cnt += ($row["DIV"] == "1" && $extra == "checked") ? $row["TEXTBOOKUNITPRICE"] : 0;
        $model->div2_price_cnt += ($row["DIV"] == "2" && $extra == "checked") ? $row["TEXTBOOKUNITPRICE"] : 0;
        $model->div3_price_cnt += ($row["DIV"] == "3" && $extra == "checked") ? $row["TEXTBOOKUNITPRICE"] : 0;
        $model->total_price_cnt += ($extra == "checked") ? $row["TEXTBOOKUNITPRICE"] : 0;

        $row["SUURYOU"] = ($row["TEXTBOOKCD"]) ? "1" : "";
        $row["KINGAKU"] = ($row["TEXTBOOKCD"]) ? ($row["TEXTBOOKUNITPRICE"] ? $row["TEXTBOOKUNITPRICE"] : "0") : "" ;

        //カンマ区切り
        $row["TEXTBOOKUNITPRICE"] = ($row["TEXTBOOKUNITPRICE"] == "") ? "" : number_format($row["TEXTBOOKUNITPRICE"]);
        $row["KINGAKU"] = ($row["KINGAKU"] == "") ? "" : number_format($row["KINGAKU"]);

        //購入済み件数
        $cnt = $db->getOne(knjb1212Query::getSchregTextbookSubclassDatCnt($model, $row));
        $row["ZUMI"] = ($cnt > 0) ? "レ" : "";

        //過年度に購入済みの場合、背景色を変更する
        $row["BGCOLOR"] = ($past_cnt > 0) ? "yellow" : "white";

        //同一教科書件数
        $txt_cnt = $db->getOne(knjb1212Query::getSubclassTextbookDatCnt($model, $row));
        $row["COLOR"] = ($txt_cnt > 0) ? "blue" : "black";

        $arg["meisai"][] = $row; 
        $subclasscd = $row["CLASSCD"].$row["SCHOOL_KIND"].$row["CURRICULUM_CD"].$row["SUBCLASSCD"];

        //更新可能明細行数
        $meisai_cnt += ($row["TEXTBOOKCD"]) ? "1" : "0";

        //値をセット（同一教科書チェック用）
        $model->allVal[] = $setval;
    }

    //hidden（同一教科書チェック用）
    knjCreateHidden($objForm, "ALLVAL", $model->allVal);

    //hidden（合計行）
    knjCreateHidden($objForm, "DIV1_NUM_CNT", $model->div1_num_cnt);
    knjCreateHidden($objForm, "DIV2_NUM_CNT", $model->div2_num_cnt);
    knjCreateHidden($objForm, "DIV3_NUM_CNT", $model->div3_num_cnt);
    knjCreateHidden($objForm, "TOTAL_NUM_CNT", $model->total_num_cnt);
    knjCreateHidden($objForm, "DIV1_PRICE_CNT", number_format($model->div1_price_cnt));
    knjCreateHidden($objForm, "DIV2_PRICE_CNT", number_format($model->div2_price_cnt));
    knjCreateHidden($objForm, "DIV3_PRICE_CNT", number_format($model->div3_price_cnt));
    knjCreateHidden($objForm, "TOTAL_PRICE_CNT", number_format($model->total_price_cnt));

    //合計行
    $arg["DIV1_NUM_CNT"]  = $model->div1_num_cnt; 
    $arg["DIV2_NUM_CNT"]  = $model->div2_num_cnt;
    $arg["DIV3_NUM_CNT"]  = $model->div3_num_cnt;
    $arg["TOTAL_NUM_CNT"] = $model->total_num_cnt;
    $arg["DIV1_PRICE_CNT"]  = number_format($model->div1_price_cnt);
    $arg["DIV2_PRICE_CNT"]  = number_format($model->div2_price_cnt);
    $arg["DIV3_PRICE_CNT"]  = number_format($model->div3_price_cnt);
    $arg["TOTAL_PRICE_CNT"] = number_format($model->total_price_cnt);

    $result->free();
    return $meisai_cnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $meisai_cnt) {

    $disable = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno && $meisai_cnt > 0) ? "" : " disabled";

    //更新
    $extra  = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
    //更新して次の生徒へ
    $extra  = "onclick=\"return btn_submit('updateNext');\"";
    $arg["button"]["btn_update_next"] = knjCreateBtn($objForm, "btn_update", "更新して次の生徒へ", $extra.$disable);
    //更新して前の生徒へ
    $extra  = "onclick=\"return btn_submit('updatePrev');\"";
    $arg["button"]["btn_update_prev"] = knjCreateBtn($objForm, "btn_update_prev", "更新して前の生徒へ", $extra.$disable);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //購入票
    $extra = ($model->schregno) ? "onclick=\"return newwin('".SERVLET_URL."');\"" : "disabled";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "購入票", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model){ 
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PRGID", "KNJB1212");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}
?>
