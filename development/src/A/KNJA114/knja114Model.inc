<?php

require_once('for_php7.php');

class knja114Model extends Model {
    var $field      = array();
    var $cmd;
    var $systemId = "";        //primary
    var $schregNo = "";        //primary
    var $file;

    function knja114Model() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->systemId);
            unset($this->schregNo);
            unset($this->outPut);
        }

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        //左画面
        if (VARS::request("LEFT_GRADE_HR_CLASS") != "") {
            $this->gradeHrClass = VARS::request("LEFT_GRADE_HR_CLASS");
        }

        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組番
                             "NAME"             =>  VARS::post("NAME"),             //氏名
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),        //氏名かな
                             "LOGINID"          =>  VARS::post("LOGINID"),          //ユーザーＩＤ
                             "PASSWORD"         =>  VARS::post("PASSWORD"),         //パスワード
                             "HID_PASSWORD"     =>  VARS::post("HID_PASSWORD"),     //パスワード(hidden)
                             "UME_PASSWORD"     =>  VARS::post("UME_PASSWORD"),     //パスワード(*埋め)
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダの有無
                             "OUTPUT"           =>  VARS::post("OUTPUT")            //処理の種類(1:ヘッダ, 2:取込, 3:エラー出力, 4:書出)
                             );

        $temp = VARS::request("SYSTEMID");
        if (isset($temp)) {
            $this->systemId = VARS::request("SYSTEMID");
        }
        $temp = VARS::get("SCHREGNO");
        if (isset($temp)) {
            $this->schregNo = VARS::get("SCHREGNO");
        }
        $temp = VARS::get("chFlg");
        if (isset($temp)) {
            unset($this->systemId);
            unset($this->schregNo);
        }

        $this->file = VARS::file("FILE");

    }

    //CVSファイル
    //サーバ ⇒ クライアント
    function getDownloadCsvModel() {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["YEAR"]                     = "年度";
        $header["GRADE"]                    = "学年";
        $header["HR_CLASS"]                 = "クラス";
        $header["ATTENDNO"]                 = "出席番号";
        $header["SCHREGNO"]                 = "※学籍番号";
        $header["NAME"]                     = "氏名";
        $header["SYSTEMID"]                 = "※システムＩＤ";
        $header["LOGINID"]                  = "※ユーザーＩＤ";
        $header["PASSWORD"]                 = "※パスワード";

        $header_rei = array();
        $header_rei["YEAR"]                 = CTRL_YEAR;
        $header_rei["GRADE"]                = "99";
        $header_rei["HR_CLASS"]             = "999";
        $header_rei["ATTENDNO"]             = "999";
        $header_rei["SCHREGNO"]             = "12345678";
        $header_rei["NAME"]                 = "アルプ太郎";
        $header_rei["SYSTEMID"]             = "99999999";
        $header_rei["LOGINID"]              = "26桁まで";
        $header_rei["PASSWORD"]             = "32桁まで";

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_OTHER_SYSTEM_USER_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") {
            //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knja114Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="3"){        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "外部システム管理データ";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

    //CVSファイル
    //クライアント ⇒ サーバ
    function getUploadCsvModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $cnt = knja114Query::insertQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata = array("SYSTEMID"                   => array(8,  "システムＩＤ"),
                               "SCHREGNO"                   => array(8,  "学籍番号"),
                               "LOGINID"                    => array(26, "ユーザーＩＤ"),
                               "PASSWORD"                   => array(32, "パスワード"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_OTHER_SYSTEM_USER_DAT');

            while (!feof ($fp))
            {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '')
                {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("SYSTEMID"            => trim($tmp[6]),
                                  "SCHREGNO"            => trim($tmp[4]),
                                  "LOGINID"             => trim($tmp[7]),
                                  "PASSWORD"            => trim($tmp[8]));

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }

                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    function errCheck($db, &$data, $data_each, $line, $duplicateCsv1, $primaryKey1) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $masterErrFlg    = false;    //マスターエラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $masterErrMsg    = "";       //マスターエラーメッセージ
        $duplicateCsvErrMsg = "";    //重複(CSV)メッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $masterSep    = "";
        $duplicateCsvSep= "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("SYSTEMID", "SCHREGNO", "LOGINID", "PASSWORD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("SCHREGNO", "SYSTEMID"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            } else if (strlen($data[$key]) < $val[0] && in_array($key,array("SCHREGNO", "SYSTEMID"))) {
                // 0埋め
                $data[$key] = sprintf("%0".$val[0]."d", $data[$key]);
            }

            //マスタチェック
            if (in_array($key,array("SCHREGNO"))) {
                if ($db->getOne(knja114Query::schregnoCount($data[$key])) == 0) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                }
            }
            if (in_array($key,array("SYSTEMID"))) {
                if ($db->getOne(knja114Query::systemidCount($data[$key])) == 0) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                }
            }

        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja114Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (strlen($this->field["LOGINID"]) == 0 && $this->cmd == "update") {
            $this->setWarning("MSG301","(ユーザーＩＤ)");
            return false;
        }
        if (strlen($this->field["PASSWORD"]) == 0 && $this->cmd == "update") {
            $this->setWarning("MSG301","(パスワード)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knja114Query::getDeleteInsertQuery($this, $this->field);

        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput()) {
            knja114Query::getDeleteQuery($this);
            $this->systemId = "";
            $this->schregNo = "";
            $this->setMessage("MSG201");

            $this->field = array();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
