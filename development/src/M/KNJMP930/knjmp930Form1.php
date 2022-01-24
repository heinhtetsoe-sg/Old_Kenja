<?php

require_once('for_php7.php');

class knjmp930Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjmp930Form1", "POST", "knjmp930index.php", "", "knjmp930Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "clear") unset($model->field);

        //年度
        $extra = "";
        $query = knjmp930Query::getYear();
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //施行科目（とりあえずLEVY_L_MST）
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjmp930Query::getLevyLDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["SEKOU_L_CD"], "SEKOU_L_CD", $extra, 1, "BLANK", $model);
        
        //施行項目（とりあえずLEVY_M_MST）
        $extra = "";
        $query = knjmp930Query::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["SEKOU_L_M_CD"], "SEKOU_L_M_CD", $extra, 1, "BLANK", $model);

        //伝票番号
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toAlphaNumber(this.value)\"";
        $value = $model->field["REQUEST_NO"];
        $arg["data"]["REQUEST_NO"] = knjCreateTextBox($objForm, $value, "REQUEST_NO", 10, 10, $extra);

        //伺い日付
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$model->field["REQUEST_DATE"]),"");

        //新規ボタン
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP930_MAIN/knjmp930_mainindex.php?cmd=main&&SEND_AUTH=".AUTHORITY."&SEND_YEAR={$model->field["YEAR"]}&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
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
        $query = knjmp930Query::selectQuery($db, $model);
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
                    $subdata = "wopen('".REQUESTROOT."/M/KNJMP930_MAIN/knjmp930_mainindex.php?&cmd=main&SEND_YEAR=".$row["YEAR"]."&SEND_SEKOU_L_CD=".$row["SEKOU_L_CD"]."&SEND_SEKOU_M_CD=".$row["SEKOU_M_CD"]."&SEND_SEKOU_L_M_CD=".$row["SEKOU_L_CD"].$row["SEKOU_M_CD"]."&SEND_REQUEST_NO=".$row["REQUEST_NO"]."&SEND_AUTH=".AUTHORITY.$extra;
                    $row["REQUEST_NO"] = View::alink("#", htmlspecialchars($row["REQUEST_NO"]),"onclick=\"$subdata\"");
                    
                    $row["SEKOU_L_NAME"] = $db->getOne(knjmp930Query::getLevyLDiv($model, $row["SEKOU_L_CD"]));
                    $row["SEKOU_M_NAME"] = $db->getOne(knjmp930Query::getLevyMDiv($model, $row["SEKOU_L_CD"], $row["SEKOU_M_CD"]));

                    $row["YEAR_SET"] = $row["YEAR"].'年度';
                    $row["REQUEST_DATE"] = str_replace("-", "/", $row["REQUEST_DATE"]);
                    
                    //支出伺のチェック
                    $RowOutgo = array();
                    $RowOutgo = $db->getRow(knjmp930Query::getOutgoData($model, $setRequestNo), DB_FETCHMODE_ASSOC);
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
        View::toHTML($model, "knjmp930Form1.html", $arg);
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