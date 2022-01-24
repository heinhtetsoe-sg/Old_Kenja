<?php

require_once('for_php7.php');

class knjd132dSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjd132dindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //学年表示
        $grade = $db->getRow(knjd132dQuery::getGrade($model), DB_FETCHMODE_ASSOC);
        $arg["GRADE"] = $grade["LABEL"];
        knjCreateHidden($objForm, "GRADE", $grade["VALUE"]);
        $model->subField["GRADE"] = $grade["VALUE"];

        //リスト作成
        $datacnt = makeList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $datacnt, $model->field["TEIKEI_CMD"]);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd132dSubForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $datacnt = 0;
    $query = knjd132dQuery::getHtrainRemarkTempDatIkkatsu($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $extra = "id=\"CHECK\"";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["REMARK"], $extra, "1");
        $arg["data"][] = $row;
        $datacnt++;
    }
    $result->free();
    return $datacnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $datacnt, $teikeiCmd) {
    //選択ボタン
    $extra = "onclick=\"return btn_submit2('".$datacnt."', '".$teikeiCmd."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>

