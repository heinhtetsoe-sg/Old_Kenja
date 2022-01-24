<?php

require_once('for_php7.php');

class knje373bModel extends Model
{
    public $field  = array();
    public $cmd;
    public $schoolKind;

    public function knje373bModel()
    {
    }

    public function init()
    {

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("GRADE"          => VARS::post("GRADE"),          //学年
                             "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"), //年組
                             "selectdata"     => VARS::post("selectdata"),     //選択情報
                             "DBNAME"         => VARS::post("DBNAME")          //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = "H";
    }

    // ＣＳＶ出力 TODO 未実装
    public function getDownloadModel()
    {
        $header = array("年度",
                        "年組",
                        "出席番号",
                        "表示氏名",
                        "ふりがな",
                        "学部",
                        "学科",
                        "専修");

        $db = Query::dbCheckOut();

        $header_data = false;
        $contents = $contents2 = "";

        $query = knje373bQuery::getSchoolCd($this);
        $this->schoolCd = $db->getOne($query);

        $query = knje373bQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            // データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);
            $contents2 .= implode($row, ",")."\n";

            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            $contents = implode($header, ",")."\n";
            $contents .= $contents2;
            $title = "クラス別推薦者名簿";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
