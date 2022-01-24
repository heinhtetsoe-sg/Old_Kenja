<?php

require_once('for_php7.php');
class knjl140mModel extends Model
{
    public $cmd;
    public $examyear;
    public $applicantdiv;
    public $outputdiv;
    public $selected_data;
    public $testdiv;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $schoolName;
    public $field;
    public $lastColumn;//CSVの最後の列の値

    public function knjl140mModel()
    {
    }

    public function init()
    {
        $this->examyear      = VARS::post("YEAR");
        $this->applicantdiv = "1";  //固定値
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");
        $this->testdiv       = VARS::post("TESTDIV");
        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode) = explode(":", VARS::post("TESTDIV"));

        $this->field = array(
                     "OUTPUTDIV"       => VARS::post("OUTPUTDIV"),       // 出力対象
                     "OUTPUTSCORE"     => VARS::post("OUTPUTSCORE")      // 成績も出力
                  );
        $db = Query::dbCheckOut();
        $query = knjl140mQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);

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
        if ($this->outputdiv == 1) {
            $dataFlg  = $this->getDownloadStudent($db, $contents);
        } elseif ($this->outputdiv == 2) {
            $dataFlg  = $this->getDownloadGuardian($db, $contents);
            $subtitle = "保護者";
        }

        if ($dataFlg) {
            //CSV出力
            $this->putCsv($db, $contents, $subtitle);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //CSV出力
    public function putCsv($db, $contents, $subtitle)
    {
        $filename = $this->examyear."_入学者".$subtitle.".csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //入学者
    public function getDownloadStudent($db, &$contents)
    {
        $header1 = array("入学年度",
                        "※受験番号",
                        "※入学区分",
                        "※学年",
                        "※組",
                        "出席番号",
                        "※内外区分",
                        "課程コード",
                        "学科コード",
                        "コースコード",
                        "氏名",
                        "氏名かな",
                        "生年月日",
                        "性別",
                        "出身学校コード",
                        "出身学校名",
                        "出身学校卒業日",
                        "郵便番号",
                        "住所１",
                        "住所２",
                        "電話番号",
                        "FAX番号",
                        "EMAIL",
                        "緊急連絡先",
                        "緊急連絡電話番号",
                        "スカラシップ区分",
                        "課程入学年度",
                        "地区コード",
                        "受験番号",
                        );
        $header2 = array("入試区分",
                         $this->lastColumn);
        if ($this->Properties["UseMusyouKaisu"] == "1") {
            $header = array_merge($header1, array("無償回数"));
            $header = array_merge($header, $header2);
        } else {
            $header = array_merge($header1, $header2);
        }
        if ($this->field["OUTPUTSCORE"] == 1) {
            if ($this->Properties["useScoreMongonDeviation"] == "1") {
                array_push($header, "偏差値");
            } else {
                array_push($header, "成績");
            }
        }

        //科目取得
        $query  = knjl140mQuery::getVNameMst($this, "L009");
        $result = $db->query($query);
        $this->l009 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->l009[$row["NAMECD2"]] = $row;
        }
        $result->free();

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl140mQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    //保護者
    public function getDownloadGuardian($db, &$contents)
    {
        $header = array("学年",
                        "クラス",
                        "出席番号",
                        "氏名",
                        "※受験番号",
                        "※続柄",
                        "保護者氏名",
                        "保護者氏名かな",
                        "性別",
                        "生年月日",
                        "郵便番号",
                        "住所1",
                        "住所2",
                        "電話番号",
                        "FAX番号",
                        "E-MAIL",
                        "職種コード",
                        "勤務先名称",
                        "勤務先電話番号",
                        "保証人続柄",
                        "保証人氏名",
                        "保証人氏名かな",
                        "保証人性別",
                        "保証人郵便番号",
                        "保証人住所1",
                        "保証人住所2",
                        "保証人電話番号",
                        "保証人職種コード",
                        "兼ねている公職",
                        $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        $query  = knjl140mQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
