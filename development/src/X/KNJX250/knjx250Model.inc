<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjx250Model extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $zipPass;
    var $field;

    /**
     * メンバ変数の初期化
     */
    function knjx250Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->zipPass = VARS::request("ZIP_PASS");
        $this->file = VARS::file("FILE");
        $this->field = array("UPLOAD_DIV"   =>  VARS::post("UPLOAD_DIV"));
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $setReceiveName = STAFFCD == "0000999999" || STAFFCD == "00999999" ? "ALP" : "";
        //1.アップロード
        if ($this->field["UPLOAD_DIV"] === '1') {
            $filename = DOCUMENTROOT ."/receive_send{$setReceiveName}/" . $setFileName[0]."_".date("Y_m_j_H_i_s").'.'.$setFileName[get_count($setFileName) - 1];
        //2.マニュアル
        } else if ($this->field["UPLOAD_DIV"] === '2') {
            $filename = DOCUMENTROOT ."/sousaManual/" . $setFileName[0].'.'.$setFileName[get_count($setFileName) - 1];
        //3.写真
        } else {
            $filename = DOCUMENTROOT ."/image/" . $setFileName[0].'.'.$setFileName[get_count($setFileName) - 1];
        }
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
        $setFileName = mb_convert_encoding($this->zipPass, "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/receive_send/" . $setFileName;

        system("rm -f {$filename}");

        $setFileName = mb_convert_encoding($this->zipPass, "SJIS-win", "UTF-8");
        $filename = DOCUMENTROOT ."/receive_sendALP/" . $setFileName;

        system("rm -f '{$filename}'");
        unset($this->zipPass);
        return true;
    }

    function getDownloadModel() {
        $path_file = $this->zipPass;
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->zipPass);

        $setReceiveName = $this->receive_sendAlp[$path_file] == "1" ? "ALP" : "";
        $path_file = DOCUMENTROOT ."/receive_send{$setReceiveName}/".$path_file;

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
        //引数でUTF8変換後のファイル名を利用するとIEで文字化けするため、変換前の変数を利用する。
        common::setDownloadHeader($this->zipPass);
        header("Content-Length: ".$content_length);

        /* ファイルを読んで出力 */
        if (!readfile($path_file)) {
            die("Cannot read the file(".$path_file.")");
        }
        exit;
        return true;
    }
}
?>
