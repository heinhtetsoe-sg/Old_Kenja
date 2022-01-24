<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjx251Model extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $zipPass;
    var $field;

    /**
     * メンバ変数の初期化
     */
    function knjx251Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->zipPass = VARS::request("ZIP_PASS");
        $this->file = VARS::file("FILE");
        $this->field = array("UPLOAD_DIV"   =>  VARS::post("UPLOAD_DIV"),
                             "DOWNLOAD_DIV" =>  VARS::post("DOWNLOAD_DIV")
                             );
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");

        if ($setFileName[get_count($setFileName) - 1] != "xml") {
            $this->warning = "拡張子が不正です。";
            return false;
        }

        $filename = str_replace("src", "pdf_design/xml/", DOCUMENTROOT).$this->file['name'];
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
        $path_file = $this->zipPass;
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $path_file = str_replace("src", "pdf_design/xml/", DOCUMENTROOT).$path_file;

        system("rm -f {$path_file}");
        unset($this->zipPass);
        return true;
    }

    function getDownloadModel() {
        $path_file = $this->zipPass;
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->zipPass);

        if ($this->field["DOWNLOAD_DIV"] == "1") {
            $path_file = str_replace("src", "pdf_design/template_xml/", DOCUMENTROOT).$path_file;
        } else {
            $path_file = str_replace("src", "pdf_design/xml/", DOCUMENTROOT).$path_file;
        }

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
        header('Content-Type: application/xml');
        common::setDownloadHeader($path_file);
        header("Content-Length: ".$content_length);

        /* ファイルを読んで出力 */
        if (!readfile($path_file)) {
            die("Cannot read the file(".$path_file.")");
        }

        return true;
    }
}
?>
