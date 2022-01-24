<?php

require_once('for_php7.php');
require_once 'Log.php';

class knjz067bModel extends Model
{
    public $cmd;
    public $ibyear;
    public $field  = array();
    public $data  = array();
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ibyear);
        }
    
        //年度（初期値）
        $this->ibyear = ($this->ibyear == "") ? CTRL_YEAR : $this->ibyear;
        if (VARS::post("IBYEAR") != "") {
            $this->ibyear = VARS::post("IBYEAR");
        }
        $this->cmd         = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    
    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        //ヘッダ、書出
        $header = array("年度",
                        "IB教科コード",
                        "",
                        "IB教科名",
                        "1年",
                        "2年",
                        "3年",
                        "4年",
                        "5・6年",
                        $this->lastColumn);
        $contents = implode(",", $header) . "\r\n";
        
        $db = Query::dbCheckOut();

        $conma = ',';
        $lf = "\r\n";   //改行
        //出力対象のIB教科を取得
        $query = knjz067bQuery::getIBClassReplace($this);
        $result1 = $db->query($query);
        while ($ibClassRow = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
            //1学年から6学年(5,6年は同じようにチェック)
            for ($setGrade = 1; $setGrade <= 5; $setGrade++) {
                //IB教科情報をセット
                if ($setGrade == "1") {
                    $contents .= $this->ibyear;
                    $contents .= ",".$ibClassRow["IBCLASSCD"];
                    $contents .= ",".$ibClassRow["IBPRG_COURSE"];
                    $contents .= ",".$ibClassRow["IBCLASSNAME_ENG"];
                }
                //学年データがあるかチェック
                $getDataCount = "";
                $getDataCount = $db->getOne(knjz067bQuery::getIBClassDataCount($this, $ibClassRow, sprintf("%02d", $setGrade)));
                //取得した教科を学年ごとに回す(複数ある場合は'/'で分ける)
                if ($getDataCount > 0) {
                    $query = knjz067bQuery::getCsvData($this, $ibClassRow, sprintf("%02d", $setGrade));
                    $result2 = $db->query($query);
                    $count = 0;
                    while ($row = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                        //データをセット
                        if ($count > 0) {
                            $contents .= '/';
                        } else {
                            $contents .= ",";
                        }
                        $contents .= $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].' '.$row["CLASSNAME"];
                        $count++;
                        $data = true;
                    }
                    $result2->free();
                } else {
                    $contents .= ',';
                }
            }
            $contents .= ','.$this->lastColumn.$lf;
        }
        $result1->free();
        Query::dbCheckIn($db);
        if ($data) {
            $couse="IB教科_学習指導要領教科_関連付け一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
