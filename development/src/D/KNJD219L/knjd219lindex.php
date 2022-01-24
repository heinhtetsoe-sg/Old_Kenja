<?php

require_once('for_php7.php');

require_once('knjd219lModel.inc');
require_once('knjd219lQuery.inc');

class knjd219lController extends Controller {
    var $ModelClassName = "knjd219lModel";
    var $ProgramID      = "KNJD219L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd219l":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd219lModel();
                    $this->callView("knjd219lForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd219lCtl = new knjd219lController;
var_dump($_REQUEST);
?>
