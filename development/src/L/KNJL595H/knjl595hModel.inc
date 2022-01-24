<?php

require_once('for_php7.php');
class knjl595hModel extends Model
{
    public $cmd;
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

    public function knjl595hModel()
    {
    }

    public function init()
    {
        $this->cmd           = VARS::request("cmd");
        $this->outputdiv     = VARS::post("OUTPUTDIV");
        $this->selected_data = VARS::post("SELECTED_DATA");
        $this->goukaku       = VARS::post("GOUKAKU");
        list($this->suc_coursecd,
             $this->suc_majorcd,
             $this->suc_coursecode) = explode(":", VARS::post("GOUKAKU"));

        $this->field = array("YEAR"                => VARS::post("YEAR"),              //年度
                              "APPLICANTDIV"        => VARS::post("APPLICANTDIV"),     //学校種別
                              "DISTINCT_ID"         => VARS::post("DISTINCT_ID"),      //入試判別
                              "OUTPUTDIV"           => VARS::post("OUTPUTDIV"),       // 出力対象
                              "OUTPUTDIV3TEMPL"     => VARS::post("OUTPUTDIV3TEMPL"), // テンプレート
                              "OUTPUTSCORE"         => VARS::post("OUTPUTSCORE")      // 成績も出力
                            );

        $db = Query::dbCheckOut();
        $query = knjl595hQuery::getSchoolName();
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
        $filename = $this->field["YEAR"]."_入学者".$subtitle.".csv";

        Query::dbCheckIn($db);

        //ファイルダウンロード
        common::downloadContents($contents, $filename);
    }

    //入学者
    public function getDownloadStudent($db, &$contents)
    {
        $header = array();

        $header["ENTEXAMYEAR"]          = "※入学年度";
        $header["SEMESTER"]             = "※学期";
        $header["SCHREGNO"]             = "※学籍番号";
        $header["ENT_DATE"]             = "※入学日";
        $header["ENT_DIV"]              = "※入学区分";
        $header["GRADE"]                = "※学年";
        $header["HR_CLASS"]             = "※組";
        $header["ATTENDNO"]             = "※出席番号";
        $header["INOUTCD"]              = "※内外区分";
        $header["COURSECD"]             = "※課程コード";
        $header["MAJORCD"]              = "※学科コード";
        $header["COURSECODE"]           = "コースコード";
        $header["NAME"]                 = "※氏名";
        $header["NAME_KANA"]            = "氏名かな";
        $header["BIRTHDAY"]             = "生年月日";
        $header["SEX"]                  = "性別";
        $header["FS_CD"]                = "出身学校コード";
        $header["FINSCHOOLGRADDATE"]    = "出身学校卒業日";
        $header["PRISCHOOLCD"]          = "塾コード";
        $header["DORMITORY_FLG"]        = "寮生フラグ";
        $header["ZIPCD"]                = "郵便番号";
        $header["ADDRESS1"]             = "住所１";
        $header["ADDRESS2"]             = "住所２";
        $header["TELNO"]                = "電話番号";
        $header["FAXNO"]                = "FAX番号";
        $header["EMAIL"]                = "EMAIL";
        $header["EMERGENCYCALL"]        = "緊急連絡先";
        $header["EMERGENCYTELNO"]       = "緊急連絡電話番号";
        $header["SCALASHIPDIV"]         = "スカラシップ区分";
        $header["KATEI_ENTEXAMYEAR"]    = "課程入学年度";
        $header["TIKUCD"]               = "地区コード";
        $header["EXAMNO"]               = "受験番号";
        $header["TESTDIV"]              = "入試種別";
        $header["EXAM_TYPE"]            = "入試方式";
        $header[$this->lastColumn]      = $this->lastColumn;

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl595hQuery::selectCvsStudentQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "", str_replace(",", ".", $row));

            // IBコースは"0002"をセットする
            if ($this->Properties["use_ExamType_IB"] == $row["EXAM_TYPE"]) {
                $row["COURSECODE"] = "0002";
            }

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

//            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }

    //保護者
    public function getDownloadGuardian($db, &$contents)
    {
        $header = array();

        $header["ENTEXAMYEAR"]              = "※入学年度";
        $header["GRADE"]                    = "学年";
        $header["HR_CLASS"]                 = "クラス";
        $header["ATTENDNO"]                 = "出席番号";
        $header["NAME"]                     = "氏名";
        $header["SCHREGNO"]                 = "※学籍番号";
        $header["ENT_DATE"]                 = "※入学日";
        $header["RELATIONSHIP"]             = "※続柄";
        $header["GNAME"]                    = "保護者氏名";
        $header["GKANA"]                    = "保護者氏名かな";
        $header["SEITO_SEX"]                = "性別";
        $header["BIRTHDAY"]                 = "生年月日";
        $header["GZIPCD"]                   = "郵便番号";
        $header["GADDRESS1"]                = "住所1";
        $header["GADDRESS2"]                = "住所2";
        $header["GTELNO"]                   = "電話番号";
        $header["GFAXNO"]                   = "FAX番号";
        $header["EMAIL"]                    = "E-MAIL";
        $header["GUARD_JOBCD"]              = "職種コード";
        $header["GUARD_WORK_NAME"]          = "勤務先名称";
        $header["GUARD_WORK_TELNO"]         = "勤務先電話番号";
        $header["GUARANTOR_RELATIONSHIP"]   = "保証人続柄";
        $header["GUARANTOR_NAME"]           = "保証人氏名";
        $header["GUARANTOR_KANA"]           = "保証人氏名かな";
        $header["GUARANTOR_SEX"]            = "保証人性別";
        $header["GUARANTOR_ZIPCD"]          = "保証人郵便番号";
        $header["GUARANTOR_ADDR1"]          = "保証人住所1";
        $header["GUARANTOR_ADDR2"]          = "保証人住所2";
        $header["GUARANTOR_TELNO"]          = "保証人電話番号";
        $header["GUARANTOR_JOBCD"]          = "保証人職種コード";
        $header["PUBLIC_OFFICE"]            = "兼ねている公職";
        $header[$this->lastColumn]          = $this->lastColumn;

        $contents = implode($header, ",")."\n";
        $dataFlg  = false;

        $query  = knjl595hQuery::selectCvsGuardQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(array("\r\n", "\r", "\n"), "", str_replace(",", ".", $row));

            //ヘッダ順に値をセット
            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }
            $contents .= "\n";

//            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        return $dataFlg;
    }
}
