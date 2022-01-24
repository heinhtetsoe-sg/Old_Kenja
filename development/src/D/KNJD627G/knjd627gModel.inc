<?php

require_once('for_php7.php');

class knjd627gModel extends Model
{
    public $programID;
    public $cmd;
    public $year;
    public $grade;              //選択されたクラスの年次
    public $a_rank;

    public function init()
    {
        $this->year              = CTRL_YEAR;
        $this->cmd               = VARS::request("cmd");
        $this->grade             = VARS::post("GRADE");
        $this->a_rank            = VARS::post("A_RANK");

        //プロパティファイル読込み
        $this->getPropertiesAll();
    }
    
    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;
        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        $csvItem = array("NO"          => array("番号"),
                         "NAME"        => array("氏名"),
                         "SOUTEN"      => array("総点"),
                         "AVG"         => array("平均"),
                         "REMARKS"     => array("未修得科目"),
                         "SOUTEN_SUB1" => array("1年")
        );

        $contents = "";

        //タイトル設定
        $db = Query::dbCheckOut();

        $title = "";
        $title = CTRL_YEAR."年度 進級判定会議資料";
        
        $grade = $this->grade == "01" ? "1" : "2";
        $contents .=  CTRL_YEAR."年度 高校 ".$grade."学年";
        $contents .=  "    A  ".$this->a_rank."点以上"."\n";
        
        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        $contents .= $header."\n";

        //SQL取得
        $seme = $db->getOne(knjd627gQuery::getLastSeme($this));
        
        $query = knjd627gQuery::getSchregSouten($this, $seme);
        $flgB = "off";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);
            
            $cma = "";
            if ($flgB == "off" && $row["SOUTEN"] < $this->a_rank) {
                $contents .= "\n"."\n";
                $contents .=  CTRL_YEAR."年度 高校 ".$grade."学年";
                $contents .=  "    B  ".$this->a_rank."点未満"."\n";
                $contents .= $header."\n";
                $flgB = "on";
            }
            foreach ($csvItem as $key => $val) {
                if ($key == "NO") {
                    $no = strlen($row["ATTENDNO"]) == 1 ? " ".$row["ATTENDNO"] : $row["ATTENDNO"];
                    $contents .= $cma.$row["HR_CLASS_NAME1"].$no;
                } else {
                    $contents .= $cma.$row[$key];
                }
                
                $cma = ",";
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
