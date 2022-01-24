<?php

require_once('for_php7.php');

class knjl514jModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別(データ取込)
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別(データ取込)
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),        //入試方式(データ取込)
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl514jQuery::updateQueryCsv($this, $this->data_arr);
            } else {
                // 削除
                $cnt = knjl514jQuery::deleteQueryCsv($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["TEST_DATE"]             = array(10,   "",       "DATE2", "試験日");
            $checkdata["EXAMNO"]                = array(10,   "HISSU",  "",      "受験番号_システム");
            $checkdata["SHIGANSYA_SEQ"]         = array(10,   "HISSU",  "INT",   "志願者SEQ");
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "",       "",      "志願者氏名（カナ）（姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "",       "",      "志願者氏名（カナ）（名）");
            $checkdata["BIRTHDAY"]              = array(10,   "HISSU",  "DATE",  "生年月日");//西暦年(4) + 月(2) + 日(2)
            $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");//'-'なし　7桁
            $checkdata["PREF_NAME"]             = array(30,   "",       "",      "都道府県");
            $checkdata["CITY_NAME"]             = array(90,   "",       "",      "市区町村");
            $checkdata["BANCHI_NAME"]           = array(90,   "",       "",      "町名・番地");
            $checkdata["ADDRESS2"]              = array(150,  "",       "",      "建物名・部屋番号");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
            $checkdata["MIRAI_FS_CD"]           = array(10,   "",       "",      "出身校コード");
            $checkdata["MIRAI_FS_NAME"]         = array(150,  "",       "",      "出身校名称");
            $checkdata["MIRAI_PS_CD"]           = array(10,   "",       "",      "塾コード");
            $checkdata["MIRAI_PS_NAME"]         = array(150,  "",       "",      "塾名称");
            $checkdata["GNAME_SEI"]             = array(60,   "",       "",      "保護者氏名(漢字)（姓）");
            $checkdata["GNAME_MEI"]             = array(60,   "",       "",      "保護者氏名(漢字)（名）");
            $checkdata["GKANA_SEI"]             = array(120,  "",       "",      "保護者氏名(カナ)（姓）");
            $checkdata["GKANA_MEI"]             = array(120,  "",       "",      "保護者氏名(カナ)（名）");
            $checkdata["GTELNO"]                = array(14,   "",       "",      "緊急連絡先（電話番号）");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl514jQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) $tmp[$t] = trim($tmp[$t]);

                    //チェック用 CSV取込項目
                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"]              = $tmp[$cnt++];
                    $data["APPNO"]                    = $tmp[$cnt++];
                    $data["LOGIN_USER_SEQ"]           = $tmp[$cnt++];
                    $data["LOGIN_MAIL"]               = $tmp[$cnt++];
                    $data["SHIGANSYA_SEQ"]            = $tmp[$cnt++];
                    $data["MIRAI_TESTDIV"]            = $tmp[$cnt++];
                    $data["NAME_SEI"]                 = $tmp[$cnt++];
                    $data["NAME_MEI"]                 = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]            = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]            = $tmp[$cnt++];
                    $data["BIRTHDAY"]                 = $tmp[$cnt++];
                    $data["ZIPCD"]                    = $tmp[$cnt++];
                    $data["PREF_NAME"]                = $tmp[$cnt++];
                    $data["CITY_NAME"]                = $tmp[$cnt++];
                    $data["BANCHI_NAME"]              = $tmp[$cnt++];
                    $data["ADDRESS2"]                 = $tmp[$cnt++];
                    $data["TELNO"]                    = $tmp[$cnt++];
                    $data["MIRAI_FS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME_OTHER"]      = $tmp[$cnt++];
                    $data["MIRAI_SH_CD1"]             = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME1"]           = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME_OTHER1"]     = $tmp[$cnt++];
                    $data["MIRAI_SH_CD2"]             = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME2"]           = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME_OTHER2"]     = $tmp[$cnt++];
                    $data["MIRAI_SH_CD3"]             = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME3"]           = $tmp[$cnt++];
                    $data["MIRAI_SH_NAME_OTHER3"]     = $tmp[$cnt++];
                    $data["MIRAI_PS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME_OTHER"]      = $tmp[$cnt++];
                    $data["GNAME_SEI"]                = $tmp[$cnt++];
                    $data["GNAME_MEI"]                = $tmp[$cnt++];
                    $data["GKANA_SEI"]                = $tmp[$cnt++];
                    $data["GKANA_MEI"]                = $tmp[$cnt++];
                    $data["GTELNO"]                   = $tmp[$cnt++];
                    $data["EXPLAIN"]                  = $tmp[$cnt++];
                    $data["APP_INFO_DIV"]             = $tmp[$cnt++];
                    $data["SRV_RCPT_DIV"]             = $tmp[$cnt++];
                    $data["COUNTER_RCPTNO"]           = $tmp[$cnt++];
                    $data["REMARKS"]                  = $tmp[$cnt++];
                    $data["SETTLE_MONEY"]             = $tmp[$cnt++];
                    $data["EXAM_MONEY"]               = $tmp[$cnt++];
                    $data["FEES"]                     = $tmp[$cnt++];
                    $data["PAY_TYPE_CD"]              = $tmp[$cnt++];
                    $data["PAY_DUE_DATE"]             = $tmp[$cnt++];
                    $data["PAY_DAY"]                  = $tmp[$cnt++];
                    $data["PAY_INFO_DIV"]             = $tmp[$cnt++];
                    $data["APP_COMP_DAY"]             = $tmp[$cnt++];
                    $data["CANCEL_FLG"]               = $tmp[$cnt++];
                    $data["CANCEL_DATE"]              = $tmp[$cnt++];
                    $data["TEST_NO"]                  = $tmp[$cnt++];//add
                    $data["TEST_DATE"]                = $tmp[$cnt++];
                    $data["TEST_NAME"]                = $tmp[$cnt++];
                    $data["TEST_NAME_ABBV"]           = $tmp[$cnt++];
                    $data["EXAMNO"]                   = $tmp[$cnt++];
                    $data["EXAMNO_MANUAL"]            = $tmp[$cnt++];
                    $data["STATUS_CD"]                = $tmp[$cnt++];//add

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $data["TEST_DATE"] = $this->date_rep2($data["TEST_DATE"]);
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

    /********************/
    /*  エラーチェック  */
    /********************/
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";

        foreach ($checkdata as $key => $val) {
            // 更新
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
                //日付チェック
                if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                } else if ($val[2] == "DATE2" && $data[$key] != "" && $this->date_rep2($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            // 削除
            } else {
                //必須項目チェック
                $array = array("ENTEXAMYEAR", "SHIGANSYA_SEQ", "EXAMNO");
                if (in_array($key, $array) && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl514jQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //生年月日変換 項目チェック後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = str_replace("/", "", $data);
            $data = substr($data,0,4)."-".substr($data,4,2)."-".substr($data,6,2);
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    //テスト日付変換 項目チェック後に行う。
    function date_rep2($data) {
        if ($data != "") {
            $data = str_replace("/", "-", $data);
            list($year, $month, $day) = explode("-", $data);
            $data = sprintf("%04d-%02d-%02d", $year, $month, $day);
            $data = common::DateConv1(str_replace("-","/",$data),4);
        }

        return $data;
    }

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header = array();
        $header["ENTEXAMYEAR"]              = "年度";
        $header["APPNO"]                    = "出願申込番号";
        $header["LOGIN_USER_SEQ"]           = "ログインユーザSEQ";
        $header["LOGIN_MAIL"]               = "ログインメールアドレス";
        $header["SHIGANSYA_SEQ"]            = "志願者SEQ";
        $header["MIRAI_TESTDIV"]            = "入試区分";
        $header["NAME_SEI"]                 = "志願者氏名（漢字）（姓）";
        $header["NAME_MEI"]                 = "志願者氏名（漢字）（名）";
        $header["NAME_KANA_SEI"]            = "志願者氏名（カナ）（姓）";
        $header["NAME_KANA_MEI"]            = "志願者氏名（カナ）（名）";
        $header["BIRTHDAY"]                 = "生年月日";
        $header["ZIPCD"]                    = "郵便番号";
        $header["PREF_NAME"]                = "都道府県";
        $header["CITY_NAME"]                = "市区町村";
        $header["BANCHI_NAME"]              = "町名・番地";
        $header["ADDRESS2"]                 = "建物名・部屋番号";
        $header["TELNO"]                    = "電話番号";
        $header["MIRAI_FS_CD"]              = "出身校コード";
        $header["MIRAI_FS_NAME"]            = "出身校名称";
        $header["MIRAI_FS_NAME_OTHER"]      = "出身校名称その他";
        $header["MIRAI_SH_CD1"]             = "併願校1コード";
        $header["MIRAI_SH_NAME1"]           = "併願校1名称";
        $header["MIRAI_SH_NAME_OTHER1"]     = "併願校1名称その他";
        $header["MIRAI_SH_CD2"]             = "併願校2コード";
        $header["MIRAI_SH_NAME2"]           = "併願校2名称";
        $header["MIRAI_SH_NAME_OTHER2"]     = "併願校2名称その他";
        $header["MIRAI_SH_CD3"]             = "併願校3コード";
        $header["MIRAI_SH_NAME3"]           = "併願校3名称";
        $header["MIRAI_SH_NAME_OTHER3"]     = "併願校3名称その他";
        $header["MIRAI_PS_CD"]              = "塾コード";
        $header["MIRAI_PS_NAME"]            = "塾名称";
        $header["MIRAI_PS_NAME_OTHER"]      = "塾名称その他";
        $header["GNAME_SEI"]                = "保護者氏名(漢字)（姓）";
        $header["GNAME_MEI"]                = "保護者氏名(漢字)（名）";
        $header["GKANA_SEI"]                = "保護者氏名(カナ)（姓）";
        $header["GKANA_MEI"]                = "保護者氏名(カナ)（名）";
        $header["GTELNO"]                   = "緊急連絡先（電話番号）";
        $header["EXPLAIN"]                  = "緊急連絡先（説明）";
        $header["APP_INFO_DIV"]             = "出願情報状態区分";
        $header["SRV_RCPT_DIV"]             = "調査書受領区分";
        $header["COUNTER_RCPTNO"]           = "窓口受付番号";
        $header["REMARKS"]                  = "備考欄";
        $header["SETTLE_MONEY"]             = "決済金額";
        $header["EXAM_MONEY"]               = "受験料";
        $header["FEES"]                     = "事務手数料";
        $header["PAY_TYPE_CD"]              = "支払種別コード";
        $header["PAY_DUE_DATE"]             = "支払期限日";
        $header["PAY_DAY"]                  = "入金日";
        $header["PAY_INFO_DIV"]             = "入金情報区分";
        $header["APP_COMP_DAY"]             = "申込完了日";
        $header["CANCEL_FLG"]               = "キャンセルフラグ";
        $header["CANCEL_DATE"]              = "キャンセル日";
        $header["TEST_NO"]                  = "試験番号";//add
        $header["TEST_DATE"]                = "試験日";
        $header["TEST_NAME"]                = "試験名";
        $header["TEST_NAME_ABBV"]           = "試験名略称";
        $header["EXAMNO"]                   = "受験番号_システム";
        $header["EXAMNO_MANUAL"]            = "受験番号_手動";
        $header["STATUS_CD"]                = "ステータスコード";//add

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
        } else if ($this->field["OUTPUT"] == "2") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //ヘッダ以外
        if ($this->field["OUTPUT"] != "3") {
            //エラー
            $query = knjl514jQuery::selectCsvErrQuery();

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //エラー
                $setArray = $header_err;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($setArray as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"] == "2"){      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志願者データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
