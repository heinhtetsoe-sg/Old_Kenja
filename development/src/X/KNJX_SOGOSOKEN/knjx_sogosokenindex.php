<?php

require_once('for_php7.php');
require_once('knjx_sogosokenModel.inc');
require_once('knjx_sogosokenQuery.inc');

class knjx_sogosokenController extends Controller
{
    public $ModelClassName = "knjx_sogosokenModel";
    public $ProgramID      = "KNJX_SOGOSOKEN";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $this->callView("knjx_sogosokenForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_sogosokenCtl = new knjx_sogosokenController();
//var_dump($_REQUEST);
