<?php
require_once('for_php7.php');
class knjl411mModel extends Model
{
    public $schoolKind;
    public $cmd;
    public $field = array();
    public $file;

    const EXAM_NYUSHI_KIKOKUSEI = "1";    // 帰国生入試
    const EXAM_NYUSHI_IPPAN = "2";        // 一般入試

    /**
     * メンバ変数の初期化
     */
    public function knjl411mModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            //$this->schoolKind = VARS::get("URL_SCHOOLKIND");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");
        $this->examyear = CTRL_YEAR + 1;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::post("OUTPUT")) {
            $this->field["OUTPUT"] = VARS::post("OUTPUT");
        }
        if (VARS::post("EXAM")) {
            $this->field["EXAM"] = VARS::post("EXAM");
        }
        $this->field["HEADER"] = VARS::post("HEADER");

        $this->file = VARS::file("FILE");

    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    /**
     * CSV取込処理
    */
    public function &getExecModel()
    {

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->data_arr === null) {
            $this->setMessage(MSG202, "CSVのフォーマットが違う可能性があります。");
        } elseif ($this->isDataMatched($this->data_arr)) {
            // 更新
            $cnt = knjl411mQuery::updateQueryCsv($this, $this->data_arr);

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne(knjl411mQuery::getCsvMsgPrgDatCnt());
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }
    /**
     * CSV出力処理
    */
    public function &getDownloadModel()
    {
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダー出力
            return $this->getDownloadCsvHeader();
        }
        if ($this->field["OUTPUT"] == "3") {
            //エラー出力
            return $this->getErrorCsvDownloadModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェックは、DB登録時に必須の物以外は必須チェックしない。
            $checkdata = $this->getCheckData();
    
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            //エラーデータの削除
            $errcnt = $db->getOne(knjl411mQuery::getCsvMsgPrgDatCnt());
            if ($errcnt > 0) {
                $query = knjl411mQuery::deleteCsvMsgPrgDat();
                $db->query($query);
            }

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $hasHeader = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 8192);
                if (trim($buffer) != '') {
                    if (!$hasHeader && $this->field["HEADER"] == "on") {
                        $hasHeader = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    if ($this->field["EXAM"] == self::EXAM_NYUSHI_KIKOKUSEI) {
                        if (get_count($tmp) != 59) {  //帰国生入試
                            Query::dbCheckIn($db);
                            fclose($fp);
                            return null;
                        }
                    } else {
                        if (get_count($tmp) != 57) {  //一般
                            Query::dbCheckIn($db);
                            fclose($fp);
                            return null;
                        }
                    }

                    //CSV取込
                    $tmpNo = 0;
                    $data = array();
                    foreach ($this->header as $key => $val) {
                        $data[$key] = $tmp[$tmpNo];
                        $tmpNo++;
                    }
                    $data["EXAM_SCHOOL_KIND"] = "J";  //校種はJ固定
                    $data["SEX"] = "2";  //校種は2固定

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        /*データ前処理*/
                        $this->preprocess($db, $data);
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

    //郵便番号整形
    public function getFormattedZipCd($noFormattedZipCd)
    {
        return substr($noFormattedZipCd, 0, 3)."-".substr($noFormattedZipCd, -4, 4);
    }

    /*データ前処理*/
    public function preprocess($db, &$data)
    {
    
        //試験名変換
        $query = knjl411mQuery::getTestName($data);
        $tInf = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data["APPLICANT_DIV"] = $tInf["APPLICANT_DIV"];
        $data["COURSE_DIV"] = $tInf["COURSE_DIV"];
        $data["FREQUENCY"] = $tInf["FREQUENCY"];

        //続柄変換
        $query = knjl411mQuery::getRelationName($data);
        $data["RELATION"] = $db->getOne($query);

        //名前連結
        $data["NAME"]               = trim($data["NAME_SEI"])."　".trim($data["NAME_MEI"]);
        $data["NAME_KANA"]          = trim($data["KANA_SEI"])."　".trim($data["KANA_MEI"]);
        $data["GUARD_NAME"]         = trim($data["GUARD_NAME_SEI"])."　".trim($data["GUARD_NAME_MEI"]);
        $data["GUARD_NAME_KANA"]    = trim($data["GUARD_KANA_SEI"])."　".trim($data["GUARD_KANA_MEI"]);

        //誕生日変換
        if ($data["BIRTHDAY"] != "") {
            $data["BIRTHDAY"] = str_replace("/", "-", $data["BIRTHDAY"]);
        }

        //郵便番号整形
        $data["ZIPCD"]  = (trim($data["ZIPCD"]) != "" && strlen($data["ZIPCD"]) >= 7) ? $this->getFormattedZipCd($data["ZIPCD"]) : "";

        // 住所１
        $data["ADDR1"] = trim($data["ADDR1_1"]) . trim($data["ADDR1_2"]) . trim($data["ADDR1_3"]);
        // 住所２
        $data["ADDR2"] = trim($data["ADDR2_1"]);

        // メールアドレス
        $data["EMAIL"] = trim($data["MAILADDR"]);
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $orderErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $orderErrMsg     = "";       //その他エラーメッセージ
        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $existSep        = "";
        $orderSep        = "";

        foreach ($data_each as $key => $val) {
            list($div, $size, $label) = $val;

            //必須項目チェック
            if ($div == "HISSU") {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }
            }
            //ログイン年度+1 = 取込データ年度?
            if ($key == "YEAR" && $data[$key] != "") {
                if ($data[$key] != (CTRL_YEAR + 1)) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $label);
                }
            }
            //日付チェック
            if ($key == "BIRTHDAY" && $data[$key] != "") {
                if (!preg_match("/([0-9]{4})[-\/]([0-9]{1,2})[-\/]([0-9]{1,2})/", $data[$key])) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $label);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $size) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
            }
            //郵便番号チェック
            if ($key == "ZIPCD" && strlen($data[$key]) < 7) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
            }

            //入試年度チェック
            if (in_array($key, array("ENTEXAMYEAR")) && $data[$key] != "") {
                if ((CTRL_YEAR + 1) != $data[$key]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $label);
                }
            }

            //試験名存在チェック
            if (in_array($key, array("TESTNAME")) && $data[$key] != "") {
                $query = knjl411mQuery::getTestName($data, "cnt");
                $cnt = $db->getOne($query);
                if ($cnt == 0) {
                    $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, $label);
                }
            }
            //続柄存在チェック
            if (in_array($key, array("RELATIONNAME")) && $data[$key] != "") {
                $query = knjl411mQuery::getRelationName($data, "cnt");
                $cnt = $db->getOne($query);
                if ($cnt == 0) {
                    $returnFlg = $this->errSet($existErrFlg, $existErrMsg, "※存在:", $existSep, $label);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existErrMsg.$orderErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
            knjl411mQuery::insertCsvMsgPrgDat($db, $line, $check_error);
        }
        return $returnFlg;
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

    /*================================== エラーデータ処理 ==================================*/
    //エラー出力
    public function getErrorCsvDownloadModel()
    {
        //項目
        $header = array(
              "MSGROW"    => "行番号"
            , "MSGREMARK" => "メッセージ"
        );

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = knjl411mQuery::selectCsvMsgPrgDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            // $row["DUMMY"] = "DUMMY";
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($isData) {
            $fileName = "エラー出力.csv";
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    /*================================== エラーデータ処理 ==================================*/

    /**
     * ヘッダ出力
     */
    public function getDownloadCsvHeader()
    {
        // CSVヘッダ出力する種類
        $kind = ($this->field["EXAM"] == self::EXAM_NYUSHI_KIKOKUSEI) ? '帰国生入試' : '一般入試';
        // ヘッダ出力
        $contents = implode($this->header, ',')."\n";
        // 項目説明行出力
        $checkdata = $this->getCheckData();
        $sep = '';
        foreach ($this->header as $field => $label) {
            $tmp = ($checkdata[$field][0] == 'HISSU') ? '必須項目' : ''; 
            $contents .= $sep.$tmp;
            $sep = ',';
        }
        $contents .= "\n";

        //ファイル名
        $fname = (CTRL_YEAR+1).'年度_'. $kind .'.csv';
        common::downloadContents($contents, $fname);
        return true;
    }

    /**
     * 項目チェック用の配列を返却する
     */
    private function getCheckData()
    {
        $checkdata = [];

        if ($this->field["EXAM"] == self::EXAM_NYUSHI_KIKOKUSEI) {
            $checkdata["ENTEXAMYEAR"]    = array("HISSU", 4, "年度");
            $checkdata["TESTNAME"]       = array("HISSU", 120, "試験名");  //★何桁
            $checkdata["RECEPTNO"]       = array("HISSU", 4, "受験番号_システム");
            $checkdata["MAILADDR"]       = array("", 50, "ログインメールアドレス");
            $checkdata["EXAMNO"]         = array("HISSU", 10, "志願者SEQ");
            $checkdata["NAME_SEI"]       = array("HISSU", 60, "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]       = array("HISSU", 60, "志願者氏名（漢字）（名）");
            $checkdata["KANA_SEI"]       = array("HISSU", 120, "志願者氏名（カナ）（姓）");
            $checkdata["KANA_MEI"]       = array("HISSU", 120, "志願者氏名（カナ）（名）");
            $checkdata["BIRTHDAY"]       = array("", 10, "生年月日");  //('/'=>'-'変換は必要)
            $checkdata["ZIPCD"]          = array("", 8, "郵便番号");
            $checkdata["ADDR1_1"]        = array("", 50, "都道府県");
            $checkdata["ADDR1_2"]        = array("", 50, "市区町村");
            $checkdata["ADDR1_3"]        = array("", 50, "町名・番地");
            $checkdata["ADDR2_1"]        = array("", 150, "建物名・部屋番号");
            $checkdata["TELNO"]          = array("", 13, "電話番号");  //★('-'有無はチェックしない？)
            $checkdata["FINSCHOOLCD"]    = array("", 12, "小学校コード"); //★存在チェック必須?
            $checkdata["REMARK1"]        = array("", 150, "海外滞在履歴(滞在国名及び滞在期間を入力してください)");
            $checkdata["REMARK2"]        = array("", 150, "上記滞在期間の在籍校（複数選択可）");
            $checkdata["SPECIAL_REMARK"] = array("", 250, "英検資格について");
            $checkdata["GUARD_NAME_SEI"] = array("", 60, "氏名(漢字)（姓）");
            $checkdata["GUARD_NAME_MEI"] = array("", 60, "氏名(漢字)（名）");
            $checkdata["GUARD_KANA_SEI"] = array("", 120, "氏名(カナ)（姓）");
            $checkdata["GUARD_KANA_MEI"] = array("", 120, "氏名(カナ)（名）");
            $checkdata["GUARD_TELNO"]    = array("", 13, "自宅以外の緊急連絡先");  //★('-'有無はチェックしない？)
            $checkdata["RELATIONNAME"]   = array("", 10, "続柄");  //★コード?文字?桁数?
        } else {
            $checkdata["ENTEXAMYEAR"]    = array("HISSU", 4, "年度");
            $checkdata["TESTNAME"]       = array("HISSU", 120, "試験名");
            $checkdata["RECEPTNO"]       = array("HISSU", 4, "受験番号_システム");
            $checkdata["MAILADDR"]       = array("", 50, "ログインメールアドレス");
            $checkdata["EXAMNO"]         = array("HISSU", 8, "志願者SEQ");
            $checkdata["NAME_SEI"]       = array("HISSU", 60, "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]       = array("HISSU", 60, "志願者氏名（漢字）（名）");
            $checkdata["KANA_SEI"]       = array("HISSU", 120, "志願者氏名（カナ）（姓）");
            $checkdata["KANA_MEI"]       = array("HISSU", 120, "志願者氏名（カナ）（名）");
            $checkdata["BIRTHDAY"]       = array("", 10, "生年月日");  //('/'=>'-'変換は必要)
            $checkdata["ZIPCD"]          = array("", 8, "郵便番号");
            $checkdata["ADDR1_1"]        = array("", 50, "都道府県");
            $checkdata["ADDR1_2"]        = array("", 50, "市区町村");
            $checkdata["ADDR1_3"]        = array("", 50, "町名・番地");
            $checkdata["ADDR2_1"]        = array("", 150, "建物名・部屋番号");
            $checkdata["TELNO"]          = array("", 13, "電話番号");     //★('-'有無はチェックしない？)
            $checkdata["FINSCHOOLCD"]    = array("", 12, "小学校コード"); //★存在チェック必須?
            $checkdata["REMARK1"]        = array("", 150, "海外滞在履歴(滞在国名及び滞在期間を入力してください)");
            $checkdata["REMARK2"]        = array("", 150, "上記滞在期間の在籍校（複数選択可）");
            $checkdata["SPECIAL_REMARK"] = array("", 250, "英検資格について");
            $checkdata["GUARD_NAME_SEI"] = array("", 60, "氏名(漢字)（姓）");
            $checkdata["GUARD_NAME_MEI"] = array("", 60, "氏名(漢字)（名）");
            $checkdata["GUARD_KANA_SEI"] = array("", 120, "氏名(カナ)（姓）");
            $checkdata["GUARD_KANA_MEI"] = array("", 120, "氏名(カナ)（名）");
            $checkdata["GUARD_TELNO"]    = array("", 13, "自宅以外の緊急連絡先");  //★('-'有無はチェックしない？)
            $checkdata["RELATIONNAME"]   = array("", 10, "続柄");  //★コード?文字?桁数?
        }

        return $checkdata;
    }

    public function setHeader()
    {
        //CSVヘッダ項目
        $this->header = array();
        if ($this->field["EXAM"] == self::EXAM_NYUSHI_KIKOKUSEI) {
            $this->header["ENTEXAMYEAR"] = "年度";  //必須
            $this->header["2"]           = "入試区分";
            $this->header["3"]           = "試験日";
            $this->header["TESTNAME"]    = "試験名";  //必須
            $this->header["5"]           = "試験名略称";
            $this->header["RECEPTNO"]    = "受験番号_システム";  //必須
            $this->header["7"]           = "受験番号_手動";
            $this->header["8"]           = "出願申込番号";
            $this->header["9"]           = "ログインユーザSEQ";
            $this->header["MAILADDR"]    = "ログインメールアドレス";  //必須
            $this->header["11"]          = "申込時メールアドレス";
            $this->header["EXAMNO"]      = "志願者SEQ";  //必須
            $this->header["NAME_SEI"]    = "志願者氏名（漢字）（姓）";  //必須
            $this->header["NAME_MEI"]    = "志願者氏名（漢字）（名）";  //必須
            $this->header["KANA_SEI"]    = "志願者氏名（カナ）（姓）";  //必須
            $this->header["KANA_MEI"]    = "志願者氏名（カナ）（名）";  //必須
            $this->header["BIRTHDAY"]    = "生年月日";  //必須
            $this->header["ZIPCD"]       = "郵便番号";  //必須
            $this->header["ADDR1_1"]     = "都道府県";  //必須
            $this->header["ADDR1_2"]     = "市区町村";  //必須
            $this->header["ADDR1_3"]     = "町名・番地";  //必須
            $this->header["ADDR2_1"]     = "建物名・部屋番号";  //必須
            $this->header["TELNO"]       = "電話番号";  //必須
            $this->header["FINSCHOOLCD"] = "小学校コード";  //必須
            $this->header["25"] = "小学校名称";
            $this->header["26"] = "小学校名称その他";
            $this->header["27"] = "自宅から本校への通学に要する時間（分）";
            $this->header["28"] = "出欠の記録(５年生)：欠席日数を入力してください";
            $this->header["29"] = "出欠の記録(６年生)：欠席日数(11月末までのもの)";
            $this->header["REMARK1"]  = "海外滞在履歴(滞在国名及び滞在期間を入力してください)";  //必須
            $this->header["REMARK2"]  = "上記滞在期間の在籍校（複数選択可）";  //必須
            $this->header["32"]       = "32";
            $this->header["33"]       = "【算・英選択者※英検準2級以上のみ】英語試験免除";
            $this->header["SPECIAL_REMARK"] = "英検資格について";  //必須
            $this->header["GUARD_NAME_SEI"] = "氏名(漢字)（姓）";  //必須
            $this->header["GUARD_NAME_MEI"] = "氏名(漢字)（名）";  //必須
            $this->header["GUARD_KANA_SEI"] = "氏名(カナ)（姓）";  //必須
            $this->header["GUARD_KANA_MEI"] = "氏名(カナ)（名）";  //必須
            $this->header["GUARD_TELNO"]    = "自宅以外の緊急連絡先";  //必須
            $this->header["RELATIONNAME"]   = "続柄";  //必須
            $this->header["41"] = "縁故者記入欄";
            $this->header["42"] = "(1)卒業生氏名（及び旧姓）・卒業年度・学校（小・中高・短大・大学）・受験生との関係";
            $this->header["43"] = "(2)在校生氏名・在学年・受験生との関係";
            $this->header["44"] = "出願情報状態区分";
            $this->header["45"] = "調査書受領区分";
            $this->header["46"] = "調査書受領メール送信日";
            $this->header["47"] = "窓口受付番号";
            $this->header["48"] = "備考欄";
            $this->header["49"] = "決済金額";
            $this->header["50"] = "受験料";
            $this->header["51"] = "事務手数料";
            $this->header["52"] = "支払種別コード";
            $this->header["53"] = "支払期限日";
            $this->header["54"] = "入金日";
            $this->header["55"] = "入金日_手動";
            $this->header["56"] = "入金情報区分";
            $this->header["57"] = "申込完了日";
            $this->header["58"] = "キャンセルフラグ";
            $this->header["59"] = "キャンセル日";
        } else {
            $this->header["ENTEXAMYEAR"] = "年度";  //必須
            $this->header["2"]           = "入試区分";
            $this->header["3"]           = "試験日";
            $this->header["TESTNAME"]    = "試験名";  //必須
            $this->header["5"]           = "試験名略称";
            $this->header["RECEPTNO"]    = "受験番号_システム";  //必須
            $this->header["7"]           = "受験番号_手動";
            $this->header["8"]           = "出願申込番号";
            $this->header["9"]           = "ログインユーザSEQ";
            $this->header["MAILADDR"]    = "ログインメールアドレス";  //必須
            $this->header["11"]          = "申込時メールアドレス";
            $this->header["EXAMNO"]      = "志願者SEQ";  //必須
            $this->header["NAME_SEI"]    = "志願者氏名（漢字）（姓）";  //必須
            $this->header["NAME_MEI"]    = "志願者氏名（漢字）（名）";  //必須
            $this->header["KANA_SEI"]    = "志願者氏名（カナ）（姓）";  //必須
            $this->header["KANA_MEI"]    = "志願者氏名（カナ）（名）";  //必須
            $this->header["BIRTHDAY"]    = "生年月日";  //必須
            $this->header["ZIPCD"]       = "郵便番号";  //必須
            $this->header["ADDR1_1"]     = "都道府県";  //必須
            $this->header["ADDR1_2"]     = "市区町村";  //必須
            $this->header["ADDR1_3"]     = "町名・番地";  //必須
            $this->header["ADDR2_1"]     = "建物名・部屋番号";  //必須
            $this->header["TELNO"]       = "電話番号";  //必須
            $this->header["FINSCHOOLCD"] = "小学校コード";  //必須
            $this->header["25"] = "小学校名称";
            $this->header["26"] = "小学校名称その他";
            $this->header["27"] = "自宅から本校への通学に要する時間（分）";
            $this->header["28"] = "出欠の記録(５年生)：欠席日数を入力してください";
            $this->header["29"] = "出欠の記録(６年生)：欠席日数(12月末までのもの)";
            $this->header["30"] = "※海外滞在履歴ありの方のみ";
            $this->header["31"] = "海外における履歴（滞在国名及び滞在期間）";
            $this->header["32"] = "上記滞在期間の在籍校（複数選択可）";
            $this->header["GUARD_NAME_SEI"] = "氏名(漢字)（姓）";  //必須
            $this->header["GUARD_NAME_MEI"] = "氏名(漢字)（名）";  //必須
            $this->header["GUARD_KANA_SEI"] = "氏名(カナ)（姓）";  //必須
            $this->header["GUARD_KANA_MEI"] = "氏名(カナ)（名）";  //必須
            $this->header["GUARD_TELNO"]    = "自宅以外の緊急連絡先";  //必須
            $this->header["RELATIONNAME"]   = "続柄";  //必須
            $this->header["39"] = "縁故者記入欄";
            $this->header["40"] = "(1)卒業生氏名（及び旧姓）・卒業年度・学校（小・中高・短大・大学）・受験生との関係";
            $this->header["41"] = "(2)在校生氏名・在学年・受験生との関係";
            $this->header["42"] = "出願情報状態区分";
            $this->header["43"] = "調査書受領区分";
            $this->header["44"] = "調査書受領メール送信日";
            $this->header["45"] = "窓口受付番号";
            $this->header["46"] = "備考欄";
            $this->header["47"] = "決済金額";
            $this->header["48"] = "受験料";
            $this->header["49"] = "事務手数料";
            $this->header["50"] = "支払種別コード";
            $this->header["51"] = "支払期限日";
            $this->header["52"] = "入金日";
            $this->header["53"] = "入金日_手動";
            $this->header["54"] = "入金情報区分";
            $this->header["55"] = "申込完了日";
            $this->header["56"] = "キャンセルフラグ";
            $this->header["57"] = "キャンセル日";
        }
    }

}
