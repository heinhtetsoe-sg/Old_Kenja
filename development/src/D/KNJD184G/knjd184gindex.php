<?php

require_once('for_php7.php');

require_once('knjd184gModel.inc');
require_once('knjd184gQuery.inc');

class knjd184gController extends Controller {
    var $ModelClassName = "knjd184gModel";
    var $ProgramID      = "KNJD184G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184g":
                    $sessionInstance->knjd184gModel();
                    $this->callView("knjd184gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184gCtl = new knjd184gController;
?>
