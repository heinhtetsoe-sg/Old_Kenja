<?php

require_once('for_php7.php');

require_once('knjd171jModel.inc');
require_once('knjd171jQuery.inc');

class knjd171jController extends Controller {
    var $ModelClassName = "knjd171jModel";
    var $ProgramID      = "KNJD171J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd171j":
                    $sessionInstance->knjd171jModel();
                    $this->callView("knjd171jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd171jCtl = new knjd171jController;
?>
