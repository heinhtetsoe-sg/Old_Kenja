<?php

require_once('for_php7.php');

class knjb1213Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjb1213index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change_year") {
            unset($model->field);
            unset($model->register_date);
        }
        if ($model->cmd == "change_date") unset($model->field);

        //対象年度コンボ
        $opt_year[] = array("label" => (CTRL_YEAR+1).'年度', "value" => (CTRL_YEAR+1));
        if ($model->search_div == "2") $opt_year[] = array("label" => CTRL_YEAR.'年度', "value" => CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('change_year'), AllClearList();\"";
        $model->year = ($model->year) ? $model->year : CTRL_YEAR + 1;
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1);

        //生徒データ表示
        makeStudentInfo($objForm, $arg, $db, $model);

        //登録日コンボ
        $query = knjb1213Query::getRegisterDate($model);
        $extra = "onchange=\"return btn_submit('change_date'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, $model->register_date, "REGISTER_DATE_CMB", $extra, 1, $model, "BLANK");

        //無償給与リストtoリスト
        makeListToList($objForm, $arg, $db, $model);

        //教科書無償給与申し込みデータ編集
        makeTextbookFreeApplyEdit($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $meisai_cnt);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1213Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model) {

    $info = $db->getRow(knjb1213Query::getStudentInfo($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            if ($key == "ENT_DATE" && $val) {
                $splitVal = preg_split("/-/", $val);
                $val = $splitVal[0]."年".trim(sprintf("%2d", $splitVal[1]))."月";
            }
            $setRow[$key] = $val;
        }

        if ($model->search_div == "2") $setRow["ANNUAL"] = ($setRow["ANNUAL"]) ? trim(sprintf("%2d", $setRow["ANNUAL"]))."年次" : "";
        $setRow["GET_CREDIT"]   = ($setRow["GET_CREDIT"] == "" || $model->search_div == "1") ? "" : $setRow["GET_CREDIT"].'単位';
        $setRow["SUBCLASS_NUM"] = ($setRow["SUBCLASS_NUM"]) ? $setRow["SUBCLASS_NUM"].'科目' : "";

        //住所
        if ($setRow["SEND_ADDR1"]) {
            $setRow["ADDRESS1"] = $setRow["SEND_ADDR1"];
            $setRow["ADDRESS2"] = $setRow["SEND_ADDR2"];
        } else {
            $setRow["ADDRESS1"] = $setRow["ADDR1"];
            $setRow["ADDRESS2"] = $setRow["ADDR2"];
        }
    } else if ($model->search_div == "2") {
        //入学年度
        $ent_date = $db->getOne(knjb1213Query::getSchregBaseMst($model));
        $splitVal = preg_split("/-/", $ent_date);
        $setRow["ENT_DATE"] = ($ent_date) ? $splitVal[0]."年".trim(sprintf("%2d", $splitVal[1]))."月" : "";

        //住所
        $send_addr = $db->getRow(knjb1213Query::getSchregSendAddressDat($model), DB_FETCHMODE_ASSOC);
        $sch_addr  = $db->getRow(knjb1213Query::getSchregAddressDat($model), DB_FETCHMODE_ASSOC);
        if ($send_addr["SEND_ADDR1"]) {
            $setRow["ADDRESS1"] = $send_addr["SEND_ADDR1"];
            $setRow["ADDRESS2"] = $send_addr["SEND_ADDR2"];
        } else {
            $setRow["ADDRESS1"] = $sch_addr["ADDR1"];
            $setRow["ADDRESS2"] = $sch_addr["ADDR2"];
        }
    }

    $setRow["SCHREGNO"]     = $model->schregno;
    $setRow["NAME"]         = $model->name;
    $setRow["YEAR"]         = $model->year;

    $arg["info"] = $setRow;
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, &$model) {

    //無償給与対象
    $category_selected = "";
    $opt_free = array();
    $l_div1_price = $l_div2_price = $l_total_num = 0;
    if ($model->cmd == "knjb1213") {
        $query = knjb1213Query::getSchregTextbookSubclassDat($model, "1");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //登録された教科書
            $cnt_textbook  = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], ""));
            $cnt_past_text = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'past'));
            $cnt_this_text = ($model->register_date) ? $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'this')) : 0;
            $touroku = ($cnt_textbook > 0) ? (($cnt_past_text > 0) ? "1" : (($cnt_this_text > 0) ? "0" : "2")) : "0";

            $row["VALUE"] = trim($row["VALUE"]).'-'.$touroku;
            $opt_free[] = $row["VALUE"];

            //集計
            if ($row["DIV"] == "1") {
                $l_div1_price += $row["TEXTBOOKUNITPRICE"];
                $l_total_num++;
            }
            if ($row["DIV"] == "2") {
                $l_div2_price += $row["TEXTBOOKUNITPRICE"];
                $l_total_num++;
            }

            //データをセット（登録済みは背景色を変える）
            $backcolor = ($cnt_past_text > 0) ? " class=\"touroku1\"" : "";
            $category_selected .= "<option value=".$row["VALUE"]." ".$backcolor.">".$row["LABEL"];
        }
        $result->free();
    } else if ($model->register_date) {
        $query = knjb1213Query::getSchregTextbookFreeDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //登録された教科書
            $cnt_textbook  = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], ""));
            $cnt_past_text = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'past'));
            $cnt_this_text = ($model->register_date) ? $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'this')) : 0;
            $touroku = ($cnt_textbook > 0) ? (($cnt_past_text > 0) ? "1" : (($cnt_this_text > 0) ? "0" : "2")) : "0";

            $row["VALUE"] = trim($row["VALUE"]).'-'.$touroku;
            $opt_free[] = $row["VALUE"];

            //集計
            if ($row["DIV"] == "1") {
                $l_div1_price += $row["TEXTBOOKUNITPRICE"];
                $l_total_num++;
            }
            if ($row["DIV"] == "2") {
                $l_div2_price += $row["TEXTBOOKUNITPRICE"];
                $l_total_num++;
            }

            //データをセット（登録済みは背景色を変える）
            $backcolor = ($cnt_past_text > 0) ? " class=\"touroku1\"" : "";
            $category_selected .= "<option value=".$row["VALUE"]." ".$backcolor.">".$row["LABEL"];
        }
        $result->free();
    } else {
        $query = knjb1213Query::getSchregTextbookSubclassDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //登録された教科書
            $cnt_textbook = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], ""));

            if ($cnt_textbook == 0) {
                $row["VALUE"] = trim($row["VALUE"]).'-0';
                $opt_free[] = $row["VALUE"];

                //集計
                if ($row["DIV"] == "1") {
                    $l_div1_price += $row["TEXTBOOKUNITPRICE"];
                    $l_total_num++;
                }
                if ($row["DIV"] == "2") {
                    $l_div2_price += $row["TEXTBOOKUNITPRICE"];
                    $l_total_num++;
                }

                //データをセット
                $backcolor = "";
                $category_selected .= "<option value=".$row["VALUE"]." ".$backcolor.">".$row["LABEL"];
            }
        }
        $result->free();

        $model->field["BOOKDIV1_GK"]    = $l_div1_price;
        $model->field["BOOKDIV2_GK"]    = $l_div2_price;
        $model->field["TOTAL_GK"]       = $l_div1_price + $l_div2_price;
        $model->field["TOTAL_COUNT"]    = $l_total_num;
    }

    //無償給与対象リストボックス
    $arg["data"]["CATEGORY_SELECTED"]  = "<select name=\"CATEGORY_SELECTED\" size=\"10\" multiple style=\"width:270px;\" width=\"270px\" ondblclick=\"move1('right')\">";
    $arg["data"]["CATEGORY_SELECTED"] .= $category_selected;
    $arg["data"]["CATEGORY_SELECTED"] .= "</select>";

    //集計結果表示
    $arg["data"]["L_DIV1_PRICE"] = number_format($l_div1_price);
    $arg["data"]["L_DIV2_PRICE"] = number_format($l_div2_price);
    $arg["data"]["L_TOTAL_PRICE"] = number_format($l_div1_price + $l_div2_price);
    $arg["data"]["L_TOTAL_NUM"] = number_format($l_total_num);

    //無償給与対象外
    $category_selected = "";
    $r_div1_price = $r_div2_price = $r_total_num = 0;
    $query = knjb1213Query::getSchregTextbookSubclassDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //登録された教科書
        $cnt_textbook  = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], ""));
        $cnt_past_text = $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'past'));
        $cnt_this_text = ($model->register_date) ? $db->getOne(knjb1213Query::checkTextbook($model, $row["TEXTBOOKCD"], 'this')) : 0;
        $touroku = ($cnt_textbook > 0) ? (($cnt_past_text > 0) ? "1" : (($cnt_this_text > 0) ? "0" : "2")) : "0";

        $row["VALUE"] = trim($row["VALUE"]).'-'.$touroku;

        //左のリストにないデータをセット（登録済みは背景色を変える）
        if (!in_array($row["VALUE"], $opt_free)) {
            $backcolor = ($cnt_past_text > 0) ? " class=\"touroku1\"" : (($touroku == "2") ? " class=\"touroku2\"" : "");
            $category_name .= "<option value=".$row["VALUE"]." ".$backcolor.">".$row["LABEL"];

            //集計
            if ($row["DIV"] == "1") {
                $r_div1_price += $row["TEXTBOOKUNITPRICE"];
                $r_total_num++;
            }
            if ($row["DIV"] == "2") {
                $r_div2_price += $row["TEXTBOOKUNITPRICE"];
                $r_total_num++;
            }
        }
    }
    $result->free();

    //無償給与対象外リストボックス
    $arg["data"]["CATEGORY_NAME"]  = "<select name=\"CATEGORY_NAME\" size=\"10\" multiple style=\"width:270px;\" width=\"270px\" ondblclick=\"move1('left')\">";
    $arg["data"]["CATEGORY_NAME"] .= $category_name;
    $arg["data"]["CATEGORY_NAME"] .= "</select>";

    //集計結果表示
    $arg["data"]["R_DIV1_PRICE"] = number_format($r_div1_price);
    $arg["data"]["R_DIV2_PRICE"] = number_format($r_div2_price);
    $arg["data"]["R_TOTAL_PRICE"] = number_format($r_div1_price + $r_div2_price);
    $arg["data"]["R_TOTAL_NUM"] = number_format($r_total_num);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//教科書無償給与申し込みデータ編集
function makeTextbookFreeApplyEdit(&$objForm, &$arg, $db, &$model) {

    if ($model->schregno && $model->year && $model->register_date && $model->cmd != "knjb1213"){
        //データ取得
        $Row = $db->getRow(knjb1213Query::getSchregTextbookFreeApplyDat($model), DB_FETCHMODE_ASSOC);
    } else {
        $Row =& $model->field;
    }

    //登録日
    $Row["REGISTER_DATE"] = ($Row["REGISTER_DATE"] == "") ? CTRL_DATE : $Row["REGISTER_DATE"];
    $Row["REGISTER_DATE"] =  str_replace("-", "/", $Row["REGISTER_DATE"]);
    $arg["data"]["REGISTER_DATE"] = View::popUpCalendar($objForm, "REGISTER_DATE", $Row["REGISTER_DATE"]);

    //給与費対象額
    $extra = "style=\"text-align:right; background-color:darkgray\" readonly";
    $arg["data"]["TOTAL_GK"] = knjCreateTextBox($objForm, number_format($Row["TOTAL_GK"]), "TOTAL_GK", 7, 7, $extra);

    //教科書（金額）
    $extra = "style=\"text-align: right\" onblur=\"CalculatePrice(this);\" ";
    $arg["data"]["BOOKDIV1_GK"] = knjCreateTextBox($objForm, number_format($Row["BOOKDIV1_GK"]), "BOOKDIV1_GK", 7, 7, $extra);

    //学習書（金額）
    $extra = "style=\"text-align: right\" onblur=\"CalculatePrice(this);\" ";
    $arg["data"]["BOOKDIV2_GK"] = knjCreateTextBox($objForm, number_format($Row["BOOKDIV2_GK"]), "BOOKDIV2_GK", 7, 7, $extra);

    //冊数
    $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
    $arg["data"]["TOTAL_COUNT"] = knjCreateTextBox($objForm, $Row["TOTAL_COUNT"], "TOTAL_COUNT", 7, 7, $extra);

    //支給対象事由コンボ
    $query = knjb1213Query::getNameMst("B020");
    makeCmb($objForm, $arg, $db, $query, $Row["PROVIDE_REASON"], "PROVIDE_REASON", "", 1, $model, "BLANK");

    //添付書類
    $arg["data"]["ATTACH_DOCUMENTS"] = knjCreateTextBox($objForm, $Row["ATTACH_DOCUMENTS"], "ATTACH_DOCUMENTS", 30, 30, "");

    //備考
    $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 30, 30, "");

    //審査結果コンボ
    $query = knjb1213Query::getNameMst("B021");
    makeCmb($objForm, $arg, $db, $query, $Row["JUDGE_RESULT"], "JUDGE_RESULT", "", 1, $model, "BLANK");

    //登録日
    $Row["DECISION_DATE"] =  str_replace("-", "/", $Row["DECISION_DATE"]);
    $arg["data"]["DECISION_DATE"] = View::popUpCalendar($objForm, "DECISION_DATE", $Row["DECISION_DATE"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if ($name == "REGISTER_DATE_CMB") {
            $opt[] = array ("label" => str_replace("-","/",$row["LABEL"]),
                            "value" => $row["VALUE"]);
        } else {
            $opt[] = array ("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
        }
    }
    $result->free();

    if ($name == "REGISTER_DATE_CMB" && ($model->cmd == "main" || $model->cmd == "change_year")) {
        $value = ($value) ? $value : $opt[1]["value"];
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    if ($name == "REGISTER_DATE_CMB" && get_count($opt) == 1) $extra .= " style=\"width=100px\"";

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $meisai_cnt) {

    //更新
    $extra  = "onclick=\"return btn_submit('update');\"";
    $extra .= (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno && $model->year) ? "" : " disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除
    $extra  = "onclick=\"return btn_submit('delete');\"";
    $extra .= (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->schregno && $model->year && $model->register_date) ? "" : " disabled";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disable);

    //取消
    $extra  = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model){ 
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEARCH_DIV", $model->search_div);
    knjCreateHidden($objForm, "NAME", $model->name);
}
?>
