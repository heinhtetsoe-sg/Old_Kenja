<?php

require_once('for_php7.php');

require_once('knjc043tModel.inc');
require_once('knjc043tQuery.inc');

class knjc043tController extends Controller
{
    public $ModelClassName = "knjc043tModel";
    public $ProgramID      = "KNJC043T";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc043t":
                case "semechg":
                    $sessionInstance->knjc043tModel();
                    $this->callView("knjc043tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc043tCtl = new knjc043tController();
//var_dump($_REQUEST);
