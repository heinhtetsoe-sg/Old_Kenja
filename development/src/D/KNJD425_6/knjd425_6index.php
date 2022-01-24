<?php

require_once('for_php7.php');

require_once('knjd425_6Model.inc');
require_once('knjd425_6Query.inc');

class knjd425_6Controller extends Controller {
    var $ModelClassName = "knjd425_6Model";
    var $ProgramID      = "KNJD425_6";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list_set":
                case "edit":
                case "set":
                case "clear":
                case "sort":
                    $sessionInstance->knjd425_6Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd425_6Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_5Form1", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT, "knjd425_5Form1", $sessionInstance->auth);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd425_6Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd425_6Ctl = new knjd425_6Controller;
?>
