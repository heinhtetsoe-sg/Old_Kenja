<?php

require_once('for_php7.php');

class knjl514hModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = VARS::post("YEAR");

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試種別
                             "EXAM_TYPE"        =>  VARS::post("EXAM_TYPE"),        //入試方式
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //ミライコンパスCSVテーブル情報取得
        $this->miraiDataCnt = 90;
        $this->miraiTableInfo = array();
        $this->miraiTableInfo = knjl514hQuery::getMiraiTableInfo($this->miraiDataCnt);

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
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl514hQuery::updateQueryCsv($this, $this->data_arr, $this->data_arr_mirai);
            } else {
                // 削除
                $cnt = knjl514hQuery::DeleteQueryCsv($this, $this->data_arr, $this->data_arr_mirai);
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
        $temp_arr_mirai = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["SHIGANSYA_SEQ"]         = array(100,  "HISSU",  "INT",   "志願者SEQ");
            $checkdata["MIRAI_TESTDIV"]         = array(100,  "HISSU",  "",      "入試区分");//先頭2桁入試区分
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字）（名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "",       "",      "志願者氏名（カナ）（姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "",       "",      "志願者氏名（カナ）（名）");
            $checkdata["NOT_PC_KANJI_NAME"]     = array(100,  "",       "",      "パソコン等で表示できない氏名の漢字");
            $checkdata["SEX"]                   = array(10,   "HISSU",  "",      "性別");
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
            $checkdata["GTELNO"]                = array(14,   "",       "",      "緊急連絡先（自宅以外の連絡先）");

            $checkdata["FS_GRDYEAR"]            = array(60,   "",       "",      "出身校卒業年月");
            $checkdata["DORMITORY_FLG"]         = array(100,  "",       "",      "入寮希望");

            $checkdata["RELATIONSHIP"]          = array(30,   "",       "",      "保護者の本人との続柄");
            $checkdata["RELATIONSHIP_OTHER"]    = array(30,   "",       "",      "保護者の本人との続柄（その他）");
            $checkdata["RELANAME1"]             = array(60,   "",       "",      "家族構成１（氏名）");
            $checkdata["RELATIONSHIP1"]         = array(30,   "",       "",      "家族構成１（続柄）");
            $checkdata["RELA_AGE1"]             = array(3,    "",       "",      "家族構成１（年齢）");
            $checkdata["RELA_JOB1"]             = array(120,  "",       "",      "家族構成１（勤務先・在学校）");
            $checkdata["RELANAME2"]             = array(60,   "",       "",      "家族構成２（氏名）");
            $checkdata["RELATIONSHIP2"]         = array(30,   "",       "",      "家族構成２（続柄）");
            $checkdata["RELA_AGE2"]             = array(3,    "",       "",      "家族構成２（年齢）");
            $checkdata["RELA_JOB2"]             = array(120,  "",       "",      "家族構成２（勤務先・在学校）");
            $checkdata["RELANAME3"]             = array(60,   "",       "",      "家族構成３（氏名）");
            $checkdata["RELATIONSHIP3"]         = array(30,   "",       "",      "家族構成３（続柄）");
            $checkdata["RELA_AGE3"]             = array(3,    "",       "",      "家族構成３（年齢）");
            $checkdata["RELA_JOB3"]             = array(120,  "",       "",      "家族構成３（勤務先・在学校）");
            $checkdata["RELANAME4"]             = array(60,   "",       "",      "家族構成４（氏名）");
            $checkdata["RELATIONSHIP4"]         = array(30,   "",       "",      "家族構成４（続柄）");
            $checkdata["RELA_AGE4"]             = array(3,    "",       "",      "家族構成４（年齢）");
            $checkdata["RELA_JOB4"]             = array(120,  "",       "",      "家族構成４（勤務先・在学校）");
            $checkdata["RELANAME5"]             = array(60,   "",       "",      "家族構成５（氏名）");
            $checkdata["RELATIONSHIP5"]         = array(30,   "",       "",      "家族構成５（続柄）");
            $checkdata["RELA_AGE5"]             = array(3,    "",       "",      "家族構成５（年齢）");
            $checkdata["RELA_JOB5"]             = array(120,  "",       "",      "家族構成５（勤務先・在学校）");

            $checkdata["TEST_CD"]               = array(10,   "HISSU",  "",      "試験番号");
            $checkdata["TEST_DATE"]             = array(10,   "HISSU",  "DATE2", "試験日");
            $checkdata["TEST_NAME"]             = array(60,   "",       "",      "試験名");
            $checkdata["TEST_NAME_ABBV"]        = array(100,  "HISSU",  "",      "試験名略称");
            $checkdata["EXAMNO"]                = array(20,   "HISSU",  "",      "受験番号");
            $checkdata["SH_FLG"]                = array(100,  "",       "",      "普通コースとの併願希望の有無");
            $checkdata["MIRAI_FS_ADDR"]         = array(300,  "",       "",      "出身校所在地");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl514hQuery::deleteQueryErr();
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

                    $data = array();

                    //チェック用 CSV取込項目
                    $data["ENTEXAMYEAR"]           = $tmp[0];
                    $data["SHIGANSYA_SEQ"]         = $tmp[4];
                    $data["MIRAI_TESTDIV"]         = $tmp[5];
                    $data["NAME_SEI"]              = $tmp[6];
                    $data["NAME_MEI"]              = $tmp[7];
                    $data["NAME_KANA_SEI"]         = $tmp[8];
                    $data["NAME_KANA_MEI"]         = $tmp[9];
                    $data["NOT_PC_KANJI_NAME"]     = $tmp[10];
                    $data["SEX"]                   = $tmp[11];
                    $data["BIRTHDAY"]              = $tmp[12];
                    $data["ZIPCD"]                 = $tmp[13];
                    $data["PREF_NAME"]             = $tmp[14];
                    $data["CITY_NAME"]             = $tmp[15];
                    $data["BANCHI_NAME"]           = $tmp[16];
                    $data["ADDRESS2"]              = $tmp[17];
                    $data["TELNO"]                 = $tmp[18];
                    $data["MIRAI_FS_CD"]           = $tmp[19];
                    $data["MIRAI_FS_NAME"]         = $tmp[20];
                    $data["MIRAI_PS_CD"]           = $tmp[22];
                    $data["MIRAI_PS_NAME"]         = $tmp[23];
                    $data["GNAME_SEI"]             = $tmp[25];
                    $data["GNAME_MEI"]             = $tmp[26];
                    $data["GKANA_SEI"]             = $tmp[27];
                    $data["GKANA_MEI"]             = $tmp[28];
                    $data["GTELNO"]                = $tmp[29];
                    $data["FS_GRDYEAR"]            = $tmp[46];
                    $data["DORMITORY_FLG"]         = $tmp[49];
                    $data["RELATIONSHIP"]          = $tmp[60];
                    $data["RELATIONSHIP_OTHER"]    = $tmp[61];
                    $data["RELANAME1"]             = $tmp[62];
                    $data["RELATIONSHIP1"]         = $tmp[63];
                    $data["RELA_AGE1"]             = $tmp[64];
                    $data["RELA_JOB1"]             = $tmp[65];
                    $data["RELANAME2"]             = $tmp[66];
                    $data["RELATIONSHIP2"]         = $tmp[67];
                    $data["RELA_AGE2"]             = $tmp[68];
                    $data["RELA_JOB2"]             = $tmp[69];
                    $data["RELANAME3"]             = $tmp[70];
                    $data["RELATIONSHIP3"]         = $tmp[71];
                    $data["RELA_AGE3"]             = $tmp[72];
                    $data["RELA_JOB3"]             = $tmp[73];
                    $data["RELANAME4"]             = $tmp[74];
                    $data["RELATIONSHIP4"]         = $tmp[75];
                    $data["RELA_AGE4"]             = $tmp[76];
                    $data["RELA_JOB4"]             = $tmp[77];
                    $data["RELANAME5"]             = $tmp[78];
                    $data["RELATIONSHIP5"]         = $tmp[79];
                    $data["RELA_AGE5"]             = $tmp[80];
                    $data["RELA_JOB5"]             = $tmp[81];
                    $data["TEST_CD"]               = $tmp[82];
                    $data["TEST_DATE"]             = $tmp[83];
                    $data["TEST_NAME"]             = $tmp[84];
                    $data["TEST_NAME_ABBV"]        = $tmp[85];
                    $data["EXAMNO"]                = $tmp[86];
                    $data["SH_FLG"]                = $tmp[88];
                    $data["MIRAI_FS_ADDR"]         = $tmp[89];

                    //ミライコンパス保持用
                    $data_mirai = array();
                    foreach ($this->miraiTableInfo as $field) {
                        $data_mirai[$field["COLNAME"]] = $tmp[$field["COLNO"]];
                    }

                    $dataCnt = $this->miraiDataCnt;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $data_mirai)) {
                        $data_mirai["TEST_DATE"] = $this->date_rep2($data["TEST_DATE"]);
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
            // 更新処理
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
            } else {
                //必須項目チェック
                $array = array("ENTEXAMYEAR", "MIRAI_TESTDIV", "EXAMNO");
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
            knjl514hQuery::insertQueryErr($db, $line, $check_error);
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
        $header["ENTEXAMYEAR"]             = "年度";
        $header["ITEM2"]                   = "出願申込番号";
        $header["ITEM3"]                   = "ログインユーザSEQ";
        $header["ITEM4"]                   = "メールアドレス";
        $header["SHIGANSYA_SEQ"]           = "志願者SEQ";
        $header["MIRAI_TESTDIV"]           = "入試区分";
        $header["NAME_SEI"]                = "志願者氏名（漢字）（姓）";
        $header["NAME_MEI"]                = "志願者氏名（漢字）（名）";
        $header["NAME_KANA_SEI"]           = "志願者氏名（カナ）（姓）";
        $header["NAME_KANA_MEI"]           = "志願者氏名（カナ）（名）";
        $header["NOT_PC_KANJI_NAME"]       = "パソコン等で表示できない氏名の漢字";
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
        $header["ITEM22"]                  = "出身校名称その他";
        $header["MIRAI_PS_CD"]             = "塾コード";
        $header["MIRAI_PS_NAME"]           = "塾名称";
        $header["ITEM25"]                  = "塾名称その他";
        $header["GNAME_SEI"]               = "保護者氏名(漢字)（姓）";
        $header["GNAME_MEI"]               = "保護者氏名(漢字)（名）";
        $header["GKANA_SEI"]               = "保護者氏名(カナ)（姓）";
        $header["GKANA_MEI"]               = "保護者氏名(カナ)（名）";
        $header["GTELNO"]                  = "緊急連絡先（自宅以外の連絡先）";
        $header["ITEM31"]                  = "緊急連絡先の説明";
        $header["ITEM32"]                  = "出願情報状態区分";
        $header["ITEM33"]                  = "調査書受領区分";
        $header["ITEM34"]                  = "窓口受付番号";
        $header["ITEM35"]                  = "備考欄";
        $header["ITEM36"]                  = "決済金額";
        $header["ITEM37"]                  = "受験料";
        $header["ITEM38"]                  = "事務手数料";
        $header["ITEM39"]                  = "支払種別コード";
        $header["ITEM40"]                  = "支払期限日";
        $header["ITEM41"]                  = "入金日";
        $header["ITEM42"]                  = "入金情報区分";
        $header["ITEM43"]                  = "申込完了日";
        $header["ITEM44"]                  = "キャンセルフラグ";
        $header["ITEM45"]                  = "キャンセル日";
        $header["ITEM46"]                  = "出身校TEL";
        $header["FS_GRDYEAR"]              = "出身校卒業年月";
        $header["ITEM48"]                  = "海外在留歴";
        $header["ITEM49"]                  = "海外就学歴";
        $header["DORMITORY_FLG"]           = "入寮希望";
        $header["ITEM51"]                  = "入寮希望（その他）";
        $header["ITEM52"]                  = "国内帰省先住所";
        $header["ITEM53"]                  = "所持する実用英語技能検定の級";
        $header["ITEM54"]                  = "所持するTOEFL iBTのスコア";
        $header["ITEM55"]                  = "所持するTOEICのスコア";
        $header["ITEM56"]                  = "所持するIELTSのオーバーオール・バンド・スコア";
        $header["ITEM57"]                  = "所持する漢字検定の級";
        $header["ITEM58"]                  = "所持する数学検定の級";
        $header["ITEM59"]                  = "その他所持する資格等";
        $header["ITEM60"]                  = "英語特別クラス受講";
        $header["RELATIONSHIP"]            = "保護者の本人との続柄";
        $header["RELATIONSHIP_OTHER"]      = "保護者の本人との続柄（その他）";
        $header["RELANAME1"]               = "家族構成１（氏名）";
        $header["RELATIONSHIP1"]           = "家族構成１（続柄）";
        $header["RELA_AGE1"]               = "家族構成１（年齢）";
        $header["RELA_JOB1"]               = "家族構成１（勤務先・在学校）";
        $header["RELANAME2"]               = "家族構成２（氏名）";
        $header["RELATIONSHIP2"]           = "家族構成２（続柄）";
        $header["RELA_AGE2"]               = "家族構成２（年齢）";
        $header["RELA_JOB2"]               = "家族構成２（勤務先・在学校）";
        $header["RELANAME3"]               = "家族構成３（氏名）";
        $header["RELATIONSHIP3"]           = "家族構成３（続柄）";
        $header["RELA_AGE3"]               = "家族構成３（年齢）";
        $header["RELA_JOB3"]               = "家族構成３（勤務先・在学校）";
        $header["RELANAME4"]               = "家族構成４（氏名）";
        $header["RELATIONSHIP4"]           = "家族構成４（続柄）";
        $header["RELA_AGE4"]               = "家族構成４（年齢）";
        $header["RELA_JOB4"]               = "家族構成４（勤務先・在学校）";
        $header["RELANAME5"]               = "家族構成５（氏名）";
        $header["RELATIONSHIP5"]           = "家族構成５（続柄）";
        $header["RELA_AGE5"]               = "家族構成５（年齢）";
        $header["RELA_JOB5"]               = "家族構成５（勤務先・在学校）";
        $header["TEST_CD"]                 = "試験番号";
        $header["TEST_DATE"]               = "試験日";
        $header["TEST_NAME"]               = "試験名";
        $header["TEST_NAME_ABBV"]          = "試験名略称";
        $header["EXAMNO"]                  = "受験番号_システム";
        $header["ITEM88"]                  = "受験番号_手動";
        $header["SH_FLG"]                  = "普通コースとの併願希望の有無";
        $header["MIRAI_FS_ADDR"]           = "出身校所在地";
        $header["APPLICANTDIV"]            = "学校種別";
        $header["TESTDIV"]                 = "入試種別";
        $header["EXAM_TYPE"]               = "入試方式";
        $header["GRADE"]                   = "学年";
        $header["DISTINCT_ID"]             = "入試判別";

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
//            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"] == "2") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4" || $this->field["OUTPUT"] == "5" || $this->field["OUTPUT"] == "6") {
            //データ
            if($this->field["HEADER"] == "on") $contents .= implode($header, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        //ヘッダ以外
        if ($this->field["OUTPUT"] != "3") {
            if($this->field["OUTPUT"] == "2") {
                //エラー
                $query = knjl514hQuery::selectCsvErrQuery();
            } else  if ($this->field["OUTPUT"] == "4" || $this->field["OUTPUT"] == "5" || $this->field["OUTPUT"] == "6") {
                //データ
                $query = knjl514hQuery::selectDataQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if($this->field["OUTPUT"] == "4" || $this->field["OUTPUT"] == "5" || $this->field["OUTPUT"] == "6") {
                    //データ
                    //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace(array("\r\n", "\r", "\n"),"↓",str_replace(",",".",$row));
                    $setArray = $header;
                } else {
                    $setArray = $header_err;
                }

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
