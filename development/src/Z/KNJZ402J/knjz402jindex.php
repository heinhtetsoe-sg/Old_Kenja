<?php

require_once('for_php7.php');

require_once('knjz402jModel.inc');
require_once('knjz402jQuery.inc');

class knjz402jController extends Controller {
    var $ModelClassName = "knjz402jModel";
    var $ProgramID      = "KNJZ402J";
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
                    $this->callView("knjz402jForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz402jCtl = new knjz402jController;
//var_dump($_REQUEST);
?>
