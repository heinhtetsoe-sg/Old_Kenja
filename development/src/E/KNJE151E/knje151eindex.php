<?php

require_once('for_php7.php');

require_once('knje151eModel.inc');
require_once('knje151eQuery.inc');

class knje151eController extends Controller {
    var $ModelClassName = "knje151eModel";
    var $ProgramID      = "KNJE151E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "updform1":
                case "form1":
                case "select1":
                case "reset":
                    $this->callView("knje151eForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updform1");
                    break 1;
                case "updform2":
                case "form2":
                case "select2":
                case "form2_reset":
                    $this->callView("knje151eForm2");
                    break 2;
                case "form2_update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updform2");
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
$knje151eCtl = new knje151eController;
?>
