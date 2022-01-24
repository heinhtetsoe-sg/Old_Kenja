<?php

require_once('for_php7.php');


class knje378Model extends model
{
    var $cmd;
    var $year;
    var $proficiencydiv;
    var $proficiencycd;

    function knje378Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->proficiencydiv = VARS::post("PROFICIENCYDIV");
        $this->proficiencycd = VARS::post("PROFICIENCYCD");
    }

    //受験番号をセット
    function &getMainModel()
    {
    }
    
    function &getCsv()
    {
        $db = Query::dbCheckOut();
        
        $query = knje378Query::getProNameDirect($this);
        $proName = $db->getOne($query);
        
        $filename = '校内推薦希望者.csv';
        $title ='校内推薦希望者,,,,,,,,,,作成日:' . date('Y/m/d') . ',,,,,,,,,,,,,,,,,,,,,' . $this->proficiencydiv . '-' . $this->proficiencycd . ':' . $proName;
        $header ='学籍番号,組,出席番号,氏名,性別,群位,合計点,第1志望学科,第2志望学科,第3志望学科,学習成績 総計,評定平均,出席状況点 総計,出席状況1年,出席状況2年,出席状況3年,';
        $header .='欠席日数1年,欠席日数2年,欠席日数3年,1年の特別活動,2年の特別活動,3年の特別活動,1年の生徒会活動,2年の生徒会活動,3年の生徒会活動,1年の部活動,2年の部活動,3年の部活動,';
        $header .='1年のクラス委員,2年のクラス委員,3年のクラス委員,学科合計点';
        $query = knje378Query::getSubData2($this);
        $result = $db->query($query);
        $Ids = array();
        $Names = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if(!isset($Ids[$row['SCHREGNO']])){
                $Ids[$row['SCHREGNO']] = array();
            }
            $Ids[$row['SCHREGNO']][$row['SUBCLASS_NAME']] = $row['SCORE'];
            $Names[$row['SUBCLASS_NAME']]=true;
        }
        $contents = $header;
        foreach ($Names as $key => $value){
            $contents.=','.$key;
            $title.=',';
        }
        $contents.="\n";
        $contents = $title . "\n" . $contents;
        $query = knje378Query::getMainData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ORDERED)) {
            for($i = 0; $i < get_count($row); $i++) {
                $contents .= $row[$i];
                if($i + 1 != get_count($row)){
                    $contents .= ',';
                }
            }
            $contents.=','.$this->_committeestring($db,$row[0],'01');
            $contents.=','.$this->_committeestring($db,$row[0],'02');
            $contents.=','.$this->_committeestring($db,$row[0],'03');
            
            foreach ($Names as $key => $value){
                $contents.=','.(isset($Ids[$row[0]][$key])?$Ids[$row[0]][$key]:'');
            }
            $contents .= "\n";
        }
        
        common::downloadContents($contents, $filename, "SJIS-win");
    }
    function _committeestring($db,$schregNo,$gradeCd){
        $result = $db->query(knje378Query::getSubData($this,$schregNo,$gradeCd));
        $committeeName='';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $committeeName.=$row['COMMITTEENAME'].'/';
        }
        return rtrim($committeeName,'/');
    }
}
?>
