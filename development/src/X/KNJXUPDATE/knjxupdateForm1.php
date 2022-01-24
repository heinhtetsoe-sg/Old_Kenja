<?php

require_once('for_php7.php');


class knjxupdateForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成

        //ファイルからの取り込み
        $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);

        //移動後のファイルパス単位
        $dataDir = DOCUMENTROOT."/sousaManual/";
        $aa = opendir($dataDir);

        $db = Query::dbCheckOut();
        
        //学校名取得
        $gakuQuery = knjxupdateQuery::getGakuName();
        $gakkoname = $db->getOne($gakuQuery);
        
        
        while (false !== ($filename = readdir($aa))) {
            
            $filename = mb_convert_encoding($filename,"UTF-8", "EUC-JP");
            $fileName = explode("_", $filename);
            if($fileName[1] == $model->prgid && $fileName[2] == $gakkoname){
                if($fileName[0] != "M"){
                    $setFilename = $filename;
                    $setFiles = array();

                    $setFiles["DOWNLOAD_FILE_NAME"] = $fileName[3];
                    
                    //チェックボックス
                    $setFiles["CHKB"] = knjcreateCheckBox($objForm, "CHECK[]", $setFilename, $extra);
        
                    $arg["data"][] = $setFiles;
                }
            }
        }
        closedir($aa);

        //textbox
        $extra = "";
        $arg["ZIP_PASS"] = knjCreateTextBox($objForm, $model->zipPass, "ZIP_PASS", 50, 100, $extra);

        //プログラムの名前を取りにいく
        $query = knjxupdateQuery::getPrgname($model->menuid);
        $prgname = $db->getOne($query);
        $arg["PRGNAME"] = $prgname;
        
        Query::dbCheckIn($db);
        
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm);

        $arg["start"]   = $objForm->get_start("knjxupdateForm1", "POST", "knjxupdateindex.php", "", "knjxupdateForm1");
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjxupdateForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('del');\"";
    $arg["button"]["BTN_DELETE"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //ダウンロード
    $extra = "onclick=\"return btn_submit('csvGet');\"";
    $arg["button"]["btn_csvGet"] = knjCreateBtn($objForm, "btn_csvGet", "ダウンロード", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('del');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"openerClose(); closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJXUPDATE");
}

?>
