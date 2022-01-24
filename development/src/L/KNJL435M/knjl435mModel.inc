<?php

require_once('for_php7.php');

class knjl435mModel extends Model
{
    public $field = array();
    public $cmd;
    public $courseDivs = array();

    public function knjl435mModel()
    {
    }

    public function init()
    {
        $this->field = array("DATE"         =>  VARS::post("DATE"),         //対象日
                             "DBNAME"       =>  VARS::post("DBNAME"),       //DB変更に備えて
                             "EXAM_SCHOOL_KIND"  =>  VARS::post("EXAM_SCHOOL_KIND"),
                             "APPLICANT_DIV"  =>  VARS::post("APPLICANT_DIV"),
                            );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");
    }

    public function clean()
    {
    }

    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        // 前処理
        $query = knjl435mQuery::selectCourseDivQuery($this);
        $result = $db->query($query);
        $this->courseDivs = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->courseDivs[$row["EXAM_NAME"]] = $row["COURSE_DIV"];
        }

        $header = array();
        $header[] = "氏名";
        $header[] = "氏名かな";

        foreach ($this->courseDivs as $key => $val) {
            $header[] = $key;
            $header[] = "合否";
        }
        $contents = implode($header, ",")."\n";

        $data = false;

        //SQL文発行
        $query = knjl435mQuery::selectCvsQuery($this);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $val) {
                $contents .= $val .",";
            }
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($data) {
            $filename = "志願者別合否リスト.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
