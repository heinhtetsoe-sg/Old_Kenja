<?php

require_once('for_php7.php');

class knjf151form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjf151index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報取得
        $arg["SCHINFO"] = $db->getOne(knjf151Query::getSchInfo($model->schregno));

        //データ取得
        if ($model->schregno) {
            $counter = 0;
            $result = $db->query(knjf151Query::getList($model, "list"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //PDFファイル
                $pdf_file = false;
                $pdf_name = $model->schregno.'_'.preg_replace("#-|/#", '', $row["VISIT_DATE"]).$row["VISIT_HOUR"].$row["VISIT_MINUTE"];
                $path_file = $pdf_name.'.pdf';
                $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
                $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
                if (file_exists($path_file) && ($fp = fopen($path_file, "r")) && ($content_length = filesize($path_file)) > 0) {
                    $pdf_file = true;
                }
                @fclose($fp);

                if ($pdf_file == true) {
                    $pdf_name = $model->schregno.'_'.preg_replace("#-|/#", '', $row["VISIT_DATE"]).$row["VISIT_HOUR"].$row["VISIT_MINUTE"];
                    $extra = "id=\"PDF{$counter}\" onclick=\"DownloadPdf(this);\"";
                    $row["PDF"] = knjCreateCheckBox($objForm, "PDF".$counter, $pdf_name, $extra, "")."<LABEL for=\"PDF{$counter}\">PDF</LABEL>";

                }

                //来室日時
                $row["VISIT_DATE"] = View::alink('knjf151index.php', str_replace("-","/",$row["VISIT_DATE"])." ".$row["VISIT_HOUR"].":".$row["VISIT_MINUTE"], "target=bottom_frame",
                                                    array("cmd"             => 'edit',
                                                          "VISIT_DATE"      => $row["VISIT_DATE"],
                                                          "VISIT_HOUR"      => $row["VISIT_HOUR"],
                                                          "VISIT_MINUTE"    => $row["VISIT_MINUTE"]));

                //相談内容
                $br = ($row["CONSULTATION_KIND1_SHOW"] && $row["CONSULTATION_KIND1_SHOW"]) ? "<br>" : "";
                $consultation_kind = "";
                if ($row["CONSULTATION_KIND1_SHOW"]) $consultation_kind .= $row["CONSULTATION_KIND1_SHOW"];
                if ($row["CONSULTATION_KIND2_SHOW"]) $consultation_kind .= $br.$row["CONSULTATION_KIND2_SHOW"];
                $row["CONSULTATION_KIND"] = $consultation_kind;

                $arg["data"][] = $row;
                $counter++;
            }
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PDF");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"] = "window.open('knjf151index.php?cmd=edit','bottom_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf151Form1.html", $arg);
    }
}
?>
