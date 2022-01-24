<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje361bModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje361bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "KISOTU"           =>  VARS::post("KISOTU"),            //既卒も含む
                             "SUISEN"           =>  VARS::post("SUISEN"),            //学内推薦
                             "YEAR"             =>  VARS::post("YEAR"),              //年度一覧
                             "SCHOOL_CD"        =>  VARS::post("SCHOOL_CD"),         //大学 指定大学1
                             "FACULTYCD"        =>  VARS::post("FACULTYCD"),         //学部 指定大学1
                             "DEPARTMENTCD"     =>  VARS::post("DEPARTMENTCD"),      //学科 指定大学1
                             "SCHOOL_CD2"       =>  VARS::post("SCHOOL_CD2"),        //大学 指定大学2
                             "FACULTYCD2"       =>  VARS::post("FACULTYCD2"),        //学部 指定大学2
                             "DEPARTMENTCD2"    =>  VARS::post("DEPARTMENTCD2"),     //学科 指定大学2
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knje361bQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knje361bQuery::updateQueryCsv($this, $this->data_arr);
                }

                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"              => array(4,"年度"),
                               "SEQ"               => array(10,"連番"),
                               "SCHREGNO"          => array(8,"学籍番号"),
                               "TOROKU_DATE"       => array(10,"登録日"),
                               "STAT_CD"           => array(8,"大学コード"),
                               "FACULTYCD"         => array(3,"学部コード"),
                               "DEPARTMENTCD"      => array(3,"学科コード"),
                               "REMARK1"           => array(2,"募集区分"),
                               "REMARK2"           => array(2,"日程コード"),
                               "REMARK3"           => array(1,"方式コード"),
                               "REMARK4"           => array(2,"大分類コード"),
                               "REMARK5"           => array(3,"系統コード"),
                               "HOWTOEXAM"         => array(2,"受験方式"),
                               "DECISION1"         => array(1,"出願区分"),
                               "DECISION2"         => array(1,"受験区分"),
                               "DECISION3"         => array(1,"合格区分"),
                               "PLANSTAT"          => array(1,"決定区分"),
                               "REMARK6"           => array(10,"締切日（窓口）"),
                               "REMARK7"           => array(10,"締切日（郵送）"),
                               "REMARK8"           => array(1,"郵送区分"),
                               "STAT_DATE1"        => array(10,"入試日"),
                               "STAT_DATE3"        => array(10,"合格発表日"),
                               "REMARK9"           => array(10,"受験番号"),
                               "CONTENTEXAM"       => array(120,"備考1"),
                               "REASONEXAM"        => array(220,"備考2"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $query = knje361bQuery::isSundaikoufu($this);
            $isSundaikoufu = ($db->getOne($query) > 0);
            
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
                    $tmp    = str_replace("↓", "\n", $tmp);
                    $tmp2 = array();
                    for ($j = 0; $j < get_count($tmp); $j++) {
                        $idx = $j;
                        if ($j >= 10) {
                            if ($isSundaikoufu == "1") {
                                $idx = $j-3;
                            } else {
                                $idx = $j-2;
                            }
                        }
                        $tmp2[$idx] = $tmp[$j];
                    }
                    $tmp3 = array();
                    for ($j = 0; $j < get_count($tmp2); $j++) {
                        $idx = $j;
                        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
                            if ($j >= 10) {
                                $idx = $j-1;
                            }
                        }
                        $tmp3[$idx] = $tmp2[$j];
                    }
                    $data = array("YEAR"            => (strlen(trim($tmp3[0]))) ? sprintf("%04d", $tmp3[0]) : "",
                                  "SEQ"             => $tmp3[1],
                                  "SCHREGNO"        => (strlen(trim($tmp3[5]))) ? sprintf("%08d", $tmp3[5]) : "",
                                  "TOROKU_DATE"     => str_replace("/", "-", $tmp3[8]),
                                  "STAT_CD"         => $tmp3[9],
                                  "FACULTYCD"       => $tmp3[11],
                                  "DEPARTMENTCD"    => $tmp3[13],
                                  "REMARK1"         => (strlen(trim($tmp3[15]))) ? sprintf("%02d", $tmp3[15]) : "",
                                  "REMARK2"         => (strlen(trim($tmp3[17]))) ? sprintf("%02d", $tmp3[17]) : "",
                                  "REMARK3"         => $tmp3[19],
                                  "REMARK4"         => $tmp3[21],
                                  "REMARK5"         => $tmp3[22],
                                  "HOWTOEXAM"       => $tmp3[23],
                                  "DECISION1"       => $tmp3[24],
                                  "DECISION2"       => $tmp3[25],
                                  "DECISION3"       => $tmp3[26],
                                  "PLANSTAT"        => $tmp3[27],
                                  "REMARK6"         => $tmp3[28],
                                  "REMARK7"         => $tmp3[29],
                                  "REMARK8"         => $tmp3[30],
                                  "STAT_DATE1"      => str_replace("/", "-", $tmp3[31]),
                                  "STAT_DATE3"      => str_replace("/", "-", $tmp3[32]),
                                  "REMARK9"         => $tmp3[33],
                                  "CONTENTEXAM"     => $tmp3[34],
                                  "REASONEXAM"      => $tmp3[35]);
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
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;    //日付エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";       //日付エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $dateSep         = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "TOROKU_DATE","STAT_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->field["SHORI_MEI"] == "2") {
                //必須項目チェック
                if (in_array($key, array("SEQ"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    } else {
                        //更新データ有無
                        if (1 > $db->getOne(knje361bQuery::updDataExists($data["YEAR"], $data[$key], $data["SCHREGNO"]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                    }
                }
            } else {
                //連番不要チェック
                if (in_array($key, array("SEQ"))) {
                    if (strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "新規選択時は".$val[1]."は空白");
                    }
                }
            }
            if (in_array($key, array("TOROKU_DATE", "REMARK6", "REMARK7", "STAT_DATE1", "STAT_DATE3"))) {
                if ($data[$key] != "" && $this->dateRep($data[$key]) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付フォーマット(斜線またはハイフン区切り):", $dateSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEQ", "SCHREGNO", "STAT_CD", "FACULTYCD", "DEPARTMENTCD", "REMARK1", "REMARK2", "REMARK3", "REMARK4", "REMARK5", "HOWTOEXAM", "DECISION1", "DECISION2", "DECISION3", "PLANSTAT", "REMARK8"))) {
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
            $checkError = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje361bQuery::insertQueryErr($db, $line, $checkError);
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


    //ＣＳＶ出力
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();
        $query = knje361bQuery::isSundaikoufu($this);
        $isSundaikoufu = ($db->getOne($query) > 0);
        Query::dbCheckIn($db);
    
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $header = array();
        $header["YEAR"]             = "年度";
        $header["SEQ"]              = "連番";
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["NAME"]             = "氏名";
        
        $header['SEX']              = '性別';
        if ($isSundaikoufu) {
            $header['GRD_TERM']     = '駿小出身';
        }
        $header['FINSCHOOL_NAME']   = '出身中学校';
        
        
        $header["GENEKI_KISOTU"]    = "現役/既卒";
        $header["TOROKU_DATE"]      = "※登録日";
        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
            $header["ENT_DIV"]          = "入学区分";
        }
        $header["STAT_CD"]          = "※大学コード";
        $header["STAT_NAME"]        = "大学名";
        $header["FACULTYCD"]        = "学部コード";
        $header["FACULTYNAME"]      = "学部名";
        $header["DEPARTMENTCD"]     = "学科コード";
        $header["DEPARTMENTNAME"]   = "学科名";
        $header["REMARK1"]          = "募集区分";
        $header["REMARK1_NAME"]     = "募集区分名";
        $header["REMARK2"]          = "日程コード";
        $header["REMARK2_NAME"]     = "日程名";
        $header["REMARK3"]          = "方式コード";
        $header["REMARK3_NAME"]     = "方式名";
        $header["REMARK4"]          = "大分類コード";
        $header["REMARK5"]          = "系統コード";
        $header["HOWTOEXAM"]        = "受験方式";
        $header["HOWTOEXAM_NAME"]   = "受験方式名";
        $header["DECISION1"]        = "出願区分";
        $header["DECISION2"]        = "受験区分";
        $header["DECISION3"]        = "合格区分";
        $header["PLANSTAT"]         = "決定区分";
        $header["REMARK6"]          = "締切日（窓口）";
        $header["REMARK7"]          = "締切日（郵送）";
        $header["REMARK8"]          = "郵送区分";
        $header["STAT_DATE1"]       = "入試日";
        $header["STAT_DATE3"]       = "合格発表日";
        $header["REMARK9"]          = "受験番号";
        $header["CONTENTEXAM"]      = "備考1";
        $header["REASONEXAM"]       = "備考2";
        $header[$this->lastColumn]  = $this->lastColumn;

        $header_rei = array();
        $header_rei["YEAR"]             = "年度";
        $header_rei["SEQ"]              = "連番";
        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["NAME"]             = "アルプ太郎";
        
        $header_rei['SEX']              = '男';
        if ($isSundaikoufu) {
            $header_rei['GRD_TERM']     = '〇';
        }
        $header_rei['FINSCHOOL_NAME']   = 'アルプ中学校';
        
        $header_rei["GENEKI_KISOTU"]    = "現役/既卒";
        $header_rei["TOROKU_DATE"]      = str_replace("-", "/", CTRL_DATE);
        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
            $header_rei["ENT_DIV"]          = "Ａ";
        }
        $header_rei["STAT_CD"]          = "12345678";
        $header_rei["STAT_NAME"]        = "大学名";
        $header_rei["FACULTYCD"]        = "999";
        $header_rei["FACULTYNAME"]      = "学部名";
        $header_rei["DEPARTMENTCD"]     = "999";
        $header_rei["DEPARTMENTNAME"]   = "学科名";
        $header_rei["REMARK1"]          = "99";
        $header_rei["REMARK1_NAME"]     = "募集区分名";
        $header_rei["REMARK2"]          = "99";
        $header_rei["REMARK2_NAME"]     = "日程名";
        $header_rei["REMARK3"]          = "1";
        $header_rei["REMARK3_NAME"]     = "方式名";
        $header_rei["REMARK4"]          = "99";
        $header_rei["REMARK5"]          = "999";
        $header_rei["HOWTOEXAM"]        = "99";
        $header_rei["HOWTOEXAM_NAME"]   = "受験方式名";
        $header_rei["DECISION1"]        = "9";
        $header_rei["DECISION2"]        = "9";
        $header_rei["DECISION3"]        = "9";
        $header_rei["PLANSTAT"]         = "9";
        $header_rei["REMARK6"]          = str_replace("-", "/", CTRL_DATE);
        $header_rei["REMARK7"]          = str_replace("-", "/", CTRL_DATE);
        $header_rei["REMARK8"]          = "9";
        $header_rei["STAT_DATE1"]       = str_replace("-", "/", CTRL_DATE);
        $header_rei["STAT_DATE3"]       = str_replace("-", "/", CTRL_DATE);
        $header_rei["REMARK9"]          = "1234567890";
        $header_rei["CONTENTEXAM"]      = "備考1";
        $header_rei["REASONEXAM"]       = "備考2";
        $header_rei[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knje361bQuery::selectMainQuery($this, $isSundaikoufu);
            }
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    if ($row["GENEKI_KISOTU"] == 1) {
                        $row["GENEKI_KISOTU"] = "現役";
                    } elseif ($row["GENEKI_KISOTU"] == 2) {
                        $row["GENEKI_KISOTU"] = "";
                    } else {
                        $row["GENEKI_KISOTU"] = $row["GENEKI_KISOTU"]."年度卒";
                    }
                    if ($row["TOROKU_DATE"]) {
                        $row["TOROKU_DATE"] = str_replace("-", "/", $row["TOROKU_DATE"]);
                    }
                    if ($row["REMARK6"]) {
                        $row["REMARK6"] = str_replace("-", "/", $row["REMARK6"]);
                    }
                    if ($row["REMARK7"]) {
                        $row["REMARK7"] = str_replace("-", "/", $row["REMARK7"]);
                    }
                    if ($row["STAT_DATE1"]) {
                        $row["STAT_DATE1"] = str_replace("-", "/", $row["STAT_DATE1"]);
                    }
                    if ($row["STAT_DATE3"]) {
                        $row["STAT_DATE3"] = str_replace("-", "/", $row["STAT_DATE3"]);
                    }
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "受験報告";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }
}
//var_dump($_REQUEST);
