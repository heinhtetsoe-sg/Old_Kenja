<?php

require_once('for_php7.php');

require_once('knja063as1Model.inc');
require_once('knja063as1Query.inc');

class knja063as1Controller extends Controller {

    var $ModelClassName = "knja063as1Model";
    var $ProgramID      = "KNJA063AS1";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
//                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("list");
                    break 1;
                case "":
                case "list":
                case "copy":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knja063as1Form1");
                    break 2;
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
$knja063as1Ctl = new knja063as1Controller;
//var_dump($_REQUEST);
?>
