<?php

require_once('for_php7.php');

//定数定義
class knjh180Model extends Model {
    var $cmd;
    var $data;
    var $max;
     //メンバ変数の初期化
    function knjh180Model() {}

    function init() {
        $this->cmd = VARS::request("cmd");
        if (VARS::file("csvfile")){
            $this->field["CSVFILE"] = VARS::file("csvfile");        //ファイルのアップロード
            //文字コードをUtF-8にデータコンバート
            $this->field["CSVFILE"]["name"] = i18n_convert($this->field["CSVFILE"]["name"], "UtF-8", "EUC");
        }
    }

    function &getMainModel() {}

    //実行モデルを返します
    function getExecuteModel()
    {
        //ファイルの文字列の取得
        $this->dataArr = array();
        $this->errData = array();
        $this->dataArr = $this->GetFileString();
        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $erraDatas = "";
            $tables = array("1" => "STATION_NETMST");
            $header = array("STATION_CD"  => "駅コード");
            $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
            }
            if (sizeof($this->errData) > 0) {
                $erraDatas .= "行番号　メッセージ"."\\n";
            }
            foreach ($this->errData as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            if ($erraDatas != "") {
                $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
            } else {
                //現在チェックしたレコードがすでにテーブルに存在するのか？
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    $query = knjh180Query::checkExist($this->dataArr[$i]["STATION_CD"]);
                    $rowCount = $db->getOne($query);
                    if ($rowCount > 0) { //ＤＢにあれば、update
                        $query = knjh180Query::updateQueryCsv($this->dataArr[$i]);
                        $db->query($query);
                    } else { //ＤＢになければ、insert
                        $query = knjh180Query::insertQueryCsv($this->dataArr[$i]);
                        $db->query($query);
                    }
                }
                //最後に路線マスタを更新する
                $query = knjh180Query::deleteTrainLine();
                $db->query($query);

                $query = knjh180Query::insertTrainLine();
                $db->query($query);

                $db->commit(); // トランザクションをコミットする。
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    private function getFileString()
    {
        $tempArr = array();
        $max = $this->max;
        set_time_limit(0); //スクリプトの実行可能な時間を秒数でセットする関数(0をセットすると制限がなくなる)

        if (!is_uploaded_file($this->field["CSVFILE"]["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("RR_CD"           => array(2, "鉄道概要コード"),
                               "LINE_CD"         => array(5, "路線コード"),
                               "STATION_CD"      => array(5, "駅コード"),
                               "LINE_SORT"       => array(5, "路線並び順"),
                               "STATION_G_CD"    => array(5, "駅グループコード"),
                               "R_TYPE"          => array(5, "駅タイプ"),
                               "RR_NAME"         => array(96, "鉄道概要名"),
                               "LINE_NAME"       => array(192, "路線名"),
                               "STATION_NAME"    => array(192, "駅名"),
                               "STATION_SORT"    => array(5, "駅並び順"),
                               "PREF_CD"         => array(2, "都道府県コード"),
                               "LON"             => array(10, "経度"),
                               "LAT"             => array(10, "緯度"),
                               "F_FLAG"          => array(1, "表示フラグ")
                              );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            $fp = fopen($this->field["CSVFILE"]["tmp_name"], "r");     //fopen--読み取り専用でファイルを開く
            fgets($fp, 4096); //1行目はヘッダなので、飛ばす。
            $i = 0;
            $line = 1;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('STATION_NETMST');
            while (!feof($fp)) {                                       //feof--ファイルポインタがファイル終端に達しているか
                $buffer = fgets($fp, 4096);                             //fgets--ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array("RR_CD"        => trim($tmp[0]),
                                  "LINE_CD"      => trim($tmp[1]),
                                  "STATION_CD"   => trim($tmp[2]),
                                  "LINE_SORT"    => trim($tmp[3]),
                                  "STATION_SORT" => trim($tmp[4]),
                                  "STATION_G_CD" => trim($tmp[5]),
                                  "R_TYPE"       => trim($tmp[6]),
                                  "RR_NAME"      => trim($tmp[7]),
                                  "LINE_NAME"    => trim($tmp[8]),
                                  "STATION_NAME" => trim($tmp[9]),
                                  "PREF_CD"      => trim($tmp[10]),
                                  "LON"          => trim($tmp[11]),
                                  "LAT"          => trim($tmp[12]),
                                  "F_FLAG"       => trim($tmp[13]));
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $tempArr;
    }

     //エラーチェック
    private function errCheck($db, $data, $dataEach, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($dataEach as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("RR_CD", "LINE_CD", "STATION_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("RR_CD", "LINE_CD", "STATION_CD", "LINE_SORT", "STATION_SORT", "STATION_G_CD", "R_TYPE", "LON", "LAT"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //変数をクリアします
    function clean() {}
}
?>