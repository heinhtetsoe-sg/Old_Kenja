<?php

require_once('for_php7.php');
class knjl364kModel extends Model
{
    public $examcourseall = array();
    public $examcourse;
    public $cmd;
    public $examyear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl364kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->examcourse   = VARS::post("EXAMCOURSE");

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

        //DB接続
        $db = Query::dbCheckOut();
        //中高判定 1:中学 2:高校
        $jhflg = (1 == $db->getOne(knjl364kQuery::getJorH())) ? 1 : 2;

        //SQL文発行 NO005
        $query = knjl364kQuery::selectCvsQuery($this, $bankcd, $jhflg);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //住所表示
            $row["GUARD_ADDR1"] = str_replace("　", " ", $row["GUARD_ADDR1"]);  //全角スペースは半角スペースに変換
            $add = explode(" ", $row["GUARD_ADDR1"], 2);     //最初にある空白で２つに分ける
            $row["GUARD_ADDR1"] = $add[0];
            $row["GUARD_ADDR2"] = $add[1];
            $row["GUARANTOR_ADDR1"] = $add[0];
            $row["GUARANTOR_ADDR2"] = $add[1];

            $fs_grdyear = $row["FS_GRDYEAR"]? $row["FS_GRDYEAR"]."/3/31" : "";

            $contents .= implode($row, ",")."\n";
            $data = true;
        }
        $result->free();

        //合格コース名
        if ($this->examcourse != "99999999") {
            $coursename = $db->getOne(knjl364kQuery::getExamCourseName($this));
        } else {
            $coursename = "全部";
        }

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $filename = $this->examyear."_入学者（".$coursename."）.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
