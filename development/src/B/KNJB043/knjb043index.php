<?php

require_once('for_php7.php');

require_once('knjb043Model.inc');
require_once('knjb043Query.inc');

class knjb043Controller extends Controller
{
    public $ModelClassName = "knjb043Model";
    public $ProgramID      = "KNJB043";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb043":
                    $sessionInstance->knjb043Model();
                    $this->callView("knjb043Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb043Ctl = new knjb043Controller();
var_dump($_REQUEST);
//
