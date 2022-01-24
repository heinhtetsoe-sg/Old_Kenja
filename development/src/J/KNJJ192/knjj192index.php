<?php

require_once('for_php7.php');

require_once('knjj192Model.inc');
require_once('knjj192Query.inc');

class knjj192Controller extends Controller {
    var $ModelClassName = "knjj192Model";
    var $ProgramID      = "KNJJ192";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjj192Form1");
                    }
                    break 2;
                case "":
                case "knjj192":
                    $sessionInstance->knjj192Model();
                    $this->callView("knjj192Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj192Ctl = new knjj192Controller;
//var_dump($_REQUEST);
?>
