<?php

require_once('for_php7.php');

class knjl610iModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;

    public function knjl610iModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->examType = 1;

        $this->field = array("GAKKA"                =>  VARS::post("GAKKA"),                //学科
                             "TESTDIV"              =>  VARS::post("TESTDIV"),              //入試区分
                             "HALL"                 =>  VARS::post("HALL"),                 //会場
                             "SEX"                  =>  VARS::post("SEX"),                  //性別
                             "SORT"                 =>  VARS::post("SORT"),                 //出力順
                             "OUTPUT"               =>  VARS::post("OUTPUT"),               //帳票タイプ
                             "SPECIAL"              =>  VARS::post("SPECIAL"),              //特待生
                             "TESTSUBCLASSCD_CHECK" =>  VARS::post("TESTSUBCLASSCD_CHECK"), //科目毎
                             "COURSE"               =>  VARS::post("COURSE"),               //コース
                             "RUIBETSU"             =>  VARS::post("RUIBETSU")              //類別
                            );

        $this->cmd = VARS::request("cmd");
        if ($this->cmd == "") {
            $this->field["TESTSUBCLASSCD_CHECK"] = "1";
        }
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;
        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        if ($this->field["OUTPUT"] == "1") {
            $csvItem = array("NO"                  => array("No."),
                             "GAKKA"               => array("学科"),
                             "EXAMNO"              => array("受験番号"),
                             "NAME"                => array("氏名"),
                             "NAME_KANA"           => array("フリガナ"),
                             "SEX"                 => array("性別"),
                             "BIRTHDAY"            => array("生年月日"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校名"),
                             "REMARK"              => array("備考")
            );
        } elseif ($this->field["OUTPUT"] == "2") {
            $csvItem = array("NO"                  => array("No."),
                             "TESTDIV_NAME"        => array("試験"),
                             "EXAMNO"              => array("受験番号"),
                             "ASPIRING1"           => array("志望順位1"),
                             "ASPIRING2"           => array("志望順位2"),
                             "ASPIRING3"           => array("志望順位3"),
                             "ASPIRING4"           => array("志望順位4"),
                             "NAME"                => array("漢字氏名"),
                             "NAME_KANA"           => array("氏名フリガナ"),
                             "NAME_HIRA"           => array("氏名かな"),
                             "SEX"                 => array("性別"),
                             "FS_CD"               => array("中学校コード"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校名"),
                             "REMARK2"             => array("特待コード"),
                             "GENERAL_MARK"        => array("特待"),
                             "SP_REASON"           => array("特待理由"),
                             "SHOGAKU"             => array("奨学希望"),
                             "RYO"                 => array("寮生"),
                             "GNAME"               => array("保護者氏名")
            );
        } elseif ($this->field["OUTPUT"] == "3") {
            $csvItem = array("NO"                  => array("No."),
                             "EXAMNO"              => array("受験番号"),
                             "NAME"                => array("氏名"),
                             "NAME_KANA"           => array("フリガナ"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校"),
                             "ATTENDANCE"          => array("欠席"),
                             "REMARK"              => array("備考(別室受験等)")
            );
        } elseif ($this->field["OUTPUT"] == "4") {
            $csvItem = array("NO"     => array("No."),
                             "EXAMNO" => array("受験番号"),
                             "SCORE"  => array("得点")
            );
        } elseif ($this->field["OUTPUT"] == "5") {
            $csvItem = array("EXAMNO"              => array("受験番号"),
                             "FINSCHOOL_NAME_ABBV" => array("出身中学校"),
                             "NAME"                => array("氏名"),
                             "NAME_KANA"           => array("フリガナ"),
                             "GENERAL_ABBV"        => array("志望学科 類・コース"),
                             "MOTIVE1"             => array("志望の動機"),
                             "MOTIVE2"             => array("志望の動機"),
                             "MOTIVE3"             => array("志望の動機 その他"),
                             "ABSENCE_DAYS"        => array("出欠状況1年"),
                             "ABSENCE_DAYS2"       => array("出欠状況2年"),
                             "ABSENCE_DAYS3"       => array("出欠状況3年"),
                             "RYO"                 => array("入寮予定"),
                             "YOBI1"               => array("県外・遠距離通学方法"),
                             "YOBI2"               => array("面接判定 服装態度"),
                             "YOBI3"               => array("面接判定 服装態度"),
                             "YOBI4"               => array("面接判定 服装態度"),
                             "YOBI5"               => array("面接判定 特記事項 判定理由等")
            );
        }

        $contents = "";

        //タイトル設定
        $db = Query::dbCheckOut();
        $testDRow = $db->getRow(knjl610iQuery::getTestDivMst($this, $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
        $testDName = $testDRow["TESTDIV_NAME"];
        $testDAbbv = $testDRow["TESTDIV_ABBV"];
        $gengouRow = $db->getRow("SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' AND '{$this->ObjYear}' BETWEEN NAMESPARE1 AND ABBV3", DB_FETCHMODE_ASSOC);
        $gengou = $gengouRow["NAME1"];
        $gannen = $gengouRow["NAMESPARE1"];
        $nen = $this->ObjYear - $gannen + 1;
        if (nen == 1) {
            $nendo = $gengou."元";
        } else {
            $nendo = $gengou.$nen;
        }

        $title = "";
        if ($this->field["OUTPUT"] == "1") {
            $title = $nendo."年度 志願者リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT"] == "2") {
            if ($this->field["SPECIAL"] == "1") {
                $subtitle = "志願者全員";
            } elseif ($this->field["SPECIAL"] == "2") {
                $subtitle = "特待生・学業";
            } else {
                $subtitle = "特待生・部活";
            }
            $title = $nendo."年度 志願者リスト(".$testDName.")(".$subtitle.")";
        } elseif ($this->field["OUTPUT"] == "3") {
            $title = $nendo."年度 ".$testDName." 出欠席点検確認書";
        } elseif ($this->field["OUTPUT"] == "4") {
            $title = $nendo."年度 ".$testDName." 得点記入書";
        } elseif ($this->field["OUTPUT"] == "5") {
            $title = $nendo."年度 ".$testDName." 面接表";
        }
        
        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        if ($this->field["OUTPUT"] != "1" && $this->field["OUTPUT"] != "3" && $this->field["OUTPUT"] != "4" && $this->field["OUTPUT"] != "5") {
            $contents .= $header."\n";
        }

        //SQL取得
        if ($this->field["OUTPUT"] == "1") {
            $query = knjl610iQuery::getApplicantListCheck($this);
        } elseif ($this->field["OUTPUT"] == "2") {
            $query = knjl610iQuery::getApplicantList($this);
        } elseif ($this->field["OUTPUT"] == "3") {
            $query = knjl610iQuery::getAttendance($this);
        } elseif ($this->field["OUTPUT"] == "4") {
            $query = knjl610iQuery::getScore($this);
        } elseif ($this->field["OUTPUT"] == "5") {
            $query = knjl610iQuery::getInterView($this);
        }
        $hallCd = "";
        $subclassName = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            $cnt = 0;
            $cma = "";

            if ($this->field["OUTPUT"] == "1") {
                if ($row["EXAMHALLCD"] != $hallCd) {
                    $hallCd = $row["EXAMHALLCD"];
                    $contents .= "会場名,".$row["EXAMHALL_NAME"]."会場,";
                    $contents .= "人数,".$row["NUM"]."名,";
                    $contents .= "受験番号,".$row["S_RECEPTNO"]."～".$row["E_RECEPTNO"];
                    $contents .= "\n";
                    $contents .= $header."\n";
                }
            } elseif ($this->field["OUTPUT"] == "3") {
                if ($this->field["TESTSUBCLASSCD_CHECK"] == "1") {
                    if ($row["EXAMHALLCD"] != $hallCd || $row["SUBCLASS_NAME"] != $subclassName) {
                        $hallCd = $row["EXAMHALLCD"];
                        $subclassName = $row["SUBCLASS_NAME"];
                        $contents .= "試験会場【".$row["EXAMHALL_NAME"]."】,".$row["SUBCLASS_NAME"];
                        $contents .= "\n";
                        $contents .= $header."\n";
                    }
                } else {
                    if ($row["EXAMHALLCD"] != $hallCd) {
                        $hallCd = $row["EXAMHALLCD"];
                        $contents .= "試験会場【".$row["EXAMHALL_NAME"]."】";
                        $contents .= "\n";
                        $contents .= $header."\n";
                    }
                }
            } elseif ($this->field["OUTPUT"] == "4") {
                if ($row["EXAMHALLCD"] != $hallCd || $row["SUBCLASS_NAME"] != $subclassName) {
                    $hallCd = $row["EXAMHALLCD"];
                    $subclassName = $row["SUBCLASS_NAME"];
                    $contents .= "教科名,".$row["SUBCLASS_NAME"].",";
                    $contents .= "試験会場,".$row["EXAMHALL_NAME"].",";
                    $contents .= "受験番号,".$row["S_RECEPTNO"]."～".$row["E_RECEPTNO"];
                    $contents .= "\n";
                    $contents .= $header."\n";
                }
            } elseif ($this->field["OUTPUT"] == "5") {
                if ($row["EXAMHALLCD"] != $hallCd) {
                    $hallCd = $row["EXAMHALLCD"];
                    $contents .= "試験会場番号,".$row["EXAMHALLCD"].",";
                    $contents .= "\n";
                    $contents .= $header."\n";
                }
            }
            foreach ($csvItem as $key => $val) {
                if ($key == "NAME_HIRA") { //カナ⇒ひら
                    $hira = mb_convert_kana($row["NAME_KANA"], cH);
                    $contents .= $cma.$hira;
                } elseif ($key == "EXAMNO" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2" || $this->field["OUTPUT"] == "3")) {
                    if ($row["DUPLICATE_FLG"] == 1) {
                        $contents .= $cma."*".$row[$key];
                    } else {
                        $contents .= $cma.$row[$key];
                    }
                } else {
                    $contents .= $cma.$row[$key];
                }
                
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
}
