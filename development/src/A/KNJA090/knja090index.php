<?php

require_once('for_php7.php');

require_once('knja090Model.inc');
require_once('knja090Query.inc');

class knja090Controller extends Controller {

    var $ModelClassName = "knja090Model";
    var $ProgramID      = "KNJA090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "":
                case "list":
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knja090Form1");
                    break 2;
                case "subform1":
                    $this->callView("knja090SubForm1");
                    break 2;
                case "subform1_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->GetSubUpdateModel();
                    $sessionInstance->setCmd("subform1");
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
$knja090Ctl = new knja090Controller;
//var_dump($_REQUEST);
?>
