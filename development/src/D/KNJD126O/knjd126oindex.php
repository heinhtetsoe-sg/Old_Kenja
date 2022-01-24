<?php

require_once('for_php7.php');

require_once('knjd126oModel.inc');
require_once('knjd126oQuery.inc');

class knjd126oController extends Controller {
    var $ModelClassName = "knjd126oModel";
    var $ProgramID      = "KNJD126O";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "chgGrd1":
                case "chgSub1":
                case "select1":
                case "reset":
                case "back":
                    $this->callView("knjd126oForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "form2":
                case "chgGrd2":
                case "chgSub2":
                case "select2":
                case "form2_reset":
                    $this->callView("knjd126oForm2");
                    break 2;
                case "form2_update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form2");
                    break 1;
                case "delete2":
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form2");
                    break 1;
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
$knjd126oCtl = new knjd126oController;
?>
