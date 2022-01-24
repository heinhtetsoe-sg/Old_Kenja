<?php

require_once('for_php7.php');
class knjl140aModel extends Model
{
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $selected_data;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $testdiv;
    public $schoolName;
    public $lastColumn;//CSVの最後の列の値

    public function knjl140aModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode,
             $this->testdiv) = explode(":", VARS::post("GOUKAKU"));

        $db = Query::dbCheckOut();
        $query = knjl140aQuery::getSchoolName();
        $this->schoolName = $db->getOne($query); //学校名取得
        Query::dbCheckIn($db);

        $this->fixApplicantDiv = '1';
        if ($this->schoolName == 'hirogaku' && SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        //プロパティファイル
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
        } else {
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

    //合格者
    public function getDownloadStudent($db, &$contents)
    {
        $header = array("入学年度",
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
                        $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl140aQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //広島国際は賢者として取込むため、かたかなに変換
            if ($this->schoolName == 'hirogaku') {
                $row["NAME_KANA"] = mb_convert_kana($row["NAME_KANA"], "cHV", "UTF-8");
            }
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

        $query  = knjl140aQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //広島国際は賢者として取込むため、かたかなに変換
            if ($this->schoolName == 'hirogaku') {
                $row["GKANA"]           = mb_convert_kana($row["GKANA"], "cHV", "UTF-8");
                $row["GUARANTOR_KANA"]  = mb_convert_kana($row["GUARANTOR_KANA"], "cHV", "UTF-8");
            }
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
