<?php

require_once('for_php7.php');

class knjl442mModel extends Model
{
    public $field = array();
    public $cmd;

    public function knjl442mModel()
    {
    }

    public function init()
    {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //対象日
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                             "EXAM_SCHOOL_KIND"  =>  VARS::post("EXAM_SCHOOL_KIND"),
                             "APPLICANT_DIV"  =>  VARS::post("APPLICANT_DIV"),
                             "COURSECODE"  =>  VARS::post("COURSECODE"),
                             "FREQUENCY"  =>  VARS::post("FREQUENCY"),
                            );
        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    public function clean()
    {
    }

    public function getDownloadModel()
    {
        // 入試区分：必須
        // 未入力チェック
        if ($this->field["APPLICANT_DIV"] == "") {
            $this->setWarning("MSG301");
            return false;
        }
        $db = Query::dbCheckOut();

        $contents = "受験番号,得点" ."\n";

        $data = false;

        //SQL文発行
        $query = knjl442mQuery::selectCvsQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sep = "";
            foreach ($row as $val) {
                $contents .= $sep.$val;
                $sep = ",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = "クラス分け用データ.csv";
            // ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
