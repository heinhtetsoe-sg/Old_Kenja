<?php

require_once('for_php7.php');

require_once('knji024Model.inc');
require_once('knji024Query.inc');

class knji024Controller extends Controller {
    var $ModelClassName = "knji024Model";
    var $ProgramID      = "KNJI024";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
                    $this->callView("knji024Form1");
                    break 2;

                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knji024Ctl = new knji024Controller;
//var_dump($_REQUEST);
?>
