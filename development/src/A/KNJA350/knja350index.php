<?php

require_once('for_php7.php');

require_once('knja350Model.inc');
require_once('knja350Query.inc');

class knja350Controller extends Controller {
    var $ModelClassName = "knja350Model";
    var $ProgramID      = "KNJA350";

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
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main";
                    $this->callView("knja350Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knja350Ctl = new knja350Controller;
//var_dump($_REQUEST);
?>
