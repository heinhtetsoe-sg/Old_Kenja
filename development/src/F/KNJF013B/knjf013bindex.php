<?php

require_once('for_php7.php');

require_once('knjf013bModel.inc');
require_once('knjf013bQuery.inc');

class knjf013bController extends Controller
{
    public $ModelClassName = "knjf013bModel";
    public $ProgramID      = "KNJF013B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "edit":
                case "reset":
                    $this->callView("knjf013bForm1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
$knjf013bCtl = new knjf013bController();
//var_dump($_REQUEST);
