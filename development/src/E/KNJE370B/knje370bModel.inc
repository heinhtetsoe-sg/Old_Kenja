<?php

require_once('for_php7.php');


class knje370bModel extends model
{
    var $cmd;
    var $year;
    var $proficiencydiv;
    var $proficiencycd;

    function knje370bModel()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
    }

    //受験番号をセット
    function &getMainModel()
    {
    }
    
    function &getCsv()
    {
        $db = Query::dbCheckOut();
        
        $filename = '進路統計資料.csv';
        $header = "学籍番号,性別,コースコード,コース名称,校種,国公私の区分,大学名称,学部名称,学科名称,大学コード,学部コード,学科コード,出願区分,合否区分,既卒フラグ,進路状況区分,留学フラグ";
        $contents = $header;
        $contents.="\n";

        $query = knje370bQuery::getMainData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ORDERED)) {
            for($i = 0; $i < get_count($row); $i++) {
                $contents .= $row[$i];
                if($i + 1 != get_count($row)){
                    $contents .= ',';
                }
            }
            $contents .= "\n";
        }
        common::downloadContents($contents, $filename, "SJIS-win");
    }
}
?>
