<?php

require_once('for_php7.php');

class knje373cModel extends Model
{
    public $field  = array();
    public $cmd;
    public $schoolKind;

    public function knje373cModel()
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
                             "DBNAME"         => VARS::post("DBNAME")          //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->schoolKind = "H";
    }

    // ＣＳＶ出力 TODO 未実装
    public function getDownloadModel()
    {
        $header = array("年度",
                        "学籍番号",
                        "表示氏名",
                        "ふりがな",
                        "学部",
                        "学科",
                        "専修");

        $db = Query::dbCheckOut();

        $header_data = false;
        $contents = $contents2 = "";

        $query = knje373cQuery::getSchoolCd($this);
        $this->schoolCd = $db->getOne($query);

        $query = knje373cQuery::selectCsvQuery($this);
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
            $title = "学部学科別志望者名簿";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
