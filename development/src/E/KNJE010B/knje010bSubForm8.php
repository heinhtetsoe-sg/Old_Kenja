<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010bSubForm8 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form8", "POST", "knje010bindex.php", "", "form8");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  ".$model->name;

        //部活動リスト
        $counter = 0;

        $moji = 50;
        $gyou = 36;

        //ALLチェック
        $extra = " id=\"CHECKALL\" onClick=\"check_all(this); OptionUse(this)\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //対象項目
        $itemArray = array("REMARK");
        knjCreateHidden($objForm, "item", implode(',', $itemArray));

        //テキストボックス
        $years = array();
        $query = knje010bQuery::getHexamEntremarkLearningDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $years[] = $row["YEAR"];

            //選択チェックボックス
            $extra = " id=\"CHECK_".$row["YEAR"]."\" onclick=\"OptionUse(this);\" class=\"rowselect\" ";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", "1", $extra, "1");

            $extra = " id=\"REMARK_".$row["YEAR"]."\" style=\"background-color:lightgray;\" ";
            $row['REMARK'] = KnjCreateTextArea($objForm, 'REMARK', ((int)$gyou + 1), ((int)$moji * 2 + 1), 'soft', $extra, $row["REMARK"]);

            $arg["data"][] = $row;
            $counter++;
        }
        //取込ボタン
        $extra = "style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$model->target}');\"";
        $arg["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "years", implode(",", $years));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010bSubForm8.html", $arg);
    }
}
?>
