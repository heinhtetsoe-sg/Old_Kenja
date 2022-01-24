<?php

require_once('for_php7.php');

require_once('knjl434hModel.inc');
require_once('knjl434hQuery.inc');

class knjl434hController extends Controller
{
    public $ModelClassName = "knjl434hModel";
    public $ProgramID      = "KNJL434H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl434h":
                    $sessionInstance->knjl434hModel();
                    $this->callView("knjl434hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl434hCtl = new knjl434hController();
