<?php

require_once('for_php7.php');

require_once('knjg045Model.inc');
require_once('knjg045Query.inc');

class knjg045Controller extends Controller
{
    public $ModelClassName = "knjg045Model";
    public $ProgramID      = "KNJG045";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "hr_class":
                case "main":
                    $this->callView("knjg045Form1");
                    break 2;
                case "kesseki":
                case "kesseki-A":
                case "chikoku":
                case "chikoku-A":
                case "soutai":
                case "soutai-A":
                case "shuchou":
                case "shuchou-A":
                case "hoketsu":
                case "hoketsu-A":
                case "etc_hoketsu":
                case "etc_hoketsu-A":
                    $this->callView("knjg045SubForm1");
                    break 2;
                case "update":
                case "delete":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update_detail":
                    $sessionInstance->getUpdateDetailModel();
                    $sessionInstance->setCmd($sessionInstance->cmd);
                    break 1;
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
$knjg045Ctl = new knjg045Controller();
//var_dump($_REQUEST);
