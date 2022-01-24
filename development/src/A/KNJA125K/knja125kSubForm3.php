<?php

require_once('for_php7.php');

class knja125kSubForm3
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form", "POST", "knja125kindex.php", "", "form");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $data = array();
        $result = $db->query(knja125kQuery::selectGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data[] = $row;
        }
        $arg['GRADENAME1'] = $data[0]['GRADE_NAME1'];
        $arg['GRADENAME2'] = $data[1]['GRADE_NAME1'];

        //学年の取得
        $row1 = $db->getRow(knja125kQuery::selectKinyuuyroku($model, $data[0]['GRADE']), DB_FETCHMODE_ASSOC);
        $row2 = $db->getRow(knja125kQuery::selectKinyuuyroku($model, $data[1]['GRADE']), DB_FETCHMODE_ASSOC);
        $extra = 'readonly = "readonly"';
        $arg['TOTALREMARK1'] = getTextOrArea($objForm, 'TOTALREMARK1', $model->textSize["TOTALREMARK"]['moji'], $model->textSize["TOTALREMARK"]['gyo'], $row1['TOTALREMARK'], $extra);
        $arg['TOTALREMARK2'] = getTextOrArea($objForm, 'TOTALREMARK2', $model->textSize["TOTALREMARK"]['moji'], $model->textSize["TOTALREMARK"]['gyo'], $row2['TOTALREMARK'], $extra);

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\" aria-label = \"戻る\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125kSubForm3.html", $arg);
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
