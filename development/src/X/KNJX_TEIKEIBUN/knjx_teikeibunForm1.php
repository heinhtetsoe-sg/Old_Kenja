<?php

require_once('for_php7.php');

class knjx_teikeibunForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("teikeibun", "POST", "knjx_teikeibunindex.php", "", "teikeibun");

        //DB接続
        $db = Query::dbCheckOut();

        //学年取得
        $getGradeName = $db->getOne(knjx_teikeibunQuery::getGradeName($model));
        //タイトル
        $arg["TITLE"] = $getGradeName.'　'.$model->title;

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjx_teikeibunForm1.html", $arg);
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $query = knjx_teikeibunQuery::getHtrainRemarkTempDat($model);
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
        
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
    //選択ボタン
    $extra = "onclick=\"return btn_submit('".$i."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
    knjCreateHidden($objForm, "TEXTBOX", $model->textBox);
}
?>

