<?php

require_once('for_php7.php');


class knjx250Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成

        //ファイルからの取り込み
        $setSize = $model->Properties["useFileSize"] ? $model->Properties["useFileSize"] : 10240000;
        $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", $setSize);

        //アップロード先選択
        $opt = array(1, 2, 3);
        $model->field["UPLOAD_DIV"] = ($model->field["UPLOAD_DIV"] == "") ? "1" : $model->field["UPLOAD_DIV"];
        $extra = array("id=\"UPLOAD_DIV1\"", "id=\"UPLOAD_DIV2\"", "id=\"UPLOAD_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "UPLOAD_DIV", $model->field["UPLOAD_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["radio"][$key] = $val;

        //移動後のファイルパス単位
        $dataDir = DOCUMENTROOT ."/receive_send/";
        $dir_h = opendir($dataDir);

        while (false !== ($file_list[] = readdir($dir_h))) ;
        // ディレクトリハンドルを閉じる
        closedir($dir_h) ;

        $model->receive_send = array();
        $file_list2 = array() ;
        foreach ($file_list as $file_name) {
            if ($file_name == "." || $file_name == "..") {
                continue;
            }
            $filedir = DOCUMENTROOT ."/receive_send/" . $file_name;
            //ファイルのみを表示
            $fileDate = date("Y/m/d H:i:s", filemtime($filedir));
            $file_list2[] = array("fileName" => $file_name,
                                  "updated"  => $fileDate);
            $model->receive_send[$file_name] = "1";
        }
        $updated = array();
        foreach ($file_list2 as $key => $val) {
            //updatedでソートする準備
            $updated[$key] = $val["updated"];
        }

        // $file_list2 をファイルの更新日時でソート
        array_multisort($updated, SORT_DESC, $file_list2);

        foreach ($file_list2 as $key => $val) {
            $filename = $val["fileName"];
            $bb = str_replace("src", "", DOCUMENTROOT);
            $bb = str_replace("/usr/local", "", $bb);
            $filedir = $bb."receive_send/".$filename;
            $info = pathinfo($filedir);
            $setFilename = mb_convert_encoding($filename,"UTF-8", "SJIS-win");
            $setFiles = array();
            //拡張子
            if ($info["extension"] == "jpg" ||
                $info["extension"] == "bmp" ||
                $info["extension"] == "pdf" ||
                $info["extension"] == "xls" ||
                $info["extension"] == "xlsx" ||
                $info["extension"] == "doc" ||
                $info["extension"] == "docx" ||
                $info["extension"] == "ppt" ||
                $info["extension"] == "pptx" ||
                $info["extension"] == "zip" ||
                $info["extension"] == "lzh" ||
                $info["extension"] == "csv" ||
                $info["extension"] == "txt"
            ) {
                $setFiles["DOWNLOAD_FILE_NAME2"] = $setFilename;
                $setFiles["DOWNLOAD_FILE_TYPE2"] = $info["extension"];
                $arg["data"][] = $setFiles;
            }
            if ($info["extension"] == "jpg" ||
                $info["extension"] == "bmp"
            ) {
                $setFiles["DOWNLOAD_FILE_NAME"] = $setFilename;
                $setFiles["DOWNLOAD_DATA"] = $filedir;
                $arg["data2"][] = $setFiles;
            }
        }

        //移動後のファイルパス単位
        $dataDir = DOCUMENTROOT ."/receive_sendALP/";
        $dir_h2 = opendir($dataDir);

        while (false !== ($file_listAlp[] = readdir($dir_h2))) ;
        // ディレクトリハンドルを閉じる
        closedir($dir_h2) ;

        $file_listAlp2 = array() ;
        $model->receive_sendAlp = array();
        foreach ($file_listAlp as $file_name) {
            if ($file_name == "." || $file_name == "..") {
                continue;
            }
            $filedir = DOCUMENTROOT ."/receive_sendALP/" . $file_name;
            //ファイルのみを表示
            $fileDate = date("Y/m/d H:i:s", filemtime($filedir));
            $file_listAlp2[] = array("fileName" => $file_name,
                                     "updated"  => $fileDate);
            $model->receive_sendAlp[$file_name] = "1";
        }
        $updatedALP = array();
        foreach ($file_listAlp2 as $key => $val) {
            //updatedでソートする準備
            $updatedALP[$key] = $val["updated"];
        }

        // $file_listAlp をファイルの更新日時でソート
        array_multisort($updatedALP, SORT_DESC, $file_listAlp2);

        foreach ($file_listAlp2 as $key => $val) {
            $filename = $val["fileName"];
            $bb = str_replace("src", "", DOCUMENTROOT);
            $bb = str_replace("/usr/local", "", $bb);
            $filedir = $bb."receive_sendALP/".$filename;
            $info = pathinfo($filedir);
            $setFilename = mb_convert_encoding($filename,"UTF-8", "SJIS-win");
            $setFiles = array();
            //拡張子
            if ($info["extension"] == "jpg" ||
                $info["extension"] == "bmp" ||
                $info["extension"] == "pdf" ||
                $info["extension"] == "xls" ||
                $info["extension"] == "xlsx" ||
                $info["extension"] == "doc" ||
                $info["extension"] == "docx" ||
                $info["extension"] == "ppt" ||
                $info["extension"] == "pptx" ||
                $info["extension"] == "zip" ||
                $info["extension"] == "lzh" ||
                $info["extension"] == "csv" ||
                $info["extension"] == "txt"
            ) {
                $setFiles["DOWNLOAD_FILE_NAME_ALP"] = $setFilename;
                $setFiles["DOWNLOAD_FILE_TYPE_ALP"] = $info["extension"];
                $arg["data3"][] = $setFiles;
            }
            if ($info["extension"] == "jpg" ||
                $info["extension"] == "bmp"
            ) {
                $setFiles["DOWNLOAD_FILE_NAME"] = $setFilename;
                $setFiles["DOWNLOAD_DATA"] = $filedir;
                $arg["data2"][] = $setFiles;
            }
        }

        //textbox
        $extra = "";
        $arg["ZIP_PASS"] = knjCreateTextBox($objForm, $model->zipPass, "ZIP_PASS", 50, 100, $extra);

        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm);

        $arg["start"]   = $objForm->get_start("knjx250Form1", "POST", "knjx250index.php", "", "knjx250Form1");
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjx250Form1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJX250");
}
?>
