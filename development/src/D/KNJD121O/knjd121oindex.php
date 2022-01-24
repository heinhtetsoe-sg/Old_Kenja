<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd121oindex.php,v 1.1 2011/04/14 09:21:55 nakamoto Exp $

require_once('knjd121oModel.inc');
require_once('knjd121oQuery.inc');

class knjd121oController extends Controller {
    var $ModelClassName = "knjd121oModel";
    var $ProgramID      = "KNJD121O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd121oForm1");
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
$knjd121oCtl = new knjd121oController;
?>
