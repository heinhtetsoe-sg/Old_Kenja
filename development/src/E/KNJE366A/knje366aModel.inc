<?php

require_once('for_php7.php');

class knje366aModel extends Model {
    var $cmd;
    var $control;

    function knje366aModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SCHOOL_GROUP"     =>  VARS::post("SCHOOL_GROUP"),      //大学タイプ
                             "S_CD"             =>  VARS::post("S_CD")               //全て、特定学部（医学、薬学、歯学）
                             );
        if ($this->field["OUTPUT"] == "") {
            $this->field["OUTPUT"] = 1;
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ（1行目：タイトル）
        //$title = "大学別進学資料";
        //$contents .= $title. str_repeat(",", 10). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";

        //ヘッダ（2行目）
        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
            $entDivName = array();
            $query = knje366aQuery::getEntDivName($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $entDivName[$row["NAMECD2"]] = $row["NAME1"];
            }
            if ($this->field["OUTPUT"] == "1") {
                $title = "大学別進学資料";
                $contents .= $title. str_repeat(",", 13). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";
                $header = array("SCHOOL_GROUP_NAME"     => "学校系列",
                                "SCHOOL_NAME"           => "大学",
                                "PASS_SCHOOL_CNT1"      => "現役合格数",
                                "PASS_SCHOOL_CNT1_A"    => $entDivName[1],
                                "PASS_SCHOOL_CNT1_B"    => $entDivName[2],
                                "PASS_SCHOOL_CNT2"      => "既卒合格数",
                                "PASS_SCHOOL_CNT2_A"    => $entDivName[1],
                                "PASS_SCHOOL_CNT2_B"    => $entDivName[2],
                                "GO_SCHOOL_CNT1"        => "現役進学数",
                                "GO_SCHOOL_CNT1_A"      => $entDivName[1],
                                "GO_SCHOOL_CNT1_B"      => $entDivName[2],
                                "GO_SCHOOL_CNT2"        => "既卒進学数",
                                "GO_SCHOOL_CNT2_A"      => $entDivName[1],
                                "GO_SCHOOL_CNT2_B"      => $entDivName[2]
                                );
            } else {
                $title = "学部・学科別進学資料";
                $contents .= $title. str_repeat(",", 15). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";
                $header = array("SCHOOL_GROUP_NAME"     => "学校系列",
                                "SCHOOL_NAME"           => "大学",
                                "FACULTYNAME"           => "学部",
                                "DEPARTMENTNAME"        => "学科",
                                "PASS_FACULTY_CNT1"     => "現役合格数",
                                "PASS_FACULTY_CNT1_A"   => $entDivName[1],
                                "PASS_FACULTY_CNT1_B"   => $entDivName[2],
                                "PASS_FACULTY_CNT2"     => "既卒合格数",
                                "PASS_FACULTY_CNT2_A"   => $entDivName[1],
                                "PASS_FACULTY_CNT2_B"   => $entDivName[2],
                                "GO_FACULTY_CNT1"       => "現役進学数",
                                "GO_FACULTY_CNT1_A"     => $entDivName[1],
                                "GO_FACULTY_CNT1_B"     => $entDivName[2],
                                "GO_FACULTY_CNT2"       => "既卒進学数",
                                "GO_FACULTY_CNT2_A"     => $entDivName[1],
                                "GO_FACULTY_CNT2_B"     => $entDivName[2]
                                );
            }
        } else {
            if ($this->field["OUTPUT"] == "1") {
                $title = "大学別進学資料";
                $contents .= $title. str_repeat(",", 5). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";
                $header = array("SCHOOL_GROUP_NAME"     => "学校系列",
                                "SCHOOL_NAME"           => "大学",
                                "PASS_SCHOOL_CNT1"      => "現役合格数",
                                "PASS_SCHOOL_CNT2"      => "既卒合格数",
                                "GO_SCHOOL_CNT1"        => "現役進学数",
                                "GO_SCHOOL_CNT2"        => "既卒進学数"
                                );
            } else {
                $title = "学部・学科別進学資料";
                $contents .= $title. str_repeat(",", 7). '作成日：'. str_replace('-', '/', CTRL_DATE) ."\n";
                $header = array("SCHOOL_GROUP_NAME"     => "学校系列",
                                "SCHOOL_NAME"           => "大学",
                                "FACULTYNAME"           => "学部",
                                "DEPARTMENTNAME"        => "学科",
                                "PASS_FACULTY_CNT1"     => "現役合格数",
                                "PASS_FACULTY_CNT2"     => "既卒合格数",
                                "GO_FACULTY_CNT1"       => "現役進学数",
                                "GO_FACULTY_CNT2"       => "既卒進学数"
                                );
            }
        }

        //ヘッダ（2行目）セット
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $data = false;
        $dataTemp = array();

        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
            //OUT_PUT1で使用
            $pass_school_cnt1_kokuritu_A = "";
            $pass_school_cnt1_kokuritu_B = "";
            $pass_school_cnt2_kokuritu_A = "";
            $pass_school_cnt2_kokuritu_B = "";
            $go_school_cnt1_kokuritu_A   = "";
            $go_school_cnt1_kokuritu_B   = "";
            $go_school_cnt2_kokuritu_A   = "";
            $go_school_cnt2_kokuritu_B   = "";
            $pass_school_cnt1_shiritsu_A = "";
            $pass_school_cnt1_shiritsu_B = "";
            $pass_school_cnt2_shiritsu_A = "";
            $pass_school_cnt2_shiritsu_B = "";
            $go_school_cnt1_shiritsu_A   = "";
            $go_school_cnt1_shiritsu_B   = "";
            $go_school_cnt2_shiritsu_A   = "";
            $go_school_cnt2_shiritsu_B   = "";
            //OUT_PUT2で使用
            $pass_faculty_cnt1_kokuritu_A = "";
            $pass_faculty_cnt1_kokuritu_B = "";
            $pass_faculty_cnt2_kokuritu_A = "";
            $pass_faculty_cnt2_kokuritu_B = "";
            $go_faculty_cnt1_kokuritu_A   = "";
            $go_faculty_cnt1_kokuritu_B   = "";
            $go_faculty_cnt2_kokuritu_A   = "";
            $go_faculty_cnt2_kokuritu_B   = "";
            $pass_faculty_cnt1_shiritsu_A = "";
            $pass_faculty_cnt1_shiritsu_B = "";
            $pass_faculty_cnt2_shiritsu_A = "";
            $pass_faculty_cnt2_shiritsu_B = "";
            $go_faculty_cnt1_shiritsu_A   = "";
            $go_faculty_cnt1_shiritsu_B   = "";
            $go_faculty_cnt2_shiritsu_A   = "";
            $go_faculty_cnt2_shiritsu_B   = "";
        }

        $pass_school_cnt1_kokuritu = "";
        $pass_school_cnt2_kokuritu = "";
        $pass_faculty_cnt1_kokuritu = "";
        $pass_faculty_cnt2_kokuritu = "";
        $go_school_cnt1_kokuritu = "";
        $go_school_cnt2_kokuritu = "";
        $go_faculty_cnt1_kokuritu = "";
        $go_faculty_cnt2_kokuritu = "";
        $pass_school_cnt1_shiritsu = "";
        $pass_school_cnt2_shiritsu = "";
        $pass_faculty_cnt1_shiritsu = "";
        $pass_faculty_cnt2_shiritsu = "";
        $go_school_cnt1_shiritsu = "";
        $go_school_cnt2_shiritsu = "";
        $go_faculty_cnt1_shiritsu = "";
        $go_faculty_cnt2_shiritsu = "";

        $query = knje366aQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",",".",$row);
            if ($row["SCHOOL_GROUP"] == "1") {
                $pass_school_cnt1_kokuritu  += $row["PASS_SCHOOL_CNT1"];
                $pass_school_cnt2_kokuritu  += $row["PASS_SCHOOL_CNT2"]; 
                $pass_faculty_cnt1_kokuritu += $row["PASS_FACULTY_CNT1"];
                $pass_faculty_cnt2_kokuritu += $row["PASS_FACULTY_CNT2"]; 
                $go_school_cnt1_kokuritu    += $row["GO_SCHOOL_CNT1"];
                $go_school_cnt2_kokuritu    += $row["GO_SCHOOL_CNT2"]; 
                $go_faculty_cnt1_kokuritu   += $row["GO_FACULTY_CNT1"];
                $go_faculty_cnt2_kokuritu   += $row["GO_FACULTY_CNT2"];
                //入学区分別カウント
                $pass_school_cnt1_kokuritu_A  += $row["PASS_SCHOOL_CNT1_A"];
                $pass_school_cnt1_kokuritu_B  += $row["PASS_SCHOOL_CNT1_B"];
                $pass_school_cnt2_kokuritu_A  += $row["PASS_SCHOOL_CNT2_A"];
                $pass_school_cnt2_kokuritu_B  += $row["PASS_SCHOOL_CNT2_B"];
                $pass_faculty_cnt1_kokuritu_A += $row["PASS_FACULTY_CNT1_A"];
                $pass_faculty_cnt1_kokuritu_B += $row["PASS_FACULTY_CNT1_B"];
                $pass_faculty_cnt2_kokuritu_A += $row["PASS_FACULTY_CNT2_A"];
                $pass_faculty_cnt2_kokuritu_B += $row["PASS_FACULTY_CNT2_B"];
                $go_school_cnt1_kokuritu_A    += $row["GO_SCHOOL_CNT1_A"];
                $go_school_cnt1_kokuritu_B    += $row["GO_SCHOOL_CNT1_B"];
                $go_school_cnt2_kokuritu_A    += $row["GO_SCHOOL_CNT2_A"];
                $go_school_cnt2_kokuritu_B    += $row["GO_SCHOOL_CNT2_B"];
                $go_faculty_cnt1_kokuritu_A   += $row["GO_FACULTY_CNT1_A"];
                $go_faculty_cnt1_kokuritu_B   += $row["GO_FACULTY_CNT1_B"];
                $go_faculty_cnt2_kokuritu_A   += $row["GO_FACULTY_CNT2_A"];
                $go_faculty_cnt2_kokuritu_B   += $row["GO_FACULTY_CNT2_B"];
            } else {
                $pass_school_cnt1_shiritsu  += $row["PASS_SCHOOL_CNT1"];
                $pass_school_cnt2_shiritsu  += $row["PASS_SCHOOL_CNT2"]; 
                $pass_faculty_cnt1_shiritsu += $row["PASS_FACULTY_CNT1"];
                $pass_faculty_cnt2_shiritsu += $row["PASS_FACULTY_CNT2"]; 
                $go_school_cnt1_shiritsu    += $row["GO_SCHOOL_CNT1"];
                $go_school_cnt2_shiritsu    += $row["GO_SCHOOL_CNT2"]; 
                $go_faculty_cnt1_shiritsu   += $row["GO_FACULTY_CNT1"];
                $go_faculty_cnt2_shiritsu   += $row["GO_FACULTY_CNT2"]; 
                //入学別カウント
                $pass_school_cnt1_shiritsu_A  += $row["PASS_SCHOOL_CNT1_A"];
                $pass_school_cnt1_shiritsu_B  += $row["PASS_SCHOOL_CNT1_B"];
                $pass_school_cnt2_shiritsu_A  += $row["PASS_SCHOOL_CNT2_A"];
                $pass_school_cnt2_shiritsu_B  += $row["PASS_SCHOOL_CNT2_B"];
                $pass_faculty_cnt1_shiritsu_A += $row["PASS_FACULTY_CNT1_A"];
                $pass_faculty_cnt1_shiritsu_B += $row["PASS_FACULTY_CNT1_B"];
                $pass_faculty_cnt2_shiritsu_A += $row["PASS_FACULTY_CNT2_A"];
                $pass_faculty_cnt2_shiritsu_B += $row["PASS_FACULTY_CNT2_B"];
                $go_school_cnt1_shiritsu_A    += $row["GO_SCHOOL_CNT1_A"];
                $go_school_cnt1_shiritsu_B    += $row["GO_SCHOOL_CNT1_B"];
                $go_school_cnt2_shiritsu_A    += $row["GO_SCHOOL_CNT2_A"];
                $go_school_cnt2_shiritsu_B    += $row["GO_SCHOOL_CNT2_B"];
                $go_faculty_cnt1_shiritsu_A   += $row["GO_FACULTY_CNT1_A"];
                $go_faculty_cnt1_shiritsu_B   += $row["GO_FACULTY_CNT1_B"];
                $go_faculty_cnt2_shiritsu_A   += $row["GO_FACULTY_CNT2_A"];
                $go_faculty_cnt2_shiritsu_B   += $row["GO_FACULTY_CNT2_B"];
            }

            $dataTemp[] = $row;
        }
        $result->free();

        foreach ($dataTemp as $key => $val) {
            //ヘッダー順で出力
            $sep = "";
            foreach ($header as $hkey => $hval) {
                //データセット
                $contents .= $sep.$dataTemp[$key][$hkey];
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }

        //合計をセット
        if ($this->Properties["useEntDiv_Hyouji"] == "1") {
            if ($this->field["OUTPUT"] == "1") {
                if ($this->field["SCHOOL_GROUP"] != "2") {
                    $contents .= ",国公立合計";
                    $contents .= ",{$pass_school_cnt1_kokuritu},{$pass_school_cnt1_kokuritu_A},{$pass_school_cnt1_kokuritu_B}";
                    $contents .= ",{$pass_school_cnt2_kokuritu},{$pass_school_cnt2_kokuritu_A},{$pass_school_cnt2_kokuritu_B}";
                    $contents .= ",{$go_school_cnt1_kokuritu},{$go_school_cnt1_kokuritu_A},{$go_school_cnt1_kokuritu_B}";
                    $contents .= ",{$go_school_cnt2_kokuritu},{$go_school_cnt2_kokuritu_A},{$go_school_cnt2_kokuritu_B}\n";
                }
                if ($this->field["SCHOOL_GROUP"] != "1") {
                    $contents .= ",私立合計";
                    $contents .= ",{$pass_school_cnt1_shiritsu},{$pass_school_cnt1_shiritsu_A},{$pass_school_cnt1_shiritsu_B}";
                    $contents .= ",{$pass_school_cnt2_shiritsu},{$pass_school_cnt2_shiritsu_A},{$pass_school_cnt2_shiritsu_B}";
                    $contents .= ",{$go_school_cnt1_shiritsu},{$go_school_cnt1_shiritsu_A},{$go_school_cnt1_shiritsu_B}";
                    $contents .= ",{$go_school_cnt2_shiritsu},{$go_school_cnt2_shiritsu_A},{$go_school_cnt2_shiritsu_B}\n";
                }
            } else {
                if ($this->field["SCHOOL_GROUP"] != "2") {
                    $contents .= ",,,国公立合計";
                    $contents .= ",{$pass_faculty_cnt1_kokuritu},{$pass_faculty_cnt1_kokuritu_A},{$pass_faculty_cnt1_kokuritu_B}";
                    $contents .= ",{$pass_faculty_cnt2_kokuritu},{$pass_faculty_cnt2_kokuritu_A},{$pass_faculty_cnt2_kokuritu_B}";
                    $contents .= ",{$go_faculty_cnt1_kokuritu},{$go_faculty_cnt1_kokuritu_A},{$go_faculty_cnt1_kokuritu_B}";
                    $contents .= ",{$go_faculty_cnt2_kokuritu},{$go_faculty_cnt2_kokuritu_A},{$go_faculty_cnt2_kokuritu_B}\n";
                }
                if ($this->field["SCHOOL_GROUP"] != "1") {
                    $contents .= ",,,私立合計";
                    $contents .= ",{$pass_faculty_cnt1_shiritsu},{$pass_faculty_cnt1_shiritsu_A},{$pass_faculty_cnt1_shiritsu_B}";
                    $contents .= ",{$pass_faculty_cnt2_shiritsu},{$pass_faculty_cnt2_shiritsu_A},{$pass_faculty_cnt2_shiritsu_B}";
                    $contents .= ",{$go_faculty_cnt1_shiritsu},{$go_faculty_cnt1_shiritsu_A},{$go_faculty_cnt1_shiritsu_B}";
                    $contents .= ",{$go_faculty_cnt2_shiritsu},{$go_faculty_cnt2_shiritsu_A},{$go_faculty_cnt2_shiritsu_B}\n";
                }
            }
        } else {
            if ($this->field["OUTPUT"] == "1") {
                if ($this->field["SCHOOL_GROUP"] != "2") {
                    $contents .= ",国公立合計";
                    $contents .= ",{$pass_school_cnt1_kokuritu}";
                    $contents .= ",{$pass_school_cnt2_kokuritu}";
                    $contents .= ",{$go_school_cnt1_kokuritu}";
                    $contents .= ",{$go_school_cnt2_kokuritu}\n";
                }
                if ($this->field["SCHOOL_GROUP"] != "1") {
                    $contents .= ",私立合計";
                    $contents .= ",{$pass_school_cnt1_shiritsu}";
                    $contents .= ",{$pass_school_cnt2_shiritsu}";
                    $contents .= ",{$go_school_cnt1_shiritsu}";
                    $contents .= ",{$go_school_cnt2_shiritsu}\n";
                }
            } else {
                if ($this->field["SCHOOL_GROUP"] != "2") {
                    $contents .= ",,,国公立合計";
                    $contents .= ",{$pass_faculty_cnt1_kokuritu}";
                    $contents .= ",{$pass_faculty_cnt2_kokuritu}";
                    $contents .= ",{$go_faculty_cnt1_kokuritu}";
                    $contents .= ",{$go_faculty_cnt2_kokuritu}\n";
                }
                if ($this->field["SCHOOL_GROUP"] != "1") {
                    $contents .= ",,,私立合計";
                    $contents .= ",{$pass_faculty_cnt1_shiritsu}";
                    $contents .= ",{$pass_faculty_cnt2_shiritsu}";
                    $contents .= ",{$go_faculty_cnt1_shiritsu}";
                    $contents .= ",{$go_faculty_cnt2_shiritsu}\n";
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($data) {
            if ($this->field["OUTPUT"] == "1") {
                $title = "大学別進学資料";
            } else {
                $title = "学部・学科別進学資料";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
