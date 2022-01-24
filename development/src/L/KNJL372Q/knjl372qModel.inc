<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl372qModel extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd;        //primary
    var $year_code;
    var $isload;
    var $tmp;				//NO001
    var $year;				//NO001
    
    /**
     * メンバ変数の初期化
     */
    function knjl372qModel()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
    }
    
    
    //CSV出力(中学校別と団体別共通)
    function getCsvModel()
    {
        if($this->cmd == "school_csv"){
            $name = "中学校";
            $query = knjl372qQuery::getSchoolCnt();
        }else{
            $name = "団体";
            $query = knjl372qQuery::getGroupCnt();
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        $header_data = "";
        
        $contents = $name."コード,".$name."名,申込数,受験数,欠席数\n";
        
        $result = $db->query($query);
        
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if($this->cmd == "school_csv"){
                $contents .= $row["SCHOOLCD"].",".$row["FINSCHOOL_NAME"];
            }else{
                $contents .= $row["GROUPCD"].",".$row["GROUPNAME"];
            }
            
            $contents .= ",".$row["APPCNT"].",".$row["TAKECNT"].",".$row["ABSCNT"]."\n";
            $header_data = true;
        }
        
        
        //DB切断
        Query::dbCheckIn($db);

        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv",$name."別申込状況一覧");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    
}
?>
