<?php

require_once('for_php7.php');
class knjb1254Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $lastColumn;//CSVの最後の列の値

    public function knjb1254Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR_SEMESTER" =>  VARS::post("YEAR_SEMESTER"),//年度学期
                             "RIREKI_CODE"   =>  VARS::post("RIREKI_CODE"),  //履修履歴コード
                             "STD_CNT"       =>  VARS::post("STD_CNT")       //受講生徒数
                            );
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //生成処理
    public function &getCreateModel()
    {
        knjb1254Query::getDeleteQuery($this);
        knjb1254Query::getCreateQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //ＣＳＶ出力
    public function getCsvModel()
    {

        //ヘッダデータ
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("YEAR"            => "※年度",
                            "SEMESTER"        => "※学期",
                            "SELECT_DATE"     => "※履修登録日付",
                            "GROUPCD"         => "※選択グループコード",
                            "CLASSCD"         => "※教科コード",
                            "SCHOOL_KIND"     => "※学校校種",
                            "CURRICULUM_CD"   => "※教育課程コード",
                            "SUBCLASSCD"      => "※科目コード",
                            "GRADE"           => "※学年",
                            "HR_CLASS"        => "組",
                            "ATTENDNO"        => "出席番号",
                            "NAME"            => "氏名",
                            "SCHREGNO"        => "※学籍番号",
                            $this->lastColumn => $this->lastColumn);
        } else {
            $header = array("YEAR"            => "※年度",
                            "SEMESTER"        => "※学期",
                            "SELECT_DATE"     => "※履修登録日付",
                            "GROUPCD"         => "※選択グループコード",
                            "CLASSCD"         => "※教科コード",
                            "CURRICULUM_CD"   => "※教育課程コード",
                            "SUBCLASSCD"      => "※科目コード",
                            "GRADE"           => "※学年",
                            "HR_CLASS"        => "組",
                            "ATTENDNO"        => "出席番号",
                            "NAME"            => "氏名",
                            "SCHREGNO"        => "※学籍番号",
                            $this->lastColumn => $this->lastColumn);
        }

        $contents  = "";    //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ出力
        $query = knjb1254Query::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "履修登録データ";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
