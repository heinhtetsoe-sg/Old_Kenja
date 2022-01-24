<?php

require_once('for_php7.php');

require_once('knjd297Model.inc');
require_once('knjd297Query.inc');

class KNJD297Controller extends Controller {
    var $ModelClassName = "KNJD297Model";
    var $ProgramID      = "KNJD297";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjd297Form1");
                    }
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main";
                case "changeKind";
                    $this->callView("knjd297Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJD297Ctl = new KNJD297Controller;
//var_dump($_REQUEST);
?>
