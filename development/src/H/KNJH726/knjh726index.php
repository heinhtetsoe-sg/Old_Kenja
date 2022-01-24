<?php

require_once('for_php7.php');

require_once('knjh726Model.inc');
require_once('knjh726Query.inc');

class knjh726Controller extends Controller
{
    public $ModelClassName = "knjh726Model";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh726":
                    $sessionInstance->knjh726Model();
                    $this->callView("knjh726Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh726Ctl = new knjh726Controller();
var_dump($_REQUEST);
