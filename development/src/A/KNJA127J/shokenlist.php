<?php

require_once('for_php7.php');

class shokenlist {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("detail", "POST", "knja127jindex.php", "", "detail");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //項目の表示内容を設定
        /* Edit by PP for PC-talker 読み start 2020/01/20 */
        $label = "の既入力内容の参照画面";
        if ($model->cmd === 'shokenlist1') {
            $arg["shokenlist1"] = "1";
            $arg["title_label"] = "総合的な学習の時間の記録$label";
        } else if ($model->cmd === 'shokenlist2') {
            $arg["shokenlist2"] = "1";
            $arg["title_label"] = "総合所見及び指導上参考となる諸事項$label";
        } else if ($model->cmd === 'shokenlist3') {
            $arg["shokenlist3"] = "1";
            $arg["title_label"] = "出欠の記録備考$label";
        } else if ($model->cmd === 'shokenlist4') {
            $arg["shokenlist4"] = "1";
            $arg["title_label"] = "行動の記録$label";
        } else if ($model->cmd === 'shokenlist5') {
            $arg["shokenlist5"] = "1";
            $arg["title_label"] = "道徳$label";
        }
        /* Edit by PP for PC-talker 読み end 2020/01/31 */

        //HTRAINREMARK_DAT 取得
        $model->data["YEAR"] = "";
        $result = $db->query(knja127jQuery::getTrainRow($model, "sanshou"));
        while ($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学習活動
            /* Edit by PP for PC-talker 読み start 2020/01/20 */
            $extra = "aria-label = \"".$Row["ANNUAL"]."学年の学習活動\"";
            $Row["TOTALSTUDYACT"] = getTextOrArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_moji, $model->totalstudyact_gyou, $Row["TOTALSTUDYACT"], $model, $extra);
            //観点
            $extra = "aria-label = \"".$Row["ANNUAL"]."学年の観点\"";
            $Row["VIEWREMARK"] = getTextOrArea($objForm, "VIEWREMARK", $model->viewremark_moji, $model->viewremark_gyou, $Row["VIEWREMARK"], $model, $extra);
            //評価
            $extra = "aria-label = \"".$Row["ANNUAL"]."学年の評価\"";
            $Row["TOTALSTUDYVAL"] = getTextOrArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_moji, $model->totalstudyval_gyou, $Row["TOTALSTUDYVAL"], $model, $extra);
            //特別活動所見
            $extra = "aria-label = \"特別活動所見\"";
            $Row["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $Row["SPECIALACTREMARK"], $model, $extra);
            //総合所見
            $extra = "aria-label = \"\"";
            $Row["TOTALREMARK"] = getTextOrArea($objForm, "TOTALREMARK", $model->totalremark_moji, $model->totalremark_gyou, $Row["TOTALREMARK"], $model, $extra);
            //出欠の記録備考
            $extra = "aria-label = \"\"";
            $Row["ATTENDREC_REMARK"] = getTextOrArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_moji, $model->attendrec_remark_gyou, $Row["ATTENDREC_REMARK"], $model, $extra);
            //行動の記録
            $extra = "aria-label = \"\"";
            $Row["BEHAVEREC_REMARK"] = getTextOrArea($objForm, "BEHAVEREC_REMARK", $model->behaverec_remark_moji, $model->behaverec_remark_gyou, $Row["BEHAVEREC_REMARK"], $model, $extra);
            //道徳
            $extra = "aria-label = \"\"";
            $Row["REMARK1"] = getTextOrArea($objForm, "REMARK1", $model->remark1_moji, $model->remark1_gyou, $Row["REMARK1"], $model, $extra);
            /* Edit by PP for PC-talker 読み end 2020/01/31 */
            $arg["data"][] = $Row;
        }
        $result->free();

        //戻るボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "onclick=\"parent.current_cursor_focus(); return parent.closeit()\" aria-label=\"戻る\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "shokenlist.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
/* Edit by PP for PC-talker 読み start 2020/01/20 */
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $setExtra = "") {
    /* Edit by PP for PC-talker 読み end 2020/01/31 */
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
        /* Edit by PP for PC-talker 読み start 2020/01/20 */
        $extra = " $setExtra style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ((int)$moji * 2), true);\"";
        /* Edit by PP for PC-talker 読み end 2020/01/31 */
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        /* Edit by PP for PC-talker 読み start 2020/01/20 */
        $extra = " $setExtra onkeypress=\"btn_keypress();\"";
        /* Edit by PP for PC-talker 読み end 2020/01/31 */
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
