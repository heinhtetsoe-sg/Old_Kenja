<?php

require_once('for_php7.php');

class knjl692iModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $examType = 1;
    public $applicantDiv = '2'; // 2:高校 固定

    public function knjl692iModel()
    {
    }

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->field = array("GAKKA"             =>  VARS::post("GAKKA"),          //学科
                             "TESTDIV"           =>  VARS::post("TESTDIV"),        //入試区分
                             "RUIBETSU"          =>  VARS::post("RUIBETSU"),       //類別
                             "EXAMNO_FROM"       =>  VARS::post("EXAMNO_FROM"),    //受験番号（開始）
                             "EXAMNO_TO"         =>  VARS::post("EXAMNO_TO"),      //受験番号（修了）
                             "SEX"               =>  VARS::post("SEX"),            //性別
                             "ORDER"             =>  VARS::post("ORDER"),          //出力順
                             "OUTPUT"            =>  VARS::post("OUTPUT"),         //帳票種類
                             "OUTPUT_DIV"        =>  VARS::post("OUTPUT_DIV")      //帳票首里 種別
                            );

        $this->cmd = VARS::request("cmd");
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;

        $db = Query::dbCheckOut();

        $generalCourseList = array();
        $technicalCourseList = array();

        //ヘッダ（書出）   フィールド => ヘッダ名
        if ($this->field["OUTPUT"] == "1") {
            $csvItem = array("EXAMNO"            => array("受験番号"),
                             "NAME"              => array("氏名"),
                             "NAME_KANA"         => array("カナ氏名"),
                             "SEX"               => array("性別"),
                             "DESIRE_COURSE_CD"  => array("志望コース"),
                             "CONSUL_COURSE_CD"  => array("相談コース"),
                             "FS_CD"             => array("中学校"),
                             "FINSCHOOL_NAME"    => array("中学校名"),
                             "COMMON_TEST"       => array("共通テスト"),
                             "ABSENCE_DAYS"      => array("欠席日数"),
                             "SP_REASON"         => array("特待理由"),
                             "REMARK"            => array("備考")
            );
        } elseif ($this->field["OUTPUT"] == "2") {
            $csvItem = array("NO"                  => "NO",
                             "FS_CD"               => "中学校コード",
                             "FINSCHOOL_NAME_ABBV" => "中学校名"
            );

            // 普通科の類別を取得
            $query = knjl692iQuery::getConsulCourse($this, "1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                $csvItem += array("GENERAL_COURSE".$row["GENERAL_CD"] => $row["GENERAL_NAME"]);
                $generalCourseList[] = $row["GENERAL_CD"];
            }
            $result->free();

            // 工業科の類別を取得
            $query = knjl692iQuery::getConsulCourse($this, "2");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                $csvItem += array("TECHNICAL_COURSE".$row["GENERAL_CD"] => $row["GENERAL_NAME"]);
                $technicalCourseList[] = $row["GENERAL_CD"];
            }
            $result->free();

            $csvItem += array("TOTAL" => "統計");
        }
        $contents = "";

        //タイトル設定
        $title = "";
        if ($this->field["OUTPUT"] == "1") {
            $title = "願書リスト";
        } elseif ($this->field["OUTPUT"] == "2") {
            $query = knjl692iQuery::getWarekiAbbv($this);
            $record = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $wareki = $record["ABBV1"].sprintf("%02d", $record["YEAR"]);
            $query = knjl692iQuery::getTestDivMst($this, $this->field["TESTDIV"]);
            $record = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $testDivName = $record["TESTDIV_NAME"];
            $title = $wareki." ".$testDivName." 出願中学校リスト";
        }
        
        //ヘッダ取得
        if ($this->field["OUTPUT"] == "1") {
            $header = "";
            foreach ($csvItem as $key => $val) {
                $header .= ($header == "") ? $val[0] : ",".$val[0];
            }
            $contents .= $header."\n";
        } elseif ($this->field["OUTPUT"] == "2") {
            $header = ",,,普通科";
            $header .= str_repeat(",", get_count($generalCourseList));

            $header .= "工業科";
            $header .= str_repeat(",", get_count($technicalCourseList));

            $contents .= $header."\n";

            $header = "";
            foreach ($csvItem as $key => $val) {
                $header .= ($header == "") ? $val : ",".$val;
            }
            $contents .= $header."\n";
        }

        if ($this->field["OUTPUT"] == "1") {
            //SQL取得
            $query = knjl692iQuery::getApplicationList($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                $cma = "";

                foreach ($csvItem as $key => $val) {
                    $contents .= $cma.$row[$key];
                    $cma = ",";
                }

                $contents .= "\n";
                $data = true;
            }
            $result->free();
        } elseif ($this->field["OUTPUT"] == "2") {
            //SQL取得
            $query = knjl692iQuery::getApplicationFinschoolList($this, $generalCourseList, $technicalCourseList);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                $cma = "";

                foreach ($csvItem as $key => $val) {
                    $contents .= $cma.$row[$key];
                    $cma = ",";
                }

                $contents .= "\n";
                $data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        if ($data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
