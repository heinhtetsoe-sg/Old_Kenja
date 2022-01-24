<?php

require_once('for_php7.php');

class knjl640iModel extends Model
{
    public $field = array();
    public $cmd;
    public $examYear;
    public $applicantdiv = "2";

    public function knjl640iModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->s_examno);
            unset($this->e_examno);
        }

        $this->examYear = CTRL_YEAR+1;

        $this->field = array("DEPT"                 =>  VARS::post("DEPT"),            //学科
                             "TESTDIV"              =>  VARS::post("TESTDIV"),         //入試区分
                             "ORDER_BY"             =>  VARS::post("ORDER_BY"),        //出力順
                             "OUTPUT_TYP"           =>  VARS::post("OUTPUT_TYP"),      //帳票タイプ
                             "OUTPUT_TYP1_SUB"      =>  VARS::post("OUTPUT_TYP1_SUB"), //帳票別納金種別1
                             "OUTPUT_TYP2_SUB"      =>  VARS::post("OUTPUT_TYP2_SUB"), //帳票別納金種別2
                             "OUTPUT_TYP3_SUB"      =>  VARS::post("OUTPUT_TYP3_SUB")  //帳票別納金種別3
                            );

        $this->cmd = VARS::request("cmd");
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;
        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        if ($this->field["OUTPUT_TYP"] == "1") {
            $csvItem = array("ROW_NUMBER"     => array("No."),
                             "SUBJECT"        => array("学科"),
                             "EXAMNO"         => array("受験番号"),
                             "NAME"           => array("氏名"),
                             "NAME_KANA"      => array("フリガナ"),
                             "SEX"            => array("性別"),
                             "FS_CD"          => array("中学校コード"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校名"),
                             "DEPOSIT"        => array("納入金"),
                             "TOTAL_MONEY"    => array("累計"),
                             "DEPOSIT_DATE"   => array("入金日"),
                             "REMARK"         => array("備考")
            );
        } elseif ($this->field["OUTPUT_TYP"] == "2") {
            $csvItem = array("DISP_DEPOSIT_DATE"  => array("入金日"),
                             "DISP_WEEKDAY"       => array(""),
                             "SEX"                => array(""),
                             "GENERAL_CNT"        => array("普通科"),
                             "TECHNICAL_CNT"      => array("工業科"),
                             "SUMCNT"             => array("計"),
                             "TOTAL"              => array("累計"),
                             "REMARK"             => array("備考")
            );
        } elseif ($this->field["OUTPUT_TYP"] == "3") {
            $csvItem = array("ROW_NUMBER"     => array("No."),
                             "SUBJECT"        => array("学科"),
                             "EXAMNO"         => array("受験番号"),
                             "NAME"           => array("氏名"),
                             "NAME_KANA"      => array("フリガナ"),
                             "SEX"            => array("性別"),
                             "FS_CD"          => array("中学校コード"),
                             "FINSCHOOL_NAME_ABBV" => array("中学校"),
                             "DEPOSIT"        => array("納入金"),
                             "TOTAL_MONEY"    => array("累計"),
                             "DEPOSIT_DATE"   => array("入金日"),
                             "REMARK"         => array("備考")
            );
        } elseif ($this->field["OUTPUT_TYP"] == "4") {
            $csvItem = array("ROW_NUMBER"       => array("No."),
                             "HR_CLASS"         => array("組"),
                             "ATTENDNO"         => array("番号"),
                             "EXAMNO"           => array("受験番号"),
                             "SCHREGNO"         => array("学籍番号"),
                             "NAME"             => array("氏名"),
                             "NAME_KANA"        => array("フリガナ"),
                             "SEX"              => array("性別"),
                             "FINSCHOOL_NAME_ABBV"   => array("中学校"),
                             "HONOR_REASON"     => array("特待理由"),
                             "ZIPCD"            => array("郵便番号"),
                             "ADDRESS"          => array("住所"),
                             "TELNO"            => array("電話番号"),
                             "DORMITORIES_NAME" => array("寮名")
            );
        } elseif ($this->field["OUTPUT_TYP"] == "5") {
            $csvItem = array("EXAMNO"           => array("受験番号"),
                             "FINSCHOOL_NAME"   => array("出身中学校"),
                             "NAME"             => array("入寮希望者名"),
                             "DORMITORIES_NAME" => array("寮名"),
                             "HONOR_REASON"     => array("特待理由")
            );
        }

        $contents = "";

        //タイトル設定
        $db = Query::dbCheckOut();
        $query = knjl640iQuery::getTestDivMst($this->examYear, $this->applicantdiv, $this->field["TESTDIV"]);
        $testDRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $testDName = $testDRow["TESTDIV_NAME"];

        $title = "";
        if ($this->field["OUTPUT_TYP"] == "1") {
            $depositKindName = "";
            if ($this->field["OUTPUT_TYP1_SUB"] == "1") {
                $depositKindName = "施設設備費";
            } else {
                $depositKindName = "入学申込金";
            }
            $title = $this->examYear."年度 ".$depositKindName." 入金者リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT_TYP"] == "2") {
            $depositKindName = "";
            if ($this->field["OUTPUT_TYP2_SUB"] == "1") {
                $depositKindName = "施設設備費";
            } else {
                $depositKindName = "入学申込金";
            }
            $title = $this->examYear."年度 入学手続者(".$testDName.") (".$depositKindName.")";
        } elseif ($this->field["OUTPUT_TYP"] == "3") {
            $depositKindName = "";
            if ($this->field["OUTPUT_TYP3_SUB"] == "1") {
                $depositKindName = "施設設備費";
            } else {
                $depositKindName = "入学申込金";
            }
            $title = $this->examYear."年度 ".$depositKindName." 未入金者リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT_TYP"] == "4") {
            $title = $this->examYear."年度 入寮者リスト(".$testDName.")";
        } elseif ($this->field["OUTPUT_TYP"] == "5") {
            $title = $this->examYear."年度 入寮希望者案内用ラベル(".$testDName.")";
        }

        //一般受験者(特待無し)の入学申込金、施設設備費を取得
        $defaultCostMap = $db->getRow(knjl640iQuery::getDefaultCost($this), DB_FETCHMODE_ASSOC);
        $entryFee       = (isset($defaultCostMap["ENTRY_FEE"])) ? $defaultCostMap["ENTRY_FEE"] : "";
        $equipmentCost  = (isset($defaultCostMap["EQUIPMENT_COST"])) ? $defaultCostMap["EQUIPMENT_COST"] : "";

        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        $contents .= $header."\n";

        //SQL取得
        if ($this->field["OUTPUT_TYP"] == "1") {
            $query = knjl640iQuery::getNyugakuTetsudukiSyuryousyaQuery($this);
        } elseif ($this->field["OUTPUT_TYP"] == "2") {
            $query = knjl640iQuery::getNyugakuTetsudukiSyuryousyaSuQuery($this);
        } elseif ($this->field["OUTPUT_TYP"] == "3") {
            $query = knjl640iQuery::getMinyukinsyaListQuery($this);
        } elseif ($this->field["OUTPUT_TYP"] == "4") {
            $query = knjl640iQuery::getNyuryouKibousyaListQuery($this);
        } elseif ($this->field["OUTPUT_TYP"] == "5") {
            $query = knjl640iQuery::getNyuryouKibousyaLabelQuery($this);
        }

        $result = $db->query($query);
        if ($this->field["OUTPUT_TYP"] == "1" || $this->field["OUTPUT_TYP"] == "3") {
            $total = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);
    
                $deposit = ($this->field["OUTPUT_TYP{$this->field["OUTPUT_TYP"]}_SUB"] == "1") ? $equipmentCost : $entryFee;
                if ($row["SP_CD"] != "" && $row["SP_REASON_CD"] != "") {
                    $deposit = $row["DEPOSIT"];
                }

                $row["DEPOSIT"] = "\\".$deposit;
                $total += $deposit;
                $row["TOTAL_MONEY"] = "\\".$total;
                $cma = "";

                foreach ($csvItem as $key => $val) {
                    $contents .= $cma.$row[$key];
                    
                    $cma = ",";
                }

                $contents .= "\n";
                $data = true;
            }
        } elseif ($this->field["OUTPUT_TYP"] == "2") {
            $tmpDepositDate = "";
            $generalTotal = 0;
            $technicalTotal = 0;
            $sumcnt = 0;
            $total = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマ・改行があれば置換
                $row = str_replace(",", ".", $row);
                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r", "", $row);
                $row = str_replace("\n", "", $row);

                if ($tmpDepositDate != "" && $row["DEPOSIT_DATE"] != $tmpDepositDate) {
                    $contents .= ",,計,".$generalTotal.",".$technicalTotal.",".$sumcnt.",".$total.",\n";
                    $generalTotal = 0;
                    $technicalTotal = 0;
                    $sumcnt = 0;
                    $tmpDepositDate = "";
                    $row["DISP_DEPOSIT_DATE"] = $row["DEPOSIT_DATE"];
                    $row["DISP_WEEKDAY"] = $row["WEEKDAY"];
                }

                if ($tmpDepositDate == "") {
                    $row["DISP_DEPOSIT_DATE"] = $row["DEPOSIT_DATE"];
                    $row["DISP_WEEKDAY"] = $row["WEEKDAY"];
                    $tmpDepositDate = $row["DEPOSIT_DATE"];
                }

                //合計行用
                $generalTotal += $row["GENERAL_CNT"];     //普通科計
                $technicalTotal += $row["TECHNICAL_CNT"]; //工業科計
                $sumcnt += $row["SUMCNT"]; //計
                $total += $row["SUMCNT"]; //累計

                $cma = "";
                foreach ($csvItem as $key => $val) {
                    $contents .= $cma.$row[$key];
                    
                    $cma = ",";
                }

                $contents .= "\n";
                $data = true;
            }
            $contents .= ",,計,".$generalTotal.",".$technicalTotal.",".$sumcnt.",".$total.",\n";
        } else {
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
