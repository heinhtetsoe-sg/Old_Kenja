<?php

require_once('for_php7.php');

require_once("Date/Calc.php");
/**
 * 定数定義
*/

class knjb060Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $field2 = array();
    public $year;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knjb060Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
    }
    public function init()
    {
        $this->field = array("RADIO"               =>  VARS::post("RADIO"),     //時間割種別ラジオ
                            "KUBUN"                =>  VARS::post("KUBUN"),     //出力区分ラジオ
                            "DATE"                 =>  VARS::post("DATE"),     //指定日付テキスト１
                            "DATE2"                =>  VARS::post("DATE2"),     //指定日付テキスト２
                            "SECTION_CD_NAME1"     =>  VARS::post("SECTION_CD_NAME1"),     //所属コンボボックス１
                            "SECTION_CD_NAME2"     =>  VARS::post("SECTION_CD_NAME2"),     //所属コンボボックス２
                            "GRADE_HR_CLASS1"      =>  VARS::post("GRADE_HR_CLASS1"),     //学年／組コンボボックス１
                            "GRADE_HR_CLASS2"      =>  VARS::post("GRADE_HR_CLASS2"),     //学年／組コンボボックス２
                            "COURSE"               =>  VARS::post("COURSE"),              //課程学科コースコンボボックス
                            "GRADE_HR_CLASS3"      =>  VARS::post("GRADE_HR_CLASS3"),     //学年／組コンボボックス３
                            "GRADE_HR_CLASS4"      =>  VARS::post("GRADE_HR_CLASS4"),     //学年／組コンボボックス４
                            "FACCD_NAME1"          =>  VARS::post("FACCD_NAME1"),     //施設コンボボックス１
                            "FACCD_NAME2"          =>  VARS::post("FACCD_NAME2"),     //施設コンボボックス２
                            "TITLE"                =>  VARS::post("TITLE"),     //時間割コンボボックス選択データ
                            "TITLE2"               =>  VARS::post("TITLE2"),    //時間割コンボボックス選択データ2
                            "CHECK"                =>  VARS::post("CHECK"),     //授業が無い「校時」を詰める、詰めないのチェックボックス
                            "TEST_CHECK"           =>  VARS::post("TEST_CHECK"),//「テスト時間割のみ出力」チェックボックス---2006/11/01
                             "SUBCLASS_CHAIR_DIV"  =>  VARS::post("SUBCLASS_CHAIR_DIV"), //出力項目(科目名・講座名)
                            "STAFF_CHECK"          =>  VARS::post("STAFF_CHECK"),      //「職員は正担任（MAX職員番号）のみ出力」チェックボックス
                            "NO_CLASS_CHECK"       =>  VARS::post("NO_CLASS_CHECK"),   //「クラス名は出力しない」チェックボックス
                            "PRINT_CHECK"          =>  VARS::post("PRINT_CHECK"),      //Ａ週・Ｂ週出力チェックボックス
                            "PRINT_CHECK2"         =>  VARS::post("PRINT_CHECK2"),     //２週間出力チェックボックス
                            "NO_REQUIRE_FLG_CHECK" =>  VARS::post("NO_REQUIRE_FLG_CHECK"), //「科目名は必履修区分を出力しない」チェックボックス
                            "SUBCLASS_ABBV_CHECK"  =>  VARS::post("SUBCLASS_ABBV_CHECK"),  //「科目名または講座名を略称名で出力する」チェックボックス
                            "STAFF_ABBV_CHECK"     =>  VARS::post("STAFF_ABBV_CHECK"),     //「教員名を略称名(職員氏名表示用)で出力する」チェックボックス
                            "NO_STAFF_CHECK"       =>  VARS::post("NO_STAFF_CHECK"),   //「教員名は出力しない」チェックボックス
                            "HEIJITSU_CHECK"       =>  VARS::post("HEIJITSU_CHECK"),   //「土曜欄無しのフォームで出力する」チェックボックス
                            "DBNAME"               =>  VARS::post("DBNAME"),          //DB変更に備えて
                     );
        //年度（初期値）
        $this->year = ($this->year == "") ? CTRL_YEAR + 1 : $this->year;
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::post("YEAR");
        }

        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knjb060Query::getSchoolName()); //学校名取得
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        //CSVデータ出力用
        $contents = "";
        $lf = "\n";
        $data = false;
        $data2 = false;

        //年度・作成日
        $nendo      = common::DateConv1($this->year."/04/01", 10);
        $sakuseibi  = common::DateConv1(str_replace("-", "/", CTRL_DATE), 0);
        //出力区分
        $kubunName = "";
        if ($this->field["KUBUN"] == 1) {
            $kubunName = "職員";
        }
        if ($this->field["KUBUN"] == 2) {
            $kubunName = "学級";
        }
        if ($this->field["KUBUN"] == 3) {
            $kubunName = "生徒";
        }
        if ($this->field["KUBUN"] == 4) {
            $kubunName = "施設";
        }
        $mainTitle = $nendo . "　" . $kubunName . "別時間割表";

        //課程学科コース名を取得
        $courseName = "";
        if ($this->field["KUBUN"] == 2 && strlen($this->field["COURSE"])) { //学級別
            $courseRow = $db->getRow(knjb060Query::getCourse($this, $this->field["COURSE"]), DB_FETCHMODE_ASSOC);
            $courseName = "　" . $courseRow["CSV_LABEL"];
        }

        //パラメータ年度・学期・日付など
        $this->setCommonParam($db);
        //１行目：タイトルと作成日
        $subTitle = "";
        if ($this->field["RADIO"] == "1") { //基本
            $subTitle = "（" . $db->getOne(knjb060Query::getSubTitle($this)) . "）";
        }
        $titleContents = $mainTitle . $subTitle . $courseName . "　" . $sakuseibi . $lf;
        //ヘッダー取得
        $headerContents = $this->getHeaderContents($db);
        //出力データ取得
        $dataContents = $this->getDataContents($db);
        if ($dataContents != "") {
            $data = true;
        }

        //基本2
        if ($this->field["RADIO"] == "1" && strlen($this->field["TITLE2"]) ||
            $this->field["RADIO"] == "2" && strlen($this->field["PRINT_CHECK2"])) {
            //パラメータ年度・学期・日付など
            $this->setCommonParam($db, "2");
            //１行目：タイトルと作成日
            $subTitle2 = "";
            if ($this->field["RADIO"] == "1") { //基本
                $subTitle2 = "（" . $db->getOne(knjb060Query::getSubTitle($this)) . "）";
            }
            $titleContents2 = $mainTitle . $subTitle2 . $courseName . "　" . $sakuseibi . $lf;
            //ヘッダー取得
            $headerContents2 = $this->getHeaderContents($db);
            //出力データ取得
            $dataContents2 = $this->getDataContents($db);
            if ($dataContents2 != "") {
                $data2 = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSVデータ出力
        if ($data || $data2) {
            if ($data) {
                $contents .= $titleContents;
                $contents .= $headerContents;
                $contents .= $dataContents;
            }
            //基本2
            if ($data2) {
                $contents .= $titleContents2;
                $contents .= $headerContents2;
                $contents .= $dataContents2;
            }
            $filename = sprintf("%s.csv", $mainTitle);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    //パラメータ
    public function setCommonParam($db, $kihon2 = "")
    {
        $this->year = "";
        $this->semester = "";
        $this->bscseq = "";
        $this->sdate = "";
        $this->edate = "";
        //時間割種別 1:基本時間割 2:通常時間割
        if ($this->field["RADIO"] == "1") { //基本
            list($year, $bscseq, $semester) = explode(",", $this->field["TITLE".$kihon2]);
            $this->year = $year;
            $this->semester = $semester;
            $this->bscseq = $bscseq;
        } else { //通常
            //指定日を含む指定週の開始日(月曜日)と終了日(土曜日)を取得
            common::DateConv2($this->field["DATE"], $OutDate1, $OutDate2, 1);
            $sdate = str_replace("/", "-", $OutDate1);
            $edate = str_replace("/", "-", $OutDate2);
            //２週間出力対応・・・７日後取得
            if (strlen($kihon2)) {
                $sdate = $db->getOne("VALUES ADD_DAYS(DATE('{$sdate}'), 7)");
                $edate = $db->getOne("VALUES ADD_DAYS(DATE('{$edate}'), 7)");
            }
            //指定日の学期情報を取得
            $query = knjb060Query::getSemesterInfo(str_replace("/", "-", $this->field["DATE"]), CTRL_YEAR);
            $semRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($semRow["SEMESTER"])) {
                $year = $semRow["YEAR"];
                $semester = $semRow["SEMESTER"];
                //学期範囲外の時、学期開始日から　または　学期終了日まで
                if ($sdate < $semRow["SDATE"]) {
                    $sdate = $semRow["SDATE"];
                }
                if ($edate > $semRow["EDATE"]) {
                    $edate = $semRow["EDATE"];
                }
            } else {
                $year = CTRL_YEAR;
                $semester = "0";
            }
            $this->year = $year;
            $this->semester = $semester;
            $this->sdate = $sdate;
            $this->edate = $edate;
        }
    }
    //ヘッダー
    public function getHeaderContents($db)
    {
        $headerContents = "";
        $lf = "\n";
        $comma = ",";

        $note = "";

        //職員別
        if ($this->field["KUBUN"] == 1) {
            $note = "所属コード" . $comma . "所属名" . $comma . "職員コード" . $comma . "職員名";
        }
        //学級別
        if ($this->field["KUBUN"] == 2) {
            $note = "年組";
        }
        //生徒別
        if ($this->field["KUBUN"] == 3) {
            $note = "年組番" . $comma . "学籍番号" . $comma . "氏名";
        }
        //施設別
        if ($this->field["KUBUN"] == 4) {
            $note = "施設コード" . $comma . "施設名";
        }

        $week = array("月","火","水","木","金","土");
        if ($this->field["RADIO"] == "2") { //通常
            $array = explode("-", $this->sdate);
        }
        $md = "";

        $headerContents .= $note . $comma;
        $headerContents .= "校時" . $comma;
        for ($len = 0; $len < 6; $len++) {
            if ($this->field["RADIO"] == "2") { //通常
                $md = Date_Calc::daysToDate(Date_Calc::dateToDays((int)$array[2] + $len, $array[1], $array[0]), "%m/%d");
                $md = "(" . $md . ")";
            }
            if ($len == 5 && $this->field["HEIJITSU_CHECK"] == "on") {
                $headerContents .= $lf;
                continue;
            }
            $headerContents .= $week[$len] . $md;
            $headerContents .= ($len == 5) ? $lf : $comma;
        }

        return $headerContents;
    }

    //出力データ取得
    public function getDataContents($db)
    {
        $dataContents = "";
        $lf = "\n";
        $comma = ",";

        $note = "";
        $kaipage = "";
        $kaipageRow = "";
        $periodcd = "";
        $daycd = "";
        $period_name = "";
        $gyo_max = 0;
        $gyo_tmp = 0;
        $day_sub_stf = array();

        if ($this->field["KUBUN"] == 1 || $this->field["KUBUN"] == 4) {
            //職員・施設別csv
            $query = knjb060Query::getTeacherFacilityCsv($this);
        } else {
            //学級・生徒別csv
            $query = knjb060Query::getClassStudentCsv($this, 1);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //職員別
            if ($this->field["KUBUN"] == 1) {
                $kaipageRow = $row["STAFFCD"];
            }
            //学級別
            if ($this->field["KUBUN"] == 2) {
                $kaipageRow = $row["GRADE"] . $row["HR_CLASS"];
            }
            //生徒別
            if ($this->field["KUBUN"] == 3) {
                $kaipageRow = $row["SCHREGNO"];
            }
            //施設別
            if ($this->field["KUBUN"] == 4) {
                $kaipageRow = $row["FACCD"];
            }
            if ($kaipage != $kaipageRow && $kaipage != "" ||
                $periodcd != $row["PERIODCD"] && $periodcd != "" ||
                $daycd != $row["DAYCD"] && $daycd != "") {
                if ($gyo_tmp > $gyo_max) {
                    $gyo_max = $gyo_tmp;
                }
                $gyo_tmp = 0;
                if ($kaipage != $kaipageRow && $kaipage != "" ||
                    $periodcd != $row["PERIODCD"] && $periodcd != "") {
                    //出力
                    $dataContents .= $this->setTimeTable($gyo_max, $day_sub_stf, $periodcd, $period_name, $note);
                    $day_sub_stf = array();
                    $gyo_max = 0;
                }
            }
            $day_sub_stf[(int)$row["DAYCD"]-2][$gyo_tmp*3] = $row["TARGETCLASS"];
            $day_sub_stf[(int)$row["DAYCD"]-2][$gyo_tmp*3+1] = $row["SUBCLASSNAME"];
            $day_sub_stf[(int)$row["DAYCD"]-2][$gyo_tmp*3+2] = $row["STAFFNAME"];

            $kaipage = $kaipageRow;
            $periodcd = $row["PERIODCD"];
            $daycd = $row["DAYCD"];
            $period_name = $row["PERIOD_NAME"];
            $gyo_tmp++;

            //職員別
            if ($this->field["KUBUN"] == 1) {
                $note = $row["SECTIONCD"] . $comma . $row["SECTIONNAME"] . $comma . $row["STAFFCD"] . $comma . $row["STAFFNAME"];
            }
            //学級別
            if ($this->field["KUBUN"] == 2) {
                $note = $row["TARGETCLASS"];
            }
            //生徒別
            if ($this->field["KUBUN"] == 3) {
                $note = $row["TARGETCLASS"] . "-" . (int) $row["ATTENDNO"] . "番" . $comma . $row["SCHREGNO"] . $comma . $row["NAME"];
            }
            //施設別
            if ($this->field["KUBUN"] == 4) {
                $note = $row["FACCD"] . $comma . $row["FACILITYNAME"];
            }
        }
        $result->free();
        if ($kaipage != "") {
            if ($gyo_tmp > $gyo_max) {
                $gyo_max = $gyo_tmp;
            }
            //出力
            $dataContents .= $this->setTimeTable($gyo_max, $day_sub_stf, $periodcd, $period_name, $note);
            $day_sub_stf = array();
        }

        return $dataContents;
    }
    public function setTimeTable($gyo_max, $day_sub_stf, $periodcd, $period_name, $note)
    {
        $lf = "\n";
        $comma = ",";
        $dataContents = "";
        for ($max_len = 0; $max_len < ($gyo_max * 3); $max_len++) {
            $dataContents .= $note . $comma;
            $dataContents .= $period_name . $comma;
            for ($len = 0; $len < 6; $len++) {
                if ($len == 5 && $this->field["HEIJITSU_CHECK"] == "on") {
                    $dataContents .= $lf;
                    continue;
                }
                $dataContents .= (strlen($day_sub_stf[$len][$max_len])) ? $day_sub_stf[$len][$max_len] : "";
                $dataContents .= ($len == 5) ? $lf : $comma;
            }
        }
        return $dataContents;
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
