<?php

require_once('for_php7.php');

require_once('knjl010kModel.inc');
require_once('knjl010kQuery.inc');

class knjl010kController extends Controller {
    var $ModelClassName = "knjl010kModel";
    var $ProgramID      = "KNJL010K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "change_target":
                   $this->callView("knjl010kForm1");
                   break 2;
                case "execute":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "output":
                    if (!$sessionInstance->OutputTmpFile()) {
                        $this->callView("knjl010kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl010kForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJL010KCtl = new knjl010kController;

//var_dump($_REQUEST);
?>
