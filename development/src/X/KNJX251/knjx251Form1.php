<?php

require_once('for_php7.php');


class knjx251Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成

        //ファイルからの取り込み
        $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);

        //ダウンロード先選択
        $opt = array(1, 2);
        $model->field["DOWNLOAD_DIV"] = ($model->field["DOWNLOAD_DIV"] == "") ? "1" : $model->field["DOWNLOAD_DIV"];
        $extra = array("id=\"DOWNLOAD_DIV1\" onClick=\"return btn_submit('knjx251');\"", "id=\"DOWNLOAD_DIV2\" onClick=\"return btn_submit('knjx251');\"");
        $radioArray = knjCreateRadio($objForm, "DOWNLOAD_DIV", $model->field["DOWNLOAD_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["radio"][$key] = $val;

        //移動後のファイルパス単位
        if ($model->field["DOWNLOAD_DIV"] == "1") {
            $dataDir = str_replace("src", "pdf_design/template_xml", DOCUMENTROOT);
        } else {
            $dataDir = str_replace("src", "pdf_design/xml", DOCUMENTROOT);
        }
        $aa = opendir($dataDir);
        while (false !== ($filename = readdir($aa))) {

            $bb = $dataDir."/";
            $filedir = $bb.$filename;
            $info = pathinfo($filedir);
            $setFilename = mb_convert_encoding($filename,"UTF-8", "SJIS-win");
            $setFiles = array();
            //拡張子
            if ($info["extension"] == "xml") {
                $setFiles["DOWNLOAD_FILE_NAME2"] = $setFilename;
                $setFiles["DOWNLOAD_FILE_TYPE2"] = $info["extension"];
                $arg["data"][] = $setFiles;
            }
        }
        closedir($aa);

        //textbox
        $extra = "";
        $arg["ZIP_PASS"] = knjCreateTextBox($objForm, $model->zipPass, "ZIP_PASS", 50, 100, $extra);

        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm);

        $arg["start"]   = $objForm->get_start("knjx251Form1", "POST", "knjx251index.php", "", "knjx251Form1");
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjx251Form1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
    //ダウンロード
    $extra = "onclick=\"return btn_submit('csvGet');\"";
    $arg["button"]["btn_csvGet"] = knjCreateBtn($objForm, "btn_csvGet", "ダウンロード", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('del');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJX251");
}

?>
