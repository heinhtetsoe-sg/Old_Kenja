<?php

require_once('for_php7.php');

require_once('knjh712Model.inc');
require_once('knjh712Query.inc');

class knjh712Controller extends Controller
{
    public $ModelClassName = "knjh712Model";
    public $ProgramID      = "KNJH712";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "change_header":
                    $sessionInstance->knjh712Model();
                    $this->callView("knjh712Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh712Ctl = new knjh712Controller();
