<?php

require_once('for_php7.php');

require_once('knja055Model.inc');
require_once('knja055Query.inc');

class knja055Controller extends Controller {
    var $ModelClassName = "knja055Model";
    var $ProgramID      = "KNJA055";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "change";
                case "main";
                    $this->callView("knja055Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knja055Ctl = new knja055Controller;
//var_dump($_REQUEST);
?>
