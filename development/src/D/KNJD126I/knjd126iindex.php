<?php

require_once('for_php7.php');

require_once('knjd126iModel.inc');
require_once('knjd126iQuery.inc');

class knjd126iController extends Controller {
    var $ModelClassName = "knjd126iModel";
    var $ProgramID      = "KNJD126I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "reset":
                    $this->callView("knjd126iForm1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "updateRecordSeme":
                    $sessionInstance->getUpdateRecordSeme();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "form2":
                case "reset2":
                    $this->callView("knjd126iForm2");
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
$knjd126iCtl = new knjd126iController;
?>
