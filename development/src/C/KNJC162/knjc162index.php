<?php

require_once('for_php7.php');

require_once('knjc162Model.inc');
require_once('knjc162Query.inc');

class knjc162Controller extends Controller
{
    public $ModelClassName = "knjc162Model";
    public $ProgramID      = "KNJC162";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semechg":
                    $sessionInstance->knjc162Model();
                    $this->callView("knjc162Form1");
                    exit;
                case "knjc162":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjc162Model();
                    $this->callView("knjc162Form1");
                    exit;
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjc162Model();
                    $this->callView("knjc162Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc162Ctl = new knjc162Controller;
var_dump($_REQUEST);
