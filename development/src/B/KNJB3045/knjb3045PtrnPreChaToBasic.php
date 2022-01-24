<?php

require_once('for_php7.php');

class knjb3045PtrnPreChaToBasic
{
    function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3045index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TEXTBOX", "PREV_BSCSEQ");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb3045PtrnPreChaToBasic.html", $arg);

    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $extra = " onchange='return chkChange(this);' ";
    $weekJp = array('日', '月', '火', '水', '木', '金', '土');
    $query = knjb3045Query::selectSchPtrnPreChaToBasicDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //選択チェックボックス
        $row["CHECK"] = knjCreateCheckBox($objForm, 'CHECK', $row['BSCSEQ'], $extra);

        $dateTime = explode('.', $row["UPDATED"]);
        list($setDate, $setTime) = explode(' ', $dateTime[0]);
        $setWeek = $weekJp[date('w', strtotime($row["UPDATED"]))];
        $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
        $row["TITLE"] = sprintf('%02d', $row["BSCSEQ"]).' '.$dispDate.' '.$row["TITLE"];
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

?>
