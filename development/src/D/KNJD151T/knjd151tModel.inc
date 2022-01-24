<?php

require_once('for_php7.php');

class knjd151tModel extends Model
{
    public $field      = array();
    public $testfield  = array();
    public $selectdata;
    public $cmd;
    public $control;
    public $staffcd;
    public $testTable;
    public $lastColumn;//CSVの最後の列の値

    public function knjd151tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト名/* NO001 */
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DEVIATION"        =>  VARS::post("DEVIATION"),        //偏差値出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $intr_term      = array("0101" => "_INTR", "0201" => "_TERM", "0202" => "_TERM2");
        $score_field    = "SEM" .$this->field["GAKKI"] .$intr_term[$this->field["TESTKINDCD"]] ."_SCORE";
        $chair_field    = "SEM" .$this->field["GAKKI"] .$intr_term[$this->field["TESTKINDCD"]] ."_CHAIRCD";
        $this->testfield = array("SCORE_FIELD"  =>  $score_field,
                                 "CHAIR_FIELD"  =>  $chair_field
                         );

        $this->selectdata  = VARS::post("selectdata");
        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    // ＣＳＶ出力
    public function getDownloadModel()
    {
        //ヘッダ情報の配列を取得
        $header1 = $this->getHeaderInfo1();//１行目
        $header2 = $this->getHeaderInfo2();//２行目

        //講座内の配列を取得
        $chair_average  = knjd151tQuery::getChairAverage($this);//生徒数・平均
        $chair_rank     = knjd151tQuery::getChairRank($this, $chair_average);//成績順位

        //遅刻何回で欠課とするかの指数取得
        $query      = knjd151tQuery::sqlSchoolAbsentCov($this->field["YEAR"]);
        $absentcov  = knjd151tQuery::sqlGetRow($query);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //出欠集計開始日付などを取得
        $attend_sdate       = "";
        $attend_seme_month  = array();
        $query  = knjd151tQuery::sqlAttendDate($this->field["YEAR"], str_replace("/", "-", $this->field["DATE"]));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend_sdate           = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            $attend_seme_month[]    = $row["SEMESTER"] .$row["MONTH"];
        }
        $result->free();
        if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".$this->field["YEAR"]."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);//学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);//次の日
        }

        //成績データなどを取得
        $header_data = false;
        $contents = $contents2 = "";
        $query = knjd151tQuery::sqlRecordAttend($this, $absentcov["ABSENT_COV"], $absentcov["ABSENT_COV_LATE"], $attend_seme_month, $attend_sdate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            //講座内情報
            $row = $this->setChairAverageRank($row, $chair_average, $chair_rank);
            //累積情報（０は出力しない）
            $row = $this->setAttendNonZero($row);
            //成績情報
            foreach ($header2 as $key => $val) {
                $contents2 .= $row[$key] .",";
            }
            $contents2 .= "\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        $contents .= implode($header1, ",")."\n";
        $contents .= implode($header2, ",")."\n";
        $contents .= $contents2;

        // ＣＳＶ出力
        if ($header_data) {
            $wareki_year = common::DateConv1($this->control["学籍処理日"], 10);
            $title = "成績個人票_" .$wareki_year;
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //ヘッダ情報（１行目）を取得
    public function getHeaderInfo1()
    {
        $query      = knjd151tQuery::sqlTrName($this->field["YEAR"], $this->field["GAKKI"], $this->field["GRADE_HR_CLASS"]);
        $tr_name    = knjd151tQuery::sqlGetRow($query);//担任：
        $query      = knjd151tQuery::sqlTestItem($this->field["YEAR"], $this->field["GAKKI"], $this->field["TESTKINDCD"], $this);
        $testname   = knjd151tQuery::sqlGetRow($query);//講座：
        $ctrl_date  = common::DateConv1($this->control["学籍処理日"], 0);
        //ヘッダ１行目
        $header  = array();
        $header  = array("TR_NAME"      => "担任：",
                         "TR_NAME1"     => $tr_name["TR_NAME1"],
                         "TR_NAME2"     => $tr_name["TR_NAME2"],
                         "TR_NAME3"     => $tr_name["TR_NAME3"],
                         "BLANK1"       => "",
                         "P_DATE"       => "累積情報：",
                         "P_DATE1"      => $this->field["DATE"] ."現在",
                         "BLANK2"       => "",
                         "TESTNAME"     => "講座：",
                         "TESTNAME1"    => $testname["SEMESTERNAME"] .$testname["TESTITEMNAME"],
                         "BLANK3"       => "",
                         "CTRL_DATE"    => "作成日：",
                         "CTRL_DATE1"   => $ctrl_date);
        return $header;
    }

    //ヘッダ情報（２行目）を取得
    public function getHeaderInfo2()
    {
        //テスト名取得
        $db = Query::dbCheckOut();
        $query = knjd151tQuery::getTestName($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testname[$row["VALUE"]] = $row["TESTITEMNAME"];
        }
        Query::dbCheckIn($db);

        //ヘッダ２行目
        $header  = array();
        if ($this->field["DEVIATION"] != "") { //偏差値欄を出力する
            $header  = array(
                         "HR_NAME"          => "年組",
                         "ATTENDNO"         => "出席番号",
                         "NAME"             => "氏名",
                         "SUBCLASSNAME"     => "科目",
                         "CREDITS"          => "単位",
                         "SEM1_INTR_SCORE"  => "前期－".$testname[10101]."－素点",
                         "SEM1_INTR_VALUE"  => "前期－".$testname[10101]."－評価",
                         "SEM1_TERM_SCORE"  => "前期－".$testname[10201]."－素点",
                         "SEM1_TERM_VALUE"  => "前期－".$testname[10201]."－評価",
                         "SEM1_VALUE"       => "前期評価",
                         "SEM2_INTR_SCORE"  => "後期－".$testname[20101]."－素点",
                         "SEM2_INTR_VALUE"  => "後期－".$testname[20101]."－評価",
                         "SEM2_TERM_SCORE"  => "後期－".$testname[20201]."－素点",
                         "SEM2_TERM_VALUE"  => "後期－".$testname[20201]."－評価",
                         "SEM2_TERM2_SCORE" => "後期－".$testname[20202]."－素点",
                         "SEM2_TERM2_VALUE" => "後期－".$testname[20202]."－評価",
                         "SEM2_VALUE"       => "後期評価",
                         "GRAD_VALUE"       => "学年評定",
                         "ABSENT"           => "欠時数",
                         "LATE_EARLY"       => "遅早数",
                         "ABSENT2"          => "欠課数",
                         "SCHNUM"           => "生徒数",
                         "RANK"             => "席次",
                         "AVERAGE"          => "平均",
                         "HENSACHI"         => "偏差値",
                         "CHAIRNAME"        => "講座名称",
                         $this->lastColumn  => $this->lastColumn);
        } else { //偏差値欄を出力しない
            $header  = array(
                         "HR_NAME"          => "年組",
                         "ATTENDNO"         => "出席番号",
                         "NAME"             => "氏名",
                         "SUBCLASSNAME"     => "科目",
                         "CREDITS"          => "単位",
                         "SEM1_INTR_SCORE"  => "前期－".$testname[10101]."－素点",
                         "SEM1_INTR_VALUE"  => "前期－".$testname[10101]."－評価",
                         "SEM1_TERM_SCORE"  => "前期－".$testname[10201]."－素点",
                         "SEM1_TERM_VALUE"  => "前期－".$testname[10201]."－評価",
                         "SEM1_VALUE"       => "前期評価",
                         "SEM2_INTR_SCORE"  => "後期－".$testname[20101]."－素点",
                         "SEM2_INTR_VALUE"  => "後期－".$testname[20101]."－評価",
                         "SEM2_TERM_SCORE"  => "後期－".$testname[20201]."－素点",
                         "SEM2_TERM_VALUE"  => "後期－".$testname[20201]."－評価",
                         "SEM2_TERM2_SCORE" => "後期－".$testname[20202]."－素点",
                         "SEM2_TERM2_VALUE" => "後期－".$testname[20202]."－評価",
                         "SEM2_VALUE"       => "後期評価",
                         "GRAD_VALUE"       => "学年評定",
                         "ABSENT"           => "欠時数",
                         "LATE_EARLY"       => "遅早数",
                         "ABSENT2"          => "欠課数",
                         "SCHNUM"           => "生徒数",
                         "RANK"             => "席次",
                         "AVERAGE"          => "平均",
                         "CHAIRNAME"        => "講座名称",
                         $this->lastColumn  => $this->lastColumn);
        }
        return $header;
    }

    //講座内情報をセット
    public function setChairAverageRank($row, $chair_average, $chair_rank)
    {
        $score_field = $this->testfield["SCORE_FIELD"];
        $chair_field = $this->testfield["CHAIR_FIELD"];
        //講座内
        if ($row[$score_field] != "") {
            $key1 = $row["SUBCLASSCD"] ."-" .$row[$chair_field];
            $avgdata = array();
            $avgdata = explode("-", $chair_average[$key1]);
            $row["SCHNUM"]      = $avgdata[0];
            $row["AVERAGE"]     = $avgdata[1];
            $row["CHAIRNAME"]   = $avgdata[2];
            $rnkdata = array();
            $rnkdata = explode("-", $chair_rank[$key1][$row[$score_field]]);
            $row["RANK"]        = $rnkdata[0];
            $row["HENSACHI"]    = $rnkdata[1];
        }
        return $row;
    }

    //累積情報をセット
    public function setAttendNonZero($row)
    {
        //０は出力しない
        $row["ABSENT"]      = (0 < $row["ABSENT"]) ? $row["ABSENT"] : "";
        $row["LATE_EARLY"]  = (0 < $row["LATE_EARLY"]) ? $row["LATE_EARLY"] : "";
        $row["ABSENT2"]     = (0 < $row["ABSENT2"]) ? $row["ABSENT2"] : "";
        return $row;
    }
}
