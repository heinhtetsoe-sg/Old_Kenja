<?php

require_once('for_php7.php');

class knjp920Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp920Form1", "POST", "knjp920index.php", "", "knjp920Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "clear") unset($model->field);

        //年度
        $extra = "";
        $query = knjp920Query::getYear();
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp920Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "", $model);

        //購入科目（とりあえずKOUNYU_L_MST）
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjp920Query::getLevyLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["KOUNYU_L_CD"], "KOUNYU_L_CD", $extra, 1, "BLANK", $model);
        
        //購入項目（とりあえずKOUNYU_M_MST）
        $extra = "";
        $query = knjp920Query::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["KOUNYU_L_M_CD"], "KOUNYU_L_M_CD", $extra, 1, "BLANK", $model);

        //伝票番号
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toAlphaNumber(this.value)\"";
        $value = $model->field["REQUEST_NO"];
        $arg["data"]["REQUEST_NO"] = knjCreateTextBox($objForm, $value, "REQUEST_NO", 10, 10, $extra);

        //伺い日付
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$model->field["REQUEST_DATE"]),"");

        //新規ボタン
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP920_MAIN/knjp920_mainindex.php?cmd=main&&SEND_AUTH=".AUTHORITY."&SEND_SCHOOL_KIND=".$model->schoolKind."&SEND_YEAR={$model->field["YEAR"]}&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
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
        $query = knjp920Query::selectQuery($db, $model);
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
                    $setRequestNo = $row["REQUEST_NO"];
                    //リンク設定
                    $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
                    $subdata = "wopen('".REQUESTROOT."/P/KNJP920_MAIN/knjp920_mainindex.php?&cmd=main&SEND_SCHOOL_KIND=".$row["SCHOOL_KIND"]."&SEND_YEAR=".$row["YEAR"]."&SEND_KOUNYU_L_CD=".$row["KOUNYU_L_CD"]."&SEND_KOUNYU_M_CD=".$row["KOUNYU_M_CD"]."&SEND_KOUNYU_L_M_CD=".$row["KOUNYU_L_CD"].$row["KOUNYU_M_CD"]."&SEND_REQUEST_NO=".$row["REQUEST_NO"]."&SEND_AUTH=".AUTHORITY.$extra;
                    $row["REQUEST_NO"] = View::alink("#", htmlspecialchars($row["REQUEST_NO"]),"onclick=\"$subdata\"");
                    
                    $row["KOUNYU_L_NAME"] = $db->getOne(knjp920Query::getLevyLDiv($model, $row["KOUNYU_L_CD"]));
                    $row["KOUNYU_M_NAME"] = $db->getOne(knjp920Query::getLevyMDiv($model, $row["KOUNYU_L_CD"], $row["KOUNYU_M_CD"]));

                    $row["YEAR_SET"] = $row["YEAR"].'年度';
                    $row["REQUEST_DATE"] = str_replace("-", "/", $row["REQUEST_DATE"]);
                    
                    //支出伺のチェック
                    $RowOutgo = array();
                    $RowOutgo = $db->getRow(knjp920Query::getOutgoData($model, $setRequestNo), DB_FETCHMODE_ASSOC);
                    if (is_array($RowOutgo)) {
                        if ($RowOutgo["OUTGO_APPROVAL"] === '1' && $RowOutgo["OUTGO_CANCEL"] == "") {
                            $row["SET_STATUS"] = $RowOutgo["REQUEST_NO"].'<font color="red">(決済 済み)</font>';
                        } else if ($RowOutgo["OUTGO_CANCEL"] === '1') {
                            $row["SET_STATUS"] = $RowOutgo["REQUEST_NO"].'<font color="red">(キャンセル)</font>';
                        } else {
                            $row["SET_STATUS"] = $RowOutgo["REQUEST_NO"].'<font>(伺い中)</font>';
                        }
                    } else {
                        $row["SET_STATUS"] = '未作成';
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
        View::toHTML($model, "knjp920Form1.html", $arg);
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
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
