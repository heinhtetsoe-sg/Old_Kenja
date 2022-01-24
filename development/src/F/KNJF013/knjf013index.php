<?php

require_once('for_php7.php');

require_once('knjf013Model.inc');
require_once('knjf013Query.inc');

class knjf013Controller extends Controller
{
    public $ModelClassName = "knjf013Model";
    public $ProgramID      = "KNJF013";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "edit":
                case "reset":
                    $this->callView("knjf013Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf013Ctl = new knjf013Controller();
//var_dump($_REQUEST);
