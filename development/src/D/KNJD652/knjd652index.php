<?php

require_once('for_php7.php');

require_once('knjd652Model.inc');
require_once('knjd652Query.inc');

class knjd652Controller extends Controller {
    var $ModelClassName = "knjd652Model";
    var $ProgramID      = "KNJD652";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd652Form1");
                    }
                    break 2;
                case "":
                case "knjd652":
                case "gakki":
                    $sessionInstance->knjd652Model();
                    $this->callView("knjd652Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd652Ctl = new knjd652Controller;
//var_dump($_REQUEST);
?>
