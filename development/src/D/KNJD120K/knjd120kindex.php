<?php

require_once('for_php7.php');

require_once('knjd120kModel.inc');
require_once('knjd120kQuery.inc');

class knjd120kController extends Controller
{
    public $ModelClassName = "knjd120kModel";
    public $ProgramID      = "KNJD120K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "cancel":
                    $sessionInstance->getMainModel();
                    $this->callView("knjd120kForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd120kCtl = new knjd120kController();
