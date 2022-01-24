<?php

require_once('for_php7.php');

require_once('knjc043bModel.inc');
require_once('knjc043bQuery.inc');

class knjc043bController extends Controller
{
    public $ModelClassName = "knjc043bModel";
    public $ProgramID      = "KNJC043B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc043b":
                case "semechg":
                    $sessionInstance->knjc043bModel();
                    $this->callView("knjc043bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc043bCtl = new knjc043bController();
//var_dump($_REQUEST);
