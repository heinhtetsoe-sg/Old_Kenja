<?php

require_once('for_php7.php');

class knjp905_schregTeikei
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("submaster", "POST", "knjp905_schregindex.php", "", "submaster");

        //DB接続
        $db = Query::dbCheckOut();

        //支出科目
        $query = knjp905_schregQuery::getLevyMDiv($model);
        $arg["TITLE"] = $db->getOne($query);

        //リスト作成
        makeList($objForm, $arg, $db, $model);

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
        View::toHTML($model, "knjp905_schregTeikei.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model)
{
    $i = 0;
    $query = knjp905_schregQuery::getLevySDiv($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        //選択checkbox
        $extra = "id=\"CHECK{$i}\"";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["LEVY_S_NAME"], $extra);

        $row["NO"]    = $i + 1;

        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}
