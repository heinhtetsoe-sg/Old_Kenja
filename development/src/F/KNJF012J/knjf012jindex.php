<?php

require_once('for_php7.php');

require_once('knjf012jModel.inc');
require_once('knjf012jQuery.inc');

class knjf012jController extends Controller
{
    public $ModelClassName = "knjf012jModel";
    public $ProgramID      = "KNJF012J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "edit":
                case "reset":
                    $this->callView("knjf012jForm1");
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
$knjf012jCtl = new knjf012jController();
