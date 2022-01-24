<?php

require_once('for_php7.php');

class knjl630iModel extends Model
{
    public $field = array();
    public $cmd;
    public $ObjYear;
    public $s_examno;
    public $e_examno;
    public $radioId = array();

    public function knjl630iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->s_examno);
            unset($this->e_examno);
        }
        $this->ObjYear = CTRL_YEAR + 1;
        $this->s_examno     = VARS::post("S_EXAMNO");
        $this->e_examno     = VARS::post("E_EXAMNO");

        $this->field = array("GAKKA"          =>  VARS::post("GAKKA"),     //学科
                             "TESTDIV"        =>  VARS::post("TESTDIV"),   //入試区分
                             "HALL"           =>  VARS::post("HALL"),      //会場
                             "SEX"            =>  VARS::post("SEX"),       //性別
                             "SORT"           =>  VARS::post("SORT"),      //出力順
                             "OUTPUT"         =>  VARS::post("OUTPUT"),    //帳票タイプ
                             "SPECIAL"        =>  VARS::post("SPECIAL"),   //特待生
                             "COURSE"         =>  VARS::post("COURSE"),    //コース
                             "RUIBETSU"       =>  VARS::post("RUIBETSU"),  //類別
                             "EXAM"           =>  VARS::post("EXAM")       //入試状況
                            );

        $this->cmd = VARS::request("cmd");
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;
        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        if ($this->field["OUTPUT"] == "1") {
            $csvItem = array("NO"             => array("No."),
                             "TESTDIV_NAME"   => array("試験"),
                             "GAKKA"          => array("学科"),
                             "EXAMNO"         => array("受験番号"),
                             "PASSCOURSE"     => array("合格コース"),
                             "NAME"           => array("漢字氏名"),
                             "NAME_KANA"      => array("氏名カナ"),
                             "SEX"            => array("性別"),
                             "FS_CD"          => array("中学校コード"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校名"),
                             "HONORDIV"       => array("特待コード"),
                             "SP_NAME"        => array("特待"),
                             "SP_REASON_MARK" => array("特待理由"),
                             "RYO"            => array("寮生"),
                             "GNAME"          => array("保護者氏名"),
                             "ZIPCD"          => array("〒番号"),
                             "ADDRESS"        => array("住所"),
                             "TELNO"          => array("電話番号")
            );
        } elseif ($this->field["OUTPUT"] == "2") {
            $csvItem = array("NO"             => array("No."),
                             "TESTDIV_NAME"   => array("試験"),
                             "GAKKA"          => array("学科"),
                             "EXAMNO"         => array("受験番号"),
                             "ASPIRING1"      => array("志望順位1"),
                             "ASPIRING2"      => array("志望順位2"),
                             "ASPIRING3"      => array("志望順位3"),
                             "ASPIRING4"      => array("志望順位4"),
                             "NAME"           => array("漢字氏名"),
                             "NAME_KANA"      => array("氏名カナ"),
                             "GAIJI"          => array("外字"),
                             "SEX"            => array("性別"),
                             "FS_CD"          => array("中学校コード"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校名"),
                             "HONORDIV"       => array("特待コード"),
                             "SP_NAME"        => array("特待"),
                             "SP_REASON_MARK" => array("特待理由"),
                             "RYO"            => array("寮生"),
                             "GNAME"          => array("保護者氏名"),
                             "HANTEI"         => array("判定")
            );
        } elseif ($this->field["OUTPUT"] == "3") {
            $csvItem = array("DATE"           => array("発行年月日"),
                             "EXAMNO"         => array("受験番号"),
                             "FINSCHOOL_NAME" => array("中学校"),
                             "NAME"           => array("氏名"),
                             "PASSCOURSE"     => array("合格学科")
            );
        } elseif ($this->field["OUTPUT"] == "4") {
            $csvItem = array("EXAMNO"         => array("受験番号"),
                             "FINSCHOOL_NAME" => array("中学校"),
                             "NAME"           => array("氏名")
            );
        } elseif ($this->field["OUTPUT"] == "5") {
            if ($this->field["EXAM"] == "1") {
                $csvItem = array("GAKKA"     => array("学科")
                );
            } elseif ($this->field["EXAM"] == "2") {
                $csvItem = array("GENERAL_NAME"  => array("類別")
                );
            } elseif ($this->field["EXAM"] == "3") {
                $csvItem = array("GENERAL_ABBV"  => array("コース別")
                );
            }
            $csvItem = array_merge($csvItem, array(
                "TESTDIV_NAME"    => array("入試"),
                "SEX_NAME" => array("性別"),
                "SHIGANSYA"    => array("志願者"),
                "JUKENSYA"    => array("受験者"),
                "PASS"    => array("合格者"),
                "NYUGAKUKIN"    => array("入学申込金"),
                "SETSUBIHI"    => array("施設設備費"),
                "ENT"    => array("入学者")
            ));
        } elseif ($this->field["OUTPUT"] == "6") {
            $csvItem = array("TESTDIV_NAME" => array("入試区分"),
                             "NO"    => array("No."),
                             "FS_CD" => array("中学校コード"),
                             "FINSCHOOL_NAME" => array("中学校名"),
                             "JUKENSYA"      => array("受験者数"),
                             "KESSEKI"   => array("未受験者数")
            );
        } elseif ($this->field["OUTPUT"] == "7") {
            $csvItem = array("FINSCHOOL_NAME" => array("中学校名"),
                             "REMARK2" => array("合格発表日"),
                             "TESTDIV_NAME" => array("入試区分"),
                             "NO"    => array("No."),
                             "EXAMNO" => array("受験番号"),
                             "NAME" => array("氏名"),
                             "SHIBOU" => array("志望"),
                             "GOUHI"      => array("合否"),
                             "COURSE"   => array("合格学科/類"),
                             "SPECIAL"   => array("城東特待生区分")
            );
        } elseif ($this->field["OUTPUT"] == "8") {
            $csvItem = array("FINSCHOOL_NAME" => array("中学校名"),
                             "PRINCNAME" => array("校長"),
                             "REMARK2" => array("合格発表日"),
                             "TESTDIV_NAME" => array("入試区分"),
                             "KOTEI1" => array("入学試験選考結果のお知らせ"),
                             "SPECIAL" => array("特待生入学手続き"),
                             "SETSUBI" => array("諸手続き及び学費について"),
                             "PASS"      => array("合格通知書類一式"),
                             "SCHEDULE1_1"   => array("入学までのスケジュール1"),
                             "SCHEDULE1_2"   => array("開催日1"),
                             "SCHEDULE1_3"   => array("時間1_1"),
                             "SCHEDULE1_4"   => array("時間1_2"),
                             "SCHEDULE1_6"   => array("対象者1"),
                             "SCHEDULE2_1"   => array("入学までのスケジュール2"),
                             "SCHEDULE2_2"   => array("開催日2"),
                             "SCHEDULE2_3"   => array("時間2_1"),
                             "SCHEDULE2_4"   => array("時間2_2"),
                             "SCHEDULE2_6"   => array("対象者2"),
                             "SCHEDULE3_1"   => array("入学までのスケジュール3"),
                             "SCHEDULE3_2"   => array("開催日3"),
                             "SCHEDULE3_3"   => array("時間3_1"),
                             "SCHEDULE3_4"   => array("時間3_2"),
                             "SCHEDULE3_6"   => array("対象者3"),
                             "SCHEDULE4_1"   => array("入学までのスケジュール4"),
                             "SCHEDULE4_2"   => array("開催日4"),
                             "SCHEDULE4_3"   => array("時間4_1"),
                             "SCHEDULE4_4"   => array("時間4_2"),
                             "SCHEDULE4_6"   => array("対象者4")
            );
        } elseif ($this->field["OUTPUT"] == "9") {
            $csvItem = array("NENDO" => array("年度"),
                             "TESTDIV_NAME" => array("入試区分"),
                             "TYPE1" => array("納入種別1"),
                             "REMARK4" => array("納入期限1_日"),
                             "REMARK5" => array("納入期限1_時"),
                             "NYUGAKU" => array("金額1"),
                             "TYPE2" => array("納入種別2"),
                             "REMARK7" => array("納入期限2_日"),
                             "REMARK8" => array("納入期限2_時"),
                             "SETSUBI" => array("金額2"),
                             "FINSCHOOL_NAME" => array("受取人"),
                             "CAUTION" => array("注意事項"),
                             "EXAMNO"      => array("受験番号"),
                             "NAME"   => array("氏名"),
                             "NAME_KANA"   => array("カナ")
            );
        }

        $contents = "";

        //タイトル設定
        $db = Query::dbCheckOut();
        $testDRow = $db->getRow(knjl630iQuery::getTestDivMst($this, $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
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
            $title = $nendo."年度 合格者リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT"] == "2") {
            $title = $nendo."年度 不合格リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT"] == "3") {
            $title = $nendo."年度 合格通知書";
        } elseif ($this->field["OUTPUT"] == "4") {
            $title = $nendo."年度 特待生内定通知書";
        } elseif ($this->field["OUTPUT"] == "5") {
            if ($this->field["EXAM"] == "1") {
                $title = $nendo."年度 入試状況(学科別)";
            } elseif ($this->field["EXAM"] == "2") {
                $title = $nendo."年度 入試状況(類別)";
            } else {
                $title = $nendo."年度 入試状況(コース別)";
            }
        } elseif ($this->field["OUTPUT"] == "6") {
            $title = $nendo."年度 合格者なし中学校リスト";
        } elseif ($this->field["OUTPUT"] == "7") {
            $title = $nendo."年度 入学試験選考結果のお知らせ";
        } elseif ($this->field["OUTPUT"] == "8") {
            $title = $nendo."年度 入学試験選考結果のお知らせ送付について";
        } elseif ($this->field["OUTPUT"] == "9") {
            $title = $nendo."年度 振込依頼書";
        }
        
        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        if ($this->field["OUTPUT"] != "10") {
            $contents .= $header."\n";
        }

        //SQL取得
        if ($this->field["OUTPUT"] == "1") {
            $query = knjl630iQuery::getPassList($this);
        } elseif ($this->field["OUTPUT"] == "2") {
            $query = knjl630iQuery::getfailList($this);
        } elseif ($this->field["OUTPUT"] == "3") {
            $query = knjl630iQuery::getPassNotice($this);
        } elseif ($this->field["OUTPUT"] == "4") {
            $query = knjl630iQuery::getSpecialNotice($this);
        } elseif ($this->field["OUTPUT"] == "5") {
            $query = knjl630iQuery::getExamStatus($this);
        } elseif ($this->field["OUTPUT"] == "6") {
            $query = knjl630iQuery::getNgList($this);
        } elseif ($this->field["OUTPUT"] == "7") {
            $query = knjl630iQuery::getResult($this);
        } elseif ($this->field["OUTPUT"] == "8") {
            $query = knjl630iQuery::getResult2($this);
        } elseif ($this->field["OUTPUT"] == "9") {
            $query = knjl630iQuery::getFurikomi($this);
        }
        
        //処理日の元号
        $gengouRow = $db->getRow("SELECT * FROM NAME_MST WHERE NAMECD1 = 'L007' AND '".substr(CTRL_DATE, 0, 4)."' BETWEEN NAMESPARE1 AND ABBV3", DB_FETCHMODE_ASSOC);
        $gengou = $gengouRow["NAME1"];
        $gannen = $gengouRow["NAMESPARE1"];
        $nen = substr(CTRL_DATE, 0, 4) - $gannen + 1;
        if (nen == 1) {
            $nendo = $gengou."元";
            $nen   = "元";
        } else {
            $nendo = $gengou.$nen;
        }

        $examCd = "";
        $cnt = 1;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            
            $cma = "";

            foreach ($csvItem as $key => $val) {
                if ($key == "NAME_HIRA") { //カナ⇒ひら
                    $hira = mb_convert_kana($row["NAME_KANA"], cH);
                    $contents .= $cma.$hira;
                } elseif ($key == "DATE") {
                    $contents .= $cma.$nendo."年".date('n月d日', strtotime(CTRL_DATE));
                } elseif ($this->field["OUTPUT"] == "6" && $key == "NO") { //連番
                    $contents .= $cma.$cnt;
                } elseif ($key == "NENDO") {
                    $contents .= $cma.$nen;
                } elseif ($key == "TYPE1") {
                    $contents .= $cma."入学金(入学申込金)";
                } elseif ($key == "TYPE2") {
                    $contents .= $cma."入学金(施設設備費)";
                } elseif ($key == "CAUTION") {
                    $contents .= $cma."※期限後受付厳禁";
                } elseif ($key == "EXAMNO" && ($this->field["OUTPUT"] == "1" || $this->field["OUTPUT"] == "2") && $row["DUPLICATE_FLG"] == 1) {
                    $contents .= $cma."*".$row[$key];
                } elseif ($this->field["OUTPUT"] == "9" && ($key == "NYUGAKU" || $key == "SETSUBI")) {
                    if ($row[$key] != "0") {
                        $contents .= $cma.$row[$key];
                    } else {
                        $contents .= $cma;
                    }
                } else {
                    $contents .= $cma.$row[$key];
                }

                $cma = ",";
            }
            $cnt++;
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
