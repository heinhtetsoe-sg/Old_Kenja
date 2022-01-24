<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd212index.php 56580 2017-10-22 12:35:29Z maeshiro $

require_once('knjd212Model.inc');
require_once('knjd212Query.inc');

class knjd212Controller extends Controller {
    var $ModelClassName = "knjd212Model";
    var $ProgramID      = "KNJD212";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "grade":
                case "subclasscd":
                case "cancel":
                    $this->callView("knjd212Form1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
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
$knjd212Ctl = new knjd212Controller;
?>
