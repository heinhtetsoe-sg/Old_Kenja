<?php

require_once('for_php7.php');

class knja233gModel extends Model
{
    public $field = array();
    public $cmd;
    public $selectdata;
    public $selectdataText;
    public $semeSDate;
    public $semeEDate;

    public function knja233gModel()
    {
    }

    public function init()
    {
        $this->field = array("B023"             =>  VARS::post("B023"),             //列名称
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DISP"             =>  VARS::post("DISP"),             //クラス生徒区分
                             "DATE"             =>  VARS::post("DATE"),             //講座名簿日付
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdataText   = VARS::post("selectdataText");

        //プロパティファイルの読み込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if ($this->cmd == "change") {
            $this->selectdata = "";
        }
    }
    
    //CSVファイル（書出）
    public function &getDownloadCsvModel()
    {
        //習熟度チェック
        $syujukudo = VARS::post("SYUJUKUDO");
        $selectdata = array();
        $selectdata = explode(",", $this->selectdata);
        $data = false;

        //ヘッダ（書出）   フィールド => ヘッダ名, ヘッダ例
        $csvItem = array("HR_NAME"          => array("現HR"),
                         "ATTENDNO"         => array("NO"),
                         "KUBUN"            => array("異動区分"),
                         "NAME"             => array("氏名"),
                         "NAME_KANA"        => array("ふりがな"),
                         "SEX"              => array("性別"),
                         "A053"             => array("出身"),
                         "EXAMNO"           => array("受験番号"),
                         "ZENNEN_HR_NAME"   => array("昨年度HR"),
                         "ZENNEN_ATTENDNO"  => array("昨年度NO"),
                         "SUBCLASS_ABBV1"   => array("選択科目名"),
                         "SUBCLASS_SMALL1"  => array("クラス"),
                         "SUBCLASS_TYPE1"   => array("種別"),
                         "SUBCLASS_ABBV2"   => array("選択科目名"),
                         "SUBCLASS_SMALL2"  => array("クラス"),
                         "SUBCLASS_TYPE2"   => array("種別"),
                         "SUBCLASS_ABBV3"   => array("選択科目名"),
                         "SUBCLASS_SMALL3"  => array("クラス"),
                         "SUBCLASS_TYPE3"   => array("種別"),
                         "SUBCLASS_ABBV4"   => array("選択科目名"),
                         "SUBCLASS_SMALL4"  => array("クラス"),
                         "SUBCLASS_TYPE4"   => array("種別"),
                         "SUBCLASS_ABBV5"   => array("選択科目名"),
                         "SUBCLASS_SMALL5"  => array("クラス"),
                         "SUBCLASS_TYPE5"   => array("種別"),
                         "SUBCLASS_ABBV6"   => array("選択科目名"),
                         "SUBCLASS_SMALL6"  => array("クラス"),
                         "SUBCLASS_TYPE7"   => array("種別"),
                         "SUBCLASS_ABBV7"   => array("選択科目名"),
                         "SUBCLASS_SMALL7"  => array("クラス"),
                         "SUBCLASS_TYPE8"   => array("種別"),
                         "SUBCLASS_ABBV8"   => array("選択科目名"),
                         "SUBCLASS_SMALL8"  => array("クラス"),
                         "SUBCLASS_TYPE8"   => array("種別")
        );

        $contents = "";

        //タイトル設定
        $title = CTRL_YEAR."年度 HR名票（選択科目一覧）";
        
        //ヘッダ取得
        $header = "";
        foreach ($csvItem as $key => $val) {
            $header .= ($header == "") ? $val[0] : ",".$val[0];
        }
        //ヘッダセット
        $contents .= $header."\n";

        //SQL取得
        $where = "('".implode("', '", $selectdata)."')";
        $db = Query::dbCheckOut();
        $query = knja233gQuery::getCsvData($this, $where, $syujukudo);
        $cnt = 0;
        $result = $db->query($query);
        $schregno = "";

        //一人の生徒に対して、科目が複数あれば複数行取得される。
        //取得された複数行をCSVでは1行で出力する。
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマ・改行があれば置換
            $row = str_replace(",", ".", $row);
            $row = str_replace("\r\n", "", $row);
            $row = str_replace("\r", "", $row);
            $row = str_replace("\n", "", $row);

            if ($row["SCHREGNO"] != $schregno) {
                $schregno = $row["SCHREGNO"];
                if ($cnt > 0) {
                    $contents .= "\n";
                }
                $cnt = 1;
                $cma = "";
            }
            foreach ($csvItem as $key => $val) {
                if ($key == "SUBCLASS_ABBV".$cnt) {
                    $contents .= $cma.$row["SUBCLASSABBV"];
                    $contents .= $cma.$row["S_CLASS"];
                    $contents .= $cma.$row["SYUJUKUDO"];
                } elseif ($cnt == 1 && strpos($key, "SUBCLASS") === false) {
                    $contents .= $cma.$row[$key];
                }
                $cma = ",";
            }
            $cnt++;
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
