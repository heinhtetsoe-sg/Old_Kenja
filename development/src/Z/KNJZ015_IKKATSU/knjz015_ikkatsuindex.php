<?php

require_once('for_php7.php');

require_once('knjz015_ikkatsuModel.inc');
require_once('knjz015_ikkatsuQuery.inc');

class knjz015_ikkatsuController extends Controller {
    var $ModelClassName = "knjz015_ikkatsuModel";
    var $ProgramID      = "KNJZ015_IKKATSU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjz015_ikkatsuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz015_ikkatsuCtl = new knjz015_ikkatsuController;
//var_dump($_REQUEST);
?>
