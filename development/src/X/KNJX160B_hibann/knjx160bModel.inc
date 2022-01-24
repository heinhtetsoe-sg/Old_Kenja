<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjx160bModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
	var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjx160bModel()
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
                $cnt = knjx160bQuery::insertQueryCsv($this,$this->data_arr);
            } else {
                $cnt = knjx160bQuery::DeleteQueryCsv($this,$this->data_arr);

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

            $checkdata = array("SCHREGNO"                  => array(8,"学籍番号"),
                               "RELATIONSHIP"              => array(2,"続柄"),
                               "GUARD_NAME"                => array(60,"保護者氏名"),
                               "GUARD_KANA"                => array(120,"保護者氏名かな"),
                               "GUARD_SEX"                 => array(1,"性別"),
                               "GUARD_BIRTHDAY"            => array(10,"生年月日"),
                               "GUARD_ZIPCD"               => array(8,"郵便番号"),
                               "GUARD_ADDR1"               => array(75,"住所1"),
                               "GUARD_ADDR2"               => array(75,"住所2"),
                               "GUARD_TELNO"               => array(14,"電話番号"),
                               "GUARD_FAXNO"               => array(14,"FAX番号"),
                               "GUARD_E_MAIL"              => array(20,"E-MAIL"),
                               "GUARD_JOBCD"               => array(2,"職種コード"),
                               "GUARD_WORK_NAME"           => array(60,"勤務先名称"),
                               "GUARD_WORK_TELNO"          => array(14,"勤務先電話番号"),
                               "GUARANTOR_RELATIONSHIP"    => array(2,"保証人続柄"),
                               "GUARANTOR_NAME"            => array(60,"保証人氏名"),
                               "GUARANTOR_KANA"            => array(120,"保証人氏名かな"),
                               "GUARANTOR_SEX"             => array(1,"保証人性別"),
                               "GUARANTOR_ZIPCD"           => array(8,"保証人郵便番号"),
                               "GUARANTOR_ADDR1"           => array(75,"保証人住所1"),
                               "GUARANTOR_ADDR2"           => array(75,"保証人住所2"),
                               "GUARANTOR_TELNO"           => array(14,"保証人電話番号"),
                               "GUARANTOR_JOBCD"           => array(2,"保証人職種コード"),
                               "PUBLIC_OFFICE"             => array(30,"兼ねている公職"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx160bQuery::deleteQueryErr();
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
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("SCHREGNO"                  => (strlen(trim($tmp[4]))) ? sprintf("%08d",$tmp[4]) : "",
                                  "RELATIONSHIP"              => (strlen(trim($tmp[5]))) ? sprintf("%02d",$tmp[5]) : "",
                                  "GUARD_NAME"                => $tmp[6],
                                  "GUARD_KANA"                => $tmp[7],
                                  "GUARD_SEX"                 => $tmp[8],
                                  "GUARD_BIRTHDAY"            => $tmp[9],
                                  "GUARD_ZIPCD"               => $tmp[10],
                                  "GUARD_ADDR1"               => $tmp[11],
                                  "GUARD_ADDR2"               => $tmp[12],
                                  "GUARD_TELNO"               => $tmp[13],
                                  "GUARD_FAXNO"               => $tmp[14],
                                  "GUARD_E_MAIL"              => $tmp[15],
                                  "GUARD_JOBCD"               => (strlen(trim($tmp[16]))) ? sprintf("%02d",$tmp[16]) : "",
                                  "GUARD_WORK_NAME"           => $tmp[17],
                                  "GUARD_WORK_TELNO"          => $tmp[18],
                                  "GUARANTOR_RELATIONSHIP"    => $tmp[19],
                                  "GUARANTOR_NAME"            => $tmp[20],
                                  "GUARANTOR_KANA"            => $tmp[21],
                                  "GUARANTOR_SEX"             => $tmp[22],
                                  "GUARANTOR_ZIPCD"           => $tmp[23],
                                  "GUARANTOR_ADDR1"           => $tmp[24],
                                  "GUARANTOR_ADDR2"           => $tmp[25],
                                  "GUARANTOR_TELNO"           => $tmp[26],
                                  "GUARANTOR_JOBCD"           => (strlen(trim($tmp[27]))) ? sprintf("%02d",$tmp[27]) : "",
                                  "PUBLIC_OFFICE"             => $tmp[28]);

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
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $sonzaiarray = array("RELATIONSHIP"             => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("SCHREGNO","RELATIONSHIP"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key,array("RELATIONSHIP","GUARD_SEX","GUARD_JOBCD","GUARANTOR_RELATIONSHIP","GUARANTOR_SEX","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && 1 > $db->getOne(knjx160bQuery::checkMstData($sonzaiarray[$key], $data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }

                if (in_array($key,array("GUARD_BIRTHDAY"))) {
                    if($data[$key] != "" && !$this->date_rep($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key,array("SCHREGNO","RELATIONSHIP","GUARD_JOBCD","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } else {
                //数値チェック
                if (in_array($key,array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx160bQuery::insertQueryErr($db, $line, $check_error);
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

        $header = array("GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "SCHREGNO"                  => "※学籍番号",
                        "RELATIONSHIP"              => "※続柄",
                        "GUARD_NAME"                => "保護者氏名",
                        "GUARD_KANA"                => "保護者氏名かな",
                        "GUARD_SEX"                 => "性別",
                        "GUARD_BIRTHDAY"            => "生年月日",
                        "GUARD_ZIPCD"               => "郵便番号",
                        "GUARD_ADDR1"               => "住所1",
                        "GUARD_ADDR2"               => "住所2",
                        "GUARD_TELNO"               => "電話番号",
                        "GUARD_FAXNO"               => "FAX番号",
                        "GUARD_E_MAIL"              => "E-MAIL",
                        "GUARD_JOBCD"               => "職種コード",
                        "GUARD_WORK_NAME"           => "勤務先名称",
                        "GUARD_WORK_TELNO"          => "勤務先電話番号",
                        "GUARANTOR_RELATIONSHIP"    => "保証人続柄",
                        "GUARANTOR_NAME"            => "保証人氏名",
                        "GUARANTOR_KANA"            => "保証人氏名かな",
                        "GUARANTOR_SEX"             => "保証人性別",
                        "GUARANTOR_ZIPCD"           => "保証人郵便番号",
                        "GUARANTOR_ADDR1"           => "保証人住所1",
                        "GUARANTOR_ADDR2"           => "保証人住所2",
                        "GUARANTOR_TELNO"           => "保証人電話番号",
                        "GUARANTOR_JOBCD"           => "保証人職種コード",
                        "PUBLIC_OFFICE"             => "兼ねている公職",
                        "DUMMY"                     => "DUMMY");

        $header_rei = array("GRADE"                     => "99",
                            "HR_CLASS"                  => "999",
                            "ATTENDNO"                  => "999",
                            "NAME"                      => "アルプ太郎",
                            "SCHREGNO"                  => "12345678",
                            "RELATIONSHIP"              => "99",
                            "GUARD_NAME"                => "全角20",
                            "GUARD_KANA"                => "全角40",
                            "GUARD_SEX"                 => "9",
                            "GUARD_BIRTHDAY"            => "9999-99-99",
                            "GUARD_ZIPCD"               => "999-9999",
                            "GUARD_ADDR1"               => "全角25",
                            "GUARD_ADDR2"               => "全角25",
                            "GUARD_TELNO"               => "9999-9999-9999",
                            "GUARD_FAXNO"               => "9999-9999-9999",
                            "GUARD_E_MAIL"              => "aaa123@bbb.co.jp",
                            "GUARD_JOBCD"               => "99",
                            "GUARD_WORK_NAME"           => "全角20",
                            "GUARD_WORK_TELNO"          => "9999-9999-9999",
                            "GUARANTOR_RELATIONSHIP"    => "99",
                            "GUARANTOR_NAME"            => "全角20",
                            "GUARANTOR_KANA"            => "全角40",
                            "GUARANTOR_SEX"             => "9",
                            "GUARANTOR_ZIPCD"           => "999-9999",
                            "GUARANTOR_ADDR1"           => "全角25",
                            "GUARANTOR_ADDR2"           => "全角25",
                            "GUARANTOR_TELNO"           => "9999-9999-9999",
                            "GUARANTOR_JOBCD"           => "99",
                            "PUBLIC_OFFICE"             => "全角10",
                            "DUMMY"                     => "DUMMY");

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
                $query = knjx160bQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjx160bQuery::selectMainQuery($this);
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
                $couse = "保護者データ";
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

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    function date_rep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

}
//var_dump($_REQUEST);
?>
