<?php
class knja352Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;  //コントロールマスタ
    public $staffcd;  //登録者コード
    public $properties = array();

    public function knja352Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;   //登録者コード
    }

    public function init()
    {
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->ctrlDate = CTRL_DATE;

        $this->field = array("OUTPUT"       => VARS::post("OUTPUT"),       //出力取込種別
                             "OUTPUT_DIV"   => VARS::post("OUTPUT_DIV"),   //出力対象
                             "SCHOOL_KIND"  => VARS::post("SCHOOL_KIND"),  //校種
                             "YEAR"         => VARS::post("YEAR"),         //年度
                             "GRD_YEAR"     => VARS::post("GRD_YEAR"),     //卒業年度
                             "FRESHMAN_DIV" => VARS::post("FRESHMAN_DIV"), //新入生詳細選択
                             "ISSUE_DATE"   => VARS::post("ISSUE_DATE"),   //発行日
                             "LIMIT_DATE"   => VARS::post("LIMIT_DATE"),   //有効期限
                             "HEADER"       => VARS::post("HEADER")        //ヘッダ有
                             );
        $this->applicantdiv = ($this->field["SCHOOL_KIND"] == "H") ? "2" : ($this->field["SCHOOL_KIND"] == "J" ? "1" : "");
        $this->cmd = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }
    }

    public function &getMainModel()
    {
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel1()
    {
        $db = Query::dbCheckOut();

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        /********************/
        /*  データ部分出力  */
        /********************/

        $header = array();
        $header["KOJIN_ID"]            = "個人ID";
        $header["GROUP_ID"]            = "学籍グループID";
        $header["SCHREGNO"]            = "学籍番号";
        $header["EXAMNO"]              = "受験番号";
        $header["ZAIGAKU_DIV"]         = "在学区分";
        $header["ENT_DATE"]            = "入学日付";
        $header["MIBUN"]               = "身分所属";
        $header["GRADE_CD"]            = "年次";
        $header["TRANSFER_DIV"]        = "留学生区分";
        $header["STUDENT_DIV"]         = "学生区分";
        $header["SCHCHAIR_DIV"]        = "履修区分";
        $header["SCHLOR_DIV"]          = "特待生区分";
        $header["TEACHER_CHILD_DIV"]   = "履修区分";
        $header["ARBITRARY_DIV"]       = "任意区分";
        $header["NAME"]                = "正式姓漢字";
        $header["MIDDLE_NAME"]         = "正式中間名漢字";
        $header["LAST_NAME"]           = "正式名漢字";
        $header["NAME_KANA"]           = "正式姓カタカナ";
        $header["MIDDLE_KANA"]         = "正式中間名カタカナ";
        $header["LAST_KANA"]           = "正式名カタカナ";
        $header["ZIPCD"]               = "納付郵便番号";
        $header["NOUHU_ADDR1"]         = "納付住所1";
        $header["NOUHU_ADDR2"]         = "納付住所2";
        $header["GUARD_NAME"]          = "保護者　姓";
        $header["GUARD_MIDDLE_NAME"]   = "保護者　中間";
        $header["GUARD_LAST_NAME"]     = "保護者　名";
        $header["CLAIM_DIV"]           = "請求区分";
        $header["BANKCD"]              = "銀行CD";
        $header["BRANCHCD"]            = "支店CD";
        $header["ACCOUNT_TYPE"]        = "講座種別コード";
        $header["ACCOUNTNO"]           = "口座番号";
        $header["ACCOUNT_KANA"]        = "口座名カナ";
        $header["ACCOUNT_NAME"]        = "口座名漢字";
        $header["CLASS"]               = "クラス";
        $header["REF_NO"]              = "整理番号";
        $header["HONGAKU_OTHER_DIV"]   = "本学他学区分";
        $header["APPLICANTDIV"]        = "入試制度";
        $header["GUARD_TELNO"]         = "連絡先電話番号";
        $header["NOUHU_ADDRESS_DIV"]   = "納付書作成住所区分";
        $header["NOUHU_NAME_DIV"]      = "納付書作成氏名区分";
        $header["GUARD_ZIPCD"]         = "保護者郵便番号";
        $header["NOUHU_GUARD_ADDR1"]   = "保護者住所1";
        $header["NOUHU_GUARD_ADDR2"]   = "保護者住所2";
        $header["MEMO"]                = "メモ";
        $header["PREFERENCE1"]         = "任意設定1";
        $header["PREFERENCE2"]         = "任意設定2";
        $header["PREFERENCE3"]         = "任意設定3";
        $header["PREFERENCE4"]         = "任意設定4";
        $header["PREFERENCE5"]         = "任意設定5";
        $header["PREFERENCE6"]         = "任意設定6";
        $header["PREFERENCE7"]         = "任意設定7";
        $header["PREFERENCE8"]         = "任意設定8";
        $header["PREFERENCE9"]         = "任意設定9";
        $header["PREFERENCE10"]        = "任意設定10";
        $header["FORCE_PTRN"]          = "強制納付パターン";
        $header["END_MARK"]            = "エンドマーク";

        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = knja352Query::selectCsvQuery($this);

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                
                //前処理
                $entDate = str_replace("-", "", $row["ENT_DATE"]);
                $row["KOJIN_ID"]            = $row["SCHREGNO"];
                $row["GROUP_ID"]            = "0";
                $row["EXAMNO"]              = ($this->field["OUTPUT_DIV"] == "1") ? substr($row["EXAMNO"], -10) : "=";
                $row["ZAIGAKU_DIV"]         = ($this->field["OUTPUT_DIV"] == "1") ? "17" : "=";
                $row["ENT_DATE"]            = $entDate;
                $row["MIBUN"]               = ($this->field["SCHOOL_KIND"] == "H") ? "1700000000" : ($this->field["SCHOOL_KIND"] == "J" ? "1400000000" : "");
                $row["GRADE_CD"]            = substr($row["GRADE_CD"], 1);
                $row["TRANSFER_DIV"]        = ($this->field["OUTPUT_DIV"] == "1") ? substr($entDate, 2, 2) : "=";
                $row["STUDENT_DIV"]         = ($this->field["OUTPUT_DIV"] == "1") ? substr($entDate, 4, 2) : "=";
                $row["SCHCHAIR_DIV"]        = "=";
                $row["SCHLOR_DIV"]          = "=";
                $row["TEACHER_CHILD_DIV"]   = "=";
                $row["ARBITRARY_DIV"]       = "=";
                $row["NAME"]                = mb_substr($row["NAME"], 0, 20);
                $row["MIDDLE_NAME"]         = "=";
                $row["LAST_NAME"]           = "=";
                $nameKana = mb_substr($row["NAME_KANA"], 0, 20);
                $row["NAME_KANA"]           = str_replace(" ", "　", mb_convert_kana($nameKana, "C"));
                $row["MIDDLE_KANA"]         = "=";
                $row["LAST_KANA"]           = "=";
                $row["ZIPCD"]               = str_replace("-", "", $row["ZIPCD"]);
                $addr = mb_convert_kana(str_replace("　", "", $row["ADDR1"]).$row["ADDR2"], "KA");
                $row["NOUHU_ADDR1"]         = mb_substr($addr, 0, 30);
                $row["NOUHU_ADDR2"]         = mb_substr($addr, 30, 30);
                $row["GUARD_NAME"]          = mb_substr($row["GUARD_NAME"], 0, 20);
                $row["GUARD_MIDDLE_NAME"]   = "=";
                $row["GUARD_LAST_NAME"]     = "=";
                $row["CLAIM_DIV"]           = ($this->field["OUTPUT_DIV"] == "1") ? "1" : "=";
                $row["BANKCD"]              = "=";
                $row["BRANCHCD"]            = "=";
                $row["ACCOUNT_TYPE"]        = "=";
                $row["ACCOUNTNO"]           = "=";
                $row["ACCOUNT_KANA"]        = "=";
                $row["ACCOUNT_NAME"]        = "=";
                $row["CLASS"]               = "=";
                $row["REF_NO"]              = "=";
                $internalFsCd = ($this->field["SCHOOL_KIND"] == "H") ? "2280426" : "1280526";
                if ($this->field["OUTPUT_DIV"] == "1" && $row["FINSCHOOLCD"] == $internalFsCd) {
                    $hongakuOtherDiv = "1";
                } elseif ($this->field["OUTPUT_DIV"] == "2") {
                    $hongakuOtherDiv = "=";
                } else {
                    $hongakuOtherDiv = "0";
                }
                $row["HONGAKU_OTHER_DIV"]   = $hongakuOtherDiv;
                $row["APPLICANTDIV"]        = "=";
                $row["NOUHU_ADDRESS_DIV"]   = ($this->field["OUTPUT_DIV"] == "1") ? "1" : "=";
                $row["NOUHU_NAME_DIV"]      = ($this->field["OUTPUT_DIV"] == "1") ? "1" : "=";
                $row["GUARD_ZIPCD"]         = str_replace("-", "", $row["GUARD_ZIPCD"]);
                //$gAddr = mb_convert_kana(str_replace("　", "", $row["GUARD_ADDR1"]).$row["GUARD_ADDR2"], "KA");
                $gAddr = str_replace("　", "", $row["GUARD_ADDR1"]).$row["GUARD_ADDR2"];
                $row["NOUHU_GUARD_ADDR1"]   = mb_substr($gAddr, 0, 30);
                $row["NOUHU_GUARD_ADDR2"]   = mb_substr($gAddr, 30, 30);
                $row["MEMO"]                = "=";
                $row["PREFERENCE1"]         = "=";
                $row["PREFERENCE2"]         = "=";
                $row["PREFERENCE3"]         = "=";
                $row["PREFERENCE4"]         = "=";
                $row["PREFERENCE5"]         = "=";
                $row["PREFERENCE6"]         = "=";
                $row["PREFERENCE7"]         = "=";
                $row["PREFERENCE8"]         = "=";
                $row["PREFERENCE9"]         = "=";
                $row["PREFERENCE10"]        = "=";
                $row["FORCE_PTRN"]          = "=";
                $row["END_MARK"]            = "E";

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $code = ($this->field["SCHOOL_KIND"] == "H") ? "17" : ($this->field["SCHOOL_KIND"] == "J" ? "14" : "");
        $outputdiv = ($this->field["OUTPUT_DIV"] == "1") ? "shin" : ($this->field["OUTPUT_DIV"] == "2" ? "zai" : "");
        $date = date("YmdHis");
        $title = "{$code}_{$outputdiv}_{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function getDownloadModel2()
    {
        $db = Query::dbCheckOut();
    
        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
    
        /********************/
        /*  データ部分出力  */
        /********************/
    
        $header = array();
        $header["SCHREGNO"]                = "学籍番号";
        $header["ZAISEKI_STATE_NAME"]      = "在籍状態区分名";
        $header["ZAISEKI_STATE"]           = "在籍状態区分";
        $header["ZAISEKI_DIV_NAME"]        = "在籍区分名";
        $header["ZAISEKI_DIV"]             = "在籍区分";
        $header["OLD_SCHREGNO"]            = "旧学籍番号";
        $header["SETTI_CD_NAME"]           = "設置コード名";
        $header["SETTI_CD"]                = "設置コード";
        $header["FACULTYCD_NAME"]          = "学部コード名";
        $header["FACULTYCD"]               = "学部コード";
        $header["BLANK_MAIL"]              = "メールアドレス";
        $header["NAME"]                    = "学生氏名（漢字）";
        $header["NAME_KANA"]               = "学生氏名（カナ）";
        $header["NAME_ENG"]                = "学生氏名（英字）";
        $header["BIRTHDAY"]                = "生年月日";
        $header["ENT_DATE"]                = "入学年月日";
        $header["GRD_PROSPECT_DIV_NAME"]   = "卒業見込判定区分名";
        $header["GRD_PROSPECT_DIV"]        = "卒業見込判定区分";
        $header["GRD_HANTEI_DIV_NAME"]     = "卒業判定区分名";
        $header["GRD_HANTEI_DIV"]          = "卒業判定区分";
        $header["GRD_PROSPECT_DATE"]       = "卒業見込年月日";
        $header["GRD_DATE"]                = "卒業年月日";
        $header["GRD_YEAR"]                = "卒業年度";
        $header["DIS_ACCEPT_DATE"]         = "退学除籍承認年月日";
        $header["ZIPCD"]                   = "現住所郵便番号";
        $header["ADDR1_PREF_NAME"]         = "現住所都道府県";
        $header["ADDR1"]                   = "現住所１";
        $header["ADDR2"]                   = "現住所２";
        $header["ADDR3"]                   = "現住所３";
        $header["TELNO"]                   = "現住所電話番号";
        $header["EMERGENCYTELNO"]          = "携帯番号";
        $header["GUARD_ZIPCD"]             = "保証人郵便番号";
        $header["GUARD_ADDR1_PREF_NAME"]   = "保証人都道府県";
        $header["GUARD_ADDR1"]             = "保証人住所１";
        $header["GUARD_ADDR2"]             = "保証人住所２";
        $header["GUARD_ADDR3"]             = "保証人住所３";
        $header["GUARD_TELNO"]             = "保証人電話番号";
        $header["GUARD_NAME"]              = "保証人氏名";
        $header["GUARD_KANA"]              = "保証人氏名カナ";
    
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }
    
        /********************/
        /*  データ部分出力  */
        /********************/
        $query = knja352Query::selectCsvQuery($this);
    
        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    
                //前処理
                $row["ZAISEKI_STATE_NAME"]      = ($row["GRD_DIV"] == "") ? "通常" : (($row["GRD_DIV"] == "1") ? "卒業" : "退学");
                $row["ZAISEKI_STATE"]           = ($row["GRD_DIV"] == "") ? "0" : (($row["GRD_DIV"] == "1") ? "1" : "5");
                $row["ZAISEKI_DIV_NAME"]        = ($row["GRD_DIV"] == "") ? "在籍中" : "非在籍";
                $row["ZAISEKI_DIV"]             = ($row["GRD_DIV"] == "") ? "0" : "1";
                $row["OLD_SCHREGNO"]            = "";
                $row["SETTI_CD_NAME"]           = ($this->field["SCHOOL_KIND"] == "H") ? "高等部" : ($this->field["SCHOOL_KIND"] == "J" ? "中学部" : "");
                $row["SETTI_CD"]                = ($this->field["SCHOOL_KIND"] == "H") ? "17" : ($this->field["SCHOOL_KIND"] == "J" ? "14" : "");
                $row["FACULTYCD_NAME"]          = "";
                $row["FACULTYCD"]               = "";
                $row["BLANK_MAIL"]              = "";
                $row["NAME"]                    = mb_substr($row["NAME"], 0, 20);
                $nameKana = mb_substr($row["NAME_KANA"], 0, 20);
                $row["NAME_KANA"]               = str_replace(" ", "　", mb_convert_kana($nameKana, "C"));
                $row["NAME_ENG"]                = mb_substr($row["NAME_ENG"], 0, 30);
                $row["BIRTHDAY"]                = str_replace("-", "/", $row["BIRTHDAY"]);
                $row["ENT_DATE"]                = str_replace("-", "/", $row["ENT_DATE"]);
                $row["GRD_PROSPECT_DIV_NAME"]   = "未判定";
                $row["GRD_PROSPECT_DIV"]        = "0";
                $row["GRD_HANTEI_DIV_NAME"]     = "未判定";
                $row["GRD_HANTEI_DIV"]          = "0";
                $row["GRD_PROSPECT_DATE"]       = "";
                $grdDate = $row["GRD_DATE"];
                $row["GRD_DATE"]                = ($row["GRD_DIV"] != "") ? str_replace("-", "/", $grdDate) : "";
                $row["GRD_YEAR"]                = ($row["GRD_DIV"] != "") ? $row["GRD_YEAR"] : "";
                $row["DIS_ACCEPT_DATE"]         = ($row["GRD_DIV"] == "2" || $row["GRD_DIV"] == "6") ? str_replace("-", "/", $grdDate) : "";

                $addrSplitFunc = function ($address, $firstLimitLen, $secondLimitLen) {
                    //住所を全角スペースまたは指定文字数で区切る
                    $sp = mb_strpos($address, "　");
                    $delPos = ($sp !== false && $sp <= $firstLimitLen) ? $sp + 1 : $firstLimitLen;
                    $first = str_replace("　", "", mb_substr($address, 0, $delPos));
                    $second = mb_substr($address, $delPos, $secondLimitLen);

                    return array($first, $second);
                };

                list($row["ADDR1_PREF_NAME"], $row["ADDR1"])                = $addrSplitFunc($row["ADDR1"], 4, 16);
                list($row["ADDR2"], $row["ADDR3"])                          = $addrSplitFunc($row["ADDR2"], 20, 20);
                list($row["GUARD_ADDR1_PREF_NAME"], $row["GUARD_ADDR1"])    = $addrSplitFunc($row["GUARD_ADDR1"], 4, 16);
                list($row["GUARD_ADDR2"], $row["GUARD_ADDR3"])              = $addrSplitFunc($row["GUARD_ADDR2"], 20, 20);
                $row["GUARD_NAME"] = mb_substr($row["GUARD_NAME"], 0, 20);
                $guardKana = mb_substr($row["GUARD_KANA"], 0, 20);
                $row["GUARD_KANA"] = mb_convert_kana($guardKana, "C");
                
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.'"'.$row[$field].'"';
                    $sep = ",";
                }
                $contents .= "\n";
    
                $header_data = true;
            }
            $result->free();
        }

        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $code = ($this->field["SCHOOL_KIND"] == "H") ? "17" : ($this->field["SCHOOL_KIND"] == "J" ? "14" : "");
        $outputdiv = ($this->field["OUTPUT_DIV"] == "1") ? "shin" : ($this->field["OUTPUT_DIV"] == "2" ? "zai" : "sotsu");
        $date = date("YmdHis");
        $title = "henkan_{$code}_{$outputdiv}_{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //校友課データ
    public function getDownloadModel3()
    {
        $db = Query::dbCheckOut();
    
        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
    
        /********************/
        /*  データ部分出力  */
        /********************/
    
        $header = array();
        $header["SCHREGNO"]      = "学籍番号";
        $header["NAME"]          = "氏名（漢字）";
        $header["NAME_KANA"]     = "氏名（カナ）";
        $header["ZIPCD"]         = "郵便番号";
        $header["ADDR1"]         = "学生住所１";
        $header["ADDR2"]         = "学生住所２";
        $header["ADDR3"]         = "学生住所３";
        $header["TELNO"]         = "学生電話番号";
        $header["GUARD_NAME"]    = "保証人氏名（漢字）";
        $header["GUARD_KANA"]    = "保証人氏名（カナ）";
        $header["GUARD_ZIPCD"]   = "保証人郵便番号";
        $header["GUARD_ADDR1"]   = "保証人住所１";
        $header["GUARD_ADDR2"]   = "保証人住所２";
        $header["GUARD_ADDR3"]   = "保証人住所３";
        $header["GUARD_TELNO"]   = "保証人電話番号";
        $header["ENT_YEAR"]      = "入学年";
        $header["ENT_MONTH"]     = "入学月";
        $header["SEX"]           = "性別";
        $header["BIRTHDAY"]      = "生年月日";
        $header["FACULTYCD"]     = "学部コード";
        $header["DEPARTMENTCD"]  = "学科コード";
        $header["MAJORCD"]       = "専攻・専修コード";
        $header["SEMICD"]        = "ゼミコード";
        $header["ENT_FORMAT"]    = "入学形式";
        $header["GUARD_ATTR"]    = "保証人属性";
        $header["CLUB_CD1"]      = "クラブコード１";
        $header["CLUB_CD2"]      = "クラブコード２";
        $header["CLUB_CD3"]      = "クラブコード３";
    
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }
    
        /********************/
        /*  データ部分出力  */
        /********************/
        $query = knja352Query::selectCsvQuery($this);
    
        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT_DIV"] == "3" && $row["GRD_DIV"] != "1") {
                    continue;
                }

                //前処理

                $splitStrInto3Parts = function ($str, $firstLen, $secondLen, $thirdLen) {
                    //文字列を引数の文字数ごとに３つの部分文字列に分割
                    $start = 0;
                    $firstStr = mb_substr($str, $start, $firstLen);
                    $start += $firstLen;
                    $secondStr = mb_substr($str, $start, $secondLen);
                    $start += $secondLen;
                    $thirdStr = mb_substr($str, $start, $thirdLen);
                
                    return array($firstStr, $secondStr, $thirdStr);
                };

                $row["NAME"]          = mb_substr($row["NAME"], 0, 20);
                $nameKana = mb_substr($row["NAME_KANA"], 0, 20);
                $row["NAME_KANA"]     = str_replace(" ", "　", mb_convert_kana($nameKana, "C"));
                $row["ZIPCD"]         = str_replace("-", "", $row["ZIPCD"]);
                list($row["ADDR1"], $row["ADDR2"], $row["ADDR3"]) = $splitStrInto3Parts(str_replace("　", "", $row["ADDR1"]).$row["ADDR2"], 17, 17, 66);
                $row["GUARD_NAME"]    = mb_substr($row["GUARD_NAME"], 0, 20);
                $guardKana = mb_substr($row["GUARD_KANA"], 0, 20);
                $row["GUARD_KANA"] = mb_convert_kana($guardKana, "C");
                $row["GUARD_ZIPCD"]   = str_replace("-", "", $row["GUARD_ZIPCD"]);
                list($row["GUARD_ADDR1"], $row["GUARD_ADDR2"], $row["GUARD_ADDR3"]) = $splitStrInto3Parts(str_replace("　", "", $row["GUARD_ADDR1"]).$row["GUARD_ADDR2"], 17, 17, 66);
                $row["ENT_YEAR"]      = substr($row["ENT_DATE"], 0, 4);
                $row["ENT_MONTH"]     = substr($row["ENT_DATE"], 5, 2);
                $row["BIRTHDAY"]      = str_replace("-", "", $row["BIRTHDAY"]);
                $row["FACULTYCD"]     = ($this->field["SCHOOL_KIND"] == "H") ? "17" : ($this->field["SCHOOL_KIND"] == "J" ? "14" : "");
                $row["DEPARTMENTCD"]  = "00";
                $row["MAJORCD"]       = "";
                $row["SEMICD"]        = "";
                $row["ENT_FORMAT"]    = "";
                $row["GUARD_ATTR"]    = "";
                $row["CLUB_CD1"]      = "";
                $row["CLUB_CD2"]      = "";
                $row["CLUB_CD3"]      = "";
                                
                
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
    
                $header_data = true;
            }
            $result->free();
        }
    
        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $code = ($this->field["SCHOOL_KIND"] == "H") ? "kou" : ($this->field["SCHOOL_KIND"] == "J" ? "chu" : "");
        $outputdiv = ($this->field["OUTPUT_DIV"] == "1") ? "_shin_" : ($this->field["OUTPUT_DIV"] == "3" ? "_sotsu_" : "_");
        $date = date("YmdHis");
        $title = "koyu_{$code}{$outputdiv}{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //図書データ
    public function getDownloadModel4()
    {
        $db = Query::dbCheckOut();
    
        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        /********************/
        /*  データ部分出力  */
        /********************/
    
        $header = array();
        $header["USER_TYPE"]          = "利用者種別";
        $header["OLD_SCHREGNO"]       = "旧学籍番号";
        $header["NAME_KANA"]          = "学生氏名（カナ）";
        $header["NAME"]               = "学生氏名（漢字）";
        $header["ZIPCD"]              = "本人郵便番号";
        $header["ADDR"]               = "本人住所";
        $header["TELNO"]              = "本人電話番号";
        $header["QUALIFIED_CD"]       = "資格コード";
        $header["DEPARTMENTCD"]       = "部課コード";
        $header["ENT_DATE"]           = "入学年月日";
        $header["GRADE"]              = "学年";
        $header["HR_CLASS_CD"]        = "クラスコード";
        $header["HOUSECD"]            = "ハウスコード";
        $header["OLD_NEW_CD"]         = "新旧コード";
        $header["USER_ID"]            = "ユーザID";
        $header["SCHREGNO"]           = "学籍番号";
        $header["SCH_STATE_CD"]       = "学生状態コード";
        $header["SCH_DIV_CD"]         = "学生区分コード";
        $header["SCH_GRPDIV_CD"]      = "学生所属区分コード";
        $header["SCH_FACULTYCD"]      = "学生学部コード";
        $header["SCH_DEPARTMENTCD"]   = "学生学科コード";
        $header["SCH_MAJORCD"]        = "学生専攻・コースコード";
        $header["REISSUE_CNT"]        = "学生証再発行回数";
        $header["ENT_YEAR"]           = "学生入学年度";
        $header["GRD_PLAN_SEMESTER"]  = "学生新旧卒業予定学期コード";
        $header["REMAIN_KETTEI_DIV"]  = "学生留級決定区分";
        $header["GRD_DATE"]           = "学生卒業日付";
        $header["BIRTHDAY"]           = "学生生年月日";
        $header["ADDR_DISPNO"]        = "学生住所表示番号";
        $header["SCH_TELNO"]          = "学生携帯電話番号";
        $header["SCH_MAIL1"]          = "学生E-MAIL1";
        $header["SCH_MAIL2"]          = "学生E-MAIL2";
        $header["GUARD_NAME"]         = "保証人漢字氏名";
        $header["GUARD_KANA"]         = "保証人カナ氏名";
        $header["GUARD_ZIPCD"]        = "保証人郵便番号";
        $header["GUARD_ADDR"]         = "保証人住所";
        $header["GADDR_DISPNO"]       = "保証人住所表示番号";
        $header["GUARD_TELNO"]        = "保証人電話番号";
        $header["SCH_GTELNO"]         = "保証人携帯電話番号";
    
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }
    
        /********************/
        /*  データ部分出力  */
        /********************/
        $query = knja352Query::selectCsvQuery($this);
    
        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $getSchStateCd = function ($transferCd) {
                    //生徒の異動区分から学生状態コードを求める
                    if ($transferCd == "1") {
                        return "2";
                    } elseif ($transferCd == "2") {
                        return "1";
                    } elseif ($transferCd == "3") {
                        return "3";
                    } else {
                        return "0";
                    }
                };

                //前処理
                $row["USER_TYPE"]          = "2";
                $row["OLD_SCHREGNO"]       = "";
                $nameKana = mb_substr($row["NAME_KANA"], 0, 20);
                $row["NAME_KANA"]          = str_replace(" ", "　", mb_convert_kana($nameKana, "C"));
                $row["NAME"]               = mb_substr($row["NAME"], 0, 20);
                $row["ZIPCD"]              = str_replace("-", "", $row["ZIPCD"]);
                $row["ADDR"]               = str_replace("　", "", $row["ADDR1"]).$row["ADDR2"];
                $row["QUALIFIED_CD"]       = "";
                $row["DEPARTMENTCD"]       = ($this->field["SCHOOL_KIND"] == "H") ? "170" : ($this->field["SCHOOL_KIND"] == "J" ? "140" : "");
                $row["ENT_DATE"]           = str_replace("-", "", $row["ENT_DATE"]);
                $row["GRADE"]              = substr($row["GRADE"], 1, 2);
                $row["HR_CLASS_CD"]        = $row["HR_CLASS_NAME1"];
                $row["HOUSECD"]            = "";
                $row["OLD_NEW_CD"]         = "1";
                $row["USER_ID"]            = "";
                $row["SCH_STATE_CD"]       = $getSchStateCd($row["TRANSFERCD"]);
                $row["SCH_DIV_CD"]         = "";
                $row["SCH_GRPDIV_CD"]      = "";
                $row["SCH_FACULTYCD"]      = ($this->field["SCHOOL_KIND"] == "H") ? "17" : ($this->field["SCHOOL_KIND"] == "J" ? "14" : "");
                $row["SCH_DEPARTMENTCD"]   = "";
                $row["SCH_MAJORCD"]        = "";
                $row["REISSUE_CNT"]        = "";
                $row["ENT_YEAR"]           = substr($row["ENT_DATE"], 0, 4);
                $row["GRD_PLAN_SEMESTER"]  = "";
                $row["REMAIN_KETTEI_DIV"]  = "";
                $row["GRD_DATE"]           = str_replace("-", "", $row["GRD_DATE"]);
                $row["BIRTHDAY"]           = str_replace("-", "", $row["BIRTHDAY"]);
                $row["ADDR_DISPNO"]        = "";
                $row["SCH_TELNO"]          = "";
                $row["SCH_MAIL1"]          = "";
                $row["SCH_MAIL2"]          = "";
                $row["GUARD_NAME"]         = mb_substr($row["GUARD_NAME"], 0, 20);
                $guardKana = mb_substr($row["GUARD_KANA"], 0, 20);
                $row["GUARD_KANA"] = mb_convert_kana($guardKana, "C");
                $row["GUARD_ZIPCD"]        = str_replace("-", "", $row["GUARD_ZIPCD"]);
                $row["GUARD_ADDR"]         = str_replace("　", "", $row["GUARD_ADDR1"]).$row["GUARD_ADDR2"];
                $row["GADDR_DISPNO"]       = "";
                $row["SCH_GTELNO"]         = "";
                                        
                
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.'"'.$row[$field].'"';
                    $sep = ",";
                }
                $contents .= "\n";
    
                $header_data = true;
            }
            $result->free();
        }

        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $code = ($this->field["SCHOOL_KIND"] == "H") ? "kou" : ($this->field["SCHOOL_KIND"] == "J" ? "chu" : "");
        $date = date("YmdHis");
        $title = "tosho_{$code}_{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //カード搭載データ
    public function getDownloadModel5()
    {
        $db = Query::dbCheckOut();
    
        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        /********************/
        /*  データ部分出力  */
        /********************/
    
        $header = array();
        $header["SCHREGNO"]                = "学籍番号";
        $header["NAME"]                    = "漢字氏名";
        $header["NAME_KANA"]               = "カナ氏名";
        $header["NAME_ENG"]                = "英字氏名";
        $header["SEX"]                     = "性別";
        $header["ISSUE_DATE"]              = "発行日";
        $header["LIMIT_DATE"]              = "有効期限";
        $header["BIRTHDAY"]                = "生年月日";
        $header["ZAISEKI_STATE_DIV_NAME"]  = "在籍状態区分名";
        $header["ZAISEKI_STATE_DIV"]       = "在籍状態区分";
        $header["ENT_DATE"]                = "入学年月日";
        $header["EXAMNO"]                  = "受験番号";
        $header["SYSTEM_ID"]               = "システム利用ID";
        $header["MG_STRIPE_ID"]            = "磁気ストライプID";
        $header["MG_STRIPE_CD"]            = "磁気ストライプCD";
        $header["DEPARTMENTCD"]            = "学科コード";
        $header["MAJORCD"]                 = "専攻コード";
        $header["GRADE_CD_NAME"]           = "Gradeコード名";
    
        if ($this->field["HEADER"] == "on") {
            $contents .= implode(",", $header)."\n";
        }
    
        /********************/
        /*  データ部分出力  */
        /********************/

        if ($this->field["OUTPUT_DIV"] == "1" && $this->field["FRESHMAN_DIV"] == "1") {
            $query = knja352Query::selectExamCsvQuery($this);
        } else {
            $query = knja352Query::selectCsvQuery($this);
        }

        //SQL文発行
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $calcCheckDigit = function ($number) {
                    $digits = array_reverse(str_split($number));
                    $sum = 0;
                    foreach ($digits as $index => $digit) {
                        $tmpDigit = ($index % 2 == 0) ? $digit * 2 : $digit;
                        $tmpDigit = ($tmpDigit >= 10) ? 1 + $tmpDigit - 10 : $tmpDigit;
                        $sum += $tmpDigit;
                    }
                    $amari = $sum % 10;
                    return ($amari > 0) ? 10 - $amari : 0;
                };

                //前処理
                $stripePrefix = ($this->field["SCHOOL_KIND"] == "J") ? "1100" : ($this->field["SCHOOL_KIND"] == "H" ? "2100" : "");
                $row["NAME"]                        = mb_substr($row["NAME"], 0, 20);
                $nameKana = mb_substr($row["NAME_KANA"], 0, 20);
                $row["NAME_KANA"]                   = mb_convert_kana($nameKana, "C");
                $row["NAME_ENG"]                    = "";
                $row["ISSUE_DATE"]                  = str_replace("-", "/", $this->field["ISSUE_DATE"]);
                $row["LIMIT_DATE"]                  = str_replace("-", "/", $this->field["LIMIT_DATE"]);
                $row["BIRTHDAY"]                    = str_replace("-", "/", $row["BIRTHDAY"]);
                $row["EXAMNO"]                      = substr($row["EXAMNO"], -4);
                $row["SYSTEM_ID"]                   = "";
                if ($this->field["OUTPUT_DIV"] == "1" && $this->field["FRESHMAN_DIV"] == "1") {
                    $enterYear = $this->year + 1;
                    $row["ZAISEKI_STATE_DIV_NAME"]  = "通常";
                    $row["ZAISEKI_STATE_DIV"]       = "0";
                    $row["ENT_DATE"]                = "{$enterYear}/04/01";
                    $row["DEPARTMENTCD"]            = "";
                    $row["MAJORCD"]                 = "";
                    $row["GRADE_CD_NAME"]           = "";
                } else {
                    $enterYear = substr($row["ENT_DATE"], 0, 4);
                    $zaisekiStateDivName = "";
                    $zaisekiStateDiv = "";
                    if ($row["GRD_DIV"] == "") {
                        $zaisekiStateDivName = "通常";
                        $zaisekiStateDiv = 0;
                    } elseif ($row["GRD_DIV"] == "1") {
                        $zaisekiStateDiv = 1;
                        $zaisekiStateDivName = "卒業";
                    } elseif ($row["GRD_DIV"] == "2") {
                        $zaisekiStateDiv = 5;
                        $zaisekiStateDivName = "退学";
                    }
                    $row["ZAISEKI_STATE_DIV_NAME"]  = $zaisekiStateDivName;
                    $row["ZAISEKI_STATE_DIV"]       = $zaisekiStateDiv;
                    $row["ENT_DATE"]                = str_replace("-", "/", $row["ENT_DATE"]);
                    $row["DEPARTMENTCD"]            = $row["HR_CLASS_NAME1"];
                    $row["MAJORCD"]                 = $row["ATTENDNO"];
                    $row["GRADE_CD_NAME"]           = substr($row["GRADE"], 1, 2);
                }
                $row["MG_STRIPE_ID"]            = $stripePrefix.$enterYear.substr($row["SCHREGNO"], 5, 3);
                $stripePrefix2                  = ($this->field["SCHOOL_KIND"] == "H") ? "32100" : ($this->field["SCHOOL_KIND"] == "J" ? "31100" : "");
                $row["MG_STRIPE_CD"]            = $calcCheckDigit($stripePrefix2.substr($enterYear, 2, 2).substr($row["SCHREGNO"], 5, 3));
                          
                
                //ヘッダ順に値をセット
                $sep = "";
                foreach ($header as $field => $label) {
                    $contents .= $sep.'"'.$row[$field].'"';
                    $sep = ",";
                }
                $contents .= "\n";
    
                $header_data = true;
            }
            $result->free();
        }

        //改行コードを統一
        $contents = preg_replace("/\r\n|\r|\n/", "\r\n", $contents);

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //ファイル名
        $code = ($this->field["SCHOOL_KIND"] == "H") ? "kou" : ($this->field["SCHOOL_KIND"] == "J" ? "chu" : "");
        $date = date("YmdHis");
        $title = "card_{$code}_{$date}";

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
