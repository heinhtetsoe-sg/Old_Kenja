<?php

require_once('for_php7.php');

require_once('knjm530Model.inc');
require_once('knjm530Query.inc');

class knjm530Controller extends Controller {
    var $ModelClassName = "knjm530Model";
    var $ProgramID      = "KNJM530";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updateCookie":
                    $sessionInstance->getUpdateCookie();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjm530");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjm530");
                    break 1;
                case "errA":
                case "errR":
                case "seito":
                case "staff":
                case "kouji":
                case "kouza":
                case "jikanwari":
                    $sessionInstance->getDownLoadModel();
                    $sessionInstance->setCmd("knjm530");
                    break 1;
                case "":
                case "knjm530":
                    $sessionInstance->knjm530Model();
                    $this->callView("knjm530Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjm530Ctl = new knjm530Controller;
?>
