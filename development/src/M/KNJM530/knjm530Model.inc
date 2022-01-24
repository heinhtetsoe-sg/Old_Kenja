<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm530Model extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $errReportFlg;
    var $errAttendFlg;
    var $cookie_name;
    /**
     * メンバ変数の初期化
     */
    function knjm530Model()
    {
    }
    function init()
    {
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "") {
            unset($this->cookie_name);
        }
        $this->cookie_name = VARS::post("COOKIE_NAME") ? VARS::post("COOKIE_NAME") : VARS::cookie("KNJK007_USER_NAME");
        $this->dir = "/home/kanri/HandyTerminal_Data";
        $this->moveDir = $this->dir."/wrk/";
    }

    function errDataUmu() {
        $aa = opendir($this->dir);
        $this->errAttendFlg = false;
        $this->errReportFlg = false;
        while (false !== ($filename = readdir($aa))) {
            if (!is_dir($this->dir."/".$filename)) {
                if ($filename == "errDataAttend.csv") {
                    $this->errAttendFlg = true;
                } else if ($filename == "errDataReport.csv") {
                    $this->errReportFlg = true;
                }
            }
        }
        closedir($aa);
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    function getUpdateCookie() {
        setcookie('KNJK007_USER_NAME', $this->cookie_name, time() + 60 * 60 * 24 * 365);
        $this->setMessage("MSG201");
        return;
    }

    function getUpdateModel()
    {
        $tanmatuName = "";
        if (!VARS::cookie("KNJK007_USER_NAME")) {
            $this->setWarning("端末名を登録して下さい。");
            return false;
        } else {
            $tanmatuName = VARS::cookie("KNJK007_USER_NAME");
        }
        $aa = opendir($this->dir);
        while (false !== ($filename = readdir($aa))) {
            if (is_dir($this->dir."/".$filename)) {
                if ($filename == $tanmatuName) {
                    $originalDir = $this->dir."/".$filename;
                    //ディレクトリごと、移動する。
                    //ディレクトリごと、移動する。
                    $zipName = $filename.date("YmdHis").".zip";
                    system('cd "' . $this->dir . '";zip -rq "' . $zipName . '" "' . $filename . '";mv "' . $zipName . '" backUpFile/');
                    system('cd "' . $this->dir . '";rm -f errData* ');
                    $moveFile = $originalDir."/";
                    system('mv "' . $moveFile . '"*.txt "' . $this->moveDir . '"');
                }
            }

        }
        closedir($aa);

        $errDataReport = array();
        $errDataAttend = array();
        //移動後のファイルパス単位
        $dataDir = opendir($this->moveDir);
        //終了後削除ファイル
        $delData = array();
        while (false !== ($filename = readdir($dataDir))) {

            $filedir = $this->moveDir."/".$filename;
            $info = pathinfo($filedir);
            //拡張子がtxt
            if ($info["extension"] == "txt") {

                $file = fopen($filedir, "r");
                while (!feof($file)) {
                    $buffer = fgets($file, 4096);
                    if (trim($buffer) != '') {
                        //文字コードを判別、改行コードを削除
                        $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                        $tmp    = common::csv2array($buffer);
                        if ($filename == "Report.txt") {
                            $dataArray = $this->getDataArrayReport($tmp);
                            list($check, $riyu) = $this->validateInputReport($dataArray);
                            if ($check) {
                                $this->insertReport($dataArray);
                            } else {
                                $errDataReport[] = $buffer.",".$riyu;
                            }
                        } else if ($filename == "Attend.txt") {
                            $dataArray = $this->getDataArrayAttend($tmp);
                            list($check, $riyu) = $this->validateInputAttend($dataArray);
                            if ($check) {
                                $this->insertAttend($dataArray);
                            } else {
                                $errDataAttend[] = $buffer.",".$riyu;
                            }
                        }
                    }
                }
                $delData[] = $filedir;
            }

        }
        closedir($dataDir);
        if (is_array($delData)) {
            foreach ($delData as $key => $val) {
                system('rm -rf "' . $val . '" ');
            }
        }

        //レポートエラー
        if (get_count($errDataReport) > 0) {
            $this->makeErrorData("errDataReport", $errDataReport);
        }
        //出欠エラー
        if (get_count($errDataAttend) > 0) {
            $this->makeErrorData("errDataAttend", $errDataAttend);
        }

        $this->setMessage("MSG201");
        return;

    }

    function getDataArrayReport($tmp) {
        $retArray = array();
        $retArray["RECEIPT_DATE"] = substr($tmp[0], 0, 4)."/".substr($tmp[0], 4, 2)."/".substr($tmp[0], 6);
        $retArray["STAFFCD"] = $tmp[1];
        $retArray["REPNO"] = $tmp[2];
        $retArray["SCHREGNO"] = $tmp[3];
        $retArray["RECEIPT_TIME"] = substr($tmp[4], 0, 2).":".substr($tmp[4], 2, 2).":".substr($tmp[4], 4);

        $db = Query::dbCheckOut();
        $query = knjm530Query::getCheckout2($retArray);
        $retArray["CHAIRCD"] = $db->getOne($query);

        Query::dbCheckIn($db);

        return $retArray;
    }

    //入力項目チェック
    function validateInputReport($data)
    {
        $retData = array(true, "");
        //DB接続
        $db = Query::dbCheckOut();

        $this->semester = 0;                //データ更新時に使用
        for ($i = 1; $i <= $this->control["学期数"]; $i++) {
            if ($this->control["学期開始日付"][$i] <= $data["RECEIPT_DATE"] &&
                $this->control["学期終了日付"][$i] >= $data["RECEIPT_DATE"]
            ) {
                $this->semester = $i;
            }
        }

        //年度範囲外
        if ($this->semester == 0) {
            $retData[0] = false;
            $retData[1] .= "○年度範囲外";
        }
        //処理日付がオーバー
        if ($data["RECEIPT_DATE"] > str_replace("-", "/", CTRL_DATE)) {
            $retData[0] = false;
            $retData[1] .= "○処理日付オーバー";
        }
        //評価送信日を入力して下さい。
        if (!$data["RECEIPT_DATE"]) {
            $retData[0] = false;
            $retData[1] .= "○評価送信日無し";
        }
        //レポートＮｏを入力して下さい。
        if (!$data["REPNO"]) {
            $retData[0] = false;
            $retData[1] .= "○レポートＮｏ無し";
        }
        //レポートＮｏは10桁です。
        if (strlen($data["REPNO"]) != 10) {
            $retData[0] = false;
            $retData[1] .= "○レポートＮｏ10桁";
        }
        //年度と一致しません。レポート番号の1桁目が不正です。
        if (substr(CTRL_YEAR, 3, 1) != substr($data["REPNO"], 0, 1)) {
            $retData[0] = false;
            $retData[1] .= "○レポート番号の1桁目が、年度と不一致。";
        }
        //学籍番号を入力して下さい。
        if (!$data["SCHREGNO"]) {
            $retData[0] = false;
            $retData[1] .= "○学籍番号無し";
        }

        //チェック用データ取得(レポート提出基準日)
        $query = knjm530Query::getCheckout2($data);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //レポート提出基準日未登録のデータです。
        if (!$row) {
            $retData[0] = false;
            $retData[1] .= "○レポート提出基準日未登録";
        }

        //チェック用データ取得(レポート提出基準日)
        $query = knjm530Query::getCheckout3($this, $data);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //講座受講生徒データ未登録のデータです。
        if (!$row) {
            $retData[0] = false;
            $retData[1] .= "○講座受講生徒データ未登録";
        }
        Query::dbCheckIn($db);

        return $retData;
    }

    function &insertReport($data)
    {
        knjm530Query::getDeleteReportIns($data);
        knjm530Query::getInsertReportQuery($data);
    }

    function getDataArrayAttend($tmp) {
        $retArray = array();
        $retArray["RECEIPT_DATE"] = substr($tmp[0], 0, 4)."/".substr($tmp[0], 4, 2)."/".substr($tmp[0], 6);
        $retArray["PERIODCD"] = $tmp[1];
        $retArray["HANDY_CHAIRCD"] = $tmp[2];
        $retArray["STAFFCD"] = $tmp[3];
        $retArray["SCHREGNO"] = $tmp[4];
        $retArray["RECEIPT_TIME"] = substr($tmp[5], 0, 2).":".substr($tmp[5], 2, 2).":".substr($tmp[5], 4);
        $retArray["SCHOOLINGKINDCD"] = $retArray["HANDY_CHAIRCD"] < 70 ? "1" : "";

        $db = Query::dbCheckOut();

        $this->semester = 0;                //データ更新時に使用
        for ($i = 1; $i <= $this->control["学期数"]; $i++) {
            if ($this->control["学期開始日付"][$i] <= $retArray["RECEIPT_DATE"] &&
                $this->control["学期終了日付"][$i] >= $retArray["RECEIPT_DATE"]
            ) {
                $this->semester = $i;
            }
        }
        $query = knjm530Query::getHandyCaircd($this, $retArray);
        $retArray["CHAIRCD"] = $db->getOne($query);

        if ($retArray["SCHOOLINGKINDCD"] == "1") {
            $query = knjm530Query::checkSchChrT($retArray);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $retArray["SCHOOLING_SEQ"] = $row["SCHOOLING_SEQ"];
            $retArray["SCHOOLING_CNT"] = $row["CNT"];
        } else {
            $retArray["SCHOOLING_SEQ"] = 0;
        }

        Query::dbCheckIn($db);

        return $retArray;
    }

    //入力項目チェック
    function validateInputAttend($data)
    {
        $retData = array(true, "");
        //DB接続
        $db = Query::dbCheckOut();

        $this->semester = 0;                //データ更新時に使用
        for ($i = 1; $i <= $this->control["学期数"]; $i++) {
            if ($this->control["学期開始日付"][$i] <= $data["RECEIPT_DATE"] &&
                $this->control["学期終了日付"][$i] >= $data["RECEIPT_DATE"]
            ) {
                $this->semester = $i;
            }
        }

        //年度範囲外
        if ($this->semester == 0) {
            $retData[0] = false;
            $retData[1] .= "○年度範囲外";
        }
        //処理日付がオーバー
        if ($data["RECEIPT_DATE"] > str_replace("-", "/", CTRL_DATE)) {
            $retData[0] = false;
            $retData[1] .= "○処理日付オーバー";
        }
        if (!$data["SCHREGNO"]) {
            $retData[0] = false;
            $retData[1] .= "○学籍番号無し";
        }
        //チェック用データ取得
        $query = knjm530Query::getCheckout($this, $data);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!$row){
            $retData[0] = false;
            $retData[1] .= "○講座登録無し";
        }

        if ($data["SCHOOLINGKINDCD"] == "") {
            //講座チェック用データ取得
            $query = knjm530Query::getCheckoutch($data);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $retData[0] = false;
                $retData[1] .= "○未登録講座";
            }

            //重複チェック用データ取得
            $query = knjm530Query::getCheckoutHr($this, $data);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row){
                $retData[0] = false;
                $retData[1] .= "○登録済み";
            }
        } else {
            if ($data["SCHOOLING_SEQ"] == "") {
                $retData[0] = false;
                $retData[1] .= "○回数設定無し";
            } else {
                $query = knjm530Query::getCheckIns($this, $data);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row){
                    $retData[0] = false;
                    $retData[1] .= "○登録済み";
                }
            }

            if ($data["SCHOOLING_CNT"] > 1){
                $retData[0] = false;
                $retData[1] .= "○回数重複";
            }
        }

        Query::dbCheckIn($db);

        return $retData;
    }

    function &insertAttend($data)
    {
        if ($data["SCHOOLINGKINDCD"] == "1") {
            knjm530Query::getInsertAttendQuery($data);
            knjm530Query::getUpdateAttendSchchr($data);
        } else {
            knjm530Query::getInsertAttendHrQuery($data);
            knjm530Query::getUpdateAttendSchchr($data);
        }
    }

    //エラーファイル作成
    function makeErrorData($filName, $errData)
    {
        $word = $this->getTitle($filName);
        foreach ($errData as $key => $val) {
            $word .= $val;
            $word .= "\r\n";
        }

        $word = mb_convert_encoding($word, "SJIS", "UTF-8");

        $pointer = fopen($this->dir."/".$filName.".csv", "w"); //ファイルを開きます
        flock($pointer, LOCK_EX);   //ファイルをロックします
        fputs($pointer, $word);     //書込みです
        flock($pointer, LOCK_UN);   //ロックを解除します
        fclose($pointer);           //ファイルを閉じます

    }

    function getTitle($filName) {
        if ($filName == "errDataReport") {
            return "受付日, 職員CD, レポート番号, 学籍番号, 受付時刻, ターミナルID, 理由\r\n";
        } else if ($filName == "errDataAttend") {
            return "受講日, 校時CD, 講座CD, 職員CD, 学籍番号, 出席時刻, ターミナルID, 氏名, 理由\r\n";
        }
    }

    function getDownLoadModel() {
        if ($this->cmd == "errA" || $this->cmd == "errR") {
            $this->errDownLoad();
        } else {
            $this->dataDownLoad();
        }
    }

    function errDownLoad() {
        if ($this->cmd == "errA") {
            $filename = "errDataAttend.csv";
        } else if ($this->cmd == "errR") {
            $filename = "errDataReport.csv";
        }

        $path_file = $this->dir."/".$filename;
        $file_size = filesize($path_file);
        ini_set('mbstring.http_output', 'pass');
        header("Accept-Ranges: none");
        header("Content-Type: application/octet-stream");
        common::setDownloadHeader($path_file);
        header("Content-Length: ".$file_size);
        readfile($path_file);

        return true;
    }

    function dataDownLoad() {
        //DB接続
        $db = Query::dbCheckOut();

        list($filename, $query) = $this->getHeadder();
        $dataFlg = false;
        $contents = "";

        //SQL文発行
        $result = $db->query($query);
        if ($this->cmd == "seito") {
            $befSchreg = "";
            $schCnt = 1;
            $w_row = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                if ($befSchreg != "" && $befSchreg != $row["SCHREGNO"]) {
                    $contents .= implode($w_row, ",") ."\r\n";
                    $w_row = array();
                    $schCnt = 1;
                }

                if ($befSchreg == $row["SCHREGNO"]) {
                    if ($schCnt >= 20) {
                        continue;
                    }
                    $w_row[] = $row["HANDY_CHAIRCD"];
                    $schCnt++;
                } else {
                    $w_row[] = $row["SCHREGNO"];
                    $w_row[] = $row["NAME"];
                    $w_row[] = $row["HANDY_CHAIRCD"];
                    $schCnt = 1;
                }

                $dataFlg = true;
                $befSchreg = $row["SCHREGNO"];
            }
            $result->free();
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row); //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                if ($this->cmd == "jikanwari") {
                    $row["EXECUTEDATE"] = str_replace("-", "", $row["EXECUTEDATE"]);
                }
                $contents .= implode($row, ",") ."\r\n";
                $dataFlg = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($dataFlg) {
            $this->makeDataDownLoad($contents, $filename);
            $this->warning = "データを保存しました。";

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //CSV作成
    function makeDataDownLoad($contents, $filename)
    {
        $word = $contents;
        $pointer = fopen($this->dir."/Hdt_Code/".$filename, "w"); //ファイルを開きます
        flock($pointer, LOCK_EX);   //ファイルをロックします
        fputs($pointer, $word);     //書込みです
        flock($pointer, LOCK_UN);   //ロックを解除します
        fclose($pointer);           //ファイルを閉じます

    }

    function getHeadder() {
        $retHeader = array();
        switch ($this->cmd) {
            case "seito":
                $retHeader[] = "Hdt_kojin.txt";
                $retHeader[] = knjm530Query::getSeitoCsv();
              break;
            case "staff":
                $retHeader[] = "Hdt_shokuin.txt";
                $retHeader[] = knjm530Query::getStaffCsv();
              break;
            case "kouji":
                $retHeader[] = "Hdt_koji.txt";
                $retHeader[] = knjm530Query::getKoujiCsv();
              break;
            case "kouza":
                $retHeader[] = "Hdt_koza.txt";
                $retHeader[] = knjm530Query::getKouzaCsv();
              break;
            case "jikanwari":
                $retHeader[] = "jikanwari.txt";
                $retHeader[] = knjm530Query::getJikanwariCsv();
              break;
            default:
              break;
        }

        return $retHeader;
    }
}
?>
