<?php

require_once('for_php7.php');

require_once('knjd231tModel.inc');
require_once('knjd231tQuery.inc');

class knjd231tController extends Controller {
    var $ModelClassName = "knjd231tModel";
    var $ProgramID      = "KNJD231T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd231t":
                    $sessionInstance->knjd231tModel();
                    $this->callView("knjd231tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd231tCtl = new knjd231tController;
var_dump($_REQUEST);
?>
