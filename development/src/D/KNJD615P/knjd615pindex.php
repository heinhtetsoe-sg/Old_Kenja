<?php

require_once('for_php7.php');

require_once('knjd615pModel.inc');
require_once('knjd615pQuery.inc');

class knjd615pController extends Controller
{
    public $ModelClassName = "knjd615pModel";
    public $ProgramID      = "KNJD615P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615p":
                case "gakki":
                    $sessionInstance->knjd615pModel();
                    $this->callView("knjd615pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd615pCtl = new knjd615pController();
var_dump($_REQUEST);
