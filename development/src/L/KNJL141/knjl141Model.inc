<?php

require_once('for_php7.php');
class knjl141Model extends Model
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

    public function knjl141Model()
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
        $query = knjl141Query::getSchoolName();
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

        $query = knjl141Query::getKamokuSuu();
        $kamokuSuu = $db->getOne($query);

        $header = array("入学年度",
                        "※受験番号",
                        "氏名",
                        "氏名かな",
                        "性別",
                        "課程コード",
                        "学科コード",
                        "コースコード",
                        );

        $query = knjl141Query::getKmaokuMei();
        $kamokuMei = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kamokuMei[] = $row["NAME1"];
        }

        $contents = implode($header, ","). ",". implode($kamokuMei, ","). ",合計,". implode($kamokuMei, ","). ",合計,".$this->lastColumn."\n";
        $dataFlg  = false;

        //SQL文発行
        $query  = knjl141Query::selectCvsQuery($this, $kamokuSuu);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= implode($row, ",")."\n";
            $dataFlg = true;
        }
        $result->free();

        if ($dataFlg) {
            //CSV出力
            $filename = $this->examyear."_入学者クラス編成用CSV出力.csv";

            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);
    }
}
