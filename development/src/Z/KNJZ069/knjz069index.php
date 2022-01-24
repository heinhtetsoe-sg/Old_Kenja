<?php

require_once('for_php7.php');

require_once('knjz069Model.inc');
require_once('knjz069Query.inc');

class knjz069Controller extends Controller {
    var $ModelClassName = "knjz069Model";
    var $ProgramID      = "KNJZ069";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel":
                case "clear";
                case "btn_def";
                case "changeCombo";
                    $this->callView("knjz069Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz069Ctl = new knjz069Controller;
//var_dump($_REQUEST);
?>
