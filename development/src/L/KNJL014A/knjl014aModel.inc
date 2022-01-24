<?php

require_once('for_php7.php');

class knjl014aModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = VARS::post("YEAR");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //賢者受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //賢者試験区分
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
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl014aQuery::updateQueryCsv($this, $this->data_arr);
            } else {
                // 削除
                $cnt = knjl014aQuery::DeleteQueryCsv($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["EXAMNO"]                = array(10,   "HISSU",  "INT",   "志願者SEQ");
            $checkdata["MIRAI_TESTDIV"]         = array(100,  "HISSU",  "",      "入試区分");
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "HISSU",  "",      "志願者氏名（かな）（姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "HISSU",  "",      "志願者氏名（かな）（名）");
            $checkdata["SEX"]                   = array(10,   "HISSU",  "",      "性別");
            $checkdata["BIRTHDAY"]              = array(10,   "HISSU",  "DATE",  "生年月日");//西暦年(4) + 月(2) + 日(2)
            $checkdata["ZIPCD"]                 = array(8,    "HISSU",  "",      "郵便番号");//'-'なし　7桁
            $checkdata["PREF_NAME"]             = array(30,   "HISSU",  "",      "都道府県");
            $checkdata["CITY_NAME"]             = array(90,   "HISSU",  "",      "市区町村");
            $checkdata["BANCHI_NAME"]           = array(90,   "HISSU",  "",      "町名・番地");
            $checkdata["ADDRESS2"]              = array(150,  "",       "",      "建物名・部屋番号");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
            $checkdata["MIRAI_FS_CD"]           = array(10,   "",       "",      "出身校コード");
            $checkdata["MIRAI_FS_NAME"]         = array(150,  "",       "",      "出身校名称");
            $checkdata["MIRAI_PS_CD"]           = array(10,   "",       "",      "塾コード");
            $checkdata["MIRAI_PS_NAME"]         = array(150,  "",       "",      "塾名称");
            $checkdata["GNAME_SEI"]             = array(60,   "",       "",      "保護者氏名(漢字)（姓）");
            $checkdata["GNAME_MEI"]             = array(60,   "",       "",      "保護者氏名(漢字)（名）");
            $checkdata["GKANA_SEI"]             = array(120,  "",       "",      "保護者氏名(かな)（姓）");
            $checkdata["GKANA_MEI"]             = array(120,  "",       "",      "保護者氏名(かな)（名）");
            $checkdata["GZIPCD"]                = array(8,    "",       "",      "郵便番号");//'-'なし　7桁
            $checkdata["GPREF_NAME"]            = array(30,   "",       "",      "都道府県");
            $checkdata["GCITY_NAME"]            = array(90,   "",       "",      "市区町村");
            $checkdata["GBANCHI_NAME"]          = array(90,   "",       "",      "町名・番地");
            $checkdata["GADDRESS2"]             = array(150,  "",       "",      "建物名・部屋番号");
            $checkdata["EMERGENCYTELNO"]        = array(14,   "",       "",      "緊急連絡先（自宅以外の連絡先）");
            $checkdata["REMARK1"]               = array(246,  "",       "",      "備考");
            $checkdata["RECEPTNO"]              = array(10,   "HISSU",  "",      "受験番号");


            //試験名マッピングテーブル取得
            $mapping = array();
            $query = knjl014aQuery::getMappingMst($this->ObjYear, $this->field["APPLICANTDIV"], $this->field["TESTDIV"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $mapping[$row["TESTDIV_NAME"]] = array("SHDIV"     => $row["SHDIV"],
                                                       "COURSEDIV" => $row["COURSEDIV"],
                                                       "EXAM_TYPE" => $row["EXAM_TYPE"]
                                                      );
            }

            //複数チェック用
            $checkExamno = array();

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014aQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (($buffer = fgetcsv($fp)) !== false) {
                //文字コードを判別、改行コードを削除
                mb_convert_variables(mb_http_output(), "SJIS-win", $buffer);
                $buffer = implode(",", $buffer);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    $data = array();

                    //入力された氏名のふりがなを全てひらがなに統一する
                    $tmp[14]     = mb_convert_kana($tmp[14], "c");    //NAME_KANA_SEI
                    $tmp[15]     = mb_convert_kana($tmp[15], "c");    //NAME_KANA_MEI
                    $tmp[30]     = mb_convert_kana($tmp[30], "c");    //GKANA_SEI
                    $tmp[31]     = mb_convert_kana($tmp[31], "c");    //GKANA_MEI

                    //※住所が文字サイズを超えた場合は半角に変換する
                    $maxSizeArray    = $db->getRow(knjl014aQuery::getAddressMaxSize(), DB_FETCHMODE_ASSOC);
                    $address1MaxSize = $maxSizeArray["ADDRESS1_MAX_SIZE"];
                    $address2MaxSize = $maxSizeArray["ADDRESS2_MAX_SIZE"];
                    if (strlen($tmp[19].$tmp[20].$tmp[21]) > $address1MaxSize) {
                        $tmp[19]     = mb_convert_kana($tmp[19], "rn"); //PREF_NAME
                        $tmp[20]     = mb_convert_kana($tmp[20], "rn"); //CITY_NAME
                        $tmp[21]     = mb_convert_kana($tmp[21], "rn"); //BANCHI_NAME
                    }
                    if (strlen($tmp[22]) > $address2MaxSize) {
                        $tmp[22]     = mb_convert_kana($tmp[22], "rn"); //ADDRESS2
                    }

                    //チェック用 CSV取込項目
                    $data["ENTEXAMYEAR"]           = $tmp[0];
                    $data["MIRAI_TESTDIV"]         = $tmp[1];
                    $data["TEST_DATE"]             = $tmp[2];
                    $data["TEST_NAME"]             = $tmp[3];
                    $data["TEST_NAME_ABBV"]        = $tmp[4];
                    $data["RECEPTNO"]              = $tmp[5];
                    $data["EXAMNO"]                = $tmp[11];
                    $data["NAME_SEI"]              = $tmp[12];
                    $data["NAME_MEI"]              = $tmp[13];
                    $data["NAME_KANA_SEI"]         = $tmp[14];
                    $data["NAME_KANA_MEI"]         = $tmp[15];
                    $data["SEX"]                   = $tmp[16];
                    $data["BIRTHDAY"]              = $tmp[17];
                    $data["ZIPCD"]                 = $tmp[18];
                    $data["PREF_NAME"]             = $tmp[19];
                    $data["CITY_NAME"]             = $tmp[20];
                    $data["BANCHI_NAME"]           = $tmp[21];
                    $data["ADDRESS2"]              = $tmp[22];
                    $data["TELNO"]                 = $tmp[23];
                    $data["MIRAI_FS_CD"]           = $tmp[24];
                    $data["MIRAI_FS_NAME"]         = $tmp[25];
                    $data["MIRAI_PS_CD"]           = $tmp[27];
                    $data["MIRAI_PS_NAME"]         = $tmp[28];
                    $data["GNAME_SEI"]             = $tmp[30];
                    $data["GNAME_MEI"]             = $tmp[31];
                    $data["GKANA_SEI"]             = $tmp[32];
                    $data["GKANA_MEI"]             = $tmp[33];
                    $data["GZIPCD"]                = $tmp[34];
                    $data["GPREF_NAME"]            = $tmp[35];
                    $data["GCITY_NAME"]            = $tmp[36];
                    $data["GBANCHI_NAME"]          = $tmp[37];
                    $data["GADDRESS2"]             = $tmp[38];
                    $data["EMERGENCYTELNO"]        = $tmp[39];
                    $data["REMARK1"]               = $tmp[45];

                    //マッピング
                    $data["SHDIV"]      = $mapping[$data["TEST_NAME"]]["SHDIV"];
                    $data["COURSEDIV"]  = $mapping[$data["TEST_NAME"]]["COURSEDIV"];
                    $data["EXAM_TYPE"]  = $mapping[$data["TEST_NAME"]]["EXAM_TYPE"];
                    $data["MAP_CHKFLG"] = ($data["SHDIV"] && $data["COURSEDIV"] && $data["EXAM_TYPE"]) ? "1" : "";

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $checkExamno)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;

                    //複数チェック用保持
                    $checkExamno[] = $data["EXAMNO"];
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
    public function errCheck($db, $data, $checkdata, $line, $checkExamno)
    {
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
            // 更新処理
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }

                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値を入力してください", $numericSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }

                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※指定した入試年度のデータを入力してください。:", $existenceSep, $val[3]);
                    }
                }

                //日付チェック
                if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key], $key) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※正しい日付を入力してください。:", $otherSep, $val[3]);
                }
                //文字数チェック
                //※住所の文字サイズはチェックしない仕様。
                if (!in_array($key, array("PREF_NAME", "CITY_NAME", "BANCHI_NAME", "ADDRESS2")) && strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字数オーバーです。:", $lengthSep, $val[3]);
                }
            } else {
                //必須項目チェック
                $array = array("ENTEXAMYEAR", "TEST_NAME", "EXAMNO", "RECEPTNO");
                if (in_array($key, $array) && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※指定した入試年度のデータを入力してください。:", $existenceSep, $val[3]);
                    }
                }
            }
        }

        //マッピング可否チェック
        if ($data["MAP_CHKFLG"] == "") {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "対応マスタに該当する設定がありません。");
        }

        //一つの志願者SEQが一つの試験に対して、複数にならないように取り込みの段階でエラー処理する
        //①登録済データとチェック
        //・一つの志願者SEQが一つの試験に対して、既に登録済みの場合エラー(受験番号も一致する登録済データの場合は上書き可能)
        $recept_cnt = $db->getOne(knjl014aQuery::cntEntexamReceptDat($this, $data));
        if ($recept_cnt > 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "志願者SEQが重複(登録済)");
        }
        //②取込CSVのチェック
        //・一つの志願者SEQが一つの試験に対して、複数ある場合、1レコード目のみ有効、2レコード目以降エラー
        if (in_array($data["EXAMNO"], $checkExamno)) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "志願者SEQが重複(CSV)");
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl014aQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function date_rep($data, $key)
    {
        if ($data != "") {
            if ($key == "BIRTHDAY") {
                $data = str_replace("/", "", $data);
                $data = substr($data, 0, 4)."-".substr($data, 4, 2)."-".substr($data, 6, 2);
            } else {
                $data = str_replace("-", "/", $data);
                list($year, $month, $day) = explode("/", $data);
                $data = $year."-".$month."-".$day;
            }

            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep."(".$val.")";
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $header = array();
        $header["ENTEXAMYEAR"]             = "年度";
        $header["MIRAI_TESTDIV"]           = "入試区分";
        $header["TEST_DATE"]               = "試験日";
        $header["TEST_NAME"]               = "試験名";
        $header["TEST_NAME_ABBV"]          = "試験名略称";
        $header["RECEPTNO"]                = "受験番号_システム";
        $header["ITEM55"]                  = "受験番号_手動";
        $header["ITEM2"]                   = "出願申込番号";
        $header["ITEM3"]                   = "ログインユーザSEQ";
        $header["ITEM4"]                   = "ログインメールアドレス";
        $header["ITEM5"]                   = "申込時メールアドレス";
        $header["EXAMNO"]                  = "志願者SEQ";
        $header["NAME_SEI"]                = "志願者氏名（漢字）（姓）";
        $header["NAME_MEI"]                = "志願者氏名（漢字）（名）";
        $header["NAME_KANA_SEI"]           = "志願者氏名（かな）（姓）";
        $header["NAME_KANA_MEI"]           = "志願者氏名（かな）（名）";
        $header["SEX"]                     = "性別";
        $header["BIRTHDAY"]                = "生年月日";
        $header["ZIPCD"]                   = "郵便番号";
        $header["PREF_NAME"]               = "都道府県";
        $header["CITY_NAME"]               = "市区町村";
        $header["BANCHI_NAME"]             = "町名・番地";
        $header["ADDRESS2"]                = "建物名・部屋番号";
        $header["TELNO"]                   = "電話番号";
        $header["MIRAI_FS_CD"]             = "出身校コード";
        $header["MIRAI_FS_NAME"]           = "出身校名称";
        $header["ITEM21"]                  = "出身校名称その他";
        $header["MIRAI_PS_CD"]             = "塾コード";
        $header["MIRAI_PS_NAME"]           = "塾名称";
        $header["ITEM24"]                  = "塾名称その他";
        $header["GNAME_SEI"]               = "保護者氏名(漢字)（姓）";
        $header["GNAME_MEI"]               = "保護者氏名(漢字)（名）";
        $header["GKANA_SEI"]               = "保護者氏名(かな)（姓）";
        $header["GKANA_MEI"]               = "保護者氏名(かな)（名）";
        $header["GZIPCD"]                  = "郵便番号";
        $header["GPREF_NAME"]              = "都道府県";
        $header["GCITY_NAME"]              = "市区町村";
        $header["GBANCHI_NAME"]            = "町名・番地";
        $header["GADDRESS2"]               = "建物名・部屋番号";
        $header["EMERGENCYTELNO"]          = "緊急連絡先";
        $header["ITEM35"]                  = "緊急連絡先名称（例：父携帯）";
        $header["ITEM36"]                  = "出願情報状態区分";
        $header["ITEM37"]                  = "調査書受領区分";
        $header["ITEM37_NEXT"]             = "調査書受領メール送信日";
        $header["ITEM38"]                  = "窓口受付番号";
        $header["REMARK1"]                 = "備考欄";
        $header["ITEM40"]                  = "決済金額";
        $header["ITEM41"]                  = "受験料";
        $header["ITEM42"]                  = "事務手数料";
        $header["ITEM43"]                  = "支払種別コード";
        $header["ITEM44"]                  = "支払期限日";
        $header["ITEM45"]                  = "入金日";
        $header["ITEM45_NEXT"]             = "入金日_手動";
        $header["ITEM46"]                  = "入金情報区分";
        $header["ITEM47"]                  = "申込完了日";
        $header["ITEM48"]                  = "キャンセルフラグ";
        $header["ITEM49"]                  = "キャンセル日";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
//            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "2") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //ヘッダ以外
        if ($this->field["OUTPUT"] != "3") {
            if ($this->field["OUTPUT"]=="2") {
                //エラー
                $query = knjl014aQuery::selectCsvErrQuery();
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
