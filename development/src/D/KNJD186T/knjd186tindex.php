<?php

require_once('for_php7.php');

require_once('knjd186tModel.inc');
require_once('knjd186tQuery.inc');

class knjd186tController extends Controller {
    var $ModelClassName = "knjd186tModel";
    var $ProgramID      = "KNJD186T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186t":
                    $sessionInstance->knjd186tModel();
                    $this->callView("knjd186tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186tCtl = new knjd186tController;
?>
