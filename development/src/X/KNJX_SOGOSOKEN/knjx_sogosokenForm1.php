<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjx_sogosokenForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjx_sogosokenindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;
        $Row = $db->getRow(knjx_sogosokenQuery::selectQuery($model, '01'), DB_FETCHMODE_ASSOC);
        $name = "TOTALREMARK";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg['data']['DATA1'] = knjCreateTextArea($objForm, 'DATA1', $gyou, $cols, "soft", $extra, $Row[$name]);

        $Row = $db->getRow(knjx_sogosokenQuery::selectQuery($model, '02'), DB_FETCHMODE_ASSOC);
        $name = "TOTALREMARK";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg['data']['DATA2'] = knjCreateTextArea($objForm, 'DATA2', $gyou, $cols, "soft", $extra, $Row[$name]);

        $Row = $db->getRow(knjx_sogosokenQuery::selectQuery($model, '03'), DB_FETCHMODE_ASSOC);
        $name = "TOTALREMARK";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg['data']['DATA3'] = knjCreateTextArea($objForm, 'DATA3', $gyou, $cols, "soft", $extra, $Row[$name]);

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_sogosokenForm1.html", $arg);
    }
}
