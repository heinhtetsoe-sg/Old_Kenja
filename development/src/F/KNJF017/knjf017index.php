<?php

require_once('for_php7.php');

require_once('knjf017Model.inc');
require_once('knjf017Query.inc');

class knjf017Controller extends Controller {
    var $ModelClassName = "knjf017Model";
    var $ProgramID      = "KNJF017";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "changeGrade":
                case "changeForm":
                case "reset":
                    $this->callView("knjf017Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf017Ctl = new knjf017Controller;
?>
