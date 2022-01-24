<?php

require_once('for_php7.php');

require_once('knjd154Model.inc');
require_once('knjd154Query.inc');

class knjd154Controller extends Controller
{
    public $ModelClassName = "knjd154Model";
    public $ProgramID      = "KNJD154";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear":
                case "knjd154":
                    $sessionInstance->knjd154Model();
                    $this->callView("knjd154Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154Ctl = new knjd154Controller();
