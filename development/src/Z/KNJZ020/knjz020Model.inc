<?php

require_once('for_php7.php');
class knjz020Model extends Model
{
    public $cmd;
    public $field = array();
    public $year;
    public $virus;
    public $lastColumn;

    public function knjz020Model()
    {
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");

        if (VARS::post("year")!="") {
            $this->year = VARS::post("year"); //年度コンボ
        } else {
            $this->year = CTRL_YEAR; //初期値
        }

        $this->field = array("SCHKIND"                        => VARS::post("SCHKIND"),
                             "FOUNDEDYEAR"                    => VARS::post("FOUNDEDYEAR"),
                             "PRESENT_EST"                    => VARS::post("PRESENT_EST"),
                             "CLASSIFICATION"                 => VARS::post("CLASSIFICATION"),
                             "SCHOOLNAME1"                    => VARS::post("SCHOOLNAME1"),
                             "SCHOOLNAME2"                    => VARS::post("SCHOOLNAME2"),
                             "SCHOOLNAME3"                    => VARS::post("SCHOOLNAME3"),
                             "SCHOOLNAME_ENG"                 => VARS::post("SCHOOLNAME_ENG"),
                             "SCHOOLZIPCD"                    => VARS::post("SCHOOLZIPCD"),
                             "SCHOOLADDR1"                    => VARS::post("SCHOOLADDR1"),
                             "SCHOOLADDR2"                    => VARS::post("SCHOOLADDR2"),
                             "SCHOOLADDR1_ENG"                => VARS::post("SCHOOLADDR1_ENG"),
                             "SCHOOLADDR2_ENG"                => VARS::post("SCHOOLADDR2_ENG"),
                             "SCHOOLTELNO"                    => VARS::post("SCHOOLTELNO"),
                             "SCHOOLFAXNO"                    => VARS::post("SCHOOLFAXNO"),
                             "SCHOOLMAIL"                     => VARS::post("SCHOOLMAIL"),
                             "SCHOOLURL"                      => VARS::post("SCHOOLURL"),
                             "SCHOOLDIV"                      => VARS::post("SCHOOLDIV"),
                             "SEMESTERDIV"                    => VARS::post("SEMESTERDIV"),
                             "GRADE_HVAL"                     => VARS::post("GRADE_HVAL"),
                             "ENTRANCE_DATE"                  => VARS::post("ENTRANCE_DATE"),
                             "GRADUATE_DATE"                  => VARS::post("GRADUATE_DATE"),
                             "GRAD_CREDITS"                   => VARS::post("GRAD_CREDITS"),
                             "GRAD_COMP_CREDITS"              => VARS::post("GRAD_COMP_CREDITS"),
                             "SEMES_ASSESSCD"                 => VARS::post("SEMES_ASSESSCD"),
                             "SEMES_FEARVAL"                  => VARS::post("SEMES_FEARVAL"),
                             "GRADE_FEARVAL"                  => VARS::post("GRADE_FEARVAL"),
                             "ABSENT_COV"                     => VARS::post("ABSENT_COV"),
                             "ABSENT_COV_LATE"                => VARS::post("ABSENT_COV_LATE"),
                             "AMARI_KURIAGE"                  => VARS::post("AMARI_KURIAGE"),
                             "GVAL_CALC"                      => VARS::post("GVAL_CALC"),
                             "SEM_OFFDAYS"                    => VARS::post("SEM_OFFDAYS"),
                             "SUB_OFFDAYS"                    => VARS::post("SUB_OFFDAYS"),
                             "SUB_MOURNING"                   => VARS::post("SUB_MOURNING"),
                             "SUB_SUSPEND"                    => VARS::post("SUB_SUSPEND"),
                             "SUB_VIRUS"                      => VARS::post("SUB_VIRUS"),
                             "SUB_KOUDOME"                    => VARS::post("SUB_KOUDOME"),
                             "SUB_ABSENT"                     => VARS::post("SUB_ABSENT"),
                             "TOKUBETU_KATUDO_KANSAN"         => VARS::post("TOKUBETU_KATUDO_KANSAN"),
                             "SYUKESSEKI_HANTEI_HOU"          => VARS::post("SYUKESSEKI_HANTEI_HOU"),
                             "HEADER"                         => VARS::post("HEADER"),                  //ヘッダの有無
                             "OUTPUT"                         => VARS::post("OUTPUT"),                  //処理の種類(1:取込、2:書出、3:エラー出力)
                             "UPDATED"                        => VARS::post("UPDATED"),
                             "YEAR_HIDDEN"                    => VARS::post("YEAR_HIDDEN"),

                             "JUGYOU_JISU_FLG"                => VARS::post("JUGYOU_JISU_FLG"),
                             "RISYU_BUNSI"                    => VARS::post("RISYU_BUNSI"),
                             "RISYU_BUNBO"                    => VARS::post("RISYU_BUNBO"),
                             "SYUTOKU_BUNSI"                  => VARS::post("SYUTOKU_BUNSI"),
                             "SYUTOKU_BUNBO"                  => VARS::post("SYUTOKU_BUNBO"),
                             "RISYU_BUNSI_SPECIAL"            => VARS::post("RISYU_BUNSI_SPECIAL"),
                             "RISYU_BUNBO_SPECIAL"            => VARS::post("RISYU_BUNBO_SPECIAL"),
                             "JOUGENTI_SANSYUTU_HOU"          => VARS::post("JOUGENTI_SANSYUTU_HOU"),
                             "JITU_JIFUN"                     => VARS::post("JITU_JIFUN"),
                             "JITU_JIFUN_SPECIAL"             => VARS::post("JITU_JIFUN_SPECIAL"),
                             "JITU_SYUSU"                     => VARS::post("JITU_SYUSU"),
                             "KESSEKI_WARN_BUNSI"             => VARS::post("KESSEKI_WARN_BUNSI"),
                             "KESSEKI_WARN_BUNBO"             => VARS::post("KESSEKI_WARN_BUNBO"),
                             "KESSEKI_OUT_BUNSI"              => VARS::post("KESSEKI_OUT_BUNSI"),
                             "KESSEKI_OUT_BUNBO"              => VARS::post("KESSEKI_OUT_BUNBO"),
                             "PREF_CD"                        => VARS::post("PREF_CD"),
                             "KYOUIKU_IINKAI_SCHOOLCD"        => VARS::post("KYOUIKU_IINKAI_SCHOOLCD"),
                             "PARTS_HYOUKA_HYOUTEI_KEISAN"    => VARS::post("PARTS_HYOUKA_HYOUTEI_KEISAN"),
                             "MAX_SEMESTER"                   => VARS::post("MAX_SEMESTER"),
                             "KEIKOKUTEN_KUBUN"               => VARS::post("KEIKOKUTEN_KUBUN"),
                             "RUIKEIHEIKIN_BUNSI"             => VARS::post("RUIKEIHEIKIN_BUNSI"),
                             "RUIKEIHEIKIN_BUNBO"             => VARS::post("RUIKEIHEIKIN_BUNBO")
                             );

        if ($this->field["MAX_SEMESTER"]) {
            for ($sem = 1; $sem <= $this->field["MAX_SEMESTER"]; $sem++) {
                $this->field["HOUTEI_SYUSU_SEMESTER".$sem] =  VARS::post("HOUTEI_SYUSU_SEMESTER".$sem);
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? $this->Properties["useVirus"] : false;
        $this->koudome   = ($this->Properties["useKoudome"] == "true") ? $this->Properties["useKoudome"] : false;

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");
        }

        $db = Query::dbCheckOut();
        //月別授業週数
        $this->semMonthInfo = array();
        $query = knjz020Query::selectSemesAll($this->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->semMonthInfo[] = $row;

            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = $i;
                if ($i > 12) {
                    $month = $i - 12;
                }
                $month = sprintf('%02d', $month);
                $this->field["HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."_".$month] =  VARS::post("HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."_".$month);
            }
        }
        $result->free();

        $query = knjz020Query::getDataBaseInfo();
        $this->dataBaseinfo = $db->getOne($query); //教育委員会情報
        Query::dbCheckIn($db);
    }

    public function validateInput()
    {
        //必須入力項目チェック
        if (
            $this->field["FOUNDEDYEAR"]==""  || $this->field["PRESENT_EST"]=="" || $this->field["CLASSIFICATION"]=="" ||
            $this->field["SCHOOLNAME1"]==""  || $this->field["SCHOOLDIV"]==""   || $this->field["SEMESTERDIV"]=="" ||
            $this->field["GRADE_HVAL"]==""   || $this->field["SEMES_ASSESSCD"]==""
        ) {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->field["GVAL_CALC"]=="") {
            $this->setWarning("MSG310", "(評定計算方法)");
            return false;
        }

        if ($this->field["FOUNDEDYEAR"] < 1) {
            $this->setWarning("MSG916", "(創立年度)");
            return false;
        }
        if ($this->field["PRESENT_EST"] < 1) {
            $this->setWarning("MSG916", "(現在期)");
            return false;
        }

        foreach (
            array("CLASSIFICATION" => "(学校種別)",
                       "SCHOOLNAME1"    => "(学校名称1)",
                       "SCHOOLNAME2"    => "(学校名称2)",
                       "SCHOOLNAME3"    => "(学校名称3)",
                       "SCHOOLADDR1"    => "(住所１)",
                       "SCHOOLADDR2"    => "(住所２)") as $key => $val
        ) {
            if ($key == "SCHOOLADDR1" || $key == "SCHOOLADDR2") {
                if (mbereg("[｡-ﾟ]", $this->field[$key])) { //半角カナ禁止
                    $this->setWarning("MSG904", $val);
                    return false;
                }
            } elseif (trim($this->field[$key]) != mb_convert_kana(trim($this->field[$key]), "AKHV")) {
                $this->setWarning("MSG904", $val);
                return false;
            }
        }

        foreach (
            array("SCHOOLNAME_ENG"  => "(学校名称英字)",
                       "SCHOOLADDR1_ENG" => "(住所１(英字))",
                       "SCHOOLADDR2_ENG" => "(住所２(英字))",
                       "SCHOOLMAIL"      => "(メールアドレス)",
                       "SCHOOLURL"       => "(ホームページ)") as $key => $val
        ) {
            if (!preg_match("/^[ A-z0-9 -~]*$/", $this->field[$key])) {
                $this->setWarning("MSG905", $val);
                return false;
            }
        }

        if (strlen($this->field["SCHOOLTELNO"]) && !preg_match("/^[-0-9]*$/", $this->field["SCHOOLTELNO"])) {
            $this->setWarning("MSG909", "(電話番号)");
            return false;
        }

        if (strlen($this->field["SCHOOLFAXNO"]) && !preg_match("/^[-0-9]*$/", $this->field["SCHOOLFAXNO"])) {
            $this->setWarning("MSG909", "(ＦＡＸ番号)");
            return false;
        }

        if ($this->field["SEMESTERDIV"] < 1 || $this->field["SEMESTERDIV"] > 8) {
            $this->setWarning("MSG916", "(学期制)");
            return false;
        }

        if ($this->field["GRADE_HVAL"] < 1) {
            $this->setWarning("MSG916", "(学年数上限)　1以上を指定してください");
            return false;
        }

        //バイト数チェック
        foreach (
            array("CLASSIFICATION"  => array( 6,"(学校種別)"),
                       "SCHOOLNAME1"     => array(90,"(学校名称１)"),
                       "SCHOOLNAME2"     => array(90,"(学校名称２)"),
                       "SCHOOLNAME3"     => array(90,"(学校名称３)"),
                       "SCHOOLNAME_ENG"  => array(60,"(学校名称英字)"),
                       "SCHOOLZIPCD"     => array( 8,"(郵便番号)"),
                       "SCHOOLADDR1"     => array(90,"(住所１)"),
                       "SCHOOLADDR2"     => array(90,"(住所２)"),
                       "SCHOOLADDR1_ENG" => array(70,"(住所１(英字))"),
                       "SCHOOLADDR2_ENG" => array(70,"(住所２(英字))"),
                       "SCHOOLTELNO"     => array(14,"(電話番号)"),
                       "SCHOOLFAXNO"     => array(14,"(ＦＡＸ番号)"),
                       "SCHOOLMAIL"      => array(50,"(メールアドレス)"),
                       "SCHOOLURL"       => array(50,"(ホームページ)")) as $key => $val
        ) {
            if (strlen($this->field[$key]) > $val[0]) {
                $this->setWarning("MSG915", $val[1]);
                return false;
            }
        }

        if ($this->field["JUGYOU_JISU_FLG"] === '1' && $this->Properties["hibiNyuuryokuNasi"] === '1') {
            $syusu_sum = 0;
            $syusu_msg = "";

            if ($this->Properties["use_Month_Syusu"] == "1") {
                for ($dcnt = 0; $dcnt < get_count($this->semMonthInfo); $dcnt++) {
                    for ($i = $this->semMonthInfo[$dcnt]["S_MONTH"]; $i <= $this->semMonthInfo[$dcnt]["E_MONTH"]; $i++) {
                        $month = $i;
                        if ($i > 12) {
                            $month = $i - 12;
                        }
                        $month = sprintf('%02d', $month);
                        //各月の週数を加算
                        $syusu_sum += $this->field["HOUTEI_SYUSU_MONTH_".$this->semMonthInfo[$dcnt]["SEMESTER"]."_".$month];
                    }
                }
                $syusu_msg = "月";
            } else {
                //各学期の週数を加算
                for ($sem = 1; $sem <= $this->field["MAX_SEMESTER"]; $sem++) {
                    $syusu_sum += intval($this->field["HOUTEI_SYUSU_SEMESTER".$sem]);
                }
                $syusu_msg = "学期";
            }
            //値チェック
            if (intval($this->field["JITU_SYUSU"]) !== $syusu_sum) {
                $this->setWarning("MSG901", "各{$syusu_msg}の週数の合計が授業週数と一致していません。");
                return false;
            }
        }

        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            if ($this->field["SEMESTERDIV"] != "") { //学期制に入力した学期分のレコードを作成
                knjz020Query::updateSemesterMst($db, $this->field["SEMESTERDIV"], $this->year);
            }
            $this->field["YEAR"] = $this->year;
            $this->updateRecord($db, $this->field);

            $db->commit();
            Query::dbCheckIn($db);

            $this->setMessage("MSG201");
        }
    }

    public function &updateRecord($db, $recordData)
    {
        knjz020Query::updateSchoolMst($db, $this, $recordData);
        knjz020Query::updateSchoolDetailDat($db, $this, $recordData);
        if ($this->Properties["use_Month_Syusu"] == "1") {
            knjz020Query::updateSyusuMst($db, $recordData);
        }
    }

    public function clean()
    {
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            $header_duplicate = array("YEAR"      => "年度",
                                    "SEMESTER"    => "学期",
                                    "SCHOOLCD"    => "大学コード",
                                    "SCHOOL_KIND" => "学校種別",
                                    "MONTH"       =>  "月",
                                    "GRADE"       =>  "学年",
                                    "COURSECD"    =>  "課程コード",
                                    "MAJORCD"     =>  "学科コード"
                                );
            //ヘッダ、エラー出力
            $header = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
            $tables = array();
            $tables = array("1" => "SEMESTER_MST",
                            "2" => "SCHOOL_MST",
                            "3" => "ATTEND_SYUSU_MST"
                        );
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header_duplicate);
                $contents .= implode($headerErr, ",")."\n";
            }

            $contents .= implode(",", $header) . "\r\n";
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } elseif ($this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "4") {
            //ヘッダ、書出

            $header = array();
            $header["YEAR"]                             = "※年度";
            $header["FOUNDEDYEAR"]                      = "創立年度";
            $header["PRESENT_EST"]                      = "現在期";
            $header["CLASSIFICATION"]                   = "学校種別";
            $header["SCHOOLNAME1"]                      = "学校名1";
            $header["SCHOOLNAME2"]                      = "学校名2";
            $header["SCHOOLNAME3"]                      = "学校名3";
            $header["SCHOOLNAME_ENG"]                   = "学校名称英字";
            $header["SCHOOLZIPCD"]                      = "学校郵便番号";
            $header["SCHOOLADDR1"]                      = "学校住所1";
            $header["SCHOOLADDR2"]                      = "学校住所2";
            $header["SCHOOLADDR1_ENG"]                  = "学校住所1(英字)";
            $header["SCHOOLADDR2_ENG"]                  = "学校住所2(英字)";
            $header["SCHOOLTELNO"]                      = "学校電話番号";
            $header["SCHOOLFAXNO"]                      = "学校FAX番号";
            $header["SCHOOLMAIL"]                       = "学校メールアドレス";
            $header["SCHOOLURL"]                        = "学校ホームページ";
            $header["SCHOOLDIV"]                        = "学校区分";
            $header["SEMESTERDIV"]                      = "学期制";
            $header["GRADE_HVAL"]                       = "学年数上限";
            $header["ENTRANCE_DATE"]                    = "入学日付";
            $header["GRADUATE_DATE"]                    = "卒業日付";
            $header["GRAD_CREDITS"]                     = "卒業単位数";
            $header["GRAD_COMP_CREDITS"]                = "卒業履修単位数";
            $header["SEMES_ASSESSCD"]                   = "学期末評定区分";
            $header["SEMES_FEARVAL"]                    = "学期末保留値";
            $header["GRADE_FEARVAL"]                    = "学年末保留値";
            $header["SEM_OFFDAYS"]                      = "休学時の欠席フラグ";
            $header["GVAL_CALC"]                        = "評定計算方法";
            $header["SUB_OFFDAYS"]                      = "休学時の欠課フラグ";
            $header["SUB_MOURNING"]                     = "忌引時の欠課フラグ";
            $header["SUB_ABSENT"]                       = "公欠時の欠課フラグ";
            $header["SUB_SUSPEND"]                      = "出停(法止)賞罰時の欠課フラグ";
            if ($this->koudome == 'true') {
                $header["SUB_KOUDOME"]                      = "出停(公止)賞罰時の欠課フラグ";
            }
            if ($this->virus == 'true') {
                $header["SUB_VIRUS"]                        = "出停伝染病時の欠課フラグ";
            }
            $header["SYUKESSEKI_HANTEI_HOU"]            = "一日出欠席の判定方法";
            $header["ABSENT_COV"]                       = "欠課数換算";
            $header["ABSENT_COV_LATE"]                  = "欠課数換算の遅刻数";
            $header["AMARI_KURIAGE"]                    = "欠課数換算の繰上げ余り回数";
            $header["TOKUBETU_KATUDO_KANSAN"]           =  "特活欠課数換算";
            if ($this->Properties["use_school_detail_gcm_dat"] != "1") {
                $header["JUGYOU_JISU_FLG"]                  =  "授業時数管理区分";
                $header["RISYU_BUNSI"]                      =  "履修上限値(分子)";
                $header["RISYU_BUNBO"]                      =  "履修上限値(分母)";
                $header["SYUTOKU_BUNSI"]                    =  "修得上限値(分子)";
                $header["SYUTOKU_BUNBO"]                    =  "修得上限値(分母)";
                $header["RISYU_BUNSI_SPECIAL"]              =  "特活上限値(分子)";
                $header["RISYU_BUNBO_SPECIAL"]              =  "特活上限値(分母)";
                $header["JOUGENTI_SANSYUTU_HOU"]            =  "上限値算出方法";
                $header["JITU_JIFUN"]                       =  "1単位あたり";
                $header["JITU_JIFUN_SPECIAL"]               =  "特活授業時間(分)";
                $header["JITU_SYUSU"]                       =  "授業週数";
                $header["KESSEKI_WARN_BUNSI"]               =  "欠席日数注意(分子)";
                $header["KESSEKI_WARN_BUNBO"]               =  "欠席日数注意(分母)";
                $header["KESSEKI_OUT_BUNSI"]                =  "欠席日数超過(分子)";
                $header["KESSEKI_OUT_BUNBO"]                =  "欠席日数超過(分母)";
            }
            $header["PREF_CD"]                          =  "都道府県";
            if ($this->dataBaseinfo === '2') {
                $header["KYOUIKU_IINKAI_SCHOOLCD"]      =  "教育委員会統計用学校番号";
            }
            $header["PARTS_HYOUKA_HYOUTEI_KEISAN"]      =  "評価・評定計算方法";
            $this->headerMArray = array();
            $this->headerSArray = array();
            if ($this->Properties["hibiNyuuryokuNasi"] === '1') {
                if ($this->Properties["use_school_detail_gcm_dat"] != "1" && $this->Properties["use_Month_Syusu"] == "1") {
                    //月別週数
                    $this->headerMArray = knjz020Query::getHeaderMSyusu($this);
                    foreach ($this->headerMArray as $key => $val) {
                        $header["HOUTEI_SYUSU_MONTH_".$key]  =  $val;
                    }
                } else {
                    //学期別週数
                    $this->headerSArray = knjz020Query::getHeaderSSyusu($this);
                    foreach ($this->headerSArray as $key => $label) {
                        $header["HOUTEI_SYUSU_SEMESTER".$key]  =  $label;
                    }
                }
            }
            if ($this->Properties["use_school_Detail_009"] === '1') {
                $header["KEIKOKUTEN_KUBUN"]                 =  "警告点区分";
                $header["RUIKEIHEIKIN_BUNSI"]               =  "類型平均(分子)";
                $header["RUIKEIHEIKIN_BUNBO"]               =  "類型平均(分母)";
            }
            $header["LASTCOLUMN"]  =  $this->lastColumn;

            if ($this->field["HEADER"]) {
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }
            $query = knjz020Query::getCsvData($this);
        }
        if ($this->field["OUTPUT"] == "4") {
            $header_rei = array();
            $header_rei["YEAR"]                                             = "2015";
            $header_rei["FOUNDEDYEAR"]                                      = "1960";
            $header_rei["PRESENT_EST"]                                      = "07";
            $header_rei["CLASSIFICATION"]                                   = "都立";
            $header_rei["SCHOOLNAME1"]                                      = "東京都立アルプ高等学校";
            $header_rei["SCHOOLNAME2"]                                      = "アルプ高校";
            $header_rei["SCHOOLNAME3"]                                      = "アルプ高等学校";
            $header_rei["SCHOOLNAME_ENG"]                                   = "tokyou alp school";
            $header_rei["SCHOOLZIPCD"]                                      = "102-0093";
            $header_rei["SCHOOLADDR1"]                                      = "東京都千代田区平河町1-2-10";
            $header_rei["SCHOOLADDR2"]                                      = "ビル名　2F";
            $header_rei["SCHOOLADDR1_ENG"]                                  = "1-2-10 HIRAKAWACHOU TIYODAKU TOKYO";
            $header_rei["SCHOOLADDR2_ENG"]                                  = "addr2";
            $header_rei["SCHOOLTELNO"]                                      = "03-3234-3571";
            $header_rei["SCHOOLFAXNO"]                                      = "03-3234-3619";
            $header_rei["SCHOOLMAIL"]                                       = "mailaddress@alp.co.jp";
            $header_rei["SCHOOLURL"]                                        = "http://homepage.address";
            $header_rei["SCHOOLDIV"]                                        = "1";
            $header_rei["SEMESTERDIV"]                                      = "3";
            $header_rei["GRADE_HVAL"]                                       = "03";
            $header_rei["ENTRANCE_DATE"]                                    = "2015/04/01";
            $header_rei["GRADUATE_DATE"]                                    = "2016/03/31";
            $header_rei["GRAD_CREDITS"]                                     = "83";
            $header_rei["GRAD_COMP_CREDITS"]                                = "100";
            $header_rei["SEMES_ASSESSCD"]                                   = "2";
            $header_rei["SEMES_FEARVAL"]                                    = "5";
            $header_rei["GRADE_FEARVAL"]                                    = "10";

            $header_rei["SEM_OFFDAYS"]                                      = "1";
            $header_rei["GVAL_CALC"]                                        = "1";
            $header_rei["SUB_OFFDAYS"]                                      = "1";
            $header_rei["SUB_MOURNING"]                                     = "1";
            $header_rei["SUB_ABSENT"]                                       = "1";
            $header_rei["SUB_SUSPEND"]                                      = "1";
            if ($this->koudome == 'true') {
                $header_rei["SUB_KOUDOME"]                                  = "1";
            }
            if ($this->virus == 'true') {
                $header_rei["SUB_VIRUS"]                                    = "1";
            }
            $header_rei["SYUKESSEKI_HANTEI_HOU"]                            = "1";
            $header_rei["ABSENT_COV"]                                       = "2";
            $header_rei["ABSENT_COV_LATE"]                                  = "5";
            $header_rei["AMARI_KURIAGE"]                                    = "4";
            $header_rei["TOKUBETU_KATUDO_KANSAN"]                           = "2";
            if ($this->Properties["use_school_detail_gcm_dat"] != "1") {
                $header_rei["JUGYOU_JISU_FLG"]                              = "1";
                $header_rei["RISYU_BUNSI"]                                  = "1";
                $header_rei["RISYU_BUNBO"]                                  = "3";
                $header_rei["SYUTOKU_BUNSI"]                                = "1";
                $header_rei["SYUTOKU_BUNBO"]                                = "60";
                $header_rei["RISYU_BUNSI_SPECIAL"]                          = "1";
                $header_rei["RISYU_BUNBO_SPECIAL"]                          = "5";
                $header_rei["JOUGENTI_SANSYUTU_HOU"]                        = "3";
                $header_rei["JITU_JIFUN"]                                   = "50";
                $header_rei["JITU_JIFUN_SPECIAL"]                           = "45";
                $header_rei["JITU_SYUSU"]                                   = "35";
                $header_rei["KESSEKI_WARN_BUNSI"]                           = "1";
                $header_rei["KESSEKI_WARN_BUNBO"]                           = "5";
                $header_rei["KESSEKI_OUT_BUNSI"]                            = "1";
                $header_rei["KESSEKI_OUT_BUNBO"]                            = "5";
            }
            $header_rei["PREF_CD"]                                          = "04";
            if ($this->dataBaseinfo === '2') {
                $header_rei["KYOUIKU_IINKAI_SCHOOLCD"]                      = "0001";
            }
            $header_rei["PARTS_HYOUKA_HYOUTEI_KEISAN"]                      = "1";
            if ($this->Properties["hibiNyuuryokuNasi"] === '1') {
                if ($this->Properties["use_school_detail_gcm_dat"] != "1" && $this->Properties["use_Month_Syusu"] == "1") {
                    //月別週数
                    foreach ($this->headerMArray as $key => $val) {
                        $header_rei["HOUTEI_SYUSU_MONTH_".$key]  =  "3";
                    }
                } else {
                    //学期別週数
                    foreach ($this->headerSArray as $key => $label) {
                        $header_rei["HOUTEI_SYUSU_SEMESTER".$key]  =  "15";
                    }
                }
            }
            if ($this->Properties["use_school_Detail_009"] === '1') {
                $header_rei["KEIKOKUTEN_KUBUN"]                                 =  "2";
                $header_rei["RUIKEIHEIKIN_BUNSI"]                               =  "2";
                $header_rei["RUIKEIHEIKIN_BUNBO"]                               =  "3";
            }
            $header_rei["LASTCOLUMN"]  =  $this->lastColumn;

            $contents .= implode(",", $header_rei);
            $data = true;
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $csvRowData = array();
                foreach ($header as $key => $val) {
                    //日付変換
                    if (in_array($key, array("ENTRANCE_DATE", "GRADUATE_DATE"))) {
                        $row[$key] = str_replace("-", "/", $row[$key]);
                    }

                    $csvRowData[$key] = $row[$key];
                }
                $contents .= implode(",", $csvRowData)."\n";
                $data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }
        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse="学校マスタエラー";
            } else {
                $couse="学校マスタ一覧";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                foreach ($this->data_arr as $data) {
                    if ($data["SEMESTERDIV"] != "") {
                        knjz020Query::updateSemesterMst($db, $data["SEMESTERDIV"], $data["YEAR"]);
                    }
                    $this->updateRecord($db, $data);
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["YEAR"]                             = array(4  ,"年度" );
            $checkdata["FOUNDEDYEAR"]                      = array(4  ,"創立年度");
            $checkdata["PRESENT_EST"]                      = array(3  ,"現在期");
            $checkdata["CLASSIFICATION"]                   = array(6  ,"学校種別");
            $checkdata["SCHOOLNAME1"]                      = array(90 ,"学校名1");
            $checkdata["SCHOOLNAME2"]                      = array(90 ,"学校名2");
            $checkdata["SCHOOLNAME3"]                      = array(90 ,"学校名3");
            $checkdata["SCHOOLNAME_ENG"]                   = array(60 ,"学校名称英字");
            $checkdata["SCHOOLZIPCD"]                      = array(8  ,"学校郵便番号");
            $checkdata["SCHOOLADDR1"]                      = array(75 ,"学校住所1");
            $checkdata["SCHOOLADDR2"]                      = array(75 ,"学校住所2");
            $checkdata["SCHOOLADDR1_ENG"]                  = array(50 ,"学校住所1(英字)");
            $checkdata["SCHOOLADDR2_ENG"]                  = array(50 ,"学校住所2(英字)");
            $checkdata["SCHOOLTELNO"]                      = array(14 ,"学校電話番号");
            $checkdata["SCHOOLFAXNO"]                      = array(14 ,"学校FAX番号");
            $checkdata["SCHOOLMAIL"]                       = array(50 ,"学校メールアドレス");
            $checkdata["SCHOOLURL"]                        = array(50 ,"学校ホームページ");
            $checkdata["SCHOOLDIV"]                        = array(1  ,"学校区分");
            $checkdata["SEMESTERDIV"]                      = array(1  ,"学期制");
            $checkdata["GRADE_HVAL"]                       = array(2  ,"学年数上限");
            $checkdata["ENTRANCE_DATE"]                    = array(10 ,"入学日付");
            $checkdata["GRADUATE_DATE"]                    = array(10 ,"卒業日付");
            $checkdata["GRAD_CREDITS"]                     = array(3  ,"卒業単位数");
            $checkdata["GRAD_COMP_CREDITS"]                = array(3  ,"卒業履修単位数");
            $checkdata["SEMES_ASSESSCD"]                   = array(1  ,"学期末評定区分");
            $checkdata["SEMES_FEARVAL"]                    = array(3  ,"学期末保留値");
            $checkdata["GRADE_FEARVAL"]                    = array(3  ,"学年末保留値");
            $checkdata["SEM_OFFDAYS"]                      = array(1  ,"休学時の欠席フラグ");
            $checkdata["GVAL_CALC"]                        = array(1  ,"評定計算方法");
            $checkdata["SUB_OFFDAYS"]                      = array(1  ,"休学時の欠課フラグ");
            $checkdata["SUB_MOURNING"]                     = array(1  ,"忌引時の欠課フラグ");
            $checkdata["SUB_ABSENT"]                       = array(1  ,"公欠時の欠課フラグ");
            $checkdata["SUB_SUSPEND"]                      = array(1  ,"出停(法止)賞罰時の欠課フラグ");
            if ($this->koudome == 'true') {
                $checkdata["SUB_KOUDOME"]                  = array(1  ,"出停(公止)賞罰時の欠課フラグ");
            }
            if ($this->virus == 'true') {
                $checkdata["SUB_VIRUS"]                    = array(1  ,"出停伝染病時の欠課フラグ");
            }
            $checkdata["SYUKESSEKI_HANTEI_HOU"]            = array(1  ,"一日出欠席の判定方法");
            $checkdata["ABSENT_COV"]                       = array(1  ,"欠課数換算");
            $checkdata["ABSENT_COV_LATE"]                  = array(2  ,"欠課数換算の遅刻数");
            $checkdata["AMARI_KURIAGE"]                    = array(2  ,"欠課数換算の繰上げ余り回数");
            $checkdata["TOKUBETU_KATUDO_KANSAN"]           = array(1  ,"特活欠課数換算");
            if ($this->Properties["use_school_detail_gcm_dat"] != "1") {
                $checkdata["JUGYOU_JISU_FLG"]              = array(1  ,"授業時数管理区分");
                $checkdata["RISYU_BUNSI"]                  = array(2  ,"履修上限値(分子)");
                $checkdata["RISYU_BUNBO"]                  = array(2  ,"履修上限値(分母)");
                $checkdata["SYUTOKU_BUNSI"]                = array(2  ,"修得上限値(分子)");
                $checkdata["SYUTOKU_BUNBO"]                = array(2  ,"修得上限値(分母)");
                $checkdata["RISYU_BUNSI_SPECIAL"]          = array(2  ,"特活上限値(分子)");
                $checkdata["RISYU_BUNBO_SPECIAL"]          = array(2  ,"特活上限値(分母)");
                $checkdata["JOUGENTI_SANSYUTU_HOU"]        = array(1  ,"上限値算出方法");
                $checkdata["JITU_JIFUN"]                   = array(3  ,"1単位あたり");
                $checkdata["JITU_JIFUN_SPECIAL"]           = array(3  ,"特活授業時間(分)");
                $checkdata["JITU_SYUSU"]                   = array(3  ,"授業週数");
                $checkdata["KESSEKI_WARN_BUNSI"]           = array(2  ,"席日数注意(分子)");
                $checkdata["KESSEKI_WARN_BUNBO"]           = array(2  ,"欠席日数注意(分母)");
                $checkdata["KESSEKI_OUT_BUNSI"]            = array(2  ,"欠席日数超過(分子)");
                $checkdata["KESSEKI_OUT_BUNBO"]            = array(2  ,"欠席日数超過(分母)");
            }
            $checkdata["PREF_CD"]                          =  array(2  ,"都道府県");
            if ($this->dataBaseinfo === '2') {
                $checkdata["KYOUIKU_IINKAI_SCHOOLCD"]      =  array(4  ,"教育委員会統計用学校番号");
            }
            $checkdata["PARTS_HYOUKA_HYOUTEI_KEISAN"]      =  array(1  ,"評価・評定計算方法");

            if ($this->Properties["hibiNyuuryokuNasi"] === '1') {
                if ($this->Properties["use_school_detail_gcm_dat"] != "1" && $this->Properties["use_Month_Syusu"] == "1") {
                    //月別週数
                    foreach ($this->headerMArray as $key => $val) {
                        $checkdata["HOUTEI_SYUSU_MONTH_".$key]  =  array(3, $val);
                    }
                } else {
                    //学期別週数
                    foreach ($this->headerSArray as $key => $label) {
                        $checkdata["HOUTEI_SYUSU_SEMESTER".$key]  =  array(3, $label);
                    }
                }
            }
            if ($this->Properties["use_school_Detail_009"] === '1') {
                $checkdata["KEIKOKUTEN_KUBUN"]                 =  array(1  ,"警告点区分");
                $checkdata["RUIKEIHEIKIN_BUNSI"]               =  array(4  ,"類型平均(分子)");
                $checkdata["RUIKEIHEIKIN_BUNBO"]               =  array(4  ,"類型平均(分母)");
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SEMESTER_MST');
            $duplicateCsv1 = array();

            $primaryKey2 = array();
            $primaryKey2 = $this->getPrimaryKey('SCHOOL_MST');
            $duplicateCsv2 = array();

            $primaryKey3 = array();
            $primaryKey3 = $this->getPrimaryKey('ATTEND_SYUSU_MST');
            $duplicateCsv3 = array();

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
                    $fieldCnt = 0;

                    //データ取込み
                    $jugyouJisuBlankFlg = false;
                    foreach ($checkdata as $key => $val) {
                        $csvVal = trim($tmp[$fieldCnt]);

                        //0埋め
                        if ($key == "PREF_CD") {
                            $csvVal = sprintf("%02d", $csvVal);
                        }

                        //授業時数管理区分設定フラグが未指定の場合
                        if ($key == "JUGYOU_JISU_FLG" && $csvVal == "") {
                            $jugyouJisuBlankFlg = true;
                        }

                        //授業時数管理区分を設定していない時はブランク
                        if ($jugyouJisuBlankFlg && in_array($key, array("RISYU_BUNSI", "RISYU_BUNBO", "SYUTOKU_BUNSI", "SYUTOKU_BUNBO", "RISYU_BUNSI_SPECIAL", "RISYU_BUNBO_SPECIAL"))) {
                            $csvVal = "";
                        }

                        $data[$key] = $csvVal;
                        $fieldCnt++;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }

                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg           = true;
        $hissuErrFlg         = false;    //必須エラーフラグ
        $lengthErrFlg        = false;    //レングスエラーフラグ
        $otherErrFlg         = false;    //年度エラーフラグ
        $valuesErrFlg        = false;
        $duplicateDbErrFlg   = false;
        $duplicateCsvErrFlg  = false;

        $hissuErrMsg         = "";       //必須エラーメッセージ
        $lengthErrMsg        = "";       //レングスエラーメッセージ
        $otherErrMsg         = "";       //その他エラーメッセージ
        $valuesErrMsg        = "";
        $duplicateDbErrMsg   = "";
        $duplicateCsvErrMsg  = "";       //重複エラーメッセージ

        $hissuSep            = "";
        $lengthSep           = "";
        $otherSep            = "";
        $valuesSep           = "";
        $duplicateCsvSep     = "";
        $duplicateDbSep      = "";

        //必須チェックするキー ※画面上で授業時数管理区分より下の必須項目は分岐で必須か否かが決まるのでここに入れない。
        $hissuKeyArray   = array();
        $hissuKeyArray[] = "YEAR";
        $hissuKeyArray[] = "FOUNDEDYEAR";
        $hissuKeyArray[] = "PRESENT_EST";
        $hissuKeyArray[] = "CLASSIFICATION";
        $hissuKeyArray[] = "SCHOOLNAME1";
        $hissuKeyArray[] = "SCHOOLDIV";
        $hissuKeyArray[] = "SEMESTERDIV";
        $hissuKeyArray[] = "GRADE_HVAL";
        $hissuKeyArray[] = "SEMES_ASSESSCD";
        $hissuKeyArray[] = "GVAL_CALC";

        $semesterdiv = $data["SEMESTERDIV"];
        $year        = $data["YEAR"];
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        $query = knjz020Query::getDBDuplicate($db, $semesterdiv, $year);
        $cur_num = $db->getOne($query);
        if ($cur_num == $semesterdiv) {
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
        }
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }


        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, $hissuKeyArray) && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            if ($checkdata["JUGYOU_JISU_FLG"] == "") {
                if (in_array($key, array("JITU_JIFUN", "JITU_SYUSU")) && !strlen(trim($data[$key]))) {
                    $returnlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            } elseif (in_array($key, array("RISYU_BUNSI","RISYU_BUNBO","SYUTOKU_BUNSI","SYUTOKU_BUNBO","RISYU_BUNSI_SPECIAL","RISYU_BUNBO_SPECIAL","JITU_JIFUN","JITU_JIFUN_SPECIAL","JITU_SYUSU","PREF_CD")) && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }

            //0不可
            if (in_array($key, array("RISYU_BUNSI","RISYU_BUNBO","SYUTOKU_BUNSI","SYUTOKU_BUNBO","RISYU_BUNSI_SPECIAL","RISYU_BUNBO_SPECIAL","JITU_JIFUN","JITU_JIFUN_SPECIAL","JITU_SYUSU","PREF_CD","KESSEKI_OUT_BUNSI","KESSEKI_OUT_BUNBO"))) {
                if (intval($data[$key]) === 0) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }

            //フラグチェック(値は1かNULL)
            if (in_array($key, array("SEM_OFFDAYS","SUB_OFFDAYS","SUB_MOURNING","SUB_SUSPEND","SUB_VIRUS","SUB_ABSENT","SUB_KOUDOME"))) {
                if ($data[$key] > 1) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        //数値チェック
        if ($data["RISYU_BUNSI"] > $data["RISYU_BUNBO"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "履修上限値(分母/分子)");
        }
        if ($data["SYUTOKU_BUNSI"] > $data["SYUTOKU_BUNBO"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "修得上限値(分母/分子)");
        }
        if ($data["RISYU_BUNSI_SPECIAL"] > $data["RISYU_BUNBO_SPECIAL"]) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "特活上限値(分母/分子)");
        }
        if ((!$data["KESSEKI_WARN_BUNSI"] && $data["KESSEKI_WARN_BUNBO"]) || ($data["KESSEKI_WARN_BUNSI"] && !$data["KESSEKI_WARN_BUNBO"])) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "欠席日数注意");
        } elseif ($data["KESSEKI_WARN_BUNSI"] && $data["KESSEKI_WARN_BUNBO"]) {
            if ($data["KESSEKI_WARN_BUNSI"] > $data["KESSEKI_WARN_BUNBO"]) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "欠席日数注意");
            }
        }
        if ((!$data["KESSEKI_OUT_BUNSI"] && $data["KESSEKI_OUT_BUNBO"]) || ($data["KESSEKI_OUT_BUNSI"] && !$data["KESSEKI_OUT_BUNBO"])) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "欠席日数超過");
        } elseif ($data["KESSEKI_OUT_BUNSI"] && $data["KESSEKI_OUT_BUNBO"]) {
            if ($data["KESSEKI_OUT_BUNSI"] > $data["KESSEKI_OUT_BUNBO"]) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "欠席日数超過");
            }
        }
        if ($data["RUIKEIHEIKIN_BUNSI"] && $data["RUIKEIHEIKIN_BUNBO"]) {
            if ($data["RUIKEIHEIKIN_BUNSI"] > $data["RUIKEIHEIKIN_BUNBO"]) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※数値:", $otherSep, "類型平均(分母/分子)");
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$lengthErrMsg.$otherErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz020Query::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }

        return true;
    }
}
