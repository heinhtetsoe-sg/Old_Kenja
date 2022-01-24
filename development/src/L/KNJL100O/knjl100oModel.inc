<?php

require_once('for_php7.php');
class knjl100oModel extends Model
{
    public $examcourse;
    public $entclass;
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $lastColumn;//CSVの最後の列の値

    public function knjl100oModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->examcourse   = VARS::post("EXAMCOURSE");
        $this->entclass     = VARS::post("ENTCLASS");
        $this->outputdiv    = VARS::post("OUTPUTDIV");

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
            $data = $this->getDownloadStudent($db, $contents);
        } else {
            $data = $this->getDownloadGuardian($db, $contents);
            $subtitle = "保護者";
        }

        if ($data) {
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
        //合格コース名
        $coursename = $db->getOne(knjl100oQuery::getExamCourseName($this));

        $filename = $this->examyear."_入学者".$subtitle."（".$coursename."）.csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //合格者
    public function getDownloadStudent($db, &$contents)
    {
        $header = array("ENTERYEAR"          => "入学年度",
                        "SCHREGNO"           => "学籍番号",
                        "ENT_DIV"            => "入学区分",
                        "HR_CLASS"           => "組",
                        "ATTENDNO"           => "出席番号",
                        "INOUTCD"            => "内外区分",
                        "COURSECD"           => "課程コード",
                        "MAJORCD"            => "学科コード",
                        "COURSECODE"         => "コースコード",
                        "NAME"               => "氏名",
                        "NAME_KANA"          => "氏名かな",
                        "BIRTHDAY"           => "生年月日",
                        "SEX"                => "性別",
                        "FINSCHOOLCD"        => "出身学校コード",
                        "FINSCHOOLGRADDATE"  => "出身学校卒業日",
                        "ZIPCD"              => "郵便番号",
                        "ADDRESS1"           => "住所１",
                        "ADDRESS2"           => "住所２",
                        "TELNO"              => "電話番号",
                        "FAXNO"              => "FAX番号",
                        "EMAIL"              => "e-mail",
                        "EMERGENCYCALL"      => "急用先名称",
                        "EMERGENCYTELNO"     => "急用先電話番号",
                        "SCALASHIPDIV"       => "スカラシップ",
                        $this->lastColumn    => $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $data = false;

        //SQL文発行
        $query = knjl100oQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $data = true;
        }
        $result->free();

        return $data;
    }

    //保護者
    public function getDownloadGuardian($db, &$contents)
    {
        $header = array("GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "SCHREGNO"                  => "※学籍番号",
                        "RELATIONSHIP"              => "※続柄",
                        "GUARD_NAME"                => "保護者氏名",
                        "GUARD_KANA"                => "保護者氏名かな",
                        "GUARD_SEX"                 => "性別",
                        "GUARD_BIRTHDAY"            => "生年月日",
                        "GUARD_ZIPCD"               => "郵便番号",
                        "GUARD_ADDR1"               => "住所1",
                        "GUARD_ADDR2"               => "住所2",
                        "GUARD_TELNO"               => "電話番号",
                        "GUARD_FAXNO"               => "FAX番号",
                        "GUARD_E_MAIL"              => "E-MAIL",
                        "GUARD_JOBCD"               => "職種コード",
                        "GUARD_WORK_NAME"           => "勤務先名称",
                        "GUARD_WORK_TELNO"          => "勤務先電話番号",
                        "GUARANTOR_RELATIONSHIP"    => "保証人続柄",
                        "GUARANTOR_NAME"            => "保証人氏名",
                        "GUARANTOR_KANA"            => "保証人氏名かな",
                        "GUARANTOR_SEX"             => "保証人性別",
                        "GUARANTOR_ZIPCD"           => "保証人郵便番号",
                        "GUARANTOR_ADDR1"           => "保証人住所1",
                        "GUARANTOR_ADDR2"           => "保証人住所2",
                        "GUARANTOR_TELNO"           => "保証人電話番号",
                        "GUARANTOR_JOBCD"           => "保証人職種コード",
                        "PUBLIC_OFFICE"             => "兼ねている公職",
                        $this->lastColumn           => $this->lastColumn);

        $contents = implode($header, ",")."\n";
        $data = false;

        $query = knjl100oQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $data = true;
        }
        $result->free();

        return $data;
    }
}
