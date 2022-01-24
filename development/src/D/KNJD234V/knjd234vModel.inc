<?php

require_once('for_php7.php');

class knjd234vModel extends Model {
    var $field;
    var $file;
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $schoolCode;
    var $rankForArray = array("SHIDOU", "YURYO", "FUSHIN");
    var $rankArray = array("HR", "COURSE", "MAJOR", "GRADE");
    var $rankname = array();
    var $isReitaku;

    function knjd234vModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        foreach ($this->rankForArray as $rf) {
            foreach ($this->rankArray as $r) {
                $this->rankname[] = $rf."_".$r."_RANK";
            }
        }
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->test_cd_before  = VARS::post("TEST_CD_BEFORE");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd_before);
        }

        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "EDATE"                =>  VARS::post("EDATE"),                //出欠集計範囲・終了日付
                             "DATE_DIV"             =>  VARS::post("DATE_DIV"),             //出欠集計範囲（累計・学期）ラジオボタン
                             "KARI_HYOTEI"          =>  VARS::post("KARI_HYOTEI"),          //仮評定を出力する
                             "SLUMP_TENSU_INF2"     =>  VARS::post("SLUMP_TENSU_INF2"),     //成績不振者　点数
                             "SHIDOU_TENSU_INF2"    =>  VARS::post("SHIDOU_TENSU_INF2"),    //追指導2　点数
                             "SHIDOU_KAMOKUSU_INF2" =>  VARS::post("SHIDOU_KAMOKUSU_INF2"), //追指導2　科目数
                             "SHIDOU_KAMOKUSU_INF1" =>  VARS::post("SHIDOU_KAMOKUSU_INF1"), //追指導1　科目数
                             "KESSEKI"              =>  VARS::post("KESSEKI"),              //出席状況　欠席
                             "CHIKOKU"              =>  VARS::post("CHIKOKU"),              //出席状況　遅刻
                             "SOUTAI"               =>  VARS::post("SOUTAI"),               //出席状況　早退
                             "NISSUU_BUNBO"         =>  VARS::post("NISSUU_BUNBO"),         //教科・科目　欠席日数分母
                             "NISSUU_BUNSHI"        =>  VARS::post("NISSUU_BUNSHI"),        //教科・科目　欠席日数分子
                             "JISUU_BUNBO"          =>  VARS::post("JISUU_BUNBO"),          //教科・科目　欠課時数分母
                             "JISUU_BUNSHI"         =>  VARS::post("JISUU_BUNSHI"),         //教科・科目　欠課時数分子
                             "OUTPUT_RANK_BEFORE"   =>  VARS::post("OUTPUT_RANK_BEFORE"),   //前定期考査順位出力
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "OUTPUT_REMARK"        =>  VARS::post("OUTPUT_REMARK"),        //備考欄
                             "MAJOR"                =>  VARS::post("MAJOR"),                //学科
                             "SHORI_MEI"            =>  VARS::post("SHORI_MEI"),            //処理名
                             "HEADER"               =>  VARS::post("HEADER"),               //ヘッダ有無
                             "D056"                 =>  VARS::post("D056"),                 //D056
                             "DOSUBUPU_COURSE"      =>  VARS::post("DOSUBUPU_COURSE"),      //科目別度数分布 コース別に出力
                             "YURYO"                =>  VARS::post("YURYO"),                //優良者
                             "JOUI_COURSE"          =>  VARS::post("JOUI_COURSE"),          //優良者 コース別に出力
                             "FUSHIN"               =>  VARS::post("FUSHIN"),               //不振者
                             "KESSHI_NOZOKU"        =>  VARS::post("KESSHI_NOZOKU"),        //欠試者は除く
                             "KAI_COURSE"           =>  VARS::post("KAI_COURSE"),           //不振者 コース別に出力
                             "KYUGAKU"              =>  VARS::post("KYUGAKU"),              //休学者を対象外
                             "OUTPUT_SLUMP"         =>  VARS::post("OUTPUT_SLUMP"),         //成績不振者を出力
                             "OUTPUT_SHIDOU"        =>  VARS::post("OUTPUT_SHIDOU"),        //要追指導を出力
                             "OUTPUT_SHUKKETSU"     =>  VARS::post("OUTPUT_SHUKKETSU"),     //出欠状況を出力
                             "OUTPUT_KYOKAKAMOKU"   =>  VARS::post("OUTPUT_KYOKAKAMOKU"),   //教科・科目を出力
                             "OUTPUT_DOSUBUPU"      =>  VARS::post("OUTPUT_DOSUBUPU"),      //度数分布を出力
                             "OUTPUT_YURYO"         =>  VARS::post("OUTPUT_YURYO"),         //優良者を出力
                             "OUTPUT_FUSHIN"        =>  VARS::post("OUTPUT_FUSHIN"),        //不振者を出力
                             "FUSHIN_DIV"           =>  VARS::post("FUSHIN_DIV"),           //不審者 or 欠点科目数
                             "KETTEN_COUNT"         =>  VARS::post("KETTEN_COUNT"),         //欠点科目数 科目数
                             "FUSHIN_ORDER_KETTEN_COUNT" =>  VARS::post("FUSHIN_ORDER_KETTEN_COUNT"),         //欠点科目数順に表示
                             );
        foreach ($this->rankname as $name) {
            $this->field[$name] = VARS::post($name);
        }
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->file = VARS::file("FILE");
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
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    $cnt = knjd234vQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    $cnt = knjd234vQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {

            $checkdata = array("YEAR"           => array(4,"年度"),
                               "SEMESTER"       => array(1,"学期"),
                               "TESTKINDCD"     => array(2,"考査種別（大分類）"),
                               "TESTITEMCD"     => array(2,"考査種別（中分類）"),
                               "SCORE_DIV"      => array(2,"考査種別（小分類）"),
                               "REASON_DIV"     => array(2,"事由区分"),
                               "CLASSCD"        => array(2,"教科"),
                               "SCHOOL_KIND"    => array(2,"校種"),
                               "CURRICULUM_CD"  => array(2,"教育課程"),
                               "SUBCLASSCD"     => array(6,"科目"),
                               "SCHREGNO"       => array(8,"学籍番号"),
                               "REMARK"         => array(120,"事由"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query =  $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SPECIAL_REASON_TEST_DAT');
            $duplicateCsv1 = array();
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $reason_div = (strlen(trim($tmp[0]))) ? sprintf("%02d",$tmp[0]) : "";
                    if ($reason_div == "01") {
                        $data = array("YEAR"           => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                      "SEMESTER"       => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                      "TESTKINDCD"     => (strlen(trim($tmp[3]))) ? sprintf("%02d",$tmp[3]) : "",
                                      "TESTITEMCD"     => (strlen(trim($tmp[4]))) ? sprintf("%02d",$tmp[4]) : "",
                                      "SCORE_DIV"      => (strlen(trim($tmp[5]))) ? sprintf("%02d",$tmp[5]) : "",
                                      "REASON_DIV"     => $reason_div,
                                      "CLASSCD"        => (strlen(trim($tmp[6]))) ? sprintf("%02d",$tmp[6]) : "",
                                      "SCHOOL_KIND"    => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[9]))) ? sprintf("%06d",$tmp[9]) : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[12]))) ? sprintf("%08d",$tmp[12]) : "",
                                      "REMARK"         => (strlen(trim($tmp[15]))) ? $tmp[15] : "");
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[1]))) ? sprintf("%04d",$tmp[1]) : "",
                                      "SEMESTER"       => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                      "TESTKINDCD"     => (strlen(trim($tmp[3]))) ? sprintf("%02d",$tmp[3]) : "",
                                      "TESTITEMCD"     => (strlen(trim($tmp[4]))) ? sprintf("%02d",$tmp[4]) : "",
                                      "SCORE_DIV"      => (strlen(trim($tmp[5]))) ? sprintf("%02d",$tmp[5]) : "",
                                      "REASON_DIV"     => $reason_div,
                                      "CLASSCD"        => "00",
                                      "SCHOOL_KIND"    => "00",
                                      "CURRICULUM_CD"  => "00",
                                      "SUBCLASSCD"     => "000000",
                                      "SCHREGNO"       => (strlen(trim($tmp[8]))) ? sprintf("%08d",$tmp[8]) : "",
                                      "REMARK"         => (strlen(trim($tmp[11]))) ? $tmp[11] : "");
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数値エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("YEAR", "SEMESTER", "TESTKINDCD", "TESTITEMCD", "SCORE_DIV", "REASON_DIV", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //事由区分チェック
            if (in_array($key,array("REASON_DIV"))) {
                if ($data[$key] != $this->field["D056"]) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]."不一致");
                }
            }

            //数値チェック
            if (in_array($key,array("YEAR", "SEMESTER", "TESTKINDCD", "TESTITEMCD", "SCORE_DIV", "REASON_DIV", "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjd234vQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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

        $header = array(    "YEAR"              => "年度",
                            "SEMESTER"          => "学期",
                            "TESTKINDCD"        => "考査種別（大分類）",
                            "TESTITEMCD"        => "考査種別（中分類）",
                            "SCORE_DIV"         => "考査種別（小分類）",
                            "REASON_DIV"        => "事由区分",
                            "CLASSCD"           => "教科",
                            "SCHOOL_KIND"       => "校種",
                            "CURRICULUM_CD"     => "教育課程",
                            "SUBCLASSCD"        => "科目",
                            "SCHREGNO"          => "学籍番号",
                            "REMARK"            => "事由");

        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        //エラー
        $tables = array();
        $tables = array("1" => "SPECIAL_REASON_TEST_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $header);
            $contents .= implode($headerErr, ",")."\n";
        }
        $contents .= implode($header_err, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();
        $query = $this->selectCsvErrQuery(PROGRAMID);

        //SQL文発行
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "エラー出力";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }

    }

}
?>
