<?php

require_once('for_php7.php');

class knja128aSubForm7
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form", "POST", "knja128aindex.php", "", "form");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $data = array();
        $result = $db->query(knja128aQuery::selectGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data[] = $row;
        }
        $arg['GRADENAME1'] = $data[0]['GRADE_NAME1'];
        $arg['GRADENAME2'] = $data[1]['GRADE_NAME1'];

        //登録データ
        $row1 = $db->getRow(knja128aQuery::selectKinyuuyroku2($model, $data[0]['GRADE']), DB_FETCHMODE_ASSOC);
        $row2 = $db->getRow(knja128aQuery::selectKinyuuyroku2($model, $data[1]['GRADE']), DB_FETCHMODE_ASSOC);
        $extra = 'readonly = "readonly"';
        $arg['REMARK1_1'] = getTextOrArea($objForm, 'REMARK1_1', $model->indep_remark_moji, $model->indep_remark_gyou, $row1['REMARK1'], $extra);
        $arg['REMARK1_2'] = getTextOrArea($objForm, 'REMARK1_2', $model->indep_remark_moji, $model->indep_remark_gyou, $row2['REMARK1'], $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128aSubForm7.html", $arg);
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
