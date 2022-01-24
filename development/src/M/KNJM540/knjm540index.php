<?php

require_once('for_php7.php');

require_once('knjm540Model.inc');
require_once('knjm540Query.inc');

class knjm540Controller extends Controller {
    var $ModelClassName = "knjm540Model";
    var $ProgramID      = "KNJM540";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                case "clear";
                case "change";
                case "yearchg";
                    $this->callView("knjm540Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm540Ctl = new knjm540Controller;
//var_dump($_REQUEST);
?>
