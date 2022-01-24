<?php

require_once('for_php7.php');

require_once('knjf175Model.inc');
require_once('knjf175Query.inc');

class knjf175Controller extends Controller
{
    public $ModelClassName = "knjf175Model";
    public $ProgramID      = "KNJF175";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf175":
                    $sessionInstance->setAccessLogDetail("S", $this->ProgramID);
                    $sessionInstance->knjf175Model();
                    $this->callView("knjf175Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf175Ctl = new knjf175Controller();
