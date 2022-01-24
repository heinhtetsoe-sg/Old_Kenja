<?php

require_once('for_php7.php');

require_once('knjd425l_4Model.inc');
require_once('knjd425l_4Query.inc');

class knjd425l_4Controller extends Controller
{
    public $ModelClassName = "knjd425l_4Model";
    public $ProgramID      = "KNJD425L_4";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "subform":
                case "reset":
                case "edit":
                case "check":
                    $sessionInstance->knjd425l_4Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd425l_4Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425l_4Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425l_4Form1", $sessionInstance->auth);
                    $sessionInstance->getDeleteModel();
                    break 1;
                case "listdelete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425l_4Form1", $sessionInstance->auth);
                    $sessionInstance->getListDeleteModel();
                    break 1;
                case "targetClass":
                case "targetClassInsertEnd":
                    $this->callView("knjd425l_4TargetClass");
                    break 2;
                case "targetClassInsert":
                    $sessionInstance->getTargetClassInsertModel();
                    $sessionInstance->setCmd("targetClassInsertEnd");
                    break 1;
                case "gouri":
                    $this->callView("knjd425l_4Gouri");
                    break 2;
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
$knjd425l_4Ctl = new knjd425l_4Controller();
