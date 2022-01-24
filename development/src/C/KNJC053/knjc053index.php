<?php

require_once('for_php7.php');

require_once('knjc053Model.inc');
require_once('knjc053Query.inc');

class knjc053Controller extends Controller
{
    public $ModelClassName = "knjc053Model";
    public $ProgramID      = "KNJC053";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "toukei":
                    $sessionInstance->knjc053Model();
                    $this->callView("knjc053Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc053cCtl = new knjc053Controller();
//var_dump($_REQUEST);
