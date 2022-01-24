<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd1232index.php,v 1.1 2011/04/14 12:23:50 nakamoto Exp $

require_once('knjd1232Model.inc');
require_once('knjd1232Query.inc');

class knjd1232Controller extends Controller {
    var $ModelClassName = "knjd1232Model";
    var $ProgramID      = "KNJD1232";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd1232Form1");
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
$knjd1232Ctl = new knjd1232Controller;
?>
