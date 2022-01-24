<?php

require_once('for_php7.php');

class knjl615aModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $year;              //入試年度
    public $semester;          //現在処理学期
    public $examno;            //受験番号
    public $applicantdiv;      //入試制度
    public $sendPrgId;
    public $sendAuth;
    public $auth;

    public function knjl615aModel()
    {
    }

    public function init()
    {
        $this->auth = AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),
                             "OUTPUT"           =>  VARS::post("OUTPUT"),
                             "HEADER"           =>  VARS::post("HEADER")
                             );

        $this->file = VARS::file("FILE");
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $cnt = knjl615aQuery::insertQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $query  = " SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
            $errcnt = $db->getOne($query);
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]    = array(4,   "年度");
            $checkdata["TNAME"]          = array(0,   "試験名");
            $checkdata["EXAMNO"]         = array(4,   "受験番号_システム");
            $checkdata["ADDRESS1"]       = array(300, "住所1");
            $checkdata["ADDRESS2"]       = array(300, "住所2");
            $checkdata["NAME"]           = array(60,  "志願者氏名");
            $checkdata["NAME_KANA"]      = array(120, "志願者カナ");
            $checkdata["FS_CD"]          = array(7,   "出身学校CD");
            $checkdata["FS_NAME"]        = array(45,  "出身学校名");
            $checkdata["GNAME"]          = array(60,  "保護者氏名");
            $checkdata["GKANA"]          = array(120, "保護者カナ");
            $checkdata["TEL"]            = array(14,  "電話番号");
            $checkdata["EMAIL"]          = array(50,  "EMAIL");
            $checkdata["G_TEL"]          = array(14,  "保護者電話番号");
            $checkdata["EMERGENCYCALL"]  = array(150, "緊急連絡先");
            $checkdata["EMERGENCYTELNO"] = array(150, "緊急連絡番号");
            $checkdata["PRICD"]          = array(7,   "塾CD");
            $checkdata["PRINAME"]        = array(150, "塾名称");
            $checkdata["EIKEN"]          = array(2,   "英語検定");
            $checkdata["EXAM_SCHOOL1"]   = array(7,   "受験校第１");
            $checkdata["EXAM_SCHOOL2"]   = array(7,   "受験校第２");
            $checkdata["EXAM_SCHOOL3"]   = array(7,   "受験校第３");
            $checkdata["TESTDIV"]        = array(0,   "入試区分");
            if ($this->field["APPLICANTDIV"] == '1') {
                $this->csvFormat = $this->getCsvFormatJ();
                $checkdata["TNAME_ABBV"] = array(0, "試験名略称");
            } else {
                $this->csvFormat = $this->getCsvFormatH();
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl615aQuery::deleteQueryErr();
            $result = $db->query($query);

            //入試区分
            $namecd1 = ($this->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
            $query = knjl615aQuery::getNameCd($this->year, $namecd1);
            $result = $db->query($query);
            $this->testDivArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->testDivArray[$row["ABBV1"]] = $row["VALUE"];
            }
            $result->free();

            //都道府県
            $query = knjl615aQuery::getPrefMst();
            $result = $db->query($query);
            $this->prefArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->prefArray[$row["PREF_NAME"]] = $row["PREF_CD"];
            }
            $result->free();

            //志望コース
            $query = knjl615aQuery::getMiraiCourseGrp($this);
            $result = $db->query($query);
            $this->miraiCourseArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->miraiCourseArray[$row["TESTDIV"]][$row["MIRAI_COURSECD"]][$row["MIRAI_COURSE_ABBV"]] = $row;
            }
            $result->free();

            //会場名・コード取得
            $query = knjl615aQuery::getNameCd2($this->year);
            $result = $db->query($query);
            $this->hallNameCd = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->hallNameCd[$row["NAMECD2"]] = $row["NAME1"];
            }
            $result->free();

            $fp = fopen($this->file["tmp_name"], "r");
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
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array();
                    foreach ($this->csvFormat as $fieldKey => $fieldVal) {
                        $data[$fieldKey] = $tmp[$fieldVal["CSV_NO"]];
                    }

                    //エラーチェック
                    if ($this->errCheckAndHenkan($db, $data, $checkdata, $line)) {
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
    public function errCheckAndHenkan($db, &$data, $data_each, $line)
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

        //データ変換
        //ENTEXAM_APPLICANTBASE_DAT
        $senbatsuArray = explode(".", $data["SENBATSU"]);
        $data["SENBATSU"] = $senbatsuArray[0];
        $data["NAME"] = $data["SEI"]."　".$data["MEI"];
        $data["NAME_KANA"] = $data["KANA_SEI"]."　".$data["KANA_MEI"];

        $data["SEX"] = $data["SEX"] == '女' ? '2' : '1';
        if (strlen($data["BIRTHDAY"]) == 8) {
            $data["BIRTHDAY"] = substr($data["BIRTHDAY"], 0, 4)."-".substr($data["BIRTHDAY"], 4, 2)."-".substr($data["BIRTHDAY"], 6);
        } else {
            $data["BIRTHDAY"] = '';
        }

        $grdDivArray = explode(".", $data["GRD_DIV"]);
        $data["GRD_DIV"] = $grdDivArray[0];
        if ($this->field["APPLICANTDIV"] == '1') {
            $data["FS_GRDDIV"] = '';
        } else {
            $data["FS_GRDDIV"] = $data["GRD_DIV"] == '01' ? '1' : '2';
        }

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        if (strpos($data["TOKUBETSU"], '01') !== false) {
            $data["TOKUBETSU"] = '1';
        } else {
            $data["TOKUBETSU"] = '';
        }
        if (strpos($data["KIKOKU"], '01') !== false) {
            $data["KIKOKU"] = '1';
        } else {
            $data["KIKOKU"] = '';
        }
        $eikenArray = explode(".", $data["EIKEN"]);
        $data["EIKEN"] = $eikenArray[0];
        if (strpos($data["ENNOU"], '02') !== false) {
            $data["ENNOU"] = '1';
        } else {
            $data["ENNOU"] = '';
        }
        if (strlen($data["PRICD"]) == 10) {
            $data["PRICD"] = sprintf("%07d", substr($data["PRICD"], 4, 6));
        }

        //ENTEXAM_APPLICANTADDR_DAT
        if (strpos($data["RELATIONSHIP"], '01') !== false) {
            $data["RELATIONSHIP"] = '01';
        } elseif (strpos($data["RELATIONSHIP"], '02') !== false) {
            $data["RELATIONSHIP"] = '02';
        } else {
            $data["RELATIONSHIP"] = '11';
        }
        if ($this->prefArray[$data["PREF_NAME"]]) {
            $data["PREF_CD"] = $this->prefArray[$data["PREF_NAME"]];
            $data["ADDRESS1"] = $data["PREF_NAME"]." ".$data["CITY"]." ".$data["TOWN"];
        } else {
            $data["PREF_CD"] = '';
            $data["ADDRESS1"] = $data["CITY"]." ".$data["TOWN"];
        }
        $data["ADDRESS1"] = mb_convert_kana($data["ADDRESS1"], "rn");
        $data["ADDRESS2"] = mb_convert_kana($data["ADDRESS2"], "rn");
        $data["GNAME"] = $data["G_SEI"]."　".$data["G_MEI"];
        $data["GKANA"] = $data["G_KANA_SEI"]."　".$data["G_KANA_MEI"];

        //ENTEXAM_RECEPT_DAT
        $data["TESTDIV"] = '';
        foreach ($this->testDivArray as $abbv1 => $testDiv) {
            if (strpos($data["TNAME"], $abbv1) !== false) {
                $data["TESTDIV"] = $testDiv;
                break;
            }
        }
        $data["RECEPTNO"] = $data["EXAMNO"].$data["TESTDIV"];

        //ENTEXAM_RECEPT_DETAIL_DAT
        $hopeArray = explode(".", $data["HOPE"]);
        $hopeCd = $hopeArray[0];
        $tnameAbbv = $this->field["APPLICANTDIV"] == '1' ? $data["TNAME_ABBV"] : "DUMMY";
        $data["HOPE1"] = $this->miraiCourseArray[$data["TESTDIV"]][$hopeCd][$tnameAbbv]["EXAMCOURSECD1"];
        $data["HOPE2"] = $this->miraiCourseArray[$data["TESTDIV"]][$hopeCd][$tnameAbbv]["EXAMCOURSECD2"];

        //試験名略称から対応する会場コードを取得(※現状は中学のみ対応)
        if ($this->field["APPLICANTDIV"] == '1') {
            foreach ($this->hallNameCd as $namecd2 => $name1) {
                if (mb_strpos($data["TNAME_ABBV"], $name1) !== false) {
                    $data["HALL_CD"] = $namecd2;
                    break;
                }
            }
        }

        $tsuugakuArray = explode("：", $data["TSUUGAKU"]);
        if (in_array($tsuugakuArray[0], array("01", "03", "05", "07"))) {
            $data["TSUUGAKU"] = '1';
        } elseif (in_array($tsuugakuArray[0], array("02", "04", "06", "08"))) {
            $data["TSUUGAKU"] = '2';
        } else {
            $data["TSUUGAKU"] = '';
        }

        //エラーチェック
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("ENTEXAMYEAR", "TNAME", "TNAME_ABBV", "EXAMNO", "TESTDIV"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //文字数チェック
            if ($val[0] > 0) {
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //年度チェック
            if (in_array($key, array("ENTEXAMYEAR"))) {
                if ($data[$key] != $this->year) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※年度外", $hissuSep, "");
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl615aQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //中学CSVフォーマット
    public function getCsvFormatJ()
    {
        $csvInfo = array();
        $csvInfo["ENTEXAMYEAR"]       = array("CSV_NO" =>  "0", "TITLE" => "年度");
        $csvInfo["TNAME"]             = array("CSV_NO" =>  "3", "TITLE" => "試験名");
        $csvInfo["TNAME_ABBV"]        = array("CSV_NO" =>  "4", "TITLE" => "試験名略称");
        $csvInfo["EXAMNO"]            = array("CSV_NO" =>  "5", "TITLE" => "受験番号_システム");
        $csvInfo["EMAIL"]             = array("CSV_NO" => "10", "TITLE" => "申込時メールアドレス");
        $csvInfo["SEI"]               = array("CSV_NO" => "12", "TITLE" => "志願者氏名（漢字）（姓）");
        $csvInfo["MEI"]               = array("CSV_NO" => "13", "TITLE" => "志願者氏名（漢字）（名）");
        $csvInfo["KANA_SEI"]          = array("CSV_NO" => "14", "TITLE" => "志願者氏名（カナ）（姓）");
        $csvInfo["KANA_MEI"]          = array("CSV_NO" => "15", "TITLE" => "志願者氏名（カナ）（名）");
        $csvInfo["SEX"]               = array("CSV_NO" => "17", "TITLE" => "性別");
        $csvInfo["BIRTHDAY"]          = array("CSV_NO" => "18", "TITLE" => "生年月日");
        $csvInfo["ZIPCD"]             = array("CSV_NO" => "19", "TITLE" => "郵便番号");
        $csvInfo["PREF_NAME"]         = array("CSV_NO" => "20", "TITLE" => "都道府県");
        $csvInfo["CITY"]              = array("CSV_NO" => "21", "TITLE" => "市区町村");
        $csvInfo["TOWN"]              = array("CSV_NO" => "22", "TITLE" => "町名・番地");
        $csvInfo["ADDRESS2"]          = array("CSV_NO" => "23", "TITLE" => "建物名・部屋番号");
        $csvInfo["TEL"]               = array("CSV_NO" => "24", "TITLE" => "電話番号");
        $csvInfo["FS_CD"]             = array("CSV_NO" => "25", "TITLE" => "出身小学校名コード");
        $csvInfo["FS_NAME"]           = array("CSV_NO" => "26", "TITLE" => "出身小学校名名称");
        $csvInfo["EXAM_SCHOOL1"]      = array("CSV_NO" => "28", "TITLE" => "受験校【第１志望】");
        $csvInfo["EXAM_SCHOOL2"]      = array("CSV_NO" => "31", "TITLE" => "受験校【第２志望】");
        $csvInfo["EXAM_SCHOOL3"]      = array("CSV_NO" => "34", "TITLE" => "受験校【第３志望】");
        $csvInfo["PRICD"]             = array("CSV_NO" => "37", "TITLE" => "塾（通塾していた方）コード");
        $csvInfo["PRINAME"]           = array("CSV_NO" => "38", "TITLE" => "塾（通塾していた方）名称");
        $csvInfo["KIKOKU"]            = array("CSV_NO" => "41", "TITLE" => "【帰国生入試に対する優遇】");
        $csvInfo["HOPE"]              = array("CSV_NO" => "46", "TITLE" => "志望コース、コース第２志望");
        $csvInfo["EIKEN"]             = array("CSV_NO" => "51", "TITLE" => "実用英語検定の取得級");
        $csvInfo["G_SEI"]             = array("CSV_NO" => "52", "TITLE" => "保護者氏名（漢字）（姓）");
        $csvInfo["G_MEI"]             = array("CSV_NO" => "53", "TITLE" => "保護者氏名（漢字）（名）");
        $csvInfo["G_KANA_SEI"]        = array("CSV_NO" => "54", "TITLE" => "保護者氏名（カナ）（姓）");
        $csvInfo["G_KANA_MEI"]        = array("CSV_NO" => "55", "TITLE" => "保護者氏名（カナ）（名）");
        $csvInfo["G_TEL"]             = array("CSV_NO" => "56", "TITLE" => "電話番号");
        $csvInfo["EMERGENCYTELNO"]    = array("CSV_NO" => "57", "TITLE" => "緊急連絡先");
        $csvInfo["EMERGENCYCALL"]     = array("CSV_NO" => "58", "TITLE" => "連絡先名称");
        $csvInfo["RELATIONSHIP"]      = array("CSV_NO" => "60", "TITLE" => "志願者との続柄");

        return $csvInfo;
    }

    //高校CSVフォーマット
    public function getCsvFormatH()
    {
        $csvInfo = array();
        $csvInfo["ENTEXAMYEAR"]     = array("CSV_NO" =>  "0", "TITLE" => "年度");
        $csvInfo["TSUUGAKU"]        = array("CSV_NO" =>  "1", "TITLE" => "入試区分");
        $csvInfo["TNAME"]           = array("CSV_NO" =>  "3", "TITLE" => "試験名");
        $csvInfo["EXAMNO"]          = array("CSV_NO" =>  "5", "TITLE" => "受験番号_システム");
        $csvInfo["EMAIL"]           = array("CSV_NO" => "10", "TITLE" => "申込時メールアドレス");
        $csvInfo["SEI"]             = array("CSV_NO" => "12", "TITLE" => "志願者氏名（漢字）（姓）");
        $csvInfo["MEI"]             = array("CSV_NO" => "13", "TITLE" => "志願者氏名（漢字）（名）");
        $csvInfo["KANA_SEI"]        = array("CSV_NO" => "14", "TITLE" => "志願者氏名（カナ）（姓）");
        $csvInfo["KANA_MEI"]        = array("CSV_NO" => "15", "TITLE" => "志願者氏名（カナ）（名）");
        $csvInfo["SEX"]             = array("CSV_NO" => "17", "TITLE" => "性別");
        $csvInfo["BIRTHDAY"]        = array("CSV_NO" => "18", "TITLE" => "生年月日");
        $csvInfo["ZIPCD"]           = array("CSV_NO" => "19", "TITLE" => "郵便番号");
        $csvInfo["PREF_NAME"]       = array("CSV_NO" => "20", "TITLE" => "都道府県");
        $csvInfo["CITY"]            = array("CSV_NO" => "21", "TITLE" => "市区町村");
        $csvInfo["TOWN"]            = array("CSV_NO" => "22", "TITLE" => "町名・番地");
        $csvInfo["ADDRESS2"]        = array("CSV_NO" => "23", "TITLE" => "建物名・部屋番号");
        $csvInfo["TEL"]             = array("CSV_NO" => "24", "TITLE" => "電話番号");
        $csvInfo["FS_CD"]           = array("CSV_NO" => "25", "TITLE" => "出身中学校名コード");
        $csvInfo["FS_NAME"]         = array("CSV_NO" => "26", "TITLE" => "出身中学校名名称");
        $csvInfo["EXAM_SCHOOL1"]    = array("CSV_NO" => "28", "TITLE" => "受験校【第１志望】");
        $csvInfo["EXAM_SCHOOL2"]    = array("CSV_NO" => "31", "TITLE" => "受験校【第２志望】");
        $csvInfo["EXAM_SCHOOL3"]    = array("CSV_NO" => "34", "TITLE" => "受験校【第３志望】");
        $csvInfo["PRICD"]           = array("CSV_NO" => "37", "TITLE" => "塾（通塾していた方）コード");
        $csvInfo["PRINAME"]         = array("CSV_NO" => "38", "TITLE" => "塾（通塾していた方）名称");
        $csvInfo["GRD_DIV"]         = array("CSV_NO" => "40", "TITLE" => "中学校卒業区分");
        $csvInfo["SENBATSU"]        = array("CSV_NO" => "41", "TITLE" => "出願する選抜方式");
        $csvInfo["TOKUBETSU"]       = array("CSV_NO" => "43", "TITLE" => "【特別活動に対する優遇】");
        $csvInfo["KIKOKU"]          = array("CSV_NO" => "44", "TITLE" => "【帰国生入試に対する優遇】");
        $csvInfo["EIKEN"]           = array("CSV_NO" => "49", "TITLE" => "実用英語検定の取得級");
        $csvInfo["ENNOU"]           = array("CSV_NO" => "50", "TITLE" => "入学金の延納希望");
        $csvInfo["HOPE"]            = array("CSV_NO" => "51", "TITLE" => "コース志望");
        $csvInfo["G_SEI"]           = array("CSV_NO" => "57", "TITLE" => "保護者氏名（漢字）（姓）");
        $csvInfo["G_MEI"]           = array("CSV_NO" => "58", "TITLE" => "保護者氏名（漢字）（名）");
        $csvInfo["G_KANA_SEI"]      = array("CSV_NO" => "59", "TITLE" => "保護者氏名（カナ）（姓）");
        $csvInfo["G_KANA_MEI"]      = array("CSV_NO" => "60", "TITLE" => "保護者氏名（カナ）（名）");
        $csvInfo["G_TEL"]           = array("CSV_NO" => "61", "TITLE" => "電話番号");
        $csvInfo["EMERGENCYTELNO"]  = array("CSV_NO" => "62", "TITLE" => "緊急連絡先");
        $csvInfo["EMERGENCYCALL"]   = array("CSV_NO" => "63", "TITLE" => "連絡先名称");
        $csvInfo["RELATIONSHIP"]    = array("CSV_NO" => "65", "TITLE" => "続柄");

        return $csvInfo;
    }

    //エラーメッセージをセット
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

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $contents .= implode($header_err, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //エラー
        $query = knjl615aQuery::selectCsvErrQuery();

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->field["OUTPUT"]=="4") { //4:データ出力
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            }
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
//var_dump($_REQUEST);
?>
