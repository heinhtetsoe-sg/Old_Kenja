<?php

require_once('for_php7.php');

require_once('knjd185tModel.inc');
require_once('knjd185tQuery.inc');

class knjd185tController extends Controller {
    var $ModelClassName = "knjd185tModel";
    var $ProgramID      = "KNJD185T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd185t":
                    $sessionInstance->knjd185tModel();
                    $this->callView("knjd185tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd185tCtl = new knjd185tController;
?>
