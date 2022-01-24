<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_3TargetClass
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_3index.php", "", "subform");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = $model->exp_year;

        //教科・科目コンボ
        $query = knjd425n_3Query::getSubclasscdCombo($model);
        $model->targetClassField["SUBCLASSCD"] = $model->targetClassField["SUBCLASSCD"] ? $model->targetClassField["SUBCLASSCD"] : $model->field["SUBCLASSCD"];
        $extra = "onChange=\"btn_submit('changeTargetClassSubclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, $model->targetClassField["SUBCLASSCD"], "SUBCLASSCD", $extra, 1);

        //リスト取得
        $query = knjd425n_3Query::getVClass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cnt = $db->getOne(knjd425n_3Query::checkTargetClass($model, $model->targetClassField["SUBCLASSCD"], $row["SCHOOL_KIND"], $row["CLASSCD"]));
            $extra = $cnt ? "checked" : "";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"], $row["CLASSCD"].":".$row["SCHOOL_KIND"], $extra);

            $arg["list"][] = $row;
        }

        //DB切断
        Query::dbCheckIn($db);

        //登録ボタンを作成
        $extra = "onclick=\"return btn_submit('targetClassInsert');\"";
        $arg["btn_insert"] = KnjCreateBtn($objForm, "btn_insert", "登 録", $extra);

        //戻るボタンを作成
        $extra = "onclick=\"parent.closeit();\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "targetClassInsertEnd") {
            $arg["parent_reload"] = "top.main_frame.right_frame.btn_submit('updateEnd');";
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425n_3TargetClass.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
