<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd121tindex.php 56586 2017-10-22 12:52:35Z maeshiro $

require_once('knjd121tModel.inc');
require_once('knjd121tQuery.inc');

class knjd121tController extends Controller {
    var $ModelClassName = "knjd121tModel";
    var $ProgramID      = "KNJD121T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd121tForm1");
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
$knjd121tCtl = new knjd121tController;
?>
