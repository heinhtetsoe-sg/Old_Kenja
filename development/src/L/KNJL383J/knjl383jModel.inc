<?php

require_once('for_php7.php');
class knjl383jModel extends Model
{
    public $cmd;
    public $examyear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl383jModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");

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

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $subtitle = "";
        $data = $this->getDownloadStudent($db, $contents);

        if ($data) {
            //CSV出力
            $this->putCsv($contents, $subtitle);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSV出力
    public function putCsv($contents, $subtitle)
    {
        $filename = $this->examyear."_プレスクール".$subtitle.".csv";
        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //合格者
    public function getDownloadStudent($db, &$contents)
    {
        $header = array("ENTEXAMYEAR"       => "入学年度",
                        "EXAMNO"            => "受験番号",
                        "NAME"              => "氏名",
                        "NAME_KANA"         => "氏名かな",
                        "JUDGE_DEVIATION"   => "判定偏差値",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "PRE_HR_CLASS"      => "仮クラス",
                        "PRE_ATTENDNO"      => "仮出席番号",
                        "ATTENDFLG1"        => "出欠１",
                        "ATTENDFLG2"        => "出欠２",
                        "SCORE1"            => "得点１（国語）",
                        "SCORE2"            => "得点２（算数）",
                        "TOTAL_SCORE"       => "合計",
                        "REMARK"            => "備考",
                        "STATIONCD1"        => "バス１",
                        "STATIONCD2"        => "バス２",
                        "STATIONCD3"        => "バス３",
                        "STATIONCD4"        => "バス４",
                        "PRE_INFO1"         => "情報１",
                        "PRE_INFO2"         => "情報２",
                        "PRE_INFO3"         => "情報３",
                        $this->lastColumn   => $this->lastColumn);

        $contents = implode($header, ",")."\r\n";
        $data = false;

        $query = knjl383jQuery::selectCsvStudentQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rtdata = array("ENTEXAMYEAR"       => $row["ENTEXAMYEAR"],
                            "EXAMNO"            => $row["EXAMNO"],
                            "NAME"              => $row["NAME"],
                            "NAME_KANA"         => $row["NAME_KANA"],
                            "JUDGE_DEVIATION"   => $row["JUDGE_DEVIATION"],
                            "HR_CLASS"          => "",
                            "ATTENDNO"          => "",
                            "PRE_HR_CLASS"      => "",
                            "PRE_ATTENDNO"      => "",
                            "ATTENDFLG1"        => "",
                            "ATTENDFLG2"        => "",
                            "SCORE1"            => "",
                            "SCORE2"            => "",
                            "TOTAL_SCORE"       => "",
                            "REMARK"            => "",
                            "STATIONCD1"        => "",
                            "STATIONCD2"        => "",
                            "STATIONCD3"        => "",
                            "STATIONCD4"        => "",
                            "PRE_INFO1"         => "",
                            "PRE_INFO2"         => "",
                            "PRE_INFO3"         => "",
                            $this->lastColumn   => $this->lastColumn);
            $contents .= implode($rtdata, ",")."\r\n";
            $data = true;
        }
        $result->free();

        return $data;
    }
}
