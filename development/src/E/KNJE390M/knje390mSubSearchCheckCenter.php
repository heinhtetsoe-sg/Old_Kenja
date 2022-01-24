<?php

require_once('for_php7.php');

class knje390mSubSearchCheckCenter
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subsearch", "POST", "knje390mindex.php", "", "subsearch");

        //DB接続
        $db = Query::dbCheckOut();

        //マスタ情報
        $setTitle = '検査機関一覧';
        $arg["TITLE"] = $setTitle;

        //圏域
        $query = knje390mQuery::getNameMst("E040");
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
        View::toHTML($model, "knje390mSubSearchCheckCenter.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $i = 0;
    $query = knje390mQuery::getSearchCheckCenter($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
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
    $extra = "onclick=\"return btn_check_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //検索ボタン
    $extra = " onclick=\"return btn_submit('check_center_search')\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
?>

