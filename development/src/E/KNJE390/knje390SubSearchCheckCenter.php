<?php

require_once('for_php7.php');

class knje390SubSearchCheckCenter
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subsearch", "POST", "knje390index.php", "", "subsearch");

        //DB接続
        $db = Query::dbCheckOut();

        //マスタ情報
        $setTitle = '検査機関一覧';
        $arg["TITLE"] = $setTitle;
        // Add by PP for PC-Talker 2020-02-03 start
        $arg["TITLE_SEREEN"] = $setTitle."画面";
        echo "<script>var TITLE= '".$arg["TITLE_SEREEN"]."';
              </script>";
        // Add by PP for PC-Talker and focus 2020-02-20 end

        //圏域
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "aria-label=\"圏域\"";
        // Add by PP for PC-Talker and focus 2020-02-20 end
        $query = knje390Query::getNameMst("E040");
        makeCmb($objForm, $arg, $db, $query, "AREACD", $model->searchfield["AREACD"], $extra, 1, 1);

        //検査機関名
        $extra = "";
        $arg["NAME"] = knjCreateTextBox($objForm, $model->searchfield["NAME"], "NAME", 30, 30, $extra);

        //住所１
        $extra = "";
        $arg["ADDR1"] = knjCreateTextBox($objForm, $model->searchfield["ADDR1"], "ADDR1", 60, 100, $extra);

        //住所２
        $extra = "";
        $arg["ADDR2"] = knjCreateTextBox($objForm, $model->searchfield["ADDR2"], "ADDR2", 60, 100, $extra);

        //内容表示
        if ($model->cmd == "check_center_search" && ($model->searchfield["AREACD"] != "" || $model->searchfield["NAME"] != "" || $model->searchfield["ADDR1"] != "" || $model->searchfield["ADDR2"] != "")) {
            $rirekiCnt = makeList($objForm, $arg, $db, $model);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubSearchCheckCenter.html", $arg); 
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $query = knje390Query::getSearchCheckCenter($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        // Add by PP for PC-Talker 2020-02-03 start
        $check = "aria-label=\"{$row["KENIKI_NAME"]}の{$row["CENTER_NAME"]}\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["CENTER_NAME"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    // Add by PP for PC-Talker and focus 2020-02-03 start
    $extra = "onclick=\"parent.current_cursor_Inspection(); return btn_check_submit('".$i."')\" aria-label=\"選択\"";
    // Add by PP for PC-Talker and focus 2020-02-20 end
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //検索ボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = " id=\"btn_search\" onclick=\"current_cursor('btn_search'); return btn_submit('check_center_search')\" aria-label=\"検索\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end

    //戻るボタン
    // Add by PP for PC-Talker and current cursor 2020-02-03 start
    $extra = "onclick=\"parent.current_cursor_focus(); return parent.closeit()\" aria-label=\"戻る\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    // Add by PP for PC-Talker and current cursor 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
?>

