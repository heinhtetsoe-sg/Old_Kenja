<?php

require_once('for_php7.php');

class knjf151form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;


        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "new") {
            unset($model->visit_date);
            unset($model->visit_hour);
            unset($model->visit_minute);
            $model->field = array();
        }

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && isset($model->visit_date) && isset($model->visit_hour) && isset($model->visit_minute) && !isset($model->warning)) {
            $val = $model->visit_date.":".$model->visit_hour.":".$model->visit_minute;
            $query = knjf151Query::getList($model, "data", $val);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入力不可
        $disable = ($model->visit_date) ? " disabled " : "";

        //来室日付
        if ($model->visit_date) $Row["VISIT_DATE"] = $model->visit_date;
        if ($Row["VISIT_DATE"] == "") $Row["VISIT_DATE"] = CTRL_DATE;
        $arg["data"]["VISIT_DATE"] = View::popUpCalendarAlp($objForm, "VISIT_DATE", str_replace("-", "/", $Row["VISIT_DATE"]), $disable);

        //来室時間（時）
        if ($model->visit_hour) $Row["VISIT_HOUR"] = $model->visit_hour;
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $Row["VISIT_HOUR"], "VISIT_HOUR", 2, 2, $extra.$disable);

        //来室時間（分）
        if ($model->visit_minute) $Row["VISIT_MINUTE"] = $model->visit_minute;
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $Row["VISIT_MINUTE"], "VISIT_MINUTE", 2, 2, $extra.$disable);

        //相談者
        $query = knjf151Query::getNameMst('F221');
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $Row["RELATIONSHIP"], "", 1, "blank");

        //相談区分
        $query = knjf151Query::getNameMst('F215');
        makeCmb($objForm, $arg, $db, $query, "CONSULTATION_METHOD", $Row["CONSULTATION_METHOD"], "", 1, "blank");

        //相談内容（人間関係）
        $query = knjf151Query::getNameMst('F222');
        makeCmb($objForm, $arg, $db, $query, "CONSULTATION_KIND1", $Row["CONSULTATION_KIND1"], "", 1, "blank");

        //相談内容（学校生活）
        $query = knjf151Query::getNameMst('F223');
        makeCmb($objForm, $arg, $db, $query, "CONSULTATION_KIND2", $Row["CONSULTATION_KIND2"], "", 1, "blank");

        //特記事項
        $extra = "";
        $arg["data"]["SPECIAL_NOTE"] = knjCreateTextBox($objForm, $Row["SPECIAL_NOTE"], "SPECIAL_NOTE", 60, 100, $extra);

        //記録
        $extra = "";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 4, 51, "soft", $extra, $Row["REMARK"]);

        //ファイルからの取り込み
        $arg["data"]["PDF_FILE"] = knjCreateFile($objForm, "PDF_FILE", "", 512000);

        //実行ボタン
        $extra  = ($model->visit_date && $model->schregno) ? "" : "disabled";
        $extra .= " onclick=\"return btn_submit('upload');\"";
        $arg["button"]["btn_upload"] = knjCreateBtn($objForm, "btn_upload", "実 行", $extra);

        //PDFファイル
        $pdf_file = false;
        if ($model->schregno && $model->visit_date) {
            $pdf_name = $model->schregno.'_'.preg_replace("#-|/#", '', $model->visit_date).$model->visit_hour.$model->visit_minute;
            $path_file = $pdf_name.'.pdf';
            $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
            $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
            if (file_exists($path_file) && ($fp = fopen($path_file, "r")) && ($content_length = filesize($path_file)) > 0) {
                $pdf_file = true;
            }
            @fclose($fp);
        }
        knjCreateHidden($objForm, "CHECK_PDF", $pdf_file);

        //使用不可
        $disable = ($model->schregno == "") ? " disabled" : "";

        //新規ボタン
        $extra = "onclick=\"return btn_submit('new');\"";
        $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra.$disable);

        //追加ボタン
        if ($model->visit_date == "") {
            $extra = "onclick=\"return btn_submit('add');\"";
            $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disable);
        }

        //更新ボタン
        if ($model->visit_date) {
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disable);
        }

        //削除ボタン
        if ($model->visit_date) {
            $extra = "onclick=\"return btn_submit('delete');\"";
            $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disable);
        }

        //取消ボタン
        if ($model->visit_date) {
            $extra = "onclick=\"return btn_submit('clear');\"";
            $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disable);
        }

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf151index.php", "", "edit");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear"  && VARS::get("cmd") != "new" && !isset($model->warning)) {
            $arg["reload"] = "window.open('knjf151index.php?cmd=list','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf151Form2.html", $arg);
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
