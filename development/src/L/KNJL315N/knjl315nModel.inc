<?php

require_once('for_php7.php');
class knjl315nModel extends Model
{
    public $field = array();
    public $cmd;
    public $test_year;

    public function init()
    {
        $this->field = array("APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),    //入試制度
                             "TESTDIV"       =>  VARS::post("TESTDIV"),         //入試区分
                             "SHDIV"         =>  VARS::post("SHDIV")            //専併区分
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //科目取得
        $this->subclassList = array();
        $query = knjl315nQuery::getTestSubclasscd($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->subclassList[$row["VALUE"]] = $row["LABEL"];
        }

        //ヘッダ
        $header = array();
        $header["KYOIKU_NAME"]          = "教育相談";
        $header["EXAMNO"]               = "受験番号";
        $header["NAME"]                 = "生徒名";
        $header["EXAMCOURSE_NAME"]      = "コース";
        $header["FINSCHOOL_NAME"]       = "中学校名";
        foreach ($this->subclassList as $subCd => $subName) {
            $header["SCORE".$subCd] = $subName;
        }
        $header["TOTAL4"]               = "合計";
        $header["TEST_KAIJOU"]          = "筆記";
        $header["INTERVIEW_KAIJOU"]     = "面接";
        $header["INTERVIEW_VALUE"]      = "判定";
        $header["INTERVIEW_REMARK"]     = "面接内容";
        $header["INTERVIEW_VALUE2"]     = "音読評価";
        $header["INTERVIEW_REMARK2"]    = "音読内容";
        $header["DIV_RANK4"]            = "順位";
        $header["TOTAL_ALL"]            = "評定合計";

        //ヘッダ出力
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $query = knjl315nQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);

            $sep = "";
            foreach ($header as $field => $label) {
                $contents .= $sep.$row[$field];
                $sep = ",";
            }

            $contents .= "\n";
            $header_data = true;
        }
        $result->free();

        //入試区分
        $testdivRow = $db->getRow(knjl315nQuery::getNameMst($this, "L004", $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);

        //専併区分
        $shdivRow = $db->getRow(knjl315nQuery::getNameMst($this, "L006", $this->field["SHDIV"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", "面接結果資料CSVデータ"."_".$this->test_year."年度_".$testdivRow["NAME1"]."_".$shdivRow["NAME1"]);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
