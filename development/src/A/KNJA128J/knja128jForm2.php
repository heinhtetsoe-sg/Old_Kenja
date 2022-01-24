<?php

require_once('for_php7.php');

class knja128jForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja128jindex.php", "", "form2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja128jQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja128jQuery::getTrainRow($model, ""), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $checkboxName1 = array("基本的な生活習慣", "健康・体力の向上", "自主・自律", "責任感", "創意工夫", "思いやり・協力", "生命尊重・自然愛護", "勤労・奉仕", "公正・公平", "公共心・公徳心");
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\" aria-label = \"".$checkboxName1[$i-1]."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */


        //特別活動の記録チェックボックス
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $checkboxName2 = array("学級活動", "生徒会活動", "学校行事");
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\" aria-label = \"".$checkboxName2[$i-1]."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */

        //特別活動の記録の観点
        $arg["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $row["SPECIALACTREMARK"], $model);
        $arg["SPECIALACTREMARK_COMMENT"] = "(全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで)";

        //学校種別
        $schoolkind = $db->getOne(knja128jQuery::getSchoolKind($model));

        //更新ボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || $schoolkind != 'J') ? "disabled" : " id = \"update2\" onclick=\"current_cursor('update2');return btn_submit('update2')\" aria-label =\"更新\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"clear2\" onclick=\"current_cursor('clear2');return btn_submit('clear2')\" aria-label =\"取消\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = " id = \"btn_back\" onclick=\"parent.current_cursor_focus(); return top.main_frame.right_frame.closeit()\" aria-label =\"戻る\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        //既入力内容参照（行動の記録・特別活動の記録）
        /* Edit by HPA for current_cursor and current_cursor start 2020/01/20 */
        $extra = " id= \"shokenlist_prg\" onclick=\"current_cursor('shokenlist_prg');loadwindow('" .REQUESTROOT ."/A/KNJA128J_SHOKEN/knja128j_shokenindex.php?cmd=edit&SCHREGNO={$model->schregno}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 500)\"";
        /* Edit by HPA for current_cursor and current_cursor end 2020/01/31 */
        $arg["button"]["shokenlist_prg"] = knjCreateBtn($objForm, "shokenlist_prg", "既入力内容の参照", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knja128jForm2.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
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
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"特別活動の記録の観点 全角14文字X7行まで\" id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
        knjCreateHidden($objForm, $name."_KETA", (int)$moji * 2);
        knjCreateHidden($objForm, $name."_GYO", $gyou);
        KnjCreateHidden($objForm, $name."_STAT", "statusarea".$name);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
