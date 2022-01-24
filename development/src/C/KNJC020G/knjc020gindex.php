<?php

require_once('for_php7.php');

require_once('knjc020gModel.inc');
require_once('knjc020gQuery.inc');

class knjc020gController extends Controller
{
    public $ModelClassName = "knjc020gModel";
    public $ProgramID        = "KNJC020G";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "today":
                case "next":
                case "before":
                case "clear":
                    $this->callView("knjc020gForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("edit");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc020gCtl = new knjc020gController();
//var_dump($_REQUEST);
