<?php

require_once('for_php7.php');

class knjb1218Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjb1218index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change") unset($model->checked);

        //生徒データ表示
        $schData = makeStudentInfo($objForm, $arg, $db, $model);

        //対象年度コンボ
        $opt_year[] = array("label" => (CTRL_YEAR+1).'年度', "value" => (CTRL_YEAR+1));
        if ($model->search_div == "2") $opt_year[] = array("label" => CTRL_YEAR.'年度', "value" => CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('change'), AllClearList();\"";
        $model->year = ($model->year) ? $model->year : CTRL_YEAR + 1;
        $arg["YEAR_CMB"] = knjCreateCombo($objForm, "YEAR_CMB", $model->year, $opt_year, $extra, 1);

        //明細
        $meisai_cnt = makeMeisai($objForm, $arg, $db, $model, $schData);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $meisai_cnt);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1218Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model) {
    $setRow = array();
    $info = $db->getRow(knjb1218Query::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) $setRow[$key] = $val;
        $setRow["ANNUAL"] = ($setRow["ANNUAL"]) ? trim(sprintf("%2d", $setRow["ANNUAL"]))."年次" : "";
    }

    $setRow["SCHREGNO"] = $model->schregno;
    $setRow["NAME"]     = $model->name;

    $arg["info"] = $setRow;

    return array($setRow["GRADE"], $setRow["SCHOOL_KIND"], $setRow["HANDICAP"]);
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//明細
function makeMeisai(&$objForm, &$arg, $db, &$model, $schData) {
    //今年度のSCHREG_TEXTBOOK_CHKFIN_DAT
    $executed = $db->getOne(knjb1218Query::getSchregTextbookFinDat($model));

    //学籍教科書購入データ取得
    $thisCnt = $pastCnt = $zumiCnt = array();
    $array = array();
    $sonzai_cnt = 0;
    $query = knjb1218Query::getSchregTextbookSubclassDatCnt($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
        if (!$thisCnt[$row["TEXTBOOKCD"]][$subclasscd]) $thisCnt[$row["TEXTBOOKCD"]][$subclasscd] = 0;
        if (!$pastCnt[$row["TEXTBOOKCD"]]) $pastCnt[$row["TEXTBOOKCD"]] = 0;
        if (!$zumiCnt[$row["TEXTBOOKCD"]]) $zumiCnt[$row["TEXTBOOKCD"]] = 0;

        if ($row["YEAR"] == CTRL_YEAR) {
            $sonzai_cnt++;
            $thisCnt[$row["TEXTBOOKCD"]][$subclasscd]++;
            $array[] = $subclasscd.'-'.$row["TEXTBOOKCD"];
        } else if ($row["YEAR"] < CTRL_YEAR) {
            $pastCnt[$row["TEXTBOOKCD"]]++;
        }
        $zumiCnt[$row["TEXTBOOKCD"]]++;
    }
    $result->free();

    //科目別教科書データの件数取得
    $same_txt = $txt = array();
    $query = knjb1218Query::getSubclassTextbookDatCnt($model, $row, $schData, "1");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
        if (!$same_txt[$row["TEXTBOOKCD"]][$subclasscd]) $same_txt[$row["TEXTBOOKCD"]][$subclasscd] = 0;
        if (!$txt[$row["TEXTBOOKCD"]][$subclasscd]) $txt[$row["TEXTBOOKCD"]][$subclasscd] = 0;

        if (in_array($subclasscd.'-'.$row["TEXTBOOKCD"], $array)) {
            $same_txt[$row["TEXTBOOKCD"]][$subclasscd] += $row["CNT"];
        }
        $txt[$row["TEXTBOOKCD"]][$subclasscd] += $row["CNT"];
    }
    $result->free();

    $model->allVal = array();
    $model->div1_num_cnt = $model->div2_num_cnt = $model->div3_num_cnt = $model->total_num_cnt = 0;
    $model->div1_price_cnt = $model->div2_price_cnt = $model->div3_price_cnt = $model->total_price_cnt = 0;
    $meisai_cnt = 0;
    $query = knjb1218Query::getMeisaiQuery($model, $schData);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //レコードを連想配列のまま配列$arg[data]に追加していく。 
        array_walk($row, "htmlspecialchars_array");

        //過去の購入済み件数（教科書ごと）
        $past_cnt = ($pastCnt[$row["TEXTBOOKCD"]]) ? $pastCnt[$row["TEXTBOOKCD"]] : 0;
        //今年度の購入済み件数（教科書ごと）
        $this_cnt = ($thisCnt[$row["TEXTBOOKCD"]][$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]]) ? $thisCnt[$row["TEXTBOOKCD"]][$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]] : 0;
        //同じ教科書で異なる科目の件数
        $same_txt_cnt = 0;
        if (get_count($same_txt[$row["TEXTBOOKCD"]]) > 0) {
            foreach ($same_txt[$row["TEXTBOOKCD"]] as $subclasscd => $cnt) {
                if ($subclasscd != $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]) {
                    $same_txt_cnt += $cnt;
                }
            }
        }

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
        $cnt = ($zumiCnt[$row["TEXTBOOKCD"]]) ? $zumiCnt[$row["TEXTBOOKCD"]] : 0;
        $row["ZUMI"] = ($cnt > 0) ? "レ" : "";

        //過年度に購入済みの場合、背景色を変更する
        $row["BGCOLOR"] = ($past_cnt > 0) ? "yellow" : "white";

        //同一教科書件数
        $txt_cnt = 0;
        if (get_count($txt[$row["TEXTBOOKCD"]]) > 0) {
            foreach ($txt[$row["TEXTBOOKCD"]] as $subclasscd => $cnt) {
                if ($subclasscd != $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]) {
                    $txt_cnt += $cnt;
                }
            }
        }
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
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model){ 
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PRGID", "KNJB1218");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}
?>
