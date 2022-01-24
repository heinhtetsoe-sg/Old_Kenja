<?php

require_once('for_php7.php');

class knjl016fModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "WEB_TESTDIV"      =>  VARS::post("WEB_TESTDIV"),      //入試区分(Web)
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        //入試区分(Web)
        $this->webTestdiv = array();
        //中学
        $this->webTestdiv["1"]["KIKOKU1"]  = array("CD" => "01", "NAME" => "海外帰国生入試　第１回",               "TESTDIV" => "6",   "EXAM_TYPE" => "");
        $this->webTestdiv["1"]["KIKOKU2"]  = array("CD" => "11", "NAME" => "海外帰国生入試　第２回",               "TESTDIV" => "7",   "EXAM_TYPE" => "");
        $this->webTestdiv["1"]["KIKOKU3"]  = array("CD" => "12", "NAME" => "海外帰国生入試　第３回",               "TESTDIV" => "15",  "EXAM_TYPE" => "");
        $this->webTestdiv["1"]["IPPAN"]    = array("CD" => "20", "NAME" => "一般入試（前期）",                    "TESTDIV" => "",    "EXAM_TYPE" => "");
        $this->webTestdiv["1"]["IPPAN2"]   = array("CD" => "30", "NAME" => "一般入試（後期）",                    "TESTDIV" => "17",  "EXAM_TYPE" => "1");
        $this->webTestdiv["1"]["TOKUTAI"]  = array("CD" => "40", "NAME" => "特待チャレンジ",                      "TESTDIV" => "5",  "EXAM_TYPE" => "3");
        $this->webTestdiv["1"]["POTEN4"]   = array("CD" => "45", "NAME" => "ポテンシャル４",                      "TESTDIV" => "3",  "EXAM_TYPE" => "2");
        //高校
        $this->webTestdiv["2"]["KIKOKU1"]  = array("CD" => "10", "NAME" => "海外帰国生入試　第１回",               "TESTDIV" => "4",   "TESTDIV0" => "1",   "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["KIKOKU2"]  = array("CD" => "11", "NAME" => "海外帰国生入試　第２回",               "TESTDIV" => "4",   "TESTDIV0" => "2",   "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["KIKOKU3"]  = array("CD" => "12", "NAME" => "海外帰国生入試　第３回",               "TESTDIV" => "5",   "TESTDIV0" => "1",   "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["KIKOKU4"]  = array("CD" => "14", "NAME" => "海外帰国生入試　第４回",               "TESTDIV" => "5",   "TESTDIV0" => "2",   "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["A_SUISEN"] = array("CD" => "20", "NAME" => "【A推薦・推薦入試（本校第１希望）】",  "TESTDIV" => "1",   "TESTDIV0" => "1",   "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["B_SUISEN"] = array("CD" => "30", "NAME" => "【B推薦入試（隣接県対応）】",          "TESTDIV" => "2",   "TESTDIV0" => "",    "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["IPPAN"]    = array("CD" => "40", "NAME" => "【一般入試（併願優遇含む）】",         "TESTDIV" => "3",   "TESTDIV0" => "",    "EXAMCOURSECD" => "");
        $this->webTestdiv["2"]["IPPAN_T"]  = array("CD" => "50", "NAME" => "【特別入試】",                        "TESTDIV" => "7",   "TESTDIV0" => "",    "EXAMCOURSECD" => "");

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl016fQuery::updateQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数   ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $db = Query::dbCheckOut();

            $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
            $checkdata["TESTDIV"]               = array(2,    "HISSU",  "INT",   "入試区分");
            if ($this->field["APPLICANTDIV"] == "1") {
                $checkdata["RECEPTNO"]              = array(4,    "HISSU",  "INT",   "受験番号");
                $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT",   "志願者SEQ");
            }
            if ($this->field["APPLICANTDIV"] == "2") {
                $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT",   "受験番号");
            }
            $checkdata["NAME_SEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字姓）");
            $checkdata["NAME_MEI"]              = array(60,   "HISSU",  "",      "志願者氏名（漢字名）");
            $checkdata["NAME_KANA_SEI"]         = array(120,  "HISSU",  "",      "志願者氏名（カナ姓）");
            $checkdata["NAME_KANA_MEI"]         = array(120,  "HISSU",  "",      "志願者氏名（カナ名）");
            $checkdata["BIRTHDAY"]              = array(8,    "",       "DATE",  "生年月日");
            $checkdata["ZIPCD"]                 = array(7,    "",       "INT",   "郵便番号");
            $checkdata["PREF_NAME"]             = array(30,   "",       "",      "都道府県");
            $checkdata["CITY_NAME"]             = array(90,   "",       "",      "市区町村");
            $checkdata["BANCHI_NAME"]           = array(90,   "",       "",      "町名・番地");
            $checkdata["ADDRESS2"]              = array(90,   "",       "",      "建物・部屋番号");
            $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
            $checkdata["MIRAI_FS_CD"]           = array(10,   "",       "INT2",  "出身学校コード");
            if ($this->field["APPLICANTDIV"] == "2") {
                $checkdata["SH_JUDGEMENT_DATE"] = array(10,   "",       "DATE",  "併願校合格発表日");
            }
            $checkdata["GNAME_SEI"]             = array(60,   "",       "",      "保護者氏名（漢字姓）");
            $checkdata["GNAME_MEI"]             = array(60,   "",       "",      "保護者氏名（漢字名）");
            $checkdata["GKANA_SEI"]             = array(120,  "",       "",      "保護者氏名（カナ姓）");
            $checkdata["GKANA_MEI"]             = array(120,  "",       "",      "保護者氏名（カナ名）");
            $checkdata["RELATIONSHIP"]          = array(2,    "",       "",      "志願者と保護者との続柄");
            $checkdata["EXAM_PAY_DATE"]         = array(10,   "",       "DATE",  "入金日");
            $checkdata["RECEPTDATE"]            = array(10,   "HISSU",  "DATE",  "申込完了日");

            //チェック用
            $checkdata["NAME"]                  = array(60,   "",       "",      "志願者氏名（漢字姓名）");
            $checkdata["NAME_KANA"]             = array(120,  "",       "",      "志願者氏名（カナ姓名）");
            $checkdata["ADDRESS1"]              = array(90,   "",       "",      "志願者住所（都道府県、市区町村、町名・番地）");
            $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名（漢字姓名）");
            $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者氏名（カナ姓名）");

            //賢者コードに変換（試験名から固定文字で変換）
            $checkRepdata = array();
            $checkRepdata["1"]["TESTDIV"]       = "入試区分";
            $checkRepdata["1"]["EXAM_TYPE"]     = "受験型";
            $checkRepdata["2"]["TESTDIV"]       = "入試区分";
            $checkRepdata["2"]["TESTDIV0"]      = "回数";
            $checkRepdata["2"]["EXAMCOURSECD"]  = "志望区分";

            //賢者コードに変換（続柄）
            $repDataH201 = array();
            $resultH201 = $db->query(knjl016fQuery::getNameMstH201($this));
            while ($row = $resultH201->fetchRow(DB_FETCHMODE_ASSOC)) {
                $repDataH201[$row["NAME1"]] = $row["NAMECD2"];
            }
            $resultH201->free();

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl016fQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
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

                    $data = array();

                    //チェック用 CSV取込項目
                    //中学
                    if ($this->field["APPLICANTDIV"] == "1") {
                        //01:中学海外帰国第1回
                        //11:中学海外帰国第2回
                        //12:中学海外帰国第3回
                        //20:中学一般
                        //30:中学一般（得意型２科入試）
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["WEB_TESTDIV"]            = substr($tmp[1], 0, 2); //CSVデータ例「01:入試区分名」
                        $data["EXAM_NAME"]              = $tmp[3];
                        $data["RECEPTNO"]               = $tmp[5];
                        $data["EXAMNO"]                 = substr($tmp[11], -5); //CSVデータ例「XXXXX20001」志願者SEQ(10桁)の下5桁を取込
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
                        $data["MIRAI_FS_CD"]            = $tmp[24];
                        $data["MIRAI_FS_NAME"]          = $tmp[25];
                        $data["EIKEN"]                  = $tmp[38];
                        $data["GNAME_SEI"]              = $tmp[39];
                        $data["GNAME_MEI"]              = $tmp[40];
                        $data["GKANA_SEI"]              = $tmp[41];
                        $data["GKANA_MEI"]              = $tmp[42];
                        $data["RELATIONSHIP"]           = $tmp[43];
                        $data["GENMEN_RIYOU"]           = $tmp[46];
                        $data["SISTER_NAME"]            = $tmp[47];
                        $data["EXAM_PAY_DATE"]          = substr($tmp[58], 0, 10); //CSVデータ例「2017/9/26  14:35:38」
                        $data["RECEPTDATE"]             = substr($tmp[61], 0, 10); //CSVデータ例「2017/9/26  14:35:38」

                        $dataCnt = 64;
                    }//中
                    //高校
                    if ($this->field["APPLICANTDIV"] == "2") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["WEB_TESTDIV"]            = substr($tmp[1], 0, 2); //CSVデータ例「01:入試区分名」
                        $data["EXAM_NAME"]              = $tmp[3];
                        $data["EXAMNO"]                 = $tmp[5];
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
                        $data["MIRAI_FS_CD"]            = $tmp[24];
                        $data["MIRAI_FS_NAME"]          = $tmp[25];
                        if ($this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["A_SUISEN"]["CD"]) {
                            //高校Ａ推薦
                            $data["SH_SCHOOLNAME"]          = "";
                            $data["SHIFT_DESIRE_FLG"]       = "";
                            $data["SH_JUDGEMENT_DATE"]      = "";
                            $data["SLIDE_FLG"]              = $tmp[33];
                            $data["GNAME_SEI"]              = $tmp[34];
                            $data["GNAME_MEI"]              = $tmp[35];
                            $data["GKANA_SEI"]              = $tmp[36];
                            $data["GKANA_MEI"]              = $tmp[37];
                            $data["RELATIONSHIP"]           = $tmp[38];
                            $data["GENMEN_RIYOU"]           = $tmp[41];
                            $data["SISTER_NAME"]            = $tmp[42];
                            $data["EXAM_PAY_DATE"]          = substr($tmp[53], 0, 10); //CSVデータ例「2017/9/26  14:35:38」
                            $data["RECEPTDATE"]             = substr($tmp[56], 0, 10); //CSVデータ例「2017/9/26  14:35:38」

                            $dataCnt = 59;
                        } elseif ($this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["B_SUISEN"]["CD"] ||
                                  $this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["IPPAN"]["CD"] ||
                                  $this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["IPPAN_T"]["CD"]) {
                            //高校Ｂ推薦
                            //高校一般
                            $data["SH_SCHOOLNAME"]          = $tmp[30] ? $tmp[30] : $tmp[29];
                            $data["SHIFT_DESIRE_FLG"]       = $tmp[34];
                            $data["SH_JUDGEMENT_DATE"]      = $tmp[35];
                            $data["SLIDE_FLG"]              = $tmp[38];
                            $data["GNAME_SEI"]              = $tmp[39];
                            $data["GNAME_MEI"]              = $tmp[40];
                            $data["GKANA_SEI"]              = $tmp[41];
                            $data["GKANA_MEI"]              = $tmp[42];
                            $data["RELATIONSHIP"]           = $tmp[43];
                            $data["GENMEN_RIYOU"]           = $tmp[46];
                            $data["SISTER_NAME"]            = $tmp[47];
                            $data["EXAM_PAY_DATE"]          = substr($tmp[58], 0, 10); //CSVデータ例「2017/9/26  14:35:38」
                            $data["RECEPTDATE"]             = substr($tmp[61], 0, 10); //CSVデータ例「2017/9/26  14:35:38」

                            $dataCnt = 64;
                        } else {
                            //高校海外帰国第1回
                            //高校海外帰国第2回
                            //高校海外帰国第3回
                            //高校海外帰国第4回
                            $data["SH_SCHOOLNAME"]          = $tmp[29];
                            $data["SHIFT_DESIRE_FLG"]       = "";
                            $data["SH_JUDGEMENT_DATE"]      = "";
                            $data["SLIDE_FLG"]              = "";
                            $data["GNAME_SEI"]              = $tmp[40];
                            $data["GNAME_MEI"]              = $tmp[41];
                            $data["GKANA_SEI"]              = $tmp[42];
                            $data["GKANA_MEI"]              = $tmp[43];
                            $data["RELATIONSHIP"]           = $tmp[44];
                            $data["GENMEN_RIYOU"]           = $tmp[47];
                            $data["SISTER_NAME"]            = $tmp[48];
                            $data["EXAM_PAY_DATE"]          = substr($tmp[59], 0, 10); //CSVデータ例「2017/9/26  14:35:38」
                            $data["RECEPTDATE"]             = substr($tmp[62], 0, 10); //CSVデータ例「2017/9/26  14:35:38」

                            $dataCnt = 65;
                        }
                    }//高

                    //チェック用
                    $data["NAME"]                   = $data["NAME_SEI"]."　".$data["NAME_MEI"];
                    $data["NAME_KANA"]              = $data["NAME_KANA_SEI"]."　".$data["NAME_KANA_MEI"];
                    $data["ADDRESS1"]               = $data["PREF_NAME"].$data["CITY_NAME"].$data["BANCHI_NAME"];
                    $data["GNAME"]                  = $data["GNAME_SEI"]."　".$data["GNAME_MEI"];
                    $data["GKANA"]                  = $data["GKANA_SEI"]."　".$data["GKANA_MEI"];

                    //卒業年月
                    $data["FS_DAY"]                 = $this->ObjYear."-03-01";

                    //入試制度は画面で指定する
                    $data["APPLICANTDIV"]           = $this->field["APPLICANTDIV"];

                    //賢者コードに変換（試験名から固定文字で変換）
                    if ($this->field["APPLICANTDIV"] == "1") {
                        $data["TEST_L_DIV"]             = ($this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["IPPAN"]["CD"] || $this->field["WEB_TESTDIV"] == $this->webTestdiv[$this->field["APPLICANTDIV"]]["IPPAN2"]["CD"]) ? "2" : "1"; //1:帰国生入試、2:一般入試
                    }
                    foreach ($checkRepdata[$this->field["APPLICANTDIV"]] as $field => $label) {
                        foreach ($this->webTestdiv[$this->field["APPLICANTDIV"]] as $keyDiv => $valArray) {
                            if ($this->field["WEB_TESTDIV"] == $valArray["CD"]) {
                                if (strlen($valArray[$field])) {
                                    $data[$field]                   = $valArray[$field];
                                } else {
                                    $data[$field]                   = $this->getRepDataJH($field, $data["EXAM_NAME"]);
                                }
                            }
                        }
                    }

                    //賢者コードに変換（固定文字で変換）
                    $data["RELATIONSHIP"]           = $repDataH201[$data["RELATIONSHIP"]];

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        /***
                                                foreach ($tmp as $keyTmp => $valTmp) {
                                                    $checkTmp = "key = ".$keyTmp." val = ".$valTmp;
                        echo $checkTmp."<BR>";
                                                }
                        ***/
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。"."checkcount = ".$checkcount." dataCnt = ".$dataCnt;
                        return;
                    }
                    //入試区分によるファイルチェック
                    if ($this->field["WEB_TESTDIV"] != $data["WEB_TESTDIV"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n入試区分が違うレコードが含まれています。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $checkRepdata)) {
                        foreach ($checkdata as $key => $val) {
                            //郵便番号を変換してセット
                            if ($key == "ZIPCD" && $data[$key] != "") {
                                $data[$key] = substr($data[$key], 0, 3)."-".substr($data[$key], 3);
                            }
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                $data[$key] = $this->dateRep($data[$key]);
                            }
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
                        }
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
    public function errCheck($db, $data, $checkdata, $line, $checkRepdata)
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
            if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->dateRep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        //変換チェック
        $msg = "";
        foreach ($checkRepdata[$this->field["APPLICANTDIV"]] as $field => $label) {
            if (!strlen(trim($data[$field]))) {
                $msg .= "(".$label.")";
            }
        }
        if (strlen($msg)) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※変換:", $existenceSep, $msg);
        }

        //受験番号範囲チェック
        if (($this->field["APPLICANTDIV"] == "1" && strlen($data["RECEPTNO"])) || ($this->field["APPLICANTDIV"] == "2" && strlen($data["EXAMNO"]))) {
            if (0 == $db->getOne(knjl016fQuery::getEntexamNoDatCnt($this, $data))) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl016fQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            //文字列の中に"/","-"が含まれていない場合
            if (strpos(str_replace("/", "-", $data), "-") === false) {
                $data = substr($data, 0, 4)."-".substr($data, 4, 2)."-".substr($data, 6, 2);
            }
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**********************/
    /*  賢者コードに変換  */
    /**********************/
    public function getRepDataJH($field, $str)
    {
        //配列(変換用)
        $repData = array();

        if ($this->field["APPLICANTDIV"] == "1") {
            $kikokuArray = array();
            for ($i = 1; $i <= 3; $i++) {
                $kikokuArray[] = $this->webTestdiv[$this->field["APPLICANTDIV"]]["KIKOKU".$i]["CD"];
            }
            //海外帰国生入試
            if (in_array($this->field["WEB_TESTDIV"], $kikokuArray)) {
                //入試区分
                $repData["TESTDIV"][] = array("CD" => "6", "WORD" => "第１回",      "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "7", "WORD" => "第２回",      "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "15", "WORD" => "第３回",     "WORD2" => "");
                //受験型
                $repData["EXAM_TYPE"][] = array("CD" => "4", "WORD" => "A方式",     "WORD2" => "");
                $repData["EXAM_TYPE"][] = array("CD" => "5", "WORD" => "B方式",     "WORD2" => "");
            } else {
                //入試区分
                $repData["TESTDIV"][] = array("CD" => "1", "WORD"  => "ポテンシャル１",            "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "2", "WORD"  => "ポテンシャル３",            "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "3", "WORD"  => "ポテンシャル４",            "WORD2" => "");
                // $repData["TESTDIV"][] = array("CD" => "4", "WORD" => "第４回",      "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "5", "WORD"  => "特待チャレンジ入試",        "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "9", "WORD"  => "思考力プレゼンテーションA",  "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "10", "WORD" => "思考力プレゼンテーションB",  "WORD2" => "");
                // $repData["TESTDIV"][] = array("CD" => "11", "WORD" => "思考力入試　C日程",    "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "12", "WORD" => "英語インタラクティブA",      "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "13", "WORD" => "英語インタラクティブB",      "WORD2" => "");
                // $repData["TESTDIV"][] = array("CD" => "14", "WORD" => "Exam　C",    "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "16", "WORD" => "ポテンシャル２",             "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "17", "WORD" => "ポテンシャル後期",           "WORD2" => "");
                $repData["TESTDIV"][] = array("CD" => "18", "WORD" => "適性検査",                   "WORD2" => "");
                //受験型
                $repData["EXAM_TYPE"][] = array("CD" => "2", "WORD" => "２科",        "WORD2" => "選択");
                $repData["EXAM_TYPE"][] = array("CD" => "1", "WORD" => "２科",        "WORD2" => "");
                $repData["EXAM_TYPE"][] = array("CD" => "3", "WORD" => "４科",        "WORD2" => "");
                // $repData["EXAM_TYPE"][] = array("CD" => "8", "WORD" => "スポーツ",    "WORD2" => "");
                $repData["EXAM_TYPE"][] = array("CD" => "7", "WORD" => "思考力プレゼンテーション",  "WORD2" => "");
                // $repData["EXAM_TYPE"][] = array("CD" => "6", "WORD" => "グローバル",  "WORD2" => "");
                $repData["EXAM_TYPE"][] = array("CD" => "9", "WORD" => "インタラクティブ",  "WORD2" => "");
                $repData["EXAM_TYPE"][] = array("CD" => "A", "WORD" => "適性検査",        "WORD2" => "");
            }
        }

        if ($this->field["APPLICANTDIV"] == "2") {
            $kikokuArray = array();
            for ($i = 1; $i <= 4; $i++) {
                $kikokuArray[] = $this->webTestdiv[$this->field["APPLICANTDIV"]]["KIKOKU".$i]["CD"];
            }
            //海外帰国生入試
            if (in_array($this->field["WEB_TESTDIV"], $kikokuArray)) {
                //入試区分
                $repData["TESTDIV"][]   = array("CD" => "4", "WORD" => "A方式",     "WORD2" => "");
                $repData["TESTDIV"][]   = array("CD" => "5", "WORD" => "B方式",     "WORD2" => "");
                //回数
                $repData["TESTDIV0"][]  = array("CD" => "1", "WORD" => "第１回",    "WORD2" => ""); //A方式1回
                $repData["TESTDIV0"][]  = array("CD" => "2", "WORD" => "第２回",    "WORD2" => ""); //A方式2回
                $repData["TESTDIV0"][]  = array("CD" => "1", "WORD" => "第３回",    "WORD2" => ""); //B方式1回
                $repData["TESTDIV0"][]  = array("CD" => "2", "WORD" => "第４回",    "WORD2" => ""); //B方式2回
            } else {
                //入試区分
                $repData["TESTDIV"][]   = array("CD" => "1", "WORD" => "A推薦",     "WORD2" => "");
                $repData["TESTDIV"][]   = array("CD" => "2", "WORD" => "B推薦",     "WORD2" => "");
                $repData["TESTDIV"][]   = array("CD" => "3", "WORD" => "一般",      "WORD2" => "");
                $repData["TESTDIV"][]   = array("CD" => "7", "WORD" => "一般特別",   "WORD2" => "");
                //回数
                $repData["TESTDIV0"][]  = array("CD" => "1", "WORD" => "第１回",    "WORD2" => "A推薦",  "OR" => "OR"); //TODO:A推薦は、試験名に"第１回"の文字がない！
                $repData["TESTDIV0"][]  = array("CD" => "2", "WORD" => "第２回",    "WORD2" => "");
                $repData["TESTDIV0"][]  = array("CD" => "3", "WORD" => "第３回",    "WORD2" => "");
            }
            //志望区分（受験コース）
            $repData["EXAMCOURSECD"][] = array("CD" => "1001", "WORD" => "理数キャリア",  "WORD2" => "Tクラス");
            $repData["EXAMCOURSECD"][] = array("CD" => "1002", "WORD" => "理数キャリア",  "WORD2" => "Aクラス");
            $repData["EXAMCOURSECD"][] = array("CD" => "2001", "WORD" => "国際教養",      "WORD2" => "Tクラス");
            $repData["EXAMCOURSECD"][] = array("CD" => "2002", "WORD" => "国際教養",      "WORD2" => "Aクラス");
            $repData["EXAMCOURSECD"][] = array("CD" => "3001", "WORD" => "スポーツ科学",  "WORD2" => "");
        }

        foreach ($repData[$field] as $key => $val) {
            //文字列の中に含まれている場合
            if (strlen($val["WORD2"]) && $val["OR"] == "OR") {
                if (strpos($str, $val["WORD"]) !== false || strpos($str, $val["WORD2"]) !== false) {
                    return $val["CD"];
                }
            } elseif (strlen($val["WORD2"])) {
                if (strpos($str, $val["WORD"]) !== false && strpos($str, $val["WORD2"]) !== false) {
                    return $val["CD"];
                }
            } else {
                if (strpos($str, $val["WORD"]) !== false) {
                    return $val["CD"];
                }
            }
        }

        //文字列の中に含まれていない場合
        return "";
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
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function isDataMatched(&$data_arr)
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
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        // エラー出力
        $contents .= implode($header_err, ",")."\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        //エラー
        $query = knjl016fQuery::selectCsvErrQuery();
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
