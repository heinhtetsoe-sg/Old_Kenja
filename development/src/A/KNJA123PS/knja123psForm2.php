<?php

require_once('for_php7.php');

class knja123psForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja123psindex.php", "", "form2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja123psQuery::getBehavior($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja123psQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);

        //行動の記録チェックボックス
        for($i=1; $i<11; $i++)
        {
            $ival = "3" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." readonly=\"readonly\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録チェックボックス
        for($i=1; $i<5; $i++)
        {
            $ival = "4" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." readonly=\"readonly\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $extra = "readonly=\"readonly\"";
        $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 2, 27, "soft", $extra, $row["SPECIALACTREMARK"]);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja123psForm2.html", $arg);
    }
}
?>