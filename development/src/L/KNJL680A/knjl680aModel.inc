<?php

require_once('for_php7.php');
class knjl680aModel extends Model
{
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $selected_data;
    public $goukaku;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $testdiv;
    public $schoolName;
    public $field;

    public function knjl680aModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->examyear);
            unset($this->applicantdiv);
            unset($this->testdiv);
            unset($this->totalcd);
            unset($this->testsubclasscd);
        }

        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        $temp = VARS::request("ENTEXAMYEAR");
        if (isset($temp)) {
            $this->examyear = VARS::request("ENTEXAMYEAR");
        }
        $temp = VARS::request("APPLICANTDIV");
        if (isset($temp)) {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }
        $temp = VARS::request("SELECTED_DATA");
        if (isset($temp)) {
            $this->selectedTestDiv = explode(",", VARS::request("SELECTED_DATA"));
        }
        $temp = VARS::request("SELECTED_DATA_LABEL");
        if (isset($temp)) {
            $this->selectedTestDivLabel = explode(",", VARS::request("SELECTED_DATA_LABEL"));
        }

        $this->field = array(
                     "HEADER"           => VARS::post("HEADER"),          //ヘッダー
                     "OUTPUTDIV"        => VARS::post("OUTPUTDIV"),       // 出力対象
                     "OUTPUTDIV3TEMPL"  => VARS::post("OUTPUTDIV3TEMPL"), // テンプレート
                     "OUTPUTSCORE"      => VARS::post("OUTPUTSCORE"),     // 成績も出力
                     "APPLICANTDIV"     => $this->applicantdiv
                  );
        //入試制度が初期値でセットされていない場合
        if ($this->applicantdiv == "") {
            $db = Query::dbCheckOut();
            $appNamespare = $db->getOne(knjl680aQuery::getNameSpare2($this, "L003"));
            $this->applicantdiv = $db->getOne(knjl680aQuery::getNameMstSet($this, "L003", $appNamespare));
            Query::dbCheckIn($db);
        }


        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //テスト科目コードの配列
        $query = knjl680aQuery::getTestSubclass($this);
        $result = $db->query($query);
        $this->testCdArr = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->testCdArr[] = $row["NAMECD2"];
        }

        /**************/
        /*  ヘッダー  */
        /**************/
        $header = array();
        $header["APPLICANTDIV"]      = "入試制度";
        $header["APPLICANTDIV_NAME"] = "入試制度名称";
        $header["TESTDIV"]           = "入試区分";
        $header["TESTDIV_NAME"]      = "入試区分名称";
        $header["EXAMNO"]            = "受験番号";
        $header["NAME"]              = "氏名";
        $header["NAME_KANA"]         = "氏名フリガナ";
        $header["SEX"]               = "性別";
        $header["ZIPCD"]             = "郵便番号";
        $header["ADDRESS1"]          = "住所";
        $header["ADDRESS2"]          = "住所2";
        $header["FS_CD"]             = "出身校";
        $header["FINSCHOOL_NAME"]    = "出身校名称";
        $header["FINSCHOOL_PREF_CD"]    = "出身校_県CD";
        $header["FINSCHOOL_PREF_NAME"]  = "出身校_県名称";
        $header["SEAT_NO"]           = "座席番号";
        if ($this->applicantdiv == "2") {
            $header["ENNOU_FLG"]       = "延納希望フラグ";
            $header["ENNOU_FLG_NAME"]  = "延納希望フラグ名称";
        }
        $header["SIBOU1_EXAMCOURSECD"]    = "第1志望コース";
        $header["SIBOU1_EXAMCOURSE_NAME"] = "第1志望コース名称";
        $header["SIBOU2_EXAMCOURSECD"]    = "第2志望コース";
        $header["SIBOU2_EXAMCOURSE_NAME"] = "第2志望コース名称";
        if ($this->applicantdiv == "2") {
            $header["SP_ACT_FLG"]       = "特別活動フラグ";
        }
        $header["KIKOKU_FLG"]           = "帰国生フラグ";
        $header["KIKOKU_FLG_NAME"]      = "帰国生フラグ名称";
        $header["QUALIFIED_ENG_CD"]     = "英検取得級";
        $header["QUALIFIED_ENG_NAME"]   = "英検取得級名称";
        if ($this->applicantdiv == "2") {
            $header["RDET007RMK5"]       = "寮or通学";
            $header["TUGAKU_DIV_NAME"]   = "寮or通学名称";
        }
        $header["GNAME"]                = "保護者名";
        $header["GKANA"]                = "保護者フリガナ";
        $header["GTELNO"]               = "保護者電話番号";
        $header["EMERGENCYTELNO"]       = "緊急連絡先";
        $header["RELATIONSHIP"]         = "続柄";
        $header["RELATIONSHIP_NAME"]    = "続柄名称";
        $header["SIBOU1_SCHOOL_CD"]     = "第1志望校";
        $header["SIBOU1_SCHOOL_NAME"]   = "第1志望校名称";
        $header["SIBOU2_SCHOOL_CD"]     = "第2志望校";
        $header["SIBOU2_SCHOOL_NAME"]   = "第2志望校名称";
        $header["SIBOU3_SCHOOL_CD"]     = "第3志望校";
        $header["SIBOU3_SCHOOL_NAME"]   = "第3志望校名称";
        if ($this->applicantdiv == "2") {
            $header["TOTAL5"]                = "調査書5科合計";
            $header["ABSENCE_DAYS3"]         = "欠席日数";
            $header["ABSENCE_DAYS3_NAME"]    = "欠席日数名称";
            $header["SPECIALACTREC"]         = "生徒会活動・クラス活動";
            $header["SPECIALACTREC_NAME"]    = "生徒会活動・クラス活動名称";
            $header["TOTALSTUDYTIME"]        = "特別活動推薦";
            $header["TOTALSTUDYTIME_NAME"]   = "特別活動推薦名称";
        }
        foreach ($this->testCdArr as $key => $val) {
            $tmp = $key + 1;
            $header["SUBNAME{$key}"]          = "受験科目名称{$tmp}";
            $header["SUBSCORE{$key}"]         = "科目得点{$tmp}";
            $header["SUBSCORE{$key}_2"]       = "科目得点{$tmp}(傾斜配点込み)";
        }
        $header["TOTAL_SCORE"]       = "総得点";
        $header["TOTAL4"]            = "総得点(傾斜配点込み)";
        $header["TOTAL_RANK4"]       = "総合点順位";
        $header["JUDGEDIV"]          = "合否";
        $header["JUDGEDIV_NAME"]     = "合否名称";
        $header["PASS_COURSE_CD"]    = "合格コース";
        $header["PASS_COURSE_NAME"]  = "合格コース名称";
        $header["SP_SCHOLAR_DIV"]    = "特別奨学種別";
        $header["SP_SCHOLAR_NAME"]   = "特別奨学種別名称";
        if ($this->applicantdiv == "2") {
            $header["APPLICANT_METHOD"]       = "出願方式";
            $header["APPLICANT_METHOD_NAME"]  = "出願方式名称";
            $header["FS_GRDDIV"]              = "中学卒業区分";
            $header["FS_GRDDIV_NAME"]         = "中学卒業区分名称";
        }
        $header["PRISCHOOLCD"]       = "塾コード";
        $header["PRISCHOOL_NAME"]    = "塾名称";
        $header["BDET009RMK1"]       = "予備フィールド1";
        $header["BDET009RMK2"]       = "予備フィールド2";
        $header["BDET009RMK3"]       = "予備フィールド3";
        $header["BDET009RMK4"]       = "予備フィールド4";
        $header["BDET009RMK5"]       = "予備フィールド5";
        $header["RDET009RMK1"]       = $this->applicantdiv == "2" ? "面接番号" : "予備フィールド1(入試区分ごとに管理する項目。)";
        $header["RDET009RMK2"]       = "予備フィールド2(入試区分ごとに管理する項目。)";
        $header["RDET009RMK3"]       = "予備フィールド3(入試区分ごとに管理する項目。)";
        $header["RDET009RMK4"]       = "予備フィールド4(入試区分ごとに管理する項目。)";
        $header["RDET009RMK5"]       = "予備フィールド5(入試区分ごとに管理する項目。)";
        $header["UPDATED"]           = "志願者データ更新日";


        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        //試験区分の数だけ同じヘッダを横並びにする
        if ($this->field["OUTPUTDIV"] == "2") {
            $header2comma = "";
            $sep = "";
            for ($i = 0; $i < get_count($this->selectedTestDiv); $i++) {
                $header2comma .= $sep.implode(",", $header);
                $sep = ",";
            }
        }

        // データ出力
        if ($this->field["HEADER"] == "on") {
            $contents .= ($this->field["OUTPUTDIV"] == "1") ? implode($header, ",") : $header2comma ;
            $contents .= "\n" ;
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        $query = "";
        $dupArray = array();
        if ($this->applicantdiv == "2") {
            $query = knjl680aQuery::selectJudgement($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dupArray[$row["EXAMNO"]] = $row;
            }
            $result->free();
        }

        $query = knjl680aQuery::selectMainQuery($this);

        //SQL文発行
        $beforeExamno = "";
        $dataContents = "";
        if ($query) {
            if ($this->field["OUTPUTDIV"] == "1") {
                $dataContents = $this->convQueryToComma1($db, $header, $query, $dupArray);
            } elseif ($this->field["OUTPUTDIV"] == "2") {
                $dataContents = $this->convQueryToComma2($db, $header, $query, $dupArray);
            }
        }

        $contents .= $dataContents;
        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl680aQuery::getVNameMst($this, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $TestdivName = "";

        //指定された試験回が1つのみの時、ファイル名に試験回を追加
        if (get_count($this->selectedTestDiv) == 1) {
            $namecd1 = ($this->applicantdiv == "1") ? "L024" : "L004";
            $TestdivName  = $db->getOne(knjl680aQuery::getVNameMst($this, $namecd1, $this->selectedTestDiv[0]));
            $TestdivName .= '_';
        }
        //ファイル名
        $title = $this->examyear.'年度_'.$AppName."_".$TestdivName.'志願者データ';

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if (dataContents != "") {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //出力データを作成(通常Ver)
    public function convQueryToComma1($db, $header, $query, $dupArray)
    {
        $dataContents = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // for ($i = 0; i < count($csvRowArray); $i++) {
            //$row = $csvRowArray[$i];
            //カンマ変換
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));
            if ($this->applicantdiv == "2") {
                if ($row["PASS_COURSE_CD"] == "") {
                    $row["PASS_COURSE_CD"] = $dupArray[$row["EXAMNO"]]["PASS_COURSE_CD"];
                }
                if ($row["PASS_COURSE_NAME"] == "") {
                    $row["PASS_COURSE_NAME"] = $dupArray[$row["EXAMNO"]]["PASS_COURSE_NAME"];
                }
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $dataContents .= $sep.$row[$field];
                $sep = ",";
            }
            $dataContents .= "\n";
        }
        $result->free();
        return $dataContents;
    }

    //出力データを作成(複数入試区分を1行で出力)
    public function convQueryToComma2($db, $header, $query, $dupArray)
    {
        $dataContents = "";
        $befExamno = "";
        $sep = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //$row = $csvRowArray[$i];
            //カンマ変換
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));
            if ($this->applicantdiv == "2") {
                if ($row["PASS_COURSE_CD"] == "") {
                    $row["PASS_COURSE_CD"] = $dupArray[$row["EXAMNO"]]["PASS_COURSE_CD"];
                }
                if ($row["PASS_COURSE_NAME"] == "") {
                    $row["PASS_COURSE_NAME"] = $dupArray[$row["EXAMNO"]]["PASS_COURSE_NAME"];
                }
            }

            if ($befExamno != "" && $befExamno != $row["EXAMNO"]) {
                $dataContents .= "\n";
                $sep = "";
            }

            if ($row["NOT_TAKEN"] == "1") {                                 //指定した試験区分のレコードが存在しない場合、その分を空白で詰める
                $dataContents .= $sep.str_repeat(",", get_count($header) - 1);
            } else {
                $dataContents .= $sep.implode(",", array_slice($row, 2));   //最初の2列(TESTDIV2, NOT_TAKEN)は制御用なので除外
            }
            $sep = ",";
            $befExamno = $row["EXAMNO"];
        }
        $result->free();

        return $dataContents;
    }
}
