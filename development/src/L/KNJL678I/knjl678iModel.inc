<?php

require_once('for_php7.php');

class knjl678iModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $examType = 1;
    public $applicantDiv = '2'; // 2:高校 固定

    public function knjl678iModel()
    {
    }

    public function init()
    {
        $this->examyear = CTRL_YEAR + 1;

        $this->field = array("OUTPUT"          =>  VARS::post("OUTPUT"),       //出力
                             "FINSCHOOLCD"     =>  VARS::post("FINSCHOOLCD"),  //中学校
                             "FS_AREA_CD"      =>  VARS::post("FS_AREA_CD")    //地区
                            );

        $this->cmd = VARS::request("cmd");
    }

    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        $data = false;
        //ヘッダ（書出）   フィールド => ヘッダ名
        $csvItem = array("EXAMNO"           => array("番号"),
                         "FS_AREA_NAME"     => array("地区名"),
                         "FINSCHOOL_NAME"   => array("中学校名"),
                         "NAME"             => array("氏名"),
                         "TESTDIV_NAME"     => array("試験区分"),
                         "CONSUL_COURSE"    => array("相談コース"),
                         "COMMON_TEST"      => array("共通テスト"),
                         "SP_CD"            => array("特待記号"),
                         "SP_REASON"        => array("特待種類"),
                         "CHANGE_TEXT"      => array("変更内容"),
                         "REMARK"           => array("備考"),
                         "EXCLUSION"        => array("除外")
        );
        $contents = "";

        //タイトル設定
        $title = "中学生リスト　（";
        if ($this->field["OUTPUT"] == "1") {
            $title .= "中学校別";
        } elseif ($this->field["OUTPUT"] == "2") {
            $title .= "地区別";
        } elseif ($this->field["OUTPUT"] == "3") {
            $title .= "特待学業";
        } elseif ($this->field["OUTPUT"] == "4") {
            $title .= "特待部活動";
        } elseif ($this->field["OUTPUT"] == "5") {
            $title .= "欠席";
        }
        $title .= "）";
        
        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        $contents .= $header."\n";

        $db = Query::dbCheckOut();
        //SQL取得
        $query = knjl678iQuery::getApplicantList($this);
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
}
