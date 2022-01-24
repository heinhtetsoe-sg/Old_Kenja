<?php

require_once('for_php7.php');

require_once('knjz310_2Model.inc');
require_once('knjz310_2Query.inc');

class knjz310_2Controller extends Controller {
    var $ModelClassName = "knjz310_2Model";
    var $ProgramID      = "KNJZ310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ310_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "change_kind":
                case "grp_meb";
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ310_2");
                    $this->callView("knjz310_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz310_2Ctl = new knjz310_2Controller;
//var_dump($_REQUEST);
?>
