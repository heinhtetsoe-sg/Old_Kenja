<?php

require_once('for_php7.php');

require_once('knjz401jModel.inc');
require_once('knjz401jQuery.inc');

class knjz401jController extends Controller {
    var $ModelClassName = "knjz401jModel";
    var $ProgramID      = "KNJZ401J";
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
                    $this->callView("knjz401jForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz401jCtl = new knjz401jController;
//var_dump($_REQUEST);
?>
