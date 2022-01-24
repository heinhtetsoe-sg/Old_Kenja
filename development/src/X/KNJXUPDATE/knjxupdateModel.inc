<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjxupdateModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $zipPass;

    /**
     * メンバ変数の初期化
     */
    function knjxupdateModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->zipPass = VARS::request("ZIP_PASS");
        $this->file = VARS::file("FILE");
        $this->prgid       = VARS::request("PROGRAMID") != "" ? VARS::request("PROGRAMID") : $this->prgid;
        $this->menuid       = VARS::request("MENUID") != "" ? VARS::request("MENUID") : $this->menuid;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $first = "U";
        $setFileName[0] = mb_convert_kana($setFileName[0],"s","UTF-8");
        $setFileName[0] = str_replace(" ", "-", $setFileName[0]);
        $setFileName[0] = str_replace("(", "（", $setFileName[0]);
        $setFileName[0] = str_replace(")", "）", $setFileName[0]);
        
        if(strstr($setFileName[0], "_")){
            $this->warning = "ファイル名に\'_\'を含まないでください。";
            return false;
        }
        
        //アップロードできるファイルを制限する
        if(mb_strtolower($setFileName[get_count($setFileName) - 1]) != "jpg"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "jpeg"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "bmp"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "png"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "tif"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "tiff"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "pdf"
           && mb_strtolower($setFileName[get_count($setFileName) - 1]) != "txt"){
        
            $this->warning = "拡張子がpdf・txt,または画像ファイルのみが保存可能です。";
            return false;
        }
        
        //学校名を取得する
        //DB接続
        $db = Query::dbCheckOut();
        
        $gakQuery = knjxupdateQuery::getGakuName();
        $gakuname = $db->getOne($gakQuery);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $setFileName[0] = mb_convert_encoding($setFileName[0], "EUC-JP", "UTF-8");
        $filename = DOCUMENTROOT."/sousaManual/" .$first."_".$this->prgid."_".$gakuname."_".$setFileName[0].'.'.$setFileName[get_count($setFileName) - 1];

        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
    }

    /**
     * 削除
    */
    function &getDeleteModel()
    {
        
        $this->zipPass = VARS::post("CHECK");
        if (is_array($this->zipPass)) {
            foreach ($this->zipPass as $key => $val) {
                $setFileName = mb_convert_encoding($val, "EUC-JP", "UTF-8");
                $filename = DOCUMENTROOT."/sousaManual/". $setFileName;

                system("rm -f {$filename}");
            }
        }
        unset($this->zipPass);
        return true;
    }

    function getDownloadModel() {
        $path_file = $this->zipPass;
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->zipPass);

        $path_file = DOCUMENTROOT."/sousaManual/".$path_file;

        /* ファイルの存在確認 */
        if (!file_exists($path_file)) {
            die("Error: File(".$path_file.") does not exist");
        }

        /* オープンできるか確認 */
        if (!($fp = fopen($path_file, "r"))) {
            die("Error: Cannot open the file(".$path_file.")");
        }
        fclose($fp);

        /* ファイルサイズの確認 */
        if (($content_length = filesize($path_file)) == 0) {
            die("Error: File size is 0.(".$path_file.")");
        }

        ini_set('mbstring.http_output', 'pass');
        /* ダウンロード用のHTTPヘッダ送信 */
        $ext = $kakuTyousi[get_count($kakuTyousi) - 1];
        if ($ext == 'jpg') {
            header("Content-type: image/jpeg");
        } else if ($ext == 'pdf') {
            header('Content-Type: application/pdf');
        } else if ($ext == 'xls' || $ext == 'xlsx') {
            header('Content-Type: application/vnd.ms-excel');
        } else if ($ext == 'doc') {
            header('Content-Type: application/msword');
        } else if ($ext == 'ppt') {
            header('Content-Type: application/vnd.ms-powerpoint');
        } else if ($ext == 'csv') {
            header('Content-Type: text/csv');
        } else if ($ext == 'txt') {
            header('Content-Type: text/plain');
        } else {
            header('Content-Type: application/octet-stream');
        }
        header("Content-Disposition: attachment; filename=\"".basename($path_file)."\"");
        header("Content-Length: ".$content_length);

        /* ファイルを読んで出力 */
        if (!readfile($path_file)) {
            die("Cannot read the file(".$path_file.")");
        }

        return true;
    }
}
?>
