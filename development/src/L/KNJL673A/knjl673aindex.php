<?php

require_once('for_php7.php');

require_once('knjl673aModel.inc');
require_once('knjl673aQuery.inc');

class knjl673aController extends Controller
{
    public $ModelClassName = "knjl673aModel";
    public $ProgramID      = "KNJL673A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl673a":
                    $this->callView("knjl673aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl673aCtl = new knjl673aController();
