<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd123tindex.php 56586 2017-10-22 12:52:35Z maeshiro $

require_once('knjd123tModel.inc');
require_once('knjd123tQuery.inc');

class knjd123tController extends Controller {
    var $ModelClassName = "knjd123tModel";
    var $ProgramID      = "KNJD123T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "chaircd":
                case "reset":
                    $this->callView("knjd123tForm1");
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
$knjd123tCtl = new knjd123tController;
?>
