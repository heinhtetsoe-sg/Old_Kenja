<?php

require_once('for_php7.php');

class knjx_sienkeikakuForm
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form", "POST", "knjx_sienkeikakuindex.php", "", "form");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学年の取得
        $row = $db->getRow(knjx_sienkeikakuQuery::selectQuery($model), DB_FETCHMODE_ASSOC);
        $extra = 'readonly = "readonly"';
        $arg['CHALLENGED_NAMES'] = getTextOrArea($objForm, 'CHALLENGED_NAMES', $model->disability_moji, $model->disability_gyou, $row['CHALLENGED_NAMES'], $extra);
        $arg['CHALLENGED_STATUS'] = getTextOrArea($objForm, 'CHALLENGED_STATUS', $model->disability_moji, $model->disability_gyou, $row['CHALLENGED_STATUS'], $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_sienkeikakuForm.html", $arg);
    }
}
//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $inextra)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = (int)$gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\"".$inextra;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = $inextra;
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
