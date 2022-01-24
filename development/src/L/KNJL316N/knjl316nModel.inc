<?php

require_once('for_php7.php');
class knjl316nModel extends Model
{
    public $field = array();
    public $cmd;
    public $test_year;

    public function init()
    {
        $this->field = array("APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"       =>  VARS::post("TESTDIV")        //入試区分
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
        $query = knjl316nQuery::getTestSubclasscd($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->subclassList[$row["VALUE"]] = $row["LABEL"];
        }

        //ヘッダ
        $header = array();
        $header["SHDIV_NAME"]       = "専併区分名称";
        foreach ($this->subclassList as $subCd => $subName) {
            $header["SCORE".$subCd] = $subName."得点";
        }
        $header["EXAMCOURSE_NAME"]  = "第一志望コース名称";

        //ヘッダ出力
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        $contents .= implode($header, ",") ."\n";

        //SQL文発行
        $query = knjl316nQuery::getCsvQuery($this);
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

        //入試区分名
        $testdivRow = $db->getRow(knjl316nQuery::getNameMst($this, "L004", $this->field["TESTDIV"]), DB_FETCHMODE_ASSOC);

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv", "得点分布表用CSVデータ"."_".$this->test_year."年度_".$testdivRow["NAME1"]);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
