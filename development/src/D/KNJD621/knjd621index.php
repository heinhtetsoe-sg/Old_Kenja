<?php

require_once('for_php7.php');

require_once('knjd621Model.inc');
require_once('knjd621Query.inc');

class knjd621Controller extends Controller {
    var $ModelClassName = "knjd621Model";
    var $ProgramID      = "KNJD621";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd621Model();
                    $this->callView("knjd621Form1");
                    exit;
                case "knjd621":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd621Model();
                    $this->callView("knjd621Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd621Form1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd621Ctl = new knjd621Controller;
?>
