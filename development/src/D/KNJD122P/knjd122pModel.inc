<?php

require_once('for_php7.php');


class knjd122pModel extends Model {
    var $field      = array();
    var $cmd;
    var $testTable;

    function knjd122pModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>    VARS::post("SEMESTER"),
                             "SUBCLASSCD"   =>  VARS::post("SUBCLASSCD"),
                             "DBNAME"       =>  VARS::post("DBNAME")
                     );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getProperties();
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function getProperties()
    {
        $retVal = "";
        
        /*
         * configディレクトリ確認
         */
        if (file_exists(CONFDIR ."/prgInfo.properties")) {
            $filename = CONFDIR ."/prgInfo.properties";
        } else {
            $filename = DOCUMENTROOT ."/prgInfo.properties";
        }

        $fp = @fopen($filename, 'r');
        while ($line = fgets($fp,1024)) {
            $pos = strpos($line, "useTestCountflg");
            // === を使用していることに注目しましょう。単純に == を使ったのでは
            // 期待通りに動作しません。なぜなら 'doRun' が 0 番目 (最初) の文字だからです。
            if ($pos === false) {
                $this->testTable = "TESTITEM_MST_COUNTFLG_NEW";
            } else {
                $retVal = str_replace("useTestCountflg = ", "", $line);
                $this->testTable = str_replace("\r\n", "", $retVal);
                break;
            }
        }
        fclose($fp);

        return $retVal;
    }

    function clean()
    {
    }
}
?>
