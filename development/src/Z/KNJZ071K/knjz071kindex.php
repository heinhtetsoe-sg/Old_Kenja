<?php
require_once('knjz071kModel.inc');
require_once('knjz071kQuery.inc');

class KNJZ071KController extends Controller {
    var $ModelClassName = "knjz071kModel";
    var $ProgramID      = "KNJZ071K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("knjz071k");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "knjz071k";
                    $this->callView("knjz071kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJZ071KCtl = new KNJZ071KController;
//var_dump($_REQUEST);
?>
