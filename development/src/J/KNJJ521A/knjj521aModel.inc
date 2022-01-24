<?php
/**
 * 定数定義
*/
class knjj521aModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $schoolkind;
    public $select;
    public $nyuryoku;
    public $type_div;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjj521aModel()
    {
        $this->field = array();
        unset($schoolkind);
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
        }

        $this->field = array(
                            "YEAR"           =>  VARS::post("YEAR"),            //年度
                            "EXECUTE_DATE"   =>  VARS::post("EXECUTE_DATE"),    //作成日
                            "REPORT"         =>  VARS::post("REPORT"),
                            "DOC_NUMBER"     =>  VARS::post("DOC_NUMBER"),      //文書番号
                            "GRADE"          =>  VARS::post("GRADE"),           //学年
                            "COURSE_MAJOR"   =>  VARS::post("COURSE_MAJOR"),    //学科
                            "SEX"            =>  VARS::post("SEX"),             //性別
                            "SOKUTEI_DATE"   =>  VARS::post("SOKUTEI_DATE"),    //測定日付
                            "WEATHER"        =>  VARS::post("WEATHER"),         //天候
                            "TEMPERATURE"    =>  VARS::post("TEMPERATURE"),      //気温
                            "HEADER"         =>  VARS::post("HEADER")           //ヘッダ
                            );

        $this->cmd     = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $db = Query::dbCheckOut();

        //SQL抽出条件における学期を取得
        $this->baseSemester = CTRL_SEMESTER;
        if ($this->field["YEAR"] != CTRL_YEAR) {
            $this->baseSemester = $db->getOne(knjj521aQuery::getLastSemester($this));
        }
        $query = knjj521aQuery::getSchoolKind($this);
        $this->schoolkind = $db->getOne($query);

        Query::dbCheckIn($db);

        //問診項目Max番号
        $this->maxInquiryNum = 6;

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //クライアント ⇒ サーバ側の処理
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            //Insertまたはupdate
            $cnt = knje410Query::insertQueryCsv($db, $this, $this->data_arr);
            $db->commit();

            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID='KNJE521A'");
            Query::dbCheckIn($db);
            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    public function getDownloadCsvModel()
    {
        $db = Query::dbCheckOut();

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
                            
            $contents = implode(",", $header) . "\r\n";
            $query = knjj521aQuery::getErrorCsvData();
        } else {
            //ヘッダ、書出
            $header = array();
            $header["GRADE"]                    = "学年";
            $header["COURSECD"]                 = "課程コード";
            $header["MAJORCD"]                  = "学科コード";
            $header["SEX"]                      = "性別コード";
            $header["ROWNO"]                    = "連番";
            $inquiryCdArray = range(1, $this->maxInquiryNum);
            foreach ($inquiryCdArray as $inquiryCd) {
                $header["INQUIRY{$inquiryCd}"] = "問診項目{$inquiryCd}";
            }
            $item_cnt = 0;
            $item_key = array();
            $query = knjj521aQuery::getSportsItemMst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $item_cnt++;
                if ($item_cnt > 9) {
                    break;
                }
                $item_key[$item_cnt] = $row["ITEMCD"];
                $header["RECORD{$row["ITEMCD"]}"] = "{$row["ITEMABBV"]}({$row["UNIT"]})";
            }
            $header["TOTAL"]                    = "総合計(点)";
            $header["VALUE"]                    = "総合判定";
            $header["HEIGHT"]                   = "身長(cm)";
            $header["WEIGHT"]                   = "体重(kg)";
            $header["SITHEIGHT"]                = "座高(cm)";
            $header[$this->lastColumn]          = $this->lastColumn;
                            
            if ($this->field["HEADER"]) {
                $contents  = implode(",", $header) . "\n";
            } else {
                $contents = "";
            }
            $query = knjj521aQuery::getList($this, $item_key);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //配列の値を,で連結
            $contents .= $this->field["GRADE"];
            $contents .= ",";
            $contents .= substr($this->field["COURSE_MAJOR"], 0, 1);
            $contents .= ",";
            $contents .= substr($this->field["COURSE_MAJOR"], 1, 3);
            $contents .= ",";
            $contents .= $this->field["SEX"];
            $contents .= ",";
            $contents .= implode($row, ",");
            $contents .= ",";
            $contents .= $this->lastColumn."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="新体力テストエラー";
            } else {
                $couse="新体力テスト測定結果一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }



    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //県への報告
    public function &getUpdateEdboardModel()
    {
        //学校側・報告テーブル
        knjj521aQuery::getUpdateReport($this);
        $this->setMessage("MSG201");
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($gyoNo = 0; $gyoNo < count($this->data["ROWNO"]); $gyoNo++) {
            //問診項目
            $inquiryCdArray = range(1, $this->maxInquiryNum);
            foreach ($inquiryCdArray as $inquiryCd) {
                $name = "INQUIRY".$inquiryCd;
                $term_name = $name."-".$gyoNo;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$name][$gyoNo] = $post_term;
                }
            }
            //種目
            foreach ($this->data["RECORD"] as $lenNo => $itemCd) {
                $name = "RECORD".$lenNo;
                $term_name = $name."-".$gyoNo;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$name][$gyoNo] = $post_term;
                }
            }

            //その他
            $nameArray = array("TOTAL", "VALUE", "HEIGHT", "WEIGHT", "SITHEIGHT");
            foreach ($nameArray as $name) {
                $term_name = $name."-".$gyoNo;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$name][$gyoNo] = $post_term;
                }
            }
        }
        //更新
        knjj521aQuery::update($this);
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "main" : "main";
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["GRADE"]     = array(1  ,"学年" );
            $checkdata["COURSECD"]  = array(1  ,"課程コード" );
            $checkdata["MAJORCD"]   = array(1  ,"学科コード" );
            $checkdata["SEX"]       = array(1  ,"性別コード" );
            $checkdata["ROWNO"]     = array(1  ,"連番" );
            $inquiryCdArray = range(1, $this->maxInquiryNum);
            $inquiry_cnt = count($inquiryCdArray);
            foreach ($inquiryCdArray as $inquiryCd) {
                $checkdata["INQUIRY{$inquiryCd}"]  = array(1  ,"問診項目{$inquiryCd}" );
            }
            $item_cnt = 0;
            $query = knjj521aQuery::getSportsItemMst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $item_cnt++;
                if ($item_cnt > 9) {
                    $item_cnt = 9;
                    break;
                }
                $checkdata["RECORD{$row["ITEMCD"]}"]  = array(60  ,"{$row["ITEMABBV"]}" );
            }
            $checkdata["TOTAL"]      = array(2  ,"総合計" );
            $checkdata["VALUE"]      = array(2  ,"総合判定" );
            $checkdata["HEIGHT"]     = array(5  ,"身長" );
            $checkdata["WEIGHT"]     = array(5  ,"体重" );
            $checkdata["SITHEIGHT"]  = array(5  ,"座高" );
    
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
    
            //エラーデータの削除
            $query  = knje410Query::deleteQueryErr();
            $db->query($query);
    
            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        if ($line == 2) {//headerが2行分
                            $header = true;
                        }
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $cnt = 0;
                    $data["GRADE"]     = trim($tmp[$cnt++]);
                    $data["COURSECD"]  = trim($tmp[$cnt++]);
                    $data["MAJORCD"]   = trim($tmp[$cnt++]);
                    $data["SEX"]       = trim($tmp[$cnt++]);
                    $data["ROWNO"]     = trim($tmp[$cnt++]);
                    for ($offset = 1; $offset <= $inquiry_cnt; $offset++) {
                        $data["INQUIRY{$inquiryCd}"]  = trim($tmp[$cnt++]);
                    }
                    for ($offset = 1; $offset <= $item_cnt; $offset++) {
                        $data["REOCRD{$inquiryCd}"]  = trim($tmp[$cnt++]);
                    }
                    $data["TOTAL"]      = trim($tmp[$cnt++]);
                    $data["VALUE"]      = trim($tmp[$cnt++]);
                    $data["HEIGHT"]     = trim($tmp[$cnt++]);
                    $data["WEIGHT"]     = trim($tmp[$cnt++]);
                    $data["SITHEIGHT"]  = trim($tmp[$cnt++]);
                      
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
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
            $numericErrFlg   = false;    //数字エラーフラグ
            $lengthErrFlg    = false;    //レングスエラーフラグ
            $spaceBarErrFlg  = false;    //スペースにつづいて長音（ー）フラグ
            $existErrFlg     = false;    //存在エラーフラグ
    
            $hissuErrMsg     = "";       //必須エラーメッセージ
            $numericErrMsg   = "";       //数字エラーメッセージ
            $lengthErrMsg    = "";       //レングスエラーメッセージ
            $spaceBarErrMsg  = "";       //スペースにつづいて長音（ー）メッセージ
            $existErrMsg     = "";       //存在エラーメッセージ
    
            $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $spaceBarErrSep = "";
        $existErrSep  = "";
    
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("GRADE", "COURSECD", "MAJORCD", "SEX"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //数値チェック
            if (!in_array($key, array("VALUE"))) {
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
            $check_error = $hissuErrMsg.$existErrMsg.$numericErrMsg.$lengthErrMsg.$spaceBarErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knje410Query::insertQueryErr($db, $line, $check_error);
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
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
    
        return true;
    }
}
