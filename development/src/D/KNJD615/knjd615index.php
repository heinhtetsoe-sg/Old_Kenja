<?php

require_once('for_php7.php');

require_once('knjd615Model.inc');
require_once('knjd615Query.inc');

class knjd615Controller extends Controller {
    var $ModelClassName = "knjd615Model";
    var $ProgramID      = "KNJD615";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615Form1");
                    }
                    break 2;
                case "":
                case "knjd615":
                case "change_grade":
                case "gakki":
                    $sessionInstance->knjd615Model();
                    $this->callView("knjd615Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615Ctl = new knjd615Controller;
//var_dump($_REQUEST);
?>
