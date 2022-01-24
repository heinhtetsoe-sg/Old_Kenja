<?php

require_once('for_php7.php');

class knjl014uModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //ミライコンパスCSVテーブル情報取得(全56項目)
        $this->miraiDataCnt = 56;
        $this->miraiTableInfo = array();
        $this->miraiTableInfo = knjl014uQuery::getMiraiTableInfo($this->miraiDataCnt);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr_mirai = array();
        list($this->data_arr, $this->data_arr_mirai) = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $cnt = knjl014uQuery::updateQueryCsv($this, $this->data_arr, $this->data_arr_mirai);

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
        $temp_arr_mirai = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");//データ例「01:中学入試」　名称あり
            $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT",   "受験番号");//先頭1桁で入試区分判別　1XXXX(第1回),2XXXX(第2回),3XXXX(第3回)
            $checkdata["RECOM_EXAMNO"]          = array(7,    "HISSU",  "INT2",  "志願者SEQ");
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "",       "",      "志願者氏名（カナ姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "",       "",      "志願者氏名（カナ名）");
            $checkdata["BIRTHDAY"]              = array(10,   "HISSU",  "DATE",  "生年月日");//西暦年(4) + 月(2) + 日(2)
            $checkdata["ZIPCD"]                 = array(7,    "HISSU",  "INT",   "郵便番号");//'-'なし　7桁
            $checkdata["PREF_NAME"]             = array(30,   "HISSU",  "",      "都道府県");
            $checkdata["CITY_NAME"]             = array(90,   "HISSU",  "",      "市区町村");
            $checkdata["BANCHI_NAME"]           = array(90,   "HISSU",  "",      "町名・番地");
            $checkdata["ADDRESS2"]              = array(150,  "",       "",      "建物名・部屋番号");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
            $checkdata["MIRAI_FS_CD"]           = array(7,    "",       "INT2",  "出身小学校コード");
            $checkdata["GNAME_SEI"]             = array(60,   "",       "",      "保護者氏名（漢字姓）");
            $checkdata["GNAME_MEI"]             = array(60,   "",       "",      "保護者氏名（漢字名）");
            $checkdata["GKANA_SEI"]             = array(120,  "",       "",      "保護者氏名（カナ姓）");
            $checkdata["GKANA_MEI"]             = array(120,  "",       "",      "保護者氏名（カナ名）");
            $checkdata["GZIPCD"]                = array(7,    "",       "INT",   "保護者郵便番号");
            $checkdata["GPREF_NAME"]            = array(30,   "",       "",      "保護者都道府県");
            $checkdata["GCITY_NAME"]            = array(90,   "",       "",      "保護者市区町村");
            $checkdata["GBANCHI_NAME"]          = array(90,   "",       "",      "保護者町名・番地");
            $checkdata["GADDRESS2"]             = array(150,  "",       "",      "保護者建物名・部屋番号");
            $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
            $checkdata["EMERGENCYTELNO"]        = array(14,   "",       "",      "緊急連絡先");

            //チェック用
            $checkdata["TESTDIV"]               = array(1,    "",       "",      "入試区分");
            $checkdata["NAME"]                  = array(60,   "",       "",      "志願者氏名（漢字姓名）");
            $checkdata["NAME_KANA"]             = array(120,  "",       "",      "志願者氏名（カナ姓名）");
            $checkdata["ADDRESS1"]              = array(150,  "",       "",      "志願者住所（都道府県、市区町村、町名・番地）");
            $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名（漢字姓名）");
            $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者氏名（カナ姓名）");
            $checkdata["GADDRESS1"]             = array(150,  "",       "",      "保護者住所（都道府県、市区町村、町名・番地）");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014uQuery::deleteQueryErr();
            $result = $db->query($query);

            //変換用出身学校コード格納
            $fscd = array();
            $query = knjl014uQuery::getFsCd();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $fscd[$row["MIRAI_FS_CD"]] = $row["FS_CD"];
            }
            $result->free();

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

                    $data = array();

                    //チェック用 CSV取込項目
                    $data["ENTEXAMYEAR"]            = $tmp[0];
                    $data["APPLICANTDIV"]           = strlen($tmp[1]) ? substr($tmp[1],1,1) : ""; //データ例「01:中学入試」
                    $data["EXAMNO"]                 = $tmp[5];
                    $data["RECOM_EXAMNO"]           = $tmp[11];
                    $data["NAME_SEI"]               = $tmp[12];
                    $data["NAME_MEI"]               = $tmp[13];
                    $data["NAME_KANA_SEI"]          = $tmp[14];
                    $data["NAME_KANA_MEI"]          = $tmp[15];
                    $data["BIRTHDAY"]               = $tmp[17];
                    $data["ZIPCD"]                  = $tmp[18];
                    $data["PREF_NAME"]              = $tmp[19];
                    $data["CITY_NAME"]              = $tmp[20];
                    $data["BANCHI_NAME"]            = $tmp[21];
                    $data["ADDRESS2"]               = $tmp[22];
                    $data["TELNO"]                  = $tmp[23];
                    $data["MIRAI_FS_CD"]            = strlen($tmp[24]) ? $fscd[$tmp[24]] : ""; //賢者の出身学校コードに変換
                    $data["GNAME_SEI"]              = $tmp[29];
                    $data["GNAME_MEI"]              = $tmp[30];
                    $data["GKANA_SEI"]              = $tmp[31];
                    $data["GKANA_MEI"]              = $tmp[32];
                    $data["GZIPCD"]                 = $tmp[33];
                    $data["GPREF_NAME"]             = $tmp[34];
                    $data["GCITY_NAME"]             = $tmp[35];
                    $data["GBANCHI_NAME"]           = $tmp[36];
                    $data["GADDRESS2"]              = $tmp[37];
                    $data["GTELNO"]                 = $tmp[38];
                    $data["EMERGENCYTELNO"]         = $tmp[39];

                    //チェック用
                    $data["TESTDIV"]                = strlen($data["EXAMNO"]) ? substr($data["EXAMNO"],0,1) : ""; //データ例「10001」「20001」「30001」
                    $data["NAME"]                   = $data["NAME_SEI"]."　".$data["NAME_MEI"];
                    $data["NAME_KANA"]              = $data["NAME_KANA_SEI"]."　".$data["NAME_KANA_MEI"];
                    $data["ADDRESS1"]               = $data["PREF_NAME"].$data["CITY_NAME"].$data["BANCHI_NAME"];
                    $data["GNAME"]                  = $data["GNAME_SEI"]."　".$data["GNAME_MEI"];
                    $data["GKANA"]                  = $data["GKANA_SEI"]."　".$data["GKANA_MEI"];
                    $data["GADDRESS1"]              = $data["GPREF_NAME"].$data["GCITY_NAME"].$data["GBANCHI_NAME"];

                    //ミライコンパス保持用
                    $data_mirai = array();
                    foreach ($this->miraiTableInfo as $field) {
                        $data_mirai[$field["COLNAME"]] = $tmp[$field["COLNO"]];
                    }

                    $dataCnt = $this->miraiDataCnt;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $data_mirai)) {
                        foreach ($checkdata as $key => $val) {
                            //郵便番号を変換してセット
                            if (in_array($key, array("ZIPCD","GZIPCD")) && strlen($data[$key])) {
                                $data[$key] = substr($data[$key],0,3)."-".substr($data[$key],3);
                            }
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                $data[$key] = $this->date_rep($data[$key]);
                            }
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}s", $data[$key]);
                            }
                        }
                        $temp_arr[$i] = $data;
                        $temp_arr_mirai[$i] = $data_mirai;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return array($temp_arr, $temp_arr_mirai);
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    function errCheck($db, $data, $checkdata, $line, $data_mirai) {
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
            $array = array("ENTEXAMYEAR","APPLICANTDIV","TESTDIV");
            if (in_array($key, $array)) {
                if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                if ($key == 'APPLICANTDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl014uQuery::nameCntSql("L003", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                if ($key == 'TESTDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl014uQuery::nameCntSql("L004", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl014uQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = substr($data,0,4)."-".substr($data,4,2)."-".substr($data,6,2);
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) $data = str_replace("/","-",$data);
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
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        // エラー出力
        $contents .= implode($header_err, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //エラー
        $query = knjl014uQuery::selectCsvErrQuery();
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header_err as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            $filename = sprintf("%s.csv", "エラー出力");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
