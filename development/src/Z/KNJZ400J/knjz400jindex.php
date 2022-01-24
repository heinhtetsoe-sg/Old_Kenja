<?php

require_once('for_php7.php');

require_once('knjz400jModel.inc');
require_once('knjz400jQuery.inc');

class knjz400jController extends Controller {
    var $ModelClassName = "knjz400jModel";
    var $ProgramID      = "KNJZ400J";
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
                    $this->callView("knjz400jForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz400jCtl = new knjz400jController;
//var_dump($_REQUEST);
?>
