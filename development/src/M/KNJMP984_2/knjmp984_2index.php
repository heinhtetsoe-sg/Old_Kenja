<?php

require_once('for_php7.php');

require_once('knjmp984_2Model.inc');
require_once('knjmp984_2Query.inc');

class knjmp984_2Controller extends Controller {
    var $ModelClassName = "knjmp984_2Model";
    var $ProgramID      = "KNJZ310";

    function main()
    {
        $sessionInstance =& Model::getModel($this);

        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "list":
                case "grp_meb";
                case "clear";
                    $this->callView("knjmp984_2Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("grp_meb");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjmp984_2Ctl = new knjmp984_2Controller;
//var_dump($_REQUEST);
?>
