<?php

require_once('for_php7.php');

require_once('knjz450_2Model.inc');
require_once('knjz450_2Query.inc');

class knjz450_2Controller extends Controller {
    var $ModelClassName = "knjz450_2Model";
    var $ProgramID      = "KNJZ450";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ450_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "update2":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ450_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "grp_meb";
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ450_2");
                    $this->callView("knjz450_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz450_2Ctl = new knjz450_2Controller;
//var_dump($_REQUEST);
?>
