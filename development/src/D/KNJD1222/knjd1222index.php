<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd1222index.php,v 1.1 2011/04/14 12:23:50 nakamoto Exp $

require_once('knjd1222Model.inc');
require_once('knjd1222Query.inc');

class knjd1222Controller extends Controller {
    var $ModelClassName = "knjd1222Model";
    var $ProgramID      = "KNJD1222";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd1222Form1");
                   break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjd1222Ctl = new knjd1222Controller;
?>
