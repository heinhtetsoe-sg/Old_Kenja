<?php

require_once('for_php7.php');

require_once('knjd126fModel.inc');
require_once('knjd126fQuery.inc');

class knjd126fController extends Controller {
    var $ModelClassName = "knjd126fModel";
    var $ProgramID      = "KNJD126F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "reset":
                    $this->callView("knjd126fForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "form2":
                case "reset2":
                    $this->callView("knjd126fForm2");
                   break 2;
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
$knjd126fCtl = new knjd126fController;
?>
