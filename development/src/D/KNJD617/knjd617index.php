<?php

require_once('for_php7.php');

require_once('knjd617Model.inc');
require_once('knjd617Query.inc');

class knjd617Controller extends Controller {
    var $ModelClassName = "knjd617Model";
    var $ProgramID      = "KNJD617";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617Form1");
                    }
                    break 2;
                case "":
                case "knjd617":
                case "gakki":
                    $sessionInstance->knjd617Model();
                    $this->callView("knjd617Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd617Ctl = new knjd617Controller;
//var_dump($_REQUEST);
?>
