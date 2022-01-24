<?php

require_once('for_php7.php');

class knjp900Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp900Form1", "POST", "knjp900index.php", "", "knjp900Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "clear") unset($model->field);

        //年度
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjp900Query::getYear();
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp900Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "", $model);

        //収入科目（とりあえずINCOME_L_MST）
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjp900Query::getLevyLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["INCOME_L_CD"], "INCOME_L_CD", $extra, 1, "BLANK", $model);
        
        //収入項目（とりあえずINCOME_M_MST）
        $extra = "";
        $query = knjp900Query::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["INCOME_L_M_CD"], "INCOME_L_M_CD", $extra, 1, "BLANK", $model);

        //伝票番号
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toAlphaNumber(this.value)\"";
        $value = $model->field["REQUEST_NO"];
        $arg["data"]["REQUEST_NO"] = knjCreateTextBox($objForm, $value, "REQUEST_NO", 10, 10, $extra);

        //伺い対象月
        $yMonth_array = array();
        $yMonth_array[] = array("label" => "", "value" => "");
        $nendo = $model->field["YEAR"];
        for ($j = 1; $j <= 2; $j++) {
            for ($i = 4; $i < 16; $i++) {
                $targetYear  = ($i > 12) ? $nendo + 1 : $nendo;
                $targetMonth = ($i > 12) ? ($i - 12) : $i;
                $yMonth_array[] = array("label" => $targetYear ."年 ". $targetMonth. "月",
                                        "value" => $targetYear ."-". sprintf("%02d", $targetMonth));
            }
            $nendo++;
        }
        $extra = "";
        $arg["data"]["SEARCH_YMONTH"] = knjCreateCombo($objForm, "SEARCH_YMONTH", $model->field["SEARCH_YMONTH"], $yMonth_array, $extra, "1");

        //伺い日付
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$model->field["REQUEST_DATE"]),"");

        //収入日付
        $arg["data"]["INCOME_DATE"] = View::popUpCalendar($objForm, "INCOME_DATE",str_replace("-","/",$model->field["INCOME_DATE"]),"");

        //決裁
        $extra = " id=\"INCOME_APPROVAL\" onclick=\"keepExclusiveChkGroup(this);\" class=\"apploval\" ";
        $checked = $model->field["INCOME_APPROVAL"] == '1' ? " checked " : "";
        $arg["data"]["INCOME_APPROVAL"] = knjCreateCheckBox($objForm, "INCOME_APPROVAL", "1", $checked.$extra);
        $extra = " id=\"NOT_INCOME_APPROVAL\" onclick=\"keepExclusiveChkGroup(this);\" class=\"apploval\" ";
        $checked = $model->field["NOT_INCOME_APPROVAL"] == '1' ? " checked " : "";
        $arg["data"]["NOT_INCOME_APPROVAL"] = knjCreateCheckBox($objForm, "NOT_INCOME_APPROVAL", "1", $checked.$extra);

        //新規ボタン
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP900_MAIN/knjp900_mainindex.php?cmd=main&&SEND_AUTH=".AUTHORITY."&SEND_SCHOOL_KIND=".$model->schoolKind."&SEND_YEAR={$model->field["YEAR"]}&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
        $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

        //検索ボタン
        $extra = "onclick=\"return btn_submit('search');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //検索結果
        $query = knjp900Query::selectQuery($db, $model);
        $searchCnt = ($model->cmd == "search") ?get_count($db->getCol($query)) : "0";
        $arg["SEARCH_CNT"] = $searchCnt;
        $arg["SEARCH_CNT_MSG"] = ($model->cmd == "search" && $searchCnt == 0) ? "該当なし" : "";

        //リスト
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->cmd == "search") {
                //リンク設定
                $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
                $subdata = "wopen('".REQUESTROOT."/P/KNJP900_MAIN/knjp900_mainindex.php?&cmd=main&SEND_SCHOOL_KIND=".$row["SCHOOL_KIND"]."&SEND_YEAR=".$row["YEAR"]."&SEND_INCOME_L_CD=".$row["INCOME_L_CD"]."&SEND_INCOME_M_CD=".$row["INCOME_M_CD"]."&SEND_INCOME_L_M_CD=".$row["INCOME_L_CD"].$row["INCOME_M_CD"]."&SEND_REQUEST_NO=".$row["REQUEST_NO"]."&SEND_AUTH=".AUTHORITY.$extra;
                $row["REQUEST_NO"] = View::alink("#", htmlspecialchars($row["REQUEST_NO"]),"onclick=\"$subdata\"");

                $row["INCOME_L_NAME"] = $db->getOne(knjp900Query::getLevyLDiv($model, $row["INCOME_L_CD"]));
                $row["INCOME_M_NAME"] = $db->getOne(knjp900Query::getLevyMDiv($model, $row["INCOME_L_CD"], $row["INCOME_M_CD"]));

                $row["YEAR_SET"] = $row["YEAR"].'年度';
                $row["REQUEST_DATE"] = str_replace("-", "/", $row["REQUEST_DATE"]);
                $row["INCOME_DATE"]  = str_replace("-", "/", $row["INCOME_DATE"]);

                if ($row["INCOME_APPROVAL"] === '1' && $row["INCOME_CANCEL"] == "") {
                    $row["SET_STATUS"] = '済';
                } else if ($row["INCOME_CANCEL"] === '1') {
                    $row["SET_STATUS"] = '<font size="1">キャンセル</font>';
                }

                $arg["data2"][] = $row;
            }
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp900Form1.html", $arg);
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
