<?php

require_once('for_php7.php');


require_once('knjd234yModel.inc');
require_once('knjd234yQuery.inc');

class knjd234yController extends Controller {
    var $ModelClassName = "knjd234yModel";
    var $ProgramID      = "KNJD234Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $this->callView("knjd234yForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd234yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd234yCtl = new knjd234yController;
?>
