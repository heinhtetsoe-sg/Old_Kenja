<?php

require_once('for_php7.php');

require_once('knjl323Model.inc');
require_once('knjl323Query.inc');

class knjl323Controller extends Controller {
    var $ModelClassName = "knjl323Model";
    var $ProgramID      = "KNJL323";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323":
                    $sessionInstance->knjl323Model();
                    $this->callView("knjl323Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjl323Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl323Ctl = new knjl323Controller;
//var_dump($_REQUEST);
?>
