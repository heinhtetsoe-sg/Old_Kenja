<?php

require_once('for_php7.php');

class knjf151Model extends Model {
    var $field = array();
    var $cmd;
    var $schregno;
    var $visit_date;
    var $visit_hour;
    var $visit_minute;

    function knjf151Model() {}

    function init() {

        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
        }

        $this->field = array("VISIT_DATE"           =>  VARS::post("VISIT_DATE"),           //来室日付
                             "VISIT_HOUR"           =>  (strlen(VARS::post("VISIT_HOUR"))) ? sprintf("%02d", VARS::post("VISIT_HOUR")) : "",        //来室時間（時）
                             "VISIT_MINUTE"         =>  (strlen(VARS::post("VISIT_MINUTE"))) ? sprintf("%02d", VARS::post("VISIT_MINUTE")) : "",    //来室時間（分）
                             "RELATIONSHIP"         =>  VARS::post("RELATIONSHIP"),         //相談者
                             "CONSULTATION_METHOD"  =>  VARS::post("CONSULTATION_METHOD"),  //相談区分
                             "CONSULTATION_KIND1"   =>  VARS::post("CONSULTATION_KIND1"),   //相談内容（人間関係）
                             "CONSULTATION_KIND2"   =>  VARS::post("CONSULTATION_KIND2"),   //相談内容（学校生活）
                             "SPECIAL_NOTE"         =>  VARS::post("SPECIAL_NOTE"),         //特記事項
                             "REMARK"               =>  VARS::post("REMARK"),               //記録
                             );

        $this->cmd = VARS::request("cmd");

        $this->pdffile = VARS::file("PDF_FILE");

        if (VARS::post("PDF")) {
            $this->pdf = VARS::post("PDF");
        }

        if (VARS::get("SCHREGNO")) {
            $this->schregno   = VARS::get("SCHREGNO");
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
        }

        if (VARS::get("VISIT_DATE")) {
            $this->visit_date = VARS::get("VISIT_DATE");
        }
        if (VARS::get("VISIT_HOUR")) {
            $this->visit_hour = VARS::get("VISIT_HOUR");
        }
        if (VARS::get("VISIT_MINUTE")) {
            $this->visit_minute = VARS::get("VISIT_MINUTE");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //PDFアップロード
    function &getUploadModel() {
        if ($this->visit_date == "") {
            $this->setWarning("MSG304","登録済みのデータからしかアップロードできません。");
            return false;
        }

        //ファイル名取得
        $target = $_FILES['PDF_FILE']['name'];
        $fileInfo = pathinfo($target);
        $kakutyousi = $fileInfo['extension'];
        $kakutyousi = strtoupper($kakutyousi);

        //拡張子チェック
        if ($kakutyousi != "pdf" && $kakutyousi != "PDF") {
            $this->warning = "PDFファイルを指定して下さい。\\n(".$kakutyousi."ファイルの取込は出来ません。)";
            return false;
        }
        $pdf_name = $this->schregno.'_'.preg_replace("#-|/#", '', $this->visit_date).$this->visit_hour.$this->visit_minute;
        $filename = DOCUMENTROOT ."/" .'pdf_download/' . $pdf_name .'.pdf';
        if (!is_uploaded_file($this->pdffile['tmp_name'])) {
            $this->warning = "ファイル名又は\\nサイズ(500KB以上)等が不正です。";
        } else if ($this->visit_date == "") {
            $this->warning = "データを指定して下さい。";
        } else {
            if (move_uploaded_file($this->pdffile['tmp_name'], $filename) == FALSE) {
                $this->warning = "PDFファイルの取込失敗です。";
            } else {
                $this->message = "PDFファイルの取込正常です。";
            }
        }
    }

    //PDFダウンロード
    function getDownloadModel() {
        $path_file = $this->pdf.'.pdf';
        $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
        $kakuTyousi = explode(".", $this->pdf);
        $filename = $path_file;

        $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
        //ファイルの存在確認
        if (!file_exists($path_file)) {
            $this->setWarning("MSG303","指定のPDFデータは存在しません。");
            return false;
        }

        //オープンできるか確認
        if (!($fp = fopen($path_file, "r"))) {
            $this->setWarning("MSG203","指定のPDFデータは開くことができません。");
            return false;
        }
        fclose($fp);

        //ファイルサイズの確認
        if (($content_length = filesize($path_file)) == 0) {
            $this->setWarning("MSG203","指定のPDFデータのファイルサイズは 0 byteです。");
            return false;
        }

        ini_set('mbstring.http_output', 'pass');
        //ダウンロード用のHTTPヘッダ送信
        header('Content-Type: application/pdf');
        common::setDownloadHeader($filename);
        header("Content-Length: ".$content_length);

        //ファイルを読んで出力
        if (!readfile($path_file)) {
            $this->setWarning("MSG203","指定のPDFデータは開くことができません。");
            return false;
        }

        return true;
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
           $this->setWarning("MSG304","(左より生徒を選択してから行ってください)");
           return false;
        }

        if ($this->cmd == "add") {
            //必須チェック
            if($this->field["VISIT_DATE"] == "" || $this->field["VISIT_HOUR"] == "" || $this->field["VISIT_MINUTE"] == "") {
                $this->setWarning("MSG301","(来室日時)");
                return false;
            }

            //データ有無チェック
            $db = Query::dbCheckOut();
            $val = $this->field["VISIT_DATE"].":".$this->field["VISIT_HOUR"].":".$this->field["VISIT_MINUTE"];
            $exists = $db->getOne(knjf151Query::getList($this, "data", $val));
            Query::dbCheckIn($db);
            if (strlen($exists) > 0) {
                $this->setWarning("MSG918");
                return false;
            }
        }

        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->visit_date == "" || $this->visit_hour == "" || $this->visit_minute == "") {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //サイズチェック
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->validate_row_cnt($this->field["SPECIAL_NOTE"], (50 * 2)) > 1) {
                $this->setWarning("MSG914","(特記事項は全角50文字までです)");
                return false;
            }
            if ($this->validate_row_cnt($this->field["REMARK"], (25 * 2)) > 4) {
                $this->setWarning("MSG914","(指導内容は4行までです)");
                return false;
            }
        }

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjf151Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

    //追加
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjf151Query::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->visit_date   = $this->field["VISIT_DATE"];
            $this->visit_hour   = $this->field["VISIT_HOUR"];
            $this->visit_minute = $this->field["VISIT_MINUTE"];
        }
    }

    //削除
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            //削除
            knjf151Query::getDeleteQuery($this);
            $this->setMessage("MSG201");

            //PDFファイル削除
            $pdf_file = false;
            $pdf_name = $this->schregno.'_'.preg_replace("#-|/#", '', $this->visit_date).$this->visit_hour.$this->visit_minute;
            $path_file = $pdf_name.'.pdf';
            $path_file = mb_convert_encoding($path_file, "SJIS-win", "UTF-8");
            $path_file = DOCUMENTROOT ."/pdf_download/".$path_file;
            if (file_exists($path_file) && ($fp = fopen($path_file, "r")) && ($content_length = filesize($path_file)) > 0) {
                $pdf_file = true;
            }
            @fclose($fp);
            if ($pdf_file == true) unlink($path_file);

            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
            $this->field = array();
        }
    }
}
?>
