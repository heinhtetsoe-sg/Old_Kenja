<?php

require_once('for_php7.php');

class knje450SubRef1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("reference1", "POST", "knje450index.php", "", "reference1");

        //DB接続
        $db = Query::dbCheckOut();

        //一覧作成
        $listCnt = makeList($objForm, $arg, $db, $model);

        //選択ボタン
        $extra = ($listCnt > 0) ? "onclick=\"return btn_submit('".$listCnt."')\"" : "disabled";
        $arg["button"]["btn_select"] = knjCreateBtn($objForm, "btn_select", "選 択", $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje450SubRef1.html", $arg); 
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $query = knje450Query::getChallengedNameMst($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["NAME"], " id=\"CHECK{$i}\"", "1");
        $row["NAME"] = "<LABEL for=\"CHECK{$i}\">{$row["NAME"]}</LABEL>";

        $arg["data"][] = $row;
        $i++;
    }
    $result->free();

    return $i;
}
?>

