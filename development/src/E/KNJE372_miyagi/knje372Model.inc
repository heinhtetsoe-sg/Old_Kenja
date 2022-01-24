<?php

require_once('for_php7.php');


class knje372Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード

    function knje372Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),             //年度
                             "GAKKI"                =>  VARS::post("GAKKI"),            //学期
                             "OUT_DIV"              =>  VARS::post("OUT_DIV"),          //対象ラジオ
                             "JUKEN_DIV"            =>  VARS::post("JUKEN_DIV"),        //受験方式ラジオ
                             "KISOTU_DIV"           =>  VARS::post("KISOTU_DIV"),       //既卒者ラジオ
                             "CNT_NOBE"             =>  VARS::post("CNT_NOBE"),         //延べ人数で出力
                             "QUESTIONNAIRECD"      =>  VARS::post("QUESTIONNAIRECD"),  //調査名
                             "CHOICE"               =>  VARS::post("CHOICE"),           //希望
                             "SCHOOL_GROUP"         =>  VARS::post("SCHOOL_GROUP"),     //学校系列
                             "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {
        //ヘッダ
        //受験方式
        if ($this->field["OUT_DIV"] == "3") {
            $header = array("SCHOOL_GROUP_NAME" => "学校系列",
                            "STAT_CD"           => "大学コード",
                            "SCHOOL_NAME"       => "大学",
                            "FACULTYNAME"       => "学部",
                            "DEPARTMENTNAME"    => "学科",
                            "SCHOOL_CNT"        => "合計人数",
                            "SCHOOL_CNT_BOY"    => "合計男子人数",
                            "SCHOOL_CNT_GIRL"   => "合計女子人数",
                            "FACULTY_CNT"       => "学部",
                            "DEPARTMENT_CNT"    => "学科"
                            );
        } else if ($this->field["JUKEN_DIV"] == "1") {
            $header = array("SCHOOL_GROUP_NAME" => "学校系列",
                            "STAT_CD"           => "大学コード",
                            "SCHOOL_NAME"       => "大学",
                            "FACULTYNAME"       => "学部",
                            "DEPARTMENTNAME"    => "学科",
                            "HOWTOEXAM_NAME"    => "受験方式",
                            "SCHOOL_CNT"        => "合格人数",
                            "SCHOOL_CNT_BOY"    => "合格男子人数",
                            "SCHOOL_CNT_GIRL"   => "合格女子人数",
                            "FACULTY_CNT"       => "学部",
                            "DEPARTMENT_CNT"    => "学科",
                            "HOWTOEXAM_CNT"     => "受験方式"
                            );
        } else {
            $header = array("SCHOOL_GROUP_NAME" => "学校系列",
                            "STAT_CD"           => "大学コード",
                            "SCHOOL_NAME"       => "大学",
                            "FACULTYNAME"       => "学部",
                            "DEPARTMENTNAME"    => "学科",
                            "SCHOOL_CNT"        => "合格人数",
                            "SCHOOL_CNT_BOY"    => "合格男子人数",
                            "SCHOOL_CNT_GIRL"   => "合格女子人数",
                            "FACULTY_CNT"       => "学部",
                            "DEPARTMENT_CNT"    => "学科"
                            );
        }
        //DB接続
        $db = Query::dbCheckOut();
        //受験方式
        if ($this->field["JUKEN_DIV"] == "1") {
            $keyCheckTemp = "";
            $dataTemp2 = array();
            $howtoexamArray = array();
            $query = knje372Query::getNameMst();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $howtoexamArray[$row["HOWTOEXAM"]] = $row["HOWTOEXAM_NAME"];
            }
            $result->free();
        }
        //SQL文発行
        $headerClass = array();
        $dataTemp = array();
        $dataClass = array();
        if($this->field["OUT_DIV"] == "3") {
            $query = knje372Query::getShinroCsv($this);
        } else {
            $query = knje372Query::getPrintSingaku($this);
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            //ヘッダ（年組）
            $gradeHrClass = $row["GRADE"] .$row["HR_CLASS"];
            $headerClass[$gradeHrClass] = $row["HR_NAME"];
            //大学・学部・学科
            $key = $row["STAT_CD"] .$row["FACULTYCD"] .$row["DEPARTMENTCD"];
            //受験方式
            if ($this->field["JUKEN_DIV"] == "1") {
                $key .= $row["HOWTOEXAM"];
            }
            if ($dataTemp[$key] == "") {
                $dataTemp[$key][] = $row["SCHOOL_GROUP_NAME"];
                $dataTemp[$key][] = $row["STAT_CD"];
                $dataTemp[$key][] = $row["SCHOOL_NAME"];
                $dataTemp[$key][] = $row["FACULTYNAME"];
                $dataTemp[$key][] = $row["DEPARTMENTNAME"];
                //受験方式
                if ($this->field["JUKEN_DIV"] == "1") {
                    $dataTemp[$key][] = $row["HOWTOEXAM_NAME"];
                }
                $dataTemp[$key][] = $row["CNT_SCHOOL"];
                $dataTemp[$key][] = $row["CNT_SCHOOL_BOY"];
                $dataTemp[$key][] = $row["CNT_SCHOOL_GIRL"];
                $dataTemp[$key][] = $row["CNT_FACULTY"];
                $dataTemp[$key][] = $row["CNT_DEPARTMENT"];
                //受験方式
                if ($this->field["JUKEN_DIV"] == "1") {
                    $dataTemp[$key][] = $row["CNT_HOWTOEXAM"];
                }
            }
            //カウント
            $dataClass[$key][$gradeHrClass] = $row["CNT_CLASS"];
            //受験方式
            if ($this->field["JUKEN_DIV"] == "1") {
                //大学・学部・学科・・・合格者がいない受験方式の判断に使用する配列
                $keyCheck = $row["STAT_CD"] .$row["FACULTYCD"] .$row["DEPARTMENTCD"];
                if ($keyCheckTemp != $keyCheck) {
                    if (get_count($howtoexamArray)) {
                        foreach ($howtoexamArray as $howtoexam => $howtoexamName) {
                            $key2 = $keyCheck .$howtoexam;
                            $dataTemp2[$key2][] = $row["SCHOOL_GROUP_NAME"];
                            $dataTemp2[$key2][] = $row["STAT_CD"];
                            $dataTemp2[$key2][] = $row["SCHOOL_NAME"];
                            $dataTemp2[$key2][] = $row["FACULTYNAME"];
                            $dataTemp2[$key2][] = $row["DEPARTMENTNAME"];
                            $dataTemp2[$key2][] = $howtoexamName;
                            $dataTemp2[$key2][] = $row["CNT_SCHOOL"];
                            $dataTemp2[$key2][] = $row["CNT_SCHOOL_BOY"];
                            $dataTemp2[$key2][] = $row["CNT_SCHOOL_GIRL"];
                            $dataTemp2[$key2][] = $row["CNT_FACULTY"];
                            $dataTemp2[$key2][] = $row["CNT_DEPARTMENT"];
                            $dataTemp2[$key2][] = 0;
                        }
                    }
                    $keyCheckTemp = $keyCheck;
                }
            }
        }
        $result->free();
        //既卒者（1浪，2浪）
        if ($this->field["KISOTU_DIV"] == "1") {
            $headerClass[99991] = "１浪";
            $headerClass[99992] = "２浪以上";
        }
        //DB切断
        Query::dbCheckIn($db);
        $contents = ""; //クリア
        //ヘッダ
        $contents .= implode($header, ",");
        if (get_count($headerClass)) {
            ksort($headerClass); //年組をソート
            foreach ($headerClass as $gradeHrClass => $hrName) {
                $contents .= "," .$hrName;
            }
        }
        $contents .= "\n";
        //データ
        $data = false;
        //受験方式
        if ($this->field["JUKEN_DIV"] == "1") {
            foreach ($dataTemp2 as $key => $val) {
                if ($dataTemp[$key] == "") {
                    $contents .= implode($val, ",");
                } else {
                    $contents .= implode($dataTemp[$key], ",");
                }
                if (get_count($headerClass)) {
                    foreach ($headerClass as $gradeHrClass => $hrName) {
                        $contents .= "," .$dataClass[$key][$gradeHrClass];
                    }
                }
                $contents .= "\n";
                $data = true;
            }
        } else {
            foreach ($dataTemp as $key => $val) {
                $contents .= implode($val, ",");
                if (get_count($headerClass)) {
                    foreach ($headerClass as $gradeHrClass => $hrName) {
                        $contents .= "," .$dataClass[$key][$gradeHrClass];
                    }
                }
                $contents .= "\n";
                $data = true;
            }
        }
        //ＣＳＶ出力
        if ($data) {
            $nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);

            if ($this->field["OUT_DIV"] == "3") {
                $title = "進路志望調査集計表";
            } else if ($this->field["OUT_DIV"] == "1") {
                $title = "大学合格者一覧";
            } else {
                $title = "大学最終進路者一覧";
            }

            $filename = sprintf("%s.csv", $nendo ."_" .$title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
