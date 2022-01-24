<?php
class knjl692hModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $schoolKind;

    public function knjl692hModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("TESTDIV"          =>  VARS::post("TESTDIV"),          //入試日程
                             "TESTDIV1"         =>  VARS::post("TESTDIV1"),         //類別
                             "RECEPTNO_START"   =>  VARS::post("RECEPTNO_START"),   //受験番号（開始）
                             "RECEPTNO_START"   =>  VARS::post("RECEPTNO_START"),   //受験番号（開始）
                             "RECEPTNO_END"     =>  VARS::post("RECEPTNO_END"),     //受験番号（終了）
                             "SEX"              =>  VARS::post("SEX"),              //男女別
                             "ORDER"            =>  VARS::post("ORDER"),            //出力順
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //帳票種類
                             "FILTER1"          =>  VARS::post("FILTER1"),          //出力条件1
                             "FILTER2"          =>  VARS::post("FILTER2")           //出力条件2
                        );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    public function clean()
    {
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        if ($this->field["OUTPUT"] == "1") {
            knjl692hModel::getDownloadCsv1();
        } elseif ($this->field["OUTPUT"] == "2") {
            knjl692hModel::getDownloadCsv2();
        } elseif ($this->field["OUTPUT"] == "3") {
            knjl692hModel::getDownloadCsv3();
        } elseif ($this->field["OUTPUT"] == "4") {
            knjl692hModel::getDownloadCsv4();
        }
        return true;
    }

    public function getDownloadCsv1()
    {
        $data = false;

        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        $csvItem = array("RECEPTNO"  => array("受験番号"),
                         "NAME"      => array("氏名"),
                         "NAME_KANA" => array("氏名(かな)"),
                         "SEX"       => array("性別"),
                         "REMARK"    => array("備考"),
                        );

        $contents = "";

        //タイトル設定
        $db = Query::dbCheckOut();
        $testDRow = $db->getRow(knjl692hQuery::getEntexamTestDivMst($this->ObjYear, "2", $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
        $testDName = $testDRow["TESTDIV_NAME"];
        $title = $this->ObjYear."年度".$testDName."入試 受験者名簿";
        $titlewk = $title." ".substr(CTRL_DATE, 2)." ".date("H:i");

        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        $contents .= $header."\n";
        $query = knjl692hQuery::getExaminee($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            $cnt = 0;
            $cma = "";
            foreach ($csvItem as $key => $val) {
                $contents .= $cma.$row[$key];
                $cma = ",";
                $cnt++;
            }

            $contents .= "\n";
            $data = true;
        }
        $result->free();
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

    public function getDownloadCsv2()
    {
        $data = false;
        if ($this->field["OUTPUT"] == "2") {
            //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
            $csvItem = array("ROW_NUMBER"            => array("整理"),
                             "ENTDIV"                => array("入学"),
                             "PROCEDUREDIV"          => array("手続"),
                             "JUDGEMENT"             => array("合格"),
                             "MARK"                  => array("種別"),
                             "RECEPTNO"              => array("受験番号"),
                             "NAME"                  => array("氏名"),
                             "NAME_KANA"             => array("ふりがな"),
                             "SEX"                   => array("性別"),
                             "BIRTHDAY"              => array("生年月日"),
                             "FINSCHOOL_DIST_NAME"   => array("出身"),
                             "FINSCHOOL_NAME"        => array("中学"),
                             "CONFIDENTIAL_RPT01"    => array("教科1"),
                             "CONFIDENTIAL_RPT02"    => array("教科2"),
                             "CONFIDENTIAL_RPT03"    => array("教科3"),
                             "CONFIDENTIAL_RPT04"    => array("教科4"),
                             "CONFIDENTIAL_RPT05"    => array("教科5"),
                             "TOTAL3"                => array("3科"),
                             "TOTAL5"                => array("5科"),
                             "TOTAL_ALL"             => array("9科"),
                             "SCORE_KOKUGO"          => array("国語"),
                             "SCORE_SUGAKU"          => array("数学"),
                             "SCORE_EIGO"            => array("英語"),
                             "TOTAL"                 => array("合計"),
                             "TOTAL_RANK"            => array("順位"),
                             "INTERVIEW_A"           => array("面接"),
                             "REMARK"                => array("備考")
                            );

            $contents = "";
            //ヘッダ取得
            $header = "";
            foreach ($csvItem as $key => $val) {
                $header     .= ($header == "") ? $val[0] : ",".$val[0];
            }
            //ヘッダセット
            $contents .= $header."\n";
        }
        $query = knjl692hQuery::getCsvData($this);

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            if ($this->field["OUTPUT"] == "2") {
                $cnt = 0;
                foreach ($csvItem as $key => $val) {
                    $contents .= ($cnt == 0) ? $row[$key] : ",".$row[$key];
                    $cnt++;
                }
            } else {
                $contents .= implode($row, ",");
            }

            $contents .= "\n";
            $data = true;
        }
        $result->free();

        $testDRow = $db->getRow(knjl692hQuery::getEntexamTestDivMst($this->ObjYear, "2", $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
        $testDName = $testDRow["TESTDIV_NAME"];

        Query::dbCheckIn($db);

        if ($data) {
            $order = "";
            if ($this->field["ORDER"] == "1") {
                $order = "受験番号順";
            } else {
                $order = "氏名順(50音順)";
            }
            $couse = $this->ObjYear."年度入試 ".$testDName."入試 ".$order;
            $filename = sprintf("%s.csv", $couse, "SJIS-win");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function getDownloadCsv3()
    {
        $data = false;

        $csvItem = array("RECEPTNO" =>  array("受験番号"));

        $contents = "";

        //データ取得
        $query = knjl692hQuery::getPassExaminee($this);

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        $cma = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cnt = 0;
            foreach ($csvItem as $key => $val) {
                $contents .= $cma.$row[$key];
                $cma = ",";
                $cnt++;
            }
            $data = true;
        }
        $contents .= "\n";
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->year."年度入試合格者一覧(掲示用)データ.csv";
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function &getDownloadCsv4()
    {
        $data = false;

        $csvItem = array("RECEPTNO" =>  array("受験番号"),
                         "NAME"   =>  array("氏名")
                        );

        $contents = "";

        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header     .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        $contents .= $header."\n";

        //データ取得
        $query = knjl692hQuery::getPassExaminee($this);

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cnt = 0;
            $cma = "";
            foreach ($csvItem as $key => $val) {
                $contents .= $cma.$row[$key];
                $cma = ",";
                $cnt++;
            }

            $contents .= "\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->ObjYear."年度合格通知書.csv";
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
