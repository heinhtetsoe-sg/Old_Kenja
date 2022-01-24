<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx190oModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
	var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolName;
    var $schoolCode;

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx190oModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd          = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //最初にこのプログラムが呼ばれたとき
        if ($this->cmd == "") {
            $this->program_id = VARS::request("program_id") ? VARS::request("program_id") : PROGRAMID;
            $this->mode       = VARS::request("mode")       ? VARS::request("mode")       : 'ungrd';
        }

        //学校名取得
        $query = knjx190oquery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getProperties();
    }

    function getProperties() {
        $useSyojikou3Flg = false;
        $tyousasyoFlg = false;
        $retVal = "";
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        if ($fp) {
            while ($line = fgets($fp,1024)) {
                $pos = strpos($line, "useSyojikou3");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    if (!$useSyojikou3Flg) $this->useSyojikou3 = 0; //デフォルト値
                } else {
                    $retVal = str_replace("useSyojikou3 = ", "", $line);
                    $retVal = str_replace("\r\n", "", $retVal);
                    $retVal = str_replace("\r", "", $retVal);
                    $retVal = str_replace("\n", "", $retVal);
                    $this->useSyojikou3 = $retVal;
                    $useSyojikou3Flg = true;
                }

                $pos = strpos($line, "tyousasyoTokuBetuFieldSize");
                // === を使用していることに注目しましょう。単純に == を使ったのでは
                // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
                if ($pos === false) {
                    if (!$tyousasyoFlg) $this->tyousasyoTokuBetuFieldSize = 0; //デフォルト値
                } else {
                    $retVal = str_replace("tyousasyoTokuBetuFieldSize = ", "", $line);
                    $retVal = str_replace("\r\n", "", $retVal);
                    $retVal = str_replace("\r", "", $retVal);
                    $retVal = str_replace("\n", "", $retVal);
                    $this->tyousasyoTokuBetuFieldSize = $retVal;
                    $tyousasyoFlg = true;
                }
            }
            fclose($fp);
        } else {
            $this->useSyojikou3 = "0";
            $this->tyousasyoTokuBetuFieldSize = "0";
        }

        return $retVal;
    }

    function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx190oQuery::insertQueryCsv($this,$this->data_arr);
            } else {
                $cnt = knjx190oQuery::DeleteQueryCsv($this,$this->data_arr);

            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->useSyojikou3 == "1") {
                if ($this->tyousasyoTokuBetuFieldSize == 1) {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(450,"特別活動記録"),
                                       "TRAIN_REF1"               => array(210,"学習／行動の特技・特徴等"),
                                       "TRAIN_REF2"               => array(210,"部活動・資格取得等"),
                                       "TRAIN_REF3"               => array(210,"その他"));
                } else {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(264,"特別活動記録"),
                                       "TRAIN_REF1"               => array(210,"学習／行動の特技・特徴等"),
                                       "TRAIN_REF2"               => array(210,"部活動・資格取得等"),
                                       "TRAIN_REF3"               => array(210,"その他"));
                }
            } else {
                if ($this->tyousasyoTokuBetuFieldSize == 1) {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(450,"特別活動記録"),
                                       "TRAIN_REF"                => array(1218,"指導上参考"));
                } else {
                    $checkdata = array("YEAR"                     => array(4,"年度"),
                                       "SCHREGNO"                 => array(8,"学籍番号"),
                                       "ANNUAL"                   => array(2,"年次"),
                                       "ATTENDREC_REMARK"         => array(90,"出欠記録備考"),
                                       "SPECIALACTREC"            => array(264,"特別活動記録"),
                                       "TRAIN_REF"                => array(615,"指導上参考"));
                }
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx190oQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
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
                    $buffer      = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp         = common::csv2array($buffer);
                    $year        = (strlen(trim($tmp[4]))) ? sprintf("%04d",$tmp[4]) : "";
                    $schregno    = (strlen(trim($tmp[5]))) ? sprintf("%08d",$tmp[5]) : "";
                    $annualCheck = $db->getOne(knjx190oQuery::getAnnual($schregno,$year));
                    $annual      = ($annualCheck) ? $annualCheck : "00";

                    if ($this->useSyojikou3 == "1") {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "ATTENDREC_REMARK" => $tmp[7],
                                      "SPECIALACTREC"    => $tmp[8],
                                      "TRAIN_REF1"       => $tmp[9],
                                      "TRAIN_REF2"       => $tmp[10],
                                      "TRAIN_REF3"       => $tmp[11]);
                    } else {
                        $data = array("YEAR"             => $year,
                                      "SCHREGNO"         => $schregno,
                                      "ANNUAL"           => $annual,
                                      "ATTENDREC_REMARK" => $tmp[7],
                                      "SPECIALACTREC"    => $tmp[8],
                                      "TRAIN_REF"        => $tmp[9]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existErrFlg     = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existErrMsg     = "";

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $existSep     = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("YEAR","SCHREGNO","ANNUAL"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("YEAR","SCHREGNO","ANNUAL"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //存在チェック
            if (in_array($key,array("SCHREGNO"))) {
                $query = knjx190oQuery::countSchregNo($data[$key]);
                $cnt = $db->getOne($query);
                if ($cnt < 1) {
                    $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx190oQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }


    //ＣＳＶ出力
    function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        if ($this->useSyojikou3 == "1") {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "YEAR"                      => "※年度",
                            "SCHREGNO"                  => "※学籍番号",
                            "ANNUAL"                    => "※年次",
                            "ATTENDREC_REMARK"          => "出欠記録備考",
                            "SPECIALACTREC"             => "特別活動記録",
                            "TRAIN_REF1"                => "学習／行動の特技・特徴等",
                            "TRAIN_REF2"                => "部活動・資格取得等",
                            "TRAIN_REF3"                => "その他",
                            "DUMMY"                     => "DUMMY");
        } else {
            $header = array("GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "NAME"                      => "氏名",
                            "YEAR"                      => "※年度",
                            "SCHREGNO"                  => "※学籍番号",
                            "ANNUAL"                    => "※年次",
                            "ATTENDREC_REMARK"          => "出欠記録備考",
                            "SPECIALACTREC"             => "特別活動記録",
                            "TRAIN_REF"                 => "指導上参考",
                            "DUMMY"                     => "DUMMY");
        }

        if ($this->useSyojikou3 == "1") {
            if ($this->tyousasyoTokuBetuFieldSize == 1) {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角150文字",
                                    "TRAIN_REF1"                => "全角70文字",
                                    "TRAIN_REF2"                => "全角70文字",
                                    "TRAIN_REF3"                => "全角70文字",
                                    "DUMMY"                     => "DUMMY");
            } else {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角88文字",
                                    "TRAIN_REF1"                => "全角70文字",
                                    "TRAIN_REF2"                => "全角70文字",
                                    "TRAIN_REF3"                => "全角70文字",
                                    "DUMMY"                     => "DUMMY");
            }
        } else {
            if ($this->tyousasyoTokuBetuFieldSize == 1) {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角150文字",
                                    "TRAIN_REF"                 => "全角406文字",
                                    "DUMMY"                     => "DUMMY");
            } else {
                $header_rei = array("GRADE"                     => "99",
                                    "HR_CLASS"                  => "999",
                                    "ATTENDNO"                  => "999",
                                    "NAME"                      => "アルプ太郎",
                                    "YEAR"                      => "9999",
                                    "SCHREGNO"                  => "12345678",
                                    "ANNUAL"                    => "99",
                                    "ATTENDREC_REMARK"          => "全角30文字",
                                    "SPECIALACTREC"             => "全角88文字",
                                    "TRAIN_REF"                 => "全角205文字",
                                    "DUMMY"                     => "DUMMY");
            }
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            //エラー
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
                $query = knjx190oQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx190oQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
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
                $couse = "調査書進学用所見データ";
            }
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

}
//var_dump($_REQUEST);
?>
