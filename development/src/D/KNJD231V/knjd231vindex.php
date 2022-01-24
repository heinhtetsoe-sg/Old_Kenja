<?php

require_once('for_php7.php');


require_once('knjd231vModel.inc');
require_once('knjd231vQuery.inc');

class knjd231vController extends Controller {
    var $ModelClassName = "knjd231vModel";
    var $ProgramID      = "KNJD231V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $this->callView("knjd231vForm1");
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
                    $this->callView("knjd231vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd231vCtl = new knjd231vController;
?>
