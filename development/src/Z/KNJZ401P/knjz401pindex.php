<?php

require_once('for_php7.php');

require_once('knjz401pModel.inc');
require_once('knjz401pQuery.inc');

class knjz401pController extends Controller {
    var $ModelClassName = "knjz401pModel";
    var $ProgramID      = "KNJZ401P";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
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
                    $this->callView("knjz401pForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz401pCtl = new knjz401pController;
//var_dump($_REQUEST);
?>
