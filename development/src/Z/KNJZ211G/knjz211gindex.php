<?php

require_once('for_php7.php');

require_once('knjz211gModel.inc');
require_once('knjz211gQuery.inc');

class knjz211gController extends Controller {
    var $ModelClassName = "knjz211gModel";
    var $ProgramID      = "KNJZ211G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                // case "select1":
                case "reset":
                    $this->callView("knjz211gForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                // case "form2":
                // case "select2":
                // case "form2_reset":
                //     $this->callView("knjz211gForm2");
                //     break 2;
                // case "form2_update":
                //     $sessionInstance->getUpdateModel();
                //     //変更済みの場合は詳細画面に戻る
                //     $sessionInstance->setCmd("form2");
                //     break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("form1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz211gCtl = new knjz211gController;
?>
