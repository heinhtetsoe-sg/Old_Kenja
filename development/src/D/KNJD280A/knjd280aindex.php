<?php

require_once('for_php7.php');

require_once('knjd280aModel.inc');
require_once('knjd280aQuery.inc');

class knjd280aController extends Controller {
    var $ModelClassName = "knjd280aModel";
    var $ProgramID      = "KNJD280A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getExecModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjd280a");
                    break 1;
                case "":
                case "knjd280a":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd280aModel();
                    $this->callView("knjd280aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd280aCtl = new knjd280aController;
var_dump($_REQUEST);
?>
