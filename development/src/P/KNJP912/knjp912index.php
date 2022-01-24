<?php

require_once('for_php7.php');

require_once('knjp912Model.inc');
require_once('knjp912Query.inc');

class knjp912Controller extends Controller {
    var $ModelClassName = "knjp912Model";
    var $ProgramID      = "KNJP912";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjp912Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
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
$knjp912Ctl = new knjp912Controller;
//var_dump($_REQUEST);
?>
