<?php
class knjl043vModel extends Model
{
    public $cmd;
    public $examyear;
    public $outputdiv;
    public $selected_data;
    public $goukaku;
    public $suc_coursecd;
    public $suc_majorcd;
    public $suc_coursecode;
    public $testdiv;
    public $schoolName;
    public $field;
    public $lastColumn;//CSVの最後の列の値

    public function knjl043vModel()
    {
    }

    public function init()
    {
        $this->examyear  = CTRL_YEAR + 1;

        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");

        $this->field = array(
                     "OUTPUTDIV"         => VARS::post("OUTPUTDIV"),        // 出力対象
                     "EXAM_SCHOOL_KIND"  => VARS::post("EXAM_SCHOOL_KIND"), // 校種
                     "APPLICANT_DIV"     => VARS::post("APPLICANT_DIV"),    //入試区分
                     "COURSE_DIV"        => VARS::post("COURSE_DIV"),       //志望コース
                     "FREQUENCY"         => VARS::post("FREQUENCY"),        //回数
                  );

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

    //入学者
    public function getDownloadStudent($db, &$contents)
    {
        //ヘッダ
        $header = array();
        $header["YEAR"]               = "入学年度";
        $header["SCHREGNO"]           = "志願者ID";
        $header["ENTDIV"]             = "※入学区分";
        $header["GRADE"]              = "※学年";
        $header["HR_CLASS"]           = "※組";
        $header["ATTENDNO"]           = "出席番号";
        $header["INOUTCD"]            = "※内外区分";
        $header["COURSECD"]           = "課程コード";
        $header["MAJORCD"]            = "学科コード";
        $header["COURSECODE"]         = "コースコード";
        $header["NAME"]               = "氏名";
        $header["NAME_KANA"]          = "氏名かな";
        $header["BIRTHDAY"]           = "生年月日";
        $header["SEX"]                = "性別";
        $header["FINSCHOOLCD"]        = "出身学校コード";
        $header["FINSCHOOL_NAME"]     = "出身学校名";
        $header["FINISH_DATE"]        = "出身学校卒業日";
        $header["ZIPCD"]              = "郵便番号";
        $header["ADDR1"]              = "住所１";
        $header["ADDR2"]              = "住所２";
        $header["TELNO"]              = "電話番号";
        $header["FAXNO"]              = "FAX番号";
        $header["EMAIL"]              = "EMAIL";
        $header["EMERGENCYCALL"]      = "緊急連絡先";
        $header["EMERGENCYTELNO"]     = "緊急連絡電話番号";
        $header["SCALASHIPDIV"]       = "スカラシップ区分";
        $header["KATEI_ENTEXAMYEAR"]  = "課程入学年度";
        $header["TIKUCD"]             = "地区コード";
        $header["RECEPTNO"]           = "受験番号";
        $header["APPLICANT_DIV"]      = "入試区分";
        $header["DUMMY"]              = $this->lastColumn;

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl043vQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //前処理
            $this->preprocess($row);

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    public function preprocess(&$row)
    {
        //値コピー
        $row["SCHREGNO"] = $row["EXAMNO"];
        if ($this->outputdiv == 1) {
            $row["KATEI_ENTEXAMYEAR"] = $row["YEAR"];
        }
        //日付形式変換
        $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
        if ($this->outputdiv == 1) {
            $row["FINISH_DATE"] = str_replace("-", "/", $row["FINISH_DATE"]);
        }

        //NULLセット
        if ($this->outputdiv == 2) {
            $row["NAME"] = "";
            $row["SEX"] = "";
            $row["BIRTHDAY"]  = "";
        }

        //値セット
        $row["DUMMY"] = $this->lastColumn;
    }

    //保護者
    public function getDownloadGuardian($db, &$contents)
    {
        //ヘッダ
        $header = array();
        $header["GRADE"]                    = "学年";
        $header["HR_CLASS"]                 = "クラス";
        $header["ATTENDNO"]                 = "出席番号";
        $header["NAME"]                     = "氏名";
        $header["SCHREGNO"]                 = "志願者ID";
        $header["RELATION"]                 = "※続柄";
        $header["GUARD_NAME"]               = "保護者氏名";
        $header["GUARD_NAME_KANA"]          = "保護者氏名かな";
        $header["SEX"]                      = "性別";
        $header["BIRTHDAY"]                 = "生年月日";
        $header["GUARD_ZIP"]                = "郵便番号";
        $header["GUARD_ADDR1"]              = "住所1";
        $header["GUARD_ADDR2"]              = "住所2";
        $header["GUARD_TELNO"]              = "電話番号";
        $header["GFAXNO"]                   = "FAX番号";
        $header["EMAIL"]                    = "E-MAIL";
        $header["GUARD_JOBCD"]              = "職種コード";
        $header["GUARD_WORK_NAME"]          = "勤務先名称";
        $header["GUARD_WORK_TELNO"]         = "勤務先電話番号";
        $header["GUARANTOR_GRELATIONSHIP"]  = "保証人続柄";
        $header["GUARANTOR_NAME"]           = "保証人氏名";
        $header["GUARANTOR_KANA"]           = "保証人氏名かな";
        $header["GUARANTOR_SEX"]            = "保証人性別";
        $header["GUARANTOR_ZIPCD"]          = "保証人郵便番号";
        $header["GUARANTOR_ADDR1"]          = "保証人住所1";
        $header["GUARANTOR_ADDR2"]          = "保証人住所2";
        $header["GUARANTOR_TELNO"]          = "保証人電話番号";
        $header["GUARANTOR_JOBCD"]          = "保証人職種コード";
        $header["PUBLIC_OFFICE"]            = "兼ねている公職";
        $header["DUMMY"]                    = $this->lastColumn;

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl043vQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //前処理
            $this->preprocess($row);

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
