<?php

require_once('for_php7.php');

require_once('knjf014Model.inc');
require_once('knjf014Query.inc');

class knjf014Controller extends Controller {
    var $ModelClassName = "knjf014Model";
    var $ProgramID      = "KNJF014";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjf014Form1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
//                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
$knjf014Ctl = new knjf014Controller;
//var_dump($_REQUEST);
?>
