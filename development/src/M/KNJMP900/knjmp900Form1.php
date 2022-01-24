<?php

require_once('for_php7.php');

class knjmp900Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjmp900Form1", "POST", "knjmp900index.php", "", "knjmp900Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "clear") unset($model->field);

        //年度
        $extra = "";
        $query = knjmp900Query::getYear();
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //収入科目（とりあえずINCOME_L_MST）
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjmp900Query::getLevyLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["INCOME_L_CD"], "INCOME_L_CD", $extra, 1, "BLANK", $model);
        
        //収入項目（とりあえずINCOME_M_MST）
        $extra = "";
        $query = knjmp900Query::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["INCOME_L_M_CD"], "INCOME_L_M_CD", $extra, 1, "BLANK", $model);

        //伝票番号
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toAlphaNumber(this.value)\"";
        $value = $model->field["REQUEST_NO"];
        $arg["data"]["REQUEST_NO"] = knjCreateTextBox($objForm, $value, "REQUEST_NO", 10, 10, $extra);

        //伺い日付
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$model->field["REQUEST_DATE"]),"");

        //新規ボタン
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP900_MAIN/knjmp900_mainindex.php?cmd=main&&SEND_AUTH=".AUTHORITY."&SEND_YEAR={$model->field["YEAR"]}&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
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
        $query = knjmp900Query::selectQuery($db, $model);
        $searchCnt = ($model->cmd == "search") ?get_count($db->getCol($query)) : "0";
        $arg["SEARCH_CNT"] = $searchCnt;
        $arg["SEARCH_CNT_MSG"] = ($model->cmd == "search" && $searchCnt == 0) ? "該当なし" : "";

        //リスト
        $checkCnt = 500;
        if($searchCnt > $checkCnt) {
            $model->setWarning("検索結果：".$searchCnt."件です。\\n表示可能件数は".$checkCnt."件までです。");
        } else {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->cmd == "search") {
                    //リンク設定
                    $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
                    $subdata = "wopen('".REQUESTROOT."/M/KNJMP900_MAIN/knjmp900_mainindex.php?&cmd=main&SEND_YEAR=".$row["YEAR"]."&SEND_INCOME_L_CD=".$row["INCOME_L_CD"]."&SEND_INCOME_M_CD=".$row["INCOME_M_CD"]."&SEND_INCOME_L_M_CD=".$row["INCOME_L_CD"].$row["INCOME_M_CD"]."&SEND_REQUEST_NO=".$row["REQUEST_NO"]."&SEND_AUTH=".AUTHORITY.$extra;
                    $row["REQUEST_NO"] = View::alink("#", htmlspecialchars($row["REQUEST_NO"]),"onclick=\"$subdata\"");
                    
                    $row["INCOME_L_NAME"] = $db->getOne(knjmp900Query::getLevyLDiv($model, $row["INCOME_L_CD"]));
                    $row["INCOME_M_NAME"] = $db->getOne(knjmp900Query::getLevyMDiv($model, $row["INCOME_L_CD"], $row["INCOME_M_CD"]));

                    $row["YEAR_SET"] = $row["YEAR"].'年度';
                    $row["REQUEST_DATE"] = str_replace("-", "/", $row["REQUEST_DATE"]);
                    
                    if ($row["INCOME_APPROVAL"] === '1' && $row["INCOME_CANCEL"] == "") {
                        $row["SET_STATUS"] = '済';
                    } else if ($row["INCOME_CANCEL"] === '1') {
                        $row["SET_STATUS"] = '<font size="1">キャンセル</font>';
                    }
                    
                    $arg["data2"][] = $row;
                }
            }
            $result->free();
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp900Form1.html", $arg);
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

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
