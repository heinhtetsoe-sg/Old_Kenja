<?php

require_once('for_php7.php');

class knja127pSubMaster
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("submaster", "POST", "knja127pindex.php", "", "submaster");

        //DB接続
        $db = Query::dbCheckOut();

        //学年取得
        $getGradeName = $db->getOne(knja127pQuery::getGradeName($model));

        //マスタ情報
        $setTitle = '道徳';

        $arg["TITLE"] = $getGradeName.'　'.$setTitle;

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
        View::toHTML($model, "knja127pSubMaster.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $query = knja127pQuery::getHtrainRemarkTempSemesDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["REMARK"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        $row["REMARK_SHOW"]  = str_replace("\n", "<br>", $row["REMARK"]);

        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}
?>
