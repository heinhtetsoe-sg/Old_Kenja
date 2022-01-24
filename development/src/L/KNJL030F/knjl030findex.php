<?php

require_once('for_php7.php');

require_once('knjl030fModel.inc');
require_once('knjl030fQuery.inc');

class knjl030fController extends Controller
{
    public $ModelClassName = "knjl030fModel";
    public $ProgramID      = "KNJL030F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl030f":
                    $sessionInstance->knjl030fModel();
                    $this->callView("knjl030fForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("knjl030f");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl030fCtl = new knjl030fController();
//var_dump($_REQUEST);
