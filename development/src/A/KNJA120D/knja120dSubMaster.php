<?php

require_once('for_php7.php');

class knja120dSubMaster
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("submaster", "POST", "knja120dindex.php", "", "submaster");

        //DB接続
        $db = Query::dbCheckOut();
        
        //学年取得
        $getGradeName = $db->getOne(knja120dQuery::getGradeName($model));
        
        //マスタ情報
        if ($model->cmd === 'teikei_act') {
            $setTitle = '総合的な学習の時間の記録（活動内容）';
        } else {
            $setTitle = '総合的な学習の時間の記録（評価）';
        }
        
        $arg["TITLE"] = $getGradeName.'　'.$setTitle;

        $rirekiCnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja120dSubMaster.html", $arg); 
    }
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $i = 0;
    $query = knja120dQuery::getHtrainRemarkTempDat($model);
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
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
?>

