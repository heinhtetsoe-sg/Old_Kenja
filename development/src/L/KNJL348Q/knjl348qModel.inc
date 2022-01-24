<?php

require_once('for_php7.php');
class knjl348qModel extends Model
{
    public $field = array();
    public $testKoumoku = array();
    public $cmd;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function knjl348qModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),          //入試区分
                             "TAISYOU"          =>  VARS::post("TAISYOU")           //出力順
                            );

        $this->cmd = VARS::request("cmd");

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

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //初期化
        $contents  = "";

        //テスト区分名取得
        $testDivName = $db->getOne(knjl348qQuery::getNameMstName($this->ObjYear, $this->nameCd2, $this->field["TESTDIV"], "abbv"));

        //テスト項目取得
        $this->testKoumoku = array();
        $query = knjl348qQuery::getTestKamokuList();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->testKoumoku[$row["NAMECD2"]] = $row["NAME"];
        }

        /****************************/
        /*  タイトル部分データ取得  */
        /****************************/
        //年度（和暦）
        $nendo = common::DateConv1($this->ObjYear."/04/01", 10);

        /**********************/
        /*  タイトル部分出力  */
        /**********************/
        if ($this->field["TAISYOU"] == "3") {
            $sortName = "出身学校順";
        } elseif ($this->field["TAISYOU"] == "2") {
            $sortName = "高得点順";
        } else {
            $sortName = "受験番号順";
        }
        //1行目
        $contents .= "{$nendo}　{$testDivName}入学試験　結果({$sortName})\n";

        /********************/
        /*  データ部分出力  */
        /********************/
        $header_data = false;   //出力フラグ

        $header = array();
        $header["DIV_RANK4"]         = "男女別順位";
        $header["EXAMNO"]            = "受験番号";
        $header["NAME"]              = "氏名";
        $header["SEX"]               = "性別";
        $header["NAME_KANA"]         = "ふりがな";
        $cnt = 1;
        foreach ($this->testKoumoku as $key => $testName) {
            $header["SCORE".$cnt]        = $testName."得点";
            $cnt++;
        }
        if (SCHOOLKIND == "P") {
            $header["TOTAL4"]            = "合計点";
        } else {
            $header["TOTAL4"]            = "筆記試験合計点";
            $header["INTERVIEW_VALUE"]   = "面接評価１";
            $header["INTERVIEW_VALUE2"]  = "面接評価２";
        }
        if (SCHOOLKIND == "P") {
            $header["FS_CD"]             = "出身園コード";
            $header["FINSCHOOL_NAME"]    = "出身園名";
        } else {
            $header["FS_CD"]             = "出身学校コード";
            $header["FINSCHOOL_NAME"]    = "出身学校名";
            $header["BROTHERS_SISTERS"]  = "兄弟姉妹";
        }
        $header[$this->lastColumn]       = $this->lastColumn;

        $contents .= implode($header, ",")."\n";

        $query = knjl348qQuery::selectCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

            if (strlen($row["SIMAI_SCHOOL_KIND1"]) && strlen($row["SIMAI_SCHOOL_KIND2"])) {
                $row["BROTHERS_SISTERS"] = $row["SIMAI_SCHOOL_KIND1"]."、".$row["SIMAI_SCHOOL_KIND2"];
            } elseif (strlen($row["SIMAI_SCHOOL_KIND1"])) {
                $row["BROTHERS_SISTERS"] = $row["SIMAI_SCHOOL_KIND1"];
            } elseif (strlen($row["SIMAI_SCHOOL_KIND2"])) {
                $row["BROTHERS_SISTERS"] = $row["SIMAI_SCHOOL_KIND2"];
            } else {
                $row["BROTHERS_SISTERS"] = "";
            }
            $row["DIV_RANK4"] = $row["SEX_NAME"].$row["DIV_RANK4"];
            $comma = "";
            foreach ($header as $key => $headerName) {
                $contents .= $comma.$row[$key];
                $comma = ",";
            }
            $contents .= "\n";

            $header_data = true;
        }
        $result->free();

        //ファイル名
        $title = "{$this->ObjYear}{$testDivName}得点チェックリスト";

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
