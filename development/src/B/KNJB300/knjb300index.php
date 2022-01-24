<?php

require_once('for_php7.php');

require_once('knjb300Model.inc');
require_once('knjb300Query.inc');

class knjb300Controller extends Controller {
    var $ModelClassName = "knjb300Model";
    var $ProgramID      = "KNJB300";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "init":
                    $sessionInstance->knjb300Model();
                    $this->callView("knjb300Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjb300Form1");
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
$knjb300Ctl = new knjb300Controller;
?>
