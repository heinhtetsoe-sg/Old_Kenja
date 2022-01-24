<?php

require_once('for_php7.php');

class knjl330aModel extends Model
{
    public $cmd;
    public $examyear;
    public $selected_data;
    public $examType;
    public $field;

    public function knjl330aModel()
    {
    }

    public function init()
    {
        $this->examyear      = CTRL_YEAR + 1;
        $this->cmd           = VARS::request("cmd");
        $this->examType = 1;    //固定

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //受験校種
                             "TESTDIV"          =>  VARS::post("TESTDIV")           //試験名
                              );
    }

    //CVSファイル
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        $contents = "試験名,受験番号,氏名,性別,入学コース,出身学校,払込フラグ\n";
        
        $db = Query::dbCheckOut();
        
        $query = knjl330aQuery::selectQuery($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $contents .= $row["TDIV_NAME"].",".$row["RECEPTNO"].",".$row["NAME"].",".$row["SEX"].",".$row["COURSENAME"].",".$row["FINSCHOOL_NAME_ABBV"].",".$row["PROCEDUREDIV"]."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data) {
            $fname = $this->examyear."年度入学予定者リスト_".$row["APPDIVNAME"];
            if ($this->field["TESTDIV"] != "ALL") {
                $fname .= "_".$row["TDIV_NAME"];
            } else {
                $fname .= "_全て";
            }
            $filename = sprintf("%s.csv", $fname);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
